package com.xresch.cfw.extensions.databases;

import java.rmi.AccessException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWResultSet;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQuerySourceDatabase extends CFWQuerySource {

	private String contextSettingsType;
	
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_TIMEZONE = "timezone";

	
	/******************************************************************
	 * Return the DB interface for the given environment ID.
	 ******************************************************************/
	public abstract DBInterface getDatabaseInterface(int environmentID);

	/******************************************************************
	 * Return true if the database connection allows updates.
	 ******************************************************************/
	public abstract boolean isUpdateAllowed(int environmentID);
	
	/******************************************************************
	 * Return the time zone for the given environment.
	 ******************************************************************/
	public abstract String getTimezone(int environmentID);

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceDatabase(CFWQuery parent, String contextSettingsType) {
		super(parent);
		
		this.contextSettingsType = contextSettingsType;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Time has to be added to the query manually using time functions.";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDBExtensions.PACKAGE_RESOURCE, "z_manual_source_database.html")
				.replaceAll("\\{sourcename\\}", this.uniqueName());
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		// if source name is given, list up to 50 available database environments
		if( helper.getCommandTokenCount() >= 2 ) {
			
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			for (Object envID : environmentMap.keySet() ) {

				Object envName = environmentMap.get(envID);
				
				JsonObject envJson = new JsonObject();
				envJson.addProperty("id", Integer.parseInt(envID.toString()));
				envJson.addProperty("name", envName.toString());
				String envJsonString = "environment="+CFW.JSON.toJSON(envJson)+" ";
				
				list.addItem(
					helper.createAutocompleteItem(
						""
					  , envJsonString
					  , "Environment: "+envName
					  , envJsonString
					)
				);
				
				i++;
				
				if((i % 10) == 0) {
					list = new AutocompleteList();
					result.addList(list);
				}
				if(i == 50) { break; }
			}
		}
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
						.setDescription("The SQL query to fetch the data.")
						.disableSanitization() //do not mess up the gorgeous queries
						.addValidator(new NotNullOrEmptyValidator())
				)
				.addField(
						CFWField.newString(FormFieldType.TEXT, FIELDNAME_ENVIRONMENT)
							.setDescription("The database environment to fetch the data from. Use Ctrl+Space in the query editor for content assist.")	
							.addValidator(new NotNullOrEmptyValidator())
					)
//				.addField(
//						CFWField.newString(FormFieldType.TEXT, FIELDNAME_TIMEZONE)
//							.setDescription("Parameter can be used to adjust time zone differences between epoch time and the database. See manual for list of available zones.")	
//					)
				
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)parameters.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(environmentString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(environmentString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				 environmentString = settingsObject.get("id").getAsInt()+"";
			}
		}
		
		int environmentID = Integer.parseInt(environmentString);
		
		//-----------------------------
		// Check Permissions
		if(this.parent.getContext().checkPermissions()) {
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new ParseException("Missing permission to fetch from the specified database environment with ID "+environmentID, -1);
			}
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		//-----------------------------
		// Resolve Query Params
		String query = (String)parameters.getField(FIELDNAME_QUERY).getValue();
		
		if(Strings.isNullOrEmpty(query)) {
			CFW.Messages.addWarningMessage("source: Please specify the parameter 'query' for the databese source.");
			return;
		}
		
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)parameters.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			CFW.Messages.addWarningMessage("source: Please specify the parameter 'environment' for the databese source(use Ctrl+Space for list of suggestions).");
			return;
		}
		
		if(environmentString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(environmentString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				 environmentString = settingsObject.get("id").getAsInt()+"";
			}
		}
		
		int environmentID = Integer.parseInt(environmentString);
		
		//-----------------------------
		// Check Permissions
		if(this.parent.getContext().checkPermissions()) {
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new AccessException("Missing permission to fetch from the specified database environment with ID "+environmentID);
			}
		}
		
		//-----------------------------
		// Resolve Timezone Offsets
//		TimeZone timezone;
//		String timezoneParam = (String)parameters.getField(FIELDNAME_TIMEZONE).getValue();
//		if(!Strings.isNullOrEmpty(timezoneParam)) {
//			timezone = TimeZone.getTimeZone(timezoneParam);
//			
//		}else {
//			timezone = TimeZone.getTimeZone(
//					Strings.nullToEmpty(this.getTimezone(environmentID))
//				);
//		}
//		
//		earliestMillis +=  timezone.getOffset(earliestMillis);
//		latestMillis +=  timezone.getOffset(latestMillis);
//		
//		query = query.replace("$earliest$", ""+earliestMillis)
//					 .replace("$latest$", ""+latestMillis)
//					 ;
		
		//-----------------------------
		// Resolve Environment & Fetch Data

		DBInterface dbInterface = this.getDatabaseInterface(environmentID);
		boolean isQueryOnly = !isUpdateAllowed(environmentID);
		
		if(dbInterface == null) { return; }
		
		//add limiting to getAsJSONArray()
		CFWResultSet cfwResult = new CFWSQL(dbInterface, null)
				.custom(query)
				.executeCFWResultSet(isQueryOnly);
		
		if(!cfwResult.isSuccess()) {
			return;
		}
		
		//-----------------------------
		// Fetch Query Result

		if(cfwResult.isResultSet()) {
			ResultSetAsJsonReader resultReader = cfwResult.toJSONReader();
			
			int recordCounter = 0;
			JsonObject object;
			while( (object = resultReader.next()) != null) {
				
				if( this.isLimitReached(limit, recordCounter)) { break; }
				
				outQueue.add(new EnhancedJsonObject(object));
			}
			
			return;
		}
		
		
		//-----------------------------
		// Update Count
		JsonObject object = new JsonObject();
		
		object.addProperty("UPDATE_COUNT", cfwResult.updateCount());
		if(cfwResult.updateCount() == -999){
			object.addProperty("MESSAGE", "seems like an error occured.");
		}
		outQueue.add(new EnhancedJsonObject(object));

	}

}

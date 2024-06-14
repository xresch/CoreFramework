package com.xresch.cfw.extensions.influxdb.query;

import java.rmi.AccessException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.extensions.influxdb.FeatureInfluxDB;
import com.xresch.cfw.extensions.influxdb.InfluxDBEnvironment;
import com.xresch.cfw.extensions.influxdb.InfluxDBEnvironmentManagement;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceInfluxQL extends CFWQuerySource {
	
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_DB = "db";

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceInfluxQL(CFWQuery parent) {
		super(parent);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "influxql";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from an InfluxDB using the old style InfluxQL syntax(SQL-like).";
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
		return CFW.Files.readPackageResource(FeatureInfluxDB.PACKAGE_RESOURCE, "manual_source_influxql.html");
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureInfluxDB.PERMISSION_INFLUXDB;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureInfluxDB.PERMISSION_INFLUXDB);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		// if source name is given, list up to 50 available environments
		helper.autocompleteContextSettingsForSource(InfluxDBEnvironment.SETTINGS_TYPE, result);
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
							.setDescription("The InfluxDB environment to fetch the data from. Use Ctrl+Space in the query editor for content assist.")	
							.addValidator(new NotNullOrEmptyValidator())
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, FIELDNAME_DB)
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
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE);
			
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
		// Resolve Query Params
		String dbName = (String)parameters.getField(FIELDNAME_DB).getValue();
		
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)parameters.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			CFW.Messages.addWarningMessage("source: Please specify the parameter 'environment' for the influxql source(use Ctrl+Space for list of suggestions).");
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
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new AccessException("Missing permission to fetch from the specified database environment with ID "+environmentID);
			}
		}
				
		//-----------------------------
		// Resolve Environment & Fetch Data
		InfluxDBEnvironment environment = InfluxDBEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { 
			this.parent.getContext().addMessageWarning("source "+uniqueName()+": environment with id "+environmentID+" seems not to exist.");
			return; 
		}
		
		JsonArray result = environment.queryRangeInfluxQLAsJsonArray(dbName, query, earliestMillis, latestMillis);
				
		//-----------------------------
		// Fetch Query Result

		if(result != null) {

			int recordCounter = 0;
			for(JsonElement record : result) {
				
				if( this.isLimitReached(limit, recordCounter)) { break; }
				
				outQueue.add(new EnhancedJsonObject(record.getAsJsonObject()));
				recordCounter++;
			}
			
			return;
		}

		

	}

}

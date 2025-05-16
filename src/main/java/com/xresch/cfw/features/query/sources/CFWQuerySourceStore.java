package com.xresch.cfw.features.query.sources;

import java.text.ParseException;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.commands.CFWQueryCommandParamDefaults;
import com.xresch.cfw.features.query.store.CFWStoredQuery;
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceStore extends CFWQuerySource {

	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_PARAMS = "params";

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceStore(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "store";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Executes a stored query and takes its results.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Time is taken from the time picker.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_store.html");
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return null;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		if( helper.getCommandTokenCount() >= 2 ) {
			
			JsonArray queryArray = CFW.DB.StoredQuery.getUserAndSharedStoredQueryList();
			
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			
			HashSet<Integer> encounteredIDs = new HashSet<>();
			for (JsonElement element : queryArray ) {

				JsonObject storedQuery = element.getAsJsonObject();
				
				JsonObject envJson = new JsonObject();
				int id = storedQuery.get(CFWStoredQueryFields.PK_ID.toString()).getAsInt();
				String queryName = storedQuery.get(CFWStoredQueryFields.NAME.toString()).getAsString();
				String query = storedQuery.get(CFWStoredQueryFields.QUERY.toString()).getAsString();

				//deduplicate
				if( !encounteredIDs.contains(id)) {
					encounteredIDs.add(id);
					
					envJson.addProperty("id", id);
					envJson.addProperty("name", queryName );
					String queryParamString = "query = "+CFW.JSON.toJSON(envJson)+" ";
					
					//-------------------------
					// Create Replacement
					String replacement = queryParamString;
					
					boolean queryParamsDefined = storedQuery.get(CFWStoredQueryFields.QUERY_PARAMS_DEFINED.toString()).getAsBoolean();
					if(queryParamsDefined) {
						String queryParams = storedQuery.get(CFWStoredQueryFields.QUERY_PARAMS.toString()).getAsString();
						replacement += "\n\tparams = "+queryParams;
					}
					
					//-------------------------
					// Add to Autocomplete
					list.addItem(
						helper.createAutocompleteItem(
							""
						  , replacement
						  , queryName
						  , queryParamString
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
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
						.setDescription("The stored query that should be executed (use Ctrl + Space for autocomplete).")
						.addValidator(new NotNullOrEmptyValidator())
				)
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_PARAMS)
						.setDescription("The parameters for the stored query.")
				)
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//do nothing
	}
	
	
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {			
		
		//-----------------------------
		// Resolve Stored Query ID
		String queryString = (String)parameters.getField(FIELDNAME_QUERY).getValue();

		if(queryString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(queryString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				queryString = settingsObject.get("id").getAsInt()+"";
			}
		}
		
		int queryID = Integer.parseInt(queryString);
		
		//-----------------------------
		// Resolve Parameters
		String paramsString = (String)parameters.getField(FIELDNAME_PARAMS).getValue();
		
		JsonObject paramsObject = new JsonObject();
		
		if(paramsString != null && paramsString.startsWith("{")) {
			paramsObject = CFW.JSON.fromJson(paramsString).getAsJsonObject();
		}
		
		//----------------------------------
		// Get Stored Query
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(queryID);
		String query = storedQuery.query();

		//----------------------------------
		// Create Subquery Context
		CFWQueryContext subqueryContext = this.getParent().getContext().createClone(false);
		
		// override check permissions 
		boolean checkPermissions = storedQuery.checkPermissions();
		if(subqueryContext.checkPermissions()) {
			subqueryContext.checkPermissions(checkPermissions);
		}
		
		subqueryContext.setParameters(paramsObject);

		//----------------------------------
		// Execute Query
		CFWQueryExecutor executor = new CFWQueryExecutor();
		CFWQueryResultList resultList = executor.parseAndExecuteAll(subqueryContext, query, null, null);
		
		for(int i = 0; i < resultList.size(); i++) {
			outQueue.addAll(resultList.get(i).getRecords());
		}
		
			
	}
}

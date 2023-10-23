package com.xresch.cfw.features.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIRequestHandler;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class APIQueryExecute extends APIDefinition{
	
	static final String FIELDNAME_TIME = "JSON_TIME";
	static final String FIELDNAME_QUERY = "QUERY";
	static final String FIELDNAME_JSON_OBJECT = "JSON_OBJECT";

	private static final String[] inputFieldnames = new String[] {FIELDNAME_TIME, FIELDNAME_QUERY};
	private static final String[] outputFieldnames = new String[] {FIELDNAME_JSON_OBJECT};
	
	public APIQueryExecute(String apiName, 
						      String actionName) {
		
		super(APIQueryExecuteFields.class, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setPostBodyParamName(FIELDNAME_QUERY);
		this.setDescription("Executes a CFWQL query and returns the results as a json object.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
				JSONResponse json = new JSONResponse();
				
				//----------------------------------
				// Validate Arguments
				if( Strings.isNullOrEmpty(bodyContents) ) {
					json.setSuccess(false);
					json.addAlert(MessageType.ERROR, "Please specify the parameter 'query' or add the query in the request body.");
					return;
				}
				
				APIQueryExecuteFields fieldsObject = new APIQueryExecuteFields();
				
				if( !fieldsObject.mapRequestParameters(request) ) {
					return;
				}
				
				//---------------------------------
				// Fetch Data, do not check permissions
				// as there are no user permission on APIs
				String query = bodyContents;
				long earliest = fieldsObject.getTimeframe().getEarliest();
				long latest = fieldsObject.getTimeframe().getLatest();
				int timezoneOffsetMinutes = fieldsObject.getTimeframe().getClientTimezoneOffset();
				
				CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(true);
				
				CFWQueryResultList resultArray = executor.parseAndExecuteAll(query, earliest, latest, timezoneOffsetMinutes);
				
				json.setPayload(resultArray.toJson());
				json.setSuccess(true);

			}
		});		
	}
}

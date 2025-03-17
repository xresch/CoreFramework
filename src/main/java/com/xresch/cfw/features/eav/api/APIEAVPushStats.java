package com.xresch.cfw.features.eav.api;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIRequestHandler;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class APIEAVPushStats extends APIDefinition{
	
	static final String FIELDNAME_VALUES = "VALUES";
	static final String FIELDNAME_JSON_OBJECT = "JSON_OBJECT";

	private static final String[] inputFieldnames = new String[] {FIELDNAME_VALUES};
	private static final String[] outputFieldnames = new String[] {FIELDNAME_JSON_OBJECT};
	
	public APIEAVPushStats(String apiName, 
						   String actionName) {
		
		super(APIEAVPushStatsFields.class, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setPostBodyParamName(FIELDNAME_VALUES);
		this.setDescription("Pushes query and returns the results as a json object.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
				JSONResponse json = new JSONResponse();
				
				//----------------------------------
				// Validate Arguments
				if( Strings.isNullOrEmpty(bodyContents) ) {
					json.setSuccess(false);
					json.addAlert(MessageType.ERROR, "Please specify the parameter 'values' or add the values in the request body.");
					return;
				}
				
				APIEAVPushStatsFields fieldsObject = new APIEAVPushStatsFields();
				
				if( !fieldsObject.mapRequestParameters(request) ) {
					return;
				}
				
				// Not needed, will be contained in bodyContents
				//String valuesString = (String)fieldsObject.getField(FIELDNAME_VALUES).getValue();
				
				JsonElement element = CFW.JSON.fromJson(bodyContents);
				if(!element.isJsonArray()) {
					json.setSuccess(false);
					json.addAlert(MessageType.ERROR, "Request body or parameter 'values' has to contain a JSON array.");
					return;
				}
				
				JsonArray array = element.getAsJsonArray();
				
				for(JsonElement current : array) {
					
					if(!current.isJsonObject()) { continue;}
					JsonObject object = current.getAsJsonObject();
					
					String type = object.get("type").getAsString();
					String category = object.get("category").getAsString();
					String entityName = object.get("entity").getAsString();
					BigDecimal value = object.get("value").getAsBigDecimal();
					
					LinkedHashMap<String,String> attributes = CFW.JSON.fromJsonLinkedHashMap(object.get("attributes").getAsJsonObject());

					if(type != null && type.trim().equalsIgnoreCase("counter") ) {
						CFW.DB.EAVStats.pushStatsCounter(category, entityName, attributes, value.intValue());
					}else {
						CFW.DB.EAVStats.pushStatsValue(category, entityName, attributes, value);
					}
				}
				
				
				json.setSuccess(true);

			}
		});		
	}
}

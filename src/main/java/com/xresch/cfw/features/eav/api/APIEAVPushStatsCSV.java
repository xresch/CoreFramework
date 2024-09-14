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
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIRequestHandler;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class APIEAVPushStatsCSV extends APIDefinition{
	
	static final String FIELDNAME_SEPARATOR = "SEPARATOR";
	static final String FIELDNAME_VALUES = "DATA";
	static final String FIELDNAME_JSON_OBJECT = "JSON_OBJECT";

	private static final String[] inputFieldnames = new String[] {FIELDNAME_SEPARATOR, FIELDNAME_VALUES};
	private static final String[] outputFieldnames = new String[] {FIELDNAME_JSON_OBJECT};
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public APIEAVPushStatsCSV(String apiName, 
						   String actionName) {
		
		super(APIEAVPushStatsCSVFields.class, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setPostBodyParamName(FIELDNAME_VALUES);
		this.setDescription("Takes the CSV data and stores them in the EAV statistics table. Data will be aggregated by the interval specified by an administrator.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
				JSONResponse json = new JSONResponse();
				
				//----------------------------------
				// Validate Arguments
				if( Strings.isNullOrEmpty(bodyContents) ) {
					json.setSuccess(false);
					json.addAlert(MessageType.ERROR, "Please specify the parameter 'data' or add the values in the request body.");
					return;
				}
				
				APIEAVPushStatsCSVFields fieldsObject = new APIEAVPushStatsCSVFields();

				if( !fieldsObject.mapRequestParameters(request) ) {
					return;
				}

				String separator = (String)fieldsObject.getField(FIELDNAME_SEPARATOR).getValue();

				String csv = bodyContents;

				JsonArray csvArray = CFW.CSV.toJsonArray(csv, separator, true, true); 
				

				for (JsonElement element : csvArray) {
					
					JsonObject object = element.getAsJsonObject();
					
					String category = object.get("category").getAsString();
					String entityName = object.get("entity").getAsString();
					JsonObject attributesObject = object.get("attributes").getAsJsonObject();
					LinkedHashMap<String,String> attributes = CFW.JSON.fromJsonLinkedHashMap(attributesObject);

					int count = 0;
					if(object.has("count")) { count = object.get("count").getAsInt(); }
					
					BigDecimal min = BigDecimal.ZERO;
					BigDecimal avg = BigDecimal.ZERO;
					BigDecimal max = BigDecimal.ZERO;
					BigDecimal sum = BigDecimal.ZERO;
					BigDecimal p50 = BigDecimal.ZERO;
					BigDecimal p95 = BigDecimal.ZERO;
					
					if(object.has("min")) {  min = object.get("min").getAsBigDecimal(); }
					if(object.has("avg")) {  avg = object.get("avg").getAsBigDecimal(); }
					if(object.has("max")) {  max = object.get("max").getAsBigDecimal(); }
					if(object.has("sum")) {  sum = object.get("sum").getAsBigDecimal(); }
					if(object.has("p50")) {  p50 = object.get("p50").getAsBigDecimal(); }
					if(object.has("p95")) {  p95 = object.get("p95").getAsBigDecimal(); }
					
					boolean success = CFW.DB.EAVStats.pushStatsCustom(
								category
								, entityName
								, attributes
								, count
								, min
								, avg
								, max
								, sum
								, p50
								, p95 
							);
					
					json.setSuccess(success);

				}	
			}
		}
		);		
	}
}

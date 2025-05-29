package com.xresch.cfw.features.query;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterFields;
import com.xresch.cfw.features.parameter.ParameterDefinition;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class ParameterQuerySelect extends ParameterDefinition {

	public static final String UNIQUE_NAME = "Query Select";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamUniqueName() { return UNIQUE_NAME; }

	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionShort() {
		return "Parameter that uses values from a query result to create a select field.";
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL, "parameter_"+UNIQUE_NAME.toLowerCase().replace(" ", "_")+".html");
	}
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = CFWField.newString(FormFieldType.QUERY_EDITOR, "JSON_QUERY_RESULT")
										 .disableSanitization();

		if(fieldValue != null) {
			settingsField.setValueConvert(fieldValue, true);
		}

		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes" })
	@Override
	public CFWField getFieldForWidget(
			  HttpServletRequest request
			, String dashboardid
			, Object parameterValue
			, CFWTimeframe timeframe
			, JsonObject userSelectedParamValues
			) {

		CFWField settingsField = 
				CFWField.newString(FormFieldType.SELECT, CFWParameterFields.VALUE)
						.addAttribute("defaultValue", "");

		if(parameterValue != null) {
			String query = parameterValue.toString();
			
			CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);

			//---------------------------------
			// Execute Query
			CFWQueryResultList resultArray = executor.parseAndExecuteAll(query, timeframe, userSelectedParamValues);
			if(resultArray.size() == 0) {
				return settingsField;
			}
			
			//---------------------------------
			// Validate Detected Fields
			CFWQueryResult firstResult = resultArray.getResultList().get(0);
			JsonArray detectedFields = firstResult.getDetectedFields();
			
			int fieldCount = detectedFields.size();
			if(fieldCount == 0) {
				return settingsField;
			}
			if(fieldCount > 2) {
				CFW.Messages.addErrorMessage("Parameter '"+getParamUniqueName()+"': Must return a result with either one or two columns. If two columns are present, first will be used as the value and second will be used as the label.");
				return settingsField;
			}
			
			//---------------------------------
			// Validate Has Results
			JsonArray results = firstResult.getRecordsAsJsonArray();
			if(results.size() == 0) {
				return settingsField;
			}
			
			//---------------------------------
			// Get options
			String firstFieldname = detectedFields.get(0).getAsString();
			String secondFieldname = (fieldCount == 1) ? null : detectedFields.get(1).getAsString();
			
			for(JsonElement result : results) {
				
				JsonObject object = result.getAsJsonObject();
				
				//--------------------------------------
				// First As String
				JsonElement firstValue = object.get(firstFieldname);
				String firstString = null;
				if		(firstValue.isJsonNull()) { firstString = ""; }
				else if	(firstValue.isJsonPrimitive()) { firstString = firstValue.getAsString(); }
				else 								   { firstString =  CFW.JSON.toJSON(firstValue); }
				
				if(fieldCount == 1) {
					settingsField.addOption(firstValue.getAsString());
				}else {
					
					//--------------------------------------
					// Second As String
					JsonElement secondValue = object.get(secondFieldname);
					String secondString = null;
					if		(secondValue.isJsonNull()) { secondString = ""; }
					else if	(secondValue.isJsonPrimitive()) { secondString = secondValue.getAsString(); }
					else 								   { secondString =  CFW.JSON.toJSON(secondValue); }
					
					settingsField.addOption(firstString, secondString);
				}
			}

		}			

		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isAvailable(HashSet<String> widgetTypesArray) {
		return true;
	}

}

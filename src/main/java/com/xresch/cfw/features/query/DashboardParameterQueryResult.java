package com.xresch.cfw.features.query;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinition;

public class DashboardParameterQueryResult extends ParameterDefinition {

	public static final String UNIQUE_NAME = "Query Select";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamUniqueName() { return UNIQUE_NAME; }

	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = CFWField.newString(FormFieldType.TEXTAREA, "JSON_QUERY_RESULT");

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
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe) {

		CFWField settingsField = 
				CFWField.newString(FormFieldType.SELECT, DashboardParameterFields.VALUE)
						.addAttribute("defaultValue", "");

		if(parameterValue != null) {
			String query = parameterValue.toString();
			
			CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);

			//---------------------------------
			// Execute Query
			JsonArray resultArray = executor.parseAndExecuteAll(query, timeframe);
			if(resultArray.size() == 0) {
				return settingsField;
			}
			
			//---------------------------------
			// Validate Detected Fields
			JsonObject firstResult = resultArray.get(0).getAsJsonObject();
			JsonArray detectedFields = firstResult.get(CFWQueryExecutor.RESULTFIELDS_DETECTED_FIELDS).getAsJsonArray();
			
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
			JsonArray results = firstResult.get(CFWQueryExecutor.RESULTFIELDS_RESULTS).getAsJsonArray();
			if(results.size() == 0) {
				return settingsField;
			}
			
			//---------------------------------
			// Get up to 256 options
			String firstFieldname = detectedFields.get(0).getAsString();
			String secondFieldname = (fieldCount == 1) ? null : detectedFields.get(1).getAsString();
			
			for(JsonElement result : results) {
				JsonObject object = result.getAsJsonObject();
				if(fieldCount == 1) {
					settingsField.addOption(object.get(firstFieldname).getAsString());
				}else {
					settingsField.addOption(
							  object.get(firstFieldname).getAsString()
							, object.get(secondFieldname).getAsString()
						);
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

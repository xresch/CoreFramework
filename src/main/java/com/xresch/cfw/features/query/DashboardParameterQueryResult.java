package com.xresch.cfw.features.query;

import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinition;

public class DashboardParameterQueryResult extends ParameterDefinition {

	public static final String LABEL = "Query Result";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamLabel() { return LABEL; }

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
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object fieldValue) {

		CFWField settingsField = CFWField.newString(FormFieldType.SELECT, DashboardParameterFields.VALUE);

		if(fieldValue != null) {
			String query = fieldValue.toString();
			
			System.out.println("query:"+query);
			
			LinkedHashMap<String, String> options = CFW.JSON.fromJsonLinkedHashMap(fieldValue.toString());
			settingsField.setOptions(options);
			
			CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
			
			//JsonArray resultArray = executor.parseAndExecuteAll(query, earliest, latest, timezoneOffsetMinutes);
			
			settingsField.setValueConvert(fieldValue, true);
			//response.setPayLoad(resultArray);	
		}			

		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isAvailable(HashSet<String> widgetTypesArray) {
		return true;
	}

}

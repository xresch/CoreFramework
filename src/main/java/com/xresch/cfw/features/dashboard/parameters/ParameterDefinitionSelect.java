package com.xresch.cfw.features.dashboard.parameters;

import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;

public class ParameterDefinitionSelect extends ParameterDefinition {

	public static final String LABEL = "Select";
	
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
		CFWField settingsField = CFWField.newValueLabel("JSON_VALUE");

		if(fieldValue != null) {
			settingsField.setValueConvert(fieldValue);
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

		if(fieldValue !=null) {
			LinkedHashMap<String, String> options = CFW.JSON.fromJsonLinkedHashMap(fieldValue.toString());
			settingsField.setOptions(options);
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

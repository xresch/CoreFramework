package com.xresch.cfw.features.parameter;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;

public class ParameterDefinitionBoolean extends ParameterDefinition {

	public static final String LABEL = "Boolean";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamUniqueName() { return LABEL; }

	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionShort() {
		return "Parameter that allows to select a boolean value.";
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureParameter.PACKAGE_MANUAL, "parameter_"+LABEL.toLowerCase()+".html");
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = CFWField.newBoolean(FormFieldType.BOOLEAN, LABEL)
										 .allowHTML(true);
		
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
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe, JsonObject  userSelectedParamValues) {

		return getFieldForSettings(request, dashboardid, parameterValue);
	}
	
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isDynamic() {
		return false;
	}
	
}

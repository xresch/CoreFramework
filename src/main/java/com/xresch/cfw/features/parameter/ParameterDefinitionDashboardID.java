package com.xresch.cfw.features.parameter;

import java.util.HashSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.logging.CFWLog;

public class ParameterDefinitionDashboardID extends ParameterDefinition {

	private static final Logger logger = CFWLog.getLogger(ParameterDefinitionDashboardID.class.getName());
	
	public static final String LABEL = "Dashboard ID";
	
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
		return "Parameter that returns the ID of the dashboard.";
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureParameter.PACKAGE_MANUAL, "parameter_"+LABEL.toLowerCase().replace(" ", "_")+".html");
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = 
				CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, LABEL)
				.allowHTML(true)
				.setValue(dashboardid)
				;
			
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

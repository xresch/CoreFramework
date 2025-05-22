package com.xresch.cfw.features.parameter;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterFields;

public class ParameterDefinitionDisplayOptions extends ParameterDefinition {

	public static final String LABEL = "Display Options";
	
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
		return "Allows to select from the list of available display options.";
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
		

		CFWField settingsField = CFWField.newString(FormFieldType.SELECT, "displayOptions")
						.setOptions( WidgetSettingsFactory.getDisplayAsOptions() )
						;
		
		if(fieldValue != null) {
			settingsField.setValue(fieldValue.toString());
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
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isAvailable(HashSet<String> widgetTypesArray) {
		return true;
	}

}

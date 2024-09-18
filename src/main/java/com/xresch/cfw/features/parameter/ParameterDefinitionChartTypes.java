package com.xresch.cfw.features.parameter;

import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.parameter.CFWParameter.DashboardParameterFields;
import com.xresch.cfw.datahandling.CFWTimeframe;

public class ParameterDefinitionChartTypes extends ParameterDefinition {

	public static final String LABEL = "Chart Types";
	
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
		return "Allows to select from the list of available chart types.";
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
		CFWField settingsField = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, "text")
						.setValue("area");

		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes" })
	@Override
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe) {

		CFWField settingsField = CFWField.newString(FormFieldType.SELECT, DashboardParameterFields.VALUE);

		settingsField.setOptions(FeatureCore.getChartTypes());

		return settingsField;
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

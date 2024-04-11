package com.xresch.cfw.extensions.databases.mssql;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.extensions.databases.oracle.FeatureDBExtensionsOracle;
import com.xresch.cfw.features.parameter.ParameterDefinition;

public class ParameterDefinitionMSSQLEnvironment extends ParameterDefinition {

	public static final String LABEL = "MSSQL Environment";
	
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
		return "Parameter that allows a user to select a MSSQL environment he has access too.";
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDBExtensionsMSSQL.PACKAGE_RESOURCE, "parameter_"+LABEL.toLowerCase().replace(" ", "_")+".html");
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = MSSQLSettingsFactory.createEnvironmentSelectorField();
				
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
		
		for(String type : widgetTypesArray) {
			if(type.contains("emp_mssql")) {
				return true;
			}
			
		}
		return false;
	}

}

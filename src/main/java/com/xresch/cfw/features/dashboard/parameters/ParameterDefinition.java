package com.xresch.cfw.features.dashboard.parameters;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;

public abstract class ParameterDefinition {
	

	/************************************************************
	 * Return a name for a category for this parameter
	 * @return String
	 ************************************************************/
	public abstract String getParamLabel();
	
	/************************************************************
	 * Creates the field for the Parameter Settings table.
	 * @param request
	 * @param dashboardid
	 * @param fieldValue 
	 * @return JSON string
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue);

	/************************************************************
	 * Creates the field for the Parameter Widget.
	 * @param request
	 * @param dashboardid
	 * @param fieldValue 
	 * @return JSON string
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object fieldValue);

	
	/************************************************************
	 * Return true if the parameter is available based on the
	 * given widget types.
	 * @param widgetTypesArray unique list of types.
	 * @return true if available
	 ************************************************************/
	public abstract boolean isAvailable(HashSet<String> widgetTypesArray);
			
}

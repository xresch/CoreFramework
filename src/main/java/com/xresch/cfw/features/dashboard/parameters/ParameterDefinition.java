package com.xresch.cfw.features.dashboard.parameters;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;

public abstract class ParameterDefinition {
	

	/************************************************************
	 * Return a name for a category for this parameter
	 * @return String
	 ************************************************************/
	public abstract String getParamLabel();
	
	/************************************************************
	 * Create a json response containing the data you need for 
	 * your widget.
	 * @param request TODO
	 * @return JSON string
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue);

	/************************************************************
	 * Create a json response containing the data you need for 
	 * your widget.
	 * @param request TODO
	 * @return JSON string
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object fieldValue);

	
}

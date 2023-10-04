package com.xresch.cfw.features.parameter;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWTimeframe;

public abstract class ParameterDefinition {
	

	/************************************************************
	 * Return a name for a category for this parameter
	 * @return String
	 ************************************************************/
	public abstract String getParamUniqueName();
	
	/************************************************************
	 * Return whether this parameter is static or dynamic:
	 *   - static: will use the value defined in the parameter
	 *     editor as the default value
	 *   - dynamic: will not have a default value as dynamic
	 *     values cannot be predicted.
	 *     
	 ************************************************************/
	public abstract boolean isDynamic();

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
	 * @param parameterValue the value defined for the parameter
	 * @param timeframe the timeframe chosen in the dashboard.
	 * @return CFW field
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe);

	/************************************************************
	 * Return true if the parameter is available based on the
	 * given widget types.
	 * @param widgetTypesArray unique list of types.
	 * @return true if available
	 ************************************************************/
	public abstract boolean isAvailable(HashSet<String> widgetTypesArray);
			
}

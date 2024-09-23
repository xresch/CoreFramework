package com.xresch.cfw.features.parameter;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
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
	
	/***********************************************************************************************
	 * Return a short description that can be shown in content assist and will be used as intro text
	 * in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionShort();
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();

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
	 * @param userSelectedParamValues TODO
	 * @return CFW field
	 ************************************************************/
	@SuppressWarnings({ "rawtypes"})
	public abstract CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe, JsonObject userSelectedParamValues);

	/************************************************************
	 * Return true if the parameter is available based on the
	 * given widget types.
	 * @param widgetTypesArray unique list of types.
	 * @return true if available
	 ************************************************************/
	public abstract boolean isAvailable(HashSet<String> widgetTypesArray);
			
}

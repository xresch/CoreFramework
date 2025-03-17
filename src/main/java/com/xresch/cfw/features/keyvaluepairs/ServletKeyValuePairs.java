package com.xresch.cfw.features.keyvaluepairs;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletKeyValuePairs extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletKeyValuePairs.class.getName());
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
				
		HTMLResponse html = new HTMLResponse("Configuration Management");
		
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			//--------------------------
			// Add Form
			content.append("<h1>Key Value Pairs</h1>");
			content.append("<p>This is a list of the key value pairs stored in the database.</p>");

			content.append("to be done");
			
			//--------------------------
			// Add Javascript
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureKeyValuePairs.RESOURCE_PACKAGE, "cfw_keyvaluepairs.js");
			html.addJavascriptData("categories", CFW.JSON.toJSON(CFW.DB.Config.getCategories()) );
			html.addJavascriptCode("cfw_config_changeToPanels();");

	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
		
}
package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletLogConfiguration extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
			HTMLResponse html = new HTMLResponse("Log Configuration");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_logconfiguration.js");
			
			html.addJavascriptCode("cfw_logconfiguration_draw();");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleActionRequest(request, response);
			}
		}else {
			CFW.Context.Request.addMessageAccessDenied();
		}
        
    }
	
	private void handleActionRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action").toLowerCase();
		String item = request.getParameter("item").toLowerCase();
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action) {

			case "fetch": 			
				switch(item) {
	  											
					case "loggers":		jsonResponse.getContent().append(getLoggerDetailsAsJSON().toString());
										break;		
	  											
					default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
										break;
				}
				break;
				
			case "update": 			
				switch(item) {
	  											
					case "loglevel":	String loggerName = request.getParameter("loggerName");
										String newLevel = request.getParameter("level");
										this.updateLogLevel(jsonResponse, loggerName, newLevel);
										break;	
										
					case "configfile":	CFWLog.initializeLogging();
										break;
										
					default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
										break;
				}
				break;		
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
	
	private JsonArray getLoggerDetailsAsJSON() {
		
		JsonArray array = new JsonArray();
		
		LogManager logManager = LogManager.getLogManager();
		Enumeration<String> loggerNames = logManager.getLoggerNames();
		
		while(loggerNames.hasMoreElements()) {
			
			String name = loggerNames.nextElement();
			Logger logger = logManager.getLogger(name);
			Level level = logger.getLevel();
			
			if(level != null) {
				JsonObject object = new JsonObject();
				object.addProperty("name", name);
				object.addProperty("level", level.getName());
				
				array.add(object);
			}
			
			
		}
		
		return array;
		
	}
	
	private void updateLogLevel(JSONResponse response, String loggerName, String level) {
				
		LogManager logManager = LogManager.getLogManager();
		Logger logger = logManager.getLogger(loggerName);
		
		logger.setLevel(Level.parse(level));
		
		response.setSuccess(true);

		
	}
			
}
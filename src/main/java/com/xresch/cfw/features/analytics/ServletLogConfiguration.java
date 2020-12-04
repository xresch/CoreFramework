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
import com.xresch.cfw._main.CFWMessages;
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
	private static final Logger logger = CFWLog.getLogger(ServletDatabaseAnalytics.class.getName());
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
			CFWMessages.accessDenied();
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
	  											
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;
				
			case "update": 			
				switch(item) {
	  											
					case "loglevel":	String loggerName = request.getParameter("loggerName");
										String newLevel = request.getParameter("level");
										this.setLogLevel(jsonResponse, loggerName, newLevel);
										break;	
										
					case "configfile":	new CFWLog(logger).audit("UPDATE", "LogLevel", "Reload log levels from logging.properties.");
										CFWLog.initializeLogging();
										break;
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;		
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	private JsonArray getLoggerDetailsAsJSON() {
		
		JsonArray array = new JsonArray();
		
		LogManager logManager = LogManager.getLogManager();
		Enumeration<String> loggerNames = logManager.getLoggerNames();
		
		while(loggerNames.hasMoreElements()) {
			
			String name = loggerNames.nextElement();
			//Do not use LogManager.getLogger() as it may return null
			Logger logger = Logger.getLogger(name);
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
	
	private void setLogLevel(JSONResponse response, String loggerName, String level) {
				
		//Do not use LogManager.getLogger() as it may return null
		Logger loggerToChange = Logger.getLogger(loggerName);
		
		new CFWLog(logger).audit("UPDATE", "LogLevel", "Change level of logger "+loggerName+" from "+loggerToChange.getLevel().toString()+" to "+level);
		loggerToChange.setLevel(Level.parse(level));
		
		response.setSuccess(true);

		
	}
			
}
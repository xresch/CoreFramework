package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletCacheStatistics extends HttpServlet
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
			HTMLResponse html = new HTMLResponse("Cache Statistics");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_cachestatistics.js");
			
			html.addJavascriptCode("cfw_cachestatistics_draw();");
			
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
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {

			case "fetch": 			
				switch(item.toLowerCase()) {
	  											
					case "cachestatistics":		jsonResponse.getContent().append(CFW.Caching.getCacheStatisticsAsJSON().toString());
												break;	
												
					case "cachedetails":		jsonResponse.getContent().append(CFW.Caching.getCacheDetailsAsJSON(request.getParameter("name")));
												break;								
												
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
						
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
		
}
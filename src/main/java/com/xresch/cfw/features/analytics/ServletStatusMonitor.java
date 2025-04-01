package com.xresch.cfw.features.analytics;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
public class ServletStatusMonitor extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		String action = request.getParameter("action");
		
		if(action == null) {
		HTMLResponse html = new HTMLResponse("Status Monitor");

		html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_statusmonitor.js");
		
		html.addJavascriptCode("cfw_statusmonitor_draw();");
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
		}else {
			handleActionRequest(request, response);
		}

    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleActionRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {

			case "fetch": 			
				switch(item.toLowerCase()) {
	  											
					case "statusmonitor":		jsonResponse.setPayload(CFWStatusMonitorRegistry.getStatusListCategorized());;
												break;	
																		
												
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
		
}
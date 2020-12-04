package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.db.CFWDBDefaultOperations;
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
public class ServletDatabaseAnalytics extends HttpServlet
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
			HTMLResponse html = new HTMLResponse("Database Analytics");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_dbanalytics.js");
			
			html.addJavascriptCode("cfw_dbanalytics_draw();");
			
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
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {
			case "dbsnapshot":		new CFWLog(logger).audit("CREATE", "DatabaseSnapshot", "Request manual database snapshot.");
									boolean isSuccess = CFW.DB.backupDatabaseFile("./snapshot", "h2_database_snapshot");
									if(isSuccess) {
										CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Snapshot created on hard disk under {APP_ROOT}/snapshot.");
									}else {
										CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error while creating snapshot.");
									}
									break;
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "tablerowcount": 		jsonResponse.getContent().append(CFW.DB.selectTableRowCountAsJSON());
	  											break;
	  											
					case "querystatistics":		jsonResponse.getContent().append(CFW.DB.selectQueryStatisticsAsJSON());
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
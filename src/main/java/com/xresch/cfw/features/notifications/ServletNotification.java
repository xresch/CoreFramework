package com.xresch.cfw.features.notifications;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 **************************************************************************************************************/
public class ServletNotification extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletNotification() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Jobs");
		
		if(CFW.Context.Request.hasPermission(FeatureNotifications.PERMISSION_NOTIFICATIONS_USER)) {
						
			String action = request.getParameter("action");
			
			if(action != null) {
				
				handleDataRequest(request, response);	
				
			}else {
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureNotifications.PACKAGE_RESOURCE, "cfw_jobs.js");
				
				html.addJavascriptCode("cfwjobs_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
					
			case "fetchpartial": 	
				
				String pagesize = request.getParameter("pagesize");
				String pagenumber = request.getParameter("pagenumber");
				String filterquery = request.getParameter("filterquery");
				String sortby = request.getParameter("sortby");
				String isAscendingString = request.getParameter("isascending");
				boolean isAscending = (isAscendingString == null || isAscendingString.equals("true")) ? true : false;
					
				switch(item.toLowerCase()) {
					case "notifications": 	jsonResponse.getContent().append(CFW.DB.Notifications.getPartialNotificationListAsJSONForUser(pagesize, pagenumber, filterquery, sortby, isAscending));
											break;
	  											  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
			
			case "fetch": 			
				switch(item.toLowerCase()) {

					case "unreadcount":		fetchUnreadCountForUser(jsonResponse);
											break;  
					
					case "unread":			fetchUnreadForUser(jsonResponse);
											break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "delete": 			
				switch(item.toLowerCase()) {

					case "single": 		deleteNotification(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
										
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void fetchUnreadCountForUser(JSONResponse jsonResponse) {
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void fetchUnreadForUser(JSONResponse jsonResponse) {
		
	}
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteNotification(JSONResponse jsonResponse, String ID) {
		
//		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
//			CFW.DB.Jobs.deleteByID(Integer.parseInt(ID));
//			return;
//		}
//		
//		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)
//		&& CFW.DB.Jobs.checkIsCurrentUserOwner(ID)) {
//			
//			CFW.DB.Jobs.deleteByID(Integer.parseInt(ID));
//			return;
//		}else {
//			CFW.Messages.noPermission();
//		}
		
	}

}
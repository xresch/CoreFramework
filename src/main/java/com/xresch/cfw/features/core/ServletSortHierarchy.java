package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWHierarchyConfig;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.spaces.Space;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * This servlet is used to handle the sorting of hierarchial CFWObjects.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSortHierarchy extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletSortHierarchy.class.getName());
	
	//name of type and associated SortConfig
	private static final HashMap<String, CFWHierarchyConfig> sortConfigMap = new HashMap<>();
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
    public static void addConfig(CFWHierarchyConfig config){
    	
    	if( sortConfigMap.containsKey(config.getConfigIdentifier()) ) {
    		new CFWLog(logger)
    		.severe("Error: A CFWHierarchyConfig with the type "+config.getConfigIdentifier()+" was already registered.");
    		return;
    	}
    	
		sortConfigMap.put(config.getConfigIdentifier(), config);
    }
	
    
	/***************************************************************************
	 * 
	 ***************************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		//--------------------------------------------
		// Execute Autocomplete Handler
		//--------------------------------------------
		String type = request.getParameter("type");
		String rootID = request.getParameter("rootid");

		CFWHierarchyConfig config = sortConfigMap.get(type);
		HTMLResponse html = new HTMLResponse("Sort Hierarchy");
		
		//--------------------------------------------
		// Check Inputs
		//--------------------------------------------
    	if( type == null || config == null) {
    		new CFWLog(logger)
	    		.severe("Error while attempting to sort the hierarchy: Type was not defined or config was not found.");
    		return;
    	}
		//--------------------------------------------
		// Execute Autocomplete Handler
		//--------------------------------------------
    	
		if(config != null && config.canAccessHierarchy(rootID)) {
			String action = request.getParameter("action");
			
			if(action == null) {

				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_sorthierarchy.js");
				
				html.addJavascriptCode("cfw_sorthierarchy_draw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response, config);
			}
		}else {
			CFW.Context.Request.addMessageAccessDenied();
		}
	
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response, CFWHierarchyConfig config) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String rootID = request.getParameter("rootid");
		String sortedElementID = request.getParameter("elementid");
		String targetParentID = request.getParameter("targetid");
		
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();
		
		//--------------------------------------
		// Check Permissions
//		if(config.canSort(sortedElementID, targetParentID)) {
//			if(!CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
//			   && !CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
//				CFW.Context.Request.addMessageNoPermission();
//				return;
//			}
//		}
		
		
		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "hierarchy": 			System.out.println("B"); fetchHierarchy(jsonResponse, rootID, config);
	  											break;
	  																						
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
									
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
	
	private void fetchHierarchy(JSONResponse jsonResponse, String rootID, CFWHierarchyConfig config) {
		
		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryField().getName();
		CFWObject parentObject = instance.select()
									.where(primaryFieldName, rootID)
									.getFirstObject();
				
		CFWHierarchy hierarchy = new CFWHierarchy(parentObject)
				.fetchAndCreateHierarchy(config.getFieldsToRetrieve());
		
		jsonResponse.getContent().append(hierarchy.toJSONArray().toString());
	}
	   

}
package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWHierarchyConfig;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.spaces.Space;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * This servlet is used to handle the sorting of hierarchial CFWObjects.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletHierarchy extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletHierarchy.class.getName());
	
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
		String configid = request.getParameter("configid");
		String rootID = request.getParameter("rootid");

		CFWHierarchyConfig config = sortConfigMap.get(configid);
		JSONResponse json = new JSONResponse();
		
		//--------------------------------------------
		// Check Inputs
		//--------------------------------------------
    	if( configid == null || config == null) {
    		new CFWLog(logger)
	    		.severe("Error while attempting to sort the hierarchy: Type was not defined or config was not found.");
    		return;
    	}
    	
		//--------------------------------------------
		// Execute Autocomplete Handler
		//--------------------------------------------
    	
		if(config.canAccessHierarchy(rootID)) {
			String action = request.getParameter("action");
			
			if(action != null) {
				handleDataRequest(request, response, config, json);
			}else {
				new CFWLog(logger).severe("Action was not defined.");
			}
		}else {
			CFWMessages.accessDenied();
		}
	
    }
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private void handleDataRequest( HttpServletRequest request, 
									HttpServletResponse response, 
									CFWHierarchyConfig config, 
									JSONResponse json) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String rootID = request.getParameter("rootid");
		String childID = request.getParameter("childid");
		String parentID = request.getParameter("parentid");
		
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
					case "hierarchy": 			fetchHierarchy(jsonResponse, rootID, config);
	  											break;
	  																						
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "update": 			
				switch(item.toLowerCase()) {
					case "parent": 				updateParent(jsonResponse, config, parentID, childID);
	  											break;
					case "childposition": 		Boolean moveUp = Boolean.parseBoolean(request.getParameter("moveup"));
												updateChildPosition(jsonResponse, config, childID, moveUp);
												break;																	
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;		
			
			default: 			CFW.Messages.actionNotSupported(action);
								break;
													
		}
	}
	
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private void updateParent(JSONResponse jsonResponse, CFWHierarchyConfig config, String parentID, String childID) {
		
		if(CFWHierarchy.updateParent(config, parentID, childID)) {
			jsonResponse.setSuccess(true);
			CFW.Messages.saved();
		}else{
			// error messages are created by CFWHierarchy.updateParent();
			jsonResponse.setSuccess(false);
		};
		
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private void updateChildPosition(JSONResponse jsonResponse, CFWHierarchyConfig config, String childID, Boolean moveUp) {
		
		if(CFWHierarchy.updatePosition(config, Integer.parseInt(childID), moveUp)) {
			jsonResponse.setSuccess(true);
			CFW.Messages.saved();
		}else{
			// error messages are created by CFWHierarchy.updatePosition();
			jsonResponse.setSuccess(false);
		};
		
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fetchHierarchy(JSONResponse jsonResponse, String rootID, CFWHierarchyConfig config) {
		
		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryKeyField().getName();
		CFWObject parentObject = null;
		
		if(rootID != null) {
			parentObject = instance.select()
				.where(primaryFieldName, rootID)
				.getFirstAsObject();
		}else {
			//---------------------------------------
			// Fetch All elements in table by using
			// primary key null
			parentObject = instance;
			parentObject.getPrimaryKeyField().setValue(null);
		}
											
		CFWHierarchy hierarchy = new CFWHierarchy(parentObject)
				.fetchAndCreateHierarchy(config.getFieldsToRetrieve());
		
		jsonResponse.getContent().append(hierarchy.toJSONArray().toString());
	}
	   

}
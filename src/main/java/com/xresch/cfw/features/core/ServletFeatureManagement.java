package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class ServletFeatureManagement extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletFeatureManagement.class.getName());
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
				
		HTMLResponse html = new HTMLResponse("Feature Management");
		
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
				
					//--------------------------
					// Add Javascript
					html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_featuremgmt.js");
					html.addJavascriptCode("cfw_feature_mgmt_createFeatureToggle();");
	
			        response.setContentType("text/html");
			        response.setStatus(HttpServletResponse.SC_OK);
						        
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}

    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String managedName = request.getParameter("name");
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "featurelist": 	jsonResponse.getContent().append(getManagedFeaturesJSON());
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
				
			case "update": 			
				switch(item.toLowerCase()) {
					case "feature": 		toggleFeatureActive(jsonResponse, managedName);
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
			
		}
	}
	
	private JsonArray getManagedFeaturesJSON() {
		
		ArrayList<CFWAppFeature> allFeatures = CFW.Registry.Features.getFeatureInstances();
		
		JsonArray resultArray = new JsonArray();
		for(CFWAppFeature feature : allFeatures) {
			if(feature.getNameForFeatureManagement() != null) {
				JsonObject current = new JsonObject();
				current.addProperty("NAME", feature.getNameForFeatureManagement() );
				current.addProperty("DESCRIPTION", feature.getDescriptionForFeatureManagement() );
				current.addProperty("VALUE", feature.isFeatureEnabled() );
				resultArray.add(current);
			}
		}
		
		return resultArray;
	}
	
	private void toggleFeatureActive(JSONResponse jsonResponse, String managedName) {
		
		boolean oldStatus = CFW.DB.KeyValuePairs.getValueAsBoolean(CFWAppFeature.KEY_VALUE_PREFIX+managedName);
		
		KeyValuePair dbEntry = CFW.DB.KeyValuePairs.selectByKey(CFWAppFeature.KEY_VALUE_PREFIX+managedName);
		dbEntry.value(!oldStatus+"");
		
		if(CFW.DB.KeyValuePairs.update(dbEntry)) {
			CFW.Context.Request.addAlertMessage(MessageType.INFO, "Changes will only take effect after application restart.");
		}else {
			jsonResponse.setSuccess(false);
		}
		
	}
		
}
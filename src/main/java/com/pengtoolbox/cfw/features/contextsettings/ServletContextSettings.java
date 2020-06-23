package com.pengtoolbox.cfw.features.contextsettings;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.datahandling.CFWFormHandler;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.utils.CFWArrayUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletContextSettings extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletContextSettings() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
   protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
   {
		HTMLResponse html = new HTMLResponse("Context Settings");
		
		if(CFW.Context.Request.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {

				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureContextSettings.RESOURCE_PACKAGE, "cfw_contextsettings.js");
				html.addJavascriptCode("cfw_contextsettings_initialDraw();");
				
				//--------------------
				// Add Types
				ArrayList<String> typesArray = CFW.Registry.ContextSettings.getContextSettingTypes();
				String typesSeparated = String.join(",", typesArray.toArray(new String[] {}));
				html.addJavascriptData("types", typesSeparated);
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
		String ID = request.getParameter("id");
		String IDs = request.getParameter("ids");
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "contextsettings": 	jsonResponse.getContent().append(CFW.DB.ContextSettings.getContextSettingsListAsJSON());
	  											break;							
	  																					
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
			
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "contextsettings": 	duplicateContextSettings(jsonResponse, ID);
												break;  
										
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;	
			case "delete": 			
				switch(item.toLowerCase()) {

					case "contextsettings": 	deleteContextSettings(jsonResponse, ID);
												break;  
										
					default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
										break;
				}
				break;	
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editcontextsettings": 	createEditContextSettingsForm(jsonResponse, ID);
													break;
					case "createcontextsettings": 	createCreateContextSettingsForm(jsonResponse, request.getParameter("type"));
													break;							
									
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
						
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteContextSettings(JSONResponse jsonResponse, String id) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			
			ContextSettings settings = CFW.DB.ContextSettings.selectByID(id);
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(settings.type());
			typeSettings.mapJsonFields(settings.settings());
			if(typeSettings.isDeletable(settings.id())) {
				jsonResponse.setSuccess(CFW.DB.ContextSettings.deleteByID(id));
			}
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateContextSettings(JSONResponse jsonResponse, String contextsettingsID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			ContextSettings duplicate = CFW.DB.ContextSettings.selectByID(contextsettingsID);
			duplicate.id(null);
			duplicate.name(duplicate.name()+"(Copy)");
			
			Integer newID = CFW.DB.ContextSettings.createGetPrimaryKey(duplicate);
			if(newID == null) {
				jsonResponse.setSuccess(false);
			}
			
		}else {
			jsonResponse.setSuccess(false);
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient permissions to duplicate the context settings.");
		}
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private void createCreateContextSettingsForm(JSONResponse json, String type) {
				
		//--------------------------------------
		// Create ContextSettings Form
		if(CFW.Context.Request.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			
			//--------------------------------
			// Create settings instance 
			ContextSettings settings = new ContextSettings();
			settings.type(type);
			
			//--------------------------------
			// Create instance for type
			CFWObject typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(type);
			settings.addFields(typeSettings.getFields());
			
			//--------------------------------
			// Create Form
			CFWForm createContextSettingsForm = settings.toForm("cfwCreateContextSettingsForm"+CFW.Security.createRandomStringAtoZ(12),
																	"{!cfw_core_add!}");
			
			createContextSettingsForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {

						//--------------------------------
						// Create settings instance 
						ContextSettings settings = new ContextSettings();
						
						boolean areFieldsValid = settings.mapRequestParameters(request);
						
						//--------------------------------
						// Create instance for type
						CFWObject typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(type);
						
						if(typeSettings.mapRequestParameters(request) && areFieldsValid) {
							settings.settings(typeSettings.toJSONEncrypted());
	
							if( CFW.DB.ContextSettings.createGetPrimaryKey(settings) != null ) {
								CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Context Settings created successfully!");
							}
						}
					}
					
				}
			});
			
			createContextSettingsForm.appendToPayload(json);
			json.setSuccess(true);	
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditContextSettingsForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			ContextSettings settings = CFW.DB.ContextSettings.selectByID(Integer.parseInt(ID));
			
			//--------------------------------
			// Create instance for type
			CFWObject typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(settings.type());
			typeSettings.mapJsonFields(settings.settings());
			// reduce overhead
			settings.settings("");
			settings.addFields(typeSettings.getFields());
			
			if(settings != null) {
				
				CFWForm editContextSettingsForm = settings.toForm("cfwEditContextSettingsForm"+ID, "Update ContextSettings");
				
				editContextSettingsForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						if(origin != null) {

							//--------------------------------
							// Create settings instance 
							ContextSettings settings = new ContextSettings();
							
							boolean areFieldsValid = settings.mapRequestParameters(request);
							
							//--------------------------------
							// Create instance for type
							CFWObject typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(settings.type());
							
							if(typeSettings.mapRequestParameters(request) && areFieldsValid) {
								settings.settings(typeSettings.toJSONEncrypted());
		
								if( CFW.DB.ContextSettings.update(settings) ) {
									CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Update successful!");
								}
							}
						}
						
					}
				});
				
				editContextSettingsForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient permissions to execute action.");
		}
	}
	

}
package com.xresch.cfw.features.contextsettings;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletContextSettings extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	
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
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
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
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "contextsettings": 	jsonResponse.getContent().append(CFW.DB.ContextSettings.getContextSettingsListAsJSON());
	  											break;							
	  																					
					default: 					CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
												break;
				}
				break;
			
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "contextsettings": 	duplicateContextSettings(jsonResponse, ID);
												break;  
										
					default: 					CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
												break;
				}
				break;	
			case "delete": 			
				switch(item.toLowerCase()) {

					case "contextsettings": 	deleteContextSettings(jsonResponse, ID);
												break;  
										
					default: 			CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
										break;
				}
				break;	
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editcontextsettings": 	createEditContextSettingsForm(jsonResponse, ID);
													break;
					case "createcontextsettings": 	createCreateContextSettingsForm(jsonResponse, request.getParameter("type"));
													break;							
									
					default: 					CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
												break;
				}
				break;
						
			default: 			CFW.Messages.addErrorMessage("The action '"+action+"' is not supported.");
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
			typeSettings.mapJsonFields(settings.settings(), true, true);
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
			CFW.Messages.addErrorMessage("Insufficient permissions to duplicate the context settings.");
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
			CFWForm createContextSettingsForm = settings.toForm("cfwCreateContextSettingsForm"+CFWRandom.stringAlphaNumSpecial(12),
																	"{!cfw_core_add!}");
			
			createContextSettingsForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {

						//--------------------------------
						// Create settings instance 
						ContextSettings settings = new ContextSettings();
						
						boolean areFieldsValid = settings.mapRequestParameters(request);
						
						if(areFieldsValid) {
							//--------------------------------
							// Create instance for type
							CFWObject typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(type);
							
							if(typeSettings.mapRequestParameters(request) && areFieldsValid) {
								settings.settings(typeSettings.toJSONEncrypted());
		
								if( CFW.DB.ContextSettings.createGetPrimaryKey(settings) != null ) {
									CFW.Messages.addSuccessMessage("Context Settings created successfully!");
								}
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
			typeSettings.mapJsonFields(settings.settings(), true, true);
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
									CFW.Messages.addSuccessMessage("Update successful!");
								}
							}
						}
						
					}
				});
				
				editContextSettingsForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Messages.addErrorMessage("Insufficient permissions to execute action.");
		}
	}
	

}
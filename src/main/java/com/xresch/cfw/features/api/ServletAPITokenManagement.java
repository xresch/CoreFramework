package com.xresch.cfw.features.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 **************************************************************************************************************/
public class ServletAPITokenManagement extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletAPITokenManagement() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("API Token Management");
		
		if(CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_APITOKEN_MGMT)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {

				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureAPI.RESOURCE_PACKAGE, "cfw_apitokenmgmt.js");
				
				html.addJavascriptCode("cfw_apitokenmgmt_draw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
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
					case "tokenlist": 		jsonResponse.getContent().append(APITokenDBMethods.getTokenListAsJSON());
	  										break;
	  										
					case "permissionmap": 	String tokenID = request.getParameter("tokenid");
											jsonResponse.getContent().append(APITokenPermissionMapDBMethods.getPermissionMapForTokenID(tokenID));
											break;
											
					default: 				CFW.Messages.itemNotSupported(item);
											break;	
				}
				break;
						
			case "delete": 			
				switch(item.toLowerCase()) {

					case "token": 		deleteToken(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "token": 	 	duplicateToken(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "update": 			
				switch(item.toLowerCase()) {
												
					case "permissionmap": 		String tokenID = request.getParameter("tokenid");
												String permissionID = request.getParameter("permissionid");
												jsonResponse.setSuccess(APITokenPermissionMapDBMethods.tooglePermissionForToken(permissionID, tokenID));
												break;
	
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "edittoken": 	createEditForm(jsonResponse, ID);
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
	private void deleteToken(JSONResponse jsonResponse, String ID) {
		APITokenDBMethods.deleteByID(Integer.parseInt(ID));
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateToken(JSONResponse jsonResponse, String id) {
		APITokenDBMethods.duplicateByID(id);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
				
		CFWForm createTokenForm = new APIToken().toForm("cfwCreateTokenForm", "Create Token");
		
		createTokenForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
								
				if(origin != null) {
					if(origin.mapRequestParameters(request)) {
						APIToken token = (APIToken)origin;
						token.foreignKeyCreator(CFW.Context.Request.getUser().id());
						if(APITokenDBMethods.create(token) ) {
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Token created successfully!");
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Don't forget to edit and add permissions to the token.");
						}
					}
				}
			}
		});
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditForm(JSONResponse json, String ID) {

		APIToken Token = APITokenDBMethods.selectByID(Integer.parseInt(ID));
		
		if(Token != null) {
			
			CFWForm editTokenForm = Token.toForm("cfwEditTokenForm"+ID, "Update Token");
			
			editTokenForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)) {
						
						if(APITokenDBMethods.update((APIToken)origin)) {
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
						}
							
					}
					
				}
			});
			
			editTokenForm.appendToPayload(json);
			json.setSuccess(true);	
		}

	}
}
package com.xresch.cfw.features.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletAPI extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletAPI.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		doHandling(request, response);
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		doHandling(request, response);
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	protected void doHandling( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String token = CFW.HTTP.getCFWAPIToken(request);
    	if(!Strings.isNullOrEmpty(token)) {
			handleAPIRequest(request, response);
    	}else {
    		handleUserBasedAPI(request, response);
    	}
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	protected void handleTokenBased( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		
		// curl -X GET "http://localhost:8888/app/api?apitoken=dias-fjkdlafjak&apiName=User&actionName=fetchData&PK_ID=1&"
		// curl -H "API-Token: Header-Token-5bn3jk5" -X GET "http://localhost:8888/app/api?apiName=User&actionName=fetchData&PK_ID=1&"
		
		String apiName = request.getParameter("apiName");
		String action = request.getParameter("actionName");
		
		//if(token hasPermissions){
			handleAPIRequest(request, response);
		//} else{
		// new JSONResponse
		// error message
		//}
			
		
	}
	/*****************************************************************
	 *
	 ******************************************************************/
	protected void handleUserBasedAPI( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{

		if(CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)) {
			String apiName = request.getParameter("apiName");
			String data = request.getParameter("overviewdata");
			
			//Use the name of the APIDefinition
			String form = request.getParameter("formName");
			

			
			if(apiName != null) {
				handleAPIRequest(request, response);
				return;
			}
			
			//--------------------------------
			// Return example form
			if(form != null) {
				createForm(request, response);
				return;
			}
			
			//--------------------------------
			// Return data for overview Page
			if(data != null) {
				JSONResponse json = new JSONResponse();
				json.getContent().append(CFW.Registry.API.getJSONArray());
				return;
			}
		
			//---------------------------
			// Create Overview Page
			createOverview(request, response);
		}else {
			@SuppressWarnings("unused")
			HTMLResponse html = new HTMLResponse("Error");
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/***************************************************************************************************
	 * 
	 * @param request
	 * @param response
	 ****************************************************************************************************/
	private void createOverview(HttpServletRequest request, HttpServletResponse response) {
		
		HTMLResponse html = new HTMLResponse("API");
		
		if(CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)) {

			//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_apioverview.js"));
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureAPI.RESOURCE_PACKAGE, "cfw_apioverview.js");
			
			html.addJavascriptCode("cfw_apioverview_draw();");
			html.addJavascriptData("id", CFW.Context.Request.getRequest().getSession().getId());
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);

		}else {

			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
	}
	
	
	/***************************************************************************************************
	 * 
	 * @param request
	 * @param response
	 ****************************************************************************************************/
	private void handleAPIRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String apiName = request.getParameter("apiName");
		String action = request.getParameter("actionName");

		//--------------------------------------
		// Get API Definition
		APIDefinition definition = CFW.Registry.API.getDefinition(apiName, action);
		
		JSONResponse json = new JSONResponse();
		if(definition == null) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The API definition could not be found - name: "+apiName+", action: "+action);
			json.setSuccess(false);
			return;
		}
		
		definition.getRequestHandler().handleRequest(request, response, definition);
		
	}
	
	/***************************************************************************************************
	 * 
	 * @param request
	 * @param response
	 ****************************************************************************************************/
	private void createForm(HttpServletRequest request, HttpServletResponse response) {
		
		String apiName = request.getParameter("formName");
		String action = request.getParameter("actionName");
		String callbackMethod = request.getParameter("callbackMethod");
		//--------------------------------------
		// Get API Definition
		APIDefinition definition = CFW.Registry.API.getDefinition(apiName, action);
		
		JSONResponse json = new JSONResponse();
		if(definition == null) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The API definition could not be found - name: "+apiName+", action: "+action);
			json.setSuccess(false);
			return;
		}
		
		//--------------------------------------
		// Create User Form
		CFWObject instance = definition.createObjectInstance();
		CFWForm sampleForm;
		if(instance != null) {
			sampleForm = instance.toForm("cfwAPIFormExample"+apiName+action, "Submit", definition.getInputFieldnames());
		}else {
			sampleForm = new CFWForm("cfwAPIFormExample"+apiName+action, "Submit");
		}
		
		sampleForm.isAPIForm(true);
		sampleForm.setResultCallback(callbackMethod);
		sampleForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
				
				definition.getRequestHandler().handleRequest(request, response, definition);
				
			}
		});
		
		sampleForm.appendToPayload(json);		
	}
	
}
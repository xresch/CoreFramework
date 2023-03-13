package com.xresch.cfw.features.api;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletAPI extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletAPI.class.getName());

	private static final Counter callCounter = Counter.build()
	         .name("cfw_api_calls_total")
	         .help("Number of calls to the API, without logins and calls made through the UI example forms.")
	         .register();
	
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
    		handleTokenBased(request, response, token);
    	}else {
    		handleUserBasedAPI(request, response);
    	}
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	protected void handleTokenBased( HttpServletRequest request, HttpServletResponse response, String token ) throws ServletException, IOException
	{

		JSONResponse json = new JSONResponse();
		
		//------------------------------------------
		// Check if the token is active
		if(!APITokenDBMethods.checkIsTokenActive(token)) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "This token is disabled or does not exist.");
			json.setSuccess(false);
			return;
		}
		
		//------------------------------------------
		// Check if the parameters are given correctly
		// else return list of permissions
		String apiName = request.getParameter("apiName");
		String actionName = request.getParameter("actionName");
		
		if(Strings.isNullOrEmpty(apiName) || Strings.isNullOrEmpty(actionName)) {
			ResultSet result = APITokenPermissionMapDBMethods.getPermissionsForToken(token);
						
			//----------------------------
			// Check For Result
			try {
				CFW.Context.Request.addAlertMessage(MessageType.INFO, "ApiName and/or action was not provided. List of permitted APIs for this token is returned.");
				JsonArray array = new JsonArray();
				while(result.next()) {
					String name = result.getString("API_NAME");
					String action = result.getString("ACTION_NAME");
					APIDefinition apidef = CFW.Registry.API.getDefinition(name, action);
					
					if(apidef != null) {
						array.add(apidef.getJSON());
					}
				}
				
				json.setPayLoad(array);
			} catch (SQLException e) {
				new CFWLog(logger).severe("SQLException occurred:"+e.getMessage(), e);
				json.setSuccess(false);
				return;
			}finally {
				CFW.DB.close(result);
			}
			json.setSuccess(true);
			return;
		}
		
		//------------------------------------------
		// Check if token has permission and handle
		// request
		if(APITokenPermissionMapDBMethods.checkHasTokenThePermission(token, apiName, actionName)){
			handleAPIRequest(request, response);
		}else {
			json.setSuccess(false);
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The token does not have access to the API "+apiName+"."+actionName+".");
		}
		
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
				json.setPayLoad(CFW.Registry.API.getJSONArray());
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
		
		callCounter.inc();
		
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
		
		//--------------------------------------
		// Get API Definition
		handleAPIRequest(request, response, definition);
	}
	
	/***************************************************************************************************
	 * 
	 * @param request
	 * @param response
	 ****************************************************************************************************/
	private void handleAPIRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition) {
		
		callCounter.inc();
		
		//--------------------------------------
		// Get Request Body
		String bodyContents = null;
		if(definition.getPostBodyParamName() != null) {
			Enumeration<String> paramNames = request.getParameterNames();
			String bodyParamName = definition.getPostBodyParamName();
			
			//------------------------
			// Make case insensitive
			while(paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();

				if(bodyParamName.equalsIgnoreCase(name)) {
					bodyParamName = name;
					break;
				}
			}
			
			//------------------------
			// Read Body Contents		
			
			bodyContents = request.getParameter(bodyParamName);
			if(Strings.isNullOrEmpty(bodyContents)) {
				bodyContents = CFW.HTTP.getRequestBody(request);
			}
		}
		
		//--------------------------------------
		// Handle Request
		definition.getRequestHandler().handleRequest(request, response, definition, bodyContents);
		
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
				handleAPIRequest(request, response, definition);
				
			}
		});
		
		sampleForm.appendToPayload(json);		
	}
	
}
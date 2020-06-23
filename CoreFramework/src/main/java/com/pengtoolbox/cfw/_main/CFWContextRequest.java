package com.pengtoolbox.cfw._main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.response.AbstractResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWContextRequest {
	
	private static InheritableThreadLocal<HttpServletRequest> httpRequest = new InheritableThreadLocal<HttpServletRequest>();
	private static InheritableThreadLocal<HttpServletResponse> httpResponse = new InheritableThreadLocal<HttpServletResponse>();
	
	private static InheritableThreadLocal<AbstractResponse> responseContent = new InheritableThreadLocal<AbstractResponse>();
	private static InheritableThreadLocal<SessionData> sessionData = new InheritableThreadLocal<SessionData>();
	
	private static InheritableThreadLocal<LinkedHashMap<String,AlertMessage>> messageArray = new InheritableThreadLocal<LinkedHashMap<String,AlertMessage>>();
		
	private static int localeFilesID = 0;
	private static HashMap<String, FileDefinition> localeFiles = new HashMap<String, FileDefinition>();
	
	public static void clearRequestContext() {
		httpRequest.set(null);
		responseContent.set(null);
		sessionData.set(null);
		messageArray.set(null);
		localeFilesID = 0;
	}
	
	public static HttpServletRequest getRequest() {
		return httpRequest.get();
	}

	public static void setRequest(HttpServletRequest request) {
		CFWContextRequest.httpRequest.set(request);
	}
	
	public static AbstractResponse getResponse() {
		return responseContent.get();
	}
	
	public static void setResponse(AbstractResponse response) {
		CFWContextRequest.responseContent.set(response);
	}
	
	public static void setHttpServletResponse(HttpServletResponse response) {
		CFWContextRequest.httpResponse.set(response);
	}
	
	public static HttpServletResponse getHttpServletResponse() {
		return httpResponse.get();
	}


	
	public static SessionData getSessionData() {
		return sessionData.get();
	}
	
	public static User getUser() {
		if(sessionData.get() != null) {
			return sessionData.get().getUser();
		}
		return null;
	}
	
	public static HashMap<String, Role> getUserRoles() {
		if(sessionData.get() != null) {
			return sessionData.get().getUserRoles();
		}
		return null;
	}
	
	public static HashMap<String, Permission> getUserPermissions() {
		if(sessionData.get() != null) {
			return sessionData.get().getUserPermissions();
		}
		return null;
	}
	
	public static boolean hasPermission(String permissionName) {
		
		if(!CFW.Properties.AUTHENTICATION_ENABLED) {
			return true;
		}
		
		if(getUserPermissions() != null && getUserPermissions().containsKey(permissionName)) {
			return true;
		}

		return false;
	}

	public static void setSessionData(SessionData sessionData) {
		CFWContextRequest.sessionData.set(sessionData);
	}

	/****************************************************************
	 * Adds a message to the message div of the template.
	 * Ignored if the message was already exists.
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static void addAlertMessage(MessageType type, String message){
		
		if(messageArray.get() == null) {
			messageArray.set(new LinkedHashMap<String,AlertMessage>());
		}
		
		messageArray.get().put(message, new AlertMessage(type, message));
				
	}
	
	/****************************************************************
	 * Returns a collection of alert Messages
	 *   
	 * @return Map or null
	 *   
	 ****************************************************************/
	public static LinkedHashMap<String,AlertMessage> getAlertMap() {
		return messageArray.get();
	}
	/****************************************************************
	 * Returns a collection of alert Messages
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static Collection<AlertMessage> getAlertMessages() {
		if(messageArray.get() == null) {
			return new ArrayList<AlertMessage>();
		}
		return messageArray.get().values();
	}
	
	/****************************************************************
	 * Rerturns a json string for the alerts.
	 * returns "[]" if the array is empty
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static String getAlertsAsJSONArray() {
		if(messageArray.get() == null) {
			return "[]";
		}

		return CFW.JSON.toJSON(messageArray.get().values());
	}
	
	

}

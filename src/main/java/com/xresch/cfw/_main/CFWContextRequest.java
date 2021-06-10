package com.xresch.cfw._main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWContextRequest {
	
	private static InheritableThreadLocal<HttpServletRequest> httpRequest = new InheritableThreadLocal<>();
	private static InheritableThreadLocal<HttpServletResponse> httpResponse = new InheritableThreadLocal<>();
	private static InheritableThreadLocal<Long> requestStartNanos = new InheritableThreadLocal<>();;
	
	private static InheritableThreadLocal<AbstractResponse> responseContent = new InheritableThreadLocal<>();
	private static InheritableThreadLocal<CFWSessionData> sessionData = new InheritableThreadLocal<>();
	
	private static InheritableThreadLocal<LinkedHashMap<String,AlertMessage>> messageArray = new InheritableThreadLocal<>();
		
	private static int localeFilesID = 0;
	private static HashMap<String, FileDefinition> localeFiles = new HashMap<>();
	
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
	
	public static long getRequestStartNanos() {
		return requestStartNanos.get();
	}

	public static void setRequestStartNanos(long value) {
		CFWContextRequest.requestStartNanos.set(value);
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


	
	public static CFWSessionData getSessionData() {
		return sessionData.get();
	}
	
	public static User getUser() {
		if(sessionData.get() != null) {
			return sessionData.get().getUser();
		}
		return null;
	}
	
	public static HashMap<Integer, Role> getUserRoles() {
		if(sessionData.get() != null) {
			return sessionData.get().getUserRoles();
		}
		return new HashMap<Integer, Role>();
	}
	
	public static boolean hasRole(int roleID) {
		
		if(!CFW.Properties.AUTHENTICATION_ENABLED) {
			return true;
		}
		
		if(getUserRoles() != null && getUserRoles().containsKey(roleID)) {
			return true;
		}

		return false;
	}
	
	public static HashMap<String, Permission> getUserPermissions() {
		if(sessionData.get() != null) {
			return sessionData.get().getUserPermissions();
		}
		return new HashMap<String, Permission>();
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

	public static void setSessionData(CFWSessionData sessionData) {
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
			messageArray.set(new LinkedHashMap<>());
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
			return new ArrayList<>();
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

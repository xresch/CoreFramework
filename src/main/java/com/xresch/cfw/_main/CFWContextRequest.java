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
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * This Context allows to access data related to the current request.
 * All the data is stored in a Thread local.
 * If you start another Thread, you have to propagate the data manually.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWContextRequest {
	
	private static final CFWContextRequest INSTANCE = new CFWContextRequest();
	private static ThreadLocal<CFWContextObject> context = new ThreadLocal<>();
	
	public class CFWContextObject{
		protected HttpServletRequest httpRequest = null;
		protected HttpServletResponse httpResponse = null;
		protected Long requestStartMillis = null;
		
		protected AbstractResponse responseContent = null;
		protected CFWSessionData sessionData = null;
		
		protected LinkedHashMap<String,CFWHTMLItemAlertMessage> messageArray = null;
		
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static CFWContextObject getContext() {
		if(context.get() == null) {
			context.set(INSTANCE.new CFWContextObject());
		}
		return context.get();
	}
	
	/**************************************************************************
	 * Use this method to propagate a context object to another thread.
	 **************************************************************************/
	public static void setContext(CFWContextObject newContext) {
		context.set(newContext);
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void clearRequestContext() {
		context.set(null);
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/	
	public static void clearMessages() {
		getContext().messageArray = null;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static HttpServletRequest getRequest() {
		return getContext().httpRequest;
	}

	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void setRequest(HttpServletRequest request) {
		getContext().httpRequest = request;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static long getRequestStartMillis() {
		return getContext().requestStartMillis;
	}

	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void setRequestStartMillis(long value) {
		getContext().requestStartMillis = value;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static AbstractResponse getResponse() {
		return getContext().responseContent;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void setResponse(AbstractResponse response) {
		getContext().responseContent = response;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void setHttpServletResponse(HttpServletResponse response) {
		getContext().httpResponse = response;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static HttpServletResponse getHttpServletResponse() {
		return getContext().httpResponse;
	}

	/**************************************************************************
	 * 
	 **************************************************************************/
	public static CFWSessionData getSessionData() {
		return getContext().sessionData;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static User getUser() {
		if(getContext().sessionData != null) {
			return getContext().sessionData.getUser();
		}
		return null;
	}
	
	
	/**************************************************************************
	 * Returns null if there is no user logged in.
	 **************************************************************************/
	public static Integer getUserID() {
		if(getContext().sessionData != null
		&& getContext().sessionData.getUser() != null) {
			return getContext().sessionData.getUser().id();
		}
		return null;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static HashMap<Integer, Role> getUserRoles() {
		if(getContext().sessionData != null) {
			return getContext().sessionData.getUserRoles();
		}
		return new HashMap<Integer, Role>();
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static boolean hasRole(int roleID) {
		
		if(!CFW.Properties.AUTHENTICATION_ENABLED) {
			return true;
		}
		
		if(getUserRoles() != null && getUserRoles().containsKey(roleID)) {
			return true;
		}

		return false;
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static HashMap<String, Permission> getUserPermissions() {
		if(getContext().sessionData != null) {
			return getContext().sessionData.getUserPermissions();
		}
		return new HashMap<String, Permission>();
	}
	
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static boolean hasPermission(String permissionName) {
		
		if(!CFW.Properties.AUTHENTICATION_ENABLED) {
			return true;
		}
		
		if(getUserPermissions() != null && getUserPermissions().containsKey(permissionName)) {
			return true;
		}

		return false;
	}

	/**************************************************************************
	 * 
	 **************************************************************************/
	public static void setSessionData(CFWSessionData sessionData) {
		getContext().sessionData = sessionData;
	}

	/****************************************************************
	 * Adds a message to the message div of the template.
	 * Ignored if the message was already exists.
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static void addAlertMessage(MessageType type, String message){
		
		if(getContext().messageArray == null) {
			getContext().messageArray = new LinkedHashMap<>();
		}
		
		getContext().messageArray.put(message, new CFWHTMLItemAlertMessage(type, message));
				
	}
	
	/****************************************************************
	 * Returns a collection of alert Messages
	 *   
	 * @return Map or null
	 *   
	 ****************************************************************/
	public static LinkedHashMap<String,CFWHTMLItemAlertMessage> getAlertMap() {
		if(getContext().messageArray == null) {
			getContext().messageArray = new LinkedHashMap<>();
		}
		return getContext().messageArray;
	}
	/****************************************************************
	 * Returns a collection of alert Messages
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static Collection<CFWHTMLItemAlertMessage> getAlertMessages() {
		if(getContext().messageArray == null) {
			return new ArrayList<>();
		}
		return getContext().messageArray.values();
	}
	
	/****************************************************************
	 * Rerturns a json string for the alerts.
	 * returns "[]" if the array is empty
	 *   
	 * @param alertType alert type from OMKeys
	 *   
	 ****************************************************************/
	public static String getAlertsAsJSONArray() {
		if(getContext().messageArray == null) {
			return "[]";
		}

		return CFW.JSON.toJSON(getContext().messageArray.values());
	}	

}

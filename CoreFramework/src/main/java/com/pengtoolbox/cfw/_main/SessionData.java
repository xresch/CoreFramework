package com.pengtoolbox.cfw._main;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.response.bootstrap.BTFooter;
import com.pengtoolbox.cfw.response.bootstrap.BTMenu;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class SessionData {
	
	private boolean isLoggedIn = false;
	private boolean hasManualPages = false;
	private User user = null;
	private HashMap<String, Role> userRoles = new HashMap<String, Role>();
	private HashMap<String, Permission> userPermissions = new HashMap<String, Permission>();
	private static LinkedHashMap<String,CFWForm> formMap = new LinkedHashMap<String,CFWForm>();
	
	private BTMenu menu;
	private BTFooter footer;
	
	public SessionData() {
		menu = CFW.Registry.Components.createMenuInstance(false);
		footer = CFW.Registry.Components.createDefaultFooterInstance();
	}
	
	public void triggerLogin() {
		isLoggedIn = true;
		menu = CFW.Registry.Components.createMenuInstance(true);
		footer = CFW.Registry.Components.createDefaultFooterInstance();
		if(user != null) {
			user.lastLogin(new Timestamp(System.currentTimeMillis())).update();
		}
	}
	
	public void triggerLogout() {
		isLoggedIn = false;
		
		menu = CFW.Registry.Components.createMenuInstance(false);
		footer = CFW.Registry.Components.createDefaultFooterInstance();
		user = null;
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
		
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		if(user != null) {
			this.user = user;
			this.userRoles = CFW.DB.Users.selectRolesForUser(user);
			this.userPermissions = CFW.DB.Users.selectPermissionsForUser(user);
		}
	}
	
	public void resetUser() {
		user = null;
	}
	
	
	
	public HashMap<String, Role> getUserRoles() {
		return userRoles;
	}

	public HashMap<String, Permission> getUserPermissions() {
		return userPermissions;
	}

	public BTMenu getMenu() {
		return menu;
	}
	
	public BTFooter getFooter() {
		return footer;
	}
	
	public void addForm(CFWForm form){
		
		//keep cached forms below 7 to prevent memory leaks
		while(formMap.size() > 7) {
			formMap.remove(formMap.keySet().toArray()[0]);
		}
		
		formMap.put(form.getFormID(), form);	
	}
	
	public void removeForm(CFWForm form){
		formMap.remove(form.getFormID(), form);	
	}
	
	public CFWForm getForm(String formID) {
		return formMap.get(formID);
	}
	
	public Collection<CFWForm> getForms() {
		return formMap.values();
	}
}

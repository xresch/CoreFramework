package com.xresch.cfw.features.usermgmt;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.Context;
import com.xresch.cfw._main.CFW.DB;
import com.xresch.cfw._main.CFW.Properties;
import com.xresch.cfw._main.CFW.Registry;
import com.xresch.cfw._main.CFW.Context.App;
import com.xresch.cfw._main.CFW.DB.Users;
import com.xresch.cfw._main.CFW.Registry.Components;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTMenu;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SessionData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean isLoggedIn = false;

	private User user = null;
	private String clientIP = "";
	private String sessionID = null;
	private HashMap<Integer, Role> userRoles = new HashMap<>();
	private HashMap<String, Permission> userPermissions = new HashMap<>();
	
	//formID and form
	private Cache<String, CFWForm> formCache = CacheBuilder.newBuilder()
			.initialCapacity(5)
			.maximumSize(20)
			.expireAfterAccess(CFW.DB.Config.getConfigAsInt(FeatureUserManagement.CONFIG_SESSIONTIMEOUT_USERS), TimeUnit.SECONDS)
			.build();
	
	private BTMenu menu;
	private BTFooter footer;
	
	public SessionData(String sessionID) {
		this.sessionID = sessionID;
		loadMenu(false);		
	}
	
	public void triggerLogin() {
		isLoggedIn = true;
		loadMenu(true);
		if(user != null) {
			user.lastLogin(new Timestamp(System.currentTimeMillis())).update();
		}
	}
	
	public void triggerLogout() {
		
		isLoggedIn = false;
		userRoles.clear();
		userPermissions.clear();
		formCache.invalidateAll();
		
		loadMenu(false);
		user = null;
		
		CFW.Context.App.getApp().removeSession(sessionID);
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	public void isLoggedIn(boolean isLoggedIn) {
		 this.isLoggedIn = isLoggedIn;
	}
	
	public String getSessionID() {
		return sessionID;
	}
		
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		if(user != null) {
			this.user = user;
			loadUserPermissions();
		}
	}
	
	public void loadUserPermissions() {
		if(user != null) {
			this.userRoles = CFW.DB.Users.selectRolesForUser(user);
			this.userPermissions = CFW.DB.Users.selectPermissionsForUser(user);
			loadMenu(true);
		}
	}
	
	public void loadMenu(boolean withUserMenu) {
		menu = CFW.Registry.Components.createMenuInstance(this, withUserMenu);
		footer = CFW.Registry.Components.createDefaultFooterInstance();
	}
	
	public void resetUser() {
		user = null;
	}
	
	public HashMap<Integer, Role> getUserRoles() {
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
		formCache.put(form.getFormID(), form);	
	}
	
	
	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public void removeForm(CFWForm form){
		formCache.invalidate(form.getFormID());	
	}
	
	public CFWForm getForm(String formID) {
		return formCache.getIfPresent(formID);
	}
	
	public Collection<CFWForm> getForms() {
		return formCache.asMap().values();
	}
}

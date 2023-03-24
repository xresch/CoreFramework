package com.xresch.cfw.features.usermgmt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTMenu;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWSessionData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(CFWSessionData.class.getName());
	
	private boolean isLoggedIn = false;

	private User user = null;
	private String clientIP = "";
	private String sessionID = null;
	private HashMap<Integer, Role> userRoles = new HashMap<>();
	private HashMap<String, Permission> userPermissions = new HashMap<>();
	
	private HashMap<String, String> customProperties = new HashMap<>();
	
	//formID and form
	protected Cache<String, CFWForm> formCache;
	
	protected BTMenu menu;
	protected BTFooter footer;

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public CFWSessionData(String sessionID) {
		initializeFormCache();
		this.sessionID = sessionID;
		loadMenu(false);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void triggerLogin() {
		initializeFormCache();
		
		isLoggedIn = true;
		loadMenu(true);
		if(user != null) {
			user.lastLogin(new Timestamp(System.currentTimeMillis())).update();
		}
				
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void triggerLogout() {
		
		isLoggedIn = false;
		
		// make new HashMaps instead of map.clear() to avoid some strange NullPointerExceptions that occurs for some strange reasons and I have absolutely no intention to now go and check why the hell this is happening, as it seems that it is caused by Jetty session handler, which stores a strange state into the database but I have no interest in finding out how to reproduce the issue, so I write this overly lengthy comment just to make sure you have something to laugh when you get to the end of this line. ;-P 
		userRoles = new HashMap<>();
		userPermissions = new HashMap<>();
		customProperties= new HashMap<>();
		
		formCache.invalidateAll();
		
		loadMenu(false);
		user = null;
		
		CFW.Context.App.getApp().removeSession(sessionID);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void isLoggedIn(boolean isLoggedIn) {
		 this.isLoggedIn = isLoggedIn;
	}
	

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void setCustom(String key, String value) {
		this.customProperties.put(key, value);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String getCustom(String key) {
		return this.customProperties.get(key);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String removeCustom(String key) {
		 return this.customProperties.remove(key);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String getSessionID() {
		return sessionID;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/	
	public User getUser() {
		return user;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void setUser(User user) {
		if(user != null) {
			this.user = user;
			loadUserPermissions();
		}
	}
	
	/***********************************************************************
	 * Loads the current users roles and permissions
	 ***********************************************************************/
	public void loadUserPermissions() {
		if(user != null) {
			loadUserPermissions(user.id());
		}
	}
	
	/***********************************************************************
	 * Load the roles for the specified user. Also sets the userID
	 * of the user set in the session data. This is needed to check for permissions
	 * based on userID when API Token is used. 
	 * This is used to load permissions for API tokens.
	 ***********************************************************************/
	public void loadUserPermissions(int userID) {
		
		if(user != null) {
			user.id(userID);
		}
		// use putAll() to not clear the HashMaps which are cached in classes CFWDBUserRoleMap/CFWDBRolePermissionMap
		this.userRoles = new HashMap<>();
		this.userRoles.putAll( CFW.DB.Users.selectRolesForUser(userID) );
		this.userPermissions = new HashMap<>();
		this.userPermissions.putAll( CFW.DB.Users.selectPermissionsForUser(userID) );
		loadMenu(true);
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void loadMenu(boolean withUserMenu) {
		menu = CFW.Registry.Components.createMenuInstance(this, withUserMenu);
		footer = CFW.Registry.Components.createDefaultFooterInstance();
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void resetUser() {
		user = null;
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public HashMap<Integer, Role> getUserRoles() {
		return userRoles;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public HashMap<String, Permission> getUserPermissions() {
		return userPermissions;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public BTMenu getMenu() {
		return menu;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public BTFooter getFooter() {
		return footer;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void addForm(CFWForm form){		
		formCache.put(form.getFormID(), form);	
	}
	

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String getClientIP() {
		return clientIP;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void initializeFormCache() {
		if(formCache == null) {
			formCache = CacheBuilder.newBuilder()
				.initialCapacity(5)
				.maximumSize(20)
				.expireAfterAccess(CFW.DB.Config.getConfigAsInt(FeatureUserManagement.CONFIG_SESSIONTIMEOUT_USERS), TimeUnit.SECONDS)
				.build();
		}
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void removeForm(CFWForm form){
		formCache.invalidate(form.getFormID());	
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public CFWForm getForm(String formID) {
		return formCache.getIfPresent(formID);
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public Collection<CFWForm> getForms() {
		return formCache.asMap().values();
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/	
	 private void writeObject(ObjectOutputStream oos) 
      throws IOException {
				
		//oos.defaultWriteObject();
		oos.writeObject(isLoggedIn);
		oos.writeObject(sessionID);
		oos.writeObject(clientIP);
		oos.writeObject(CFW.JSON.toJSON(customProperties));
		
		String username = null;
		if(user != null) {
			username = user.username();
			oos.writeObject(username);
		}else {
			oos.writeObject(null);
		}
		
		new CFWLog(logger).fine("Stored session state to DB: user="+username+", sessionID="+sessionID);
				 
    }

	/***********************************************************************
	 * 
	 ***********************************************************************/
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    	
    	//ois.defaultReadObject();
       
       this.isLoggedIn 		= (boolean) ois.readObject();
       this.sessionID 		= (String) ois.readObject();
       this.clientIP 		= (String) ois.readObject();
       this.customProperties = CFW.JSON.fromJsonLinkedHashMap((String)ois.readObject());
       
       String username		= (String) ois.readObject();
       if(isLoggedIn && username != null) {
    	   this.setUser(CFW.DB.Users.selectByUsernameOrMail(username));
    	   this.triggerLogin();
       }

       initializeFormCache();
       
       new CFWLog(logger).fine("Loaded session state from DB: user="+username+", sessionID="+sessionID);

    }
}

package com.xresch.cfw.features.contextsettings;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.contextsettings.ContextSettings.ContextSettingsFields;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBContextSettings {
	
	private static Class<ContextSettings> cfwObjectClass = ContextSettings.class;
	private static final String[] auditLogFieldnames = new String[] { ContextSettingsFields.PK_ID.toString(), ContextSettingsFields.CFW_CTXSETTINGS_TYPE.toString(), ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString()};
	
	private static final Logger logger = CFWLog.getLogger(CFWDBContextSettings.class.getName());
	
	private static ArrayList<ContextSettingsChangeListener> changeListeners = new ArrayList<ContextSettingsChangeListener>();
	
	// Cache with Type and List of Contexts
	// Will be cleared when the settings change.
	private static LinkedHashMap<String, ArrayList<AbstractContextSettings>> settingsCache = new LinkedHashMap<String, ArrayList<AbstractContextSettings>>();
	
	/********************************************************************************************
	 * Add a change listener that listens to config changes.
	 * 
	 ********************************************************************************************/
	public static void addChangeListener(ContextSettingsChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/********************************************************************************************
	 * Clear the Cache if the configuration changes.
	 * 
	 ********************************************************************************************/
	private static void clearCache() {
		settingsCache = new LinkedHashMap<String, ArrayList<AbstractContextSettings>>();
	}
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			ContextSettings settings = (ContextSettings)object;
			
			if(settings == null || settings.name().isEmpty()) {
				new CFWLog(logger)
					.severe("Please specify a name for the environment.", new Throwable());
				return false;
			}
			
			if(checkExistsIgnoreCurrent(settings)) {
				new CFWLog(logger)
					.severe("A setting of type '"+settings.type()+"' and the name '"+settings.name()+"' already exists.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			ContextSettings environment = (ContextSettings)object;
			
			if(environment == null ) {
				return false;
			}
			
			return true;
		}
	};
		
	
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static Integer createGetPrimaryKey(ContextSettings item) 		{ 
		
		Integer primaryKey = CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, auditLogFieldnames, item);
		
		if(primaryKey != null) {
			
			ContextSettings fromDB = CFW.DB.ContextSettings.selectByID(primaryKey);
			
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(fromDB.type());
			typeSettings.mapJsonFields(fromDB.settings(), true, true);
			typeSettings.setWrapper(fromDB);
			
			for(ContextSettingsChangeListener listener : changeListeners) {
				
				if(listener.listensOnType(item.type())) {					
					listener.onChange(typeSettings, true);
				}
			}
			
			clearCache();
		}
		return primaryKey;
	}
	
	/********************************************************************************************
	 * Creates a new configuration in the DB if the name was not already given.
	 * All newly created permissions are by default assigned to the Superuser Role.
	 * 
	 * @param settings with the values that should be inserted. ID will be set by the Database.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void oneTimeCreate(ContextSettings settings) {
		
		if(settings == null || Strings.isNullOrEmpty(settings.name()) ) {
			return;
		}

		if(!CFW.DB.ContextSettings.checkExists(settings)) {
			CFW.DB.ContextSettings.createGetPrimaryKey(settings);
		}

	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(ContextSettings item) 		{ 
		
		boolean success = CFWDBDefaultOperations.update(prechecksCreateUpdate, auditLogFieldnames, item);
		
		AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(item.type());
		typeSettings.mapJsonFields(item.settings(), true, true);
		typeSettings.setWrapper(item);
		
		//------------------------------------
		// If Active, Trigger Change, else Trigger Deactivate
		//------------------------------------
		if(item.isActive()) {
			for(ContextSettingsChangeListener listener : changeListeners) {
				
				if(listener.listensOnType(item.type())) {
					listener.onChange(typeSettings, false);
				}
			}
		}else {
			for(ContextSettingsChangeListener listener : changeListeners) {
				
				if(listener.listensOnType(item.type())) {
					listener.onDeleteOrDeactivate(typeSettings);
				}
			}
		}
		
		if(success) {
			clearCache();
		}
		
		return success;
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(String id) 					{ 
		clearCache(); 
		ContextSettings item = CFW.DB.ContextSettings.selectByID(id);
		
		boolean success = CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, ContextSettingsFields.PK_ID.toString(), id); 
				
		if(success) {
			clearCache();
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(item.type());
			typeSettings.mapJsonFields(item.settings(), true, true);
			typeSettings.setWrapper(item);
			
			for(ContextSettingsChangeListener listener : changeListeners) {
				
				if(listener.listensOnType(item.type())) {
					listener.onDeleteOrDeactivate(typeSettings);
				}
			}
		}
		
		return success;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static ContextSettings selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, ContextSettingsFields.PK_ID.toString(), id);
	}
	
	public static ContextSettings selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, ContextSettingsFields.PK_ID.toString(), id);
	}
	
	public static ContextSettings selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString(), name);
	}
	
	/***************************************************************
	 * Select a dashboard by it's ID and return it as JSON string.
	 * @param id of the dashboard
	 * @return Returns a dashboard or null if not found or in case of exception.
	 ****************************************************************/
	public static String getContextSettingsAsJSON(String id) {
		
		return new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "getContextSettingsAsJSON")
				.select()
				.where(ContextSettingsFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Returns context settings for the selected type.
	 * @param activeOnly TODO
	 * @param id of the dashboard
	 * @return Returns a dashboard or null if not found or in case of exception.
	 ****************************************************************/
	public static ArrayList<AbstractContextSettings> getContextSettingsForType(String type, boolean activeOnly) {
		
		if(settingsCache.containsKey(type)) {
			return settingsCache.get(type);
		}
		
		ArrayList<CFWObject> objects =  new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "getContextSettingsForType")
				.select()
				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE, type)
				.getAsObjectList();

		ArrayList<AbstractContextSettings> settingsArray = new ArrayList<AbstractContextSettings>();
		
		for(CFWObject object : objects) {
			ContextSettings current = (ContextSettings)object;
			
			if( !activeOnly || current.isActive()) {
				AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(current.type());
				
				typeSettings.mapJsonFields(current.settings(), true, true);
				typeSettings.setWrapper(current);
				settingsArray.add(typeSettings);
			}
		}
		
		settingsCache.put(type, settingsArray);
		return settingsArray;
	}
	
	
	/***************************************************************
	 * Returns a map with ID/Name values for select options for
	 * the current user
	 * @param type of the context setting
	 * @param options for user, empty map if user is null.
	 ****************************************************************/
	private static CFWSQL createQueryForTypeAndUser(String type, User user) {
				
		int userID = user.id();
		
		//----------------------------------
		// Create Query
		String restrictedUserslikeID = "%\""+userID+"\":%";
		
		CFWSQL query =  new CFWSQL(new ContextSettings())
				.loadSQLResource(FeatureContextSettings.RESOURCE_PACKAGE, "SQL_getContextSettingsForUser_PartialQuery.sql", 
						type,
						restrictedUserslikeID);
		
//		LinkedHashMap<Object, Object> objects =  new ContextSettings()
//				.queryCache(CFWDBContextSettings.class, "getSelectOptionsForType")
//				.select()
//				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE.toString(), type)
//				.orderby(ContextSettingsFields.CFW_CTXSETTINGS_NAME)
//				.getAsLinkedHashMap(ContextSettingsFields.PK_ID, ContextSettingsFields.CFW_CTXSETTINGS_NAME);
//		
		
		//------------------------------------
		// Add Filter by Role
		if(user.hasPermission(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS)) {
			//--------------------------------------
			// If has Context Settings permissions, 
			// always grant access
			query.or().custom("1 = 1");
		}else {
			//--------------------------------------
			// Filter by Roles
			Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
			for(int i = 0 ; i < roleArray.length; i++ ) {
				int roleID = roleArray[i];
	
				query.or().like(ContextSettingsFields.JSON_RESTRICTED_TO_GROUPS, "%\""+roleID+"\":%");
			}
		}

		query.custom(")")
			.orderby(ContextSettingsFields.CFW_CTXSETTINGS_NAME);
		
		return query;
	}
	
	/***************************************************************
	 * Select a dashboard by it's ID and return it as JSON string.
	 * @param id of the dashboard
	 * @return Returns a dashboard or null if not found or in case of exception.
	 ****************************************************************/
	public static ArrayList<AbstractContextSettings> getContextSettingsForTypeAndUser(String type, User user){
	
		ArrayList<AbstractContextSettings> settingsArray = new ArrayList<AbstractContextSettings>();
		
		if (user == null) { return settingsArray; }
		
		ArrayList<CFWObject> objects =  createQueryForTypeAndUser(type, user).getAsObjectList();
		
		for(CFWObject object : objects) {
			ContextSettings current = (ContextSettings)object;
			
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(current.type());
			
			typeSettings.mapJsonFields(current.settings(), true, true);
			typeSettings.setWrapper(current);
			settingsArray.add(typeSettings);
		}
		
		return settingsArray;
	}
	
	/***************************************************************
	 * Returns a map with ID/Name values for select options for
	 * the current user.
	 * 
	 * @param type of the context setting
	 * @param options for user, empty map if user is null.
	 ****************************************************************/
	public static HashMap<Integer, Object> getSelectOptionsForTypeAndUser(String type) {
		
		User user = CFW.Context.Request.getUser();

		return getSelectOptionsForTypeAndUser(type, user);
	}
	
	/***************************************************************
	 * Returns a map with ID/Name values for select options for
	 * the specified user.
	 * 
	 * @param type of the context setting
	 * @param options for user, empty map if user is null.
	 ****************************************************************/
	public static HashMap<Integer, Object> getSelectOptionsForTypeAndUser(String type, User user) {
		
		HashMap<Integer, Object> objects = new LinkedHashMap<>();
		
		if (user == null) { return objects; }
		
		objects = createQueryForTypeAndUser(type, user)
						.getAsIDValueMap(ContextSettingsFields.PK_ID, ContextSettingsFields.CFW_CTXSETTINGS_NAME);
		
		return objects;
	}
	
	/***************************************************************
	 * Return a list of all user dashboards
	 * 
	 * @return Returns a resultSet with all dashboards or null.
	 ****************************************************************/
	public static ResultSet getContextSettingsList() {
		
		return new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "getUserContextSettingsList")
				.select()
				.orderby(ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString())
				.getResultSet();
		
	}
	
	
	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getContextSettingsListAsJSON() {
		
		return new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "getUserContextSettingsListAsJSON")
				.select()
				.orderby(ContextSettingsFields.CFW_CTXSETTINGS_TYPE)
				.getAsJSON();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static JsonArray auditAccessToContextSettings(User user) {
		
		//-----------------------------------
		// Check User is Admin
		HashMap<String, Permission> permissions = CFW.DB.Permissions.selectPermissionsForUser(user);
		
		if( permissions.containsKey(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS) ) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Context Settings Administrator and has access to all settings.");
			JsonArray adminResult = new JsonArray(); 
			adminResult.add(adminObject);
			return adminResult;
		}
				
		return new CFWSQL(new ContextSettings())
			.queryCache()
			.loadSQLResource(FeatureContextSettings.RESOURCE_PACKAGE, "SQL_auditAccessToContextSettings.sql", 
					user.id())
			.getAsJSONArray();
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExists(ContextSettings settings) {
		return checkExists(settings.type(), settings.name());
	}
	
	public static boolean checkExists(String type, String name) {	
		int count = new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "checkExists")
				.selectCount()
				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE, type)
				.and(ContextSettingsFields.CFW_CTXSETTINGS_NAME, name)
				.limit(1)
				.executeCount();
		
		return (count > 0);
	}
	
	public static boolean checkExistsIgnoreCurrent(ContextSettings settings) {
		return checkExistsIgnoreCurrent(settings.id(), settings.type(), settings.name());
	}
	
	public static boolean checkExistsIgnoreCurrent(Integer currentID, String type, String name) {	
		int count = new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "checkExistsIgnoreCurrent")
				.selectCount()
				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE, type)
				.and(ContextSettingsFields.CFW_CTXSETTINGS_NAME, name)
				.and().not().is(ContextSettingsFields.PK_ID, currentID)
				.limit(1)
				.executeCount();
		
		return (count > 0);
	}

		
}

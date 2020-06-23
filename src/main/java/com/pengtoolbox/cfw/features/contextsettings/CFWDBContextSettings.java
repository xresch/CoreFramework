package com.pengtoolbox.cfw.features.contextsettings;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDBDefaultOperations;
import com.pengtoolbox.cfw.db.PrecheckHandler;
import com.pengtoolbox.cfw.features.contextsettings.ContextSettings.ContextSettingsFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBContextSettings {
	
	private static Class<ContextSettings> cfwObjectClass = ContextSettings.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBContextSettings.class.getName());
	
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
					.method("doCheck")
					.severe("Please specify a name for the environment.", new Throwable());
				return false;
			}
			
			if(checkExistsIgnoreCurrent(settings)) {
				new CFWLog(logger)
					.method("doCheck")
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
		
		Integer primaryKey = CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);
		
		if(primaryKey != null) {
			
			ContextSettings fromDB = CFW.DB.ContextSettings.selectByID(primaryKey);
			
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(fromDB.type());
			typeSettings.mapJsonFields(fromDB.settings());
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
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(ContextSettings item) 		{ 
		
		boolean success = CFWDBDefaultOperations.update(prechecksCreateUpdate, item);
		
		AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(item.type());
		typeSettings.mapJsonFields(item.settings());
		typeSettings.setWrapper(item);
		
		for(ContextSettingsChangeListener listener : changeListeners) {
			
			if(listener.listensOnType(item.type())) {
				listener.onChange(typeSettings, false);
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
		
		boolean success = CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, ContextSettingsFields.PK_ID.toString(), id); 
				
		if(success) {
			clearCache();
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(item.type());
			typeSettings.mapJsonFields(item.settings());
			typeSettings.setWrapper(item);
			
			for(ContextSettingsChangeListener listener : changeListeners) {
				
				if(listener.listensOnType(item.type())) {
					listener.onDelete(typeSettings);
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
	 * Select a dashboard by it's ID and return it as JSON string.
	 * @param id of the dashboard
	 * @return Returns a dashboard or null if not found or in case of exception.
	 ****************************************************************/
	public static ArrayList<AbstractContextSettings> getContextSettingsForType(String type) {
		
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
			AbstractContextSettings typeSettings = CFW.Registry.ContextSettings.createContextSettingInstance(current.type());
			
			typeSettings.mapJsonFields(current.settings());
			typeSettings.setWrapper(current);
			settingsArray.add(typeSettings);
		}
		
		settingsCache.put(type, settingsArray);
		return settingsArray;
	}
	
	/***************************************************************
	 * Returns a map with ID/Name values for select options.
	 * @param type of the context setting
	 ****************************************************************/
	public static LinkedHashMap<Object, Object> getSelectOptionsForType(String type) {
		
		LinkedHashMap<Object, Object> objects =  new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "getSelectOptionsForType")
				.select()
				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE.toString(), type)
				.orderby(ContextSettingsFields.CFW_CTXSETTINGS_NAME)
				.getAsLinkedHashMap(ContextSettingsFields.PK_ID, ContextSettingsFields.CFW_CTXSETTINGS_NAME);
		
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
				.getCount();
		
		return (count > 0);
	}
	
	public static boolean checkExistsIgnoreCurrent(ContextSettings settings) {
		return checkExistsIgnoreCurrent(settings.id(), settings.type(), settings.name());
	}
	
	public static boolean checkExistsIgnoreCurrent(Integer currentID, String type, String name) {	
		int count = new ContextSettings()
				.queryCache(CFWDBContextSettings.class, "checkExists")
				.selectCount()
				.where(ContextSettingsFields.CFW_CTXSETTINGS_TYPE, type)
				.and(ContextSettingsFields.CFW_CTXSETTINGS_NAME, name)
				.and().not().is(ContextSettingsFields.PK_ID, currentID)
				.limit(1)
				.getCount();
		
		return (count > 0);
	}

		
}

package com.xresch.cfw.features.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.config.Configuration.ConfigFields;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBConfig {
	
	private static final String KEY_SEPARATOR = "_x_x_";

	private static final Logger logger = CFWLog.getLogger(CFWDBConfig.class.getName());
	
	//name/value pairs of configuration elements
	private static LinkedHashMap<String, String> configCache = new LinkedHashMap<String, String>();
	
	private static ArrayList<ConfigChangeListener> changeListeners = new ArrayList<ConfigChangeListener>();
	
	/********************************************************************************************
	 * Creates the table and default admin user if not already exists.
	 * This method is executed by CFW.DB.initialize().
	 * 
	 ********************************************************************************************/
	public static void initializeTable() {
		new Configuration().createTable();
	}
	
	/********************************************************************************************
	 * Add a change listener that listens to config changes.
	 * 
	 ********************************************************************************************/
	public static void addChangeListener(ConfigChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/********************************************************************************************
	 * Creates the table and default admin user if not already exists.
	 * This method is executed by CFW.DB.initialize().
	 * 
	 ********************************************************************************************/
	public static boolean updateCache() {
		ResultSet result = new Configuration()
			.select(
					  ConfigFields.CATEGORY.toString()
					, ConfigFields.NAME.toString()
					, ConfigFields.VALUE.toString()
				)
			.getResultSet();
		
		if(result == null) {
			return false;
		}
		
		try {
			LinkedHashMap<String, String> newCache = new LinkedHashMap<String, String>();
			
			
			while(result.next()) {
				String category = result.getString(ConfigFields.CATEGORY.toString());
				String name = result.getString(ConfigFields.NAME.toString());
				String cacheKey = createKey(category, name);
				
				newCache.put( cacheKey, result.getString(ConfigFields.VALUE.toString()) );
			}
			cacheAndTriggerChange(newCache);
			
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error updating configuration cache.", e);
			return false;
		}finally {
			CFWDB.close(result);
		}
		
		return true;
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	private static void cacheAndTriggerChange(LinkedHashMap<String, String> newCache) {
		
		LinkedHashMap<String, String> tempOldCache = configCache;
		configCache = newCache;
		
		ArrayList<ConfigChangeListener> triggered = new ArrayList<ConfigChangeListener>();
		
		for(Entry<String, String> entry: newCache.entrySet()) {
			String cacheKey = entry.getKey();
			String configName = getNameFromKey(cacheKey);
			String newValue = entry.getValue();
			
			String oldValue = tempOldCache.get(cacheKey);
			

			if((oldValue == null && newValue != null) 
			|| (oldValue != null && newValue == null) 
			|| (oldValue != null && newValue != null && !oldValue.equals(newValue) ) ) {
				for(ConfigChangeListener listener : changeListeners) {
					if ( (!triggered.contains(listener)) && listener.listensOnConfig(configName)) {
						listener.onChange();
						triggered.add(listener);
					}
				}
			}
		}
		
		
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	private static String createKey(String category, String configName) {
		return category+KEY_SEPARATOR+configName;
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	private static String getNameFromKey(String cacheKey) {
		return cacheKey.split(KEY_SEPARATOR)[1];
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as String
	 * 
	 ********************************************************************************************/
	public static String getConfigAsString(String category, String configName) {
		return configCache.get( createKey(category,configName) );
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as JsonArray
	 * 
	 ********************************************************************************************/
	public static JsonArray getConfigAsJsonArray(String category, String configName) {
		JsonElement element = CFW.JSON.fromJson( 
				configCache.get( createKey(category,configName) ) 
			);
		
		if(element.isJsonNull()) {
			return new JsonArray();
		}
		return element.getAsJsonArray();
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as ArrayList
	 * 
	 ********************************************************************************************/
	public static ArrayList<String> getConfigAsArrayList(String category, String configName) {
		
		ArrayList<String> arrayList = new ArrayList<>();
		
		for(JsonElement element : getConfigAsJsonArray(category, configName)) {
			arrayList.add(element.getAsString());
		}
		
		return arrayList;
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as boolean
	 * 
	 ********************************************************************************************/
	public static boolean getConfigAsBoolean(String category, String configName) {
		return Boolean.parseBoolean(
				configCache.get( createKey(category,configName) )
			);
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as integer.
	 * 
	 ********************************************************************************************/
	public static int getConfigAsInt(String category, String configName) {
		return Integer.parseInt(
				configCache.get( createKey(category,configName) ) 
			);
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as long.
	 * 
	 ********************************************************************************************/
	public static long getConfigAsLong(String category, String configName) {
		return Long.parseLong(
				configCache.get( createKey(category,configName) )
			);
	}
	
	/********************************************************************************************
	 * Returns a config value from cache as long.
	 * 
	 ********************************************************************************************/
	public static float getConfigAsFloat(String category, String configName) {
		return Float.parseFloat(
				configCache.get( createKey(category,configName) )
			);
	}
	
	/********************************************************************************************
	 * Creates multiple configs in the DB.
	 * @param Configs with the values that should be inserted. ID will be set by the Database.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void create(Configuration... configs) {
		
		for(Configuration config : configs) {
			create(config);
		}
	}
	
	/********************************************************************************************
	 * Creates a new configuration in the DB if the name was not already given.
	 * All newly created permissions are by default assigned to the Superuser Role.
	 * 
	 * @param config with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(Configuration config) {
		
		if(config == null || Strings.isNullOrEmpty(config.name()) ) {
			return false;
		}
		
		boolean result = true; 
		if(!CFW.DB.Config.checkConfigExists(config)) {
			
			result &= CFW.DB.Config.create(config);
			
			if( CFW.DB.Config.selectBy(config.category(),config.name()) == null ) {
				result = false;
			}
		}else {
			//--------------------------
			// Update Available Options
			Configuration configToUpdate = CFW.DB.Config.selectBy(config.category(), config.name());
			if( configToUpdate != null ) {
				configToUpdate.options(config.options());
				configToUpdate.update(ConfigFields.OPTIONS);
			}
			
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * Creates a new config in the DB.
	 * @param KeyValuePair with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(Configuration config) {
		
		if(config == null) {
			new CFWLog(logger)
				.warn("The config cannot be null");
			return false;
		}
		
		if(config.name() == null || config.name().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a name for the config to create.");
			return false;
		}
		
		if(checkConfigExists(config)) {
			new CFWLog(logger)
				.warn("The config '"+config.name()+"' cannot be created as a config with this name already exists.");
			return false;
		}
		
		boolean insertResult =  config
				.queryCache(CFWDBConfig.class, "create")
				.insert();
		
		updateCache();
		
		return insertResult;
	}
	
	/***************************************************************
	 * Select a config by it's name.
	 * @param id of the config
	 * @return Returns a config or null if not found or in case of exception.
	 ****************************************************************/
	public static Configuration selectBy(String category, String name) {
		
		return (Configuration)new Configuration()
				.select()
				.where(ConfigFields.CATEGORY.toString(), category)
				.and(ConfigFields.NAME.toString(), name)
				.getFirstAsObject();

	}
	
	/***************************************************************
	 * Select a config by it's ID.
	 * @param id of the config
	 * @return Returns a config or null if not found or in case of exception.
	 ****************************************************************/
	public static Configuration selectByID(int id ) {

		return (Configuration)new Configuration()
				.queryCache(CFWDBConfig.class, "selectByID")
				.select()
				.where(ConfigFields.PK_ID.toString(), id)
				.getFirstAsObject();
		
	}
	
	/***************************************************************
	 * Select a config by it's ID and return it as JSON string.
	 * @param id of the config
	 * @return Returns a config or null if not found or in case of exception.
	 ****************************************************************/
	public static ArrayList<String> getCategories() {
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "getCategories")
				.distinct()
				.select(ConfigFields.CATEGORY)
				.orderby(ConfigFields.CATEGORY)
				.getAsStringArrayList(ConfigFields.CATEGORY);
		
	}
	
	/***************************************************************
	 * Select a config by it's ID and return it as JSON string.
	 * @param id of the config
	 * @return Returns a config or null if not found or in case of exception.
	 ****************************************************************/
	public static String getConfigAsJSON(String id) {
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "getConfigAsJSON")
				.select()
				.where(ConfigFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all configs
	 * 
	 * @return Returns a resultSet with all configs or null.
	 ****************************************************************/
	public static ResultSet getConfigList() {
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "getConfigList")
				.select()
				.orderby(ConfigFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all configs
	 * 
	 * @return Returns a resultSet with all configs or null.
	 ****************************************************************/
	public static ArrayList<CFWObject> getConfigObjectList() {
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "getConfigList")
				.select()
				.orderby(ConfigFields.NAME.toString())
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return a list of all users as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getConfigListAsJSON() {
		return new Configuration()
				.queryCache(CFWDBConfig.class, "getConfigListAsJSON")
				.select()
				.orderby(ConfigFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param config
	 * @return true or false
	 ****************************************************************/
	public static boolean update(Configuration config) {
		
		if(config == null) {
			new CFWLog(logger)
				.warn("The config that should be updated cannot be null");
			return false;
		}
		
		if(config.name() == null || config.name().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a name for the config.");
			return false;
		}
		String cacheKey = createKey(config.category(), config.name());
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Configuration.class, "Change config '"+config.name()+"' from '"+configCache.get(cacheKey)+"' to '"+config.value()+"'");
		boolean updateResult =  config
				.queryCache(CFWDBConfig.class, "update")
				.update();
		
		updateCache();
		
		return updateResult;
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * you have to call the updateCache(method manually after using 
	 * this method.
	 * 
	 * @param config
	 * @return true if the value was updated
	 ****************************************************************/
	public static boolean updateValue(int id, String value) {
		
		// does not update cache automatically 
		Configuration currentConfigFromDB = selectByID(id);
		
		if(currentConfigFromDB != null) {
			String oldValue = currentConfigFromDB.value();
			
			if( (oldValue != null  && !oldValue.equals(value))
			 || (oldValue == null  && !Strings.isNullOrEmpty(value))	) {
				new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Configuration.class, "Change config '"+currentConfigFromDB.name()+"' from '"+oldValue+"' to '"+value+"'");
			}
			
			return new Configuration()
				.id(id)
				.value(value)
				.queryCache(CFWDBConfig.class, "updateValue")
				.update(ConfigFields.VALUE.toString());
		}else {
			new CFWLog(logger).severe("The configuration with id '"+id+"' does not exist.");
			return false;
		}
	}
	

	/****************************************************************
	 * Deletes the config by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		Configuration config = selectByID(id);
		if(config == null ) {
			new CFWLog(logger)
			.severe("The config with id '"+id+"'+could not be found.");
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, Configuration.class, "Delete configuration: '"+config.name()+"'");
		return new Configuration()
				.queryCache(CFWDBConfig.class, "deleteByID")
				.delete()
				.where(ConfigFields.PK_ID.toString(), id)
				.executeDelete();
					
	}
	
	/****************************************************************
	 * Deletes multiple users by id.
	 * @param ids of the users separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByID(String resultIDs) {
		
		//----------------------------------
		// Check input format
		if(resultIDs == null ^ !resultIDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.severe("The userID's '"+resultIDs+"' are not a comma separated list of strings.");
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, Configuration.class, "Delete Multiple configurations: '"+resultIDs+"'");
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "deleteMultipleByID")
				.delete()
				.whereIn(ConfigFields.PK_ID.toString(), resultIDs)
				.executeDelete();
					
	}
	
	/****************************************************************
	 * Deletes the config by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteBy(String category, String name) {
		
		Configuration config = selectBy(category, name);
		if(config == null ) {
			new CFWLog(logger)
			.severe("The config with name '"+name+"'+could not be found.");
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, Configuration.class, "Delete configuration: '"+config.name()+"'");
		
		return new Configuration()
				.queryCache(CFWDBConfig.class, "deleteByName")
				.delete()
				.where(ConfigFields.NAME.toString(), name)
				.executeDelete();
					
	}
	
	
	/****************************************************************
	 * Check if the config exists by name.
	 * 
	 * @param config to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkConfigExists(Configuration config) {
		if(config == null) { return false;}
		
		return checkConfigExists(config.category(), config.name());
	}
	
	/****************************************************************
	 * Check if the config exists by name.
	 * 
	 * @param configname to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkConfigExists(String configCategory, String configName) {
		
		int count = new Configuration()
				.queryCache(CFWDBConfig.class, "checkConfigExists")
				.selectCount()
				.where(ConfigFields.CATEGORY.toString(), configCategory)
				.and(ConfigFields.NAME.toString(), configName)
				.executeCount();
		
		return (count > 0);
		
	}
	
}

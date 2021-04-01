package com.xresch.cfw.features.keyvaluepairs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair.KeyValuePairFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBKeyValuePairs {
	
	private static final Logger logger = CFWLog.getLogger(CFWDBKeyValuePairs.class.getName());
	
	//key/value pairs of keyValuePair elements
	private static LinkedHashMap<String, String> keyValCache = new LinkedHashMap<String, String>();
	
	private static ArrayList<KeyValueChangeListener> changeListeners = new ArrayList<KeyValueChangeListener>();
	
	/********************************************************************************************
	 * Creates the table and default admin user if not already exists.
	 * This method is executed by CFW.DB.initialize().
	 * 
	 ********************************************************************************************/
	public static void initializeTable() {
		new KeyValuePair().createTable();
	}
	
	/********************************************************************************************
	 * Add a change listener that listens to keyVal changes.
	 * 
	 ********************************************************************************************/
	public static void addChangeListener(KeyValueChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/********************************************************************************************
	 * Creates the table and default admin user if not already exists.
	 * This method is executed by CFW.DB.initialize().
	 * 
	 ********************************************************************************************/
	public static boolean updateCache() {
		ResultSet result = new KeyValuePair()
			.select(KeyValuePairFields.KEY.toString(), KeyValuePairFields.VALUE.toString())
			.getResultSet();
		
		if(result == null) {
			return false;
		}
		
		try {
			LinkedHashMap<String, String> newCache = new LinkedHashMap<String, String>();
			while(result.next()) {
				newCache.put(
					result.getString(KeyValuePairFields.KEY.toString()),
					result.getString(KeyValuePairFields.VALUE.toString())
				);
			}
			cacheAndTriggerChange(newCache);
			
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error updating keyValuePair cache.", e);
			return false;
		}finally {
			CFWDB.close(result);
		}
		
		return true;
	}
	
	/********************************************************************************************
	 * Creates the table and default admin user if not already exists.
	 * This method is executed by CFW.DB.initialize().
	 * 
	 ********************************************************************************************/
	private static void cacheAndTriggerChange(LinkedHashMap<String, String> newCache) {
		
		LinkedHashMap<String, String> tempOldCache = keyValCache;
		keyValCache = newCache;
		
		ArrayList<KeyValueChangeListener> triggered = new ArrayList<KeyValueChangeListener>();
		
		for(Entry<String, String> entry: newCache.entrySet()) {
			String keyValName = entry.getKey();
			String newValue = entry.getValue();
			
			String oldValue = tempOldCache.get(keyValName);
			
			if((oldValue == null && newValue != null) 
			|| (oldValue != null && newValue == null) 
			|| (oldValue != null && newValue != null && !oldValue.equals(newValue) ) ) {
				for(KeyValueChangeListener listener : changeListeners) {
					if ( (!triggered.contains(listener)) && listener.listensOnConfig(keyValName)) {
//						System.out.println("====================");
//						System.out.println("keyValName:"+keyValName);
//						System.out.println("newValue:"+newValue);
//						System.out.println("oldValue:"+oldValue);
						listener.onChange();
						triggered.add(listener);
					}
				}
			}
		}
		
		
	}
	/********************************************************************************************
	 * Returns a keyVal value from cache as String
	 * 
	 ********************************************************************************************/
	public static String getValueAsString(String key) {
		return keyValCache.get(key);
	}
	
	/********************************************************************************************
	 * Returns a keyVal value from cache as boolean
	 * 
	 ********************************************************************************************/
	public static boolean getValueAsBoolean(String key) {
		System.out.println("===== Key: "+key+", Value: "+keyValCache.get(key));
		return Boolean.parseBoolean(keyValCache.get(key));
	}
	
	/********************************************************************************************
	 * Returns a keyVal value from cache as integer.
	 * 
	 ********************************************************************************************/
	public static int getValueAsInt(String key) {
		return Integer.parseInt(keyValCache.get(key));
	}
	
	/********************************************************************************************
	 * Returns a keyVal value from cache as long.
	 * 
	 ********************************************************************************************/
	public static long getValueAsLong(String key) {
		return Long.parseLong(keyValCache.get(key));
	}
		
	/********************************************************************************************
	 * Creates a new keyValuePair in the DB if the key was not already given.
	 * 
	 * @param keyValuePair with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(KeyValuePair keyValuePair) {
		
		if(keyValuePair == null || Strings.isNullOrEmpty(keyValuePair.key()) ) {
			return false;
		}
		
		boolean result = true; 
		if(!CFW.DB.KeyValuePairs.checkKeyExists(keyValuePair)) {
			
			result &= CFW.DB.KeyValuePairs.create(keyValuePair);
			
			if( CFW.DB.KeyValuePairs.selectByKey(keyValuePair.key()) == null ) {
				result = false;
			}
		}
		
		updateCache();
		return result;
	}
	
	/********************************************************************************************
	 * Creates a new keyVal in the DB.
	 * @param KeyValuePair with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(KeyValuePair keyVal) {
		
		if(keyVal == null) {
			new CFWLog(logger)
				.warn("The keyVal cannot be null");
			return false;
		}
		
		if(keyVal.key() == null || keyVal.key().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a key for the keyVal to create.");
			return false;
		}
		
		if(checkKeyExists(keyVal)) {
			new CFWLog(logger)
				.warn("The keyVal '"+keyVal.key()+"' cannot be created as a keyVal with this name already exists.");
			return false;
		}
		
		boolean insertResult =  new CFWSQL(keyVal)
				.queryCache()
				.insert();
		
		updateCache();
		
		return insertResult;
	}
	
	/***************************************************************
	 * Select a keyVal by it's key.
	 * @param id of the keyVal
	 * @return Returns a keyVal or null if not found or in case of exception.
	 ****************************************************************/
	public static KeyValuePair selectByKey(String name) {
		
		return (KeyValuePair)new CFWSQL(new KeyValuePair())
				.queryCache()
				.select()
				.where(KeyValuePairFields.KEY.toString(), name)
				.getFirstAsObject();

	}
	
	/***************************************************************
	 * Select a keyVal by it's ID.
	 * @param id of the keyVal
	 * @return Returns a keyVal or null if not found or in case of exception.
	 ****************************************************************/
	public static KeyValuePair selectByID(int id ) {

		return (KeyValuePair)new CFWSQL(new KeyValuePair())
				.queryCache()
				.select()
				.where(KeyValuePairFields.PK_ID.toString(), id)
				.getFirstAsObject();
		
	}
	
	/***************************************************************
	 * Select a keyVal by it's ID and return it as JSON string.
	 * @param id of the keyVal
	 * @return Returns a keyVal or null if not found or in case of exception.
	 ****************************************************************/
	public static ArrayList<String> getCategories() {
		
		return new CFWSQL(new KeyValuePair())
				.queryCache()
				.distinct()
				.select(KeyValuePairFields.CATEGORY)
				.orderby(KeyValuePairFields.CATEGORY)
				.getAsStringArrayList(KeyValuePairFields.CATEGORY);
		
	}
	
	/***************************************************************
	 * Select a keyVal by it's ID and return it as JSON string.
	 * @param id of the keyVal
	 * @return Returns a keyVal or null if not found or in case of exception.
	 ****************************************************************/
	public static String getKeyValuePairsAsJSON(String id) {
		
		return new CFWSQL(new KeyValuePair())
				.queryCache()
				.select()
				.where(KeyValuePairFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all keyVals
	 * 
	 * @return Returns a resultSet with all keyVals or null.
	 ****************************************************************/
	public static ResultSet getKeyValuePairList() {
		
		return new CFWSQL(new KeyValuePair())
				.queryCache()
				.select()
				.orderby(KeyValuePairFields.KEY.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all keyVals
	 * 
	 * @return Returns a resultSet with all keyVals or null.
	 ****************************************************************/
	public static ArrayList<CFWObject> getConfigObjectList() {
		
		return new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "getConfigList")
				.select()
				.orderby(KeyValuePairFields.KEY.toString())
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return a list of all users as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getConfigListAsJSON() {
		return new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "getConfigListAsJSON")
				.select()
				.orderby(KeyValuePairFields.KEY.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param keyVal
	 * @return true or false
	 ****************************************************************/
	public static boolean update(KeyValuePair keyVal) {
		
		if(keyVal == null) {
			new CFWLog(logger)
				.warn("The keyVal that should be updated cannot be null");
			return false;
		}
		
		if(keyVal.key() == null || keyVal.key().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a name for the keyVal.");
			return false;
		}
		
		new CFWLog(logger).audit("UPDATE", KeyValuePair.class, "Change keyVal '"+keyVal.key()+"' from '"+keyValCache.get(keyVal.key())+"' to '"+keyVal.value()+"'");
		boolean updateResult =  keyVal
				.queryCache(CFWDBKeyValuePairs.class, "update")
				.update();
		
		updateCache();
		
		return updateResult;
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * you have to call the updateCache(method manually after using 
	 * this method.
	 * 
	 * @param keyVal
	 * @return true if the value was updated
	 ****************************************************************/
	public static boolean updateValue(int id, String value) {
		
		// does not update cache automatically 
		KeyValuePair currentConfigFromDB = selectByID(id);
		
		if(currentConfigFromDB != null) {
			String oldValue = currentConfigFromDB.value();
			
			if( (oldValue != null  && !oldValue.equals(value))
			 || (oldValue == null  && !Strings.isNullOrEmpty(value))	) {
				new CFWLog(logger).audit("UPDATE", KeyValuePair.class, "Change keyVal '"+currentConfigFromDB.key()+"' from '"+oldValue+"' to '"+value+"'");
			}
			
			return new KeyValuePair()
				.id(id)
				.value(value)
				.queryCache(CFWDBKeyValuePairs.class, "updateValue")
				.update(KeyValuePairFields.VALUE.toString());
		}else {
			new CFWLog(logger).severe("The keyValuePair with id '"+id+"' does not exist.");
			return false;
		}
	}
	

	/****************************************************************
	 * Deletes the keyVal by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		KeyValuePair keyVal = selectByID(id);
		if(keyVal == null ) {
			new CFWLog(logger)
			.severe("The keyVal with id '"+id+"'+could not be found.");
			return false;
		}
		
		new CFWLog(logger).audit("DELETE", KeyValuePair.class, "Delete keyValuePair: '"+keyVal.key()+"'");
		return new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "deleteByID")
				.delete()
				.where(KeyValuePairFields.PK_ID.toString(), id)
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
		
		new CFWLog(logger).audit("DELETE", KeyValuePair.class, "Delete Multiple keyValuePairs: '"+resultIDs+"'");
		
		return new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "deleteMultipleByID")
				.delete()
				.whereIn(KeyValuePairFields.PK_ID.toString(), resultIDs)
				.executeDelete();
					
	}
	
	/****************************************************************
	 * Deletes the keyVal by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByName(String name) {
		
		KeyValuePair keyVal = selectByKey(name);
		if(keyVal == null ) {
			new CFWLog(logger)
			.severe("The keyVal with name '"+name+"'+could not be found.");
			return false;
		}
		
		new CFWLog(logger).audit("DELETE", KeyValuePair.class, "Delete keyValuePair: '"+keyVal.key()+"'");
		
		return new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "deleteByName")
				.delete()
				.where(KeyValuePairFields.KEY.toString(), name)
				.executeDelete();
					
	}
	
	
	/****************************************************************
	 * Check if the keyVal exists by name.
	 * 
	 * @param keyVal to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkKeyExists(KeyValuePair keyVal) {
		if(keyVal == null) { return false;}
		
		return checkKeyExists(keyVal.key());
	}
	
	/****************************************************************
	 * Check if the keyVal exists by name.
	 * 
	 * @param keyValname to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkKeyExists(String keyValName) {
		
		int count = new KeyValuePair()
				.queryCache(CFWDBKeyValuePairs.class, "checkConfigExists")
				.selectCount()
				.where(KeyValuePairFields.KEY.toString(), keyValName)
				.getCount();
		
		return (count > 0);
		
	}
	
}

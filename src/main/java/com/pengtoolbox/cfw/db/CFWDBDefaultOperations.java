package com.pengtoolbox.cfw.db;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class CFWDBDefaultOperations<O extends CFWObject> {
	
	public static Logger logger = CFWLog.getLogger(CFWDBDefaultOperations.class.getName());
	
	
	/********************************************************************************************
	 * Creates multiple items in the DB.
	 * @param Roles with the values that should be inserted. ID will be set by the Database.
	 * @return true if all created successful
	 * 
	 ********************************************************************************************/
	public static <O extends CFWObject> boolean create(PrecheckHandler precheck, O... cfwObjects) {
		
		boolean result = true;
		for(O object : cfwObjects) {
			result &= create(precheck, object);
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * Creates a new item in the DB.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static <O extends CFWObject> boolean create(PrecheckHandler precheck, O object) {
		
		if(object == null) {
			new CFWLog(logger)
				.method("create")
				.warn("The object cannot be null", new Throwable());
			return false;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		return object
			.queryCache(object.getClass(), "CFWDBDefaultOperations.create")
			.insert();

	}
	
	/********************************************************************************************
	 * Creates a new item in the DB.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return primary key or null if not successful
	 * 
	 ********************************************************************************************/
	public static <O extends CFWObject> Integer createGetPrimaryKey(PrecheckHandler precheck, O object) {
		
		if(object == null) {
			new CFWLog(logger)
				.method("create")
				.warn("The object cannot be null", new Throwable());
			return null;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return null;
		}
		
		return object
			.queryCache(object.getClass(), "CFWDBDefaultOperations.insertGetPrimaryKey")
			.insertGetPrimaryKey();

	}
	
	/********************************************************************************************
	 * Updates multiple items in the DB.
	 * @param Objects with the values that should be inserted. ID will be set by the Database.
	 * @return true if all updated successful
	 * 
	 ********************************************************************************************/
	public static <O extends CFWObject> boolean update(PrecheckHandler precheck, O... cfwObjects) {
		
		boolean result = true;
		for(O object : cfwObjects) {
			result &= update(precheck, object);
		}
		
		return result;
	}
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static <O extends CFWObject> boolean update(PrecheckHandler precheck, O object) {
		
		if(object == null) {
			new CFWLog(logger)
				.method("update")
				.warn("The role that should be updated cannot be null");
			return false;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
				
		return object
				.queryCache(object.getClass(), "CFWDBDefaultOperations.update")
				.update();
		
	}
	
	/***************************************************************
	 * Deletes the objects selected where the �recheck returns true.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static <O extends CFWObject> boolean deleteBy(PrecheckHandler precheck, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		
		ArrayList<CFWObject> objectArray = CFWDBDefaultOperations.selectBy(cfwObjectClass, column, value);
		
		for(CFWObject object : objectArray) {
			if(precheck != null && !precheck.doCheck(object)) {
				return false;
			}
			
			return object
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteBy"+column)
					.delete()
					.where(column, value)
					.executeDelete();
		}
		
		return false;
	}
	
	/***************************************************************
	 * Deletes the first object selected.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static <O extends CFWObject> boolean deleteFirstBy(PrecheckHandler precheck, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		
		CFWObject object = CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, column, value);
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		if(object != null) {
			return object
				.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteFirstBy"+column)
				.deleteTop(1)
				.where(column, value)
				.executeDelete();
		}
		
		return false;
		
	}
	
	/****************************************************************
	 * Deletes multiple items by id.
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static <O extends CFWObject> boolean deleteMultipleByID(Class<? extends CFWObject> cfwObjectClass, String commaSeparatedIDs) {
		
		//----------------------------------
		// Check input format
		if(commaSeparatedIDs == null ^ !commaSeparatedIDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.method("deleteMultipleByID")
			.severe("The input '"+commaSeparatedIDs+"' are not a comma separated list of IDs.");
			return false;
		}

		try {
			CFWObject instance = cfwObjectClass.newInstance();
			return instance
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteMultipleByID")
					.delete()
					.whereIn(instance.getPrimaryField().getName(), commaSeparatedIDs)
					.executeDelete();
			
		} catch (Exception e) {
			new CFWLog(logger)
				.method("deleteMultipleByID")
				.warn("Error while instanciating object.", e);
			return false;
		}
		
	}
	
	/****************************************************************
	 * Deletes multiple items by id where a certain condition mets.
	 * This is useful to check if the item is from a specific user
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static <O extends CFWObject> boolean deleteMultipleByIDWhere(
											Class<? extends CFWObject> cfwObjectClass, 
											String commaSeparatedIDs,
											Object fieldnameToCheck,
											Object valueToCheck) {
		
		//----------------------------------
		// Check input format
		if(commaSeparatedIDs == null ^ !commaSeparatedIDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.method("deleteMultipleByIDWhere")
			.severe("The input '"+commaSeparatedIDs+"' are not a comma separated list of IDs.");
			return false;
		}

		try {
			CFWObject instance = cfwObjectClass.newInstance();
			return instance
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteMultipleByIDWhere")
					.delete()
					.whereIn(instance.getPrimaryField().getName(), commaSeparatedIDs)
					.and(fieldnameToCheck, valueToCheck)
					.executeDelete();
			
		} catch (Exception e) {
			new CFWLog(logger)
				.method("deleteMultipleByIDWhere")
				.warn("Error while instanciating object.", e);
			return false;
		}
		
	}
	
	/***************************************************************
	 * Select a role by it's name.
	 * @param id of the role
	 * @return Returns a role or null if not found or in case of exception.
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	public static <O extends CFWObject> O selectFirstBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			return (O)cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectFirstBy"+column)
					.select()
					.where(column, value)
					.getFirstObject();
		} catch (Exception e) {
			new CFWLog(logger)
				.method("selectFirstBy")
				.warn("Error while instanciating object.", e);
		} 
		
		return null;

	}
	
	/***************************************************************
	 * Select a role by it's name.
	 * @param id of the role
	 * @return Returns a role or null if not found or in case of exception.
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	public static <O extends CFWObject> ArrayList<O> selectBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			return (ArrayList<O>)cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectBy"+column)
					.select()
					.where(column, value)
					.getAsObjectList();
			
		} catch (Exception e) {
			new CFWLog(logger)
			.method("selectBy")
			.warn("Error while instanciating object.", e);
		} 
		
		return null;

	}
	
	/***************************************************************
	 * Select a role by it's name.
	 * @param id of the role
	 * @return Returns a role or null if not found or in case of exception.
	 ****************************************************************/
	public static <O extends CFWObject> boolean checkExistsBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			int count = cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.checkExistsBy"+column)
					.selectCount()
					.where(column, value)
					.limit(1)
					.getCount();
			
			return (count > 0);
			
		} catch (Exception e) {
			new CFWLog(logger)
				.method("checkExistsBy")
				.warn("Error while instanciating object.", e);
		} 
		
		return false;

	}
	
}

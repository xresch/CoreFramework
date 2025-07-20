package com.xresch.cfw.db;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public  class CFWDBDefaultOperations {
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDefaultOperations.class.getName());
	
	/********************************************************************************************
	 * Creates multiple items in the DB and sets the primary key on the object.
	 * @param Roles with the values that should be inserted. ID will be set by the Database.
	 * @return true if all created successful
	 * 
	 ********************************************************************************************/
	public static boolean create(PrecheckHandler precheck, CFWObject... cfwObjects) {
		return create(precheck, null, cfwObjects);
	}
	/********************************************************************************************
	 * Creates multiple items in the DB and sets the primary key on the object.
	 * @param Roles with the values that should be inserted. ID will be set by the Database.
	 * @return true if all created successful
	 * 
	 ********************************************************************************************/
	public static boolean create(PrecheckHandler precheck, String[] auditLogFieldnames, CFWObject... cfwObjects) {
		
		boolean result = true;
		for(CFWObject object : cfwObjects) {
			result &= create(precheck, object);
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(PrecheckHandler precheck, CFWObject object) {
		
		return create(precheck, null, object);

	}
	
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(PrecheckHandler precheck,  String[] auditLogFieldnames, CFWObject object) {
		
		if(object == null) {
			new CFWLog(logger)
				.warn("The object cannot be null", new Throwable());
			return false;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.CREATE, object, auditLogFieldnames);
		
		Integer id = object
			.queryCache(object.getClass(), "CFWDBDefaultOperations.create")
			.insertGetPrimaryKey();
		
		if(id == null) {
			 return false;
		}else if( object.getPrimaryKeyValue() == null) {
			object.getPrimaryKeyField().setValue(id);
		}
		
		return true;

	}
	
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return primary key or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer createGetPrimaryKey(PrecheckHandler precheck, CFWObject object) {
		return createGetPrimaryKey(precheck, null, object);
	}
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return primary key or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer createGetPrimaryKey(PrecheckHandler precheck,  String[] auditLogFieldnames, CFWObject object) {
		
		if(object == null) {
			new CFWLog(logger)
				.warn("The object cannot be null", new Throwable());
			return null;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return null;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.CREATE, object, auditLogFieldnames);
		
		Integer id = object
				// somehow messes stuff up, only god knows why... okay maybe not even he knows
				// .queryCache(object.getClass(), "CFWDBDefaultOperations.createGetPrimaryKey")
				.insertGetPrimaryKey();
				 
		if(id != null && object.getPrimaryKeyValue() == null) {
			object.getPrimaryKeyField().setValue(id);
		}
		
		return id;

	}
	
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return primary key or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer createGetPrimaryKeyWithout(PrecheckHandler precheck, CFWObject object, Object... fieldnamesToExclude) {
		return createGetPrimaryKeyWithout(precheck, null, object, fieldnamesToExclude);
	}
	
	/********************************************************************************************
	 * Creates a new item in the DB and sets the primary key on the object.
	 * @param Object with the values that should be inserted. ID will be set by the Database.
	 * @return primary key or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer createGetPrimaryKeyWithout(PrecheckHandler precheck,  String[] auditLogFieldnames, CFWObject object, Object... fieldnamesToExclude) {
		
		if(object == null) {
			new CFWLog(logger)
				.warn("The object cannot be null", new Throwable());
			return null;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return null;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.CREATE, object, auditLogFieldnames);
		
		Integer id =  new CFWSQL(object)
				//.queryCache(object.getClass(), "CFWDBDefaultOperations.createGetPrimaryKeyWithout")
				.insertGetPrimaryKeyWithout(fieldnamesToExclude);
				 
		if(id != null && object.getPrimaryKeyValue() == null) {
			object.getPrimaryKeyField().setValue(id);
		}
		
		return id;

	}
	
	/********************************************************************************************
	 * Updates multiple items in the DB.
	 * @param Objects with the values that should be inserted. ID will be set by the Database.
	 * @return true if all updated successful
	 * 
	 ********************************************************************************************/
	public static boolean update(PrecheckHandler precheck, CFWObject... cfwObjects) {
		
		boolean result = true;
		for(CFWObject object : cfwObjects) {
			result &= update(precheck, null, object);
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * Updates multiple items in the DB.
	 * @param Objects with the values that should be inserted. ID will be set by the Database.
	 * @return true if all updated successful
	 * 
	 ********************************************************************************************/
	public static boolean update(PrecheckHandler precheck, String[] auditLogFieldnames, CFWObject... cfwObjects) {
		
		boolean result = true;
		for(CFWObject object : cfwObjects) {
			result &= update(precheck, auditLogFieldnames, object);
		}
		
		return result;
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean update(PrecheckHandler precheck, CFWObject object) {
		
		return update(precheck, null, object);
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean update(PrecheckHandler precheck, String[] auditLogFieldnames, CFWObject object) {
		
		if(object == null) {
			new CFWLog(logger)
				.warn("The role that should be updated cannot be null");
			return false;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, object, auditLogFieldnames);
		
		return object
				.queryCache(object.getClass(), "CFWDBDefaultOperations.update")
				.update();
		
	}
	
	/***************************************************************
	 * Updates the object selecting by ID, ignores the specified
	 * fields.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean updateWithout(PrecheckHandler precheck, CFWObject object, String... fieldsToIgnore) {
		
		return updateWithout(precheck, null, object, fieldsToIgnore);
	}
	
	/***************************************************************
	 * Updates the object selecting by ID, ignores the specified
	 * fields.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean updateWithout(PrecheckHandler precheck, String[] auditLogFieldnames, CFWObject object, String... fieldsToIgnore) {
		
		if(object == null) {
			new CFWLog(logger)
				.warn("The role that should be updated cannot be null");
			return false;
		}
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, object, auditLogFieldnames);
		
		String cacheName = String.join("-", fieldsToIgnore);
		
		return object
				.queryCache(object.getClass(), "CFWDBDefaultOperations.updateWithout-"+cacheName)
				.updateWithout(fieldsToIgnore);
		
	}
	
	/***************************************************************
	 * Deletes the objects selected where the �recheck returns true.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean deleteBy(PrecheckHandler precheck, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		return deleteBy(precheck, null, cfwObjectClass, column, value);
	}
	
	/***************************************************************
	 * Deletes the objects selected where the precheck returns true.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean deleteBy(PrecheckHandler precheck, String[] auditLogFieldnames, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		
		ArrayList<CFWObject> objectArray = CFWDBDefaultOperations.selectBy(cfwObjectClass, column, value);
		
		boolean isSuccess = true;
		for(CFWObject object : objectArray) {
			if(precheck != null && !precheck.doCheck(object)) {
				isSuccess = false;
				continue;
			}
			
			new CFWLog(logger).audit(CFWAuditLogAction.DELETE, object, auditLogFieldnames);
			
			isSuccess &= object
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteBy"+column)
					.delete()
					.where(column, value)
					.executeDelete();
		}
		
		return isSuccess;
	}
	
	/***************************************************************
	 * Deletes the first object selected.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean deleteFirstBy(PrecheckHandler precheck, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		return  deleteFirstBy(precheck, null, cfwObjectClass, column, value);
	}
	
	/***************************************************************
	 * Deletes the first object selected.
	 * @param object
	 * @return true or false
	 ****************************************************************/
	public static boolean deleteFirstBy(PrecheckHandler precheck, String[] auditLogFieldnames, Class<? extends CFWObject> cfwObjectClass, String column, Object value) {
		
		CFWObject object = CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, column, value, auditLogFieldnames);
		
		if(precheck != null && !precheck.doCheck(object)) {
			return false;
		}
		
		if(object != null) {
			
			new CFWLog(logger).audit(CFWAuditLogAction.DELETE, object, auditLogFieldnames);
			
			return object
				.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteFirstBy"+column)
				.delete()
				.where(column, value)
				.custom("FETCH FIRST ROW ONLY")
				.executeDelete();
		}
		
		return false;
		
	}
	
	/****************************************************************
	 * Deletes multiple items by id.
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByID(PrecheckHandler precheck, Class<? extends CFWObject> cfwObjectClass, String commaSeparatedIDs) {
		
		return  deleteMultipleByID(precheck, null, cfwObjectClass, commaSeparatedIDs);
	}
	/****************************************************************
	 * Deletes multiple items by id.
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByID(PrecheckHandler precheck, String[] auditLogFieldnames, Class<? extends CFWObject> cfwObjectClass, String commaSeparatedIDs) {
		
		//----------------------------------
		// Check input format
		if(commaSeparatedIDs == null ^ !commaSeparatedIDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.severe("The input '"+commaSeparatedIDs+"' are not a comma separated list of IDs.");
			return false;
		}

		boolean success = true;
		
		try {
			CFWObject instance = cfwObjectClass.newInstance();
			String idColumn = instance.getPrimaryKeyField().getName();
			
			for(String id : commaSeparatedIDs.split(",")) {
				success &= deleteBy(precheck, auditLogFieldnames, cfwObjectClass, idColumn, id);
			}
			
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
			return false;
		}
		
		return success;
		
	}
	
	/****************************************************************
	 * Deletes multiple items by id where a certain condition mets.
	 * This is useful to check if the item is from a specific user
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByIDWhere(
											PrecheckHandler precheck, 
											Class<? extends CFWObject> cfwObjectClass, 
											String commaSeparatedIDs,
											Object fieldnameToCheck,
											Object valueToCheck) {
		
		return deleteMultipleByIDWhere(precheck, null, cfwObjectClass, commaSeparatedIDs, fieldnameToCheck, valueToCheck);
		
	}
	/****************************************************************
	 * Deletes multiple items by id where a certain condition mets.
	 * This is useful to check if the item is from a specific user
	 * @param ids separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByIDWhere(
											PrecheckHandler precheck, 
											String[] auditLogFieldnames,
											Class<? extends CFWObject> cfwObjectClass, 
											String commaSeparatedIDs,
											Object fieldnameToCheck,
											Object valueToCheck) {
		
		//----------------------------------
		// Check input format
		if(commaSeparatedIDs == null ^ !commaSeparatedIDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.severe("The input '"+commaSeparatedIDs+"' are not a comma separated list of IDs.");
			return false;
		}

		boolean success = true;
		try {
			CFWObject instance = cfwObjectClass.newInstance();
			String primaryFieldname = instance.getPrimaryKeyField().getName();
			
			String[] fieldsArray = CFW.Utils.Array.merge(new String[] {primaryFieldname}, auditLogFieldnames);
			
			ArrayList<CFWObject> objectsToDelete = instance
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteMultipleByIDWhere-Select")
					.select( ((Object[])fieldsArray) )
					.whereIn(primaryFieldname, commaSeparatedIDs)
					.and(fieldnameToCheck, valueToCheck)
					.getAsObjectList();
			
			for(CFWObject object : objectsToDelete) {
				
				if(precheck != null && !precheck.doCheck(object)) {
					success &= false;
					continue;
				}
				
				new CFWLog(logger).audit(CFWAuditLogAction.DELETE, object, auditLogFieldnames);
				success &= 	object
						.queryCache(cfwObjectClass, "CFWDBDefaultOperations.deleteMultipleByIDWhere-Delete")
						.delete()
						.where(primaryFieldname, object.getPrimaryKeyField().getValue())
						.executeDelete();
			}
			
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
			return false;
		}
		
		return success;
		
	}
	
	/***************************************************************
	 * Select first by a certain column and value.
	 * 
	 * @param cfwObjectClass the CFWObject class
	 * @param column the name of the column
	 * @param value the value to match against
	 * 
	 * @return Returns a single object
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	public static <O extends CFWObject> O selectFirstBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			return (O)cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectFirstBy"+column)
					.select()
					.where(column, value)
					.getFirstAsObject();
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return null;

	}
	
	/***************************************************************
	 * Select first by a certain column and value.
	 * 
	 * @param cfwObjectClass the CFWObject class
	 * @param column the name of the column
	 * @param value the value to match against
	 * @param fieldnames names of the fields that should be retrieved from the database
	 * 
	 * @return Returns a single object
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	public static <O extends CFWObject> O selectFirstBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value, Object ...fieldnames ) {
		
		try {
			return (O)cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectFirstBy"+column)
					.select(fieldnames)
					.where(column, value)
					.getFirstAsObject();
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return null;

	}
	
	/***************************************************************
	 * Select first by a certain column/value and without fetching 
	 * the data of the defined columns.
	 * @param cfwObjectClass the CFWObject class
	 * @param column the name of the column
	 * @param value the value to match against
	 * @param withoutFields list of field to not fetch the data for
	 * @return Returns a single object
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	public static <O extends CFWObject> O selectFirstByWithout(Class<? extends CFWObject> cfwObjectClass, String column, Object value, String... withoutFields ) {
		
		try {
			return (O)cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectFirstBy"+column)
					.selectWithout(withoutFields)
					.where(column, value)
					.getFirstAsObject();
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return null;

	}
	
	/***************************************************************
	 * Select a role by it's name.
	 * @param id of the role
	 * @return Returns an array(can be empty)
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
			.warn("Error while instanciating object.", e);
		} 
		
		return new ArrayList<>();

	}
	
	
	/***************************************************************
	 * Select a role by it's name.
	 * @param id of the role
	 * @return Returns an JsonArray as string (can be empty array)
	 ****************************************************************/
	public static String selectByAsJSON(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			return cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.selectByAsJSON"+column)
					.select()
					.where(column, value)
					.getAsJSON();
			
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return "[]";

	}
	
	/***************************************************************
	 * Checks if a records with the given value already exists.
	 * @return Returns true if exists, false otherwise.
	 ****************************************************************/
	public static boolean checkExistsBy(Class<? extends CFWObject> cfwObjectClass, String column, Object value ) {
		
		try {
			int count = cfwObjectClass.newInstance()
					.queryCache(cfwObjectClass, "CFWDBDefaultOperations.checkExistsBy"+column)
					.selectCount()
					.where(column, value)
					.limit(1)
					.executeCount();
			
			return (count > 0);
			
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return false;

	}
	
	/***************************************************************
	 * Checks if a records with the given value already exists,
	 * ignores the value of the record passed to this method.
	 * @return Returns true if exists, false otherwise.
	 ****************************************************************/
	public static boolean checkExistsByIgnoreSelf(CFWObject object, String column, Object value ) {
		
		try {

			if(object.getPrimaryKeyValue() != null
			&& object.getPrimaryKeyValue() > 0) {
				int count = object
								.queryCache(object.getClass(), "CFWDBDefaultOperations.checkExistsByIgnoreSelf"+column)
								.selectCount()
								.where(column, value)
								.and().not().is(object.getPrimaryKeyFieldname(), object.getPrimaryKeyValue())
								.limit(1)
								.executeCount();

				return (count > 0);
			}else {
				return checkExistsBy(object.getClass(), column, value );
			}
			
		} catch (Exception e) {
			new CFWLog(logger)
				.warn("Error while instanciating object.", e);
		} 
		
		return false;

	}
	
}

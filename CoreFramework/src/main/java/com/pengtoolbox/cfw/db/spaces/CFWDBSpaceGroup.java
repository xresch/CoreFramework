package com.pengtoolbox.cfw.db.spaces;

import java.sql.ResultSet;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.db.spaces.SpaceGroup.SpaceGroupFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBSpaceGroup {

	public static Logger logger = CFWLog.getLogger(CFWDBSpaceGroup.class.getName());
	
	/********************************************************************************************
	 * Creates multiple SpaceGroups in the DB.
	 * @param SpaceGroups with the values that should be inserted. ID will be set by the Database.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void create(SpaceGroup... SpaceGroups) {
		
		for(SpaceGroup SpaceGroup : SpaceGroups) {
			create(SpaceGroup);
		}
	}
	
	/********************************************************************************************
	 * Creates a new SpaceGroup in the DB.
	 * @param CFWSpace with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(SpaceGroup SpaceGroup) {
		
		if(SpaceGroup == null) {
			new CFWLog(logger)
				.method("create")
				.warn("The SpaceGroup cannot be null");
			return false;
		}
		
		if(SpaceGroup.name() == null || SpaceGroup.name().isEmpty()) {
			new CFWLog(logger)
				.method("create")
				.warn("Please specify a name for the SpaceGroup to create.");
			return false;
		}
		
		if(checkSpaceGroupExists(SpaceGroup)) {
			new CFWLog(logger)
				.method("create")
				.warn("The SpaceGroup '"+SpaceGroup.name()+"' cannot be created as a SpaceGroup with this name already exists.");
			return false;
		}
		
		return SpaceGroup
				.queryCache(CFWDBSpaceGroup.class, "create")
				.insert();
	}
	
	/***************************************************************
	 * Select a SpaceGroup by it's name.
	 * @param id of the SpaceGroup
	 * @return Returns a SpaceGroup or null if not found or in case of exception.
	 ****************************************************************/
	public static SpaceGroup selectByName(String name) {
		
		return (SpaceGroup)new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "selectByName")
				.select()
				.where(SpaceGroupFields.NAME.toString(), name)
				.getFirstObject();

	}
	
	/***************************************************************
	 * Select a SpaceGroup by it's ID.
	 * @param id of the SpaceGroup
	 * @return Returns a SpaceGroup or null if not found or in case of exception.
	 ****************************************************************/
	public static SpaceGroup selectByID(int id ) {

		return (SpaceGroup)new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "selectByID")
				.select()
				.where(SpaceGroupFields.PK_ID.toString(), id)
				.getFirstObject();
		
	}
	
	/***************************************************************
	 * Select a SpaceGroup by it's ID and return it as JSON string.
	 * @param id of the SpaceGroup
	 * @return Returns a SpaceGroup or null if not found or in case of exception.
	 ****************************************************************/
	public static String getSpaceGroupsAsJSON(String id) {
		
		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "getSpaceGroupsAsJSON")
				.select()
				.where(SpaceGroupFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user SpaceGroups
	 * 
	 * @return Returns a resultSet with all SpaceGroups or null.
	 ****************************************************************/
	public static ResultSet getSpaceGroupList() {
		
		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "getSpaceGroupList")
				.select()
				.orderby(SpaceGroupFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all user SpaceGroups as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getSpaceGroupListAsJSON() {
		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "getSpaceGroupListAsJSON")
				.select()
				.orderby(SpaceGroupFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param SpaceGroup
	 * @return true or false
	 ****************************************************************/
	public static boolean update(SpaceGroup SpaceGroup) {
		
		if(SpaceGroup == null) {
			new CFWLog(logger)
				.method("update")
				.warn("The SpaceGroup that should be updated cannot be null");
			return false;
		}
		
		if(SpaceGroup.name() == null || SpaceGroup.name().isEmpty()) {
			new CFWLog(logger)
				.method("update")
				.warn("Please specify a name for the SpaceGroup.");
			return false;
		}
				
		return SpaceGroup
				.queryCache(CFWDBSpaceGroup.class, "update")
				.update();
		
	}
	
	/****************************************************************
	 * Deletes the SpaceGroup by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "deleteByID")
				.delete()
				.where(SpaceGroupFields.PK_ID.toString(), id)
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
			.method("deleteMultipleByID")
			.severe("The userID's '"+resultIDs+"' are not a comma separated list of strings.");
			return false;
		}

		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "deleteMultipleByID")
				.delete()
				.whereIn(SpaceGroupFields.PK_ID.toString(), resultIDs)
				.executeDelete();
					
	}
	
	/****************************************************************
	 * Deletes the SpaceGroup by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByName(String name) {
		
		SpaceGroup SpaceGroup = selectByName(name);
		if(SpaceGroup != null) {
			new CFWLog(logger)
			.method("deleteByName")
			.severe("The SpaceGroup '"+SpaceGroup.name()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		return new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "deleteByName")
				.delete()
				.where(SpaceGroupFields.NAME.toString(), name)
				.executeDelete();
					
	}
	
	
	/****************************************************************
	 * Check if the SpaceGroup exists by name.
	 * 
	 * @param SpaceGroup to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkSpaceGroupExists(SpaceGroup SpaceGroup) {
		if(SpaceGroup != null) {
			return checkSpaceGroupExists(SpaceGroup.name());
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the SpaceGroup exists by name.
	 * 
	 * @param SpaceGroupname to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkSpaceGroupExists(String SpaceGroupName) {
		
		int count = new SpaceGroup()
				.queryCache(CFWDBSpaceGroup.class, "checkSpaceGroupExists")
				.selectCount()
				.where(SpaceGroupFields.NAME.toString(), SpaceGroupName)
				.getCount();
		
		return (count > 0);
		
	}
	
}

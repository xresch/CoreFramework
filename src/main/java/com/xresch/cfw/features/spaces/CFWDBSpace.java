package com.xresch.cfw.features.spaces;

import java.sql.ResultSet;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.features.spaces.Space.SpaceFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBSpace {
	
	private static final Logger logger = CFWLog.getLogger(CFWDBSpace.class.getName());
	
	
	/********************************************************************************************
	 * Creates a new space in the DB.
	 * @param CFWSpace with the values that should be inserted. ID will be set by the Database.
	 * @return id of created element if successful, null otherwise
	 * 
	 ********************************************************************************************/
	public static Integer create(Integer parentID, Space space) {
		
		if(space == null) {
			new CFWLog(logger)
				.warn("The space cannot be null.", new IllegalArgumentException());
			return null;
		}
		
		if(space.name() == null || space.name().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a name for the space to create.", new IllegalStateException());
			return null;
		}
		
		if(checkSpaceExists(space)) {
			new CFWLog(logger)
				.warn("The space '"+space.name()+"' cannot be created as a space with this name already exists.");
			return null;
		}
		
		return CFWHierarchy.create(parentID, space);
	}
	
	/***************************************************************
	 * Select a space by it's name.
	 * @param id of the space
	 * @return Returns a space or null if not found or in case of exception.
	 ****************************************************************/
	public static Space selectByName(String name) {
		
		return (Space)new Space()
				.queryCache(CFWDBSpace.class, "selectByName")
				.select()
				.where(SpaceFields.NAME.toString(), name)
				.getFirstAsObject();

	}
	
	/***************************************************************
	 * Select a space by it's ID.
	 * @param id of the space
	 * @return Returns a space or null if not found or in case of exception.
	 ****************************************************************/
	public static Space selectByID(int id ) {

		return (Space)new Space()
				.queryCache(CFWDBSpace.class, "selectByID")
				.select()
				.where(SpaceFields.PK_ID.toString(), id)
				.getFirstAsObject();
		
	}
	
	/***************************************************************
	 * Select a space by it's ID and return it as JSON string.
	 * @param id of the space
	 * @return Returns a space or null if not found or in case of exception.
	 ****************************************************************/
	public static String getUserSpacesAsJSON(String id) {
		
		return new Space()
				.queryCache(CFWDBSpace.class, "getUserSpacesAsJSON")
				.select()
				.where(SpaceFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user spaces
	 * 
	 * @return Returns a resultSet with all spaces or null.
	 ****************************************************************/
	public static ResultSet getUserSpaceList() {
		
		return new Space()
				.queryCache(CFWDBSpace.class, "getUserSpaceList")
				.select()
				.orderby(SpaceFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all user spaces as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserSpaceListAsJSON() {
		return new Space()
				.queryCache(CFWDBSpace.class, "getUserSpaceListAsJSON")
				.select()
				.orderby(SpaceFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param space
	 * @return true or false
	 ****************************************************************/
	public static boolean update(Space space) {
		
		if(space == null) {
			new CFWLog(logger)
				.warn("The space that should be updated cannot be null");
			return false;
		}
		
		if(space.name() == null || space.name().isEmpty()) {
			new CFWLog(logger)
				.warn("Please specify a name for the space.");
			return false;
		}
				
		return space
				.queryCache(CFWDBSpace.class, "update")
				.update();
		
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified space.
	 * @param space
	 * @return Hashmap with spaces(key=space name, value=space object), or null on exception
	 ****************************************************************/
//	public static HashMap<String, Permission> selectPermissionsForSpace(Space space) {
//		return CFW.DB.SpacePermissionMap.selectPermissionsForSpace(space);
//	}
	
	/****************************************************************
	 * Deletes the space by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		Space space = selectByID(id);
		if(space != null && space.isDeletable() == false) {
			new CFWLog(logger)
			.severe("The space '"+space.name()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		return new Space()
				.queryCache(CFWDBSpace.class, "deleteByID")
				.delete()
				.where(SpaceFields.PK_ID.toString(), id)
				.and(SpaceFields.IS_DELETABLE.toString(), true)
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

		return new Space()
				.queryCache(CFWDBSpace.class, "deleteMultipleByID")
				.delete()
				.whereIn(SpaceFields.PK_ID.toString(), resultIDs)
				.and(SpaceFields.IS_DELETABLE.toString(), true)
				.executeDelete();
					
	}
	
	/****************************************************************
	 * Deletes the space by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByName(String name) {
		
		Space space = selectByName(name);
		if(space != null && space.isDeletable() == false) {
			new CFWLog(logger)
			.severe("The space '"+space.name()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		return new Space()
				.queryCache(CFWDBSpace.class, "deleteByName")
				.delete()
				.where(SpaceFields.NAME.toString(), name)
				.and(SpaceFields.IS_DELETABLE.toString(), true)
				.executeDelete();
					
	}
	
	
	/****************************************************************
	 * Check if the space exists by name.
	 * 
	 * @param space to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkSpaceExists(Space space) {
		if(space != null) {
			return checkSpaceExists(space.name());
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the space exists by name.
	 * 
	 * @param spacename to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkSpaceExists(String spaceName) {
		
		int count = new Space()
				.queryCache(CFWDBSpace.class, "checkSpaceExists")
				.selectCount()
				.where(SpaceFields.NAME.toString(), spaceName)
				.executeCount();
		
		return (count > 0);
		
	}
	
}

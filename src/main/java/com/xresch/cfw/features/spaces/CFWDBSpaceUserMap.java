package com.xresch.cfw.features.spaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpaceUserMap.CFWSpaceUserMapFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWDBSpaceUserMap {

	private static final String TABLE_NAME = new CFWSpaceUserMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBSpaceUserMap.class.getName());
	
	/********************************************************************************************
	 * Adds the user to the specified space.
	 * @param user
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToSpace(User user, CFWSpace space) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(space == null) {
			new CFWLog(logger)
				.warn("Space cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || space.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or Space-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToSpace(user, space)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is already in the favorites of the space '"+space.name()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWSpaceUserMapFields.FK_ID_USER +", "
				  + CFWSpaceUserMapFields.FK_ID_SPACE
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceUserMap.class, "Add User to CFWSpace Space: "+space.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				space.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceAdminsMap.class, "Add Admin to Space: "+space.name()+", User: "+user.username());
		}

		return success;
		
	}
	/********************************************************************************************
	 * Adds the user to the specified space.
	 * @param userID
	 * @param spaceID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToSpace(int userID, int spaceID) {
		
		
		if(userID < 0 || spaceID < 0) {
			new CFWLog(logger)
				.warn("User-ID or space-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToSpace(userID, spaceID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the space '"+spaceID+"'.");
			return false;
		}
		
		CFWSpace space = CFW.DB.Spaces.selectByID(spaceID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToSpace(user, space);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified space.
	 * @param user
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserSpaceAssignments(CFWSpace space, LinkedHashMap<String,String> usersKeyLabel) {
				
		//----------------------------------------
		// Clean all and Add all New
		
		// only returns true if anything was updated. Therefore cannot include in check.
		boolean hasCleared = new CFWSQL(new CFWSpaceUserMap())
						.delete()
						.where(CFWSpaceUserMapFields.FK_ID_SPACE, space.id())
						.executeDelete();
		if(hasCleared) {
			new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWSpaceAdminsMap.class, "Clear Space Admins for Space: "+space.name());
		}
		
		boolean isSuccess = true;
		for(String userID : usersKeyLabel.keySet()) {
			isSuccess &= assignUserToSpace(Integer.parseInt(userID), space.id());
		}
		
		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified space.
	 * @param user
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromSpace(User user, CFWSpace space) {
		
		if(user == null || space == null ) {
			new CFWLog(logger)
				.warn("User and Space cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || space.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and Space-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToSpace(user, space)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to space '"+space.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromCFWSpaceSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWSpaceUserMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + CFWSpaceUserMapFields.FK_ID_SPACE +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceUserMap.class, "Remove User from Space: "+space.name()+", User: "+user.username());
		
		return CFWDB.preparedExecute(removeUserFromCFWSpaceSQL, 
				user.id(),
				space.id()
				);
	}
	/********************************************************************************************
	 * Remove a user from the space.
	 * @param user
	 * @param space
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromSpace(int userID, int spaceID) {
		
		if(!checkIsUserAssignedToSpace(userID, spaceID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the space '"+ spaceID+"' and cannot be removed.");
			return false;
		}
				
		CFWSpace space = CFW.DB.Spaces.selectByID(spaceID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromSpace(user, space);

	}
	
	/****************************************************************
	 * Check if the user is in the given space.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToSpace(User user, CFWSpace space) {
		
		if(user != null && space != null) {
			return checkIsUserAssignedToSpace(user.id(), space.id());
		}else {
			new CFWLog(logger)
				.severe("The user and space cannot be null. User: '"+user+"', CFWSpace: '"+space+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToSpace(int userid, int spaceid) {
		
		return 0 != new CFWSQL(new CFWSpaceUserMap())
			.queryCache()
			.selectCount()
			.where(CFWSpaceUserMapFields.FK_ID_USER.toString(), userid)
			.and(CFWSpaceUserMapFields.FK_ID_SPACE.toString(), spaceid)
			.executeCount();

	}

	/***************************************************************
	 * Retrieve the spaces for a user as key/labels.
	 * Useful for autocomplete.
	 * @param space
	 * @return ResultSet
	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForSpaceAsKeyLabel(int spaceID) {
		
		ArrayList<CFWObject> userList =  new CFWSQL(new User())
				.queryCache()
				.loadSQLResource(FeatureSpace.PACKAGE_RESOURCE
						, "sql_selectSpacesForUsersKeyValue.sql"
						, spaceID)
				.getAsObjectList();
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(CFWObject userObject : userList) {
			User user = (User) userObject;
						
			result.put(user.id()+"", user.createUserLabel());
			
		}
		
		return result;
	}
	
	/***************************************************************
	 * Retrieve the users for the specified space.
	 * @param space
	 * @return Hashmap with users(key=space name), or null on exception
	 ****************************************************************/
//	public static ResultSet selectUsersForSpaceAsResultSet(CFWSpace space) {
//		
//		if( space == null) {
//			new CFWLog(logger)
//				.severe("The space cannot be null.");
//			return null;
//		}
//		
//		return new CFWSQL(space)
//				.queryCache()
//				.custom(
//					"SELECT P.* "
//					+"FROM CFW_PERMISSION P "
//					+"JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_DASHBOARD = P.PK_ID "
//					+"JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_USER = GP.FK_ID_USER "
//					+"WHERE UG.FK_ID_USER = ?;", 
//					space.id())
//				.getResultSet();
//		
//	}
	
	/***************************************************************
	 * Retrieve the user overview for all spaces.
	 * @param space
	 * @return ResultSet
	 ****************************************************************/
//	public static ResultSet getUserOverview() {
//		
//		return new CFWSQL(new User())
//				.queryCache()
//				.loadSQLResource(FeatureUsers.PACKAGE_RESOURCE, "sql_userOverviewAllCFWSpaces.sql")
//				.getResultSet();
//		
//	}
	
	/***************************************************************
	 * Retrieve the user overview for the specified space.
	 ****************************************************************/
//	public static JsonArray getUserOverview(CFWSpace space) {
//		
//		return new CFWSQL(new User())
//				.queryCache()
//				.loadSQLResource(FeatureUsers.PACKAGE_RESOURCE, "sql_userOverviewForCFWSpace.sql", space.id())
//				.getAsJSONArray();
//		
//	}
//	
	
	/***************************************************************
	 * Returns a list of all spaces and if the space is part of them 
	 * as a json array.
	 * @param space
	 * @return Hashmap with spaces(key=space name, value=space object), or null on exception
	 ****************************************************************/
//	public static String getUserMapForCFWSpaceAsJSON(String spaceID) {
//		
//		//----------------------------------
//		// Check input format
//		if(spaceID == null ^ !spaceID.matches("\\d+")) {
//			new CFWLog(logger)
//			.severe("The spaceID '"+spaceID+"' is not a number.");
//			return "[]";
//		}
//		
//		String sqlString = "SELECT P.PK_ID, P.NAME, P.DESCRIPTION, M.FK_ID_USER AS ITEM_ID, M.IS_DELETABLE FROM "+User.TABLE_NAME+" P "
//				+ " LEFT JOIN "+OMDBUsersSpaceMap.TABLE_NAME+" M "
//				+ " ON M.FK_ID_DASHBOARD = P.PK_ID"
//				+ " AND M.FK_ID_USER = ?"
//				+ " ORDER BY LOWER(P.NAME)";;
//		
//		ResultSet result = CFWDB.preparedExecuteQuery(sqlString, 
//				spaceID);
//		
//		String json = ResultSetUtils.toJSON(result);
//		CFWDB.close(result);	
//		return json;
//
//	}
	
	/***************************************************************
	 * Remove the user from the space if it is assigned to the space, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToSpace(String userID, String spaceID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(spaceID == null ^ !spaceID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The spaceID '"+spaceID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToSpace(Integer.parseInt(userID), Integer.parseInt(spaceID));
		
	}
	
	/***************************************************************
	 * Remove the user from the space if it is assigned to the space, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToSpace(int userID, int spaceID) {
		
		if(checkIsUserAssignedToSpace(userID, spaceID)) {
			return removeUserFromSpace(userID, spaceID);
		}else {
			return assignUserToSpace(userID, spaceID);
		}

	}
		
}

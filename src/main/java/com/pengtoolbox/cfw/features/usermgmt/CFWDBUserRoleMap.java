package com.pengtoolbox.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.features.usermgmt.UserRoleMap.UserRoleMapFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBUserRoleMap {

	public static String TABLE_NAME = new UserRoleMap().getTableName();
	
	public static Logger logger = CFWLog.getLogger(CFWDBUserRoleMap.class.getName());
	
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param rolename
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToRole(User user, String rolename, boolean isDeletable) {
		return addUserToRole(user, CFW.DB.Roles.selectFirstByName(rolename), isDeletable);
	}
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToRole(User user, Role role, boolean isDeletable) {
		
		if(user == null || role == null ) {
			new CFWLog(logger)
				.method("addUserToRole")
				.warn("User and role cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.method("addUserToRole")
				.warn("User-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserInRole(user, role)) {
			new CFWLog(logger)
				.method("addUserToRole")
				.warn("The user '"+user.username()+"' is already part of the role '"+role.name()+"'.");
			return false;
		}
		
		return addUserToRole(user.id(), role.id(), isDeletable);
	}
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @param isdeletable, define if this association can be deleted
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToRole(int userid, int roleid, boolean isDeletable) {
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + UserRoleMapFields.FK_ID_USER +", "
				  + UserRoleMapFields.FK_ID_ROLE +", "
				  + UserRoleMapFields.IS_DELETABLE +" "
				  + ") VALUES (?,?,?);";
		
		return CFWDB.preparedExecute(insertRoleSQL, 
				userid,
				roleid,
				isDeletable
				);
	}
	
	/********************************************************************************************
	 * Remove a user from the role.
	 * @param user
	 * @param role
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromRole(User user, Role role) {
		
		if(user == null || role == null ) {
			new CFWLog(logger)
				.method("removeUserFromRole")
				.warn("User and role cannot be null.");
			return false;
		}
		
		if(!checkIsUserInRole(user, role)) {
			new CFWLog(logger)
				.method("removeUserFromRole")
				.warn("The user '"+user.username()+"' is not part of the role '"+role.name()+"' and cannot be removed.");
			return false;
		}
		
		return removeUserFromRole(user.id(), role.id());
	}

	/********************************************************************************************
	 * Remove a user from the role.
	 * @param user
	 * @param role
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromRole(int userID, int roleID) {
		String removeUserFromRoleSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + UserRoleMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + UserRoleMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + UserRoleMapFields.IS_DELETABLE +" = TRUE "
				  + ";";
		
		return CFWDB.preparedExecute(removeUserFromRoleSQL, 
				userID,
				roleID
				);
	}
	
	/********************************************************************************************
	 * Update if the user can be deleted.
	 * @param user
	 * @param role
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateIsDeletable(int userID, int roleID, boolean isDeletable) {
		String removeUserFromRoleSQL = "UPDATE "+TABLE_NAME
				+" SET "+ UserRoleMapFields.IS_DELETABLE +" = ? "
				+" WHERE "
				  + UserRoleMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + UserRoleMapFields.FK_ID_ROLE +" = ? "
				  + ";";
		
		return CFWDB.preparedExecute(removeUserFromRoleSQL, 
				isDeletable,
				userID,
				roleID
				);
	}
	
	/****************************************************************
	 * Check if the user is in the given role.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserInRole(User user, Role role) {
		
		if(user != null && role != null) {
			return checkIsUserInRole(user.id(), role.id());
		}else {
			new CFWLog(logger)
				.method("checkIsUserInRole")
				.severe("The user and role cannot be null. User: '"+user+"', Role: '"+role+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserInRole(int userid, int roleid) {
		
		String checkIsUserInRole = "SELECT COUNT(*) FROM "+TABLE_NAME
				+" WHERE "+UserRoleMapFields.FK_ID_USER+" = ?"
				+" AND "+UserRoleMapFields.FK_ID_ROLE+" = ?";
		
		ResultSet result = CFW.DB.preparedExecuteQuery(checkIsUserInRole, userid, roleid);
		
		try {
			if(result != null && result.next()) {
				int count = result.getInt(1);
				return (count == 0) ? false : true;
			}
		} catch (Exception e) {
			new CFWLog(logger)
			.method("roleExists")
			.severe("Exception occured while checking of role exists.", e);
			
			return false;
		}finally {
			CFWDB.close(result);
		}
		
		
		return false;
	}
	

	/***************************************************************
	 * Select user roles by the user id.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Role> selectAllRolesForUser(User user) {
		if( user == null) {
			new CFWLog(logger)
				.method("create")
				.severe("The user cannot be null");
			return null;
		}
		
		return selectAllRolesForUser(user.id());
	}
	
	/***************************************************************
	 * Select user roles by the user id.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Role> selectAllRolesForUser(int userID) {
		
		
		String selectRolesForUser = "SELECT * FROM "+Role.TABLE_NAME+" G "
				+ " INNER JOIN "+CFWDBUserRoleMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_ROLE = G.PK_ID "
				+ " WHERE M.FK_ID_USER = ?";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectRolesForUser, 
				userID);
		
		HashMap<String, Role> roleMap = new HashMap<String, Role>(); 
		
		try {
			while(result != null && result.next()) {
				Role role = new Role(result);
				roleMap.put(role.name(), role);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.method("selectRolesForUser")
			.severe("Error while selecting roles for the user with id '"+userID+"'.", e);
			return null;
		}finally {
			CFWDB.close(result);
		}
		
		return roleMap;
	
	}
	
	/***************************************************************
	 * Returns a list of all roles and if the user is part of them 
	 * as a json array.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static String getUserRoleMapForUserAsJSON(String userID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
			.method("deleteMultipleByID")
			.severe("The userID '"+userID+"' is not a number.");
			return "[]";
		}
		
		String selectRolesForUser = "SELECT G.PK_ID, G.NAME, G.DESCRIPTION, M.FK_ID_USER AS ITEM_ID, M.IS_DELETABLE FROM "+Role.TABLE_NAME+" G "
				+ " LEFT JOIN "+CFWDBUserRoleMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_ROLE = G.PK_ID "
				+ " AND G.CATEGORY = ?"
				+ " AND M.FK_ID_USER = ?"
				+ " ORDER BY LOWER(G.NAME)";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectRolesForUser, 
				"user",
				userID);
		String json = CFWDB.resultSetToJSON(result);
		CFWDB.close(result);	
		return json;

	}
	
	/***************************************************************
	 * Remove the user from the role if it is a member of the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserInRole(String userID, String roleID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInRole")
			.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInRole")
			.severe("The roleID '"+userID+"' is not a number.");
			return false;
		}
		
		return toggleUserInRole(Integer.parseInt(userID), Integer.parseInt(roleID));
		
	}
	
	/***************************************************************
	 * Remove the user from the role if it is a member of the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toggleUserInRole(int userID, int roleID) {
		
		if(checkIsUserInRole(userID, roleID)) {
			return removeUserFromRole(userID, roleID);
		}else {
			return addUserToRole(userID, roleID, true);
		}

	}
		
}

package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.usermgmt.UserRoleMap.UserRoleMapFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBUserRoleMap {

	public static final String TABLE_NAME = new UserRoleMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBUserRoleMap.class.getName());
	
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param rolename
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addRoleToUser(User user, String rolename, boolean isDeletable) {
		return addRoleToUser(user, CFW.DB.Roles.selectFirstByName(rolename), isDeletable);
	}
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addRoleToUser(User user, Role role, boolean isDeletable) {
		
		if(user == null || role == null ) {
			new CFWLog(logger)
				.warn("User and role cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserInRole(user, role)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is already part of the role '"+role.name()+"'.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + UserRoleMapFields.FK_ID_USER +", "
				  + UserRoleMapFields.FK_ID_ROLE +", "
				  + UserRoleMapFields.IS_DELETABLE +" "
				  + ") VALUES (?,?,?);";
		
		new CFWLog(logger).audit("UPDATE", User.class, "Add Role to User: "+user.username()+", Role: "+role.name());
		return CFWDB.preparedExecute(insertRoleSQL, 
				user.id(),
				role.id(),
				isDeletable
				);
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @param isdeletable, define if this association can be deleted
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addRoleToUser(int userID, int roleID, boolean isDeletable) {
		User user = CFW.DB.Users.selectByID(userID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return addRoleToUser(user, role, isDeletable);
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
				.warn("User and role cannot be null.");
			return false;
		}
		
		if(!checkIsUserInRole(user, role)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not part of the role '"+role.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromRoleSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + UserRoleMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + UserRoleMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + UserRoleMapFields.IS_DELETABLE +" = TRUE "
				  + ";";
		
		new CFWLog(logger).audit("UPDATE", User.class, "Remove Role from User: "+user.username()+", Role: "+role.name());
		return CFWDB.preparedExecute(removeUserFromRoleSQL, 
				user.id(),
				role.id()
				);
		
	}

	/********************************************************************************************
	 * Remove a user from the role.
	 * @param user
	 * @param role
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromRole(int userID, int roleID) {
		User user = CFW.DB.Users.selectByID(userID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeUserFromRole(user, role);
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
	public static boolean checkIsUserInRole(Integer userid, Integer roleid) {
		
		if(userid == null || roleid == null) {
			return false;
		}
		
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
	public static HashMap<Integer, Role> selectAllRolesForUser(User user) {
		if( user == null) {
			new CFWLog(logger)
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
	public static HashMap<Integer, Role> selectAllRolesForUser(Integer userID) {
		
		if(userID == null) {
			return new HashMap<Integer, Role>();
		}
		
		String selectRolesForUser = "SELECT * FROM "+Role.TABLE_NAME+" G "
				+ " INNER JOIN "+CFWDBUserRoleMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_ROLE = G.PK_ID "
				+ " WHERE M.FK_ID_USER = ?";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectRolesForUser, 
				userID);
		
		HashMap<Integer, Role> roleMap = new HashMap<Integer, Role>(); 
		
		try {
			while(result != null && result.next()) {
				Role role = new Role(result);
				roleMap.put(role.id(), role);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
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
		if(userID == null || !userID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(roleID == null || !roleID.matches("\\d+")) {
			new CFWLog(logger)
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
			return addRoleToUser(userID, roleID, true);
		}

	}
		
}

package com.pengtoolbox.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.features.usermgmt.RolePermissionMap.RolePermissionMapFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBRolePermissionMap {

	private static final String TABLE_NAME = new RolePermissionMap().getTableName();
	
	public static Logger logger = CFWLog.getLogger(CFWDBRolePermissionMap.class.getName());
	

	/********************************************************************************************
	 * Adds the permission to the specified role.
	 * @param permission
	 * @param role
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addPermissionToRole(Permission permission, Role role, boolean isDeletable) {
		
		if(permission == null) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Permission cannot be null.");
			return false;
		}
		
		if(role == null) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Role cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Permission-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsPermissionInRole(permission, role)) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("The permission '"+permission.name()+"' is already part of the role '"+role.name()+"'.");
			return false;
		}
		
		return addPermissionToRole(permission.id(), role.id(), isDeletable);
	}
	/********************************************************************************************
	 * Adds the permission to the specified role.
	 * @param permissionID
	 * @param roleID
	 * @return return true if permission was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addPermissionToRole(int permissionID, int roleID, boolean isDeletable) {
		
		
		if(permissionID < 0 || roleID < 0) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Permission-ID or role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsPermissionInRole(permissionID, roleID)) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("The permission '"+permissionID+"' is already part of the role '"+roleID+"'.");
			return false;
		}
		
		String insertPermissionSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + RolePermissionMapFields.FK_ID_PERMISSION +", "
				  + RolePermissionMapFields.FK_ID_ROLE +", "
				  + RolePermissionMapFields.IS_DELETABLE +" "
				  + ") VALUES (?,?,?);";
		
		return CFWDB.preparedExecute(insertPermissionSQL, 
				permissionID,
				roleID,
				isDeletable
				);
	}
	
	/********************************************************************************************
	 * Update if the permission can be deleted.
	 * @param user
	 * @param role
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateIsDeletable(int permissionID, int roleID, boolean isDeletable) {
		String removeUserFromRoleSQL = "UPDATE "+TABLE_NAME
				+" SET "+ RolePermissionMapFields.IS_DELETABLE +" = ? "
				+" WHERE "
				  + RolePermissionMapFields.FK_ID_PERMISSION +" = ? "
				  + " AND "
				  + RolePermissionMapFields.FK_ID_ROLE +" = ? "
				  + ";";
		
		return CFWDB.preparedExecute(removeUserFromRoleSQL, 
				isDeletable,
				permissionID,
				roleID
				);
	}
	/********************************************************************************************
	 * Adds the permission to the specified role.
	 * @param permission
	 * @param role
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removePermissionFromRole(Permission permission, Role role) {
		
		if(permission == null || role == null ) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Permission and role cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("Permission-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsPermissionInRole(permission, role)) {
			new CFWLog(logger)
				.method("addPermissionToRole")
				.warn("The permission '"+permission.name()+"' is not part of the role '"+role.name()+"' and cannot be removed.");
			return false;
		}
		
		return removePermissionFromRole(permission.id(), role.id());
	}
	/********************************************************************************************
	 * Remove a permission from the role.
	 * @param permission
	 * @param role
	 * @return return true if permission was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removePermissionFromRole(int permissionID, int roleID) {
		
		if(!checkIsPermissionInRole(permissionID, roleID)) {
			new CFWLog(logger)
				.method("removePermissionFromRole")
				.warn("The permission '"+permissionID+"' is not part of the role '"+ roleID+"' and cannot be removed.");
			return false;
		}
		
		String removePermissionFromRoleSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + RolePermissionMapFields.FK_ID_PERMISSION +" = ? "
				  + " AND "
				  + RolePermissionMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + RolePermissionMapFields.IS_DELETABLE +" = TRUE "
				  + ";";
		
		return CFWDB.preparedExecute(removePermissionFromRoleSQL, 
				permissionID,
				roleID
				);
	}
	
	/****************************************************************
	 * Check if the permission is in the given role.
	 * 
	 * @param permission to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsPermissionInRole(Permission permission, Role role) {
		
		if(permission != null && role != null) {
			return checkIsPermissionInRole(permission.id(), role.id());
		}else {
			new CFWLog(logger)
				.method("checkIsPermissionInRole")
				.severe("The user and role cannot be null. User: '"+permission+"', Role: '"+role+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the permission exists by name.
	 * 
	 * @param permission to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsPermissionInRole(int permissionid, int roleid) {
		
		return 0 != new RolePermissionMap()
			.queryCache(CFWDBRolePermissionMap.class, "checkIsPermissionInRole")
			.selectCount()
			.where(RolePermissionMapFields.FK_ID_PERMISSION.toString(), permissionid)
			.and(RolePermissionMapFields.FK_ID_ROLE.toString(), roleid)
			.getCount();
			
			
//		String checkIsPermissionInRole = "SELECT COUNT(*) FROM "+TABLE_NAME
//				+" WHERE "+RolePermissionMapFields.FK_ID_PERMISSION+" = ?"
//				+" AND "+RolePermissionMapFields.FK_ID_ROLE+" = ?";
//		
//		ResultSet result = CFW.DB.preparedExecuteQuery(checkIsPermissionInRole, permissionid, roleid);
//		
//		try {
//			if(result != null && result.next()) {
//				int count = result.getInt(1);
//				return (count == 0) ? false : true;
//			}
//		} catch (Exception e) {
//			new CFWLog(logger)
//			.method("checkIsPermissionInRole")
//			.severe("Exception occured while checking of role exists.", e);
//			
//			return false;
//		}finally {
//			CFWDB.close(result);
//		}
//		
//		
//		return false;
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified role.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForRole(Role role) {
		
		if( role == null) {
			new CFWLog(logger)
				.method("create")
				.severe("The user cannot be null");
			return null;
		}
		
		String selectPermissionsForRole = "SELECT P.* FROM "+Permission.TABLE_NAME+" P "
				+ " INNER JOIN "+CFWDBRolePermissionMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_PERMISSION = P.PK_ID "
				+ " WHERE M.FK_ID_ROLE = ?";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectPermissionsForRole, 
				role.id());
		
		HashMap<String, Permission> permissionMap = new HashMap<String, Permission>(); 
		
		try {
			while(result != null && result.next()) {
				Permission permission = new Permission(result);
				permissionMap.put(permission.name(), permission);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.method("selectRolesForUser")
			.severe("Error while selecting permissions for the role '"+role.name()+"'.", e);
			return null;
		}finally {
			CFWDB.close(result);
		}
		
		return permissionMap;
	
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForUser(User user) {
		
		ResultSet result = selectPermissionsForUserResultSet(user);
		
		HashMap<String, Permission> permissionMap = new HashMap<String, Permission>(); 
		try {
			while(result != null && result.next()) {
				Permission permission = new Permission(result);
				permissionMap.put(permission.name(), permission);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.method("selectRolesForUser")
			.severe("Error while selecting permissions for the role '"+user.username()+"'.", e);
			return null;
		}finally {
			CFWDB.close(result);
		}
		
		return permissionMap;
		
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static ResultSet selectPermissionsForUserResultSet(User user) {
		
		if( user == null) {
			new CFWLog(logger)
				.method("create")
				.severe("The user cannot be null.");
			return null;
		}
		
		return new CFWSQL(user)
				.queryCache(CFWDBRolePermissionMap.class, "selectPermissionsForUserResultSet")
				.custom(
					"SELECT P.* "
					+"FROM CFW_PERMISSION P "
					+"JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_PERMISSION = P.PK_ID "
					+"JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_ROLE = GP.FK_ID_ROLE "
					+"WHERE UG.FK_ID_USER = ?;", 
					user.id())
				.getResultSet();
		
	}
	
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static ResultSet getPermissionOverview() {
		
		return new CFWSQL(new Permission())
				.queryCache(CFWDBRolePermissionMap.class, "getPermissionOverview")
				.custom(
					"SELECT U.USERNAME, G.NAME AS ROLENAME, P.NAME AS PERMISSION"
					+" FROM CFW_USER U"
					+" LEFT JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_USER = U.PK_ID"
					+" LEFT JOIN CFW_ROLE AS G ON UG.FK_ID_ROLE = G.PK_ID"
					+" LEFT JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_ROLE = G.PK_ID"
					+" LEFT JOIN CFW_PERMISSION AS P ON GP.FK_ID_PERMISSION = P.PK_ID"
					+" ORDER BY LOWER(U.USERNAME), LOWER(G.NAME), LOWER(P.NAME)")
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Returns a list of all roles and if the user is part of them 
	 * as a json array.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static String getPermissionMapForRoleAsJSON(String roleID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
			.method("getPermissionMapForRoleAsJSON")
			.severe("The roleID '"+roleID+"' is not a number.");
			return "[]";
		}
		
		String sqlString = "SELECT P.PK_ID, P.NAME, P.DESCRIPTION, M.FK_ID_ROLE AS ITEM_ID, M.IS_DELETABLE FROM "+Permission.TABLE_NAME+" P "
				+ " LEFT JOIN "+CFWDBRolePermissionMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_PERMISSION = P.PK_ID"
				+ " AND M.FK_ID_ROLE = ?"
				+ " ORDER BY LOWER(P.NAME)";;
		
		ResultSet result = CFWDB.preparedExecuteQuery(sqlString, 
				roleID);
		
		String json = CFWDB.resultSetToJSON(result);
		CFWDB.close(result);	
		return json;

	}
	/***************************************************************
	 * Remove the user from the role if it is a member of the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean tooglePermissionInRole(String permissionID, String roleID) {
		
		//----------------------------------
		// Check input format
		if(permissionID == null ^ !permissionID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInRole")
			.severe("The userID '"+permissionID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInRole")
			.severe("The roleID '"+permissionID+"' is not a number.");
			return false;
		}
		
		return tooglePermissionInRole(Integer.parseInt(permissionID), Integer.parseInt(roleID));
		
	}
	
	/***************************************************************
	 * Remove the user from the role if it is a member of the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean tooglePermissionInRole(int userID, int roleID) {
		
		if(checkIsPermissionInRole(userID, roleID)) {
			return removePermissionFromRole(userID, roleID);
		}else {
			return addPermissionToRole(userID, roleID, true);
		}

	}
		
}

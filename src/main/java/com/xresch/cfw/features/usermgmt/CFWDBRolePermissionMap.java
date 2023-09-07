package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.usermgmt.Permission.PermissionFields;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.features.usermgmt.RolePermissionMap.RolePermissionMapFields;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBRolePermissionMap {

	private static final String TABLE_NAME = new RolePermissionMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBRolePermissionMap.class.getName());
	
	// Cache<UserID, HashMap<PermissionName, PermissionObject>>
	// Cached to make loading permissions of API Tokens more efficient
	private static Cache<Integer, HashMap<String, Permission>> userPermissionsCache = CFW.Caching.addCache("CFW User Permissions", 
			CacheBuilder.newBuilder()
				.initialCapacity(50)
				.maximumSize(500)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);

	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	private static void invalidateCache() {
		userPermissionsCache.invalidateAll();
	}
	
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
				.warn("Permission cannot be null.");
			return false;
		}
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.warn("Permission-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsPermissionInRole(permission, role)) {
			new CFWLog(logger)
				.warn("The permission '"+permission.name()+"' is already part of the role '"+role.name()+"'.");
			return false;
		}
		
		String insertPermissionSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + RolePermissionMapFields.FK_ID_PERMISSION +", "
				  + RolePermissionMapFields.FK_ID_ROLE +", "
				  + RolePermissionMapFields.IS_DELETABLE +" "
				  + ") VALUES (?,?,?);";
		
		invalidateCache();
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Role.class, "Add Permission to Role: "+role.name()+", Permission: "+permission.name());
		
		return CFWDB.preparedExecute(insertPermissionSQL, 
				permission.id(),
				role.id(),
				isDeletable
				);
		
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
				.warn("Permission-ID or role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsPermissionInRole(permissionID, roleID)) {
			new CFWLog(logger)
				.warn("The permission '"+permissionID+"' is already part of the role '"+roleID+"'.");
			return false;
		}
		
		Role role = CFW.DB.Roles.selectByID(roleID);
		Permission permission = CFW.DB.Permissions.selectByID(permissionID);
		
		return addPermissionToRole(permission, role, isDeletable);
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
				.warn("Permission and role cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.warn("Permission-ID and role-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsPermissionInRole(permission, role)) {
			new CFWLog(logger)
				.warn("The permission '"+permission.name()+"' is not part of the role '"+role.name()+"' and cannot be removed.");
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
		
		invalidateCache();
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Role.class, "Remove Permission from Role: "+role.name()+", Permission: "+permission.name());
		
		return CFWDB.preparedExecute(removePermissionFromRoleSQL, 
				permission.id(),
				role.id()
				);
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
				.warn("The permission '"+permissionID+"' is not part of the role '"+ roleID+"' and cannot be removed.");
			return false;
		}
				
		Role role = CFW.DB.Roles.selectByID(roleID);
		Permission permission = CFW.DB.Permissions.selectByID(permissionID);
		return removePermissionFromRole(permission, role);

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
			.executeCount();

	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified role.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForRole(Role role) {
		
		if( role == null) {
			new CFWLog(logger)
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
		return selectPermissionsForUser(user.id());
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForUser(int userID) {
		
		HashMap<String, Permission> userPermissions = new HashMap<>();
		try {
			userPermissions = userPermissionsCache.get(userID, new Callable<HashMap<String, Permission>>() {

				@Override
				public HashMap<String, Permission> call() throws Exception {
					ResultSet result = selectPermissionsForUserResultSet(userID);
					
					HashMap<String, Permission> permissionMap = new HashMap<String, Permission>(); 
					try {
						while(result != null && result.next()) {
							Permission permission = new Permission(result);
							permissionMap.put(permission.name(), permission);
						}
					} catch (SQLException e) {
						new CFWLog(logger)
						.severe("Error while selecting permissions for the user with id '"+userID+"'.", e);
						return null;
					}finally {
						CFWDB.close(result);
					}
					
					return permissionMap;
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading permissions from cache or database.", e);
		}
		
		return userPermissions;
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static ResultSet selectPermissionsForUserResultSet(User user) {
		
		if(user == null) {
			new CFWLog(logger)
				.severe("The user cannot be null.");
			return null;
		}
		
		return selectPermissionsForUserResultSet(user.id());
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static ResultSet selectPermissionsForUserResultSet(int userID) {
		
		return new CFWSQL(new User())
				.queryCache(CFWDBRolePermissionMap.class, "selectPermissionsForUserResultSet")
				.custom(
					"SELECT P.* "
					+"FROM CFW_PERMISSION P "
					+"JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_PERMISSION = P.PK_ID "
					+"JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_ROLE = GP.FK_ID_ROLE "
					+"WHERE UG.FK_ID_USER = ?;", 
					userID)
				.getResultSet();
		
	}
	
	
	/***************************************************************
	 * Retrieve the permission overview for all users.
	 * @param role
	 * @return ResultSet
	 ****************************************************************/
	public static ResultSet getPermissionOverview() {
		
		return new CFWSQL(new Permission())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, "sql_permissionOverviewAllUsers.sql")
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Retrieve the permission overview for the specified user.
	 ****************************************************************/
	public static JsonArray getPermissionOverview(User user) {
		
		return new CFWSQL(new Permission())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, "sql_permissionOverviewForUser.sql", user.id())
				.getAsJSONArray();
		
	}
		
	/***************************************************************
	 * Returns a list of roles without groups and if the user is part of them 
	 * as a json array.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static String getPermissionMapForRoleAsJSON(String roleID, String pageSize, String pageNumber, String filterquery, String sortby, boolean sortAscending) {
		return getPermissionMapForRoleAsJSON(roleID, Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, sortAscending);
	}
	
	/***************************************************************
	 * Returns a list of roles without groups and if the user is part of them 
	 * as a json array.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static String getPermissionMapForRoleAsJSON(
			  String roleID
			, int pageSize
			, int pageNumber
			, String filterquery
			, String sortby
			, boolean sortAscending) {	
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The roleID '"+roleID+"' is not a number.");
			return "[]";
		}
				
		
		//----------------------------------
		// Create Base Query
		String baseQueryPartial = 
				 "SELECT T.PK_ID, T.NAME, T.DESCRIPTION, M.FK_ID_ROLE AS ITEM_ID, M.IS_DELETABLE "
				 + ", COUNT(*) OVER() AS TOTAL_RECORDS "
				+ " FROM "+Permission.TABLE_NAME+" T "
				+ " LEFT JOIN "+CFWDBRolePermissionMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_PERMISSION = T.PK_ID"
				+ " AND M.FK_ID_ROLE = ?"
				;
				
		CFWSQL finalQuery = new CFWSQL(null)
			//.queryCache() cannot cache as query string is dynamic
			.custom(baseQueryPartial
					, roleID);
		
		//----------------------------------
		// Filter 
		if(!Strings.isNullOrEmpty(filterquery)) {
			finalQuery
				.where()
				.like(PermissionFields.NAME, "%"+filterquery+"%")
				.or()
				.like(PermissionFields.DESCRIPTION, "%"+filterquery+"%")
				;
		}
		
		//--------------------------------
		// Order By
		if( !Strings.isNullOrEmpty(sortby) ) {
			if(sortAscending) {
				finalQuery.orderby(sortby);
			}else {
				finalQuery.orderbyDesc(sortby);
			}

		}else {
			finalQuery.orderby(PermissionFields.NAME);
		}
		
		//--------------------------------
		// Fetch data
		return finalQuery.limit(pageSize)
			.offset(pageSize*(pageNumber-1))
			.getAsJSON();
		
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
				.severe("The userID '"+permissionID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
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

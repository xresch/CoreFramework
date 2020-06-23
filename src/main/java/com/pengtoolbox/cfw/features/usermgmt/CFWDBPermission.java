package com.pengtoolbox.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWDBDefaultOperations;
import com.pengtoolbox.cfw.features.usermgmt.Permission.PermissionFields;
import com.pengtoolbox.cfw.features.usermgmt.Role.RoleFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBPermission {

	public static Logger logger = CFWLog.getLogger(CFWDBPermission.class.getName());
	
	
	/********************************************************************************************
	 * Creates multiple permissions in the DB.
	 * @param Permissions with the values that should be inserted. ID will be set by the database.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void create(Permission... permissions) {
		
		for(Permission permission : permissions) {
			create(permission);
		}
	}
	
	/********************************************************************************************
	 * Creates a new permission in the DB if the name was not already given.
	 * All newly created permissions are by default assigned to the Superuser Role.
	 * 
	 * @param permission with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(Permission permission, boolean addToAdminRole, boolean addToUserRole) {
		
		boolean result = true; 
		if(!CFW.DB.Permissions.checkExistsByName(permission)) {
			
			result &= CFW.DB.Permissions.create(permission);
			
			permission = CFW.DB.Permissions.selectByName(permission.name());
			
			if(addToAdminRole) {
				Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
				result &= CFW.DB.RolePermissionMap.addPermissionToRole(permission, adminRole, true);
			}
			if(addToUserRole) {
				Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
				result &= CFW.DB.RolePermissionMap.addPermissionToRole(permission, userRole, true);
			}
		}
		
		return result;
	}
	/********************************************************************************************
	 * Creates a new permission in the DB.
	 * All newly created permissions are by default assigned to the Superuser Role.
	 * 
	 * @param permission with the values that should be inserted. ID will be set by the Database.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean create(Permission permission) {
		
		if(permission == null) {
			new CFWLog(logger)
				.method("create")
				.warn("The permission cannot be null");
			return false;
		}
		
		if(permission.name() == null || permission.name().isEmpty()) {
			new CFWLog(logger)
				.method("create")
				.warn("Please specify a name for the permission to create.");
			return false;
		}
		
		if(checkExistsByName(permission)) {
			new CFWLog(logger)
				.method("create")
				.warn("The permission '"+permission.name()+"' cannot be created as a permission with this name already exists.");
			return false;
		}
		
		permission
			.queryCache(CFWDBPermission.class, "create")
				.insert();
		
		//----------------------------------------
		// Add new permission to superuser
		Permission permissionFromDB = CFW.DB.Permissions.selectByName(permission.name());
		Role superuser = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_SUPERUSER);
		
		return CFW.DB.RolePermissionMap.addPermissionToRole(permissionFromDB, superuser, false);
				
	}
	
	/***************************************************************
	 * Select a permission by it's name.
	 * @param id of the permission
	 * @return Returns a permission or null if not found or in case of exception.
	 ****************************************************************/
	public static Permission selectByName(String name ) {
		
		return (Permission)new Permission()
				.queryCache(CFWDBPermission.class, "selectByName")
				.select()
				.where(PermissionFields.NAME.toString(), name)
				.getFirstObject();
		
	}
	
	/***************************************************************
	 * Select a permission by it's ID.
	 * @param id of the permission
	 * @return Returns a permission or null if not found or in case of exception.
	 ****************************************************************/
	public static Permission selectByID(int id ) {
		
		return (Permission)new Permission()
				.queryCache(CFWDBPermission.class, "selectByID")
				.select()
				.where(PermissionFields.PK_ID.toString(), id)
				.getFirstObject();
		
	}
	
	/***************************************************************
	 * Return a list of all user permissions
	 * 
	 * @return Returns a resultSet with all permissions or null.
	 ****************************************************************/
	public static ResultSet getUserPermissionList() {
		
		return new Permission()
				.queryCache(CFWDBPermission.class, "getUserPermissionList")
				.select()
				.where(PermissionFields.CATEGORY.toString(), "user")
				.orderby(PermissionFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all users as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserPermissionListAsJSON() {
		return new Permission()
				.queryCache(CFWDBPermission.class, "getUserPermissionListAsJSON")
				.select()
				.where(PermissionFields.CATEGORY.toString(), "user")
				.orderby(PermissionFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForUser(User user) {
		return CFW.DB.RolePermissionMap.selectPermissionsForUser(user);
	}
	
	/***************************************************************
	 * Updates the object selecting by ID.
	 * @param permission
	 * @return true or false
	 ****************************************************************/
	public static boolean update(Permission permission) {
		
		if(permission == null) {
			new CFWLog(logger)
				.method("update")
				.warn("The permission that should be updated cannot be null");
			return false;
		}
		
		if(permission.name() == null || permission.name().isEmpty()) {
			new CFWLog(logger)
				.method("update")
				.warn("Please specify a name for the permission.");
			return false;
		}
				
		return permission
				.queryCache(CFWDBPermission.class, "update")
				.update();
		
	}
	
	/****************************************************************
	 * Deletes the permission by id.
	 * @param id of the permission
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		Permission permission = selectByID(id);
		if(permission != null) {
			new CFWLog(logger)
			.method("deleteByID")
			.severe("The permission '"+permission.name()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		return new Permission()
				.queryCache(CFWDBPermission.class, "deleteByID")
				.delete()
				.where(PermissionFields.PK_ID.toString(), id)
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

		return new Permission()
				.queryCache(CFWDBPermission.class, "deleteMultipleByID")
				.delete()
				.whereIn(PermissionFields.PK_ID.toString(), resultIDs)
				.executeDelete();
			
	}
	
	/****************************************************************
	 * Deletes the permission by id.
	 * @param id of the permission
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByName(String name) {
		
		Permission permission = selectByName(name);
		if(permission != null) {
			new CFWLog(logger)
			.method("deleteByName")
			.severe("The permission '"+permission.name()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		return new Permission()
				.queryCache(CFWDBPermission.class, "deleteByName")
				.delete()
				.where(PermissionFields.NAME.toString(), name)
				.executeDelete();
			
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	private static Class<Permission> cfwObjectClass = Permission.class;
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, RoleFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(Permission object) {
		if(object != null) {
			return checkExistsByName(object.name());
		}
		return false;
	}
	
//	/****************************************************************
//	 * Check if the permission exists by name.
//	 * 
//	 * @param permission to check
//	 * @return true if exists, false otherwise or in case of exception.
//	 ****************************************************************/
//	public static boolean checkExistsByName(Permission permission) {
//		if(permission != null) {
//			return checkExistsByName(permission.name());
//		}
//		return false;
//	}
//	
//	/****************************************************************
//	 * Check if the permission exists by name.
//	 * 
//	 * @param permission to check
//	 * @return true if exists, false otherwise or in case of exception.
//	 ****************************************************************/
//	public static boolean checkExistsByName(String permissionName) {
//		
//		int count = new Permission()
//				.queryCache(CFWDBPermission.class, "checkPermissionExists")
//				.selectCount()
//				.where(PermissionFields.NAME.toString(), permissionName)
//				.getCount();
//		
//		return (count > 0);
//	}
	
}

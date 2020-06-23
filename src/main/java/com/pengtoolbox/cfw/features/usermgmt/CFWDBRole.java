package com.pengtoolbox.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDBDefaultOperations;
import com.pengtoolbox.cfw.db.PrecheckHandler;
import com.pengtoolbox.cfw.features.usermgmt.Role.RoleFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBRole {

	
	public static String CFW_ROLE_SUPERUSER = "Superuser";
	public static String CFW_ROLE_ADMIN = "Administrator";
	public static String CFW_ROLE_USER = "User";
	
	private static Class<Role> cfwObjectClass = Role.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBRole.class.getName());
		
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Role role = (Role)object;
			
			if(role.name() == null || role.name().isEmpty()) {
				new CFWLog(logger)
					.method("doCheck")
					.warn("Please specify a name for the role.", new Throwable());
				return false;
			}
			
			if(checkExistsByName(role)) {
				new CFWLog(logger)
					.method("doCheck")
					.warn("The role with the name '"+role.name()+"' already exists.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	private static PrecheckHandler prechecksUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Role role = (Role)object;
			
			if(role.name() == null || role.name().isEmpty()) {
				new CFWLog(logger)
					.method("doCheck")
					.warn("The name of the role cannot be null.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			Role role = (Role)object;
			
			if(role != null && role.isDeletable() == false) {
				new CFWLog(logger)
				.method("doCheck")
				.severe("The role '"+role.name()+"' cannot be deleted as it is marked as not deletable.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(Role... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate, items); }
	public static boolean 	create(Role item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(Role... items) 	{ return CFWDBDefaultOperations.update(prechecksUpdate, items); }
	public static boolean 	update(Role item) 		{ return CFWDBDefaultOperations.update(prechecksUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, RoleFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
	
	public static boolean 	deleteByName(String name) 		{ 
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, RoleFields.NAME.toString(), name); 
	}
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static Role selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, RoleFields.PK_ID.toString(), id);
	}
	
	public static Role selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, RoleFields.NAME.toString(), name);
	}
	
	/***************************************************************
	 * Select a role by it's ID and return it as JSON string.
	 * @param id of the role
	 * @return Returns a role or null if not found or in case of exception.
	 ****************************************************************/
	public static String getUserRolesAsJSON(String id) {
		
		return new Role()
				.queryCache(CFWDBRole.class, "getUserRolesAsJSON")
				.select()
				.where(RoleFields.PK_ID.toString(), Integer.parseInt(id))
				.and(RoleFields.CATEGORY.toString(), "user")
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user roles
	 * 
	 * @return Returns a resultSet with all roles or null.
	 ****************************************************************/
	public static ResultSet getUserRoleList() {
		
		return new Role()
				.queryCache(CFWDBRole.class, "getUserRoleList")
				.select()
				.where(RoleFields.CATEGORY.toString(), "user")
				.orderby(RoleFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all user roles as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserRoleListAsJSON() {
		return new Role()
				.queryCache(CFWDBRole.class, "getUserRoleListAsJSON")
				.select()
				.where(RoleFields.CATEGORY.toString(), "user")
				.orderby(RoleFields.NAME.toString())
				.getAsJSON();
	}
	
	
	/***************************************************************
	 * Retrieve the permissions for the specified role.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForRole(Role role) {
		return CFW.DB.RolePermissionMap.selectPermissionsForRole(role);
	}
			
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, RoleFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(Role item) {
		if(item != null) {
			return checkExistsByName(item.name());
		}
		return false;
	}
		
}

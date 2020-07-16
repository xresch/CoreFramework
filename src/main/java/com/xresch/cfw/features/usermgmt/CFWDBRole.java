package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.logging.CFWLog;

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
	
	/****************************************************************
	 * Returns a AutocompleteResult with roles.
	 * 
	 * @param searchValue
	 * @param maxResults
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static AutocompleteResult autocompleteRole(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		String likeString = "%"+searchValue.toLowerCase()+"%";
		
		return new Role()
			.queryCache(CFWDBRole.class, "autocompleteRole(String, int)")
			.select(RoleFields.PK_ID,
					RoleFields.NAME,
					RoleFields.DESCRIPTION)
			.whereLike("LOWER("+RoleFields.NAME+")", likeString)
			.limit(maxResults)
			.getAsAutocompleteResult(RoleFields.PK_ID, RoleFields.NAME, RoleFields.DESCRIPTION);

	}
	
	
	/***************************************************************
	 * Return a list of all user that have the specified role as a
	 * JSON string.
	 * 
	 ****************************************************************/
	public static String getUsersForRoleAsJSON(String roleID) {

		String JSON = new Role()
				.queryCache(CFWDBRole.class, "getUsersForRoleAsJSON")
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, 
						"sql_users_for_role.sql", 
						Integer.parseInt(roleID))
				.getAsJSON();
		
		return JSON;
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

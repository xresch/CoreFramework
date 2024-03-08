package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.util.ArrayList;
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
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBRole {

	
	public static final String CFW_ROLE_SUPERUSER = "Superuser";
	public static final String CFW_ROLE_ADMIN = "Administrator";
	public static final String CFW_ROLE_USER = "User";
	
	private static final String SQL_SUBQUERY_GROUPOWNER = "SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_GROUPOWNER";
	
	private static Class<Role> cfwObjectClass = Role.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBRole.class.getName());
		
	private static final String[] auditLogFieldnames = new String[] { RoleFields.PK_ID.toString(), RoleFields.NAME.toString()};
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Role role = (Role)object;
			
			if(role.name() == null || role.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a name for the role.", new Throwable());
				return false;
			}
			
			if(checkExistsByName(role)) {
				new CFWLog(logger)
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
				.severe("The role '"+role.name()+"' cannot be deleted as it is marked as not deletable.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(Role... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate, auditLogFieldnames,items); }
	public static boolean 	create(Role item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, auditLogFieldnames, item);}
	public static Integer 	createGetPrimaryKey(Role item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, auditLogFieldnames, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(Role item) 		{ 
		//Do not use CFWDBDefaultOperations as this cannot be cached
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, item, auditLogFieldnames);
		return item.update();
	}
		
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 	{ 
		Role role = selectByID(id);
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, "Role", "Role: "+role.name());
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, RoleFields.PK_ID.toString(), id); 
	}
	
	public static boolean 	deleteMultipleByID(String IDs) 	{ 
		
		if(IDs == null ^ !IDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.severe("The role ID's '"+IDs+"' are not a comma separated list of strings.");
			return false;
		}
		
		boolean success = true;
		for(String id : IDs.split(",")) {
			success &= deleteByID(Integer.parseInt(id));
		}

		return success;
	}
	
	public static boolean 	deleteByName(String name) 		{ 
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, "Role", "Role: "+name);
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
				.and(RoleFields.CATEGORY.toString(), FeatureUserManagement.CATEGORY_USER)
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
				.where(RoleFields.CATEGORY.toString(), FeatureUserManagement.CATEGORY_USER)
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
	public static AutocompleteResult autocompleteGroup(String searchValue, int maxResults) {
		
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
			.and(RoleFields.IS_GROUP, true)
			.limit(maxResults)
			.getAsAutocompleteResult(RoleFields.PK_ID, RoleFields.NAME, RoleFields.DESCRIPTION);

	}
	
	/***************************************************************
	 * Return a list of all user that have the specified role.
	 * 
	 ****************************************************************/
	public static ArrayList<User> getUsersForRole(String roleID) {

		return new CFWSQL(new Role())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, 
						"sql_users_for_role.sql", 
						Integer.parseInt(roleID))
				.getAsObjectListConvert(User.class);
		
	}
	
	/***************************************************************
	 * Return a list of all user that have the specified role.
	 * 
	 ****************************************************************/
	public static ArrayList<User> getAdminsAndSuperusers() {

		return new CFWSQL(new Role())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, 
						"sql_getAdminsAndSuperusers.sql", 
						CFW_ROLE_ADMIN,
						CFW_ROLE_SUPERUSER)
				.getAsObjectListConvert(User.class);
		
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
	 * Return a list of user roles as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserRoleListAsJSON() {
		return new CFWSQL(new Role())
				.select()
				.where(RoleFields.CATEGORY.toString(), FeatureUserManagement.CATEGORY_USER)
				.custom(" AND (")
					.is(RoleFields.IS_GROUP, false)
					.or().isNull(RoleFields.IS_GROUP)
				.custom(")")
				.orderby(RoleFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a list of groups as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getGroupListAsJSON() {
		return new CFWSQL(new Role())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_GROUPOWNER)
				.select()
				.where(RoleFields.CATEGORY.toString(), FeatureUserManagement.CATEGORY_USER)
				.and(RoleFields.IS_GROUP, true)
				.orderby(RoleFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isGroupOfCurrentUser(String groupID) {
		return isGroupOfCurrentUser(Integer.parseInt(groupID));
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isGroupOfCurrentUser(int groupID) {
		
		int count = new CFWSQL(new Role())
			.selectCount()
			.where(RoleFields.PK_ID, groupID)
			.and(RoleFields.PK_ID.IS_GROUP, true)
			.and(RoleFields.FK_ID_GROUPOWNER, CFW.Context.Request.getUser().id())
			.executeCount();
		
		return count > 0;
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

package com.xresch.cfw.features.usermgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBUser {

	private static final Logger logger = CFWLog.getLogger(CFWDBUser.class.getName());
	
	/* Query to select the Username for the ID specified by column FK_ID_USER*/
	public static final String USERNAME_SUBQUERY = "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER";
	
	private static final String[] auditLogFieldnames = new String[] { 
			UserFields.PK_ID.toString()
		  , UserFields.USERNAME.toString()
		};
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			User user = (User)object;
			
			if( user == null) {
				new CFWLog(logger)
					.severe("The user cannot be null");
				return false;
			}
			
			if( user.username() == null || user.username().isEmpty() ) {
				new CFWLog(logger)
					.severe("Please provide at least one character for the username.");
				return false;
			}
			
			if( checkUsernameExists(user.username())) {
				new CFWLog(logger)
					.warn("The user '"+user.username()+"' cannot be created as a user with this name already exists.");
				return false;
			}
			
			if( user.email() != null
			&& !user.email().isEmpty()
			&& checkEmailExists(user.email())) {
				
				new CFWLog(logger)
					.warn("The user '"+user.username()+"' cannot be created as the email '"+user.email()+"' is already used by another account.");
				return false;
			}
			
			return true;
		}
	};
	
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean 	create(User item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, auditLogFieldnames, item);}
	public static Integer 	createGetPrimaryKey(User item) {  return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, auditLogFieldnames, item);}
	public static Integer 	createGetPrimaryKeyWithout(User item, Object... excludeFields) { return CFWDBDefaultOperations.createGetPrimaryKeyWithout(prechecksCreateUpdate, auditLogFieldnames, item, excludeFields);}
		
		
	/********************************************************************************************
	 * Creates multiple users in the DB.
	 * @param Users with the values that should be inserted. ID will be set by the Database.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void create(User... users) {
		
		for(User user : users) {
			create(user);
		}
	}
	

	/***************************************************************
	 * Select a user by it's username or email address.
	 * This method is useful for login forms.
	 * 
	 * @param username or eMail address
	 * @return Returns a user or null if not found or in case of exception.
	 ****************************************************************/
	public static User selectByUsernameOrMail(String usernameOrMail) {
		
		if( usernameOrMail == null) {
			new CFWLog(logger)
				.severe("The user or eMail cannot be null.");
			return null;
		}
		
		return (User)new User()
				.queryCache(CFWDBUser.class, "selectByUsernameOrMail")
				.select()
				.where(UserFields.USERNAME.toString(), usernameOrMail, false)
				.or(UserFields.EMAIL.toString(), usernameOrMail, false)
				.getFirstAsObject();
			
	}
	
	/***************************************************************
	 * Select a user by it's ID.
	 * 
	 * @param id of the User
	 * @return Returns a user or null if not found or in case of exception.
	 ****************************************************************/
	public static User selectByID(String id) {
		return selectByID(Integer.parseInt(id));
	}
	
	/***************************************************************
	 * Select a user by it's ID.
	 * 
	 * @param id of the User
	 * @return Returns a user or null if not found or in case of exception.
	 ****************************************************************/
	public static User selectByID(int id) {
			
		return (User)new CFWSQL(new User())
				.queryCache()
				.select()
				.where(UserFields.PK_ID.toString(), id)
				.getFirstAsObject();
		
	}
	
	/***************************************************************
	 * Select the username for the given ID.
	 * 
	 * @param id of the User
	 * @return username or null if not found or in case of exception.
	 ****************************************************************/
	public static String selectUsernameByID(int id) {
		
		
		User user = (User)new CFWSQL(new User())
				.queryCache()
				.select(UserFields.USERNAME)
				.where(UserFields.PK_ID.toString(), id)
				.getFirstAsObject()
				;
		
		if(user == null) {
			return null;
		}
		
		return user.username();
	}
	
	/***************************************************************
	 * Select a user by it's ID and return it as a JSON string.
	 * 
	 * @param id of the User
	 * @return Returns a user or null if not found or in case of exception.
	 ****************************************************************/
	public static String getUserAsJSON(String userID) {
		
		return new User()
				.queryCache(CFWDBUser.class, "getUserAsJSON")
				.selectWithout(UserFields.PASSWORD_HASH.toString(), 
						       UserFields.PASSWORD_SALT.toString())
				.where(UserFields.PK_ID.toString(), userID)
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all users.
	 * 
	 * @return Returns a resultSet with all roles or null.
	 ****************************************************************/
	public static ArrayList<CFWObject> getUserList() {
		
		return new CFWSQL(new User())
				.queryCache()
				.select()
				.orderby(UserFields.USERNAME)
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return a list of all users, but only ID, username firstname 
	 * and lastname.
	 * 
	 * @return Returns a resultSet with all roles or null.
	 ****************************************************************/
	public static ArrayList<CFWObject> getUserListMinimal() {
		
		return new CFWSQL(new User())
				.queryCache()
				.select(UserFields.PK_ID, UserFields.USERNAME, UserFields.FIRSTNAME, UserFields.LASTNAME)
				.orderby(UserFields.USERNAME)
				.getAsObjectList();
		
	}
	
	
	/***************************************************************
	 * Takes a Map<UserID, something> and fetches the users from
	 * the DB and returns a HashMap<Integer, User>.
	 * @param removeInactive TODO
	 * 
	 * @return Returns a list, can be empty, never null
	 ****************************************************************/
	public static HashMap<Integer, User> convertToUserList(Map<String, String> usersToAlert, boolean removeInactive) {
		
		HashMap<Integer, User> uniqueUsers = new HashMap<>();
		if(usersToAlert != null) {
			for(String userID : usersToAlert.keySet()) {
				User user = CFW.DB.Users.selectByID(userID);
				
				if(removeInactive && !user.isStatusActive()) {
					continue;
				}
				
				if(user != null) {
					uniqueUsers.put(user.id(), user);
				}
			}
		}
		
		return uniqueUsers;
	}
	
	/***************************************************************
	 * Return a list of all users.
	 * Don't forget to close the db connection using CFWDB.close().
	 * 
	 * @return Returns a resultSet with all roles or null.
	 ****************************************************************/
	public static String getUserListAsJSON() {
		
		return new User()
				.queryCache(CFWDBUser.class, "getUserList")
				.selectWithout(UserFields.PASSWORD_HASH.toString(),
								UserFields.PASSWORD_SALT.toString()
								)
				.orderby(UserFields.USERNAME.toString())
				.getAsJSON();
		
	}
	
	
//	/***************************************************************
//	 * Return a list of all users as json string.
//	 * 
//	 * @return Returns a result set with all users or null.
//	 ****************************************************************/
//	public static String getUserListAsJSON() {
//		String selectAllUsers = 
//				"SELECT "
//				  + UserFields.PK_ID +", "
//				  + UserFields.USERNAME +", "
//				  + UserFields.EMAIL +", "
//				  + UserFields.FIRSTNAME +", "
//				  + UserFields.LASTNAME +", "
//				  + UserFields.DATE_CREATED +", "
//				  + UserFields.STATUS +", "
//				  + UserFields.IS_DELETABLE +", "
//				  + UserFields.IS_RENAMABLE + ", "
//				  + UserFields.IS_FOREIGN 
//				+" FROM "+User.TABLE_NAME
//				+" ORDER BY LOWER("+UserFields.USERNAME +") ASC";
//		
//		ResultSet result = CFWDB.preparedExecuteQuery(selectAllUsers);
//		String json = CFWDB.resultSetToJSON(result);
//		CFWDB.close(result);	
//		return json;
//	}
	
	/***************************************************************
	 * Retrieve the roles for the specified user.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<Integer, Role> selectRolesForUser(int userID) {
		
		return CFW.DB.UserRoleMap.selectAllRolesForUser(userID);
	
	}
	/***************************************************************
	 * Retrieve the roles for the specified user.
	 * @param role
	 * @return Hashmap with roles(key=role name, value=role object), or null on exception
	 ****************************************************************/
	public static HashMap<Integer, Role> selectRolesForUser(User user) {
		
		return CFW.DB.UserRoleMap.selectAllRolesForUser(user);
	
	}
	
	/***************************************************************
	 * Retrieve the permissions for the specified user.
	 * @param role
	 * @return Hashmap with permissions(key=role name), or null on exception
	 ****************************************************************/
	public static HashMap<String, Permission> selectPermissionsForUser(int userID) {
		return CFW.DB.RolePermissionMap.selectPermissionsForUser(userID);
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
	 * @param role
	 * @return true or false
	 ****************************************************************/
	public static boolean update(User user) {
		
		if(user == null) {
			new CFWLog(logger)
			.severe("The user cannot be null.");
			return false;
		}
		
		if(!user.isSaveable()) {
			new CFWLog(logger)
			.severe("The user cannot be updated as it was marked as not saveable.");
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, "User", "Username: "+user.username());

		boolean resultUpdate = 
			   user.queryCache(CFWDBUser.class, "update")
					.updateWithout(UserFields.USERNAME.toString());

		
		boolean resultRename = true;
		
		if(user.hasUsernameChanged()) {
			
			if(!user.isRenamable()) {
				new CFWLog(logger)
				.severe("The user '"+user.username()+"' cannot be renamed as it is marked as not renamable.");
				return false;
			}
			
			resultRename = 
					   user.queryCache(CFWDBUser.class, "updateNameOnly")
							.update(UserFields.USERNAME);
		}
		
		return resultUpdate && resultRename;
		
	}
	
	/****************************************************************
	 * Deletes the User by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
		
		User user = selectByID(id);
		
		if(user == null ) {
			new CFWLog(logger)
			.severe("The user with ID '"+id+"' cannot be deleted as it does not exist.");
			return false;
		}
		
		if(user.isDeletable() == false) {
			new CFWLog(logger)
			.severe("The user '"+user.username()+"' cannot be deleted as it is marked as not deletable.");
			return false;
		}
		
		new CFWLog(logger).audit(CFWAuditLogAction.DELETE, "User", "Username: "+user.username());

		return new User()
				.queryCache(CFWDBUser.class, "deleteByID")
				.delete()
				.where(UserFields.PK_ID.toString(), id)
				.and(UserFields.IS_DELETABLE.toString(), true)
				.executeDelete();
		
			
	}
	
	/****************************************************************
	 * Deletes multiple users by id.
	 * @param ids of the users separated by comma
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteMultipleByID(String IDs) {
		
		//----------------------------------
		// Check input format
		if(IDs == null ^ !IDs.matches("(\\d,?)+")) {
			new CFWLog(logger)
			.severe("The userID's '"+IDs+"' are not a comma separated list of strings.");
			return false;
		}
		
		boolean success = true;
		for(String id : IDs.split(",")) {
			success &= deleteByID(Integer.parseInt(id));
		}

		return success;
			
	}
	
	
	
	
	/****************************************************************
	 * Check if the user exists by it's username.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkUsernameExists(User user) {
		if(user != null) {
			return checkUsernameExists(user.username());
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the user exists by it's username.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkUsernameExists(String username) {
		
		int count = new User()
				.queryCache(CFWDBUser.class, "checkUsernameExists")
				.selectCount()
				.where(UserFields.USERNAME.toString(), username, false)
				.executeCount();
		
		return (count > 0);
		
	}
	
	/****************************************************************
	 * Check if the email of the user is already in use.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkEmailExists(User user) {
		if(user != null) {
			return checkEmailExists(user.email());
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the email of the user is already in use.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkEmailExists(String email) {
		
		int count = new User()
				.queryCache(CFWDBUser.class, "checkEmailExists")
				.selectCount()
				.where(UserFields.EMAIL.toString(), email, false)
				.executeCount();
		
		return (count > 0);

	}
	
	/****************************************************************
	 * Returns a AutocompleteResult with users.
	 * 
	 * @param searchValue
	 * @param maxResults
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static AutocompleteResult autocompleteUser(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		String likeString = "%"+searchValue.toLowerCase()+"%";
		ArrayList<CFWObject> userList = new User()
			.queryCache(CFWDBUser.class, "autocompleteUser")
			.select(UserFields.PK_ID,
					UserFields.USERNAME,
					UserFields.FIRSTNAME,
					UserFields.LASTNAME,
					UserFields.EMAIL)
			.where().like(UserFields.USERNAME, likeString, false)
			.or().like(UserFields.FIRSTNAME, likeString, false)
			.or().like(UserFields.LASTNAME, likeString, false)
			.and().not().is(UserFields.PK_ID, CFW.Context.Request.getUser().id())
			.limit(maxResults)
			.getAsObjectList();
		
		AutocompleteList autocompleteList = new AutocompleteList();
		for(CFWObject userObject : userList) {
			User user = (User) userObject;
						
			autocompleteList.addItem(user.id(), user.createUserLabel(), user.email());
			
		}
			
		return new AutocompleteResult(autocompleteList);
	}
	
}

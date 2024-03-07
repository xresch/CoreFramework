package com.xresch.cfw.features.usermgmt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.usermgmt.RoleEditorsMap.RoleEditorsMapFields;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBRoleEditorsMap {

	private static final String TABLE_NAME = new RoleEditorsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBRoleEditorsMap.class.getName());
	
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @return return true if role was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToRole(User user, Role role) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or Role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToRole(user, role)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is already shared with '"+user.username()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + RoleEditorsMapFields.FK_ID_USER +", "
				  + RoleEditorsMapFields.FK_ID_ROLE
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, RoleEditorsMap.class, "Add User to Role: "+role.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				role.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, RoleEditorsMap.class, "Add User to Role: "+role.name()+", User: "+user.username());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param userID
	 * @param roleID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToRole(int userID, int roleID) {
		
		
		if(userID < 0 || roleID < 0) {
			new CFWLog(logger)
				.warn("User-ID or role-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToRole(userID, roleID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the role '"+roleID+"'.");
			return false;
		}
		
		Role role = CFW.DB.Roles.selectByID(roleID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToRole(user, role);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @return return true if role was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserRoleAssignments(Role role, LinkedHashMap<String,String> usersKeyLabel) {
				
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new RoleEditorsMap())
						.delete()
						.where(RoleEditorsMapFields.FK_ID_ROLE, role.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, RoleEditorsMap.class, "Update Shared User Assignments: "+role.name());
			}
			
			if(usersKeyLabel != null) {
				for(String userID : usersKeyLabel.keySet()) {
					isSuccess &= assignUserToRole(Integer.parseInt(userID), role.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified role.
	 * @param user
	 * @param role
	 * @return return true if role was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromRole(User user, Role role) {
		
		if(user == null || role == null ) {
			new CFWLog(logger)
				.warn("User and Role cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || role.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and Role-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToRole(user, role)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to role '"+role.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromRoleSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + RoleEditorsMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + RoleEditorsMapFields.FK_ID_ROLE +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, RoleEditorsMap.class, "Remove User from Role: "+role.name()+", User: "+user.username());
		
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
		
		if(!checkIsUserAssignedToRole(userID, roleID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the role '"+ roleID+"' and cannot be removed.");
			return false;
		}
				
		Role role = CFW.DB.Roles.selectByID(roleID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromRole(user, role);

	}
	
	/****************************************************************
	 * Check if the user is in the given role.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToRole(User user, Role role) {
		
		if(user != null && role != null) {
			return checkIsUserAssignedToRole(user.id(), role.id());
		}else {
			new CFWLog(logger)
				.severe("The user and role cannot be null. User: '"+user+"', Role: '"+role+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToRole(int userid, int roleid) {
		
		return 0 != new CFWSQL(new RoleEditorsMap())
			.queryCache()
			.selectCount()
			.where(RoleEditorsMapFields.FK_ID_USER.toString(), userid)
			.and(RoleEditorsMapFields.FK_ID_ROLE.toString(), roleid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the roles for a user as key/labels.
//	 * Useful for autocomplete.
//	 * @param role
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForRoleAsKeyLabel(Integer roleID) {
		
		if(roleID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.USERNAME, U.FIRSTNAME, U.LASTNAME "  
				+ " FROM "+User.TABLE_NAME+" U " 
				+ " LEFT JOIN "+RoleEditorsMap.TABLE_NAME+" M ON M.FK_ID_USER = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_ROLE = ? " 
				+ " ORDER BY LOWER(U.USERNAME) "
				;
		
		ArrayList<User> userList =  new CFWSQL(new User())
				.queryCache()
				.custom(query
						, roleID)
				.getAsObjectListConvert(User.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(User user : userList) {						
			result.put(user.id()+"", user.createUserLabel());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the user from the role if it is assigned to the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToRole(String userID, String roleID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToRole(Integer.parseInt(userID), Integer.parseInt(roleID));
		
	}
	
	/***************************************************************
	 * Remove the user from the role if it is assigned to the role, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToRole(int userID, int roleID) {
		
		if(checkIsUserAssignedToRole(userID, roleID)) {
			return removeUserFromRole(userID, roleID);
		}else {
			return assignUserToRole(userID, roleID);
		}

	}
		
}

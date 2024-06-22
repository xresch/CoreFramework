package com.xresch.cfw.features.credentials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.credentials.CFWCredentials.CFWCredentialsFields;
import com.xresch.cfw.features.credentials.CFWCredentialsSharedUserMap.CFWCredentialsSharedUserMapFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBCredentialsSharedUserMap {

	private static final String TABLE_NAME = new CFWCredentialsSharedUserMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBCredentialsSharedUserMap.class.getName());
	
	/********************************************************************************************
	 * Used to migrated from old field to new table
	 * 
	 ********************************************************************************************/
	public static boolean migrateOldStructure(CFWCredentials credentials) {
		
		if(credentials == null || credentials.id() == null) {
			return true;
		}
		
		boolean isSuccess = true;
		int credentialsID = credentials.id();
		
		//-------------------------------------
		// Get Old data
		ResultSet result = new CFWSQL(new CFWCredentials())
			.select(CFWCredentials.FIELDNAME_SHARE_WITH_USERS)
			.where(CFWCredentialsFields.PK_ID, credentialsID)
			.getResultSet();
			;
		
		String jsonString = null; 
		
		try {
			if(result.next()) {
				jsonString = result.getString(CFWCredentials.FIELDNAME_SHARE_WITH_USERS);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Strings.isNullOrEmpty(jsonString) ) { return true; }
		
		LinkedHashMap<String, String> selectedValues = CFW.JSON.fromJsonLinkedHashMap(jsonString);
		
		//-------------------------------------
		// Insert into Table
		for (String userIDString : selectedValues.keySet() ) {
			int userID = Integer.parseInt(userIDString);
			
			// handle non-existing users
			if( CFW.DB.Users.selectByID(userID) == null ){
				continue;
			}
			
			// add user to credentials
			if(! checkIsUserAssignedToCredentials(userID, credentialsID) ) {
				isSuccess &= assignUserToCredentials(userID, credentialsID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the user to the specified credentials.
	 * @param user
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToCredentials(User user, CFWCredentials credentials) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(credentials == null) {
			new CFWLog(logger)
				.warn("Org Credentials cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || credentials.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or Credentials-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToCredentials(user, credentials)) {
			new CFWLog(logger)
				.warn("The credentials '"+credentials.name()+"' is already shared with '"+user.username()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWCredentialsSharedUserMapFields.FK_ID_USER +", "
				  + CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedUserMap.class, "Add User to Credentials: "+credentials.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				credentials.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedUserMap.class, "Add User to Credentials: "+credentials.name()+", User: "+user.username());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified credentials.
	 * @param userID
	 * @param credentialsID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToCredentials(int userID, int credentialsID) {
		
		
		if(userID < 0 || credentialsID < 0) {
			new CFWLog(logger)
				.warn("User-ID or credentials-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToCredentials(userID, credentialsID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the credentials '"+credentialsID+"'.");
			return false;
		}
		
		CFWCredentials credentials = CFW.DB.Credentials.selectByID(credentialsID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToCredentials(user, credentials);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified credentials.
	 * @param user
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserCredentialsAssignments(CFWCredentials credentials, LinkedHashMap<String,String> usersKeyLabel) {
		
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			//----------------------------------------
			// Clean all and Add all New
				
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWCredentialsSharedUserMap())
						.delete()
						.where(CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS, credentials.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWCredentialsSharedUserMap.class, "Update Shared User Assignments: "+credentials.name());
			}
			
			if(usersKeyLabel != null) {
				for(String userID : usersKeyLabel.keySet()) {
					isSuccess &= assignUserToCredentials(Integer.parseInt(userID), credentials.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified credentials.
	 * @param user
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromCredentials(User user, CFWCredentials credentials) {
		
		if(user == null || credentials == null ) {
			new CFWLog(logger)
				.warn("User and Credentials cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || credentials.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and Credentials-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToCredentials(user, credentials)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to credentials '"+credentials.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromCredentialsSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWCredentialsSharedUserMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedUserMap.class, "Remove User from Credentials: "+credentials.name()+", User: "+user.username());
		
		return CFWDB.preparedExecute(removeUserFromCredentialsSQL, 
				user.id(),
				credentials.id()
				);
	}
	

	/********************************************************************************************
	 * Remove a user from the credentials.
	 * @param user
	 * @param credentials
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromCredentials(int userID, int credentialsID) {
		
		if(!checkIsUserAssignedToCredentials(userID, credentialsID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the credentials '"+ credentialsID+"' and cannot be removed.");
			return false;
		}
				
		CFWCredentials credentials = CFW.DB.Credentials.selectByID(credentialsID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromCredentials(user, credentials);

	}
	
	/****************************************************************
	 * Check if the user is in the given credentials.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToCredentials(User user, CFWCredentials credentials) {
		
		if(user != null && credentials != null) {
			return checkIsUserAssignedToCredentials(user.id(), credentials.id());
		}else {
			new CFWLog(logger)
				.severe("The user and credentials cannot be null. User: '"+user+"', Credentials: '"+credentials+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToCredentials(int userid, int credentialsid) {
		
		return 0 != new CFWSQL(new CFWCredentialsSharedUserMap())
			.queryCache()
			.selectCount()
			.where(CFWCredentialsSharedUserMapFields.FK_ID_USER.toString(), userid)
			.and(CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS.toString(), credentialsid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the credentials for a user as key/labels.
//	 * Useful for autocomplete.
//	 * @param credentials
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForCredentialsAsKeyLabel(Integer credentialsID) {
		
		if(credentialsID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.USERNAME, U.FIRSTNAME, U.LASTNAME "  
				+ " FROM "+User.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWCredentialsSharedUserMap.TABLE_NAME+" M ON M.FK_ID_USER = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_CREDENTIALS = ? " 
				+ " ORDER BY LOWER(U.USERNAME) "
				;
		
		ArrayList<User> userList =  new CFWSQL(new User())
				.queryCache()
				.custom(query
						, credentialsID)
				.getAsObjectListConvert(User.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(User user : userList) {						
			result.put(user.id()+"", user.createUserLabel());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the user from the credentials if it is assigned to the credentials, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToCredentials(String userID, String credentialsID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(credentialsID == null ^ !credentialsID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The credentialsID '"+credentialsID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToCredentials(Integer.parseInt(userID), Integer.parseInt(credentialsID));
		
	}
	
	/***************************************************************
	 * Remove the user from the credentials if it is assigned to the credentials, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToCredentials(int userID, int credentialsID) {
		
		if(checkIsUserAssignedToCredentials(userID, credentialsID)) {
			return removeUserFromCredentials(userID, credentialsID);
		}else {
			return assignUserToCredentials(userID, credentialsID);
		}

	}
		
}

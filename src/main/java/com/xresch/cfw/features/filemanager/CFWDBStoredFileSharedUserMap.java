package com.xresch.cfw.features.filemanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.filemanager.CFWStoredFileSharedUserMap.CFWStoredFileSharedUserMapFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBStoredFileSharedUserMap {

	private static final String TABLE_NAME = new CFWStoredFileSharedUserMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredFileSharedUserMap.class.getName());
	
	/********************************************************************************************
	 * Used to migrated from old field to new table
	 * 
	 ********************************************************************************************/
	public static boolean migrateOldStructure(CFWStoredFile storedfile) {
		
		if(storedfile == null || storedfile.id() == null) {
			return true;
		}
		
		boolean isSuccess = true;
		int storedfileID = storedfile.id();
		
		//-------------------------------------
		// Get Old data
		ResultSet result = new CFWSQL(new CFWStoredFile())
			.select(CFWStoredFile.FIELDNAME_SHARE_WITH_USERS)
			.where(CFWStoredFileFields.PK_ID, storedfileID)
			.getResultSet();
			;
		
		String jsonString = null; 
		
		try {
			if(result.next()) {
				jsonString = result.getString(CFWStoredFile.FIELDNAME_SHARE_WITH_USERS);
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
			
			// add user to storedfile
			if(! checkIsUserAssignedToStoredFile(userID, storedfileID) ) {
				isSuccess &= assignUserToStoredFile(userID, storedfileID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the user to the specified storedfile.
	 * @param user
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToStoredFile(User user, CFWStoredFile storedfile) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(storedfile == null) {
			new CFWLog(logger)
				.warn("Org StoredFile cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || storedfile.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or StoredFile-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToStoredFile(user, storedfile)) {
			new CFWLog(logger)
				.warn("The Stored File '"+storedfile.name()+"' is already shared with '"+user.username()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWStoredFileSharedUserMapFields.FK_ID_USER +", "
				  + CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileSharedUserMap.class, "Add User to StoredFile: "+storedfile.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				storedfile.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileSharedUserMap.class, "Add User to StoredFile: "+storedfile.name()+", User: "+user.username());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified storedfile.
	 * @param userID
	 * @param storedfileID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToStoredFile(int userID, int storedfileID) {
		
		
		if(userID < 0 || storedfileID < 0) {
			new CFWLog(logger)
				.warn("User-ID or storedfile-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToStoredFile(userID, storedfileID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the Stored File '"+storedfileID+"'.");
			return false;
		}
		
		CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(storedfileID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToStoredFile(user, storedfile);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified storedfile.
	 * @param user
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserStoredFileAssignments(CFWStoredFile storedfile, LinkedHashMap<String,String> usersKeyLabel) {
		
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			//----------------------------------------
			// Clean all and Add all New
				
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWStoredFileSharedUserMap())
						.delete()
						.where(CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE, storedfile.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWStoredFileSharedUserMap.class, "Update Shared User Assignments: "+storedfile.name());
			}
			
			if(usersKeyLabel != null) {
				for(String userID : usersKeyLabel.keySet()) {
					isSuccess &= assignUserToStoredFile(Integer.parseInt(userID), storedfile.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified storedfile.
	 * @param user
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromStoredFile(User user, CFWStoredFile storedfile) {
		
		if(user == null || storedfile == null ) {
			new CFWLog(logger)
				.warn("User and StoredFile cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || storedfile.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and StoredFile-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToStoredFile(user, storedfile)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to Stored File '"+storedfile.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromStoredFileSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWStoredFileSharedUserMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileSharedUserMap.class, "Remove User from Stored File: "+storedfile.name()+", User: "+user.username());
		
		return CFWDB.preparedExecute(removeUserFromStoredFileSQL, 
				user.id(),
				storedfile.id()
				);
	}
	

	/********************************************************************************************
	 * Remove a user from the storedfile.
	 * @param user
	 * @param storedfile
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromStoredFile(int userID, int storedfileID) {
		
		if(!checkIsUserAssignedToStoredFile(userID, storedfileID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the Stored File '"+ storedfileID+"' and cannot be removed.");
			return false;
		}
				
		CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(storedfileID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromStoredFile(user, storedfile);

	}
	
	/****************************************************************
	 * Check if the user is in the given storedfile.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToStoredFile(User user, CFWStoredFile storedfile) {
		
		if(user != null && storedfile != null) {
			return checkIsUserAssignedToStoredFile(user.id(), storedfile.id());
		}else {
			new CFWLog(logger)
				.severe("The user and Stored File cannot be null. User: '"+user+"', StoredFile: '"+storedfile+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToStoredFile(int userid, int storedfileid) {
		
		return 0 != new CFWSQL(new CFWStoredFileSharedUserMap())
			.queryCache()
			.selectCount()
			.where(CFWStoredFileSharedUserMapFields.FK_ID_USER.toString(), userid)
			.and(CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE.toString(), storedfileid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the storedfile for a user as key/labels.
//	 * Useful for autocomplete.
//	 * @param storedfile
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForStoredFileAsKeyLabel(Integer storedfileID) {
		
		if(storedfileID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.USERNAME, U.FIRSTNAME, U.LASTNAME "  
				+ " FROM "+User.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWStoredFileSharedUserMap.TABLE_NAME+" M ON M.FK_ID_USER = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_STOREDFILE = ? " 
				+ " ORDER BY LOWER(U.USERNAME) "
				;
		
		ArrayList<User> userList =  new CFWSQL(new User())
				.queryCache()
				.custom(query
						, storedfileID)
				.getAsObjectListConvert(User.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(User user : userList) {						
			result.put(user.id()+"", user.createUserLabel());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the user from the storedfile if it is assigned to the storedfile, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToStoredFile(String userID, String storedfileID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(storedfileID == null ^ !storedfileID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The storedfileID '"+storedfileID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToStoredFile(Integer.parseInt(userID), Integer.parseInt(storedfileID));
		
	}
	
	/***************************************************************
	 * Remove the user from the storedfile if it is assigned to the storedfile, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToStoredFile(int userID, int storedfileID) {
		
		if(checkIsUserAssignedToStoredFile(userID, storedfileID)) {
			return removeUserFromStoredFile(userID, storedfileID);
		}else {
			return assignUserToStoredFile(userID, storedfileID);
		}

	}
		
}

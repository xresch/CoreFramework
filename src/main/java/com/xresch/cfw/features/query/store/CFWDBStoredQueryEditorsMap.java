package com.xresch.cfw.features.query.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.features.query.store.CFWStoredQueryEditorsMap.CFWStoredQueryEditorsMapFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBStoredQueryEditorsMap {

	private static final String TABLE_NAME = new CFWStoredQueryEditorsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredQueryEditorsMap.class.getName());
	
	/********************************************************************************************
	 * Used to migrated from old field to new table
	 * 
	 ********************************************************************************************/
	public static boolean migrateOldStructure(CFWStoredQuery storedQuery) {
		
		if(storedQuery == null || storedQuery.id() == null) {
			return true;
		}
		
		boolean isSuccess = true;
		int storedQueryID = storedQuery.id();
		
		//-------------------------------------
		// Get Old data
		ResultSet result = new CFWSQL(new CFWStoredQuery())
			.select(CFWStoredQuery.FIELDNAME_EDITORS)
			.where(CFWStoredQueryFields.PK_ID, storedQueryID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(CFWStoredQuery.FIELDNAME_EDITORS);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Strings.isNullOrEmpty(editorsJSONString) ) { return true; }
		
		LinkedHashMap<String, String> selectedValues = CFW.JSON.fromJsonLinkedHashMap(editorsJSONString);
		
		//-------------------------------------
		// Insert into Table
		for (String userIDString : selectedValues.keySet() ) {
			int userID = Integer.parseInt(userIDString);
			
			// handle non-existing users
			if( CFW.DB.Users.selectByID(userID) == null ){
				continue;
			}
			
			// add user to storedQuery
			if(! checkIsUserAssignedToStoredQuery(userID, storedQueryID) ) {
				isSuccess &= assignUserToStoredQuery(userID, storedQueryID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the user to the specified storedQuery.
	 * @param user
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToStoredQuery(User user, CFWStoredQuery storedQuery) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(storedQuery == null) {
			new CFWLog(logger)
				.warn("StoredQuery cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || storedQuery.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or StoredQuery-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToStoredQuery(user, storedQuery)) {
			new CFWLog(logger)
				.warn("The storedQuery '"+storedQuery.name()+"' is already shared with '"+user.username()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWStoredQueryEditorsMapFields.FK_ID_USER +", "
				  + CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQueryEditorsMap.class, "Add User to StoredQuery: "+storedQuery.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				storedQuery.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQueryEditorsMap.class, "Add User to StoredQuery: "+storedQuery.name()+", User: "+user.username());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified storedQuery.
	 * @param userID
	 * @param storedQueryID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToStoredQuery(int userID, int storedQueryID) {
		
		
		if(userID < 0 || storedQueryID < 0) {
			new CFWLog(logger)
				.warn("User-ID or storedQuery-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToStoredQuery(userID, storedQueryID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the storedQuery '"+storedQueryID+"'.");
			return false;
		}
		
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(storedQueryID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToStoredQuery(user, storedQuery);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified storedQuery.
	 * @param user
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserStoredQueryAssignments(CFWStoredQuery storedQuery, LinkedHashMap<String,String> usersKeyLabel) {
				
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWStoredQueryEditorsMap())
						.delete()
						.where(CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY, storedQuery.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWStoredQueryEditorsMap.class, "Update Shared User Assignments: "+storedQuery.name());
			}
			
			if(usersKeyLabel != null) {
				for(String userID : usersKeyLabel.keySet()) {
					isSuccess &= assignUserToStoredQuery(Integer.parseInt(userID), storedQuery.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified storedQuery.
	 * @param user
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromStoredQuery(User user, CFWStoredQuery storedQuery) {
		
		if(user == null || storedQuery == null ) {
			new CFWLog(logger)
				.warn("User and StoredQuery cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || storedQuery.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and StoredQuery-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToStoredQuery(user, storedQuery)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to storedQuery '"+storedQuery.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromStoredQuerySQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWStoredQueryEditorsMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQueryEditorsMap.class, "Remove User from StoredQuery: "+storedQuery.name()+", User: "+user.username());
		
		return CFWDB.preparedExecute(removeUserFromStoredQuerySQL, 
				user.id(),
				storedQuery.id()
				);
	}
	

	/********************************************************************************************
	 * Remove a user from the storedQuery.
	 * @param user
	 * @param storedQuery
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromStoredQuery(int userID, int storedQueryID) {
		
		if(!checkIsUserAssignedToStoredQuery(userID, storedQueryID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the storedQuery '"+ storedQueryID+"' and cannot be removed.");
			return false;
		}
				
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(storedQueryID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromStoredQuery(user, storedQuery);

	}
	
	/****************************************************************
	 * Check if the user is in the given storedQuery.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToStoredQuery(User user, CFWStoredQuery storedQuery) {
		
		if(user != null && storedQuery != null) {
			return checkIsUserAssignedToStoredQuery(user.id(), storedQuery.id());
		}else {
			new CFWLog(logger)
				.severe("The user and storedQuery cannot be null. User: '"+user+"', StoredQuery: '"+storedQuery+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToStoredQuery(int userid, int storedQueryid) {
		
		return 0 != new CFWSQL(new CFWStoredQueryEditorsMap())
			.queryCache()
			.selectCount()
			.where(CFWStoredQueryEditorsMapFields.FK_ID_USER.toString(), userid)
			.and(CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY.toString(), storedQueryid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the storedQuery for a user as key/labels.
//	 * Useful for autocomplete.
//	 * @param storedQuery
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForStoredQueryAsKeyLabel(Integer storedQueryID) {
		
		if(storedQueryID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.USERNAME, U.FIRSTNAME, U.LASTNAME "  
				+ " FROM "+User.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWStoredQueryEditorsMap.TABLE_NAME+" M ON M.FK_ID_USER = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_STOREDQUERY = ? " 
				+ " ORDER BY LOWER(U.USERNAME) "
				;
		
		ArrayList<User> userList =  new CFWSQL(new User())
				.queryCache()
				.custom(query
						, storedQueryID)
				.getAsObjectListConvert(User.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(User user : userList) {						
			result.put(user.id()+"", user.createUserLabel());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the user from the storedQuery if it is assigned to the storedQuery, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToStoredQuery(String userID, String storedQueryID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(storedQueryID == null ^ !storedQueryID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The storedQueryID '"+storedQueryID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToStoredQuery(Integer.parseInt(userID), Integer.parseInt(storedQueryID));
		
	}
	
	/***************************************************************
	 * Remove the user from the storedQuery if it is assigned to the storedQuery, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToStoredQuery(int userID, int storedQueryID) {
		
		if(checkIsUserAssignedToStoredQuery(userID, storedQueryID)) {
			return removeUserFromStoredQuery(userID, storedQueryID);
		}else {
			return assignUserToStoredQuery(userID, storedQueryID);
		}

	}
		
}

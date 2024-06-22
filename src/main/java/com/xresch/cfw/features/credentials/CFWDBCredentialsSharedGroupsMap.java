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
import com.xresch.cfw.features.credentials.CFWCredentialsSharedGroupsMap.CFWCredentialsSharedGroupsMapFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBCredentialsSharedGroupsMap {

	private static final String TABLE_NAME = new CFWCredentialsSharedGroupsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBCredentialsSharedGroupsMap.class.getName());
	
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
			.select(CFWCredentials.FIELDNAME_SHARE_WITH_GROUPS)
			.where(CFWCredentialsFields.PK_ID, credentialsID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(CFWCredentials.FIELDNAME_SHARE_WITH_GROUPS);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Strings.isNullOrEmpty(editorsJSONString) ) { return true; }
		
		LinkedHashMap<String, String> selectedValues = CFW.JSON.fromJsonLinkedHashMap(editorsJSONString);
		
		//-------------------------------------
		// Insert into Table
		for (String roleIDString : selectedValues.keySet() ) {
			int roleID = Integer.parseInt(roleIDString);
			
			// handle non-existing roles
			if( CFW.DB.Roles.selectByID(roleID) == null ){
				continue;
			}
			
			// add role to credentials
			if(! checkIsGroupAssignedToCredentials(roleID, credentialsID) ) {
				isSuccess &= assignGroupToCredentials(roleID, credentialsID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the role to the specified credentials.
	 * @param role
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToCredentials(Role role, CFWCredentials credentials) {
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(credentials == null) {
			new CFWLog(logger)
				.warn("Credentials cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || credentials.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and/or Credentials-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToCredentials(role, credentials)) {
			new CFWLog(logger)
				.warn("The credentials '"+credentials.name()+"' is already shared with '"+role.name()+"'.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE +", "
				  + CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedGroupsMap.class, "Add Role to Credentials: "+credentials.name()+", Role: "+role.name());
		
		boolean success = CFWDB.preparedExecute(insertRoleSQL, 
				role.id(),
				credentials.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedGroupsMap.class, "Add Role to Credentials: "+credentials.name()+", Role: "+role.name());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the role to the specified credentials.
	 * @param roleID
	 * @param credentialsID
	 * @return return true if role was added or if role/credentials did not exist, false if failed
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToCredentials(int roleID, int credentialsID) {
		
		
		if(roleID < 0 || credentialsID < 0) {
			new CFWLog(logger)
				.warn("Role-ID or credentials-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToCredentials(roleID, credentialsID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is already part of the credentials '"+credentialsID+"'.");
			return false;
		}
		
		CFWCredentials credentials = CFW.DB.Credentials.selectByID(credentialsID);
		if(credentials == null) { return true; }
		
		Role role = CFW.DB.Roles.selectByID(roleID);
		if(role == null) { return true; }
		
		return assignGroupToCredentials(role, credentials);
	}
	
	
	/********************************************************************************************
	 * Adds the role to the specified credentials.
	 * @param role
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateGroupCredentialsAssignments(CFWCredentials credentials, LinkedHashMap<String,String> rolesKeyLabel) {
				
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			//----------------------------------------
			// Clean all and Add all New
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWCredentialsSharedGroupsMap())
						.delete()
						.where(CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS, credentials.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWCredentialsSharedGroupsMap.class, "Update Shared Role Assignments: "+credentials.name());
			}
		
			if(rolesKeyLabel != null) {
				for(String roleID : rolesKeyLabel.keySet()) {
					isSuccess &= assignGroupToCredentials(Integer.parseInt(roleID), credentials.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the role to the specified credentials.
	 * @param role
	 * @param credentials
	 * @return return true if credentials was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromCredentials(Role role, CFWCredentials credentials) {
		
		if(role == null || credentials == null ) {
			new CFWLog(logger)
				.warn("Role and Credentials cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || credentials.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and Credentials-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsGroupAssignedToCredentials(role, credentials)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is not assigned to credentials '"+credentials.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeRoleFromCredentialsSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentialsSharedGroupsMap.class, "Remove Role from Credentials: "+credentials.name()+", Role: "+role.name());
		
		return CFWDB.preparedExecute(removeRoleFromCredentialsSQL, 
				role.id(),
				credentials.id()
				);
	}

	/********************************************************************************************
	 * Remove a role from the credentials.
	 * @param role
	 * @param credentials
	 * @return return true if role was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromCredentials(int roleID, int credentialsID) {
		
		if(!checkIsGroupAssignedToCredentials(roleID, credentialsID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is not assigned to the credentials '"+ credentialsID+"' and cannot be removed.");
			return false;
		}
				
		CFWCredentials credentials = CFW.DB.Credentials.selectByID(credentialsID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeGroupFromCredentials(role, credentials);

	}
	
	/****************************************************************
	 * Check if the role is in the given credentials.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToCredentials(Role role, CFWCredentials credentials) {
		
		if(role != null && credentials != null) {
			return checkIsGroupAssignedToCredentials(role.id(), credentials.id());
		}else {
			new CFWLog(logger)
				.severe("The role and credentials cannot be null. Role: '"+role+"', Credentials: '"+credentials+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToCredentials(int roleid, int credentialsid) {
		
		return 0 != new CFWSQL(new CFWCredentialsSharedGroupsMap())
			.queryCache()
			.selectCount()
			.where(CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE.toString(), roleid)
			.and(CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS.toString(), credentialsid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the credentials for a role as key/labels.
//	 * Useful for autocomplete.
//	 * @param credentials
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectGroupsForCredentialsAsKeyLabel(Integer credentialsID) {
		
		if(credentialsID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.NAME"  
				+ " FROM "+Role.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWCredentialsSharedGroupsMap.TABLE_NAME+" M ON M.FK_ID_ROLE = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_CREDENTIALS = ? " 
				+ " ORDER BY LOWER(U.NAME) "
				;
		
		ArrayList<Role> roleList =  new CFWSQL(new Role())
				.queryCache()
				.custom(query
						, credentialsID)
				.getAsObjectListConvert(Role.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(Role role : roleList) {						
			result.put(role.id()+"", role.name());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the role from the credentials if it is assigned to the credentials, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToCredentials(String roleID, String credentialsID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(credentialsID == null ^ !credentialsID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The credentialsID '"+credentialsID+"' is not a number.");
			return false;
		}
		
		return toogleGroupAssignedToCredentials(Integer.parseInt(roleID), Integer.parseInt(credentialsID));
		
	}
	
	/***************************************************************
	 * Remove the role from the credentials if it is assigned to the credentials, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToCredentials(int roleID, int credentialsID) {
		
		if(checkIsGroupAssignedToCredentials(roleID, credentialsID)) {
			return removeGroupFromCredentials(roleID, credentialsID);
		}else {
			return assignGroupToCredentials(roleID, credentialsID);
		}

	}
		
}

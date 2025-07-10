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
import com.xresch.cfw.features.filemanager.CFWStoredFileEditorGroupsMap.CFWStoredFileEditorGroupsMapFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBStoredFileEditorGroupsMap {

	private static final String TABLE_NAME = new CFWStoredFileEditorGroupsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredFileEditorGroupsMap.class.getName());
	
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
			.select(CFWStoredFile.FIELDNAME_EDITOR_GROUPS)
			.where(CFWStoredFileFields.PK_ID, storedfileID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(CFWStoredFile.FIELDNAME_EDITOR_GROUPS);
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
			
			// add role to storedfile
			if(! checkIsGroupAssignedToStoredFile(roleID, storedfileID) ) {
				isSuccess &= assignGroupToStoredFile(roleID, storedfileID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the role to the specified storedfile.
	 * @param role
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToStoredFile(Role role, CFWStoredFile storedfile) {
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(storedfile == null) {
			new CFWLog(logger)
				.warn("StoredFile cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || storedfile.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and/or StoredFile-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToStoredFile(role, storedfile)) {
			new CFWLog(logger)
				.warn("The Stored File '"+storedfile.name()+"' is already assigned to role '"+role.name()+"' to allow editing.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWStoredFileEditorGroupsMapFields.FK_ID_ROLE +", "
				  + CFWStoredFileEditorGroupsMapFields.FK_ID_STOREDFILE
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileEditorGroupsMap.class, "Add Role to StoredFile: "+storedfile.name()+", Role: "+role.name());
		
		boolean success = CFWDB.preparedExecute(insertRoleSQL, 
				role.id(),
				storedfile.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileEditorGroupsMap.class, "Add Role to StoredFile: "+storedfile.name()+", Role: "+role.name());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the role to the specified storedfile.
	 * @param roleID
	 * @param storedfileID
	 * @return return true if role was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToStoredFile(int roleID, int storedfileID) {
		
		
		if(roleID < 0 || storedfileID < 0) {
			new CFWLog(logger)
				.warn("Role-ID or storedfile-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToStoredFile(roleID, storedfileID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is already part of the Stored File '"+storedfileID+"'.");
			return false;
		}
		
		CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(storedfileID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		
		return assignGroupToStoredFile(role, storedfile);
	}
	
	
	/********************************************************************************************
	 * Adds the role to the specified storedfile.
	 * @param role
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateGroupStoredFileAssignments(CFWStoredFile storedfile, LinkedHashMap<String,String> rolesKeyLabel) {
			
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
			
			//----------------------------------------
			// Clean all and Add all New
			
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWStoredFileEditorGroupsMap())
						.delete()
						.where(CFWStoredFileEditorGroupsMapFields.FK_ID_STOREDFILE, storedfile.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWStoredFileEditorGroupsMap.class, "Update Editor Group Assignments: "+storedfile.name());
			}
			
			if(rolesKeyLabel != null) {
				for(String roleID : rolesKeyLabel.keySet()) {
					isSuccess &= assignGroupToStoredFile(Integer.parseInt(roleID), storedfile.id());
				}
			}
			
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }
		

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the role to the specified storedfile.
	 * @param role
	 * @param storedfile
	 * @return return true if storedfile was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromStoredFile(Role role, CFWStoredFile storedfile) {
		
		if(role == null || storedfile == null ) {
			new CFWLog(logger)
				.warn("Role and StoredFile cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || storedfile.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and StoredFile-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsGroupAssignedToStoredFile(role, storedfile)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is not assigned to Stored File '"+storedfile.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeRoleFromStoredFileSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWStoredFileEditorGroupsMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + CFWStoredFileEditorGroupsMapFields.FK_ID_STOREDFILE +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFileEditorGroupsMap.class, "Remove Role from Stored File: "+storedfile.name()+", Role: "+role.name());
		
		return CFWDB.preparedExecute(removeRoleFromStoredFileSQL, 
				role.id(),
				storedfile.id()
				);
	}

	/********************************************************************************************
	 * Remove a role from the storedfile.
	 * @param role
	 * @param storedfile
	 * @return return true if role was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromStoredFile(int roleID, int storedfileID) {
		
		if(!checkIsGroupAssignedToStoredFile(roleID, storedfileID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is not assigned to the Stored File '"+ storedfileID+"' and cannot be removed.");
			return false;
		}
				
		CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(storedfileID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeGroupFromStoredFile(role, storedfile);

	}
	
	/****************************************************************
	 * Check if the role is in the given storedfile.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToStoredFile(Role role, CFWStoredFile storedfile) {
		
		if(role != null && storedfile != null) {
			return checkIsGroupAssignedToStoredFile(role.id(), storedfile.id());
		}else {
			new CFWLog(logger)
				.severe("The role and Stored File cannot be null. Role: '"+role+"', StoredFile: '"+storedfile+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToStoredFile(int roleid, int storedfileid) {
		
		return 0 != new CFWSQL(new CFWStoredFileEditorGroupsMap())
			.queryCache()
			.selectCount()
			.where(CFWStoredFileEditorGroupsMapFields.FK_ID_ROLE.toString(), roleid)
			.and(CFWStoredFileEditorGroupsMapFields.FK_ID_STOREDFILE.toString(), storedfileid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the storedfile for a role as key/labels.
//	 * Useful for autocomplete.
//	 * @param storedfile
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectGroupsForStoredFileAsKeyLabel(Integer storedfileID) {
		
		if(storedfileID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.NAME"  
				+ " FROM "+Role.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWStoredFileEditorGroupsMap.TABLE_NAME+" M ON M.FK_ID_ROLE = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_STOREDFILE = ? " 
				+ " ORDER BY LOWER(U.NAME) "
				;
		
		ArrayList<Role> roleList =  new CFWSQL(new Role())
				.queryCache()
				.custom(query
						, storedfileID)
				.getAsObjectListConvert(Role.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(Role role : roleList) {						
			result.put(role.id()+"", role.name());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the role from the storedfile if it is assigned to the storedfile, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToStoredFile(String roleID, String storedfileID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(storedfileID == null ^ !storedfileID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The storedfileID '"+storedfileID+"' is not a number.");
			return false;
		}
		
		return toogleGroupAssignedToStoredFile(Integer.parseInt(roleID), Integer.parseInt(storedfileID));
		
	}
	
	/***************************************************************
	 * Remove the role from the storedfile if it is assigned to the storedfile, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToStoredFile(int roleID, int storedfileID) {
		
		if(checkIsGroupAssignedToStoredFile(roleID, storedfileID)) {
			return removeGroupFromStoredFile(roleID, storedfileID);
		}else {
			return assignGroupToStoredFile(roleID, storedfileID);
		}

	}
		
}

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
import com.xresch.cfw.features.query.store.CFWStoredQuerySharedGroupsMap.CFWStoredQuerySharedGroupsMapFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBStoredQuerySharedGroupsMap {

	private static final String TABLE_NAME = new CFWStoredQuerySharedGroupsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredQuerySharedGroupsMap.class.getName());
	
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
			.select(CFWStoredQuery.FIELDNAME_SHARE_WITH_GROUPS)
			.where(CFWStoredQueryFields.PK_ID, storedQueryID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(CFWStoredQuery.FIELDNAME_SHARE_WITH_GROUPS);
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
			
			// add role to storedQuery
			if(! checkIsGroupAssignedToStoredQuery(roleID, storedQueryID) ) {
				isSuccess &= assignGroupToStoredQuery(roleID, storedQueryID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the role to the specified storedQuery.
	 * @param role
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToStoredQuery(Role role, CFWStoredQuery storedQuery) {
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(storedQuery == null) {
			new CFWLog(logger)
				.warn("StoredQuery cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || storedQuery.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and/or StoredQuery-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToStoredQuery(role, storedQuery)) {
			new CFWLog(logger)
				.warn("The storedQuery '"+storedQuery.name()+"' is already shared with '"+role.name()+"'.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE +", "
				  + CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQuerySharedGroupsMap.class, "Add Role to StoredQuery: "+storedQuery.name()+", Role: "+role.name());
		
		boolean success = CFWDB.preparedExecute(insertRoleSQL, 
				role.id(),
				storedQuery.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQuerySharedGroupsMap.class, "Add Role to StoredQuery: "+storedQuery.name()+", Role: "+role.name());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the role to the specified storedQuery.
	 * @param roleID
	 * @param storedQueryID
	 * @return return true if role was added or if role/storedQuery did not exist, false if failed
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToStoredQuery(int roleID, int storedQueryID) {
		
		
		if(roleID < 0 || storedQueryID < 0) {
			new CFWLog(logger)
				.warn("Role-ID or storedQuery-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToStoredQuery(roleID, storedQueryID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is already part of the storedQuery '"+storedQueryID+"'.");
			return false;
		}
		
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(storedQueryID);
		if(storedQuery == null) { return true; }
		
		Role role = CFW.DB.Roles.selectByID(roleID);
		if(role == null) { return true; }
		
		return assignGroupToStoredQuery(role, storedQuery);
	}
	
	
	/********************************************************************************************
	 * Adds the role to the specified storedQuery.
	 * @param role
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateGroupStoredQueryAssignments(CFWStoredQuery storedQuery, LinkedHashMap<String,String> rolesKeyLabel) {
				
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			//----------------------------------------
			// Clean all and Add all New
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWStoredQuerySharedGroupsMap())
						.delete()
						.where(CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY, storedQuery.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWStoredQuerySharedGroupsMap.class, "Update Shared Role Assignments: "+storedQuery.name());
			}
		
			if(rolesKeyLabel != null) {
				for(String roleID : rolesKeyLabel.keySet()) {
					isSuccess &= assignGroupToStoredQuery(Integer.parseInt(roleID), storedQuery.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the role to the specified storedQuery.
	 * @param role
	 * @param storedQuery
	 * @return return true if storedQuery was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromStoredQuery(Role role, CFWStoredQuery storedQuery) {
		
		if(role == null || storedQuery == null ) {
			new CFWLog(logger)
				.warn("Role and StoredQuery cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || storedQuery.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and StoredQuery-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsGroupAssignedToStoredQuery(role, storedQuery)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is not assigned to storedQuery '"+storedQuery.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeRoleFromStoredQuerySQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQuerySharedGroupsMap.class, "Remove Role from StoredQuery: "+storedQuery.name()+", Role: "+role.name());
		
		return CFWDB.preparedExecute(removeRoleFromStoredQuerySQL, 
				role.id(),
				storedQuery.id()
				);
	}

	/********************************************************************************************
	 * Remove a role from the storedQuery.
	 * @param role
	 * @param storedQuery
	 * @return return true if role was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromStoredQuery(int roleID, int storedQueryID) {
		
		if(!checkIsGroupAssignedToStoredQuery(roleID, storedQueryID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is not assigned to the storedQuery '"+ storedQueryID+"' and cannot be removed.");
			return false;
		}
				
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(storedQueryID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeGroupFromStoredQuery(role, storedQuery);

	}
	
	/****************************************************************
	 * Check if the role is in the given storedQuery.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToStoredQuery(Role role, CFWStoredQuery storedQuery) {
		
		if(role != null && storedQuery != null) {
			return checkIsGroupAssignedToStoredQuery(role.id(), storedQuery.id());
		}else {
			new CFWLog(logger)
				.severe("The role and storedQuery cannot be null. Role: '"+role+"', StoredQuery: '"+storedQuery+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToStoredQuery(int roleid, int storedQueryid) {
		
		return 0 != new CFWSQL(new CFWStoredQuerySharedGroupsMap())
			.queryCache()
			.selectCount()
			.where(CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE.toString(), roleid)
			.and(CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY.toString(), storedQueryid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the storedQuery for a role as key/labels.
//	 * Useful for autocomplete.
//	 * @param storedQuery
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectGroupsForStoredQueryAsKeyLabel(Integer storedQueryID) {
		
		if(storedQueryID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.NAME"  
				+ " FROM "+Role.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWStoredQuerySharedGroupsMap.TABLE_NAME+" M ON M.FK_ID_ROLE = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_STOREDQUERY = ? " 
				+ " ORDER BY LOWER(U.NAME) "
				;
		
		ArrayList<Role> roleList =  new CFWSQL(new Role())
				.queryCache()
				.custom(query
						, storedQueryID)
				.getAsObjectListConvert(Role.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(Role role : roleList) {						
			result.put(role.id()+"", role.name());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the role from the storedQuery if it is assigned to the storedQuery, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToStoredQuery(String roleID, String storedQueryID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(storedQueryID == null ^ !storedQueryID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The storedQueryID '"+storedQueryID+"' is not a number.");
			return false;
		}
		
		return toogleGroupAssignedToStoredQuery(Integer.parseInt(roleID), Integer.parseInt(storedQueryID));
		
	}
	
	/***************************************************************
	 * Remove the role from the storedQuery if it is assigned to the storedQuery, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToStoredQuery(int roleID, int storedQueryID) {
		
		if(checkIsGroupAssignedToStoredQuery(roleID, storedQueryID)) {
			return removeGroupFromStoredQuery(roleID, storedQueryID);
		}else {
			return assignGroupToStoredQuery(roleID, storedQueryID);
		}

	}
		
}

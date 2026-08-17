package com.xresch.cfw.features.spaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.spaces.CFWSpaceUserGroupsMap.CFWSpaceUserGroupsMapFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWDBSpaceUserGroupsMap {

	private static final String TABLE_NAME = new CFWSpaceUserGroupsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBSpaceUserGroupsMap.class.getName());
	
	/********************************************************************************************
	 * Adds the role to the specified space.
	 * @param role
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToSpace(Role role, CFWSpace space) {
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(space == null) {
			new CFWLog(logger)
				.warn("CFWSpace cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || space.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and/or CFWSpace-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToSpace(role, space)) {
			new CFWLog(logger)
				.warn("The space '"+space.name()+"' is already shared with '"+role.name()+"'.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + CFWSpaceUserGroupsMapFields.FK_ID_ROLE +", "
				  + CFWSpaceUserGroupsMapFields.FK_ID_SPACE
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceUserGroupsMap.class, "Add Role to CFWSpace: "+space.name()+", Role: "+role.name());
		
		boolean success = CFWDB.preparedExecute(insertRoleSQL, 
				role.id(),
				space.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceUserGroupsMap.class, "Add Role to CFWSpace: "+space.name()+", Role: "+role.name());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the role to the specified space.
	 * @param roleID
	 * @param spaceID
	 * @return return true if role was added or if role/space did not exist, false if failed
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToSpace(int roleID, int spaceID) {
		
		
		if(roleID < 0 || spaceID < 0) {
			new CFWLog(logger)
				.warn("Role-ID or space-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToSpace(roleID, spaceID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is already part of the space '"+spaceID+"'.");
			return false;
		}
		
		CFWSpace space = CFW.DB.Spaces.selectByID(spaceID);
		if(space == null) { return true; }
		
		Role role = CFW.DB.Roles.selectByID(roleID);
		if(role == null) { return true; }
		
		return assignGroupToSpace(role, space);
	}
	
	
	/********************************************************************************************
	 * Adds the role to the specified space.
	 * @param role
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateGroupSpaceAssignments(CFWSpace space, LinkedHashMap<String,String> rolesKeyLabel) {
				
		boolean isSuccess = true;	
		
		boolean wasStarted =CFW.DB.transactionIsStarted();
		if(!wasStarted) { CFW.DB.transactionStart(); }
		
			//----------------------------------------
			// Clean all and Add all New
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new CFWSpaceUserGroupsMap())
						.delete()
						.where(CFWSpaceUserGroupsMapFields.FK_ID_SPACE, space.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, CFWSpaceUserGroupsMap.class, "Update Shared Role Assignments: "+space.name());
			}
		
			if(rolesKeyLabel != null) {
				for(String roleID : rolesKeyLabel.keySet()) {
					isSuccess &= assignGroupToSpace(Integer.parseInt(roleID), space.id());
				}
			}
		
		if(!wasStarted) { CFW.DB.transactionEnd(isSuccess); }

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the role to the specified space.
	 * @param role
	 * @param space
	 * @return return true if space was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromSpace(Role role, CFWSpace space) {
		
		if(role == null || space == null ) {
			new CFWLog(logger)
				.warn("Role and CFWSpace cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || space.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and CFWSpace-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsGroupAssignedToSpace(role, space)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is not assigned to space '"+space.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeRoleFromCFWSpaceSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + CFWSpaceUserGroupsMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + CFWSpaceUserGroupsMapFields.FK_ID_SPACE +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWSpaceUserGroupsMap.class, "Remove Role from CFWSpace: "+space.name()+", Role: "+role.name());
		
		return CFWDB.preparedExecute(removeRoleFromCFWSpaceSQL, 
				role.id(),
				space.id()
				);
	}

	/********************************************************************************************
	 * Remove a role from the space.
	 * @param role
	 * @param space
	 * @return return true if role was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromSpace(int roleID, int spaceID) {
		
		if(!checkIsGroupAssignedToSpace(roleID, spaceID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is not assigned to the space '"+ spaceID+"' and cannot be removed.");
			return false;
		}
				
		CFWSpace space = CFW.DB.Spaces.selectByID(spaceID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeGroupFromSpace(role, space);

	}
	
	/****************************************************************
	 * Check if the role is in the given space.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToSpace(Role role, CFWSpace space) {
		
		if(role != null && space != null) {
			return checkIsGroupAssignedToSpace(role.id(), space.id());
		}else {
			new CFWLog(logger)
				.severe("The role and space cannot be null. Role: '"+role+"', CFWSpace: '"+space+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToSpace(int roleid, int spaceid) {
		
		return 0 != new CFWSQL(new CFWSpaceUserGroupsMap())
			.queryCache()
			.selectCount()
			.where(CFWSpaceUserGroupsMapFields.FK_ID_ROLE.toString(), roleid)
			.and(CFWSpaceUserGroupsMapFields.FK_ID_SPACE.toString(), spaceid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the spaces for a role as key/labels.
//	 * Useful for autocomplete.
//	 * @param space
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectGroupsForSpaceAsKeyLabel(Integer spaceID) {
		
		if(spaceID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.NAME"  
				+ " FROM "+Role.TABLE_NAME+" U " 
				+ " LEFT JOIN "+CFWSpaceUserGroupsMap.TABLE_NAME+" M ON M.FK_ID_ROLE = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_SPACE = ? " 
				+ " ORDER BY LOWER(U.NAME) "
				;
		
		ArrayList<Role> roleList =  new CFWSQL(new Role())
				.queryCache()
				.custom(query
						, spaceID)
				.getAsObjectListConvert(Role.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(Role role : roleList) {						
			result.put(role.id()+"", role.name());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the role from the space if it is assigned to the space, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToSpace(String roleID, String spaceID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(spaceID == null ^ !spaceID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The spaceID '"+spaceID+"' is not a number.");
			return false;
		}
		
		return toogleGroupAssignedToSpace(Integer.parseInt(roleID), Integer.parseInt(spaceID));
		
	}
	
	/***************************************************************
	 * Remove the role from the space if it is assigned to the space, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToSpace(int roleID, int spaceID) {
		
		if(checkIsGroupAssignedToSpace(roleID, spaceID)) {
			return removeGroupFromSpace(roleID, spaceID);
		}else {
			return assignGroupToSpace(roleID, spaceID);
		}

	}
		
}

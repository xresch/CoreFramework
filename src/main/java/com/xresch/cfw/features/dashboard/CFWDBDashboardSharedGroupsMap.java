package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.dashboard.DashboardSharedGroupsMap.DashboardSharedGroupsMapFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBDashboardSharedGroupsMap {

	private static final String TABLE_NAME = new DashboardSharedGroupsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardSharedGroupsMap.class.getName());
	
	/********************************************************************************************
	 * Used to migrated from old field to new table
	 * 
	 ********************************************************************************************/
	public static boolean migrateOldStructure(Dashboard dashboard) {
		
		if(dashboard == null || dashboard.id() == null) {
			return true;
		}
		
		boolean isSuccess = true;
		int dashboardID = dashboard.id();
		
		//-------------------------------------
		// Get Old data
		ResultSet result = new CFWSQL(new Dashboard())
			.select(Dashboard.FIELDNAME_SHARE_WITH_GROUPS)
			.where(DashboardFields.PK_ID, dashboardID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(Dashboard.FIELDNAME_SHARE_WITH_GROUPS);
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
			
			// add role to dashboard
			if(! checkIsGroupAssignedToDashboard(roleID, dashboardID) ) {
				isSuccess &= assignGroupToDashboard(roleID, dashboardID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the role to the specified dashboard.
	 * @param role
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToDashboard(Role role, Dashboard dashboard) {
		
		if(role == null) {
			new CFWLog(logger)
				.warn("Role cannot be null.");
			return false;
		}
		
		if(dashboard == null) {
			new CFWLog(logger)
				.warn("Dashboard cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || dashboard.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and/or Dashboard-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToDashboard(role, dashboard)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboard.name()+"' is already shared with '"+role.name()+"'.");
			return false;
		}
		
		String insertRoleSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + DashboardSharedGroupsMapFields.FK_ID_ROLE +", "
				  + DashboardSharedGroupsMapFields.FK_ID_DASHBOARD
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardSharedGroupsMap.class, "Add Role to Dashboard: "+dashboard.name()+", Role: "+role.name());
		
		boolean success = CFWDB.preparedExecute(insertRoleSQL, 
				role.id(),
				dashboard.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardSharedGroupsMap.class, "Add Role to Dashboard: "+dashboard.name()+", Role: "+role.name());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the role to the specified dashboard.
	 * @param roleID
	 * @param dashboardID
	 * @return return true if role was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignGroupToDashboard(int roleID, int dashboardID) {
		
		
		if(roleID < 0 || dashboardID < 0) {
			new CFWLog(logger)
				.warn("Role-ID or dashboard-ID are not set correctly.");
			return false;
		}
		
		if(checkIsGroupAssignedToDashboard(roleID, dashboardID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is already part of the dashboard '"+dashboardID+"'.");
			return false;
		}
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		
		return assignGroupToDashboard(role, dashboard);
	}
	
	
	/********************************************************************************************
	 * Adds the role to the specified dashboard.
	 * @param role
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateGroupDashboardAssignments(Dashboard dashboard, LinkedHashMap<String,String> rolesKeyLabel) {
				
		//----------------------------------------
		// Clean all and Add all New
		
		CFW.DB.transactionStart();
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new DashboardSharedGroupsMap())
						.delete()
						.where(DashboardSharedGroupsMapFields.FK_ID_DASHBOARD, dashboard.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, DashboardSharedGroupsMap.class, "Update Shared Role Assignments: "+dashboard.name());
			}
			
			boolean isSuccess = true;
			for(String roleID : rolesKeyLabel.keySet()) {
				isSuccess &= assignGroupToDashboard(Integer.parseInt(roleID), dashboard.id());
			}
		
		CFW.DB.transactionEnd(isSuccess);

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the role to the specified dashboard.
	 * @param role
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromDashboard(Role role, Dashboard dashboard) {
		
		if(role == null || dashboard == null ) {
			new CFWLog(logger)
				.warn("Role and Dashboard cannot be null.");
			return false;
		}
		
		if(role.id() < 0 || dashboard.id() < 0) {
			new CFWLog(logger)
				.warn("Role-ID and Dashboard-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsGroupAssignedToDashboard(role, dashboard)) {
			new CFWLog(logger)
				.warn("The role '"+role.name()+"' is not assigned to dashboard '"+dashboard.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeRoleFromDashboardSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + DashboardSharedGroupsMapFields.FK_ID_ROLE +" = ? "
				  + " AND "
				  + DashboardSharedGroupsMapFields.FK_ID_DASHBOARD +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardSharedGroupsMap.class, "Remove Role from Dashboard: "+dashboard.name()+", Role: "+role.name());
		
		return CFWDB.preparedExecute(removeRoleFromDashboardSQL, 
				role.id(),
				dashboard.id()
				);
	}

	/********************************************************************************************
	 * Remove a role from the dashboard.
	 * @param role
	 * @param dashboard
	 * @return return true if role was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeGroupFromDashboard(int roleID, int dashboardID) {
		
		if(!checkIsGroupAssignedToDashboard(roleID, dashboardID)) {
			new CFWLog(logger)
				.warn("The role '"+roleID+"' is not assigned to the dashboard '"+ dashboardID+"' and cannot be removed.");
			return false;
		}
				
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		Role role = CFW.DB.Roles.selectByID(roleID);
		return removeGroupFromDashboard(role, dashboard);

	}
	
	/****************************************************************
	 * Check if the role is in the given dashboard.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToDashboard(Role role, Dashboard dashboard) {
		
		if(role != null && dashboard != null) {
			return checkIsGroupAssignedToDashboard(role.id(), dashboard.id());
		}else {
			new CFWLog(logger)
				.severe("The role and dashboard cannot be null. Role: '"+role+"', Dashboard: '"+dashboard+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the role exists by name.
	 * 
	 * @param role to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsGroupAssignedToDashboard(int roleid, int dashboardid) {
		
		return 0 != new CFWSQL(new DashboardSharedGroupsMap())
			.queryCache()
			.selectCount()
			.where(DashboardSharedGroupsMapFields.FK_ID_ROLE.toString(), roleid)
			.and(DashboardSharedGroupsMapFields.FK_ID_DASHBOARD.toString(), dashboardid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the dashboards for a role as key/labels.
//	 * Useful for autocomplete.
//	 * @param dashboard
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectGroupsForDashboardAsKeyLabel(Integer dashboardID) {
		
		if(dashboardID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.NAME"  
				+ " FROM "+Role.TABLE_NAME+" U " 
				+ " LEFT JOIN "+DashboardSharedGroupsMap.TABLE_NAME+" M ON M.FK_ID_ROLE = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_DASHBOARD = ? " 
				+ " ORDER BY LOWER(U.NAME) "
				;
		
		ArrayList<Role> roleList =  new CFWSQL(new Role())
				.queryCache()
				.custom(query
						, dashboardID)
				.getAsObjectListConvert(Role.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(Role role : roleList) {						
			result.put(role.id()+"", role.name());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the role from the dashboard if it is assigned to the dashboard, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToDashboard(String roleID, String dashboardID) {
		
		//----------------------------------
		// Check input format
		if(roleID == null ^ !roleID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The roleID '"+roleID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(dashboardID == null ^ !dashboardID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The dashboardID '"+dashboardID+"' is not a number.");
			return false;
		}
		
		return toogleGroupAssignedToDashboard(Integer.parseInt(roleID), Integer.parseInt(dashboardID));
		
	}
	
	/***************************************************************
	 * Remove the role from the dashboard if it is assigned to the dashboard, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleGroupAssignedToDashboard(int roleID, int dashboardID) {
		
		if(checkIsGroupAssignedToDashboard(roleID, dashboardID)) {
			return removeGroupFromDashboard(roleID, dashboardID);
		}else {
			return assignGroupToDashboard(roleID, dashboardID);
		}

	}
		
}

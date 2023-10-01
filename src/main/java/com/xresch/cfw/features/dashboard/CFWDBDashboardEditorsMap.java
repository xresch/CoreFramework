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
import com.xresch.cfw.features.dashboard.DashboardEditorsMap.DashboardEditorsMapFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBDashboardEditorsMap {

	private static final String TABLE_NAME = new DashboardEditorsMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardEditorsMap.class.getName());
	
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
			.select(Dashboard.FIELDNAME_EDITORS)
			.where(DashboardFields.PK_ID, dashboardID)
			.getResultSet();
			;
		
		String editorsJSONString = null; 
		
		try {
			if(result.next()) {
				editorsJSONString = result.getString(Dashboard.FIELDNAME_EDITORS);
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
			
			// add user to dashboard
			if(! checkIsUserAssignedToDashboard(userID, dashboardID) ) {
				isSuccess &= assignUserToDashboard(userID, dashboardID);
			}
		}
		
		return isSuccess;
	}
	
	/********************************************************************************************
	 * Adds the user to the specified dashboard.
	 * @param user
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToDashboard(User user, Dashboard dashboard) {
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(dashboard == null) {
			new CFWLog(logger)
				.warn("Dashboard cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || dashboard.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and/or Dashboard-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToDashboard(user, dashboard)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboard.name()+"' is already shared with '"+user.username()+"'.");
			return false;
		}
		
		String insertUserSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + DashboardEditorsMapFields.FK_ID_USER +", "
				  + DashboardEditorsMapFields.FK_ID_DASHBOARD
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardEditorsMap.class, "Add User to Dashboard: "+dashboard.name()+", User: "+user.username());
		
		boolean success = CFWDB.preparedExecute(insertUserSQL, 
				user.id(),
				dashboard.id()
				);
		
		if(success) {
			new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardEditorsMap.class, "Add User to Dashboard: "+dashboard.name()+", User: "+user.username());
		}

		return success;
		
	}
	
	/********************************************************************************************
	 * Adds the user to the specified dashboard.
	 * @param userID
	 * @param dashboardID
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean assignUserToDashboard(int userID, int dashboardID) {
		
		
		if(userID < 0 || dashboardID < 0) {
			new CFWLog(logger)
				.warn("User-ID or dashboard-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserAssignedToDashboard(userID, dashboardID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is already part of the dashboard '"+dashboardID+"'.");
			return false;
		}
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		User user = CFW.DB.Users.selectByID(userID);
		
		return assignUserToDashboard(user, dashboard);
	}
	
	
	/********************************************************************************************
	 * Adds the user to the specified dashboard.
	 * @param user
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateUserDashboardAssignments(Dashboard dashboard, LinkedHashMap<String,String> usersKeyLabel) {
				
		//----------------------------------------
		// Clean all and Add all New
		
		CFW.DB.transactionStart();
		
			// only returns true if anything was updated. Therefore cannot include in check.
			boolean hasCleared = new CFWSQL(new DashboardEditorsMap())
						.delete()
						.where(DashboardEditorsMapFields.FK_ID_DASHBOARD, dashboard.id())
						.executeDelete();
			
			if(hasCleared) {
				new CFWLog(logger).audit(CFWAuditLogAction.CLEAR, DashboardEditorsMap.class, "Update Shared User Assignments: "+dashboard.name());
			}
			
			boolean isSuccess = true;
			for(String userID : usersKeyLabel.keySet()) {
				isSuccess &= assignUserToDashboard(Integer.parseInt(userID), dashboard.id());
			}
		
		CFW.DB.transactionEnd(isSuccess);

		return isSuccess;
	}
	

	/********************************************************************************************
	 * Adds the user to the specified dashboard.
	 * @param user
	 * @param dashboard
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromDashboard(User user, Dashboard dashboard) {
		
		if(user == null || dashboard == null ) {
			new CFWLog(logger)
				.warn("User and Dashboard cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || dashboard.id() < 0) {
			new CFWLog(logger)
				.warn("User-ID and Dashboard-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsUserAssignedToDashboard(user, dashboard)) {
			new CFWLog(logger)
				.warn("The user '"+user.username()+"' is not assigned to dashboard '"+dashboard.name()+"' and cannot be removed.");
			return false;
		}
		
		String removeUserFromDashboardSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + DashboardEditorsMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + DashboardEditorsMapFields.FK_ID_DASHBOARD +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardEditorsMap.class, "Remove User from Dashboard: "+dashboard.name()+", User: "+user.username());
		
		return CFWDB.preparedExecute(removeUserFromDashboardSQL, 
				user.id(),
				dashboard.id()
				);
	}
	

	/********************************************************************************************
	 * Remove a user from the dashboard.
	 * @param user
	 * @param dashboard
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromDashboard(int userID, int dashboardID) {
		
		if(!checkIsUserAssignedToDashboard(userID, dashboardID)) {
			new CFWLog(logger)
				.warn("The user '"+userID+"' is not assigned to the dashboard '"+ dashboardID+"' and cannot be removed.");
			return false;
		}
				
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		User user = CFW.DB.Users.selectByID(userID);
		return removeUserFromDashboard(user, dashboard);

	}
	
	/****************************************************************
	 * Check if the user is in the given dashboard.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToDashboard(User user, Dashboard dashboard) {
		
		if(user != null && dashboard != null) {
			return checkIsUserAssignedToDashboard(user.id(), dashboard.id());
		}else {
			new CFWLog(logger)
				.severe("The user and dashboard cannot be null. User: '"+user+"', Dashboard: '"+dashboard+"'");
		}
		return false;
	}
	

	/****************************************************************
	 * Check if the user exists by name.
	 * 
	 * @param user to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserAssignedToDashboard(int userid, int dashboardid) {
		
		return 0 != new CFWSQL(new DashboardEditorsMap())
			.queryCache()
			.selectCount()
			.where(DashboardEditorsMapFields.FK_ID_USER.toString(), userid)
			.and(DashboardEditorsMapFields.FK_ID_DASHBOARD.toString(), dashboardid)
			.executeCount();

	}

//	/***************************************************************
//	 * Retrieve the dashboards for a user as key/labels.
//	 * Useful for autocomplete.
//	 * @param dashboard
//	 * @return ResultSet
//	 ****************************************************************/
	public static LinkedHashMap<String, String> selectUsersForDashboardAsKeyLabel(Integer dashboardID) {
		
		if(dashboardID == null) {
			return new LinkedHashMap<String, String>();
		}
		
		String query = 
				"SELECT U.PK_ID, U.USERNAME, U.FIRSTNAME, U.LASTNAME "  
				+ " FROM "+User.TABLE_NAME+" U " 
				+ " LEFT JOIN "+DashboardEditorsMap.TABLE_NAME+" M ON M.FK_ID_USER = U.PK_ID\r\n"
				+ " WHERE M.FK_ID_DASHBOARD = ? " 
				+ " ORDER BY LOWER(U.USERNAME) "
				;
		
		ArrayList<User> userList =  new CFWSQL(new User())
				.queryCache()
				.custom(query
						, dashboardID)
				.getAsObjectListConvert(User.class);
		
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for(User user : userList) {						
			result.put(user.id()+"", user.createUserLabel());
		}
		
		return result;
	}
	

	
	/***************************************************************
	 * Remove the user from the dashboard if it is assigned to the dashboard, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToDashboard(String userID, String dashboardID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(dashboardID == null ^ !dashboardID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The dashboardID '"+dashboardID+"' is not a number.");
			return false;
		}
		
		return toogleUserAssignedToDashboard(Integer.parseInt(userID), Integer.parseInt(dashboardID));
		
	}
	
	/***************************************************************
	 * Remove the user from the dashboard if it is assigned to the dashboard, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserAssignedToDashboard(int userID, int dashboardID) {
		
		if(checkIsUserAssignedToDashboard(userID, dashboardID)) {
			return removeUserFromDashboard(userID, dashboardID);
		}else {
			return assignUserToDashboard(userID, dashboardID);
		}

	}
		
}

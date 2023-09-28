package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.dashboard.DashboardFavoritesMap.DashboardFavoritenMapFields;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBDashboardFavoriteMap {

	private static final String TABLE_NAME = new DashboardFavoritesMap().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardFavoriteMap.class.getName());
	
	/********************************************************************************************
	 * Adds the dashboard to the specified user.
	 * @param dashboard
	 * @param user
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addDashboardToUserFavs(Dashboard dashboard, User user, boolean isDeletable) {
		
		if(dashboard == null) {
			new CFWLog(logger)
				.warn("Dashboard cannot be null.");
			return false;
		}
		
		if(user == null) {
			new CFWLog(logger)
				.warn("User cannot be null.");
			return false;
		}
		
		if(dashboard.id() < 0 || user.id() < 0) {
			new CFWLog(logger)
				.warn("Dashboard-ID and user-ID are not set correctly.");
			return false;
		}
		
		if(checkIsDashboardFavedByUser(dashboard, user)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboard.name()+"' is already in the favorites of the user '"+user.username()+"'.");
			return false;
		}
		
		String insertDashboardSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + DashboardFavoritenMapFields.FK_ID_DASHBOARD +", "
				  + DashboardFavoritenMapFields.FK_ID_USER
				  + ") VALUES (?,?);";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardFavoritesMap.class, "Add Dashboard to User Favorites: "+user.username()+", Dashboard: "+dashboard.name());
		return CFWDB.preparedExecute(insertDashboardSQL, 
				dashboard.id(),
				user.id()
				);
		
	}
	/********************************************************************************************
	 * Adds the dashboard to the specified user.
	 * @param dashboardID
	 * @param userID
	 * @return return true if dashboard was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addDashboardToUserFavs(int dashboardID, int userID, boolean isDeletable) {
		
		
		if(dashboardID < 0 || userID < 0) {
			new CFWLog(logger)
				.warn("Dashboard-ID or user-ID are not set correctly.");
			return false;
		}
		
		if(checkIsDashboardInUserFavs(dashboardID, userID)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboardID+"' is already part of the user '"+userID+"'.");
			return false;
		}
		
		User user = CFW.DB.Users.selectByID(userID);
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		
		return addDashboardToUserFavs(dashboard, user, isDeletable);
	}
	

	/********************************************************************************************
	 * Adds the dashboard to the specified user.
	 * @param dashboard
	 * @param user
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeDashboardFromUserFavs(Dashboard dashboard, User user) {
		
		if(dashboard == null || user == null ) {
			new CFWLog(logger)
				.warn("Dashboard and user cannot be null.");
			return false;
		}
		
		if(dashboard.id() < 0 || user.id() < 0) {
			new CFWLog(logger)
				.warn("Dashboard-ID and user-ID are not set correctly.");
			return false;
		}
		
		if(!checkIsDashboardFavedByUser(dashboard, user)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboard.name()+"' is not faved the user '"+user.username()+"' and cannot be removed.");
			return false;
		}
		
		String removeDashboardFromUserSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + DashboardFavoritenMapFields.FK_ID_DASHBOARD +" = ? "
				  + " AND "
				  + DashboardFavoritenMapFields.FK_ID_USER +" = ? "
				  + ";";
		
		new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, DashboardFavoritesMap.class, "Remove Dashboard from User Favorites: "+user.username()+", Dashboard: "+dashboard.name());
		
		return CFWDB.preparedExecute(removeDashboardFromUserSQL, 
				dashboard.id(),
				user.id()
				);
	}
	/********************************************************************************************
	 * Remove a dashboard from the user.
	 * @param dashboard
	 * @param user
	 * @return return true if dashboard was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeDashboardFromUserFavs(int dashboardID, int userID) {
		
		if(!checkIsDashboardInUserFavs(dashboardID, userID)) {
			new CFWLog(logger)
				.warn("The dashboard '"+dashboardID+"' is not part of the user '"+ userID+"' and cannot be removed.");
			return false;
		}
				
		User user = CFW.DB.Users.selectByID(userID);
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		return removeDashboardFromUserFavs(dashboard, user);

	}
	
	/****************************************************************
	 * Check if the dashboard is in the given user.
	 * 
	 * @param dashboard to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsDashboardFavedByUser(Dashboard dashboard, User user) {
		
		if(dashboard != null && user != null) {
			return checkIsDashboardInUserFavs(dashboard.id(), user.id());
		}else {
			new CFWLog(logger)
				.severe("The dashboard and user cannot be null. User: '"+dashboard+"', User: '"+user+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the dashboard exists by name.
	 * 
	 * @param dashboard to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsDashboardInUserFavs(int dashboardid, int userid) {
		
		return 0 != new DashboardFavoritesMap()
			.queryCache(CFWDBDashboardFavoriteMap.class, "checkIsDashboardInUser")
			.selectCount()
			.where(DashboardFavoritenMapFields.FK_ID_DASHBOARD.toString(), dashboardid)
			.and(DashboardFavoritenMapFields.FK_ID_USER.toString(), userid)
			.executeCount();

	}

	
	/***************************************************************
	 * Retrieve the dashboards for the specified user.
	 * @param user
	 * @return Hashmap with dashboards(key=user name), or null on exception
	 ****************************************************************/
//	public static ResultSet selectDashboardsForUserResultSet(User user) {
//		
//		if( user == null) {
//			new CFWLog(logger)
//				.severe("The user cannot be null.");
//			return null;
//		}
//		
//		return new CFWSQL(user)
//				.queryCache(CFWDBDashboardFavoriteMap.class, "selectDashboardsForUserResultSet")
//				.custom(
//					"SELECT P.* "
//					+"FROM CFW_PERMISSION P "
//					+"JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_DASHBOARD = P.PK_ID "
//					+"JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_USER = GP.FK_ID_USER "
//					+"WHERE UG.FK_ID_USER = ?;", 
//					user.id())
//				.getResultSet();
//		
//	}
	
	/***************************************************************
	 * Retrieve the dashboard overview for all users.
	 * @param user
	 * @return ResultSet
	 ****************************************************************/
	public static ResultSet getDashboardOverview() {
		
		return new CFWSQL(new Dashboard())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, "sql_dashboardOverviewAllUsers.sql")
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Retrieve the dashboard overview for the specified user.
	 ****************************************************************/
	public static JsonArray getDashboardOverview(User user) {
		
		return new CFWSQL(new Dashboard())
				.queryCache()
				.loadSQLResource(FeatureUserManagement.RESOURCE_PACKAGE, "sql_dashboardOverviewForUser.sql", user.id())
				.getAsJSONArray();
		
	}
	
	
	/***************************************************************
	 * Returns a list of all users and if the user is part of them 
	 * as a json array.
	 * @param user
	 * @return Hashmap with users(key=user name, value=user object), or null on exception
	 ****************************************************************/
//	public static String getDashboardMapForUserAsJSON(String userID) {
//		
//		//----------------------------------
//		// Check input format
//		if(userID == null ^ !userID.matches("\\d+")) {
//			new CFWLog(logger)
//			.severe("The userID '"+userID+"' is not a number.");
//			return "[]";
//		}
//		
//		String sqlString = "SELECT P.PK_ID, P.NAME, P.DESCRIPTION, M.FK_ID_USER AS ITEM_ID, M.IS_DELETABLE FROM "+Dashboard.TABLE_NAME+" P "
//				+ " LEFT JOIN "+CFWDBDashboardFavoriteMap.TABLE_NAME+" M "
//				+ " ON M.FK_ID_DASHBOARD = P.PK_ID"
//				+ " AND M.FK_ID_USER = ?"
//				+ " ORDER BY LOWER(P.NAME)";;
//		
//		ResultSet result = CFWDB.preparedExecuteQuery(sqlString, 
//				userID);
//		
//		String json = ResultSetUtils.toJSON(result);
//		CFWDB.close(result);	
//		return json;
//
//	}
	
	/***************************************************************
	 * Remove the user from the user if it is a member of the user, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleDashboardInUserFavs(String dashboardID, String userID) {
		
		//----------------------------------
		// Check input format
		if(dashboardID == null ^ !dashboardID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+dashboardID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The userID '"+dashboardID+"' is not a number.");
			return false;
		}
		
		return toogleDashboardInUserFavs(Integer.parseInt(dashboardID), Integer.parseInt(userID));
		
	}
	
	/***************************************************************
	 * Remove the user from the user if it is a member of the user, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleDashboardInUserFavs(int dashboardID, int userID) {
		
		if(checkIsDashboardInUserFavs(dashboardID, userID)) {
			return removeDashboardFromUserFavs(dashboardID, userID);
		}else {
			return addDashboardToUserFavs(dashboardID, userID, true);
		}

	}
		
}

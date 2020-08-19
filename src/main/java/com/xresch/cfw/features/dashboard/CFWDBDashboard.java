package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.usermgmt.CFWDBUser;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBDashboard {
	
	private static Class<Dashboard> cfwObjectClass = Dashboard.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBDashboard.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Dashboard dashboard = (Dashboard)object;
			
			if(dashboard == null || dashboard.name().isEmpty()) {
				new CFWLog(logger)
					.method("doCheck")
					.warn("Please specify a name for the dashboard.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			Dashboard dashboard = (Dashboard)object;
			
			if(dashboard != null && dashboard.isDeletable() == false) {
				new CFWLog(logger)
				.method("doCheck")
				.severe("The dashboard '"+dashboard.name()+"' cannot be deleted as it is marked as not deletable.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(Dashboard... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(Dashboard item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(Dashboard item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(Dashboard... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(Dashboard item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, DashboardFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
	
	public static boolean 	deleteMultipleByIDForUser(int userid, String commaSeparatedIDs)	{ 
		return CFWDBDefaultOperations.deleteMultipleByIDWhere(cfwObjectClass, commaSeparatedIDs, DashboardFields.FK_ID_USER, userid); 
	} 
	
	public static boolean 	deleteByName(String name) 		{ 
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, DashboardFields.NAME.toString(), name); 
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static Dashboard selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardFields.PK_ID.toString(), id);
	}
	
	public static Dashboard selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardFields.PK_ID.toString(), id);
	}
	
	public static Dashboard selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardFields.NAME.toString(), name);
	}
	
	/***************************************************************
	 * Select a dashboard by it's ID and return it as JSON string.
	 * @param id of the dashboard
	 * @return Returns a dashboard or null if not found or in case of exception.
	 ****************************************************************/
	public static String getDashboardAsJSON(String id) {
		
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getDashboardAsJSON")
				.select()
				.where(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
				.or(DashboardFields.IS_SHARED.toString(), true)
				.where(DashboardFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user dashboards
	 * 
	 * @return Returns a resultSet with all dashboards or null.
	 ****************************************************************/
	public static ResultSet getUserDashboardList() {
		
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getUserDashboardList")
				.select()
				.where(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
				.orderby(DashboardFields.NAME.toString())
				.getResultSet();
		
	}
	
	/***************************************************************
	 * Return a list of all user dashboards
	 * 
	 * @return Returns a resultSet with all dashboards or null.
	 ****************************************************************/
//	public static ResultSet getSharedDashboardList() {
//		// SELECT (SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER ) AS USERNAME, * FROM CFW_DASHBOARD WHERE IS_SHARED = TRUE ORDER BY LOWER(NAME)
//		return new Dashboard()
//				.queryCache(CFWDBDashboard.class, "getSharedDashboardList")
//				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
//				.select()
//				.where(DashboardFields.IS_SHARED.toString(), true)
//				.orderby(DashboardFields.NAME.toString())
//				.getResultSet();
//		
//	}
	
	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserDashboardListAsJSON() {
		
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getUserDashboardListAsJSON")
				.select()
				.where(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
				.orderby(DashboardFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminDashboardListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getAdminDashboardListAsJSON")
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
				.select()
				.orderby(DashboardFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
			return "[]";
		}
	}
	
	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getSharedDashboardListAsJSON() {
		
		int userID = CFW.Context.Request.getUser().id();
		String sharedUserslikeID = "%\""+userID+"\":%";
		
		//---------------------
		// Shared with User
		CFWSQL query =  new CFWSQL(new Dashboard())
			.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_getSharedDashboardListAsJSON.sql", 
					userID,
					userID,
					sharedUserslikeID,
					sharedUserslikeID);
			
		
		//-------------------------
		// Union with Shared Roles
		query.union()
			.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			.select(DashboardFields.PK_ID, DashboardFields.NAME, DashboardFields.DESCRIPTION)
			.where(DashboardFields.IS_SHARED, true)
			.and().custom("(");
		
		Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(DashboardFields.JSON_SHARE_WITH_ROLES, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		
		//-------------------------
		// Union with Editor Roles
		query.union()
			.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			.select(DashboardFields.PK_ID, DashboardFields.NAME, DashboardFields.DESCRIPTION)
			.where().custom("(");
		
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(DashboardFields.JSON_EDITOR_ROLES, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		//System.out.println(query.getStatementString());
	
		return query
			.custom("ORDER BY NAME")
			.getAsJSON();
	}
	
	
	
	
	/***************************************************************
	 * Return a JSON string for export.
	 * 
	 * @return Returns a JSON array string.
	 ****************************************************************/
	public static String getJsonArrayForExport(String dashboardID) {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)) {			
			JsonArray elements = null;
			if(Strings.isNullOrEmpty(dashboardID)) {
				elements = new Dashboard()
						.queryCache(CFWDBDashboard.class, "getJsonArrayForExportAll")
						.select()
						.getAsJSONArray();
			}else {
				elements = new Dashboard()
						.queryCache(CFWDBDashboard.class, "getJsonArrayForExport")
						.select()
						.where(DashboardFields.PK_ID, dashboardID)
						.getAsJSONArray();
			}
									 
			for(JsonElement element : elements) {
				if(element.isJsonObject()) {
					//-------------------------------
					// Get Username
					JsonElement useridElement = element.getAsJsonObject().get(DashboardFields.FK_ID_USER.toString());
					if(!useridElement.isJsonNull() && useridElement.isJsonPrimitive()) {
						String username = CFW.DB.Users.selectUsernameByID(useridElement.getAsInt());
						element.getAsJsonObject().addProperty("username", username);
					}
					//-------------------------------
					// Get Widgets
					JsonElement idElement = element.getAsJsonObject().get(DashboardFields.PK_ID.toString());
					if(!idElement.isJsonNull() && idElement.isJsonPrimitive()) {
						JsonArray widgets = CFW.DB.DashboardWidgets.getJsonArrayForExport(idElement.getAsString());
						element.getAsJsonObject().add("widgets", widgets);
					}
				}
			}
			
			return CFW.JSON.toJSON(elements);
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!") );
			return "[]";
		}
	}
	
	/***************************************************************
	 * Import an jsonArray exported with getJsonArrayForExport().
	 * 
	 * @return Returns a JSON array string.
	 ****************************************************************/
	public static boolean importByJson(String jsonArray, boolean keepOwner) {

		//-----------------------------
		// Resolve JSON Array
		JsonElement element = CFW.JSON.jsonStringToJsonElement(jsonArray);
		JsonArray array = null;
		
		if(element.isJsonArray()) {
			array = element.getAsJsonArray();
		}else if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if(object.has("payload")) {
				array = object.get("payload").getAsJsonArray();
			}else {
				new CFWLog(logger)
					.method("importByJson")
					.severe(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."), new Exception());
				return false;
			}
		}else {
			new CFWLog(logger)
				.method("importByJson")
				.severe(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."), new Exception());
			return false;
		}
		
		//-----------------------------
		// Create Dashboards
		for(JsonElement dashboardElement : array) {
			if(dashboardElement.isJsonObject()) {
				JsonObject dashboardObject = dashboardElement.getAsJsonObject();
				//-----------------------------
				// Map values
				Dashboard dashboard = new Dashboard();
				dashboard.mapJsonFields(dashboardObject);
				
				//-----------------------------
				// Reset Dashboard ID and Owner
				dashboard.id(null);
				
				if(keepOwner && dashboardObject.has("username")) {
					String username = dashboardObject.get("username").getAsString();
					User owner = CFW.DB.Users.selectByUsernameOrMail(username);
					if(owner != null) {
						dashboard.foreignKeyUser(owner.id());
					}else {
						CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
								CFW.L("cfw_dashboard_error_usernotresolved",
									  "The the dashboard owner with name '{1}' could not be resolved. Set the owner to the importing user.",
									  dashboardObject.has("username"))
						);
						dashboard.foreignKeyUser(CFW.Context.Request.getUser().id());
					}
				}else {
					dashboard.foreignKeyUser(CFW.Context.Request.getUser().id());
				}
				
				
				//-----------------------------
				// Resolve Shared Users
				if(dashboard.sharedWithUsers() != null) {
					LinkedHashMap<String, String> resolvedEditors = new LinkedHashMap<String, String>();
					for(String username : dashboard.sharedWithUsers().values()) {
						User user = CFW.DB.Users.selectByUsernameOrMail(username);
						if(user != null) {
							resolvedEditors.put(""+user.id(), user.username());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_usernotfound",
										  "The user '{1}' could not be found.",
										  username)
							);
						}
						
					}
					dashboard.sharedWithUsers(resolvedEditors);
				}
				
				//-----------------------------
				// Resolve Editors
				if(dashboard.editors() != null) {
					LinkedHashMap<String, String> resolvedEditors = new LinkedHashMap<String, String>();
					for(String username : dashboard.editors().values()) {
						User user = CFW.DB.Users.selectByUsernameOrMail(username);
						if(user != null) {
							resolvedEditors.put(""+user.id(), user.username());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_usernotfound",
										  "The user '{1}' could not be found.",
										  username)
							);
						}
					}
					dashboard.editors(resolvedEditors);
				}
				
				//-----------------------------
				// Create Dashboard
				Integer newDashboardID = CFW.DB.Dashboards.createGetPrimaryKey(dashboard);
				if(newDashboardID == null) {
					continue;
				}
				System.out.println("after null");
				//-----------------------------
				// Create Widgets
				if(dashboardObject.has("widgets")) {
					System.out.println("after haswidgets");
					//-----------------------------
					// Check format
					if(!dashboardObject.get("widgets").isJsonArray()) {
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
						continue;
					}
					System.out.println("after format check");
					//-----------------------------
					// Create Widgets
					JsonArray widgetsArray = dashboardObject.get("widgets").getAsJsonArray();
					for(JsonElement widgetElement : widgetsArray) {
						System.out.println("in loop");
						if(widgetElement.isJsonObject()) {
							System.out.println("in isObject");
							JsonObject widgetObject = widgetElement.getAsJsonObject();
							//-----------------------------
							// Map values
							DashboardWidget widget = new DashboardWidget();
							widget.mapJsonFields(widgetObject);
							
							//-----------------------------
							// Reset Dashboard ID and Owner
							widget.foreignKeyDashboard(newDashboardID);
							
							//-----------------------------
							// Create Widget
							if(!CFW.DB.DashboardWidgets.create(widget)) {
								CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error creating imported widget.");
							}
							
						}
					}
				}
				
			}else {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
				continue;
			}
		}
		
		return true;
	}
	
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isDashboardOfCurrentUser(String dashboardID) {
		
		int count = new CFWSQL(new Dashboard())
			.selectCount()
			.where(DashboardFields.PK_ID.toString(), dashboardID)
			.and(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
			.getCount();
		
		return count > 0;
	}
	
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToDashboard(String dashboardID) {
		
		//-----------------------------------
		// Check User is Admin
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		int userID = CFW.Context.Request.getUser().id();
		String likeID = "%\""+userID+"\":%";
		
		int count = new CFWSQL(new Dashboard())
			.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_hasUserAccessToDashboard.sql", 
					dashboardID, 
					userID, 
					likeID,
					likeID)
			.getCount();
		
		if( count > 0) {
			return true;
		}
		
		//-----------------------------------
		// Get Dashboard 
		Dashboard dashboard = (Dashboard)new CFWSQL(new Dashboard())
			.select(DashboardFields.JSON_SHARE_WITH_ROLES, DashboardFields.JSON_EDITOR_ROLES)
			.where(DashboardFields.PK_ID, dashboardID)
			.getFirstObject();
		
		//-----------------------------------
		// Check User has Shared Role
		LinkedHashMap<String, String> sharedRoles = dashboard.shareWithRoles();
		
		if(sharedRoles != null && sharedRoles.size() > 0) {
			for(String roleID : sharedRoles.keySet()) {
				if(CFW.Context.Request.hasRole(Integer.parseInt(roleID)) ) {
					return true;
				}
			}
		}
		
		//-----------------------------------
		// Check User has Editor Role
		LinkedHashMap<String, String> editorRoles = dashboard.editorRoles();
		
		if(editorRoles != null && editorRoles.size() > 0) {
			for(String roleID : editorRoles.keySet()) {
				if(CFW.Context.Request.hasRole(Integer.parseInt(roleID)) ) {
					System.out.println("HAS ROLE!!!!");
					return true;
				}
			}
		}
		
		return false;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static AutocompleteResult autocompleteDashboard(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		
		return new Dashboard()
			.queryCache(CFWDBDashboard.class, "autocompleteDashboard")
			.select(DashboardFields.PK_ID.toString(),
					DashboardFields.NAME.toString())
			.whereLike(DashboardFields.NAME.toString(), "%"+searchValue+"%")
			.limit(maxResults)
			.getAsAutocompleteResult(DashboardFields.PK_ID.toString(), 
					DashboardFields.NAME.toString());
		
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, DashboardFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(Dashboard item) {
		if(item != null) {
			return checkExistsByName(item.name());
		}
		return false;
	}
		
}

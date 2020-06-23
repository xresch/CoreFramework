package com.pengtoolbox.cfw.features.dashboard;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDBDefaultOperations;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.db.PrecheckHandler;
import com.pengtoolbox.cfw.features.api.FeatureAPI;
import com.pengtoolbox.cfw.features.dashboard.Dashboard.DashboardFields;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

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
		
//		SELECT *, (SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER ) AS USERNAME 
//		FROM CFW_DASHBOARD 
//		WHERE ( IS_SHARED = TRUE AND JSON_SHARE_WITH_USERS IS NULL )
//		OR ( IS_SHARED = TRUE AND JSON_SHARE_WITH_USERS LIKE '%"65":%') 
//		OR JSON_EDITORS LIKE '%"65":%'
//		ORDER BY LOWER(NAME);
		int userID = CFW.Context.Request.getUser().id();
		String likeID = "%\""+userID+"\":%";
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getSharedDashboardListAsJSON")
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
				.select()
				.where("("+DashboardFields.IS_SHARED, true)
						.and()
							.custom("(")
								.isNull(DashboardFields.JSON_SHARE_WITH_USERS)
								.or()
								.is(DashboardFields.JSON_SHARE_WITH_USERS, "{}")
								.or()
								.is(DashboardFields.FK_ID_USER, userID)
							.custom(")")
					.custom(")")
				.or("("+DashboardFields.IS_SHARED.toString(), true)
						.and().like(DashboardFields.JSON_SHARE_WITH_USERS, likeID)
					.custom(")")
				.or().like(DashboardFields.JSON_EDITORS, likeID)
				.orderby(DashboardFields.NAME)
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
	
	public static boolean isDashboardOfCurrentUser(String dashboardID) {
		
		int count = new CFWSQL(new Dashboard())
			.selectCount()
			.where(DashboardFields.PK_ID.toString(), dashboardID)
			.and(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
			.getCount();
		
		return count > 0;
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

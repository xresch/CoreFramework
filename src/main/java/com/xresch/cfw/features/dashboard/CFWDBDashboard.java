package com.xresch.cfw.features.dashboard;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.eav.CFWDBEAVStats;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBDashboard {
	
	private static final String EAV_ATTRIBUTE_USERID = "userid";
	private static final String EAV_ATTRIBUTE_DASHBOARDID = "dashboardid";
	private static final String SQL_SUBQUERY_OWNER = "SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_USER";
	private static final String SQL_SUBQUERY_ISFAVED = "(SELECT COUNT(*) FROM CFW_DASHBOARD_FAVORITE_MAP M WHERE M.FK_ID_USER = ? AND M.FK_ID_DASHBOARD = T.PK_ID) > 0";

	private static Class<Dashboard> cfwObjectClass = Dashboard.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboard.class.getName());
	
	private static final String[] auditLogFieldnames = new String[] { 
			DashboardFields.PK_ID.toString()
		  , DashboardFields.NAME.toString()
		};
	
	public static TreeSet<String> cachedTags = null;
	
	//####################################################################################################
	// Precheck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Dashboard dashboard = (Dashboard)object;
			
			if(dashboard == null || dashboard.name().isEmpty()) {
				new CFWLog(logger)
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
				.severe("The dashboard '"+dashboard.name()+"' cannot be deleted as it is marked as not deletable.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static Integer createGetPrimaryKey(Dashboard item) { 
		updateTags(item); 
		return CFWDBDefaultOperations.createGetPrimaryKeyWithout(prechecksCreateUpdate, auditLogFieldnames, item);
	}
	
	/**********************************************************************************
	 * 
	 * @param dashboardID¨the id of the dashboard that should be duplicated.
	 * @param forVersioning true if this duplicate should be for versioning
	 * @return
	 **********************************************************************************/
	public static Integer createDuplicate(String dashboardID, boolean forVersioning) { 

		Dashboard duplicate = CFW.DB.Dashboards.selectByID(dashboardID);
		duplicate.updateSelectorFields();
		
		//---------------------------------
		// Make sure it has a version group 
		if(Strings.isNullOrEmpty(duplicate.versionGroup()) ) {
			duplicate.versionGroup(UUID.randomUUID().toString());
			duplicate.update(DashboardFields.VERSION_GROUP);
		}
		
		int originalID = duplicate.id();
		
		duplicate.id(null);
		duplicate.timeCreated( new Timestamp(new Date().getTime()) );
		duplicate.foreignKeyOwner(CFW.Context.Request.getUser().id());
		
		if(forVersioning) {
			
			int maxVersion = getMaxVersionForDashboard(originalID);
			duplicate.version(maxVersion+1);
			
		}else {
			duplicate.name(duplicate.name()+"(Copy)");
			duplicate.version(0);
			duplicate.versionGroup(UUID.randomUUID().toString());
			duplicate.isShared(false);
		}
		
		CFW.DB.transactionStart();
		
		Integer newID = duplicate.insertGetPrimaryKey();
		
		if(newID != null) {
			
				duplicate.id(newID);
				//-----------------------------------------
				// Save Selector Fields
				//-----------------------------------------
				boolean success = true;
				success &= duplicate.saveSelectorFields();
				if(!success) {
					CFW.DB.transactionRollback();
					new CFWLog(logger).severe("Error while saving selector fields for duplicate.");
					return null;
				}
				//-----------------------------------------
				// Duplicate Widgets
				//-----------------------------------------
				ArrayList<DashboardWidget> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
				
				for(DashboardWidget widgetToCopy : widgetList) {
					widgetToCopy.id(null);
					widgetToCopy.foreignKeyDashboard(newID);
					
					if(!widgetToCopy.insert()) {
						CFW.DB.transactionRollback();
						new CFWLog(logger).severe("Error while duplicating widget.");
						return null;
					}
				}
				
				//-----------------------------------------
				// Duplicate Parameters
				//-----------------------------------------
				ArrayList<CFWParameter> parameterList = CFW.DB.Parameters.getParametersForDashboard(dashboardID);
				
				for(CFWParameter paramToCopy : parameterList) {
					
					paramToCopy.id(null);
					paramToCopy.foreignKeyDashboard(newID);
					
					if(!paramToCopy.insert()) {
						CFW.DB.transactionRollback();
						new CFWLog(logger).severe("Error while duplicating dashboard parameter.");
						return null;
					}

				}
				
			CFW.DB.transactionCommit();
			
			CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Dashboard duplicated successfully.");
		}
			
		
		
		return newID;

	}

	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean update(Dashboard item) { 
		updateTags(item); 
		item.lastUpdated(new Timestamp(System.currentTimeMillis()));
		return CFWDBDefaultOperations.updateWithout(prechecksCreateUpdate, auditLogFieldnames, item); 
	}
	
	public static boolean updateLastUpdated(String dashboardID){ 
		return updateLastUpdated(Integer.parseInt(dashboardID));
	}
	
	public static boolean updateLastUpdated(int dashboardID){ 
		Dashboard toUpdate = new Dashboard().id(dashboardID).lastUpdated(new Timestamp(System.currentTimeMillis()));
		
		return new CFWSQL(toUpdate)
			.update(DashboardFields.LAST_UPDATED)
			;
		
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(String id) {
		
		boolean success = true;
		CFW.DB.transactionStart();
		
			// delete widgets and related jobs first to not have jobs unrelated to widgets.
			success &= CFW.DB.DashboardWidgets.deleteWidgetsForDashboard(id); 
			success &= CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, DashboardFields.PK_ID.toString(), id); 
		
		CFW.DB.transactionEnd(success);

		return success;
	}

	
	
	public static boolean deleteByIDForCurrentUser(String id)	{ 
		
		if(isDashboardOfCurrentUser(id)) {
			return deleteByID(id);
		}else {
			CFW.Messages.noPermission();
			return false;
		}
	} 
	
	
	
	private static boolean deleteJobsForDashboard(String id)	{ 
		
		ArrayList<DashboardWidget> widgets = CFW.DB.DashboardWidgets.getWidgetsForDashboard(id);
		
		
			boolean success = true;
			for(DashboardWidget widget : widgets) {
				success &= CFW.DB.DashboardWidgets.deleteJobsForWidget(widget.id());
			}
		
		return success;
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
	 * Return a list of all user dashboards
	 * 
	 * @return Returns a resultSet with all dashboards or null.
	 ****************************************************************/
	public static ResultSet getUserDashboardList() {
		
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getUserDashboardList")
				.select()
				.where(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
				.and(DashboardFields.VERSION, 0)
				.orderby(DashboardFields.NAME.toString())
				.getResultSet();
		
	}
	

	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserDashboardListAsJSON() {
		
		return new Dashboard()
				.queryCache(CFWDBDashboard.class, "getUserDashboardListAsJSON")
				.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, CFW.Context.Request.getUserID())
				.select()
				.where(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
				.and(DashboardFields.VERSION, 0)
				.orderby(DashboardFields.NAME.toString())
				.getAsJSON();
	}
	
	
	/***************************************************************
	 * Return a list of all dashboards the user has faved.
	 * 
	 * @return ArrayList<Dashboard>
	 ****************************************************************/
	public static ArrayList<Dashboard> getFavedDashboardList() {
		
		return new CFWSQL(new Dashboard())
				.queryCache()
				.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_getFavedDashboardListAsJSON.sql", 
						CFW.Context.Request.getUserID()
					)
				.getAsObjectListConvert(Dashboard.class);
	}
	/***************************************************************
	 * Return a list of all dashboards the user has faved.
	 * 
	 * @return json string
	 ****************************************************************/
	public static String getFavedDashboardListAsJSON() {
		
		return new CFWSQL(new Dashboard())
				.queryCache()
				.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_getFavedDashboardListAsJSON.sql", 
						CFW.Context.Request.getUserID()
					)
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
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, CFW.Context.Request.getUserID())
				.select()
				.where(DashboardFields.VERSION, 0)
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
					userID,
					sharedUserslikeID,
					sharedUserslikeID);
			
		
		//-------------------------
		// Union with Shared Groups
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, userID)
			.select(DashboardFields.PK_ID
				  , DashboardFields.NAME
				  , DashboardFields.DESCRIPTION
				  , DashboardFields.TAGS
				  , DashboardFields.IS_PUBLIC
				  , DashboardFields.ALLOW_EDIT_SETTINGS
				  )
			.where(DashboardFields.IS_SHARED, true)
			.and(DashboardFields.VERSION, 0)
			.and().custom("(");
		
		Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(DashboardFields.JSON_SHARE_WITH_GROUPS, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		
		//-------------------------
		// Union with Editor Roles
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, userID)
			.select(DashboardFields.PK_ID
					, DashboardFields.NAME
					, DashboardFields.DESCRIPTION
					, DashboardFields.TAGS
					, DashboardFields.IS_PUBLIC
					, DashboardFields.ALLOW_EDIT_SETTINGS
					)
			.where(DashboardFields.VERSION, 0)
			.and().custom("(");
		
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(DashboardFields.JSON_EDITOR_GROUPS, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
	
		//-------------------------
		// Grab Results
		
		// IMPORTANT: Do not change to CFWObjects as you will lose the fields OWNER and IS_FAVED
		JsonArray sharedBoards = query
			.custom("ORDER BY NAME")
			.getAsJSONArray();
		
		//-------------------------
		// Add IS_EDITOR
		
		// TODO a bit of a hack, need to be done with SQL after database structure change
		for(JsonElement boardElement : sharedBoards) {
			JsonObject board = boardElement.getAsJsonObject();
			board.addProperty("IS_EDITOR"
					, checkCanEdit(board.get(DashboardFields.PK_ID.toString()).getAsInt())
				);
			
		}
	
		//-------------------------
		// Return
		return CFW.JSON.toJSON(sharedBoards);
	}
	
	
	/***************************************************************
	 * Return a list of all version of a dashboard as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getDashboardVersionsListAsJSON(String dashboardID) {
		
		Dashboard dashboard = selectByID(dashboardID);
		
		return new CFWSQL(dashboard)
				.queryCache()
				.select()
				.where(DashboardFields.PK_ID, dashboardID)
				.or(DashboardFields.VERSION_GROUP, dashboard.versionGroup())
				.orderbyDesc(DashboardFields.VERSION)
				.getAsJSON();
	}
	
	
	/***************************************************************
	 * Return a JSON string for export.
	 * 
	 * @return Returns a JSON array string.
	 ****************************************************************/
	public static String getJsonArrayForExport(String dashboardID) {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)
		|| CFW.DB.Dashboards.checkCanEdit(dashboardID)) {			
			JsonArray dashboardArray = null;
			if(Strings.isNullOrEmpty(dashboardID)) {
				dashboardArray = new Dashboard()
						.queryCache(CFWDBDashboard.class, "getJsonArrayForExportAll")
						.select()
						.getObjectsAsJSONArray();
			}else {
				dashboardArray = new Dashboard()
						.queryCache(CFWDBDashboard.class, "getJsonArrayForExport")
						.select()
						.where(DashboardFields.PK_ID, dashboardID)
						.getObjectsAsJSONArray();
			}
			
			//-------------------------------
			// For Every Dashboard
			for(JsonElement element : dashboardArray) {
				if(element.isJsonObject()) {
					//-------------------------------
					// Get Username
					JsonElement useridElement = element.getAsJsonObject().get(DashboardFields.FK_ID_USER.toString());
					if(!useridElement.isJsonNull() && useridElement.isJsonPrimitive()) {
						String username = CFW.DB.Users.selectUsernameByID(useridElement.getAsInt());
						element.getAsJsonObject().addProperty("username", username);
					}
					//-------------------------------
					// Get Widgets & Parameters
					JsonElement idElement = element.getAsJsonObject().get(DashboardFields.PK_ID.toString());
					if(!idElement.isJsonNull() && idElement.isJsonPrimitive()) {
						JsonArray widgets = CFW.DB.DashboardWidgets.getJsonArrayForExport(idElement.getAsString());
						element.getAsJsonObject().add("widgets", widgets);
						
						JsonArray parameters = CFW.DB.Parameters.getJsonArrayForExport(idElement.getAsString());
						element.getAsJsonObject().add("parameters", parameters);
					}
					
				}
			}
			
			return CFW.JSON.toJSONPretty(dashboardArray);
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
		JsonElement element = CFW.JSON.stringToJsonElement(jsonArray);
		JsonArray array = null;
		
		if(element.isJsonArray()) {
			array = element.getAsJsonArray();
		}else if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if(object.has("payload")) {
				array = object.get("payload").getAsJsonArray();
			}else {
				new CFWLog(logger)
					.severe(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."), new Exception());
				return false;
			}
		}else {
			new CFWLog(logger)
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
				dashboard.mapJsonFields(dashboardObject, true, true);
				dashboard.id(null);
				dashboard.timeCreated( new Timestamp(new Date().getTime()) );
				
				String importedName = dashboard.name() +"(Imported)";
				dashboard.name(importedName);
				
				//-----------------------------
				// Reset Dashboard ID and Owner
				dashboard.id(null);
				
				if(keepOwner && dashboardObject.has("username")) {
					String username = dashboardObject.get("username").getAsString();
					User owner = CFW.DB.Users.selectByUsernameOrMail(username);
					if(owner != null) {
						dashboard.foreignKeyOwner(owner.id());
					}else {
						CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
								CFW.L("cfw_dashboard_error_usernotresolved",
									  "The the dashboard owner with name '{0}' could not be resolved. Set the owner to the importing user.",
									  dashboardObject.has("username"))
						);
						dashboard.foreignKeyOwner(CFW.Context.Request.getUser().id());
					}
				}else {
					dashboard.foreignKeyOwner(CFW.Context.Request.getUser().id());
				}
				
				//-----------------------------
				// Resolve Shared Users
				if(dashboard.sharedWithUsers() != null) {
					LinkedHashMap<String, String> resolvedViewers = new LinkedHashMap<String, String>();
					for(String id : dashboard.sharedWithUsers().keySet()) {
						User user = CFW.DB.Users.selectByID(Integer.parseInt(id));
						if(user != null) {
							
							resolvedViewers.put(""+user.id(), user.createUserLabel());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_usernotfound",
										  "The user '{0}' could not be found.",
										  dashboard.sharedWithUsers().get(id))
							);
						}
						
					}
					dashboard.sharedWithUsers(resolvedViewers);
				}
				
				//-----------------------------
				// Resolve Editors
				if(dashboard.editors() != null) {
					LinkedHashMap<String, String> resolvedEditors = new LinkedHashMap<String, String>();
					for(String id : dashboard.editors().keySet()) {
						User user = CFW.DB.Users.selectByID(Integer.parseInt(id));
						if(user != null) {
							resolvedEditors.put(""+user.id(), user.username());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_usernotfound",
										  "The  user '{0}' could not be found.",
										  dashboard.editors().get(id))
							);
						}
					}
					dashboard.editors(resolvedEditors);
				}
				
				//-----------------------------
				// Resolve Shared Roles
				if(dashboard.sharedWithGroups() != null) {
					LinkedHashMap<String, String> resolvedSharedRoles = new LinkedHashMap<String, String>();
					for(String id : dashboard.sharedWithGroups().keySet()) {
						Role role = CFW.DB.Roles.selectByID(Integer.parseInt(id));
						if(role != null) {
							resolvedSharedRoles.put(""+role.id(), role.name());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_rolenotfound",
										  "The  role '{0}' could not be found.",
										  dashboard.sharedWithGroups().get(id))
							);
						}
					}
					dashboard.sharedWithGroups(resolvedSharedRoles);
				}
				
				//-----------------------------
				// Resolve Editor Roles
				if(dashboard.editorGroups() != null) {
					LinkedHashMap<String, String> resolvedEditorRoles = new LinkedHashMap<String, String>();
					for(String id : dashboard.editorGroups().keySet()) {
						Role role = CFW.DB.Roles.selectByID(Integer.parseInt(id));
						if(role != null) {
							resolvedEditorRoles.put(""+role.id(), role.name());
						}else {
							CFW.Context.Request.addAlertMessage(MessageType.WARNING, 
									CFW.L("cfw_core_error_rolenotfound",
										  "The  role '{0}' could not be found.",
										  dashboard.editorGroups().get(id))
							);
						}
					}
					dashboard.editorGroups(resolvedEditorRoles);
				}
				
				//-----------------------------
				// Create Dashboard
				Integer newDashboardID = CFW.DB.Dashboards.createGetPrimaryKey(dashboard);
				if(newDashboardID == null) {
					new CFWLog(logger)
						.severe("Dashboard '"+dashboard.name()+"' could not be imported.");
					continue;
				}
				
				dashboard.saveSelectorFields();
				
				//-----------------------------
				// Create Parameters
				HashMap<Integer, Integer> oldNewParamIDs = new HashMap<>();
				if(dashboardObject.has("parameters")) {
					
					//-----------------------------
					// Check format
					if(!dashboardObject.get("parameters").isJsonArray()) {
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
						continue;
					}
					
					//-----------------------------
					// Create Parameters
					JsonArray paramsArray = dashboardObject.get("parameters").getAsJsonArray();
					for(JsonElement paramsElement : paramsArray) {
						
						if(paramsElement.isJsonObject()) {
							
							JsonObject paramsObject = paramsElement.getAsJsonObject();
							//-----------------------------
							// Map values
							CFWParameter param = new CFWParameter();
							param.mapJsonFields(paramsObject, true, true);
							
							//-----------------------------
							// Reset IDs
							Integer oldID = param.getPrimaryKeyValue();
							param.id(null);
							param.foreignKeyDashboard(newDashboardID);
							
							//-----------------------------
							// Create Parameter
							
							Integer newID = CFW.DB.Parameters.createGetPrimaryKey(param);
							if(newID == null) {
								CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error creating imported parameter.");
								continue;
							}
							
							oldNewParamIDs.put(oldID, newID);
							
						}
					}
				}
				
				//-----------------------------
				// Create Widgets
				if(dashboardObject.has("widgets")) {
					
					//-----------------------------
					// Check format
					if(!dashboardObject.get("widgets").isJsonArray()) {
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
						continue;
					}
					
					//-----------------------------
					// Create Widgets
					JsonArray widgetsArray = dashboardObject.get("widgets").getAsJsonArray();
					for(JsonElement widgetElement : widgetsArray) {
						
						if(widgetElement.isJsonObject()) {
							
							JsonObject widgetObject = widgetElement.getAsJsonObject();
							//-----------------------------
							// Map values
							DashboardWidget widget = new DashboardWidget();
							widget.mapJsonFields(widgetObject, true, true);
							
							//-----------------------------
							// Reset Dashboard ID and Owner
							widget.id(null);
							widget.foreignKeyDashboard(newDashboardID);
							
							//-----------------------------
							// Reset Parameter IDs of
							// Parameter Widgets
							if(widget.type().equals(WidgetParameter.WIDGET_TYPE)) {
								JsonObject settings = CFW.JSON.fromJson(widget.settings()).getAsJsonObject();
								
								if(!settings.isJsonNull() 
								&& settings.has("JSON_PARAMETERS")
								&& settings.get("JSON_PARAMETERS").isJsonObject()) {
									JsonObject paramsForWidget = settings.get("JSON_PARAMETERS").getAsJsonObject();
									for(Entry<Integer, Integer> entry : oldNewParamIDs.entrySet()) {
										String oldID = entry.getKey()+"";
										if(paramsForWidget.has(oldID)) {
											JsonElement value = paramsForWidget.get(oldID);
											paramsForWidget.remove(oldID);
											
											String newID = entry.getValue()+"";
											paramsForWidget.add(newID, value);
											
										}
									}
								}
							}
							
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
		return isDashboardOfCurrentUser(Integer.parseInt(dashboardID));
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isDashboardOfCurrentUser(int dashboardID) {
		
		int count = new CFWSQL(new Dashboard())
			.selectCount()
			.where(DashboardFields.PK_ID.toString(), dashboardID)
			.and(DashboardFields.FK_ID_USER.toString(), CFW.Context.Request.getUser().id())
			.executeCount();
		
		return count > 0;
	}
	
	/***************************************************************
	 * 
	 * @param isPublicServlet set to true if request is coming true
	 * the public servlet ServletDashboardViewPublic, else set false.
	 ***************************************************************/
	public static boolean hasUserAccessToDashboard(int dashboardID, boolean isPublicServlet) {
		return hasUserAccessToDashboard(dashboardID+"", isPublicServlet);
	}
	
	/***************************************************************
	 * 
	 * @param isPublicServlet set to true if request is coming true
	 * the public servlet ServletDashboardViewPublic, else set false.
	 ***************************************************************/
	public static boolean hasUserAccessToDashboard(String dashboardID, boolean isPublicServlet) {

		if(!isPublicServlet) {
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
				.executeCount();
			
			if( count > 0) {
				return true;
			}
		}
		
		//-----------------------------------
		// Get Dashboard 
		Dashboard dashboard = (Dashboard)new CFWSQL(new Dashboard())
			.select(DashboardFields.JSON_SHARE_WITH_GROUPS, DashboardFields.JSON_EDITOR_GROUPS, DashboardFields.IS_PUBLIC)
			.where(DashboardFields.PK_ID, dashboardID)
			.getFirstAsObject();
		
		//-----------------------------------
		// Handle Public Dashboards
		if(isPublicServlet) {
			if(dashboard.isPublic() == true) {
				return true;
			}else {
				return false;
			}
		}
		
		//-----------------------------------
		// Check User has Shared Role
		LinkedHashMap<String, String> sharedRoles = dashboard.sharedWithGroups();
		
		if(sharedRoles != null && sharedRoles.size() > 0) {
			for(String roleID : sharedRoles.keySet()) {
				if(CFW.Context.Request.hasRole(Integer.parseInt(roleID)) ) {
					return true;
				}
			}
		}
		
		//-----------------------------------
		// Check User has Editor Role
		LinkedHashMap<String, String> editorRoles = dashboard.editorGroups();
		
		if(editorRoles != null && editorRoles.size() > 0) {
			for(String roleID : editorRoles.keySet()) {
				if(CFW.Context.Request.hasRole(Integer.parseInt(roleID)) ) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static JsonArray permissionAuditByUser(User user) {
		
		//-----------------------------------
		// Check User is Admin
		HashMap<String, Permission> permissions = CFW.DB.Permissions.selectPermissionsForUser(user);
		
		if( permissions.containsKey(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN) ) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Dashboard Administrator and has access to every dashboard.");
			JsonArray adminResult = new JsonArray(); 
			adminResult.add(adminObject);
			return adminResult;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		String likeID = "%\""+user.id()+"\":%";
		
		return new CFWSQL(new Dashboard())
			.queryCache()
			.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_permissionAuditByUser.sql", 
					user.id(), 
					likeID,
					likeID)
			.getAsJSONArray();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static JsonArray permissionAuditByUsersGroups(User user) {
		
		//-----------------------------------
		// Check User is Shared/Editor
		
		return new CFWSQL(new Dashboard())
				.queryCache()
				.loadSQLResource(FeatureDashboard.PACKAGE_RESOURCES, "SQL_permissionAuditByUsersGroups.sql", 
						user.id())
				.getAsJSONArray();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static AutocompleteResult autocompleteDashboard(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		
		ResultSet resultSet = new Dashboard()
			.queryCache(CFWDBDashboard.class, "autocompleteDashboard")
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(DashboardFields.PK_ID,
					DashboardFields.NAME)
			.whereLike(DashboardFields.NAME, "%"+searchValue+"%")
			.limit(maxResults)
			.getResultSet();
		
		//------------------------------------
		// Filter by Access
		AutocompleteList list = new AutocompleteList();
		try {
			while(resultSet != null && resultSet.next()) {
				int id = resultSet.getInt("PK_ID");
				if(hasUserAccessToDashboard(id, false)) {
					String name = resultSet.getString("NAME");
					String owner = resultSet.getString("OWNER");
					list.addItem(id, name, "Owner: "+owner);
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error while autocomplete dashboards.", new Throwable());
		} finally {
			CFWDB.close(resultSet);
		}

		
		return new AutocompleteResult(list);
		
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
	
	/*****************************************************************
	 * Checks if the current user can edit the dashboard.
	 *****************************************************************/
	public static boolean checkCanEdit(String dashboardID) {
		return checkCanEdit(Integer.parseInt(dashboardID));
	}
	/*****************************************************************
	 * Checks if the current user can edit the dashboard.
	 *****************************************************************/
	public static boolean checkCanEdit(int dashboardID) {
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		return checkCanEdit(dashboard);
	}
	
	/*****************************************************************
	 * Checks if the current user can edit the dashboard.
	 *****************************************************************/
	public static boolean checkCanEdit(Dashboard dashboard) {
		User user = CFW.Context.Request.getUser();
		
		//--------------------------------------
		// if user is not logged in / public dashboards
		if(user == null) { return false; }
		
		//--------------------------------------
		// Check User is Dashboard owner, admin
		// or listed in editors
		if( dashboard.foreignKeyOwner().equals(user.id())
		|| ( dashboard.editors() != null && dashboard.editors().containsKey(user.id().toString()) )
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}
		
		//--------------------------------------
		// Check User has Editor Role
		if(dashboard.editorGroups() != null) {
			for(int roleID : CFW.Context.Request.getUserRoles().keySet()) {
				if (dashboard.editorGroups().containsKey(""+roleID)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/********************************************************************************************
	 * Switch between dashboard versions
	 * 
	 ********************************************************************************************/
	public static boolean switchToVersion(String dashboardID, String versionID) {
		
		boolean success = true;
		CFW.DB.transactionStart();
		
			//----------------------------
			// Fetch Original Stuff
			Dashboard current = selectByID(dashboardID);
			int originalID = current.id();
			int newVersion = 1 + getMaxVersionForDashboard(originalID);
			
			//----------------------------
			// Fetch Version Stuff
			Dashboard toVersion = selectByID(versionID);
			int toID = toVersion.id();
			
			//----------------------------
			// Switch current
			current.version(newVersion);
			success &= update(current);
			
			toVersion.version(0);
			success &= update(toVersion);
			
			//----------------------------
			// Update Favorites
			success &= CFW.DB.DashboardFavorites.switchFavorites(originalID, toID);
			
			//----------------------------
			// Delete Jobs
			success &= deleteJobsForDashboard(dashboardID);
					
			//----------------------------
			// Success Message
			if(success) {
				CFW.Messages.addSuccessMessage("Version successfully switched.");
			}else {
				CFW.Messages.addErrorMessage("Error occured while switching versions.");
			}
			
		CFW.DB.transactionEnd(success);
		
		return success;
	}

	/********************************************************************************************
	 * Returns the maximum version for the dashboard
	 * 
	 ********************************************************************************************/
	public static int  getMaxVersionForDashboard(int id) {
		
		Dashboard dashboard = selectByID(id);
		
		return new CFWSQL(new Dashboard())
			.custom("SELECT MAX(VERSION) AS MAXVERSION"
					+" FROM "+Dashboard.TABLE_NAME
					+" WHERE PK_ID = ? OR VERSION_GROUP = ?"
					, id
					, dashboard.versionGroup()
					)
			.getFirstAsInteger();
	}
	
	
	/********************************************************************************************
	 * Creates multiple Dashboards in the DB.
	 * @param Dashboards with the values that should be inserted. ID will be set by the Database.
	 * @return 
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static TreeSet<String> getTags() {
		
		if(cachedTags == null) {
			fetchAndCacheTags();
		}
		
		return cachedTags;
	}
	
	/********************************************************************************************
	 * Creates multiple Dashboards in the DB.
	 * @param Dashboards with the values that should be inserted. ID will be set by the Database.
	 * @return 
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static String getTagsAsJSON() {
				
		return CFW.JSON.toJSON(getTags().toArray(new String[] {}));
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified dashboard.
	 * @param Dashboards with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(Dashboard... dashboards) {
		
		for(Dashboard dashboard : dashboards) {
			updateTags(dashboard);
		}
	}
	/********************************************************************************************
	 * Adds the tags to the cache for the specified dashboard.
	 * @param Dashboards with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(Dashboard dashboard) {
		
		if(cachedTags == null) {
			fetchAndCacheTags();
		}
		
		if(dashboard.tags() != null) {
			for(Object tag : dashboard.tags()) {
				cachedTags.add(tag.toString());
			}
		}
	}
	
	/********************************************************************************************
	 * Fetch cachedTags from the database and stores them into the cache.
	 * 
	 ********************************************************************************************/
	public static void fetchAndCacheTags() {
		
		cachedTags = new TreeSet<String>();
		
		ResultSet resultSet = new CFWSQL(new Dashboard())
			.queryCache()
			.select(DashboardFields.TAGS.toString())
			.getResultSet();
		
		try {
			while(resultSet.next()) {
				
				Array tagsArray = resultSet.getArray(1);

				if(tagsArray != null) {
					Object[] objectArray = (Object[])tagsArray.getArray();
					for(int i = 0 ; i < objectArray.length; i++) {
						cachedTags.add(objectArray[i].toString());
					}
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Tags could not be fetched because an error occured.", e);
		} finally {
			CFWDB.close(resultSet);
		}
				
	}
	
	/********************************************************************************************
	 * Fetch cachedTags from the database that are visible to the user.
	 * 
	 ********************************************************************************************/
	public static String getTagsForUserAsJSON(int userID) {
		
		TreeSet<String> tags = new TreeSet<String>();
		
		ResultSet resultSet = new CFWSQL(new Dashboard())
			.queryCache()
			.select(DashboardFields.TAGS.toString())
			.where(DashboardFields.FK_ID_USER.toString(), userID)
			.or(DashboardFields.IS_SHARED.toString(), true)
			.getResultSet();
		
		try {
			while(resultSet.next()) {
				Object[] tagsArray = (Object[])resultSet.getObject(1);
				
				if(tagsArray != null) {
					for(int i = 0 ; i < tagsArray.length; i++) {
						tags.add(tagsArray[i].toString());
					}
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Tags could not be fetched because an error occured.", e);
		} finally {
			CFWDB.close(resultSet);
		}
		
		return CFW.JSON.toJSON(tags.toArray(new String[] {}));
	}

	/*****************************************************************
	 *
	 *****************************************************************/
	static void pushEAVStats(String entityName, String dashboardID, int value) {
		
		Integer userID = CFW.Context.Request.getUserID();
		String userIDString = (userID != null) ? ""+userID : null;
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
		attributes.put(EAV_ATTRIBUTE_DASHBOARDID, dashboardID);
		attributes.put(EAV_ATTRIBUTE_USERID, userIDString);
		
		CFW.DB.EAVStats.pushStatsCounter(FeatureDashboard.EAV_STATS_CATEGORY, entityName, attributes, value);
		
	}
	
	/***************************************************************
	 * Return a list of all user dashboards as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static JsonArray getEAVStats(String boardID, long earliest, long latest) {
		
		String category = FeatureDashboard.EAV_STATS_CATEGORY;
		String entity = "%";
		
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		values.put(EAV_ATTRIBUTE_DASHBOARDID, boardID);
		
		JsonArray array = CFWDBEAVStats.fetchStatsAsJsonArray(category, entity, values, earliest, latest);
		
		return array;
	}
		
}

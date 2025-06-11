package com.xresch.cfw.features.query.store;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.features.eav.CFWDBEAVStats;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterScope;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBStoredQuery {
	
	private static final String EAV_ATTRIBUTE_USERID = "userid";
	private static final String EAV_ATTRIBUTE_STOREDQUERYID = "storedQueryid";
	private static final String SQL_SUBQUERY_OWNER = "SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_OWNER";

	private static Class<CFWStoredQuery> cfwObjectClass = CFWStoredQuery.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredQuery.class.getName());
	
	private static final String[] auditLogFieldnames = new String[] { 
			CFWStoredQueryFields.PK_ID.toString()
		  , CFWStoredQueryFields.NAME.toString()
		};
	
	private static final HashMap<Integer, WidgetStoredQuery> widgetCache = new HashMap<>();
	
	public static TreeSet<String> cachedTags = null;
	
	//####################################################################################################
	// Precheck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWStoredQuery storedQuery = (CFWStoredQuery)object;
			
			if(storedQuery == null) {
				new CFWLog(logger)
					.warn("Stored Query cannot be null.", new Throwable());
				return false;
			}
			
			if(storedQuery.name() == null || storedQuery.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a name for the stored query.", new Throwable());
				return false;
			}
			
			if(!checkCanSaveWithName(storedQuery)) {
				CFW.Messages.addWarningMessage("The name '"+storedQuery.name()+"' is already in use. (Queries of other users and archived queries count too).");
				return false;
			}
			
			storedQuery.updateQueryParams();
			return true;
		}
	};
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			CFWStoredQuery storedQuery = (CFWStoredQuery)object;
			
			if(storedQuery == null) {
				new CFWLog(logger)
				.severe("The stored query ID was null, so nothing to delete.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static Integer createGetPrimaryKey(CFWStoredQuery item) { 
		updateTags(item); 
		
		item.versionGroup(UUID.randomUUID().toString());
		
		Integer primaryKey =  CFWDBDefaultOperations.createGetPrimaryKeyWithout(prechecksCreateUpdate, auditLogFieldnames, item);
		item.id(primaryKey);
		updateWidgetCache(item);
		
		return primaryKey;
		
	}
	
	/**********************************************************************************
	 * 
	 * @param storedQueryID¨the id of the storedQuery that should be duplicated.
	 * @param forVersioning true if this duplicate should be for versioning
	 * @return
	 **********************************************************************************/
	public static Integer createDuplicate(String storedQueryID, boolean forVersioning) { 

		CFWStoredQuery duplicateThis = CFW.DB.StoredQuery.selectByID(storedQueryID);
		return createDuplicate(duplicateThis, forVersioning);
	}
	/**********************************************************************************
	 * 
	 * @param storedQueryID¨the id of the storedQuery that should be duplicated.
	 * @param forVersioning true if this duplicate should be for versioning
	 * @return
	 **********************************************************************************/
	public static Integer createDuplicate(CFWStoredQuery duplicateThis, boolean forVersioning) { 

		duplicateThis.updateSelectorFields();
		
		//---------------------------------
		// Make sure it has a version group 
		if(Strings.isNullOrEmpty(duplicateThis.versionGroup()) ) {
			duplicateThis.versionGroup(UUID.randomUUID().toString());
			duplicateThis.update(DashboardFields.VERSION_GROUP);
		}
		
		int originalID = duplicateThis.id();
		
		duplicateThis.id(null);
		duplicateThis.timeCreated( new Timestamp(new Date().getTime()) );
		
		// need to check if null for automatic versioning
		Integer id =  CFW.Context.Request.getUserID();
		
		if(forVersioning) {
			
			int newVersion = 1 + getMaxVersionForQuery(originalID);
			duplicateThis.version(newVersion);
			duplicateThis.name(duplicateThis.name()+" v"+newVersion);
			
		}else {
			duplicateThis.foreignKeyOwner(id);
			duplicateThis.name(duplicateThis.name()+"(Copy-"+CFW.Random.stringAlphaNum(8)+")");
			duplicateThis.version(0);
			duplicateThis.versionGroup(UUID.randomUUID().toString());
			duplicateThis.isShared(false);
		}
		
		CFW.DB.transactionStart();
		
		Integer newID = duplicateThis.insertGetPrimaryKey();
		
		if(newID != null) {
			
				duplicateThis.id(newID);
				//-----------------------------------------
				// Save Selector Fields
				//-----------------------------------------
				boolean success = true;
				success &= duplicateThis.saveSelectorFields();
				if(!success) {
					CFW.DB.transactionRollback();
					new CFWLog(logger).severe("Error while saving selector fields for duplicate.");
					return null;
				}
				
				//-----------------------------------------
				// Duplicate Parameters
				//-----------------------------------------
				ArrayList<CFWParameter> parameterList = CFW.DB.Parameters.getParametersForQuery(originalID);
				
				for(CFWParameter paramToCopy : parameterList) {
					
					paramToCopy.id(null);
					paramToCopy.foreignKeyQuery(newID);
					
					if(!paramToCopy.insert()) {
						CFW.DB.transactionRollback();
						new CFWLog(logger).severe("Error while duplicating query parameter.");
						return null;
					}

				}

				CFW.DB.transactionCommit();
				
				updateWidgetCache(duplicateThis);
				
				if(forVersioning) {
					int age = CFW.DB.Config.getConfigAsInt(
							FeatureStoredQuery.CONFIG_CATEGORY
							, FeatureStoredQuery.CONFIG_DEFAULT_AUTOVERSION_AGE
							);
					CFW.Messages.addInfoMessage("New version created. (Auto-Version when last update is older than "+age+" minutes)");
				}else {
					CFW.Messages.addSuccessMessage("Stored query duplicated successfully.");
				}
		}else {
			CFW.DB.transactionRollback();
		}
			
		
		
		return newID;

	}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean update(CFWStoredQuery item) { 
		return updateWithout(item, new String[] {});
	}
	
	public static boolean updateWithout(CFWStoredQuery item, String... fieldsToIgnore) { 
		updateTags(item); 
		updateWidgetCache(item);
		item.lastUpdated(new Timestamp(System.currentTimeMillis()));
		return CFWDBDefaultOperations.updateWithout(prechecksCreateUpdate, auditLogFieldnames, item, fieldsToIgnore); 
	}
	
	public static boolean updateLastUpdated(String storedQueryID){ 
		return updateLastUpdated(Integer.parseInt(storedQueryID));
	}
	
	public static boolean updateLastUpdated(int storedQueryID){ 
		CFWStoredQuery toUpdate = new CFWStoredQuery().id(storedQueryID)
				.lastUpdated(new Timestamp(System.currentTimeMillis()));
		
		return new CFWSQL(toUpdate)
			.update(CFWStoredQueryFields.LAST_UPDATED)
			;
		
	}
	
	public static boolean updateIsArchived(String storedQueryID, boolean isArchived){ 
		return updateIsArchived(Integer.parseInt(storedQueryID), isArchived);
	}
	
	public static boolean updateIsArchived(int storedQueryID, boolean isArchived){ 
		CFWStoredQuery toUpdate = selectByID(storedQueryID);
		
		toUpdate.isArchived(isArchived);
		
		String auditMessage = ( 
							(isArchived) ? 
							"Moving stored query to archive:"
							: "Extracting stored query from archive:"
							)
							+" ID="+storedQueryID+", NAME="+toUpdate.name()
							;
		new CFWLog(logger).audit(CFWAuditLogAction.MOVE, CFWStoredQuery.class, auditMessage);
		
		
		boolean updateSuccess = new CFWSQL(toUpdate).update(CFWStoredQueryFields.IS_ARCHIVED);
		
		updateWidgetCache(toUpdate);
		
		return updateSuccess;
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(String id) {
		removeWidgetCache(Integer.parseInt(id));
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, CFWStoredQueryFields.PK_ID.toString(), id); 

	}

	
	
	public static boolean deleteByIDForCurrentUser(String id)	{ 
		
		if(isStoredQueryOfCurrentUser(id)) {
			return deleteByID(id);
		}else {
			CFW.Messages.noPermission();
			return false;
		}
	} 
	
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWStoredQuery selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWStoredQueryFields.PK_ID.toString(), id);
	}
	
	public static CFWStoredQuery selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWStoredQueryFields.PK_ID.toString(), id);
	}
	
	public static CFWStoredQuery selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWStoredQueryFields.NAME.toString(), name);
	}
	
	
	/***************************************************************
	 * Return a list of all user storedQuery
	 * 
	 * @return Returns a resultSet with all storedQuery or null.
	 ****************************************************************/
	public static ResultSet getUserStoredQueryList() {
		
		return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.select()
				.where(CFWStoredQueryFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWStoredQueryFields.VERSION, 0)
				.and(CFWStoredQueryFields.IS_ARCHIVED, false)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getResultSet();
		
	}
	

	/***************************************************************
	 * Return a list of all user storedQuery as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserStoredQueryListAsJSON() {
		
		return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.select()
				.where(CFWStoredQueryFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWStoredQueryFields.VERSION, 0)
				.and(CFWStoredQueryFields.IS_ARCHIVED, false)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getAsJSON();
	}
	
	
	/***************************************************************
	 * Return a list of all personal and shared queries the 
	 * user can access.
	 * 
	 * @return Returns a resultSet with all storedQuery or null.
	 ****************************************************************/
	public static JsonArray getUserAndSharedStoredQueryList() {
		
		JsonArray userQueries = new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.select()
				.where(CFWStoredQueryFields.FK_ID_OWNER, CFW.Context.Request.getUser().id())
					.and(CFWStoredQueryFields.VERSION, 0)
					.and(CFWStoredQueryFields.IS_ARCHIVED, false)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getAsJSONArray();
		
		JsonArray sharedQueries = getSharedStoredQueryListAsJSONArray();
		
		userQueries.addAll(sharedQueries);
		
		return userQueries;
		
	}
	
	/***************************************************************
	 * Return a list of all user storedQuery as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserArchivedListAsJSON() {
		
		return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.select()
				.where(CFWStoredQueryFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
					.and(CFWStoredQueryFields.VERSION, 0)
					.and(CFWStoredQueryFields.IS_ARCHIVED, true)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getAsJSON();
	}
		

	/***************************************************************
	 * Return a list of all user storedQuery as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminStoredQueryListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.select()
				.where(CFWStoredQueryFields.IS_ARCHIVED, false)
					.and(CFWStoredQueryFields.VERSION, 0)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	/***************************************************************
	 * Return a list of all archived storedQuery as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminArchivedListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.select()
				.where(CFWStoredQueryFields.IS_ARCHIVED, true)
					.and(CFWStoredQueryFields.VERSION, 0)
				.orderby(CFWStoredQueryFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	/***************************************************************
	 * Return a list of all stored queries the user has access to
	 * as json string.
	 * 
	 * @return Returns a result set with stored queries
	 ****************************************************************/
	public static String getSharedStoredQueryListAsJSON() {
		
		return CFW.JSON.toJSON(
				getSharedStoredQueryListAsJSONArray()
			);
	}
	
	/***************************************************************
	 * Return a list of all stored queries the user has access to.
	 * 
	 * @return Returns a result set with stored queries.
	 ****************************************************************/
	public static JsonArray getSharedStoredQueryListAsJSONArray() {
		
		int userID = CFW.Context.Request.getUser().id();
		String sharedUserslikeID = "%\""+userID+"\":%";
		
		//---------------------
		// Shared with User
		CFWSQL query =  new CFWSQL(new CFWStoredQuery())
			.loadSQLResource(FeatureStoredQuery.PACKAGE_RESOURCES, "SQL_getSharedStoredQueryListAsJSON.sql", 
					userID,
					userID,
					sharedUserslikeID,
					sharedUserslikeID);
			
		
		//-------------------------
		// Union with Shared Groups
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWStoredQueryFields.PK_ID
				  , CFWStoredQueryFields.NAME
				  , CFWStoredQueryFields.QUERY
				  , CFWStoredQueryFields.QUERY_PARAMS_DEFINED
				  , CFWStoredQueryFields.QUERY_PARAMS
				  , CFWStoredQueryFields.CHECK_PERMISSIONS
				  , CFWStoredQueryFields.DESCRIPTION
				  , CFWStoredQueryFields.TAGS
				  )
			.where(CFWStoredQueryFields.IS_SHARED, true)
				.and(CFWStoredQueryFields.VERSION, 0)
				.and(CFWStoredQueryFields.IS_ARCHIVED, false)
				.and().custom("(");
		
		Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWStoredQueryFields.JSON_SHARE_WITH_GROUPS, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		
		//-------------------------
		// Union with Editor Roles
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWStoredQueryFields.PK_ID
					, CFWStoredQueryFields.NAME
					, CFWStoredQueryFields.QUERY
					, CFWStoredQueryFields.QUERY_PARAMS_DEFINED
					, CFWStoredQueryFields.QUERY_PARAMS
					, CFWStoredQueryFields.CHECK_PERMISSIONS
					, CFWStoredQueryFields.DESCRIPTION
					, CFWStoredQueryFields.TAGS
					)
			.where(CFWStoredQueryFields.IS_ARCHIVED, false)
				.and(CFWStoredQueryFields.VERSION, 0)
				.and().custom("(");
		
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWStoredQueryFields.JSON_EDITOR_GROUPS, "%\""+roleID+"\":%");
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
					, checkCanEdit(board.get(CFWStoredQueryFields.PK_ID.toString()).getAsInt())
				);
			
		}
	
		//-------------------------
		// Return
		return sharedBoards;
	}
	
				
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isStoredQueryOfCurrentUser(String storedQueryID) {
		return isStoredQueryOfCurrentUser(Integer.parseInt(storedQueryID));
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isStoredQueryOfCurrentUser(int storedQueryID) {
		
		int count = new CFWSQL(new CFWStoredQuery())
			.selectCount()
			.where(CFWStoredQueryFields.PK_ID.toString(), storedQueryID)
			.and(CFWStoredQueryFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
			.executeCount();
		
		return count > 0;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToStoredQuery(int storedQueryID) {
		return  hasUserAccessToStoredQuery(""+storedQueryID);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToStoredQuery(String storedQueryID) {

		// -----------------------------------
		// Check User is Admin
		if (CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			return true;
		}

		// -----------------------------------
		// Check User is Shared/Editor

		int userID = CFW.Context.Request.getUser().id();
		String likeID = "%\"" + userID + "\":%";

		int count = new CFWSQL(new CFWStoredQuery())
				.loadSQLResource(FeatureStoredQuery.PACKAGE_RESOURCES
						,"SQL_hasUserAccessToStoredQuery.sql"
						, storedQueryID
						, userID
						, likeID
						, likeID)
				.executeCount();

		if (count > 0) {
			return true;
		}

		//-----------------------------------
		// Get StoredQuery
		CFWStoredQuery storedQuery = (CFWStoredQuery) new CFWSQL(new CFWStoredQuery())
				.select(CFWStoredQueryFields.IS_SHARED
						, CFWStoredQueryFields.JSON_SHARE_WITH_GROUPS
						, CFWStoredQueryFields.JSON_EDITOR_GROUPS
					)
				.where(CFWStoredQueryFields.PK_ID, storedQueryID).getFirstAsObject();

		//-----------------------------------
		// Check User has Shared Role
		if(storedQuery.isShared()) {
			LinkedHashMap<String, String> sharedRoles = storedQuery.sharedWithGroups();

			if(sharedRoles != null && sharedRoles.size() > 0) {
				for (String roleID : sharedRoles.keySet()) {
					if (CFW.Context.Request.hasRole(Integer.parseInt(roleID))) {
						return true;
					}
				}
			}
		}

		//-----------------------------------
		// Check User has Editor Role
		LinkedHashMap<String, String> editorRoles = storedQuery.editorGroups();

		if(editorRoles != null && editorRoles.size() > 0) {
			for (String roleID : editorRoles.keySet()) {
				if (CFW.Context.Request.hasRole(Integer.parseInt(roleID))) {

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
		
		if( permissions.containsKey(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN) ) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is StoredQuery Administrator and has access to every storedQuery.");
			JsonArray adminResult = new JsonArray(); 
			adminResult.add(adminObject);
			return adminResult;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		String likeID = "%\""+user.id()+"\":%";
		
		return new CFWSQL(new CFWStoredQuery())
			.queryCache()
			.loadSQLResource(FeatureStoredQuery.PACKAGE_RESOURCES, "SQL_permissionAuditByUser.sql", 
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
		
		return new CFWSQL(new CFWStoredQuery())
				.queryCache()
				.loadSQLResource(FeatureStoredQuery.PACKAGE_RESOURCES, "SQL_permissionAuditByUsersGroups.sql", 
						user.id())
				.getAsJSONArray();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static AutocompleteResult autocompleteStoredQuery(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		
		ResultSet resultSet = new CFWStoredQuery()
			.queryCache(CFWDBStoredQuery.class, "autocompleteStoredQuery")
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWStoredQueryFields.PK_ID,
					CFWStoredQueryFields.NAME)
			.whereLike(CFWStoredQueryFields.NAME, "%"+searchValue+"%")
				.and(CFWStoredQueryFields.VERSION, 0)
			.limit(maxResults)
			.getResultSet();
		
		//------------------------------------
		// Filter by Access
		AutocompleteList list = new AutocompleteList();
		try {
			while(resultSet != null && resultSet.next()) {
				int id = resultSet.getInt("PK_ID");
				if(hasUserAccessToStoredQuery(id)) {
					String name = resultSet.getString("NAME");
					String owner = resultSet.getString("OWNER");
					list.addItem(id, name, "Owner: "+owner);
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error while autocomplete storedQuery.", new Throwable());
		} finally {
			CFWDB.close(resultSet);
		}

		
		return new AutocompleteResult(list);
		
	}
	
	/***************************************************************
	 * Return a list of all version of a dashboard as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getQueryVersionsListAsJSON(String queryID) {
		
		CFWStoredQuery query = selectByID(queryID);
		
		return new CFWSQL(query)
				.queryCache()
				.select()
				.where(CFWStoredQueryFields.PK_ID, queryID)
				.or(CFWStoredQueryFields.VERSION_GROUP, query.versionGroup())
				.orderbyDesc(CFWStoredQueryFields.VERSION)
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a JSON string for export.
	 * 
	 * @return Returns a JSON array string.
	 ****************************************************************/
	public static String getJsonForExport(String StoredQueryID) {

		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)
		|| CFW.DB.StoredQuery.checkCanEdit(StoredQueryID)) {			
			JsonArray StoredQueryArray = null;
			if(Strings.isNullOrEmpty(StoredQueryID)) {
				StoredQueryArray = new CFWStoredQuery()
						.queryCache(CFWDBStoredQuery.class, "getJsonForExportAll")
						.select()
						.getObjectsAsJSONArray();
			}else {
				StoredQueryArray = new CFWStoredQuery()
						.queryCache(CFWDBStoredQuery.class, "getJsonForExport")
						.select()
						.where(CFWStoredQueryFields.PK_ID, StoredQueryID)
						.getObjectsAsJSONArray();
			}
			
			//-------------------------------
			// For Every StoredQuery
			for(JsonElement element : StoredQueryArray) {
				if(element.isJsonObject()) {
					
					//-------------------------------
					// Get Username
					JsonElement useridElement = element.getAsJsonObject().get(CFWStoredQueryFields.FK_ID_OWNER.toString());
					if(!useridElement.isJsonNull() && useridElement.isJsonPrimitive()) {
						String username = CFW.DB.Users.selectUsernameByID(useridElement.getAsInt());
						element.getAsJsonObject().addProperty("username", username);
					}
					
					//-------------------------------
					// Get Widgets & Parameters
					JsonElement idElement = element.getAsJsonObject().get(CFWStoredQueryFields.PK_ID.toString());
					if(!idElement.isJsonNull() && idElement.isJsonPrimitive()) {
						
						JsonArray parameters = CFW.DB.Parameters.getJsonArrayForExport(CFWParameterScope.query, idElement.getAsString());
						element.getAsJsonObject().add("parameters", parameters);
					}
					
				}
			}
			
			JsonObject resultObject = new JsonObject();
			resultObject.add("storedquery", StoredQueryArray);
			return CFW.JSON.toJSONPretty(resultObject);
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!") );
			return "[]";
		}
	}
	
	/***************************************************************
	 * Import an jsonArray exported with getJsonArrayForExport().
	 * @param json json object or array string
	 *   - Array of StoredQuerys:  [{ ... StoredQueryFields ...}, { ... StoredQueryFields ...}]	
	 *   - Object with StoredQuerys: { StoredQuerys: [ ...] }
	 *   - Object with Payload(One of above):  { payload: <objectOrArray> }
	 *     	
	 * @return Returns a JSON array string.
	 ****************************************************************/
	public static boolean importByJson(String json, boolean keepOwner) {

		//-----------------------------
		// Resolve JSON Array
		JsonElement element = CFW.JSON.stringToJsonElement(json);
		JsonArray array = null;
		
		if(element.isJsonArray()) {
			array = element.getAsJsonArray();
		}else if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if(object.has("payload")) {
				return importByJson(object.get("payload").toString(), keepOwner);
				
			}if(object.has("storedquery")) {
				return importByJson(object.get("storedquery").toString(), keepOwner);
				
			}else {
				new CFWLog(logger)
					.warn(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."), new Exception());
				return false;
			}
		}else {
			new CFWLog(logger)
				.warn(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."), new Exception());
			return false;
		}
		
		//-----------------------------
		// Create StoredQuerys
		for(JsonElement StoredQueryElement : array) {
			if(StoredQueryElement.isJsonObject()) {
				JsonObject storedQueryObject = StoredQueryElement.getAsJsonObject();
				
				//-----------------------------
				// Map values
				CFWStoredQuery storedQuery = new CFWStoredQuery();
				storedQuery.mapJsonFields(storedQueryObject, true, true);
				storedQuery.id(null);
				storedQuery.timeCreated( new Timestamp(new Date().getTime()) );
				
				String importedName = storedQuery.name();
				if(!checkCanSaveWithName(storedQuery)) {
					importedName += "-"+CFW.Random.stringAlphaNum(6);
					storedQuery.name(importedName);
				}
				
				
				//-----------------------------
				// Reset StoredQuery ID and Owner
				storedQuery.id(null);
				
				if(keepOwner && storedQueryObject.has("username")) {
					String username = storedQueryObject.get("username").getAsString();
					User owner = CFW.DB.Users.selectByUsernameOrMail(username);
					if(owner != null) {
						storedQuery.foreignKeyOwner(owner.id());
					}else {
						CFW.Messages.addWarningMessage(
							"The the stored query owner with name '"+username+"' could not be resolved. Set the owner to the importing user."
						);
						storedQuery.foreignKeyOwner(CFW.Context.Request.getUser().id());
					}
				}else {
					storedQuery.foreignKeyOwner(CFW.Context.Request.getUser().id());
				}
				
				//-----------------------------
				// Resolve Shared Users
				if(storedQuery.sharedWithUsers() != null) {
					LinkedHashMap<String, String> resolvedViewers = new LinkedHashMap<String, String>();
					for(String id : storedQuery.sharedWithUsers().keySet()) {
						User user = CFW.DB.Users.selectByID(Integer.parseInt(id));
						if(user != null) {
							
							resolvedViewers.put(""+user.id(), user.createUserLabel());
						}else {
							CFW.Messages.addWarningMessage(
									CFW.L("cfw_core_error_usernotfound",
										  "The user '{0}' could not be found.",
										  storedQuery.sharedWithUsers().get(id))
							);
						}
						
					}
					storedQuery.sharedWithUsers(resolvedViewers);
				}
				
				//-----------------------------
				// Resolve Editors
				if(storedQuery.editors() != null) {
					LinkedHashMap<String, String> resolvedEditors = new LinkedHashMap<String, String>();
					for(String id : storedQuery.editors().keySet()) {
						User user = CFW.DB.Users.selectByID(Integer.parseInt(id));
						if(user != null) {
							resolvedEditors.put(""+user.id(), user.username());
						}else {
							CFW.Messages.addWarningMessage(
									CFW.L("cfw_core_error_usernotfound",
										  "The  user '{0}' could not be found.",
										  storedQuery.editors().get(id))
							);
						}
					}
					storedQuery.editors(resolvedEditors);
				}
				
				//-----------------------------
				// Resolve Shared Roles
				if(storedQuery.sharedWithGroups() != null) {
					LinkedHashMap<String, String> resolvedSharedRoles = new LinkedHashMap<String, String>();
					for(String id : storedQuery.sharedWithGroups().keySet()) {
						Role role = CFW.DB.Roles.selectByID(Integer.parseInt(id));
						if(role != null) {
							resolvedSharedRoles.put(""+role.id(), role.name());
						}else {
							CFW.Messages.addWarningMessage(
									CFW.L("cfw_core_error_rolenotfound",
										  "The  role '{0}' could not be found.",
										  storedQuery.sharedWithGroups().get(id))
							);
						}
					}
					storedQuery.sharedWithGroups(resolvedSharedRoles);
				}
				
				//-----------------------------
				// Resolve Editor Roles
				if(storedQuery.editorGroups() != null) {
					LinkedHashMap<String, String> resolvedEditorRoles = new LinkedHashMap<String, String>();
					for(String id : storedQuery.editorGroups().keySet()) {
						Role role = CFW.DB.Roles.selectByID(Integer.parseInt(id));
						if(role != null) {
							resolvedEditorRoles.put(""+role.id(), role.name());
						}else {
							CFW.Messages.addWarningMessage(
									CFW.L("cfw_core_error_rolenotfound",
										  "The  role '{0}' could not be found.",
										  storedQuery.editorGroups().get(id))
							);
						}
					}
					storedQuery.editorGroups(resolvedEditorRoles);
				}
				
				//-----------------------------
				// Create StoredQuery
				Integer newStoredQueryID = CFW.DB.StoredQuery.createGetPrimaryKey(storedQuery);
				if(newStoredQueryID == null) {
					new CFWLog(logger)
						.severe("StoredQuery '"+storedQuery.name()+"' could not be imported.");
					continue;
				}
				
				storedQuery.saveSelectorFields();
				
				//-----------------------------
				// Create Parameters
				HashMap<Integer, Integer> oldNewParamIDs = new HashMap<>();
				if(storedQueryObject.has("parameters")) {
					
					//-----------------------------
					// Check format
					if(!storedQueryObject.get("parameters").isJsonArray()) {
						CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
						continue;
					}
					
					//-----------------------------
					// Create Parameters
					JsonArray paramsArray = storedQueryObject.get("parameters").getAsJsonArray();
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
							param.foreignKeyQuery(newStoredQueryID);
							
							//-----------------------------
							// Create Parameter
							
							Integer newID = CFW.DB.Parameters.createGetPrimaryKey(param);
							if(newID == null) {
								CFW.Messages.addErrorMessage("Error creating imported parameter.");
								continue;
							}
							
							oldNewParamIDs.put(oldID, newID);
							
						}
					}
				}
				
				
			}else {
				CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_wronginputformat","The provided import format seems not to be supported."));
				continue;
			}
		}
		
		return true;
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, CFWStoredQueryFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(CFWStoredQuery item) {
		if(item != null) {
			return checkExistsByName(item.name());
		}
		return false;
	}
	
	public static boolean checkCanSaveWithName(CFWStoredQuery storedQuery) {	
		return !CFWDBDefaultOperations.checkExistsByIgnoreSelf(storedQuery, CFWStoredQueryFields.NAME.toString(), storedQuery.name());
	}
	
	/*****************************************************************
	 * Checks if the current user can edit the storedQuery.
	 *****************************************************************/
	public static boolean checkCanEdit(String storedQueryID) {
		return checkCanEdit(Integer.parseInt(storedQueryID));
	}
	/*****************************************************************
	 * Checks if the current user can edit the storedQuery.
	 *****************************************************************/
	public static boolean checkCanEdit(int storedQueryID) {
		
		CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(storedQueryID);
		return checkCanEdit(storedQuery);
	}

	
	/*****************************************************************
	 * Checks if the current user can edit the storedQuery.
	 *****************************************************************/
	public static boolean checkCanEdit(CFWStoredQuery storedQuery) {
		User user = CFW.Context.Request.getUser();
		
		//--------------------------------------
		// if user is not logged in / public storedQuery
		if(user == null) { return false; }
		
		//--------------------------------------
		// Check User is StoredQuery owner, admin
		// or listed in editors
		if( storedQuery.foreignKeyOwner().equals(user.id())
		|| ( storedQuery.editors() != null && storedQuery.editors().containsKey(user.id().toString()) )
		|| CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			return true;
		}
		
		//--------------------------------------
		// Check User has Editor Role
		if(storedQuery.editorGroups() != null) {
			for(int roleID : CFW.Context.Request.getUserRoles().keySet()) {
				if (storedQuery.editorGroups().containsKey(""+roleID)) {
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
	public static boolean switchToVersion(String queryID, String versionID) {
		
		boolean success = true;
		CFW.DB.transactionStart();
		
			//----------------------------
			// Fetch Original Stuff
			CFWStoredQuery current = selectByID(queryID);
			int originalID = current.id();

			//----------------------------
			// Fetch Version Stuff
			CFWStoredQuery toVersion = selectByID(versionID);
			int toID = toVersion.id();
			
			//----------------------------
			// Switch current
			
			// remove data we do not want to override
			toVersion.removeField(CFWStoredQueryFields.PK_ID);
			toVersion.removeField(CFWStoredQueryFields.NAME);
			toVersion.removeField(CFWStoredQueryFields.VERSION);
			String toVersionSettings = toVersion.toJSON();
			
			current.mapJsonFields(toVersionSettings, false, false);

			success &= update(current);
			
			if(success) {
				current.saveSelectorFields();
			}
			//----------------------------
			// Replace Parameter
			CFW.DB.Parameters.replaceParameters(CFWParameterScope.query, toID, originalID);
			
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
	public static int  getMaxVersionForQuery(int id) {
		
		CFWStoredQuery query = selectByID(id);
		
		return new CFWSQL(new CFWStoredQuery())
			.custom("SELECT MAX(VERSION) AS MAXVERSION"
					+" FROM "+CFWStoredQuery.TABLE_NAME
					+" WHERE PK_ID = ? OR VERSION_GROUP = ?"
					, id
					, query.versionGroup()
					)
			.getFirstAsInteger();
	}
	
	/********************************************************************************************
	 * Creates multiple StoredQuery in the DB.
	 * @param StoredQuery with the values that should be inserted. ID will be set by the Database.
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
	 * Creates multiple StoredQuery in the DB.
	 * @param StoredQuery with the values that should be inserted. ID will be set by the Database.
	 * @return 
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static String getTagsAsJSON() {
				
		return CFW.JSON.toJSON(getTags().toArray(new String[] {}));
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified storedQuery.
	 * @param StoredQuery with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWStoredQuery... storedQuery) {
		
		for(CFWStoredQuery credential : storedQuery) {
			updateTags(credential);
		}
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified storedQuery.
	 * @param StoredQuery with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWStoredQuery storedQuery) {
		
		if(cachedTags == null) {
			fetchAndCacheTags();
		}
		
		if(storedQuery.tags() != null) {
			for(Object tag : storedQuery.tags()) {
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
		
		ResultSet resultSet = new CFWSQL(new CFWStoredQuery())
			.queryCache()
			.select(CFWStoredQueryFields.TAGS.toString())
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
		
		ResultSet resultSet = new CFWSQL(new CFWStoredQuery())
			.queryCache()
			.select(CFWStoredQueryFields.TAGS.toString())
			.where(CFWStoredQueryFields.FK_ID_OWNER.toString(), userID)
			.or(CFWStoredQueryFields.IS_SHARED.toString(), true)
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
	
	
	/********************************************************************************************
	 * Adds the widget to the cache for the specified storedQuery.
	 * @param StoredQuery with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateWidgetCache(CFWStoredQuery storedQuery) {
		
		removeWidgetCache(storedQuery.id());
		
		Integer id = storedQuery.id();
		
		if( !storedQuery.isArchived()
		&&  storedQuery.makeWidget() 
		&&  storedQuery.version() == 0
		&&  id != null
		){
			
			WidgetStoredQuery widget = new WidgetStoredQuery(storedQuery);
			widgetCache.put(id, widget);
			CFW.Registry.Widgets.add(widget);
			CFW.Registry.Widgets.resetCachedFiles();
		}
	}
	
	/********************************************************************************************
	 * Removes the widget from the cache for the specified storedQuery.
	 * @param StoredQuery with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void removeWidgetCache(Integer storedQueryID) {
		
		WidgetStoredQuery widget = widgetCache.remove(storedQueryID);
		CFW.Registry.Widgets.remove(widget);
		CFW.Registry.Widgets.resetCachedFiles();
	}
	
	/********************************************************************************************
	 * Fetch cachedTags from the database and stores them into the cache.
	 * 
	 ********************************************************************************************/
	public static void fetchAndCacheWidgets() {
		
		cachedTags = new TreeSet<String>();
		
		ArrayList<CFWStoredQuery> queryArray = new CFWSQL(new CFWStoredQuery())
			.queryCache()
			.select()
			.where(CFWStoredQueryFields.IS_ARCHIVED, false)
			.and(CFWStoredQueryFields.MAKE_WIDGET, true)
			.and(CFWStoredQueryFields.VERSION, 0)
			.getAsObjectListConvert(CFWStoredQuery.class);
		
		for(CFWStoredQuery query : queryArray) {
			updateWidgetCache(query);
		}
		
				
	}

	/*****************************************************************
	 *
	 *****************************************************************/
	static void pushEAVStats(String entityName, String storedQueryID, int value) {
		
		Integer userID = CFW.Context.Request.getUserID();
		String userIDString = (userID != null) ? ""+userID : null;
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
		attributes.put(EAV_ATTRIBUTE_STOREDQUERYID, storedQueryID);
		attributes.put(EAV_ATTRIBUTE_USERID, userIDString);
		
		CFW.DB.EAVStats.pushStatsCounter(FeatureStoredQuery.EAV_STATS_CATEGORY, entityName, attributes, value);
		
	}
	
	/***************************************************************
	 * Return a list of all user storedQuery as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static JsonArray getEAVStats(String boardID, long earliest, long latest) {
		
		String category = FeatureStoredQuery.EAV_STATS_CATEGORY;
		String entity = "%";
		
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		values.put(EAV_ATTRIBUTE_STOREDQUERYID, boardID);
		
		JsonArray array = CFWDBEAVStats.fetchStatsAsJsonArray(category, entity, values, earliest, latest);
		
		return array;
	}
		
}

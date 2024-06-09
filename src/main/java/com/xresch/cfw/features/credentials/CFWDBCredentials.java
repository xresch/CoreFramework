package com.xresch.cfw.features.credentials;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
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
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.credentials.CFWCredentials.CFWCredentialsFields;
import com.xresch.cfw.features.eav.CFWDBEAVStats;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBCredentials {
	
	private static final String EAV_ATTRIBUTE_USERID = "userid";
	private static final String EAV_ATTRIBUTE_DASHBOARDID = "credentialsid";
	private static final String SQL_SUBQUERY_OWNER = "SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_USER";
	private static final String SQL_SUBQUERY_ISFAVED = "(SELECT COUNT(*) FROM CFW_DASHBOARD_FAVORITE_MAP M WHERE M.FK_ID_USER = ? AND M.FK_ID_DASHBOARD = T.PK_ID) > 0";

	private static Class<CFWCredentials> cfwObjectClass = CFWCredentials.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBCredentials.class.getName());
	
	private static final String[] auditLogFieldnames = new String[] { 
			CFWCredentialsFields.PK_ID.toString()
		  , CFWCredentialsFields.NAME.toString()
		};
	
	public static TreeSet<String> cachedTags = null;
	
	//####################################################################################################
	// Precheck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWCredentials credentials = (CFWCredentials)object;
			
			if(credentials == null || credentials.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a name for the credentials.", new Throwable());
				return false;
			}
			
			if(!checkCanSaveWithName(credentials)) {
				return false;
			}

			return true;
		}
	};
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			CFWCredentials credentials = (CFWCredentials)object;
			
			if(credentials == null) {
				new CFWLog(logger)
				.severe("The credential ID was null, so nothing to delete.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static Integer createGetPrimaryKey(CFWCredentials item) { 
		updateTags(item); 
		return CFWDBDefaultOperations.createGetPrimaryKeyWithout(prechecksCreateUpdate, auditLogFieldnames, item);
	}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean update(CFWCredentials item) { 
		updateTags(item); 
		item.lastUpdated(new Timestamp(System.currentTimeMillis()));
		return CFWDBDefaultOperations.updateWithout(prechecksCreateUpdate, auditLogFieldnames, item); 
	}
	
	public static boolean updateLastUpdated(String credentialsID){ 
		return updateLastUpdated(Integer.parseInt(credentialsID));
	}
	
	public static boolean updateLastUpdated(int credentialsID){ 
		CFWCredentials toUpdate = new CFWCredentials().id(credentialsID)
				.lastUpdated(new Timestamp(System.currentTimeMillis()));
		
		return new CFWSQL(toUpdate)
			.update(CFWCredentialsFields.LAST_UPDATED)
			;
		
	}
	
	public static boolean updateIsArchived(String credentialsID, boolean isArchived){ 
		return updateIsArchived(Integer.parseInt(credentialsID), isArchived);
	}
	
	public static boolean updateIsArchived(int credentialsID, boolean isArchived){ 
		CFWCredentials toUpdate = selectByID(credentialsID);
		
		toUpdate.isArchived(isArchived);
		
		String auditMessage = ( 
							(isArchived) ? 
							"Moving credentials to archive:"
							: "Extracting credentials from archive:"
							)
							+" ID="+credentialsID+", NAME="+toUpdate.name()
							;
		new CFWLog(logger).audit(CFWAuditLogAction.MOVE, CFWCredentials.class, auditMessage);
		
		return new CFWSQL(toUpdate)
			.update(CFWCredentialsFields.IS_ARCHIVED)
			;
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(String id) {
		
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, CFWCredentialsFields.PK_ID.toString(), id); 

	}

	
	
	public static boolean deleteByIDForCurrentUser(String id)	{ 
		
		if(isCredentialsOfCurrentUser(id)) {
			return deleteByID(id);
		}else {
			CFW.Messages.noPermission();
			return false;
		}
	} 
	
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWCredentials selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWCredentialsFields.PK_ID.toString(), id);
	}
	
	public static CFWCredentials selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWCredentialsFields.PK_ID.toString(), id);
	}
	
	public static CFWCredentials selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWCredentialsFields.NAME.toString(), name);
	}
	
	
	/***************************************************************
	 * Return a list of all user credentials
	 * 
	 * @return Returns a resultSet with all credentials or null.
	 ****************************************************************/
	public static ResultSet getUserCredentialsList() {
		
		return new CFWCredentials()
				.queryCache(CFWDBCredentials.class, "getUserCredentialsList")
				.select()
				.where(CFWCredentialsFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.orderby(CFWCredentialsFields.NAME.toString())
				.getResultSet();
		
	}
	

	/***************************************************************
	 * Return a list of all user credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserCredentialsListAsJSON() {
		
		return new CFWCredentials()
				.queryCache(CFWDBCredentials.class, "getUserCredentialsListAsJSON")
				.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, CFW.Context.Request.getUserID())
				.select()
				.where(CFWCredentialsFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWCredentialsFields.IS_ARCHIVED, false)
				.orderby(CFWCredentialsFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a list of all user credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserArchivedListAsJSON() {
		
		return new CFWSQL(new CFWCredentials())
				.queryCache()
				.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, CFW.Context.Request.getUserID())
				.select()
				.where(CFWCredentialsFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWCredentialsFields.IS_ARCHIVED, true)
				.orderby(CFWCredentialsFields.NAME.toString())
				.getAsJSON();
	}
		

	/***************************************************************
	 * Return a list of all user credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminCredentialsListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			return new CFWSQL(new CFWCredentials())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.select()
				.where(CFWCredentialsFields.IS_ARCHIVED, false)
				.orderby(CFWCredentialsFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	/***************************************************************
	 * Return a list of all archived credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminArchivedListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			return new CFWSQL(new CFWCredentials())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.select()
				.where(CFWCredentialsFields.IS_ARCHIVED, true)
				.orderby(CFWCredentialsFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	
	/***************************************************************
	 * Return a list of all user credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getSharedCredentialsListAsJSON() {
		
		int userID = CFW.Context.Request.getUser().id();
		String sharedUserslikeID = "%\""+userID+"\":%";
		
		//---------------------
		// Shared with User
		CFWSQL query =  new CFWSQL(new CFWCredentials())
			.loadSQLResource(FeatureCredentials.PACKAGE_RESOURCES, "SQL_getSharedCredentialsListAsJSON.sql", 
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
			.select(CFWCredentialsFields.PK_ID
				  , CFWCredentialsFields.NAME
				  , CFWCredentialsFields.DESCRIPTION
				  , CFWCredentialsFields.TAGS
				  , CFWCredentialsFields.ALLOW_EDIT_SETTINGS
				  )
			.where(CFWCredentialsFields.IS_SHARED, true)
			.and(CFWCredentialsFields.IS_ARCHIVED, false)
			.and().custom("(");
		
		Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWCredentialsFields.JSON_SHARE_WITH_GROUPS, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		
		//-------------------------
		// Union with Editor Roles
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.columnSubquery("IS_FAVED", SQL_SUBQUERY_ISFAVED, userID)
			.select(CFWCredentialsFields.PK_ID
					, CFWCredentialsFields.NAME
					, CFWCredentialsFields.DESCRIPTION
					, CFWCredentialsFields.TAGS
					, CFWCredentialsFields.ALLOW_EDIT_SETTINGS
					)
			.where(CFWCredentialsFields.IS_ARCHIVED, false)
			.and().custom("(");
		
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWCredentialsFields.JSON_EDITOR_GROUPS, "%\""+roleID+"\":%");
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
					, checkCanEdit(board.get(CFWCredentialsFields.PK_ID.toString()).getAsInt())
				);
			
		}
	
		//-------------------------
		// Return
		return CFW.JSON.toJSON(sharedBoards);
	}
	
	
	/***************************************************************
	 * Return a list of all version of a credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getCredentialsVersionsListAsJSON(String credentialsID) {
		
		CFWCredentials credentials = selectByID(credentialsID);
		
		return new CFWSQL(credentials)
				.queryCache()
				.select()
				.where(CFWCredentialsFields.PK_ID, credentialsID)
				.getAsJSON();
	}
			
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isCredentialsOfCurrentUser(String credentialsID) {
		return isCredentialsOfCurrentUser(Integer.parseInt(credentialsID));
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isCredentialsOfCurrentUser(int credentialsID) {
		
		int count = new CFWSQL(new CFWCredentials())
			.selectCount()
			.where(CFWCredentialsFields.PK_ID.toString(), credentialsID)
			.and(CFWCredentialsFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
			.executeCount();
		
		return count > 0;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToCredentials(int credentialsID) {
		return  hasUserAccessToCredentials(""+credentialsID);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToCredentials(String credentialsID) {


		//-----------------------------------
		// Check User is Admin
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			return true;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		
		int userID = CFW.Context.Request.getUser().id();
		String likeID = "%\""+userID+"\":%";
		
		int count = new CFWSQL(new CFWCredentials())
			.loadSQLResource(FeatureCredentials.PACKAGE_RESOURCES, "SQL_hasUserAccessToCredentials.sql", 
					credentialsID, 
					userID, 
					likeID,
					likeID)
			.executeCount();
		
		if( count > 0) {
			return true;
		}
		
		
		//-----------------------------------
		// Get Credentials 
		CFWCredentials credentials = (CFWCredentials)new CFWSQL(new CFWCredentials())
			.select(CFWCredentialsFields.JSON_SHARE_WITH_GROUPS, CFWCredentialsFields.JSON_EDITOR_GROUPS)
			.where(CFWCredentialsFields.PK_ID, credentialsID)
			.getFirstAsObject();
		
		//-----------------------------------
		// Check User has Shared Role
		LinkedHashMap<String, String> sharedRoles = credentials.sharedWithGroups();
		
		if(sharedRoles != null && sharedRoles.size() > 0) {
			for(String roleID : sharedRoles.keySet()) {
				if(CFW.Context.Request.hasRole(Integer.parseInt(roleID)) ) {
					return true;
				}
			}
		}
		
		//-----------------------------------
		// Check User has Editor Role
		LinkedHashMap<String, String> editorRoles = credentials.editorGroups();
		
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
		
		if( permissions.containsKey(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN) ) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Credentials Administrator and has access to every credentials.");
			JsonArray adminResult = new JsonArray(); 
			adminResult.add(adminObject);
			return adminResult;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		String likeID = "%\""+user.id()+"\":%";
		
		return new CFWSQL(new CFWCredentials())
			.queryCache()
			.loadSQLResource(FeatureCredentials.PACKAGE_RESOURCES, "SQL_permissionAuditByUser.sql", 
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
		
		return new CFWSQL(new CFWCredentials())
				.queryCache()
				.loadSQLResource(FeatureCredentials.PACKAGE_RESOURCES, "SQL_permissionAuditByUsersGroups.sql", 
						user.id())
				.getAsJSONArray();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static AutocompleteResult autocompleteCredentials(String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return new AutocompleteResult();
		}
		
		ResultSet resultSet = new CFWCredentials()
			.queryCache(CFWDBCredentials.class, "autocompleteCredentials")
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWCredentialsFields.PK_ID,
					CFWCredentialsFields.NAME)
			.whereLike(CFWCredentialsFields.NAME, "%"+searchValue+"%")
			.limit(maxResults)
			.getResultSet();
		
		//------------------------------------
		// Filter by Access
		AutocompleteList list = new AutocompleteList();
		try {
			while(resultSet != null && resultSet.next()) {
				int id = resultSet.getInt("PK_ID");
				if(hasUserAccessToCredentials(id)) {
					String name = resultSet.getString("NAME");
					String owner = resultSet.getString("OWNER");
					list.addItem(id, name, "Owner: "+owner);
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error while autocomplete credentials.", new Throwable());
		} finally {
			CFWDB.close(resultSet);
		}

		
		return new AutocompleteResult(list);
		
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, CFWCredentialsFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(CFWCredentials item) {
		if(item != null) {
			return checkExistsByName(item.name());
		}
		return false;
	}
	
	public static boolean checkCanSaveWithName(CFWCredentials credentials) {	
		return CFWDBDefaultOperations.checkExistsByIgnoreSelf(credentials, CFWCredentialsFields.NAME.toString(), credentials.name());
	}
	
	/*****************************************************************
	 * Checks if the current user can edit the credentials.
	 *****************************************************************/
	public static boolean checkCanEdit(String credentialsID) {
		return checkCanEdit(Integer.parseInt(credentialsID));
	}
	/*****************************************************************
	 * Checks if the current user can edit the credentials.
	 *****************************************************************/
	public static boolean checkCanEdit(int credentialsID) {
		
		CFWCredentials credentials = CFW.DB.Credentials.selectByID(credentialsID);
		return checkCanEdit(credentials);
	}
	
	/*****************************************************************
	 * Checks if the current user can edit the credentials.
	 *****************************************************************/
	public static boolean checkCanEdit(CFWCredentials credentials) {
		User user = CFW.Context.Request.getUser();
		
		//--------------------------------------
		// if user is not logged in / public credentials
		if(user == null) { return false; }
		
		//--------------------------------------
		// Check User is Credentials owner, admin
		// or listed in editors
		if( credentials.foreignKeyOwner().equals(user.id())
		|| ( credentials.editors() != null && credentials.editors().containsKey(user.id().toString()) )
		|| CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			return true;
		}
		
		//--------------------------------------
		// Check User has Editor Role
		if(credentials.editorGroups() != null) {
			for(int roleID : CFW.Context.Request.getUserRoles().keySet()) {
				if (credentials.editorGroups().containsKey(""+roleID)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	/********************************************************************************************
	 * Creates multiple Credentials in the DB.
	 * @param Credentials with the values that should be inserted. ID will be set by the Database.
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
	 * Creates multiple Credentials in the DB.
	 * @param Credentials with the values that should be inserted. ID will be set by the Database.
	 * @return 
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static String getTagsAsJSON() {
				
		return CFW.JSON.toJSON(getTags().toArray(new String[] {}));
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified credentials.
	 * @param Credentials with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWCredentials... credentials) {
		
		for(CFWCredentials credential : credentials) {
			updateTags(credential);
		}
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified credentials.
	 * @param Credentials with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWCredentials credentials) {
		
		if(cachedTags == null) {
			fetchAndCacheTags();
		}
		
		if(credentials.tags() != null) {
			for(Object tag : credentials.tags()) {
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
		
		ResultSet resultSet = new CFWSQL(new CFWCredentials())
			.queryCache()
			.select(CFWCredentialsFields.TAGS.toString())
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
		
		ResultSet resultSet = new CFWSQL(new CFWCredentials())
			.queryCache()
			.select(CFWCredentialsFields.TAGS.toString())
			.where(CFWCredentialsFields.FK_ID_OWNER.toString(), userID)
			.or(CFWCredentialsFields.IS_SHARED.toString(), true)
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
	static void pushEAVStats(String entityName, String credentialsID, int value) {
		
		Integer userID = CFW.Context.Request.getUserID();
		String userIDString = (userID != null) ? ""+userID : null;
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
		attributes.put(EAV_ATTRIBUTE_DASHBOARDID, credentialsID);
		attributes.put(EAV_ATTRIBUTE_USERID, userIDString);
		
		CFW.DB.EAVStats.pushStatsCounter(FeatureCredentials.EAV_STATS_CATEGORY, entityName, attributes, value);
		
	}
	
	/***************************************************************
	 * Return a list of all user credentials as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static JsonArray getEAVStats(String boardID, long earliest, long latest) {
		
		String category = FeatureCredentials.EAV_STATS_CATEGORY;
		String entity = "%";
		
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		values.put(EAV_ATTRIBUTE_DASHBOARDID, boardID);
		
		JsonArray array = CFWDBEAVStats.fetchStatsAsJsonArray(category, entity, values, earliest, latest);
		
		return array;
	}
		
}

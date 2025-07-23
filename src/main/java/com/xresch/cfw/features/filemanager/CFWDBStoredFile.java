package com.xresch.cfw.features.filemanager;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.eav.CFWDBEAVStats;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBStoredFile {
	
	private static final String EAV_ATTRIBUTE_USERID = "userid";
	private static final String EAV_ATTRIBUTE_STOREDFILEID = "storedfileid";
	private static final String SQL_SUBQUERY_OWNER = "SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_OWNER";

	private static Class<CFWStoredFile> cfwObjectClass = CFWStoredFile.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBStoredFile.class.getName());
	
	private static final String[] auditLogFieldnames = new String[] { 
			CFWStoredFileFields.PK_ID.toString()
		  , CFWStoredFileFields.NAME.toString()
		  , CFWStoredFileFields.SIZE.toString()
		};
	
	public static TreeSet<String> cachedTags = null;
	
	//####################################################################################################
	// Precheck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWStoredFile storedfile = (CFWStoredFile)object;
			
			if(storedfile == null || storedfile.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a name for the Stored File.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			CFWStoredFile storedfile = (CFWStoredFile)object;
			
			if(storedfile == null) {
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
	public static Integer createGetPrimaryKey(CFWStoredFile item) { 
		updateTags(item); 
		
		return CFWDBDefaultOperations.createGetPrimaryKeyWithout(prechecksCreateUpdate, auditLogFieldnames, item, CFWStoredFileFields.DATA);
	
	}
	
	/**********************************************************************************
	 * Store file data to the DATA column
	 * 
	 * @param item the file the data should be stored to.
	 * @param fileData the inputStream providing the data.
	 **********************************************************************************/
	public static boolean storeData(CFWStoredFile item, InputStream fileData) { 
		return new CFWSQL(item).executeStreamBytes(CFWStoredFileFields.DATA, fileData);
	}
	
	/**********************************************************************************
	 * Retrieve Data from the DATA column.
	 * @param item the file the data should be stored to.
	 * @param fileData the inputStream providing the data.
	 **********************************************************************************/
	public static boolean retrieveData(CFWStoredFile item, OutputStream out) { 
		return new CFWSQL(item).executeRetrieveBytes(CFWStoredFileFields.DATA, out);
	}
	
	/**********************************************************************************
	 * Creates a new file and stores the data.
	 * @param item the file the data should be stored to.
	 * @param fileData the inputStream providing the data.
	 **********************************************************************************/
	public static boolean createAndStoreData(CFWStoredFile item, InputStream fileData) { 
		
		boolean success = true;
		
		CFW.DB.transactionStart();
			
			Integer primaryKey = createGetPrimaryKey(item);
			
			if(primaryKey != null) {
				success &= storeData(item, fileData);
			}else {
				success = false;
			}
			
		CFW.DB.transactionEnd(success);
		
		return success;
	
	}
	
//	/**********************************************************************************
//	 * 
//	 * @param storedfileID¨the id of the storedfile that should be duplicated.
//	 * @param forVersioning true if this duplicate should be for versioning
//	 * @return
//	 **********************************************************************************/
//	public static Integer createDuplicate(String storedfileID, boolean forVersioning) { 
//
//		CFWStoredFile duplicate = CFW.DB.StoredFile.selectByID(storedfileID);
//		duplicate.updateSelectorFields();
//		
//		//---------------------------------
//		// Make sure it has a version group 
//		duplicate.id(null);
//		duplicate.timeCreated( new Timestamp(new Date().getTime()) );
//		
//		// need to check if null for automatic versioning
//		Integer id =  CFW.Context.Request.getUserID();
//
//		duplicate.foreignKeyOwner(id);
//		duplicate.name(duplicate.name()+"(Copy)");
//		duplicate.isShared(false);
//		
//		CFW.DB.transactionStart();
//		
//		Integer newID = duplicate.insertGetPrimaryKey();
//		
//		if(newID != null) {
//			
//				duplicate.id(newID);
//				//-----------------------------------------
//				// Save Selector Fields
//				//-----------------------------------------
//				boolean success = true;
//				success &= duplicate.saveSelectorFields();
//				if(!success) {
//					CFW.DB.transactionRollback();
//					new CFWLog(logger).severe("Error while saving selector fields for duplicate.");
//					return null;
//				}
//
//			CFW.DB.transactionCommit();
//			
//			CFW.Messages.addSuccessMessage("StoredFile duplicated successfully.");
//		}
//			
//		
//		
//		return newID;
//
//	}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean update(CFWStoredFile item) { 
		updateTags(item); 
		item.lastUpdated(new Timestamp(System.currentTimeMillis()));
		return CFWDBDefaultOperations.updateWithout(prechecksCreateUpdate, auditLogFieldnames, item, CFWStoredFileFields.DATA.toString()); 
	}
	
	public static boolean updateLastUpdated(String storedfileID){ 
		return updateLastUpdated(Integer.parseInt(storedfileID));
	}
	
	public static boolean updateLastUpdated(int storedfileID){ 
		CFWStoredFile toUpdate = new CFWStoredFile().id(storedfileID)
				.lastUpdated(new Timestamp(System.currentTimeMillis()));
		
		return new CFWSQL(toUpdate)
			.update(CFWStoredFileFields.LAST_UPDATED)
			;
		
	}
	
	public static boolean updateIsArchived(String storedfileID, boolean isArchived){ 
		return updateIsArchived(Integer.parseInt(storedfileID), isArchived);
	}
	
	public static boolean updateIsArchived(int storedfileID, boolean isArchived){ 
		CFWStoredFile toUpdate = selectByID(storedfileID);
		
		toUpdate.isArchived(isArchived);
		
		String auditMessage = ( 
							(isArchived) ? 
							"Moving Stored File to archive:"
							: "Extracting Stored File from archive:"
							)
							+" ID="+storedfileID+", NAME="+toUpdate.name()
							;
		new CFWLog(logger).audit(CFWAuditLogAction.MOVE, CFWStoredFile.class, auditMessage);
		
		return new CFWSQL(toUpdate)
			.update(CFWStoredFileFields.IS_ARCHIVED)
			;
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(String id) {
		
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, CFWStoredFileFields.PK_ID.toString(), id); 

	}


	public static boolean deleteByIDForCurrentUser(String id)	{ 
		
		if(isStoredFileOfCurrentUser(id)) {
			return deleteByID(id);
		}else {
			CFW.Messages.noPermission();
			return false;
		}
	} 
	
	public static boolean deleteMultipleByID(String IDs) 	{ 

		if(Strings.isNullOrEmpty(IDs)) { return true; }
		
		if(!IDs.matches("(\\d,?)+")) {
			new CFWLog(logger).severe("The File ID's '"+IDs+"' are not a comma separated list of strings.");
			return false;
		}

		boolean success = true;
		for(String id : IDs.split(",")) {
			success &= deleteByID(id);
		}

		return success;
	}
	
	public static boolean deleteMultipleByIDOfCurrentUser(String IDs) 	{ 

		if(Strings.isNullOrEmpty(IDs)) { return true; }
		
		if(!IDs.matches("(\\d,?)+")) {
			new CFWLog(logger).severe("The File ID's '"+IDs+"' are not a comma separated list of strings.");
			return false;
		}

		boolean success = true;
		for(String id : IDs.split(",")) {
			success &= deleteByIDForCurrentUser(id);
		}

		return success;
	}
	
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWStoredFile selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstByWithout(cfwObjectClass, CFWStoredFileFields.PK_ID.toString(), id, CFWStoredFileFields.DATA.toString());
	}
	
	public static CFWStoredFile selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstByWithout(cfwObjectClass, CFWStoredFileFields.PK_ID.toString(), id, CFWStoredFileFields.DATA.toString());
	}
	
	
	/***************************************************************
	 * Return a list of all user storedfile
	 * 
	 * @return Returns a resultSet with all storedfile or null.
	 ****************************************************************/
	public static ResultSet getUserStoredFileList() {
		
		return new CFWStoredFile()
				.queryCache(CFWDBStoredFile.class, "getUserStoredFileList")
				.selectWithout(CFWStoredFileFields.DATA.toString())
				.where(CFWStoredFileFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.orderby(CFWStoredFileFields.NAME.toString())
				.getResultSet();
		
	}
	

	/***************************************************************
	 * Return a list of all user storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserStoredFileListAsJSON() {
		
		return new CFWStoredFile()
				.queryCache(CFWDBStoredFile.class, "getUserStoredFileListAsJSON")
				.selectWithout(CFWStoredFileFields.DATA.toString())
				.where(CFWStoredFileFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWStoredFileFields.IS_ARCHIVED, false)
				.orderby(CFWStoredFileFields.NAME.toString())
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a list of all user storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getUserArchivedListAsJSON() {
		
		return new CFWSQL(new CFWStoredFile())
				.queryCache()
				.selectWithout(CFWStoredFileFields.DATA.toString())
				.where(CFWStoredFileFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
				.and(CFWStoredFileFields.IS_ARCHIVED, true)
				.orderby(CFWStoredFileFields.NAME.toString())
				.getAsJSON();
	}
		

	/***************************************************************
	 * Return a list of all user storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminStoredFileListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			return new CFWSQL(new CFWStoredFile())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.selectWithout(CFWStoredFileFields.DATA.toString())
				.where(CFWStoredFileFields.IS_ARCHIVED, false)
				.orderby(CFWStoredFileFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	/***************************************************************
	 * Return a list of all archived storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getAdminArchivedListAsJSON() {
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			return new CFWSQL(new CFWStoredFile())
				.queryCache()
				.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
				.selectWithout(CFWStoredFileFields.DATA.toString())
				.where(CFWStoredFileFields.IS_ARCHIVED, true)
				.orderby(CFWStoredFileFields.NAME.toString())
				.getAsJSON();
		}else {
			CFW.Messages.accessDenied();
			return "[]";
		}
	}
	
	
	/***************************************************************
	 * Return a list of all user storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getSharedStoredFileListAsJSON() {
		
		int userID = CFW.Context.Request.getUser().id();
		String sharedUserslikeID = "%\""+userID+"\":%";
		
		//---------------------
		// Shared with User
		CFWSQL query =  new CFWSQL(new CFWStoredFile())
			.loadSQLResource(FeatureFilemanager.PACKAGE_RESOURCES, "SQL_getSharedStoredFileListAsJSON.sql", 
					userID,
					userID,
					sharedUserslikeID,
					sharedUserslikeID);
			
		
		//-------------------------
		// Union with Shared Groups
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWStoredFileFields.PK_ID
				  , CFWStoredFileFields.NAME
				  , CFWStoredFileFields.SIZE
				  , CFWStoredFileFields.DESCRIPTION
				  , CFWStoredFileFields.TAGS
				  )
			.where(CFWStoredFileFields.IS_SHARED, true)
			.and(CFWStoredFileFields.IS_ARCHIVED, false)
			.and().custom("(");
		
		Integer[] roleArray = CFW.Context.Request.getUserRoles().keySet().toArray(new Integer[] {});
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWStoredFileFields.JSON_SHARE_WITH_GROUPS, "%\""+roleID+"\":%");
		}
		
		query.custom(")");
		
		//-------------------------
		// Union with Editor Roles
		query.union()
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select(CFWStoredFileFields.PK_ID
					, CFWStoredFileFields.NAME
					, CFWStoredFileFields.SIZE
					, CFWStoredFileFields.DESCRIPTION
					, CFWStoredFileFields.TAGS
					)
			.where(CFWStoredFileFields.IS_ARCHIVED, false)
			.and().custom("(");
		
		for(int i = 0 ; i < roleArray.length; i++ ) {
			int roleID = roleArray[i];
			if(i > 0) {
				query.or();
			}
			query.like(CFWStoredFileFields.JSON_EDITOR_GROUPS, "%\""+roleID+"\":%");
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
					, checkCanEdit(board.get(CFWStoredFileFields.PK_ID.toString()).getAsInt())
				);
			
		}
	
		//-------------------------
		// Return
		return CFW.JSON.toJSON(sharedBoards);
	}
	
				
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isStoredFileOfCurrentUser(String storedfileID) {
		return isStoredFileOfCurrentUser(Integer.parseInt(storedfileID));
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean isStoredFileOfCurrentUser(int storedfileID) {
		
		int count = new CFWSQL(new CFWStoredFile())
			.selectCount()
			.where(CFWStoredFileFields.PK_ID.toString(), storedfileID)
			.and(CFWStoredFileFields.FK_ID_OWNER.toString(), CFW.Context.Request.getUser().id())
			.executeCount();
		
		return count > 0;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToStoredFile(int storedfileID) {
		return  hasUserAccessToStoredFile(""+storedfileID);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static boolean hasUserAccessToStoredFile(String storedfileID) {

		// -----------------------------------
		// Check User is Admin
		if (CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			return true;
		}

		// -----------------------------------
		// Check User is Shared/Editor

		int userID = CFW.Context.Request.getUser().id();
		String likeID = "%\"" + userID + "\":%";

		int count = new CFWSQL(new CFWStoredFile())
				.loadSQLResource(FeatureFilemanager.PACKAGE_RESOURCES
						,"SQL_hasUserAccessToStoredFile.sql"
						, storedfileID
						, userID
						, likeID
						, likeID)
				.executeCount();

		if (count > 0) {
			return true;
		}

		//-----------------------------------
		// Get StoredFile
		CFWStoredFile storedfile = (CFWStoredFile) new CFWSQL(new CFWStoredFile())
				.select(CFWStoredFileFields.IS_SHARED
						, CFWStoredFileFields.JSON_SHARE_WITH_GROUPS
						, CFWStoredFileFields.JSON_EDITOR_GROUPS
					)
				.where(CFWStoredFileFields.PK_ID, storedfileID).getFirstAsObject();

		//-----------------------------------
		// Check User has Shared Role
		if(storedfile.isShared()) {
			LinkedHashMap<String, String> sharedRoles = storedfile.sharedWithGroups();

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
		LinkedHashMap<String, String> editorRoles = storedfile.editorGroups();

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
		
		if( permissions.containsKey(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN) ) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Stored File Administrator and has access to every Stored File.");
			JsonArray adminResult = new JsonArray(); 
			adminResult.add(adminObject);
			return adminResult;
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		String likeID = "%\""+user.id()+"\":%";
		
		return new CFWSQL(new CFWStoredFile())
			.queryCache()
			.loadSQLResource(FeatureFilemanager.PACKAGE_RESOURCES, "SQL_permissionAuditByUser.sql", 
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
		
		return new CFWSQL(new CFWStoredFile())
				.queryCache()
				.loadSQLResource(FeatureFilemanager.PACKAGE_RESOURCES, "SQL_permissionAuditByUsersGroups.sql", 
						user.id())
				.getAsJSONArray();
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static void autocompleteFileForQuery(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		if( helper.getCommandTokenCount() < 2 ) {
			return;
		}
		
		//-----------------------------
		// Get Search
		String searchValue = "";
		
		if(helper.getCommandTokenCount() > 2) {
			CFWQueryToken token = helper.getTokenBeforeCursor(0);
			
			if(token.isStringOrText()) {
				searchValue = token.value();
			};
		}
		
		ResultSet resultSet = new CFWStoredFile()
			.queryCache(CFWDBStoredFile.class, "autocompleteStoredFile")
			.columnSubquery("OWNER", SQL_SUBQUERY_OWNER)
			.select( CFWStoredFileFields.PK_ID
					, CFWStoredFileFields.NAME
					, CFWStoredFileFields.SIZE
					)
			.whereLike(CFWStoredFileFields.NAME, "%"+searchValue+"%")
				.and(CFWStoredFileFields.IS_ARCHIVED, false)
			.limit(50)
			.getResultSet();
		
		
		//------------------------------------
		// Filter by Access
		AutocompleteList list = new AutocompleteList();
		result.addList(list);
		int i = 0;
		try {
			while(resultSet != null && resultSet.next()) {

				int id = resultSet.getInt("PK_ID");
				if(hasUserAccessToStoredFile(id)) {

					String name = resultSet.getString("NAME");
					String owner = resultSet.getString("OWNER");
					Long size = resultSet.getLong("SIZE");
					
					JsonObject json = new JsonObject();
					json.addProperty("id", id);
					json.addProperty("name", name);
					
					String fileJsonString = "file = "+CFW.JSON.toJSON(json)+" ";
					
					
					//---------------------
					// Make Item
					AutocompleteItem item = new AutocompleteItem();
					item.value(fileJsonString);
					item.label(name);
					item.description(
							  "<span>"
								+ "<b>ID:&nbsp;</b>" + id 
								+ "&emsp;<b>Owner:&nbsp;</b>" + owner 
								+ "&emsp;<b>Size:&nbsp;</b>" + CFW.Utils.Text.toHumanReadableBytes(size, 1) 
							+ "<span>"
							);
					
					item.setMethodReplaceBeforeCursor(searchValue);
					
					
					list.addItem(item);
					
					
					//---------------------
					// Make Columns
					i++;
					
					if((i % 10) == 0) {
						list = new AutocompleteList();
						result.addList(list);
					}
					if(i == 50) { break; }
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error while autocomplete storedfile.", new Throwable());
		} finally {
			CFW.DB.close(resultSet);
		}
		
	}
	
	//####################################################################################################
	// CHECKS
	//####################################################################################################
	public static boolean checkExistsByName(String itemName) {	return CFWDBDefaultOperations.checkExistsBy(cfwObjectClass, CFWStoredFileFields.NAME.toString(), itemName); }
	public static boolean checkExistsByName(CFWStoredFile item) {
		if(item != null) {
			return checkExistsByName(item.name());
		}
		return false;
	}
	
	public static boolean checkCanSaveWithName(CFWStoredFile storedfile) {	
		return !CFWDBDefaultOperations.checkExistsByIgnoreSelf(storedfile, CFWStoredFileFields.NAME.toString(), storedfile.name());
	}
	
	/*****************************************************************
	 * Checks if the current user can edit the storedfile.
	 *****************************************************************/
	public static boolean checkCanEdit(String storedfileID) {
		return checkCanEdit(Integer.parseInt(storedfileID));
	}
	/*****************************************************************
	 * Checks if the current user can edit the storedfile.
	 *****************************************************************/
	public static boolean checkCanEdit(int storedfileID) {
		
		CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(storedfileID);
		return checkCanEdit(storedfile);
	}

	
	/*****************************************************************
	 * Checks if the current user can edit the storedfile.
	 *****************************************************************/
	public static boolean checkCanEdit(CFWStoredFile storedfile) {
		User user = CFW.Context.Request.getUser();
		
		//--------------------------------------
		// if user is not logged in / public storedfile
		if(user == null) { return false; }
		
		//--------------------------------------
		// Check User is StoredFile owner, admin
		// or listed in editors
		if( storedfile.foreignKeyOwner().equals(user.id())
		|| ( storedfile.editors() != null && storedfile.editors().containsKey(user.id().toString()) )
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			return true;
		}
		
		//--------------------------------------
		// Check User has Editor Role
		if(storedfile.editorGroups() != null) {
			for(int roleID : CFW.Context.Request.getUserRoles().keySet()) {
				if (storedfile.editorGroups().containsKey(""+roleID)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	/********************************************************************************************
	 * Creates multiple StoredFile in the DB.
	 * @param StoredFile with the values that should be inserted. ID will be set by the Database.
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
	 * Creates multiple StoredFile in the DB.
	 * @param StoredFile with the values that should be inserted. ID will be set by the Database.
	 * @return 
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static String getTagsAsJSON() {
				
		return CFW.JSON.toJSON(getTags().toArray(new String[] {}));
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified storedfile.
	 * @param StoredFile with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWStoredFile... storedfile) {
		
		for(CFWStoredFile credential : storedfile) {
			updateTags(credential);
		}
	}
	
	/********************************************************************************************
	 * Adds the tags to the cache for the specified storedfile.
	 * @param StoredFile with the tags.
	 * @return nothing
	 * 
	 ********************************************************************************************/
	public static void updateTags(CFWStoredFile storedfile) {
		
		if(cachedTags == null) {
			fetchAndCacheTags();
		}
		
		if(storedfile.tags() != null) {
			for(Object tag : storedfile.tags()) {
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
		
		ResultSet resultSet = new CFWSQL(new CFWStoredFile())
			.queryCache()
			.select(CFWStoredFileFields.TAGS.toString())
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
		
		ResultSet resultSet = new CFWSQL(new CFWStoredFile())
			.queryCache()
			.select(CFWStoredFileFields.TAGS.toString())
			.where(CFWStoredFileFields.FK_ID_OWNER.toString(), userID)
			.or(CFWStoredFileFields.IS_SHARED.toString(), true)
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
	static void pushEAVStats(String entityName, String storedfileID, int value) {
		
		Integer userID = CFW.Context.Request.getUserID();
		String userIDString = (userID != null) ? ""+userID : null;
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
		attributes.put(EAV_ATTRIBUTE_STOREDFILEID, storedfileID);
		attributes.put(EAV_ATTRIBUTE_USERID, userIDString);
		
		CFW.DB.EAVStats.pushStatsCounter(FeatureFilemanager.EAV_STATS_CATEGORY, entityName, attributes, value);
		
	}
	
	/***************************************************************
	 * Return a list of all user storedfile as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static JsonArray getEAVStats(String boardID, long earliest, long latest) {
		
		String category = FeatureFilemanager.EAV_STATS_CATEGORY;
		String entity = "%";
		
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		values.put(EAV_ATTRIBUTE_STOREDFILEID, boardID);
		
		JsonArray array = CFWDBEAVStats.fetchStatsAsJsonArray(category, entity, values, earliest, latest);
		
		return array;
	}
		
}

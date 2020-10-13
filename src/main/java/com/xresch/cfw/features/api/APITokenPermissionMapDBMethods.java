package com.xresch.cfw.features.api;

import java.sql.ResultSet;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.APITokenPermissionMap.APITokenPermissionMapFields;
import com.xresch.cfw.features.usermgmt.CFWDBRolePermissionMap;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class APITokenPermissionMapDBMethods {
	
	private static Class<APITokenPermissionMap> cfwObjectClass = APITokenPermissionMap.class;
	
	public static Logger logger = CFWLog.getLogger(APITokenPermissionMapDBMethods.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			APITokenPermissionMap item = (APITokenPermissionMap)object;
			
			if(item == null || item.foreignKeyAPIToken() == null || item.foreignKeyPermission() == null) {
				new CFWLog(logger)
					.warn("Please specify the permission correctly.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(APITokenPermissionMap... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(APITokenPermissionMap item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(APITokenPermissionMap item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(APITokenPermissionMap... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(APITokenPermissionMap item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################	
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, APITokenPermissionMapFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		APITokenPermissionMap item = selectByID(id);
		if(item != null) {
			item.id(null);
			return create(item);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static APITokenPermissionMap selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenPermissionMapFields.PK_ID.toString(), id);
	}
	
	public static APITokenPermissionMap selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenPermissionMapFields.PK_ID.toString(), id);
	}
		
	/***************************************************************
	 * 
	 ****************************************************************/
	public static ResultSet getPermissionsForToken(String token) {
				
		return new CFWSQL(null)
				.queryCache()
				.loadSQLResource(FeatureAPI.RESOURCE_PACKAGE, "sql_getPermissionsForToken.sql", token)
				.getResultSet();

	}
	/***************************************************************
	 * 
	 ****************************************************************/
	public static String getPermissionMapForTokenID(String tokenID) {
				
		return new CFWSQL(null)
				.queryCache()
				.loadSQLResource(FeatureAPI.RESOURCE_PACKAGE, "sql_getPermissionMapForTokenID.sql", tokenID)
				.getAsJSON();

	}
	
	/***************************************************************
	 * 
	 ****************************************************************/
	public static String getPermissionListAsJSON() {
		
		return new CFWSQL(new APITokenPermissionMap())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static int getCount() {
		
		return new CFWSQL(new APITokenPermissionMap())
				.queryCache()
				.selectCount()
				.getCount();
		
	}
	
	
	
	/***************************************************************
	 * Remove a permission from the token or otherwise if it was 
	 * already added.
	 ****************************************************************/
	public static boolean tooglePermissionForToken(String permissionID, String tokenID) {
		
		//----------------------------------
		// Check input format
		if(permissionID == null ^ !permissionID.matches("\\d+")) {
			new CFWLog(logger)
				.severe("The userID '"+permissionID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(tokenID == null ^ !tokenID.matches("\\d+")) {
			new CFWLog(logger)
			.severe("The tokenID '"+permissionID+"' is not a number.");
			return false;
		}
		
		return tooglePermissionForToken(Integer.parseInt(permissionID), Integer.parseInt(tokenID));
		
	}
	
	/***************************************************************
	 * 
	 ****************************************************************/
	public static boolean tooglePermissionForToken(int permissionID, int tokenID) {
		
		if(checkHasTokenThePermission(tokenID, permissionID)) {
			return removePermissionFromAPIToken(permissionID, tokenID);
		}else {
			return addPermissionToAPIToken(permissionID, tokenID);
		}

	}
	
	/********************************************************************************************
	 * Adds the permission to the specified token.
	 * @param permission
	 * @param token
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addPermissionToAPIToken(APITokenPermission permission, APIToken token) {
		
		if(permission == null) {
			new CFWLog(logger)
				.warn("Permission cannot be null.");
			return false;
		}
		
		if(token == null) {
			new CFWLog(logger)
				.warn("APIToken cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || token.id() < 0) {
			new CFWLog(logger)
				.warn("Permission-ID and token-ID are not set correctly.");
			return false;
		}
		
		if(checkHasTokenThePermission(token, permission)) {
			new CFWLog(logger)
			.warn("The permission '"+permission.apiName()+"."+permission.actionName()+"' is already assigned to the token '"+token.token()+"'.");
			return false;
		}
		
		return addPermissionToAPIToken(permission.id(), token.id());
	}
	/********************************************************************************************
	 * Adds the permission to the specified token.
	 * @param permissionID
	 * @param tokenID
	 * @return return true if permission was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addPermissionToAPIToken(int permissionID, int tokenID) {
		
		
		if(permissionID < 0 || tokenID < 0) {
			new CFWLog(logger)
				.warn("Permission-ID or token-ID are not set correctly.");
			return false;
		}
		
		if(checkHasTokenThePermission(tokenID, permissionID)) {
			new CFWLog(logger)
				.warn("The permission '"+permissionID+"' is already part of the token '"+tokenID+"'.");
			return false;
		}
		
		return create(new APITokenPermissionMap()
				.foreignKeyAPIToken(tokenID)
				.foreignKeyPermission(permissionID)
		);

	}
	
	
	/********************************************************************************************
	 * Remove the permission to the specified token.
	 * @param permission
	 * @param token
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removePermissionFromAPIToken(APITokenPermission permission, APIToken token) {
		
		if(permission == null || token == null ) {
			new CFWLog(logger)
				.warn("Permission and token cannot be null.");
			return false;
		}
		
		if(permission.id() < 0 || token.id() < 0) {
			new CFWLog(logger)
				.warn("Permission-ID and Token-ID are not set correctly.");
			return false;
		}
		
		if(!checkHasTokenThePermission(token, permission)) {
			new CFWLog(logger)
				.warn("The permission '"+permission.apiName()+"."+permission.actionName()+"' is not part of the token '"+token.token()+"' and cannot be removed.");
			return false;
		}
		
		return removePermissionFromAPIToken(permission.id(), token.id());
	}
	/********************************************************************************************
	 * Remove a permission from the token.
	 * @param permission
	 * @param token
	 * @return return true if permission was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removePermissionFromAPIToken(int permissionID, int tokenID) {
		
		if(!checkHasTokenThePermission(tokenID, permissionID)) {
			new CFWLog(logger)
				.warn("The permission '"+permissionID+"' is not part of the token '"+ tokenID+"' and cannot be removed.");
			return false;
		}
		
		return new CFWSQL(new APITokenPermissionMap())
			.queryCache()
			.delete()
			.where(APITokenPermissionMapFields.FK_ID_PERMISSION, permissionID)
			.and(APITokenPermissionMapFields.FK_ID_TOKEN, tokenID)
			.executeDelete();
		
	}
	
	
	/****************************************************************
	 * Check if the permission exists by name.
	 * 
	 * @param permission to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkHasTokenThePermission(String token, String apiName, String actionName) {
		
		APIToken apiToken = APITokenDBMethods.selectFirstByToken(token);
		APITokenPermission permission = APITokenPermissionDBMethods.selectFirst(apiName, actionName);

		return checkHasTokenThePermission(apiToken, permission);

	}
	/****************************************************************
	 * Check if the permission is assigned to the given token.
	 * 
	 * @param permission to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkHasTokenThePermission(APIToken token, APITokenPermission permission ) {
		
		if(permission != null && token != null) {
			return checkHasTokenThePermission(token.id(), permission.id());
		}else {
			new CFWLog(logger)
				.severe("The permission and token cannot be null. Permission: "+permission+", APIToken: "+token+".", new Throwable());
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the permission exists by name.
	 * 
	 * @param permission to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkHasTokenThePermission(int tokenid, int permissionid) {
		
		return 0 != new CFWSQL(new APITokenPermissionMap())
			.queryCache()
			.selectCount()
			.where(APITokenPermissionMapFields.FK_ID_PERMISSION, permissionid)
			.and(APITokenPermissionMapFields.FK_ID_TOKEN, tokenid)
			.getCount();

	}
	


		
}

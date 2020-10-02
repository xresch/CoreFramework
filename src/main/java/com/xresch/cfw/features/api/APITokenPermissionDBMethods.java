package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.APIToken.APITokenFields;
import com.xresch.cfw.features.api.APITokenPermission.APITokenPermissionFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class APITokenPermissionDBMethods {
	
	private static Class<APITokenPermission> cfwObjectClass = APITokenPermission.class;
	
	public static Logger logger = CFWLog.getLogger(APITokenPermissionDBMethods.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			APITokenPermission permission = (APITokenPermission)object;
			
			if(permission == null || permission.apiName().isEmpty() || permission.actionName().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a token.", new Throwable());
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
	public static boolean	create(APITokenPermission... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(APITokenPermission item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(APITokenPermission item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	public static boolean oneTimeCreate(APITokenPermission item){
		
		if( !checkExistsByAPI(item.apiName(), item.actionName()) ) {
			return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		}
		else return false;
	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(APITokenPermission... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(APITokenPermission item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################	
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, APITokenPermissionFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		APITokenPermission item = selectByID(id);
		if(item != null) {
			item.id(null);
			return create(item);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static APITokenPermission selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenPermissionFields.PK_ID.toString(), id);
	}
	
	public static APITokenPermission selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenPermissionFields.PK_ID.toString(), id);
	}
	
	public static APITokenPermission selectFirst(String apiName, String actionName) { 
		return (APITokenPermission)new CFWSQL(new APITokenPermission())
				.queryCache()
				.select()
				.where(APITokenPermissionFields.API_NAME, apiName)
				.and(APITokenPermissionFields.ACTION_NAME, actionName)
				.getFirstObject();
	}
		
	
	public static String getTokenPermissionListAsJSON() {
		
		return new CFWSQL(new APITokenPermission())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static int getCount() {
		
		return new CFWSQL(new APITokenPermission())
				.queryCache()
				.selectCount()
				.getCount();
		
	}
	
	public static boolean checkExistsByAPI(String apiName, String actionName) {
		
		return 0 < new CFWSQL(new APITokenPermission())
				.queryCache()
				.selectCount()
				.where(APITokenPermissionFields.API_NAME, apiName)
				.and(APITokenPermissionFields.ACTION_NAME, actionName)
				.getCount();
		
	}
	
	
	
	
	

	


		
}

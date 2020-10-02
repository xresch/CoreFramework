package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.APITokenPermissionMap.APITokenPermissionMapFields;
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
		
	
	public static String getTokenPermissionListAsJSON() {
		
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
	
	
	
	
	

	


		
}

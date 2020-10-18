package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.APIToken.APITokenFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class APITokenDBMethods {
	
	private static Class<APIToken> cfwObjectClass = APIToken.class;
	
	public static Logger logger = CFWLog.getLogger(APITokenDBMethods.class.getName());
	
	private static final String[] auditLogFieldnames = new String[] { APITokenFields.PK_ID.toString(), APITokenFields.TOKEN.toString()};
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			APIToken apiToken = (APIToken)object;
			
			if(apiToken == null || apiToken.token().isEmpty()) {
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
	public static boolean	create(APIToken... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, auditLogFieldnames, items); }
	public static boolean 	create(APIToken item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, auditLogFieldnames, item);}
	public static Integer 	createGetPrimaryKey(APIToken item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(APIToken... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, auditLogFieldnames, items); }
	public static boolean 	update(APIToken item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, auditLogFieldnames,  item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################	
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, auditLogFieldnames, cfwObjectClass, APITokenFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, auditLogFieldnames, cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		APIToken item = selectByID(id);
		if(item != null) {
			item.id(null);
			item.foreignKeyCreator(CFW.Context.Request.getUser().id());
			return create(item);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static APIToken selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenFields.PK_ID.toString(), id);
	}
	
	public static APIToken selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenFields.PK_ID.toString(), id);
	}
	
	public static APIToken selectFirstByToken(String token) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenFields.TOKEN.toString(), token);
	}
	
	
	public static String getTokenListAsJSON() {
		
		return new CFWSQL(new APIToken())
				.queryCache()
				.columnSubquery("CREATED_BY", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_CREATOR")
				.select()
				.getAsJSON();
		
	}
	
	public static int getCount() {
		
		return new CFWSQL(new APIToken())
				.queryCache()
				.selectCount()
				.getCount();
		
	}
	
	public static boolean checkIsTokenActive(String token) {
		
		return 0 < new CFWSQL(new APIToken())
				.queryCache()
				.selectCount()
				.where(APITokenFields.IS_ACTIVE, true)
				.and(APITokenFields.TOKEN, token)
				.getCount();
		
	}
	
	
	
	
	

	


		
}

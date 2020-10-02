package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.google.common.base.Strings;
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
	public static boolean	create(APIToken... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(APIToken item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(APIToken item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(APIToken... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(APIToken item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################	
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, APITokenFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		APIToken person = selectByID(id);
		if(person != null) {
			person.id(null);
			return create(person);
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
	
	public static APIToken selectFirstByToken(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, APITokenFields.TOKEN.toString(), name);
	}
	
	
	
	public static String getTokenListAsJSON() {
		
		return new CFWSQL(new APIToken())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static int getCount() {
		
		return new CFWSQL(new APIToken())
				.queryCache()
				.selectCount()
				.getCount();
		
	}
	
	
	
	
	

	


		
}

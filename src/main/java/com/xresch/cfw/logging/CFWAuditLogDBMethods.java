package com.xresch.cfw.logging;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogFields;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWAuditLogDBMethods {
	
	private static Class<CFWAuditLog> cfwObjectClass = CFWAuditLog.class;
	
	public static Logger logger = CFWLog.getLogger(CFWAuditLogDBMethods.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWAuditLog Person = (CFWAuditLog)object;
			
//			if(Person == null || Person.firstname().isEmpty()) {
//				new CFWLog(logger)
//					.warn("Please specify a firstname for the person.", new Throwable());
//				return false;
//			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			CFWAuditLog Person = (CFWAuditLog)object;
			
//			if(Person != null && Person.likesTiramisu() == true) {
//				new CFWLog(logger)
//				.severe("The Person '"+Person.firstname()+"' cannot be deleted as people that like tiramisu will prevail for all eternity!", new Throwable());
//				return false;
//			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(CFWAuditLog... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(CFWAuditLog item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(CFWAuditLog item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWAuditLog... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(CFWAuditLog item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, CFWAuditLogFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		CFWAuditLog person = selectByID(id);
		if(person != null) {
			person.id(null);
			return create(person);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWAuditLog selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.PK_ID.toString(), id);
	}
	
	public static CFWAuditLog selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.PK_ID.toString(), id);
	}
	
	public static CFWAuditLog selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.ACTION.toString(), name);
	}
	
	
	
	public static String getPersonListAsJSON() {
		
		return new CFWSQL(new CFWAuditLog())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static String getPartialPersonListAsJSON(String pageSize, String pageNumber, String filterquery, String sortby, boolean sortAscending) {
		return getPartialPersonListAsJSON(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, sortAscending);
	}
	
	
	public static String getPartialPersonListAsJSON(int pageSize, int pageNumber, String filterquery, String sortby, boolean sortAscending) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the Person Object

		//Do not cache this statement
		return new CFWSQL(new CFWAuditLog())
				.fulltextSearchLucene(filterquery, sortby, sortAscending, pageSize, pageNumber)
				.getAsJSON();
		
		
		//===========================================
		// Manual Alternative
		//===========================================
		
//		if(Strings.isNullOrEmpty(filterquery)) {
//			//-------------------------------------
//			// Unfiltered
//			return new CFWSQL(new Person())
//				.queryCache()
//				.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
//				.select()
//				.limit(pageSize)
//				.offset(pageSize*(pageNumber-1))
//				.getAsJSON();
//		}else {
//			//-------------------------------------
//			// Filter with fulltext search
//			// Enabled by CFWObject.enableFulltextSearch()
//			// on the Person Object
//			return new CFWSQL(new Person())
//					.queryCache()
//					.select()
//					.fulltextSearch()
//						.custom(filterquery)
//						.build(pageSize, pageNumber)
//					.getAsJSON();
//		}
		
	}
	
	public static int getCount() {
		
		return new CFWAuditLog()
				.queryCache(CFWAuditLogDBMethods.class, "getCount")
				.selectCount()
				.getCount();
		
	}
	
	
	
	
	

	


		
}

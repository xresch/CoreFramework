package com.xresch.cfw.features.query.database;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.query.database.CFWQueryHistory.CFWQueryHistoryFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWDBQueryHistory {
	
	private static Class<CFWQueryHistory> cfwObjectClass = CFWQueryHistory.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBQueryHistory.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWQueryHistory QueryHistory = (CFWQueryHistory)object;
			
			if(QueryHistory == null || QueryHistory.query().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a firstname for the person.", new Throwable());
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
	public static boolean	create(CFWQueryHistory... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(CFWQueryHistory item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(CFWQueryHistory item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, CFWQueryHistoryFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		CFWQueryHistory person = selectByID(id);
		if(person != null) {
			person.id(null);
			return create(person);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWQueryHistory selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWQueryHistoryFields.PK_ID.toString(), id);
	}
	
	public static CFWQueryHistory selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWQueryHistoryFields.PK_ID.toString(), id);
	}
	
	public static String getQueryHistoryListAsJSON() {
		
		return new CFWSQL(new CFWQueryHistory())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static String getPartialQueryHistoryListAsJSON(String pageSize, String pageNumber, String filterquery, String sortby, boolean sortAscending) {
		return getPartialQueryHistoryListAsJSON(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, sortAscending);
	}
	
	
	public static String getPartialQueryHistoryListAsJSON(int pageSize, int pageNumber, String filterquery, String sortby, boolean sortAscending) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the QueryHistory Object

		//Do not cache this statement
		return new CFWSQL(new CFWQueryHistory())
				.fulltextSearchLucene(filterquery, sortby, sortAscending, pageSize, pageNumber)
				.getAsJSON();
		
		
		//===========================================
		// Manual Alternative
		//===========================================
		
//		if(Strings.isNullOrEmpty(filterquery)) {
//			//-------------------------------------
//			// Unfiltered
//			return new CFWSQL(new QueryHistory())
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
//			// on the QueryHistory Object
//			return new CFWSQL(new QueryHistory())
//					.queryCache()
//					.select()
//					.fulltextSearch()
//						.custom(filterquery)
//						.build(pageSize, pageNumber)
//					.getAsJSON();
//		}
		
	}
	
	public static int getCount() {
		
		return new CFWQueryHistory()
				.queryCache(CFWDBQueryHistory.class, "getCount")
				.selectCount()
				.executeCount();
		
	}
	
	
	
	
	

	


		
}

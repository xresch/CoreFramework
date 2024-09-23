package com.xresch.cfw.features.query.database;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.query.FeatureQuery;
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
	
	/********************************************************
	 * 
	 ********************************************************/
	public static CFWQueryHistory selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWQueryHistoryFields.PK_ID.toString(), id);
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	public static String getHistoryForUserAsJSON() {
		
		int historyLimit = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_CATEGORY, FeatureQuery.CONFIG_QUERY_HISTORY_LIMIT);
		
		return new CFWSQL(new CFWQueryHistory())
				.queryCache()
				.select()
				.where(CFWQueryHistoryFields.FK_ID_USER, CFW.Context.Request.getUserID())
				.orderbyDesc(CFWQueryHistoryFields.TIME)
				.limit(historyLimit)
				.getAsJSON();
		
	}
	
	
	/********************************************************
	 * 
	 ********************************************************/
	public static int getCount() {
		
		return new CFWQueryHistory()
				.queryCache(CFWDBQueryHistory.class, "getCount")
				.selectCount()
				.executeCount();
	}
	
	
	/********************************************************
	 * Removes the oldest entries.
	 * @param amount of records to keep
	 ********************************************************/
	public static void removeOldestEntries(int keepCount) {
		ArrayList<Integer> userIDList = new CFWSQL(new CFWQueryHistory())
				.distinct()
				.select(CFWQueryHistoryFields.FK_ID_USER)
				.getAsIntegerArrayList(CFWQueryHistoryFields.FK_ID_USER);
		
		for(Integer userID : userIDList) {
			
			new CFWSQL(null)
			.custom(
				   "DELETE FROM CFW_QUERY_HISTORY H\r\n"
				 + "WHERE H.PK_ID IN (\r\n"
				 + "	SELECT PK_ID FROM CFW_QUERY_HISTORY\r\n"
				 + "	WHERE FK_ID_USER = ?\r\n"
				 + "	ORDER BY TIME DESC\r\n"
				 + "	OFFSET ?"
				 + ")"
			, userID
			, keepCount)
			.executeDelete()
			;
			
		}
	}
		
}

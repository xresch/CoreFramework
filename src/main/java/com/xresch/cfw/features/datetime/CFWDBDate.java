package com.xresch.cfw.features.datetime;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.datetime.CFWDate.CFWDateFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBDate {
	
	private static Class<CFWDate> cfwObjectClass = CFWDate.class;		
	
	public static Logger logger = CFWLog.getLogger(CFWDBDate.class.getName());
	
	private static ArrayList<String> dateIDCache = new ArrayList<>(); 
	
	// dayID and scopeAndDayMap
	private static Cache<Integer, LinkedHashMap<String,Integer> > scopeAndDayCache = CFW.Caching.addCache("CFW Date: ScopeAndDayMap", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(1000)
				.expireAfterAccess(25, TimeUnit.HOURS)
			);
	
	//####################################################################################################
	// Cache Management
	//####################################################################################################
	public static void reloadCache() {
		dateIDCache = new CFWSQL(new CFWDate())
			.select(CFWDateFields.PK_ID)
			.getAsStringArrayList(CFWDateFields.PK_ID);
				
	}
	
	//####################################################################################################
	// Cache Management
	//####################################################################################################
	public static void addtoCache(String id) {
		dateIDCache.add(id);
	}
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
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
	protected static boolean create(CFWDate item) 		{ 
		
		boolean result = CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		
		if(result == true) {
			addtoCache(item.id()+"");
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * Creates a new date if it not already exists
	 * @param dateObject with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	protected static boolean oneTimeCreate(int dateID) {
		return oneTimeCreate(CFWDate.newDate(dateID));
	}
	/********************************************************************************************
	 * Creates a new date if it not already exists
	 * @param dateObject with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	protected static boolean oneTimeCreate(String dateID) {
		return oneTimeCreate(CFWDate.newDate(dateID));
	}
	
	/********************************************************************************************
	 * Creates a new date if it not already exists
	 * @param dateObject with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(CFWDate dateObject) {
		
		if(dateObject == null || dateObject.id() == null) {
			return false;
		}
		
		boolean result = true; 
		if(!checkExistsByID(dateObject.id())) {
			
			result &= create(dateObject);
			
		}
		
		return result;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	// public static boolean 	update(CFWDate... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	// public static boolean 	update(CFWDate item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	// public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, CFWDateFields.PK_ID.toString(), id); }
	// public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWDate selectByID(String id ) {
		oneTimeCreate(id);
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWDateFields.PK_ID.toString(), id);
	}
	
	public static CFWDate selectByID(int id ) {
		oneTimeCreate(id+"");
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWDateFields.PK_ID.toString(), id);
	}
	
	/*****************************************************************************
	 * For the specified dateID, select the last day for the Week, Month, Quarter
	 * and Year of the day represented by the dayID.
	 * @returns map with Keys WEEK,MONTH,QUARTER,YEAR and dayID as values, returns null on error
	 *****************************************************************************/
	public static LinkedHashMap<String, Integer> selectLastDaysForAggregation(int id) {
		
		LinkedHashMap<String, Integer> scopeAndDayID = null;
		try {
			scopeAndDayID = 
				scopeAndDayCache.get(id, new Callable<LinkedHashMap<String, Integer>>() {
					@Override
					public LinkedHashMap<String, Integer> call() throws Exception {
						
						//------------------------------
						// Get Values
						ResultSet result = new CFWSQL(null).loadSQLResource(
										FeatureDateTime.PACKAGE_RESOURCE
										, "sql_selectLastDaysForAggregation.sql"
										, id, id, id, id, id, id, id
									).getResultSet();
						
						//------------------------------
						// Create Map
						LinkedHashMap<String, Integer> scopeDayMap = new LinkedHashMap<String, Integer>();
						 while(result.next()) {
							 scopeDayMap.put(
									   result.getString("SCOPE")
									 , result.getInt(CFWDateFields.PK_ID.toString()) 
									);
						 }
						 
						//------------------------------
						// Close and Return
						CFW.DB.close(result);
						
						return scopeDayMap;
	
					}
				});
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while loading last days for aggregation: "+e.getMessage(), e);
		}
		
		return scopeAndDayID;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExistsByID(String id) {
		return dateIDCache.contains(id);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExistsByID(int id) {
		return dateIDCache.contains(id+"");
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new CFWDate())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	
}

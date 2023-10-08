package com.xresch.cfw.features.datetime;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.datetime.CFWDate.CFWDateFields;
import com.xresch.cfw.features.datetime.CFWDate.CFWDateFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBDate {
	
	private static Class<CFWDate> cfwObjectClass = CFWDate.class;		
	
	public static Logger logger = CFWLog.getLogger(CFWDBDate.class.getName());
	
	private static ArrayList<String> dateIDCache = new ArrayList<>(); 
	
	
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
	private static boolean 	create(CFWDate item) 		{ 
		
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
	public static boolean oneTimeCreate(int dateID) {
		return oneTimeCreate(new CFWDate(dateID));
	}
	/********************************************************************************************
	 * Creates a new date if it not already exists
	 * @param dateObject with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(String dateID) {
		return oneTimeCreate(new CFWDate(dateID));
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

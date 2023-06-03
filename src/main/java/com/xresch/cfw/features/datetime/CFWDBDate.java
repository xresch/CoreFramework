package com.xresch.cfw.features.datetime;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.datetime.CFWDate.OMDatapointDateFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDBDate {
	
	private static Class<CFWDate> cfwObjectClass = CFWDate.class;		
	
	public static Logger logger = CFWLog.getLogger(CFWDBDate.class.getName());
	
	
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
	private static boolean 	create(CFWDate item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(CFWDate item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	/********************************************************************************************
	 * Creates a new aggregation in the DB if the name was not already given.
	 * All newly created permissions are by default assigned to the Superuser Role.
	 * 
	 * @param dateObject with the values that should be inserted. ID should be set by the user.
	 * @return true if successful, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(CFWDate dateObject) {
		
		if(dateObject == null) {
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
	public static boolean 	update(CFWDate... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(CFWDate item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, OMDatapointDateFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWDate selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, OMDatapointDateFields.PK_ID.toString(), id);
	}
	
	public static CFWDate selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, OMDatapointDateFields.PK_ID.toString(), id);
	}
	
	public static CFWDate selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, OMDatapointDateFields.DATE.toString(), name);
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExistsByID(int id) {
		
		return 0 < new CFWSQL(new CFWDate())
				.queryCache()
				.selectCount()
				.where(OMDatapointDateFields.PK_ID, id)
				.executeCount();
		
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

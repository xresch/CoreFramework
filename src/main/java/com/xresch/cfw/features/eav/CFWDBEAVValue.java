package com.xresch.cfw.features.eav;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.eav.EAVValue.EAVValueFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVValue {

	private static Class<EAVValue> cfwObjectClass = EAVValue.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVValue.class.getName());
	

	
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
	private static Integer 	createGetPrimaryKey(EAVValue item) 		{ 
		
		return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);

	}

	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param category the category of the attribute
	 * @param attributeName the name of the attribute to create
	 * @return id if created, null otherwise
	 * 
	 ********************************************************************************************/
	public static Integer oneTimeCreate(int entityID, int attributeID, String value) {
		return oneTimeCreate(new EAVValue(entityID, attributeID, value));
	}
	
	/********************************************************************************************
	 * Creates a new value if it not already exists
	 * @param value with the values that should be inserted. ID should be set by the user.
	 * @return id if created, null otherwise
	 * 
	 ********************************************************************************************/
	public static Integer oneTimeCreate(EAVValue value) {
		
		if(value == null) {
			return null;
		}
		
		boolean result = true; 
		if( !checkExists(value.foreignKeyEntity(), value.foreignKeyAttribute(), value.value()) ) {
			
			return createGetPrimaryKey(value);
			
		}
		
		return null;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVValue item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVValueFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVValue selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVValueFields.PK_ID.toString(), id);
	}
	
	public static EAVValue selectByID(int id) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVValueFields.PK_ID.toString(), id);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVValue selecFirstBy(int entityID, int attributeID, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeID, null);
		}
		
		return (EAVValue)new CFWSQL(new EAVValue())
					.select()
					.where(EAVValueFields.FK_ID_ENTITY, entityID)
					.and(EAVValueFields.FK_ID_ATTR, attributeID)
					.getFirstAsObject()
					;
				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVValue selecFirstBy(int entityID, int attributeID, String value, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeID, value);
		}
		
		return (EAVValue)new CFWSQL(new EAVValue())
					.select()
					.where(EAVValueFields.FK_ID_ENTITY, entityID)
					.and(EAVValueFields.FK_ID_ATTR, attributeID)
					.and(EAVValueFields.VALUE, value)
					.getFirstAsObject()
					;
				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(int entityID, int attributeID, String value) {
		return checkExists(""+entityID, ""+attributeID, value);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(String entityID, String attributeID, String value) {
		
		return 0 < new CFWSQL(new EAVValue())
				.queryCache()
				.selectCount()
				.where(EAVValueFields.FK_ID_ENTITY, entityID)
				.and(EAVValueFields.FK_ID_ATTR, attributeID)
				.and(EAVValueFields.VALUE, value)
				.executeCount();
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVValue())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}

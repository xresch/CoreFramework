package com.xresch.cfw.features.eav;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.eav.EAVAttribute.EAVAttributeFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVAttribute {

	private static Class<EAVAttribute> cfwObjectClass = EAVAttribute.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVAttribute.class.getName());
	

	
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
	private static boolean 	create(EAVAttribute item) 		{ 
		
		boolean result = CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		
		return result;
	}

	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param category the category of the attribute
	 * @param attributeName the name of the attribute to create
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(String entityID, String attributeName) {
		return oneTimeCreate(new EAVAttribute(entityID, attributeName));
	}
	
	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param attribute with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(EAVAttribute attribute) {
		
		if(attribute == null) {
			return false;
		}
		
		boolean result = true; 
		if( !checkExists(attribute.foreignKeyEntity(), attribute.name()) ) {
			
			result &= create(attribute);
			
		}
		
		return result;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVAttribute item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVAttributeFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVAttribute selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVAttributeFields.PK_ID.toString(), id);
	}
	
	public static EAVAttribute selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVAttributeFields.PK_ID.toString(), id);
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVAttribute selecFirstBy(String entityID, String attributeName, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeName);
		}
		
		return (EAVAttribute)new CFWSQL(new EAVAttribute())
					.select()
					.where(EAVAttributeFields.FK_ID_ENTITY, entityID)
					.and(EAVAttributeFields.NAME, attributeName)
					.getFirstAsObject()
					;
				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(int entityID, String attributeName) {
		return checkExists(""+entityID, attributeName);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(String entityID, String attributeName) {
		
		return 0 < new CFWSQL(new EAVAttribute())
				.queryCache()
				.selectCount()
				.where(EAVAttributeFields.FK_ID_ENTITY, entityID)
				.and(EAVAttributeFields.NAME, attributeName)
				.executeCount();
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVAttribute())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}

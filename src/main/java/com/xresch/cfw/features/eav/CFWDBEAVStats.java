package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.eav.EAVStats.EAVStatsFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVStats {

	private static Class<EAVStats> cfwObjectClass = EAVStats.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVStats.class.getName());
	
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
	private static Integer createGetPrimaryKey(EAVStats item) 		{ 
		
		return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);

	}

	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param category the category of the attribute
	 * @param attributeName the name of the attribute to create
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
//	public static boolean oneTimeCreate(int entityID, int attributeID, String value) {
//		return oneTimeCreate(new EAVStats(entityID, attributeID));
//	}
	
	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param attribute with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
//	public static boolean oneTimeCreate(EAVStats attribute) {
//		
//		if(attribute == null) {
//			return false;
//		}
//		
//		boolean result = true; 
//		if( !checkExists(attribute.foreignKeyEntity(), attribute.foreignKeyAttribute()) ) {
//			
//			result &= create(attribute);
//			
//		}
//		
//		return result;
//	}
	
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static boolean pushStats(
			String category
			, String entityName
			, LinkedHashMap<String,String> attributes
			, BigDecimal statsValue) {
		
		boolean result = true;
		
		EAVEntity entity = CFW.DB.EAVEntity.selecFirstBy(category, entityName, true);
		
		TreeSet<Integer> valueIDs = new TreeSet<>();
		
		for(Entry<String, String> current : attributes.entrySet()) {
			String attributeName = current.getKey();
			String attributeValue = current.getValue();
			
			EAVAttribute attribute = CFW.DB.EAVAttribute.selecFirstBy(entity.id(), attributeName, true);
			EAVValue value = CFW.DB.EAVValue.selecFirstBy(entity.id(), attribute.id(), attributeValue, true); 
			valueIDs.add(value.id());
		}
		
		EAVStats stats = new EAVStats(entity.id(), valueIDs)
				.granularity(0)
				.addValue(statsValue)
				.calculateStatistics(getCount())
				;
		
		createGetPrimaryKey(stats);
		
		return result;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVStats item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVStatsFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVStats selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVStatsFields.PK_ID.toString(), id);
	}
	
	public static EAVStats selectByID(int id) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVStatsFields.PK_ID.toString(), id);
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
//	public static EAVStats selecFirstBy(String entityID, String attributeID, boolean createIfNotExists) {
//				
//		return (EAVStats)new CFWSQL(new EAVStats())
//					.select()
//					.where(EAVStatsFields.FK_ID_ENTITY, entityID)
//					.and(EAVStatsFields.FK_ID_VALUES, attributeID)
//					.getFirstAsObject()
//					;
//				
//	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
//	public static boolean checkExists(int entityID, int attributeID) {
//		return checkExists(""+entityID, ""+attributeID);
//	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
//	public static boolean checkExists(String entityID, String attributeID) {
//		
//		return 0 < new CFWSQL(new EAVStats())
//				.queryCache()
//				.selectCount()
//				.where(EAVStatsFields.FK_ID_ENTITY, entityID)
//				.and(EAVStatsFields.FK_ID_ATTR, attributeID)
//				.executeCount();
//		
//	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVStats())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}

package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.eav.EAVStats.EAVStatsFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVStats {

	private static Class<EAVStats> cfwObjectClass = EAVStats.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVStats.class.getName());
	
	private static HashMap<String, EAVStats> eavStatsToBeStored = new HashMap<>();
	
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
		
		String storeKey = entity.id()+"_"+Joiner.on(",").join(valueIDs);
		System.out.println(storeKey);
		synchronized (eavStatsToBeStored) {
			EAVStats stats;
			if(eavStatsToBeStored.containsKey(storeKey)) {
				stats = eavStatsToBeStored.get(storeKey);
				stats.addValue(statsValue);
			}else {
				stats = new EAVStats(entity.id(), valueIDs)
						.addValue(statsValue)
						;
				eavStatsToBeStored.put(storeKey, stats);
			}
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	protected static void storeStatsToDB(int granularityMinutes) {
				
		long currentTimeRounded = CFWTimeUnit.m.round(System.currentTimeMillis(), granularityMinutes);
		synchronized (eavStatsToBeStored) {
			for(Entry<String, EAVStats> current : eavStatsToBeStored.entrySet()) {
				
				EAVStats stats = current.getValue();
				
				stats.granularity(granularityMinutes)
					.time(currentTimeRounded)
					.calculateStatistics();
				
				createGetPrimaryKey(stats);

			}
			eavStatsToBeStored.clear();
		}
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
	public static int getCount() {
		
		return new CFWSQL(new EAVStats())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}

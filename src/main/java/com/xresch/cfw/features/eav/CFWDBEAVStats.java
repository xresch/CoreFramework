package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
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
	
	
	/***************************************************************
	 * Get the timestamp of the oldest record that has a ganularity lower
	 * than the one specified by the parameter.
	 * @param granularity
	 * @return timestamp
	 ****************************************************************/
	public static Timestamp getOldestAgedRecord(int granularity, Timestamp ageOutTime  ) {

		EAVStats first =(EAVStats)new EAVStats()
				.queryCache(CFWDBEAVStats.class, "getOldestAgedRecord")
				.select()
				.custom("WHERE GRANULARITY < ?", granularity)
				.custom("AND TIME <= ?", ageOutTime)
				.orderby(EAVStatsFields.TIME.toString())
				.limit(1)
				.getFirstAsObject();
		
		if(first != null) {
			return first.time();
		}else {
			return null;
		}
		
		
	}
	
	/***************************************************************
	 * Get the timestamp of the oldest record that has a ganularity lower
	 * than the one specified by the parameter.
	 * @param granularity
	 * @return timestamp
	 ****************************************************************/
	public static Timestamp getYoungestAgedRecord(int granularity, Timestamp ageOutTime ) {

		EAVStats first =(EAVStats)new EAVStats()
				.queryCache(CFWDBEAVStats.class, "getYoungestAgedRecord")
				.select()
				.custom(" WHERE GRANULARITY < ? ", granularity)
				.custom(" AND TIME <= ? ", ageOutTime)
				.orderbyDesc(EAVStatsFields.TIME.toString())
				.limit(1)
				.getFirstAsObject();
		
		if(first != null) {
			return first.time();
		}else {
			return null;
		}
		
	}
	
	/***************************************************************
	 * Aggregates the statistics in the given timeframe.
	 * 
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public static boolean aggregateStatistics(Timestamp startTime, Timestamp endTime, int newGranularity) {
				
		CFWDB.transactionStart();
		boolean success = true;
		int cacheCounter = 0;
		
		//--------------------------------------------
		// Check if there is anything to aggregate
		int count =  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.selectCount() 
				.custom(" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY < ?;"
						 ,startTime, endTime, newGranularity)
				.executeCount();

		if(count == 0) {
			CFWDB.transactionRollback();
			return true;
		}
		
		//--------------------------------------------
		// Start Aggregation
		new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("CREATE TEMP TABLE" + 
						" IF NOT EXISTS CFW_EAV_STATS_AGGREGATION" + 
						" (TIME TIMESTAMP, FK_ID_ENTITY INT, FK_ID_VALUES NUMERIC ARRAY, COUNT NUMERIC, MIN NUMERIC, AVG NUMERIC, MAX NUMERIC, SUM NUMERIC, GRANULARITY INT);")
				.execute();
		
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("INSERT INTO CFW_EAV_STATS_AGGREGATION" + 
						" SELECT (" + 
						"		DATEADD(" + 
						"			SECOND," + 
						"			DATEDIFF(SECOND, MIN(TIME), MAX(TIME)) / 2," + 
						"			MIN(TIME)" + 
						"		)) AS NewTime," + 
						" FK_ID_ENTITY, FK_ID_VALUES, SUM(COUNT), MIN(MIN), AVG(AVG), MAX(MAX), SUM(SUM), ?"+ 
						" FROM CFW_EAV_STATS" + 
						" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY <  ?" + 
						" GROUP BY FK_ID_ENTITY, FK_ID_VALUES;"
						,newGranularity ,startTime, endTime, newGranularity)
				.execute();
		
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM CFW_EAV_STATS" + 
						" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY < ?;"
						 ,startTime, endTime, newGranularity)
				.execute();
		
		
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom(" INSERT INTO CFW_EAV_STATS (TIME, FK_ID_ENTITY, FK_ID_VALUES,  COUNT, MIN, AVG, MAX, SUM, GRANULARITY)" + 
						" SELECT * FROM CFW_EAV_STATS_AGGREGATION;")
				.execute();
		
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM CFW_EAV_STATS_AGGREGATION;\r\n")
				.execute();
		
		CFW.DB.transactionEnd(success);
		
		
		return success;
	}
	

	
		
}

package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.datetime.CFWDate;
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;
import com.xresch.cfw.features.eav.EAVStats.EAVStatsFields;
import com.xresch.cfw.features.eav.EAVStats.EAVStatsType;
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
	private static final String TEMP_TABLE_AGGREGATION = "CFW_EAV_STATS_AGGREGATION";
	
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
	public static boolean pushStatsCounter(
			  String category
			, String entityName
			, LinkedHashMap<String,String> attributes
			, int increaseCounterBy) {
		
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
		
		String storeKey = "COUNTER_"+entity.id()+"_"+Joiner.on(",").join(valueIDs);

		synchronized (eavStatsToBeStored) {
			EAVStats stats;
			if(eavStatsToBeStored.containsKey(storeKey)) {
				stats = eavStatsToBeStored.get(storeKey);
				stats.increaseCounter(increaseCounterBy);
			}else {
				stats = new EAVStats(entity.id(), valueIDs, EAVStatsType.COUNTER)
						.increaseCounter(increaseCounterBy)
						;
				eavStatsToBeStored.put(storeKey, stats);
			}
		}
		
		return result;
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static boolean pushStatsValue(
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
		
		String storeKey = "VALUES_"+entity.id()+"_"+Joiner.on(",").join(valueIDs);

		synchronized (eavStatsToBeStored) {
			EAVStats stats;
			if(eavStatsToBeStored.containsKey(storeKey)) {
				stats = eavStatsToBeStored.get(storeKey);
				stats.addValue(statsValue);
			}else {
				stats = new EAVStats(entity.id(), valueIDs, EAVStatsType.VALUES)
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
	public static JsonArray fetchStatsAsJsonArray(
			  String category
			, String entityName
			, LinkedHashMap<String,String> attributes
			, long earliest
			, long latest
			) {
		
		JsonArray result = new JsonArray();
		
		EAVEntity entity = CFW.DB.EAVEntity.selecFirstBy(category, entityName, true);
		
		TreeSet<Integer> valueIDs = new TreeSet<>();
		
		for(Entry<String, String> current : attributes.entrySet()) {
			String attributeName = current.getKey();
			String attributeValue = current.getValue();
			
			EAVAttribute attribute = CFW.DB.EAVAttribute.selecFirstBy(entity.id(), attributeName, true);
			EAVValue value = CFW.DB.EAVValue.selecFirstBy(entity.id(), attribute.id(), attributeValue, true); 
			valueIDs.add(value.id());
		}
		
		result = new CFWSQL(new EAVStats())
				.select()
				.where(EAVStatsFields.FK_ID_ENTITY, entity.id())
				.and(EAVStatsFields.FK_ID_VALUES, valueIDs.toArray(new Integer[] {}))
				.and().custom(" TIME >= ?", new Timestamp(earliest) )
				.and().custom(" TIME <= ?", new Timestamp(latest) )
				.dump()
				.getAsJSONArray()
				;
		
		//---------------------------------------
		// Unnest Values
		for(int i = 0; i < result.size(); i++) {
			JsonObject current = result.get(i).getAsJsonObject();
			
			int fkidEntity =  current.get(EAVStatsFields.FK_ID_ENTITY.toString()).getAsInt();
			JsonArray fkidValues =  current.get(EAVStatsFields.FK_ID_VALUES.toString()).getAsJsonArray();
			
			EAVEntity currentEntity = CFW.DB.EAVEntity.selectByID(fkidEntity);
			
			current.remove("PK_ID");
					

			current.addProperty("CATEGORY", currentEntity.category());
			current.addProperty("ENTITY", currentEntity.name());
			
			JsonObject attributesList = new JsonObject();
			for(int k = 0; k < fkidValues.size(); k++) {
				int valueID = fkidValues.get(k).getAsInt();
				EAVValue eavValue = CFW.DB.EAVValue.selectByID(valueID);
				EAVAttribute attribute = CFW.DB.EAVAttribute.selectByID(eavValue.foreignKeyAttribute());
				attributesList.addProperty(attribute.name(), eavValue.value());
				//current.addProperty(attribute.name().toUpperCase(), eavValue.value());
			}
			current.add("ATTRIBUTES", attributesList);
			
			current.add(EAVStatsFields.TIME.name(), 
				    current.remove(EAVStatsFields.TIME.name())
				);
			
			current.add(EAVStatsFields.COUNT.name(), 
			    	current.remove(EAVStatsFields.COUNT.name())
			    );
			
			current.add(EAVStatsFields.MIN.name(), 
			    	current.remove(EAVStatsFields.MIN.name())
			    );
			
			current.add(EAVStatsFields.AVG.name(), 
					current.remove(EAVStatsFields.AVG.name())
					);
			
			current.add(EAVStatsFields.MAX.name(), 
					current.remove(EAVStatsFields.MAX.name())
					);
			
			current.add(EAVStatsFields.SUM.name(), 
					current.remove(EAVStatsFields.SUM.name())
					);
			
			current.add(EAVStatsFields.GRANULARITY.name(), 
					current.remove(EAVStatsFields.GRANULARITY.name())
					);
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
		// Create Temp Table
		new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("CREATE TEMP TABLE" + 
						" IF NOT EXISTS " 
						+ TEMP_TABLE_AGGREGATION 
						+" (TIME TIMESTAMP, FK_ID_DATE INT, FK_ID_ENTITY INT, FK_ID_VALUES NUMERIC ARRAY, COUNT NUMERIC, MIN NUMERIC, AVG NUMERIC, MAX NUMERIC, SUM NUMERIC, GRANULARITY INT);")
				.execute();
		
		//--------------------------------------------
		// Aggregate Statistics in Temp Table
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.loadSQLResource(FeatureEAV.PACKAGE_RESOURCE, "sql_createTempAggregatedStatistics.sql"
						, newGranularity
						, startTime
						, endTime
						, newGranularity)
				.execute();
		
		//--------------------------------------------
		// Get Aggregated Records with missing FK_ID_DATE
		 ResultSet noDateRecords = new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("SELECT TIME FROM "+TEMP_TABLE_AGGREGATION+" T")
				.where().isNull("FK_ID_DATE")
				.getResultSet();
		 
		try {
			while(noDateRecords.next()) {
				Timestamp time = noDateRecords.getTimestamp("TIME");
				CFWDate date = CFWDate.newDate(time.getTime());
				boolean isSuccess = new CFWSQL(null)
					.custom("UPDATE "+TEMP_TABLE_AGGREGATION+" SET FK_ID_DATE=? WHERE TIME=?"
							, date.id()
							, time
							)
					.execute();
				if(!isSuccess) {
					CFW.DB.transactionRollback();
					return false;
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger).severe("EAV Aggregation: Error while handling missing dates: ",e);
			CFW.DB.transactionRollback();
			return false;
			
		}
		//--------------------------------------------
		// Delete Old Stats in CFW_EAV_STATS
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM CFW_EAV_STATS" + 
						" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY < ?;"
						 ,startTime, endTime, newGranularity)
				.execute();
		

		
		//--------------------------------------------
		// Move Temp Stats to EAVTable
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom(" INSERT INTO CFW_EAV_STATS (TIME, FK_ID_DATE, FK_ID_ENTITY, FK_ID_VALUES, COUNT, MIN, AVG, MAX, SUM, GRANULARITY)" + 
						" SELECT * FROM "+TEMP_TABLE_AGGREGATION+";")
				.execute();
		
		success &=  new EAVStats()
				.queryCache(CFWDBEAVStats.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM "+TEMP_TABLE_AGGREGATION+";\r\n")
				.execute();
		
		CFW.DB.transactionEnd(success);
		
		
		return success;
	}
	

	
		
}

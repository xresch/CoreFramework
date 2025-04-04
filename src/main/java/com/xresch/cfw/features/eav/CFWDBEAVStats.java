package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.datetime.CFWDate;
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
	 * Will combine all the stats given 
	 ********************************************************************************************/
	public static boolean pushStatsCustom(
			  String category
			, String entityName
			, LinkedHashMap<String,String> attributes
			, int count
			, BigDecimal min
			, BigDecimal avg
			, BigDecimal max
			, BigDecimal sum
			, BigDecimal p25
			, BigDecimal p50
			, BigDecimal p75
			, BigDecimal p95
		) {
		
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
		
		String storeKey = "CUSTOM_"+entity.id()+"_"+Joiner.on(",").join(valueIDs);

		synchronized (eavStatsToBeStored) {
			
			EAVStats stats;
			if(eavStatsToBeStored.containsKey(storeKey)) {
				stats = eavStatsToBeStored.get(storeKey);
			}else {
				stats = new EAVStats(entity.id(), valueIDs, EAVStatsType.CUSTOM);		
				eavStatsToBeStored.put(storeKey, stats);
			}
			
			stats.addStatisticsCustom(
					  count
					, min
					, avg
					, max
					, sum
					, p25
					, p50
					, p75
					, p95
				);
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
		return fetchStatsAsJsonArray(category, entityName, attributes, earliest, latest, false);
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
				, boolean fetchAllAttributes
			) {
			
		JsonArray resultAll = new JsonArray();
		
		ArrayList<EAVEntity> entityList = CFW.DB.EAVEntity.selectLike(category, entityName);
		
		//--------------------------------
		// Iterate Entities
		outer:
		for(EAVEntity entity : entityList) {
			TreeSet<Integer> valueIDs = new TreeSet<>();
			
			//--------------------------------
			// Iterate Filtering Attributes 
			CFWSQL partialSQL = new CFWSQL(null);
			inner:
			for(Entry<String, String> current : attributes.entrySet()) {
				String attributeName = current.getKey();
				String attributeValue = current.getValue();
				
				//--------------------------------
				// Ignore '%', performance improvement
				if(attributeValue != null && attributeValue.equals("%")) { 
					continue inner; 
				}
				
				//--------------------------------
				// Get Attribute ID
				EAVAttribute attribute = CFW.DB.EAVAttribute.selecFirstBy(entity.id(), attributeName, false);
				if(attribute == null) {
					continue outer;
				}
				
				//--------------------------------
				// Fetch Values		
				ArrayList<EAVValue> valueArray = CFW.DB.EAVValue.selectLike(entity.id(), attribute.id(), attributeValue); 
				if(!valueArray.isEmpty()) {
					partialSQL.and().custom("(");
						partialSQL.arrayContains(EAVStatsFields.FK_ID_VALUES, valueArray.get(0).id());
						for(int i=1; i < valueArray.size(); i++) {
							partialSQL.or().arrayContains(EAVStatsFields.FK_ID_VALUES, valueArray.get(i).id());
						}
					partialSQL.custom(") ");
				}else {
					continue outer;
				}
				
			}
			
			//--------------------------------
			// Execute SQL for Each Entity
			CFWSQL sql = new CFWSQL(new EAVStats())
					.select()
					.where(EAVStatsFields.FK_ID_ENTITY, entity.id())
					.append(partialSQL)
					;

//			for(int id : valueIDs) {
//				sql.and().arrayContains(EAVStatsFields.FK_ID_VALUES, id);
//			}
			
			sql.and().custom(" TIME >= ?", new Timestamp(earliest) )
			   .and().custom(" TIME <= ?", new Timestamp(latest) )
			   //.groupby(xxx) cannot group here
			   ;

			JsonArray result = sql.getAsJSONArray();
			
			//---------------------------------------
			// Unnest Values & Group
			// Note: Done like this to allow flexible number
			// of attributes. No easy solution found to do this
			// in DB directly.
			result = unnestValuesAndGroupBy(result, attributes.keySet(), fetchAllAttributes);
			
			resultAll.addAll(result);
			
		}
		
		return resultAll;
	}

	/********************************************************************************************
	 * Get the values for the specified valueIDs.
	 * @param groupByAttributes the attributes grouped by
	 * @param fetchAllAttributes TODO
	 * 
	 ********************************************************************************************/
	private static JsonArray unnestValuesAndGroupBy(JsonArray result, Set<String> groupByAttributes, boolean fetchAllAttributes) {
		
		JsonArray groupedResults = new JsonArray();
		LinkedHashMap<String, ArrayList<JsonObject>> groupsMap = new LinkedHashMap<>();
		
		//=====================================================
		// Unnest Values
		//=====================================================
		for(int i = 0; i < result.size(); i++) {
			
			//---------------------------------------
			// Get Entity NAME & CATEGORY
			JsonObject current = result.get(i).getAsJsonObject();
			
			int fkidEntity =  current.get(EAVStatsFields.FK_ID_ENTITY.toString()).getAsInt();
			JsonArray fkidValues =  current.get(EAVStatsFields.FK_ID_VALUES.toString()).getAsJsonArray();
			
			EAVEntity currentEntity = CFW.DB.EAVEntity.selectByID(fkidEntity);
			
			current.remove("PK_ID");
					
			current.addProperty("CATEGORY", currentEntity.category());
			current.addProperty("ENTITY", currentEntity.name());
			
			//---------------------------------------
			// Get Values and add to Record
			Set<String> finalGroupByAttributes = new HashSet<>();
			
			for(int k = 0; k < fkidValues.size(); k++) {
				int valueID = fkidValues.get(k).getAsInt();
				EAVValue eavValue = CFW.DB.EAVValue.selectByID(valueID);
				EAVAttribute attribute = CFW.DB.EAVAttribute.selectByID(eavValue.foreignKeyAttribute());
				
				if(fetchAllAttributes || groupByAttributes.contains(attribute.name())) {
					finalGroupByAttributes.add(attribute.name());
					current.addProperty(attribute.name(), eavValue.value());
				}
				
				//attributesList.addProperty(attribute.name(), eavValue.value());
				
			}
			
			//current.add("ATTRIBUTES", attributesList);
			
			//---------------------------------------
			// Create Groups
			String groupKey = fkidEntity+"_"+current.get(EAVStatsFields.TIME.toString()).getAsLong();
			for(String name : finalGroupByAttributes) {
				JsonElement attributeElement = current.get(name);
				groupKey += "_";
				if(attributeElement != null && !attributeElement.isJsonNull()) {
					groupKey += attributeElement.getAsString();
				}else {
					groupKey += "cfwnullPlaceholder";
				}
			}

			ArrayList<JsonObject> existingGroup = groupsMap.get(groupKey);
			if(existingGroup == null) {
				existingGroup = new ArrayList<JsonObject>();
				groupsMap.put(groupKey, existingGroup);
			}
			
			existingGroup.add(current);
			
		}
		
				
		//=====================================================
		// Recalculate Values by Groups	
		//=====================================================
		for(ArrayList<JsonObject> currentArray : groupsMap.values()) {
			
			JsonObject target =  currentArray.get(0);
			groupedResults.add(target);

			//---------------------------------------
			// No Calculation needed
			if(currentArray.size() == 1) {
				continue;
			}
			
			
			//---------------------------------------
			// Calculate Statistics
			EAVStats aggregation = new EAVStats()
										.type(EAVStatsType.CUSTOM)
										.granularity(target.get(EAVStatsFields.GRANULARITY.name()).getAsInt())
										;
			
			for(int k = 0; k < currentArray.size(); k++) {	
				
				JsonObject toMerge = currentArray.get(k);
				
				//---------------------------------------
				// Get Values of Merge
				BigDecimal min = null;
				BigDecimal avg = null;
				BigDecimal max = null;
				BigDecimal sum = null;

				BigDecimal p25 = null;
				BigDecimal p50 = null;
				BigDecimal p75 = null;
				BigDecimal p95 = null;
								
				int count = target.get(EAVStats.COUNT).getAsInt();
				
				if( toMerge.has(EAVStats.MIN) && !toMerge.get(EAVStats.MIN).isJsonNull()) { min = toMerge.get(EAVStats.MIN).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.AVG) && !toMerge.get(EAVStats.AVG).isJsonNull()) { avg = toMerge.get(EAVStats.AVG).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.MAX) && !toMerge.get(EAVStats.MAX).isJsonNull()) { max = toMerge.get(EAVStats.MAX).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.SUM) && !toMerge.get(EAVStats.SUM).isJsonNull()) { sum = toMerge.get(EAVStats.SUM).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.P25) && !toMerge.get(EAVStats.P25).isJsonNull()) { p25 = toMerge.get(EAVStats.P25).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.P50) && !toMerge.get(EAVStats.P50).isJsonNull()) { p50 = toMerge.get(EAVStats.P50).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.P75) && !toMerge.get(EAVStats.P75).isJsonNull()) { p75 = toMerge.get(EAVStats.P75).getAsBigDecimal(); }
				if( toMerge.has(EAVStats.P95) && !toMerge.get(EAVStats.P95).isJsonNull()) { p95 = toMerge.get(EAVStats.P95).getAsBigDecimal(); }

				aggregation.addStatisticsCustom(count, min, avg, max, sum, p25, p50, p75, p95);
			}
			
			//---------------------------------------
			// Calculate AVG and VAL
			aggregation.calculateStatistics();
			target.addProperty(EAVStats.COUNT, aggregation.count());
			target.addProperty(EAVStats.MIN, aggregation.min());
			target.addProperty(EAVStats.AVG, aggregation.avg());
			target.addProperty(EAVStats.MAX, aggregation.max());
			target.addProperty(EAVStats.SUM, aggregation.sum());
			target.addProperty(EAVStats.VAL, aggregation.val());
			target.addProperty(EAVStats.P25, aggregation.p25());
			target.addProperty(EAVStats.P50, aggregation.p50());
			target.addProperty(EAVStats.P75, aggregation.p75());
			target.addProperty(EAVStats.P95, aggregation.p95());
			
		}
		
		return groupedResults;
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
					.calculateStatistics()
					;
				
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
						+" (TIME TIMESTAMP, FK_ID_DATE INT, FK_ID_ENTITY INT, FK_ID_VALUES NUMERIC ARRAY, COUNT NUMERIC(64,6), MIN NUMERIC(64,6), AVG NUMERIC(64,6), MAX NUMERIC(64,6), SUM NUMERIC(64,6), VAL NUMERIC(64,6), P50 NUMERIC(64,6), P95 NUMERIC(64,6), GRANULARITY INT);")
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
				.custom(" INSERT INTO CFW_EAV_STATS (TIME, FK_ID_DATE, FK_ID_ENTITY, FK_ID_VALUES, COUNT, MIN, AVG, MAX, SUM, VAL, P50, P95, GRANULARITY)" + 
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

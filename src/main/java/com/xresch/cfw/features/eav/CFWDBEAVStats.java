package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.xresch.cfw.features.query.CFWQueryExecutor;
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
					new CFWLog(logger).warn("Attribute '"+attributeName+"' for entity '"+entityName+"' cannot be found.");
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
					new CFWLog(logger).warn("Value '"+attributeValue+"' for attribute '"+attributeName+"' and entity '"+entityName+"' cannot be found.");
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
			   .groupby(EAVStatsFields.TIME,EAVStatsFields.FK_ID_ENTITY,EAVStatsFields.FK_ID_VALUES)
			   .dump()
			   ;

			JsonArray result = sql.getAsJSONArray();
			
			//---------------------------------------
			// Unnest Values & Group
			// Note: Done like this to allow flexible number
			// of attributes. No easy solution found to do this
			// in DB directly.
			result = unnestValuesAndGroupBy(result, attributes.keySet());
			
			resultAll.addAll(result);
			
		}
		
		return resultAll;
	}

	/********************************************************************************************
	 * Get the values for the specified valueIDs.
	 * @param groupByAttributes the attributes grouped by
	 * 
	 ********************************************************************************************/
	private static JsonArray unnestValuesAndGroupBy(JsonArray result, Set<String> groupByAttributes) {
		
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
			JsonObject attributesList = new JsonObject();
			
			for(int k = 0; k < fkidValues.size(); k++) {
				int valueID = fkidValues.get(k).getAsInt();
				EAVValue eavValue = CFW.DB.EAVValue.selectByID(valueID);
				EAVAttribute attribute = CFW.DB.EAVAttribute.selectByID(eavValue.foreignKeyAttribute());
				
				if(groupByAttributes.contains(attribute.name())) {
					current.addProperty(attribute.name().toUpperCase(), eavValue.value());
				}
				
				//attributesList.addProperty(attribute.name(), eavValue.value());
				
			}
			
			//current.add("ATTRIBUTES", attributesList);
			
			//---------------------------------------
			// Create Groups
			String groupKey = fkidEntity+"_"+current.get(EAVStatsFields.TIME.toString()).getAsLong();
			
			for(String name : groupByAttributes) {
				JsonElement attributeElement = current.get(name);
				groupKey += "_";
				if(attributeElement != null) {
					if(!attributeElement.isJsonNull()) {
						groupKey += attributeElement.getAsString();
					}else {
						groupKey += "cfwnullPlaceholder";
					}
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
			// Calculate COUNT, MIN, MAX, SUM
			
			for(int k = 1; k < currentArray.size(); k++) {	
				
				JsonObject toMerge = currentArray.get(k);
				
				//---------------------------------------
				// Merge it Together
				int count = target.get(EAVStatsFields.COUNT.name() ).getAsInt();
				BigDecimal min = target.get(EAVStatsFields.MIN.name() ).getAsBigDecimal();
				BigDecimal max = target.get(EAVStatsFields.MAX.name() ).getAsBigDecimal();
				BigDecimal sum = target.get(EAVStatsFields.SUM.name() ).getAsBigDecimal();
				BigDecimal val = target.get(EAVStatsFields.VAL.name() ).getAsBigDecimal();
				
				int count2 = toMerge.get(EAVStatsFields.COUNT.name() ).getAsInt();
				BigDecimal min2 = toMerge.get(EAVStatsFields.MIN.name() ).getAsBigDecimal();
				BigDecimal max2 = toMerge.get(EAVStatsFields.MAX.name() ).getAsBigDecimal();
				BigDecimal sum2 = toMerge.get(EAVStatsFields.SUM.name() ).getAsBigDecimal();
				BigDecimal val2 = toMerge.get(EAVStatsFields.VAL.name() ).getAsBigDecimal();
				
				target.addProperty(EAVStatsFields.COUNT.name(), 		count+count2);
				target.addProperty(EAVStatsFields.MIN.name(), 			(min.compareTo(min2) <= 0 ? min : min2) );
				target.addProperty(EAVStatsFields.MAX.name(), 			(max.compareTo(max2) >= 0 ? max : max2) );
				target.addProperty(EAVStatsFields.SUM.name(), 			sum.add(sum2) );
				target.addProperty("VALSUM", 							val2.add(val2) );

			}
			
			//---------------------------------------
			// Calculate AVG and VAL
			BigDecimal finalCount = new BigDecimal(target.get(EAVStatsFields.COUNT.name() ).getAsInt());
			BigDecimal finalSum = target.get(EAVStatsFields.SUM.name() ).getAsBigDecimal();
			BigDecimal valSum = target.remove("VALSUM").getAsBigDecimal();

			target.addProperty(EAVStatsFields.AVG.name(), finalSum.divide(finalCount, RoundingMode.HALF_UP));
			target.addProperty(EAVStatsFields.VAL.name(), valSum.divide(finalCount, RoundingMode.HALF_UP));
			
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
						+" (TIME TIMESTAMP, FK_ID_DATE INT, FK_ID_ENTITY INT, FK_ID_VALUES NUMERIC ARRAY, COUNT NUMERIC(64,6), MIN NUMERIC(64,6), AVG NUMERIC(64,6), MAX NUMERIC(64,6), SUM NUMERIC(64,6), VAL NUMERIC(64,6), GRANULARITY INT);")
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
				.custom(" INSERT INTO CFW_EAV_STATS (TIME, FK_ID_DATE, FK_ID_ENTITY, FK_ID_VALUES, COUNT, MIN, AVG, MAX, SUM, VAL, GRANULARITY)" + 
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

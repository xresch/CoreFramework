package com.xresch.cfw.features.analytics;

import java.sql.Timestamp;
import java.util.logging.Logger;

import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.analytics.CPUSample.StatsCPUSampleFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBCPUSample {
	
	private static final Logger logger = CFWLog.getLogger(CFWDBCPUSample.class.getName());
		
	/********************************************************************************************
	 * Creates a new cpuSample in the DB and returns it's primary key.
	 * @param cpuSample to create
	 * @return id or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer insertGetID(CPUSample cpuSample) {
		
		if(cpuSample == null) {
			new CFWLog(logger)
				.warn("The cpuSample cannot be null");
			return null;
		}

		return cpuSample
				.queryCache(CFWDBCPUSample.class, "create")
				.insertGetPrimaryKey();
	}
	
	
	/***************************************************************
	 * Select a cpuSample by it's ID.
	 * @param id of the cpuSample
	 * @return Returns a cpuSample or null if not found or in case of exception.
	 ****************************************************************/
	public static CPUSample selectByID(int id ) {

		return (CPUSample)new CPUSample()
				.queryCache(CFWDBCPUSample.class, "selectByID")
				.select()
				.where(StatsCPUSampleFields.PK_ID.toString(), id)
				.getFirstAsObject();
		
	}
	
	/***************************************************************
	 * Get the timestamp of the oldest record that has a ganularity lower
	 * than the one specified by the parameter.
	 * @param granularity
	 * @return timestamp
	 ****************************************************************/
	public static Timestamp getOldestAgedRecord(int granularity, Timestamp ageOutTime  ) {

		CPUSample first =(CPUSample)new CPUSample()
				.queryCache(CFWDBCPUSample.class, "getOldestAgedRecord")
				.select()
				.custom("WHERE GRANULARITY < ?", granularity)
				.custom("AND TIME <= ?", ageOutTime)
				.orderby(StatsCPUSampleFields.TIME.toString())
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

		CPUSample first =(CPUSample)new CPUSample()
				.queryCache(CFWDBCPUSample.class, "getYoungestAgedRecord")
				.select()
				.custom(" WHERE GRANULARITY < ? ", granularity)
				.custom(" AND TIME <= ? ", ageOutTime)
				.orderbyDesc(StatsCPUSampleFields.TIME.toString())
				.limit(1)
				.getFirstAsObject();
		
		if(first != null) {
			return first.time();
		}else {
			return null;
		}
		
	}
	

	/***************************************************************
	 * Return a list of the latest cpuSamples as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getLatestAsJSON() {
		return new CPUSample()
				.queryCache(CFWDBCPUSample.class, "getLatestAsJSON")
				.select()
				.custom(" WHERE time = (SELECT MAX(time) FROM "+CPUSample.TABLE_NAME+" )")
				.getAsJSON();
	}
	
	/***************************************************************
	 * Return a list of the cpuSamples as json string.
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getForTimeframeAsJSON(long earliestMillis, long latestMillis) {
		return getForTimeframeAsJSON(new Timestamp(earliestMillis), new Timestamp(latestMillis));
	}
	
	/***************************************************************
	 * Return a list of the cpuSamples as json string.
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getForTimeframeAsJSON(Timestamp earliest, Timestamp latest) {
		
		return new CPUSample()
				.queryCache(CFWDBCPUSample.class, "getForTimeframeAsJSON")
				.loadSQLResource(FeatureSystemAnalytics.RESOURCE_PACKAGE, "cpusampling_fetch_by_timeframe.sql", 
						earliest, 
						latest)
				.getAsJSON();
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
		int count =  new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.selectCount() 
				.custom(" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY < ?;"
						 ,startTime, endTime, newGranularity)
				.getCount();

		if(count == 0) {
			CFWDB.transactionRollback();
			return true;
		}
		
		//--------------------------------------------
		// Start Aggregation
		new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.custom("CREATE TEMP TABLE" + 
						" IF NOT EXISTS CFW_STATS_CPUSAMPLE_AGGREGATION" + 
						" (TIME TIMESTAMP, FK_ID_SIGNATURE INT, FK_ID_PARENT INT , COUNT INT, MIN INT, AVG INT, MAX INT, GRANULARITY INT);")
				.execute();
		
		success &=  new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.custom("INSERT INTO CFW_STATS_CPUSAMPLE_AGGREGATION" + 
						" SELECT (" + 
						"		DATEADD(" + 
						"			SECOND," + 
						"			DATEDIFF(SECOND, MIN(TIME), MAX(TIME)) / 2," + 
						"			MIN(TIME)" + 
						"		)) AS NewTime," + 
						" FK_ID_SIGNATURE, FK_ID_PARENT, SUM(COUNT), MIN(MIN), AVG(AVG), MAX(MAX), ?"+ 
						" FROM CFW_STATS_CPUSAMPLE" + 
						" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY <  ?" + 
						" GROUP BY FK_ID_SIGNATURE, FK_ID_PARENT;"
						,newGranularity ,startTime, endTime, newGranularity)
				.execute();
		
		success &=  new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM CFW_STATS_CPUSAMPLE" + 
						" WHERE TIME >= ?" + 
						" AND TIME < ?" + 
						" AND GRANULARITY < ?;"
						 ,startTime, endTime, newGranularity)
				.execute();
		
		
		success &=  new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.custom(" INSERT INTO CFW_STATS_CPUSAMPLE (TIME, FK_ID_SIGNATURE, FK_ID_PARENT,  COUNT, MIN, AVG, MAX, GRANULARITY)" + 
						" SELECT * FROM CFW_STATS_CPUSAMPLE_AGGREGATION;")
				.execute();
		
		success &=  new CPUSample()
				.queryCache(CFWDBCPUSample.class, "aggregateStatistics"+(cacheCounter++))
				.custom("DELETE FROM CFW_STATS_CPUSAMPLE_AGGREGATION;\r\n")
				.execute();
		
		
		if(success) {
			CFWDB.transactionCommit();
		}else {
			CFWDB.transactionRollback();
		}
		
		return success;
	}
	
	/****************************************************************
	 * Deletes the cpuSample by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
				
		return new CPUSample()
				.queryCache(CFWDBCPUSample.class, "deleteByID")
				.delete()
				.where(StatsCPUSampleFields.PK_ID.toString(), id)
				.executeDelete();
					
	}	
	
}

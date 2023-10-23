package com.xresch.cfw.features.eav;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.h2.expression.ArrayElementReference;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TaskEAVStatsAgeOut extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskEAVStatsAgeOut.class.getName());

	
	
	public void execute() {
		
		new CFWLog(logger).info("Age Out EAV Statistics");
		//----------------------------
		// Iterate all granularities
		for(int granularity : CFW.Time.AGE_OUT_GRANULARITIES) {
			//--------------------------
			// Get Age Out Time
			Timestamp ageOutTime = CFW.Time.getDefaultAgeOutTime(granularity);
			
			//--------------------------
			// Get timespan 
			Timestamp oldest = CFWDBEAVStats.getOldestAgedRecord(granularity, ageOutTime);
			Timestamp youngest = CFWDBEAVStats.getYoungestAgedRecord(granularity, ageOutTime);
			if(oldest == null || youngest == null ) {
				//nothing to aggregate for this granularity
				continue;
			}
			
			//--------------------------
			// Iterate with offsets
			Timestamp startTime = oldest;
			Timestamp endTime = CFW.Time.offsetTimestamp(oldest, 0, 0, 0, 0, granularity);
			
			// do-while to execute at least once, else would not work if (endTime - startTime) < granularity
			do {
				CFWDBEAVStats.aggregateStatistics(startTime, endTime, granularity);
				startTime =  CFW.Time.offsetTimestamp(startTime, 0, 0, 0, 0, granularity);
				endTime = CFW.Time.offsetTimestamp(endTime, 0, 0, 0, 0, granularity);

			} while(endTime.getTime() < youngest.getTime());

		}
		
	}
	
	/********************************************************************************************
	 * Get the default age out time of the application.
	 * @return timestamp
	 ********************************************************************************************/
	public Timestamp getAgeOutTime(int granularityMinutes) {
		
		int hours15Min = -1 * CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_15MIN);
		int days1Hour = -1 * CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_1HOUR);
		int days6Hours = -1 * CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_6HOURS);
		int days24Hours = -1 * CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_24HOURS);
		int days1Week = -1 * CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_1WEEK);
		
		// offset from present time by the duration you want to keep the specific granularity
		long ageOutOffset;
		
		if		(granularityMinutes <= CFW.Time.MINUTES_OF_15) 		{ ageOutOffset = CFWTimeUnit.h.offset(null, hours15Min); }
		else if	(granularityMinutes <= CFW.Time.MINUTES_OF_HOUR) 	{ ageOutOffset = CFWTimeUnit.d.offset(null, days1Hour); }
		else if (granularityMinutes <= CFW.Time.MINUTES_OF_6HOURS) { ageOutOffset = CFWTimeUnit.d.offset(null, days6Hours); }
		else if (granularityMinutes <= CFW.Time.MINUTES_OF_DAY) 	{ ageOutOffset = CFWTimeUnit.d.offset(null, days24Hours); }
		else if (granularityMinutes <= CFW.Time.MINUTES_OF_WEEK) 	{ ageOutOffset = CFWTimeUnit.d.offset(null, days1Week); }
		else  														{ ageOutOffset = CFWTimeUnit.d.offset(null, days1Week); }

		return new Timestamp(ageOutOffset);
	}

}

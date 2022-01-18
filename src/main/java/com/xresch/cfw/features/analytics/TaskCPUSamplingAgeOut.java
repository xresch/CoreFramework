package com.xresch.cfw.features.analytics;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.Utils;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskCPUSamplingAgeOut extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskCPUSamplingAgeOut.class.getName());

	
	public void execute() {
		
		Configuration config = CFW.DB.Config.selectByName(FeatureConfiguration.CONFIG_CPU_SAMPLING_AGGREGATION);
		ArrayList<String> granularities = config.options();
		
		//----------------------------
		// Iterate all granularities
		for(String string : granularities) {
			
			//--------------------------
			// Get Age Out Time
			int granularity = Integer.parseInt(string);
			Timestamp ageOutTime = Utils.Time.getDefaultAgeOutTime(granularity);
			
			//--------------------------
			// Get timespan 
			Timestamp oldest = CFWDBCPUSample.getOldestAgedRecord(granularity, ageOutTime);
			Timestamp youngest = CFWDBCPUSample.getYoungestAgedRecord(granularity, ageOutTime);
			
			if(oldest == null || youngest == null ) {
				//nothing to aggregate for this granularity
				continue;
			}
			


			
			//--------------------------
			// Iterate with offsets
			Timestamp startTime = oldest;
			Timestamp endTime = Utils.Time.offsetTimestamp(oldest, 0, 0, 0, 0, granularity);
			
			while(endTime.getTime() < youngest.getTime()) {

				CFWDBCPUSample.aggregateStatistics(startTime, endTime, granularity);
				startTime =  Utils.Time.offsetTimestamp(startTime, 0, 0, 0, 0, granularity);
				endTime = Utils.Time.offsetTimestamp(endTime, 0, 0, 0, 0, granularity);

			}

		}
		
		
	}

}

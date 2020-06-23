package com.pengtoolbox.cfw.features.cpusampling;

import java.sql.Timestamp;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.schedule.CFWScheduledTask;

public class TaskCPUSamplingAggregation extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskCPUSamplingAggregation.class.getName());

	
	public void execute() {
		
		//System.out.println("============= RUN StatsCPUSamplingAggregationTask ============");
		Configuration config = CFW.DB.Config.selectByName(Configuration.CPU_SAMPLING_AGGREGATION);
		Object[] granularities = (Object[])config.options();
		
		//----------------------------
		// Iterate all granularities
		for(Object object : granularities) {
			
			//--------------------------
			// Get Age Out Time
			int granularity = Integer.parseInt(object.toString());
			Timestamp ageOutTime = CFW.Time.getDefaultAgeOutTime(granularity);
			
			//--------------------------
			// Get timespan 
			Timestamp oldest = CFWDBCPUSample.getOldestAgedRecord(granularity, ageOutTime);
			Timestamp youngest = CFWDBCPUSample.getYoungestAgedRecord(granularity, ageOutTime);
//			System.out.println("=========================================");
//			System.out.println("granularity:"+granularity);
//			System.out.println("oldest:"+oldest);
//			System.out.println("youngest:"+youngest);
//			System.out.println("ageOutTime:"+ageOutTime.toString());
			
			if(oldest == null || youngest == null ) {
				//nothing to aggregate for this granularity
				continue;
			}
			


			
			//--------------------------
			// Iterate with offsets
			Timestamp startTime = oldest;
			Timestamp endTime = CFW.Time.offsetTimestamp(oldest, 0, 0, 0, granularity);
			
			while(endTime.getTime() < youngest.getTime()) {
//				System.out.println("---------- Aggregate ----------");
//				System.out.println("startTime:"+startTime.toString());
//				System.out.println("endTime:"+endTime.toString());
				boolean success = CFWDBCPUSample.aggregateStatistics(startTime, endTime, granularity);
				startTime =  CFW.Time.offsetTimestamp(startTime, 0, 0, 0, granularity);
				endTime = CFW.Time.offsetTimestamp(endTime, 0, 0, 0, granularity);
//				System.out.println("success: "+success);
			}

		}
		
		
	}

}

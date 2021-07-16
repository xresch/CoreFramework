package com.xresch.cfw.features.analytics;

import java.util.ArrayList;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.utils.CFWUtilsAnalysis;

public class CFWJobTaskThreadDumps extends CFWJobTask {
	

	@Override
	public String uniqueName() {
		return "Thread Dumps";
	}

	@Override
	public String taskDescription() {
		return "Writes Thread dumps to the disk for analytical purposes.";
	}

	@Override
	public ArrayList<JobTaskProperty> jobProperties() {
		ArrayList<JobTaskProperty> properties = new ArrayList<JobTaskProperty>(); 
		JobTaskProperty folder = new JobTaskProperty("folder", "./threaddumps", "the path of the folder were the thread dumps should be written on the server."); 
		properties.add(folder);
		return properties;
	}

	@Override
	public int minIntervalSeconds() {
		return 15;
	}

	@Override
	public boolean hasPermission() {
		
		if( CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS) ) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getTrigger().getJobDataMap();
		String folderpath = data.getString("folder");
		
        String filepath = "threaddump_"+CFW.Utils.Time.currentTimestamp().replace(":", "")+".txt";
        CFWUtilsAnalysis.threadDumpToDisk(folderpath, filepath);
		
	}
	
}

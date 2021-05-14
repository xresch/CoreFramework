package com.xresch.cfw.features.analytics;

import java.util.ArrayList;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.jobs.JobTask;

public class JobTaskThreadDumps extends JobTask{

	@Override
	public String uniqueName() {
		return "Create Thread Dumps";
	}
	
	@Override
	public String taskDescription() {
		return "Creates Thread dumps to the disk. Useful to analyze reoccuring system issues.";
	}
	
	@Override
	public ArrayList<JobTaskProperty> jobProperties() {
		ArrayList<JobTaskProperty> properties = new ArrayList<JobTaskProperty>();
		
		properties.add(new JobTaskProperty("targetFolder", "./threaddumps", "Define the target folder on the system where the thread dumps should be stored."));
		return null;
	}

	@Override
	public boolean hasPermission() {
		
		if(CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS)) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {

		JobDataMap data = context.getMergedJobDataMap();
		System.out.println("someProp = " + data.getString("someProp"));
	}



}

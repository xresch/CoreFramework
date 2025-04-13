package com.xresch.cfw.features.core.acme;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.TaskDatabaseBackup;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TaskACMEUpdateCertificates extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskACMEUpdateCertificates.class.getName());
	
	private static ScheduledFuture<?> future = null;
	
	public static void setupTask() {
		
		//---------------------------
		// Take down running taks
		if(future != null) {
			future.cancel(true);
			future = null;
		}
		
		//---------------------------
		// Check is ACME Enabled
		if(!CFW.Properties.HTTPS_ACME_ENABLED) {
			return;
		}
		
		//---------------------------
		// Create new Task
		int oneDayInSeconds =  24 * 60 * 60;

		future = CFW.Schedule.runPeriodically(
				  oneDayInSeconds
				, oneDayInSeconds
				, new TaskACMEUpdateCertificates()
			);
	}

	@Override
	public void execute() {
		
		try {
			CFWACMEClient.fetchCACertificate();
		} catch (Exception e) {
			new CFWLog(logger).warn(
					"Exception while trying to retrieve certificate from CA authority: "+e.getMessage()
					, e
				);
		}
	}
	
}

package com.xresch.cfw.features.jobs;

import java.util.HashMap;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;

import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

public class CFWJobsAlertingChannelAppLog extends CFWJobsAlertingChannel {

	private static Logger logger = CFWLog.getLogger(CFWJobsAlertingChannelAppLog.class.getName());
	
	@Override
	public String uniqueName() {
		return "Application Log";
	}

	@Override
	public String channelDescription() {
		return "Writes the alerts to the application log file.";
	}

	@Override
	public void sendAlerts(JobExecutionContext context, CFWJobsAlertObject alertObject, HashMap<Integer, User> usersToAlert, String subject, String content, String contentHTML) {
				
		new CFWLog(logger)
				.silent(true)
				.custom("alertType", alertObject.getLastAlertType())
				.custom("alertSubject", subject)
				.custom("jobid",  alertObject.getJobID())
				.custom("taskname",  alertObject.getTaskName())
				.off(content);

	}

	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}

}

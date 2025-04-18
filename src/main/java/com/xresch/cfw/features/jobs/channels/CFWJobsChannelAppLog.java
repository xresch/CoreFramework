package com.xresch.cfw.features.jobs.channels;

import java.util.HashMap;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsChannelAppLog extends CFWJobsChannel {

	private static Logger logger = CFWLog.getLogger(CFWJobsChannelAppLog.class.getName());
	
	public static final String UNIQUE_NAME = "Application Log";
	
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public String getLabel() {
		return getUniqueName();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/	
	@Override
	public String manualPageTitle() {
		return UNIQUE_NAME;
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public String manualPageContent() {
		return "<p>Writes the data to the application log file.</p>";
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public void sendReport(JobExecutionContext context, MessageType messageType, CFWJobsAlertObject alertObject, HashMap<Integer, User> usersToAlert, String subject, String content, String contentHTML) {
				
		CFWLog logEvent = new CFWLog(logger)
				.silent(true)
				.contextless(true)
				.custom("alertType", alertObject.getLastAlertType())
				.custom("alertSubject", subject)
				.custom("jobid",  alertObject.getJobID())
				.custom("taskname",  alertObject.getTaskName());
		
		//------------------------
		// Handle Custom Notes
		String customNotes = alertObject.getCustomNotes();
		if( !Strings.isNullOrEmpty(customNotes) 
		 && !customNotes.trim().toLowerCase().equals("null") ) {
			logEvent.custom("customNotes", alertObject.getCustomNotes());
		}
		
		//------------------------
		// Do Log
		logEvent.off(content);

	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public void addTextData(String name, String filetype, String data) {
		// do nothing
	}

}

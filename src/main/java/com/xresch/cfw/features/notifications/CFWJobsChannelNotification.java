package com.xresch.cfw.features.notifications;

import java.util.HashMap;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannel;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsChannelNotification extends CFWJobsChannel {
	
	public static final String UNIQUE_NAME = "Notification";
	
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
		return "<p>Sends the data to the users by adding a notification to their notification list.</p>";
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/	
	@Override
	public void sendReport(JobExecutionContext context
			, MessageType messageType
			, CFWJobsAlertObject alertObject
			, HashMap<Integer, User> usersToAlert, String subject
			, String content, String contentHTML) {
				
		String jobID = context.getJobDetail().getKey().getName();
		CFWJob job = CFW.DB.Jobs.selectByID(jobID);
		
		//----------------------------------------
		// Create Message Content
		String messageContent = Strings.isNullOrEmpty(contentHTML) ? "<p>"+content+"</p>" : contentHTML;
		
		//------------------------
		// Handle Custom Notes
		String customNotes = alertObject.getCustomNotes();
		if( !Strings.isNullOrEmpty(customNotes) 
		 && !customNotes.trim().toLowerCase().equals("null") ) {
			messageContent = 
					"<br>"
					+"<span><b>Job:&nbsp;</b>"+job.jobname()+" (ID: "+jobID+")</span>"
					+"<br>"
					+"<span><b>Custom Notes:</b></span>"
					+"<br>"
					+ "<span>" + alertObject.getCustomNotes() + "</span>"
					+"<hr>"
					+ messageContent;
		}
				
		//----------------------------------------
		// Create Notifications for users 
		Notification templateNotification = 
				new Notification()
						.isRead(false)
						.messageType(messageType)
						.title(subject)
						.message(messageContent);
		
		CFW.DB.Notifications.createForUsers(usersToAlert.values(), templateNotification);

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

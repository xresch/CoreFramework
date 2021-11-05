package com.xresch.cfw.features.jobs;

import java.util.HashMap;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWJobsAlertingChannelNotification extends CFWJobsAlertingChannel {

	@Override
	public String uniqueName() {
		return "Notification";
	}

	@Override
	public String channelDescription() {
		return "Sends the alerts to the users by adding a notification to their notification list.";
	}

	@Override
	public void sendAlerts(JobExecutionContext context
			, MessageType messageType
			, CFWJobsAlertObject alertObject
			, HashMap<Integer, User> usersToAlert, String subject
			, String content, String contentHTML) {
				
		String jobID = context.getJobDetail().getKey().getName();
		CFWJob job = CFW.DB.Jobs.selectByID(jobID);
		
		//----------------------------------------
		// Create Mail Content
		String messageContent = Strings.isNullOrEmpty(contentHTML) ? "<p>"+content+"</p>" : contentHTML;
		
		//------------------------
		// Handle Custom Notes
		if( !Strings.isNullOrEmpty(alertObject.getCustomNotes()) ) {
			messageContent += "<p><b>Custom Notes</b><br>"+alertObject.getCustomNotes()+"</p>";
		}
				
		//----------------------------------------
		// Create Notifications for users 
		Notification templateNotification = 
				new Notification()
						.isRead(false)
						.messageType(messageType)
						.title(subject)
						.message(messageContent);
		
		for(Integer id : usersToAlert.keySet()) {
			if(id != null) {
				templateNotification.foreignKeyUser(id);
				CFW.DB.Notifications.create(templateNotification);
			}
		}
		
		
	}

	@Override
	public boolean hasPermission(User user) {
		
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}

}

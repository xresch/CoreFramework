package com.xresch.cfw.features.jobs;

import java.util.HashMap;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;

public class CFWJobsAlertingChannelEMail extends CFWJobsAlertingChannel {

	@Override
	public String uniqueName() {
		return "eMail";
	}

	@Override
	public String channelDescription() {
		return "Sends the alerts to the users eMail addresses.";
	}

	@Override
	public void sendAlerts(JobExecutionContext context, CFWJobsAlertObject alertObject, HashMap<Integer, User> usersToAlert, String subject, String content, String contentHTML) {
				
		String jobID = context.getJobDetail().getKey().getName();
		CFWJob job = CFW.DB.Jobs.selectByID(jobID);
		
		//----------------------------------------
		// Create Mail Content
		String mailContent = Strings.isNullOrEmpty(contentHTML) ? content : contentHTML;
		
		//------------------------
		// Handle Custom Notes
		if( !Strings.isNullOrEmpty(alertObject.getCustomNotes()) ) {
			mailContent += "<p><b>Custom Notes</b><br>"+alertObject.getCustomNotes()+"</p>";
		}
		
		
		
		//----------------------------------------
		// Create and Send Mail 
		new CFWMailBuilder(subject)
				.addMessage(mailContent, true)
				.fromNoReply()
				.recipientsBCC(usersToAlert)
				.addAttachment("jobdetails.json", CFW.JSON.toJSONPretty(job))
				.send();
		
	}

	@Override
	public boolean hasPermission(User user) {
		
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}

}

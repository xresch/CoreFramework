package com.xresch.cfw.features.jobs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWJobsAlertingChannelEMail extends CFWJobsAlertingChannel {

	LinkedHashMap<String,String> attachments = new LinkedHashMap<>();
	
	@Override
	public String uniqueName() {
		return "eMail";
	}

	@Override
	public String channelDescription() {
		return "Sends the alerts to the users eMail addresses.";
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
		String mailContent = Strings.isNullOrEmpty(contentHTML) ? content : contentHTML;
		
		//------------------------
		// Handle Custom Notes
		String customNotes = alertObject.getCustomNotes();
		if( !Strings.isNullOrEmpty(customNotes) 
		 && !customNotes.trim().toLowerCase().equals("null") ) {
			mailContent = "<span><b>Custom Notes:</b></span>"
							+"<br>"
							+ "<span>" + alertObject.getCustomNotes() + "</span>"
							+"<hr>"
							+ mailContent;
		}
		
		
		//----------------------------------------
		// Create and Send Mail 
		CFWMailBuilder builder = new CFWMailBuilder(subject)
				.addMessage(mailContent, true)
				.fromNoReply()
				.recipientsBCC(usersToAlert)
				.addAttachment("jobdetails.json", CFW.JSON.toJSONPretty(job));
		
		for(Entry<String, String> entry : attachments.entrySet()) {
			builder.addAttachment(entry.getKey(), entry.getValue());
		}
		
		builder.send();
		
	}

	@Override
	public boolean hasPermission(User user) {
		
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}
	
	@Override
	public void addTextData(String name, String filetype, String data) {
		attachments.put(name+"."+filetype, data);
	}

}

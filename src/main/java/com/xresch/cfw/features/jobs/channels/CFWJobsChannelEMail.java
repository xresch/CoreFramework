package com.xresch.cfw.features.jobs.channels;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsChannelEMail extends CFWJobsChannel {

	public static final String UNIQUE_NAME = "eMail";
	
	LinkedHashMap<String,String> attachments = new LinkedHashMap<>();
	
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
		return "<p>Sends the data to the users eMail addresses."
				+ " For this to work, you will need to configure an SMTP server in the file '{APP_ROOT}/config/cfw.properties'.</p>";
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
		attachments.put(name+"."+filetype, data);
	}

}

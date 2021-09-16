package com.xresch.cfw.features.jobs;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.common.base.Strings;
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
	public void sendAlerts(CFWJobsAlertObject alertObject, HashMap<Integer, User> usersToAlert, String subject, String content, String contentHTML) {
		
		
		//----------------------------------------
		// Create Recipients List
		LinkedHashMap<String,String> emailDisplayNameMap = new LinkedHashMap<>();
		for(User user : usersToAlert.values()) {
			
			if( !Strings.isNullOrEmpty(user.email()) ) {
				emailDisplayNameMap.put(user.email(), user.createUserLabel());
			}
		}
		
		//----------------------------------------
		// Create Mail and Send
		String mailContent = Strings.isNullOrEmpty(contentHTML) ? content : contentHTML;
		
		new CFWMailBuilder(subject)
				.addMessage(mailContent)
				.fromNoReply()
				.recipientsBCC(emailDisplayNameMap)
				.send();
		
	}

	@Override
	public boolean hasPermission(User user) {
		
		return user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) || user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}

}

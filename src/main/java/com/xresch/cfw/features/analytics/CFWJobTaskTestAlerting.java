package com.xresch.cfw.features.analytics;

import java.util.HashMap;
import java.util.Locale;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWJobTaskTestAlerting extends CFWJobTask {
	

	@Override
	public String uniqueName() {
		return "Test: Alerting";
	}

	@Override
	public String taskDescription() {
		return "Used to test alerts.";
	}

	@Override
	public CFWObject getParameters() {
		return new CFWJobsAlertObject();
	}

	@Override
	public int minIntervalSeconds() {
		return 15;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		return null;
	}

	@Override
	public boolean hasPermission(User user) {
		
		if(user.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS) ) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		//JobDataMap data = context.getMergedJobDataMap();
		
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this);
		
		alertObject.mapJobExecutionContext(context);
		
		boolean randomCondition = CFW.Random.randomBoolean();
		CFW.Messages.addInfoMessage("Last Condition: "+randomCondition);
		
		AlertType type = alertObject.checkSendAlert(randomCondition, null);
		
		if(!type.equals(AlertType.NONE)) {

			String message = "Hi There!\n\nThis is only a test, have a marvelous day!";
			String messageHTML = "<p>Hi There!<p></p>This is only a test, have a marvelous day!</p>";
			
			
			if(type.equals(AlertType.RAISE)) {
				alertObject.doSendAlert(context, CFW.Random.randomMessageType(), "[TEST] Alert: A situation is occuring!", message, messageHTML);
			}
			
			if(type.equals(AlertType.RESOLVE)) {
				message += " "+CFW.Random.randomIssueResolvedMessage();
				messageHTML += "<p>"+CFW.Random.randomIssueResolvedMessage()+"</p>";
				alertObject.doSendAlert(context, MessageType.SUCCESS, "[TEST] Alert: A situation has resolved!.", message, messageHTML);
			}
		}
				
	}
	
}

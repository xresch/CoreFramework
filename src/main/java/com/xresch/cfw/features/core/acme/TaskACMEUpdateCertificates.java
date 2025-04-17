package com.xresch.cfw.features.core.acme;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

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
				  0
				, oneDayInSeconds
				, new TaskACMEUpdateCertificates()
			);
	}

	@Override
	public void execute() {
		
		try {
			CFWACMEClient.fetchCACertificate();
		} catch (Exception e) {
			
			//----------------------------
			// Notification
			CFW.DB.Notifications.createForAdminUsers(
				new Notification()
					.category("ACME")
					.messageType(MessageType.ERROR)
					.title("ACME: Error retrieving Certificate from Authority")
					.message("""
						<p>The application was unable to retrieve a certificate from the certificate authority.</p>
						<p><b>Certificate Authority:&nbsp;</b> %s</p>
						<p><b>Domains:&nbsp;</b> %s</p>
						<p><b>Error Message:&nbsp;</b> %s</p>
						<p><b>Error Stacktrace:&nbsp;</b> %s</p>
					""".formatted(
							  CFW.Properties.HTTPS_ACME_URL
							, CFW.Properties.HTTPS_ACME_DOMAINS
							, e.getMessage()
							, CFW.Utils.Text.stacktraceToString(e)
							
						)
					)
			);
			
			//----------------------------
			// Log
			new CFWLog(logger)
					.severe(
						"ACME: Error while attempting to retrieve certificate from CA authority: " 
						+ e.getMessage()
						, e
					);
			
		}
	}
	
}

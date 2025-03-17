package com.xresch.cfw.extensions.cli;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannel;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsChannelCLI extends CFWJobsChannel {

	private static Logger logger = CFWLog.getLogger(CFWJobsChannelCLI.class.getName());
	
	LinkedHashMap<String,String> attachments = new LinkedHashMap<>();
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String getLabel() {
		return this.getContextSettings().getName();
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String manualPageTitle() {
		return "CLI";
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String manualPageContent() {
		return CFW.Files.readPackageResource(FeatureCLIExtensions.PACKAGE_RESOURCES, "manual_channel_cli.html");
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void sendReport(JobExecutionContext context
			, MessageType messageType
			, CFWJobsAlertObject alertObject
			, HashMap<Integer, User> usersToAlert
			, String subject
			, String content
			, String contentHTML
			){
		
		//----------------------------------------
		// Get Variables
		String jobID = context.getJobDetail().getKey().getName();
		CFWJob job = CFW.DB.Jobs.selectByID(jobID);
		
		CFWJobsChannelCLISettings cliReportSettings = getContextSettings();
		
		String workingDir = cliReportSettings.workingDir();
		String commands = cliReportSettings.commands();
		Integer timeout = cliReportSettings.timeout();
		
		LinkedHashMap<String, String> envVariables = cliReportSettings.envVariables();
		if(envVariables == null) {
			envVariables = new LinkedHashMap<>();
		}
		
		//----------------------------------------
		// Add ENV Variables
		envVariables.put("CLIREPORTER_SUBJECT", subject );
		envVariables.put("CLIREPORTER_TYPE_MESSAGE", messageType.name() );
		envVariables.put("CLIREPORTER_ALERT_STATE", alertObject.getLastAlertType().name() );
		envVariables.put("CLIREPORTER_CONTENT", content );
		envVariables.put("CLIREPORTER_CONTENT_HTML", contentHTML );
		envVariables.put("CLIREPORTER_CUSTOM_NOTES", alertObject.getCustomNotes() );
		envVariables.put("CLIREPORTER_JOBDETAILS", CFW.JSON.toJSONPretty(job) );
		envVariables.put("CLIREPORTER_TIMEOUT", ""+timeout);
		

		//----------------------------------------
		// Add ENV Variables: Attachments
		
		JsonArray attachmentsArray = new JsonArray();
		
		for(Entry<String, String> entry : attachments.entrySet()) {
			JsonObject object = new JsonObject();
			object.addProperty("name", entry.getKey());
			object.addProperty("contents", entry.getValue());
			attachmentsArray.add(object);
		}
		
		envVariables.put("CLIREPORTER_ATTACHMENTS", CFW.JSON.toJSONPretty(attachmentsArray) );
		
		//----------------------------------------
		// Add ENV Variables: Users
		
		JsonArray usersArray = new JsonArray();
		
		for(User user : usersToAlert.values()) {
			JsonObject object = new JsonObject();
			object.addProperty("username", user.username());
			object.addProperty("firstname", user.firstname());
			object.addProperty("lastname", user.lastname());
			object.addProperty("email", user.email());
			usersArray.add(object);
		}
		
		envVariables.put("CLIREPORTER_ALERT_USERS",  CFW.JSON.toJSONPretty(usersArray) );
		
		//----------------------------------------
		// Create Mail Content
		CFWCLIExecutor executor = new CFWCLIExecutor(workingDir, commands, envVariables); 
		
		//----------------------------------------
		// Create attachment files
		try {
			executor.execute();
		}catch(Exception e) { 
			writeError(jobID, e); 
		}
		
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	private CFWJobsChannelCLISettings getContextSettings() {
		
		int environmentID = CFWJobsChannelCLISettings.extractIDFromUniqueName(getUniqueName());
		
		CFWJobsChannelCLISettings cliReporterSettings =
				CFWJobsChannelCLISettingsManagement.getEnvironment(environmentID);
		
		return cliReporterSettings;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	private void writeError(String jobID, Exception e) {
		new CFWLog(logger).severe(
				  "CLI Reporter(Job ID: "+jobID+"): Error while writing report: "
				+ e.getMessage()
				, e
				);
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		

		if( user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN) ){
			return true;
		}else {
			CFWJobsChannelCLISettings filesystemSettings = getContextSettings();
			
			
			HashMap<Integer, Object> settingsMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(
					CFWJobsChannelCLISettings.SETTINGS_TYPE
					, user
				);
			
			return settingsMap.containsKey(filesystemSettings.getDefaultObject().id() );
			
		}
			
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void addTextData(String name, String filetype, String data) {
		attachments.put(name+"."+filetype, data);
	}

}

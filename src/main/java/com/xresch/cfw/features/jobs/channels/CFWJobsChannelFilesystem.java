package com.xresch.cfw.features.jobs.channels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsChannelFilesystem extends CFWJobsChannel {

	private static Logger logger = CFWLog.getLogger(CFWJobsChannelFilesystem.class.getName());
	
	LinkedHashMap<String,String> attachments = new LinkedHashMap<>();
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String getLabel() {
		return this.getContextSettings().getName();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/	
	@Override
	public String manualPageTitle() {
		return "Filesystem";
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String manualPageContent() {
		return "<p>Stores the data and any potential attachments to a filesystem location."
				+ " For this to be selecteable, you need to go to &quot;Menu &gt;&gt; Admin &gt;&gt; Context Settings&quot;"
				+ " and define one or more 'Report Channel: Filesystem' to get it as an option in the list of channels.</p>"
				;
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
				
		String jobID = context.getJobDetail().getKey().getName();
		CFWJob job = CFW.DB.Jobs.selectByID(jobID);
		
		CFWJobsChannelFilesystemSettings filesystemSettings = getContextSettings();
		
		//----------------------------------------
		// Make Ze Path De Folder
		String timestamp = CFW.Time.currentTimestamp().replace(":", ".");
		String folderPath = filesystemSettings.folderPath()+"/"+timestamp;
				
		//----------------------------------------
		// Create Mail Content
		String reportContent = Strings.isNullOrEmpty(contentHTML) ? content : contentHTML;
				
		try {
			
			//----------------------------------------
			// Create Dir
			Path path = Paths.get(folderPath);
			
			if( !Files.isDirectory(path) ){
				Files.createDirectories(path);
			}

			//----------------------------------------
			// Create report.html
			try {
				Path htmlPath = Paths.get(folderPath, "/report.html");
				Files.write(htmlPath, reportContent.getBytes());
			}catch(IOException e) { 
				writeError(jobID, e); 
			}
			
			//------------------------
			// Custom Notes
			String customNotes = alertObject.getCustomNotes();
			try {
				
				if( !Strings.isNullOrEmpty(customNotes) 
				 && !customNotes.trim().toLowerCase().equals("null") ) {
					Path customNotesPath = Paths.get(folderPath, "/customNotes.txt");
					Files.write(customNotesPath, customNotes.getBytes());
				}
				
			} catch(IOException e) { 
				writeError(jobID, e); 
			}
			//----------------------------------------
			// Create jobdetails.json
			try {
				Path jobdetailsPath = Paths.get(folderPath, "/jobdetails.json");
				Files.write(jobdetailsPath, CFW.JSON.toJSONPretty(job).getBytes());
			} catch(IOException e) { 
				writeError(jobID, e); 
			}
			
			//----------------------------------------
			// Create attachment files
			for(Entry<String, String> entry : attachments.entrySet()) {
				
				try {
					Path currentPath = Paths.get(folderPath, "/", entry.getKey());
					Files.write(currentPath, entry.getValue().getBytes());
				}catch(IOException e) { 
					writeError(jobID, e); 
				}

			}
			
		}catch(IOException e) { 
			writeError(jobID, e); 
		}
		
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	private CFWJobsChannelFilesystemSettings getContextSettings() {
		int environmentID = CFWJobsChannelFilesystemSettings.extractIDFromUniqueName(uniqueName);
		CFWJobsChannelFilesystemSettings fileSystemSettings =
				CFWJobsChannelFilesystemSettingsManagement.getEnvironment(environmentID);
		return fileSystemSettings;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	private void writeError(String jobID, IOException e) {
		new CFWLog(logger).severe(
				  "Filesystem(Job ID: "+jobID+"): Error while writing report: "
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
			CFWJobsChannelFilesystemSettings filesystemSettings = getContextSettings();
			
			
			HashMap<Integer, Object> settingsMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(
					CFWJobsChannelFilesystemSettings.SETTINGS_TYPE
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

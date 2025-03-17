package com.xresch.cfw.features.jobs.channels;

import java.util.HashMap;

import org.quartz.JobExecutionContext;

import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWJobsChannel {
	
	String uniqueName = "";
	
	/*************************************************************************
	 * Set a unique name for this channel.
	 * This will be used in the UI and as ID.
	 * Changing this name afterwards will corrupt existing Alerts.
	 *************************************************************************/
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
		
	/*************************************************************************
	 * Return a description for this channel.
	 *************************************************************************/
	public abstract String manualPageTitle();
	
	/*************************************************************************
	 * Return the content of the manual page as HTML.
	 *************************************************************************/
	public abstract String manualPageContent();
	
	/*************************************************************************
	 * Optional: Data will be given to this method if available, use the data
	 * if useful or ignore it.
	 * @param name the name for the data
	 * @param filetype like "txt" or "json" in case it is converted into a file 
	 * @param data the data to be added
	 *************************************************************************/
	public abstract void addTextData(String name, String filetype, String data);
	
	/*************************************************************************
	 * Send the alerts.
	 * @param context the context of the job
	 * @param messageType type of the message
	 * @param alertObject the alerting object
	 * @param uniqueUsers the users to alert
	 * @param content the report content as plain text
	 * @param contentHTML the report content as HTML
	 *************************************************************************/
	public abstract void sendReport(JobExecutionContext context, MessageType messageType, CFWJobsAlertObject alertObject, HashMap<Integer, User> uniqueUsers, String subject, String content, String contentHTML);
	
	/*************************************************************************
	 * Return if the user is able to select this channel.
	 *************************************************************************/
	public abstract boolean hasPermission(User user);
	
	/*************************************************************************
	 * Returns the unique name for this channel.
	 * This will be used in the UI and as ID.
	 * Changing this name afterwards might corrupt existing Alerts.
	 *************************************************************************/
	public String getUniqueName() {
		return uniqueName;
	};
	
	/*************************************************************************
	 * Returns the label for this channel that should be shown on the UI.
	 *************************************************************************/
	public abstract String getLabel();
	
}

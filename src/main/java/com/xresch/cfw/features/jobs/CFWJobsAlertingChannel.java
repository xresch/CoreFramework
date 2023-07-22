package com.xresch.cfw.features.jobs;

import java.util.HashMap;

import org.quartz.JobExecutionContext;

import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public abstract class CFWJobsAlertingChannel {
	
	/*************************************************************************
	 * Return a unique name for this channel.
	 * This will be used in the UI and as ID.
	 * Changing this name afterwards will corrupt existing Alerts.
	 *************************************************************************/
	public abstract String uniqueName();
	
	
	/*************************************************************************
	 * Return a description for this channel.
	 *************************************************************************/
	public abstract String channelDescription();
	
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
	 * @param context TODO
	 * @param messageType TODO
	 *************************************************************************/
	public abstract void sendAlerts(JobExecutionContext context, MessageType messageType, CFWJobsAlertObject alertObject, HashMap<Integer, User> uniqueUsers, String subject, String content, String contentHTML);
	
	/*************************************************************************
	 * Return if the user is able to select this channel.
	 *************************************************************************/
	public abstract boolean hasPermission(User user);
	
}

package com.xresch.cfw.features.jobs;

import com.xresch.cfw.features.usermgmt.User;

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
	 * Send the alerts.
	 *************************************************************************/
	public abstract boolean sendAlerts(CFWJobsAlertObject alertObject, String Subject, String content);
	
	/*************************************************************************
	 * Return if the user is able to select this channel.
	 *************************************************************************/
	public abstract boolean hasPermission(User user);
	
	
}

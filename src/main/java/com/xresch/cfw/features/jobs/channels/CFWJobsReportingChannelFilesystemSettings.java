package com.xresch.cfw.features.jobs.channels;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class CFWJobsReportingChannelFilesystemSettings extends AbstractContextSettings {
	
	public static final String PREFIX_LABEL = "Filesystem: ";
	public static final String PREFIX_CHANNEL_NAME = "channel-";

	public static final String SETTINGS_TYPE = "Report Channel: Filesystem";
	
	private CFWJobsReportingChannelFilesystemSettings INSTANCE;
	private static Logger logger = CFWLog.getLogger(CFWJobsReportingChannelFilesystemSettings.class.getName());
	
	public enum CFWJobsReportingChannelFilesystemSettingsFields {
		FOLDER_PATH

	}
	
	private CFWField<String> folderPath = CFWField.newString(FormFieldType.TEXT, CFWJobsReportingChannelFilesystemSettingsFields.FOLDER_PATH)
			.setDescription("The folder path where the reports should be written.")
			.setValue("C:\\Temp")
			;

	/*********************************************************************
	 * 
	 *********************************************************************/
	public CFWJobsReportingChannelFilesystemSettings() {
		INSTANCE = this;
		this.addFields(folderPath);
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean isProperlyDefined(){
		
		if(folderPath.getValue() == null
		|| folderPath.getValue().isBlank()
		){
			return false;
		}
		
		return true;
	}
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public boolean isDeletable(int settingsID) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String createChannelLabel() {
		return PREFIX_LABEL + this.getDefaultObject().name();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String createChannelUniqueName() {
		return  PREFIX_CHANNEL_NAME + this.getDefaultObject().id();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public static int extractIDFromUniqueName(String uniqueName) {
		return Integer.parseInt( uniqueName.replace(PREFIX_CHANNEL_NAME, "") );
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String getName() {
		return PREFIX_LABEL + this.getDefaultObject().name();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String folderPath() {
		return folderPath.getValue();
	}
	
	
	
	
}

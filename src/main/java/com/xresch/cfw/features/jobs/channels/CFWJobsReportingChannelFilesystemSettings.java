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
	
	public static final String LABEL_PREFIX = "Filesystem: ";

	public static final String SETTINGS_TYPE = "Reporting Channel: Filesystem";
	
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
	public String createReportChannelLabel() {
		return LABEL_PREFIX + this.getDefaultObject().name();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String extractSettingsNameFromLabel(String label) {
		return label.replace(LABEL_PREFIX, "");
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String getName() {
		return LABEL_PREFIX + this.getDefaultObject().name();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String folderPath() {
		return folderPath.getValue();
	}
	
	
	
	
}

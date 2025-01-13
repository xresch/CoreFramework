package com.xresch.cfw.extensions.cli;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class CFWJobsChannelCLISettings extends AbstractContextSettings {
	
	private static Logger logger = CFWLog.getLogger(CFWJobsChannelCLISettings.class.getName());
	
	private CFWJobsChannelCLISettings INSTANCE;
	
	public static final String PREFIX_LABEL = "CLI: ";
	public static final String PREFIX_CHANNEL_NAME = "channel-cli-";

	public static final String SETTINGS_TYPE = "Report Channel: CLI";
			
	private CFWField<String> workingDir = CFWCLIExtensionsCommon.createSettingsFieldWorkingDir();
	private CFWField<String> commands = CFWCLIExtensionsCommon.createSettingsFieldCommands();
	private CFWField<LinkedHashMap<String, String>> envVariables = CFWCLIExtensionsCommon.createSettingsFieldEnvVariables();
	private CFWField<Integer> timeout = CFWCLIExtensionsCommon.createSettingsFieldTimeout();
	

	/*********************************************************************
	 * 
	 *********************************************************************/
	public CFWJobsChannelCLISettings() {
		INSTANCE = this;
		this.addFields(workingDir, commands, envVariables, timeout);
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean isProperlyDefined(){
		
		if(commands.getValue() == null
		|| commands.getValue().isBlank()
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
	public String workingDir() {
		return workingDir.getValue();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String commands() {
		return commands.getValue();
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public LinkedHashMap<String, String> envVariables() {
		return envVariables.getValue();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public Integer timeout() {
		return timeout.getValue();
	}
	
}

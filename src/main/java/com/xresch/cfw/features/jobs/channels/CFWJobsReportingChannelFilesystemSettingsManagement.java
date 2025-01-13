package com.xresch.cfw.features.jobs.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class CFWJobsReportingChannelFilesystemSettingsManagement {
	
	private static Logger logger = CFWLog.getLogger(CFWJobsReportingChannelFilesystemSettingsManagement.class.getName());
		
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, CFWJobsReportingChannelFilesystemSettings> environments = new HashMap<Integer, CFWJobsReportingChannelFilesystemSettings>();
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private CFWJobsReportingChannelFilesystemSettingsManagement() {
		//hide public constructor
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(CFWJobsReportingChannelFilesystemSettings.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				
				//-------------------------------------
				// Get Variables
				CFWJobsReportingChannelFilesystemSettings oldSettings = environments.get(setting.getDefaultObject().id());
				CFWJobsReportingChannelFilesystemSettings newSettings = (CFWJobsReportingChannelFilesystemSettings)setting;
				
				//-------------------------------------
				// Check for Name change
				if(oldSettings != null) {
					CFW.Registry.JobsReporting.removeChannel(oldSettings.createChannelUniqueName());
				}
				CFWJobsReportingChannelFilesystemSettingsManagement.createChannelSettings(newSettings);
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				CFWJobsReportingChannelFilesystemSettings oldSettings = environments.remove(typeSettings.getDefaultObject().id());
				
				CFW.Registry.JobsReporting.removeChannel(oldSettings.createChannelUniqueName());
				
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createChannelSettingsAll();
		isInitialized = true;
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createChannelSettingsAll() {
		// Clear environments
		environments = new HashMap<Integer, CFWJobsReportingChannelFilesystemSettings>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(CFWJobsReportingChannelFilesystemSettings.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			CFWJobsReportingChannelFilesystemSettings current = (CFWJobsReportingChannelFilesystemSettings)settings;
			createChannelSettings(current);
			
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createChannelSettings(CFWJobsReportingChannelFilesystemSettings channelSettings) {
		
		Integer id = channelSettings.getDefaultObject().id();
		
		environments.remove(id);

		if(channelSettings.isProperlyDefined()) {
			
			CFW.Registry.JobsReporting.registerChannel(
					  channelSettings.createChannelUniqueName()
					,  new CFWJobsReportingChannelFilesystem());
			
			environments.put(id, channelSettings);
		}else {
			CFW.Messages.addInfoMessage("Configuration incomplete, at least Folder Path has to be defined.");
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static CFWJobsReportingChannelFilesystemSettings getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static HashMap<Integer, CFWJobsReportingChannelFilesystemSettings> getEnvironmentsAll() {
		if(!isInitialized) { initialize(); }
		
		HashMap<Integer, CFWJobsReportingChannelFilesystemSettings> clone = new HashMap<>();
		clone.putAll(environments);
		
		return  clone;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(CFWJobsReportingChannelFilesystemSettings env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
}

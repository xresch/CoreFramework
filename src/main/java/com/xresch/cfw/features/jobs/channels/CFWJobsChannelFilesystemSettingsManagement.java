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
public class CFWJobsChannelFilesystemSettingsManagement {
	
	private static Logger logger = CFWLog.getLogger(CFWJobsChannelFilesystemSettingsManagement.class.getName());
		
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, CFWJobsChannelFilesystemSettings> environments = new HashMap<Integer, CFWJobsChannelFilesystemSettings>();
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private CFWJobsChannelFilesystemSettingsManagement() {
		//hide public constructor
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(CFWJobsChannelFilesystemSettings.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				
				//-------------------------------------
				// Get Variables
				CFWJobsChannelFilesystemSettings oldSettings = environments.get(setting.getDefaultObject().id());
				CFWJobsChannelFilesystemSettings newSettings = (CFWJobsChannelFilesystemSettings)setting;
				
				//-------------------------------------
				// Check for Name change
				if(oldSettings != null) {
					CFW.Registry.JobsReporting.removeChannel(oldSettings.createChannelUniqueName());
				}
				CFWJobsChannelFilesystemSettingsManagement.createChannelSettings(newSettings);
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				CFWJobsChannelFilesystemSettings oldSettings = environments.remove(typeSettings.getDefaultObject().id());
				
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
		environments = new HashMap<Integer, CFWJobsChannelFilesystemSettings>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(CFWJobsChannelFilesystemSettings.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			CFWJobsChannelFilesystemSettings current = (CFWJobsChannelFilesystemSettings)settings;
			createChannelSettings(current);
			
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createChannelSettings(CFWJobsChannelFilesystemSettings channelSettings) {
		
		Integer id = channelSettings.getDefaultObject().id();
		
		environments.remove(id);

		if(channelSettings.isProperlyDefined()) {
			
			CFW.Registry.JobsReporting.registerChannel(
					  channelSettings.createChannelUniqueName()
					,  new CFWJobsChannelFilesystem());
			
			environments.put(id, channelSettings);
		}else {
			CFW.Messages.addInfoMessage("Configuration incomplete, at least Folder Path has to be defined.");
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static CFWJobsChannelFilesystemSettings getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static HashMap<Integer, CFWJobsChannelFilesystemSettings> getEnvironmentsAll() {
		if(!isInitialized) { initialize(); }
		
		HashMap<Integer, CFWJobsChannelFilesystemSettings> clone = new HashMap<>();
		clone.putAll(environments);
		
		return  clone;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(CFWJobsChannelFilesystemSettings env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
}

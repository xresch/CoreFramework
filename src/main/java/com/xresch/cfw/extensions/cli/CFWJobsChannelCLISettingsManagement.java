package com.xresch.cfw.extensions.cli;

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
public class CFWJobsChannelCLISettingsManagement {
	
	private static Logger logger = CFWLog.getLogger(CFWJobsChannelCLISettingsManagement.class.getName());
		
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, CFWJobsChannelCLISettings> environments = new HashMap<Integer, CFWJobsChannelCLISettings>();
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private CFWJobsChannelCLISettingsManagement() {
		//hide public constructor
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(CFWJobsChannelCLISettings.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				
				//-------------------------------------
				// Get Variables
				CFWJobsChannelCLISettings oldSettings = environments.get(setting.getDefaultObject().id());
				CFWJobsChannelCLISettings newSettings = (CFWJobsChannelCLISettings)setting;
				
				//-------------------------------------
				// Check for Name change
				if(oldSettings != null) {
					CFW.Registry.JobsReporting.removeChannel(oldSettings.createChannelUniqueName());
				}
				CFWJobsChannelCLISettingsManagement.createChannelSettings(newSettings);
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				CFWJobsChannelCLISettings oldSettings = environments.remove(typeSettings.getDefaultObject().id());
				
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
		environments = new HashMap<Integer, CFWJobsChannelCLISettings>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(CFWJobsChannelCLISettings.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			CFWJobsChannelCLISettings current = (CFWJobsChannelCLISettings)settings;
			createChannelSettings(current);
			
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createChannelSettings(CFWJobsChannelCLISettings channelSettings) {
		
		Integer id = channelSettings.getDefaultObject().id();
		
		environments.remove(id);

		if(channelSettings.isProperlyDefined()) {
			
			CFW.Registry.JobsReporting.registerChannel(
					  channelSettings.createChannelUniqueName()
					,  new CFWJobsChannelCLI());
			
			environments.put(id, channelSettings);
		}else {
			CFW.Messages.addInfoMessage("Configuration incomplete, at least a command has to be defined.");
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static CFWJobsChannelCLISettings getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static HashMap<Integer, CFWJobsChannelCLISettings> getEnvironmentsAll() {
		if(!isInitialized) { initialize(); }
		
		HashMap<Integer, CFWJobsChannelCLISettings> clone = new HashMap<>();
		clone.putAll(environments);
		
		return  clone;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(CFWJobsChannelCLISettings env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
}

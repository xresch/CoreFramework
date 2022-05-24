package com.xresch.cfw.features.config;

import java.util.ArrayList;

public abstract class ConfigChangeListener {

	private ArrayList<String> configNames = new ArrayList<String>();
	
	/***********************************************************
	 * Create a new config change listener.
	 * @param configNames the names of the configurations to 
	 *  listen to, any config if null.
	 ***********************************************************/
	public ConfigChangeListener(String ...configNames) {
		for(String name : configNames) {
			this.configNames.add(name.trim());
		}
	}

	/***********************************************************
	 * Will be triggered once if any of the provided configuration
	 * changes.
	 ***********************************************************/
	public abstract void onChange();

	public boolean listensOnConfig(String configName) {
		
		if(configNames.size() == 0) return true;
		
		return configNames.contains(configName.trim());
	}

}

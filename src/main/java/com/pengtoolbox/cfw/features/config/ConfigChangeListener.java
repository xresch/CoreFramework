package com.pengtoolbox.cfw.features.config;

import java.util.ArrayList;

public abstract class ConfigChangeListener {

	private ArrayList<String> configNames = new ArrayList<String>();
	
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
		return configNames.contains(configName.trim());
	}

}

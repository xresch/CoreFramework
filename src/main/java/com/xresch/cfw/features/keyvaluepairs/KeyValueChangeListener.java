package com.xresch.cfw.features.keyvaluepairs;

import java.util.ArrayList;

public abstract class KeyValueChangeListener {

	private ArrayList<String> configNames = new ArrayList<String>();
	
	public KeyValueChangeListener(String ...keys) {
		for(String name : keys) {
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

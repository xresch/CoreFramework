package com.xresch.cfw.features.contextsettings;

import java.util.ArrayList;

public abstract class ContextSettingsChangeListener {

	private ArrayList<String> contextSettingTypes = new ArrayList<String>();
	
	public ContextSettingsChangeListener(String ...contextSettingTypes) {
		for(String name : contextSettingTypes) {
			this.contextSettingTypes.add(name.trim());
		}
	}

	public boolean listensOnType(String configName) {
		if(contextSettingTypes.size() == 0) return true;
		
		return contextSettingTypes.contains(configName.trim());
	}
	
	/***********************************************************
	 * Will be triggered for every context setting that changes.
	 * 
	 ***********************************************************/
	public abstract void onChange(AbstractContextSettings changedSetting, boolean isNew);

	/***********************************************************
	 * Will be triggered for every context setting that is deleted.
	 * 
	 ***********************************************************/
	public abstract void onDeleteOrDeactivate(AbstractContextSettings typeSettings);
}

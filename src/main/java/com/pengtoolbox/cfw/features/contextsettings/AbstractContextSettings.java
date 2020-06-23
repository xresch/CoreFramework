package com.pengtoolbox.cfw.features.contextsettings;

import com.pengtoolbox.cfw.datahandling.CFWObject;

public abstract class AbstractContextSettings extends CFWObject {
	
	private ContextSettings wrapper = null; 
	

	/**************************************************************
	 * Check if the Settings can be deleted. 
	 * @return true if deletable, false otherwise.
	 **************************************************************/
	public abstract boolean isDeletable(int settingsID);


	/**************************************************************
	 * Returns ContextSetting object containing the default fields
	 * like id, type and description.
	 * This will be set by CFW.DB.ContextSettings.getContextSettingsForType(type).
	 **************************************************************/
	public ContextSettings getDefaultObject() {
		return wrapper;
	}


	/**************************************************************
	 * Set the wrapping object.
	 **************************************************************/
	public void setWrapper(ContextSettings wrapper) {
		this.wrapper = wrapper;
		
	}

	


	
	
	
	
}

package com.xresch.cfw.features.contextsettings;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

public abstract class AbstractContextSettings extends CFWObject {
	
	private ContextSettings wrapper = null; 
	

	/**************************************************************
	 * Check if the Settings can be deleted. 
	 * @return true if deletable, false otherwise.
	 **************************************************************/
	public abstract boolean isDeletable(int settingsID);

	/**************************************************************
	 * Override this method to enable monitoring.
	 **************************************************************/
	public boolean isMonitoringEnabled() {
		return false;
	}
	
	/**************************************************************
	 * Override this method, check the status and return the state of the monitor.
	 **************************************************************/
	public CFWStateOption getStatus() {
		return CFWStateOption.NONE;
	}

	/**************************************************************
	 * Returns ContextSetting object containing the default fields
	 * like id, type and description.
	 * This will be set by CFW.DB.ContextSettings.getContextSettingsForType(type).
	 **************************************************************/
	@Override
	public ContextSettings getDefaultObject() {
		return wrapper;
	}
	

	/**************************************************************
	 * Set the wrapping object.
	 **************************************************************/
	public Boolean isActive() {
		return this.wrapper.isActive();
		
	}


	/**************************************************************
	 * Set the wrapping object.
	 **************************************************************/
	public void setWrapper(ContextSettings wrapper) {
		this.wrapper = wrapper;
		
	}

	


	
	
	
	
}

package com.xresch.cfw._main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWAppFeature {
	
	public static final String KEY_VALUE_CATEGORY = "Feature";

	/************************************************************************************
	 * Return the unique name of this feature for the feature management.
	 * If this method returns null(default), the feature will not be visible in the 
	 * Feature Management.
	 * 
	 ************************************************************************************/
	public String getNameForFeatureManagement() {
		return null;
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	public String getDescriptionForFeatureManagement() {
		return null;
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	/************************************************************************************
	 * Register components and objects.
	 ************************************************************************************/
	public abstract void register();
	
	/************************************************************************************
	 * Initialize database with data, for example add additional permissions.
	 ************************************************************************************/
	public abstract void initializeDB();
	
	/************************************************************************************
	 * Add servlets to the application.
	 * This is executed before the application is started.
	 ************************************************************************************/
	public abstract void addFeature(CFWApplicationExecutor app);
	
	/************************************************************************************
	 * Start scheduled tasks
	 ************************************************************************************/
	public abstract void startTasks();
	
	/************************************************************************************
	 * Actions that should be executed when the application is stopped.
	 ************************************************************************************/
	public abstract void stopFeature();
	
	/************************************************************************************
	 * Internal method to determine if the feature is enabled.
	 ************************************************************************************/
	protected boolean isFeatureEnabled() {
		if(getNameForFeatureManagement() == null) {
			return true;
		}else {
			return CFW.DB.KeyValuePairs.getValueAsBoolean(getNameForFeatureManagement());
		}
	}
}

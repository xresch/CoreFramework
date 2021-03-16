package com.xresch.cfw._main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWAppFeature {

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
}

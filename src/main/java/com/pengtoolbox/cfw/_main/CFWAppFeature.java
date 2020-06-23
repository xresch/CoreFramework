package com.pengtoolbox.cfw._main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class CFWAppFeature {

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

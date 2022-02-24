package com.xresch.cfw.spi;

import com.xresch.cfw._main.CFWApplicationExecutor;

/**************************************************************************************************************
 * 
 * The methods of a CFWAppInterface are executed in the following order with other loading mechanisms:
 * <ol>
 * <li>{@link CFWAppInterface#settings()} of the first detected application.</li>
 * <li>{@link CFWAppInterface#register()} of the first detected application.</li>
 * <li>{@link CFWAppFeature#register()} of all registered and enabled features.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#migrateTable() CFWObject.migrateTable()} of all registered CFWObjects.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#createTable() CFWObject.createTable()} of all registered CFWObjects.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#updateTable() CFWObject.updateTable()} of all registered CFWObjects.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#initDB() CFWObject.initDB()} of all registered CFWObjects.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#initDBSecond() CFWObject.initDBSecond()} of all registered CFWObjects.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#initDBThird() CFWObject.initDBThird()} of all registered CFWObjects.</li>
 * <li>{@link CFWAppFeature#initializeDB()} of all registered and enabled features.</li>
 * <li>{@link CFWAppInterface#initializeDB()} of all registered and enabled features.</li>
 * <li>{@link com.xresch.cfw.datahandling.CFWObject#getAPIDefinitions() CFWObject.initDBThird()} of all registered CFWObjects.</li>
 * <li>{@link CFWAppFeature#addFeature()} of all registered and enabled features.</li>
 * <li>{@link CFWAppInterface#startApp()} of all registered and enabled features.</li>
 * <li>{@link CFWAppFeature#startTasks()} of all registered and enabled features.</li>
 * </ol>
 * @author Reto Scheiwiller, (c) Copyright 2021 
 **************************************************************************************************************/
public interface CFWAppInterface {

	/************************************************************************************
	 * Set the application settings. 
	 * You can change CFW.Properties inside this method e.g. for testing purposes.
	 * Do not access the internal DB as it is not initialized yet.
	 ************************************************************************************/
	public void settings();
	
	/************************************************************************************
	 * Register components, features and objects.
	 ************************************************************************************/
	public void register();
	
	/************************************************************************************
	 * Initialize database with data.
	 ************************************************************************************/
	public void initializeDB();
	
	/************************************************************************************
	 * Add servlets and start the application.
	 * Make sure to call app.start();
	 ************************************************************************************/
	public void startApp(CFWApplicationExecutor executor);
	
	
	/************************************************************************************
	 * Start scheduled tasks
	 ************************************************************************************/
	public void startTasks();
	
	/************************************************************************************
	 * Actions that should be executed when the application is stopped.
	 ************************************************************************************/
	public void stopApp();
}

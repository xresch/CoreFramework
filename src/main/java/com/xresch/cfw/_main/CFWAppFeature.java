package com.xresch.cfw._main;

/**************************************************************************************************************
 * Abstract class for creating Application Features.
 * The features are loaded in the order as they were registered.
 * You can either register a Feature by using the annotation @CFWExtensionFeature, or your can register them directly 
 * using CFW.Registry.Features.addFeature(). The Features are loaded in the order they are registered. When using
 * the annotation, the order is not predictable.
 * 
 * For the order in which the methods are executed see {@link CFWAppInterface}
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWAppFeature {
	
	public static final String KEY_VALUE_CATEGORY = "Feature";
	public static final String KEY_VALUE_PREFIX = "FeatureEnabled:";
	

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
	 * Return if the managed feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	/************************************************************************************
	 * Register components and objects.
	 ************************************************************************************/
	public abstract void register();
	
	/************************************************************************************
	 * Initialize the INTERNAL database with data, for example add configurations
	 * and permissions.
	 * To pre-load caches or creating connections to other databases, which are mandatory
	 * for the application to work properly, do those actions in the method 
	 * {@link #addFeature(CFWApplicationExecutor app) addFeature}.
	 ************************************************************************************/
	public abstract void initializeDB();
	
	/************************************************************************************
	 * Add servlets to the application.
	 * This is executed before the application is started.
	 ************************************************************************************/
	public abstract void addFeature(CFWApplicationExecutor app);
	
	/************************************************************************************
	 * Start scheduled tasks. This tasks will be started in all running modes.
	 * If you want to start those only in certain modes, make sure to add an if statement 
	 * like:
	 * <pre><code>
	 *  String mode = CFW.Properties.MODE;
	 *  if(mode.equals(CFW.MODE_FULL) || mode.equals(CFW.MODE_APP))
	 *</code></pre>
	 * 
	 ************************************************************************************/
	public abstract void startTasks();
	
	/************************************************************************************
	 * Actions that should be executed when the application is stopped.
	 ************************************************************************************/
	public abstract void stopFeature();
	
	/************************************************************************************
	 * Internal method to determine if the feature is enabled.
	 ************************************************************************************/
	public boolean isFeatureEnabled() {
		if(getNameForFeatureManagement() == null) {
			return true;
		}else {
			return CFW.DB.KeyValuePairs.getValueAsBoolean(KEY_VALUE_PREFIX+getNameForFeatureManagement());
		}
	}
}

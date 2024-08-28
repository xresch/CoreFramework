package com.xresch.cfw.features.core.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.logging.CFWLog;


public class SSOProviderSettingsManagement {
	
	private static Logger logger = CFWLog.getLogger(SSOProviderSettingsManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated  example environment
	private static HashMap<Integer, SSOProviderSettings> environments = new HashMap<Integer, SSOProviderSettings>();
	
	private SSOProviderSettingsManagement() {
		// hide public constructor
	}
	
	private static ArrayList<Class<? extends SSOProviderSettings>> ssoProviderSettingsList = new ArrayList<Class<? extends SSOProviderSettings>>();
	
	/***********************************************************************
	 * Adds a SSOProviderSettings class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void register(Class<? extends SSOProviderSettings> objectClass)  {
		ssoProviderSettingsList.add(objectClass);
	}
	
	/***********************************************************************
	 * Removes a SSOProviderSettings class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void unregister(Class<? extends SSOProviderSettings> objectClass)  {
		ssoProviderSettingsList.remove(objectClass);
	}
	
	/***********************************************************************
	 * Removes a SSOProviderSettings class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<Class<? extends SSOProviderSettings>> getSSOProviderSettingsList()  {
		return ssoProviderSettingsList;
	}
	
	/***********************************************************************
	 * Get a list of SSOProviderSettings instances.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<SSOProviderSettings> getSSOProviderSettingsInstances()  {
		ArrayList<SSOProviderSettings> instanceArray = new ArrayList<SSOProviderSettings>();
		
		for(Class<? extends SSOProviderSettings> clazz : ssoProviderSettingsList) {
			try {
				SSOProviderSettings instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
		//-----------------------------------------
		// Add Listener to react to changes
		for(SSOProviderSettings settingsInstance : getSSOProviderSettingsInstances()) {

			ContextSettingsChangeListener listener = 
					new ContextSettingsChangeListener(settingsInstance.getSettingsType()) {
				
				@Override
				public void onChange(AbstractContextSettings setting, boolean isNew) {
					SSOProviderSettings env = (SSOProviderSettings)setting;
					SSOProviderSettingsManagement.createEnvironment(env);
				}
				
				@Override
				public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
					environments.remove(typeSettings.getDefaultObject().id());
				}
			};
			
			CFW.DB.ContextSettings.addChangeListener(listener);
		}
		
		//-----------------------------------------
		// Add Listener to react to changes
		createEnvironments();
		isInitialized = true;
		
	
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironments() {
		// Clear environments
		environments = new HashMap<Integer, SSOProviderSettings>();
		
		for(SSOProviderSettings settingsInstance : getSSOProviderSettingsInstances()) {

			ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(settingsInstance.getSettingsType(), true);
			
			for(AbstractContextSettings settings : settingsArray) {
				SSOProviderSettings current = (SSOProviderSettings)settings;
				createEnvironment(current);
			}
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(SSOProviderSettings environment) {

		environments.remove(environment.getDefaultObject().id());
		
		environments.put(environment.getDefaultObject().id(), environment);
		
	}
	
	/************************************************************************
	 * Return true if there is at least one valid environment.
	 * 
	 ************************************************************************/
	public static boolean hasValidEnvironment() {
		if(!isInitialized) { initialize(); }
		
		for(SSOProviderSettings setting : environments.values()) {
			if(setting.isDefined()) {
				return true;
			}
		}
		
		return false;
		
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static String getHTMLButtonsForLoginPage(String url) {
		
		String encodedURL = CFW.HTTP.encode(url);
		
		String urlParam = "";
		if(!Strings.isNullOrEmpty(encodedURL)) {
			urlParam = "&url="+encodedURL;
		}
		StringBuilder builder = new StringBuilder();
		
		for(SSOProviderSettings setting : environments.values()) {
			if(setting.isDefined()) {
				int settingsid = setting.getDefaultObject().id(); 
				String settingsName = setting.getDefaultObject().name(); 
				builder.append("<a class=\"btn btn-primary w-100\" role=\"button\" href=\""+FeatureCore.SERVLET_PATH_LOGIN+"?ssoid="+settingsid+urlParam+"\" >"+settingsName+"</a>");
			}
		}
		
		return builder.toString();
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static SSOProviderSettings getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
}

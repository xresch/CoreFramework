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


public class SSOOpenIDConnectProviderManagement {
	
	private static Logger logger = CFWLog.getLogger(SSOOpenIDConnectProviderManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated  example environment
	private static HashMap<Integer, SSOOpenIDConnectProvider> environments = new HashMap<Integer, SSOOpenIDConnectProvider>();
	
	private SSOOpenIDConnectProviderManagement() {
		// hide public constructor
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
	
		//-----------------------------------------
		// Add Listener to react to changes
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(SSOOpenIDConnectProvider.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				SSOOpenIDConnectProvider env = (SSOOpenIDConnectProvider)setting;
				SSOOpenIDConnectProviderManagement.createEnvironment(env);
			}
			
			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				environments.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
		
	
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironments() {
		// Clear environments
		environments = new HashMap<Integer, SSOOpenIDConnectProvider>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(SSOOpenIDConnectProvider.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			SSOOpenIDConnectProvider current = (SSOOpenIDConnectProvider)settings;
			createEnvironment(current);
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(SSOOpenIDConnectProvider environment) {

		environments.remove(environment.getDefaultObject().id());
		
		environments.put(environment.getDefaultObject().id(), environment);
		
	}
	
	/************************************************************************
	 * Return true if there is at least one valid environment.
	 * 
	 ************************************************************************/
	public static boolean hasValidEnvironment() {
		if(!isInitialized) { initialize(); }
		
		for(SSOOpenIDConnectProvider setting : environments.values()) {
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
		
		for(SSOOpenIDConnectProvider setting : environments.values()) {
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
	public static SSOOpenIDConnectProvider getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
}

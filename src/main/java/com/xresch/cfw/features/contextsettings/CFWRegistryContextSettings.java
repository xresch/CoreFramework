package com.xresch.cfw.features.contextsettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryContextSettings {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryContextSettings.class.getName());
	
	// contains type and class
	private static TreeMap<String, Class<? extends AbstractContextSettings>> contextSettings = new TreeMap<String, Class<? extends AbstractContextSettings>>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void register(String type, Class<? extends AbstractContextSettings> environmentClass)  {		
		contextSettings.put(type, environmentClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void remove(String type)  {
		contextSettings.remove(type);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, Class<? extends AbstractContextSettings>> getContextSettingList()  {
		return contextSettings;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<AbstractContextSettings> createContextSettingInstances()  {
		ArrayList<AbstractContextSettings> instanceArray = new ArrayList<AbstractContextSettings>();
		
		for(Class<? extends AbstractContextSettings> clazz : contextSettings.values()) {
			try {
				AbstractContextSettings instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	/***********************************************************************
	 * Get a new instance for the specified ContextSetting.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static AbstractContextSettings createContextSettingInstance(String type)  {
		
		AbstractContextSettings instance = null;
		Class<? extends AbstractContextSettings> clazz =  contextSettings.get(type);
		try {
			if(clazz != null) {
				instance = clazz.newInstance();
			}
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
	
	
	/***********************************************************************
	 * Get a list of all registered ContextSetting s.
	 * 
	 ***********************************************************************/
	public static ArrayList<String> getContextSettingTypes()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(contextSettings.keySet());
		
		return Array;
	}
	
}

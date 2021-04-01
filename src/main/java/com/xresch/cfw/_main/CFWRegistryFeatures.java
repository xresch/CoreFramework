package com.xresch.cfw._main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryFeatures {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryFeatures.class.getName());
	
	private static LinkedHashSet<Class<? extends CFWAppFeature>> featureClassSet = new LinkedHashSet<>();
	
	//Map of Managed Features: Unique Name and Featue Class
	private static LinkedHashMap<String, CFWAppFeature> ManagedFeatures = new LinkedHashMap<>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void addFeature(Class<? extends CFWAppFeature> objectClass)  {
		featureClassSet.add(objectClass);
		
		CFWAppFeature instance = getFeatureInstance(objectClass);
		if(instance != null && instance.getNameForFeatureManagement() != null) {
			ManagedFeatures.put(instance.getNameForFeatureManagement(), instance);
		}
	}
		
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeCFWObject(Class<? extends CFWAppFeature> objectClass)  {
		featureClassSet.remove(objectClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static LinkedHashSet<Class<? extends CFWAppFeature>> getCFWObjectList()  {
		return featureClassSet;
	}
	
	/***********************************************************************
	 * Get a list of CFWObject instances.
	 * @param objectClass
	 ***********************************************************************/
	protected static CFWAppFeature getFeatureInstance(Class<? extends CFWAppFeature> objectClass)  {
		CFWAppFeature instance = null;
		try {
			instance = objectClass.newInstance();
			return instance;
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+objectClass.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
	/***********************************************************************
	 * Get a list of CFWObject instances.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<CFWAppFeature> getFeatureInstances()  {
		ArrayList<CFWAppFeature> instanceArray = new ArrayList<>();
		
		for(Class<? extends CFWAppFeature> clazz : featureClassSet) {
			CFWAppFeature instance = getFeatureInstance(clazz);
			if(instance != null) {
				instanceArray.add(instance);
			}
		}
		return instanceArray;
	}
	
}

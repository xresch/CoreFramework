package com.xresch.cfw._main;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryFeatures {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryFeatures.class.getName());
	
	private static LinkedHashSet<Class<? extends CFWAppFeature>> featureClassSet = new LinkedHashSet<>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void addFeature(Class<? extends CFWAppFeature> objectClass)  {
		featureClassSet.add(objectClass);
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
	public static ArrayList<CFWAppFeature> getFeatureInstances()  {
		ArrayList<CFWAppFeature> instanceArray = new ArrayList<>();
		
		for(Class<? extends CFWAppFeature> clazz : featureClassSet) {
			try {
				CFWAppFeature instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
}

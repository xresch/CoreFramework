package com.pengtoolbox.cfw._main;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryFeatures {
	
	public static Logger logger = CFWLog.getLogger(CFWRegistryFeatures.class.getName());
	
	private static ArrayList<Class<? extends CFWAppFeature>> featureClassArray = new ArrayList<Class<? extends CFWAppFeature>>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void addFeature(Class<? extends CFWAppFeature> objectClass)  {
		featureClassArray.add(objectClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeCFWObject(Class<? extends CFWAppFeature> objectClass)  {
		featureClassArray.remove(objectClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<Class<? extends CFWAppFeature>> getCFWObjectList()  {
		return featureClassArray;
	}
	
	/***********************************************************************
	 * Get a list of CFWObject instances.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<CFWAppFeature> getFeatureInstances()  {
		ArrayList<CFWAppFeature> instanceArray = new ArrayList<CFWAppFeature>();
		
		for(Class<? extends CFWAppFeature> clazz : featureClassArray) {
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

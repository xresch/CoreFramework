package com.pengtoolbox.cfw.datahandling;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryObjects {
	
	public static Logger logger = CFWLog.getLogger(CFWRegistryObjects.class.getName());
	
	private static ArrayList<Class<? extends CFWObject>> cfwObjects = new ArrayList<Class<? extends CFWObject>>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void addCFWObject(Class<? extends CFWObject> objectClass)  {
		cfwObjects.add(objectClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeCFWObject(Class<? extends CFWObject> objectClass)  {
		cfwObjects.remove(objectClass);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<Class<? extends CFWObject>> getCFWObjectList()  {
		return cfwObjects;
	}
	
	/***********************************************************************
	 * Get a list of CFWObject instances.
	 * @param objectClass
	 ***********************************************************************/
	public static ArrayList<CFWObject> getCFWObjectInstances()  {
		ArrayList<CFWObject> instanceArray = new ArrayList<CFWObject>();
		
		for(Class<? extends CFWObject> clazz : cfwObjects) {
			try {
				CFWObject instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
}

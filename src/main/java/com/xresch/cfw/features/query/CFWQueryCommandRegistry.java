package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryCommandRegistry {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandRegistry.class.getName());
	
	// command name and command class
	private static LinkedHashMap<String, Class<? extends CFWQueryCommand>> queryCommandMap = new LinkedHashMap<String, Class<? extends CFWQueryCommand>>();
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void register(CFWQueryCommand command)  {
		
		if( queryCommandMap.containsKey(command.uniqueName()) ) {
			new CFWLog(logger).severe("A JobTask with the name '"+command.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		queryCommandMap.put(command.uniqueName(), command.getClass());
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void remove(String commandName)  {
		queryCommandMap.remove(commandName);
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static LinkedHashMap<String, Class<? extends CFWQueryCommand>> getQueryCommandList()  {
		return queryCommandMap;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<CFWQueryCommand> createQueryCommandInstances()  {
		ArrayList<CFWQueryCommand> instanceArray = new ArrayList<CFWQueryCommand>();
		
		for(Class<? extends CFWQueryCommand> clazz : queryCommandMap.values()) {
			try {
				CFWQueryCommand instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	/***********************************************************************
	 * Get a new instance for the specified QueryCommand.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static CFWQueryCommand createQueryCommandInstance(String commandName)  {
		
		CFWQueryCommand instance = null;
		Class<? extends CFWQueryCommand> clazz =  queryCommandMap.get(commandName);
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
	 * Get a list of all registered QueryCommand s.
	 * 
	 ***********************************************************************/
	public static ArrayList<String> getQueryCommandNames()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(queryCommandMap.keySet());
		
		return Array;
	}
	
}

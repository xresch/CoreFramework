package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryRegistry {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryRegistry.class.getName());
	
	// command name and command class
	private static TreeMap<String, Class<? extends CFWQueryCommand>> queryCommandMap = new TreeMap<>();
	
	
	// command name and command class
	private static TreeMap<String, Class<? extends CFWQuerySource>> querySourceMap = new TreeMap<>();
	
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void registerCommand(CFWQueryCommand command)  {
		
		for(String name : command.uniqueNameAndAliases()) {
			String lowercaseName = name.trim().toLowerCase();
			
			if( queryCommandMap.containsKey(lowercaseName) ) {
				new CFWLog(logger).severe("A Command with the name '"+command.uniqueNameAndAliases()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
				return;
			}
			
			queryCommandMap.put(lowercaseName, command.getClass());
		}
		
		
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeCommand(String commandName)  {
		queryCommandMap.remove(commandName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, Class<? extends CFWQueryCommand>> getCommandList()  {
		return queryCommandMap;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<CFWQueryCommand> createCommandInstances()  {
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
	public static CFWQueryCommand createCommandInstance(CFWQuery parent, String commandName)  {
		
		CFWQueryCommand instance = null;
		Class<? extends CFWQueryCommand> clazz =  queryCommandMap.get(commandName.trim().toLowerCase());
		try {
			if(clazz != null) {
				instance = clazz.getConstructor(CFWQuery.class).newInstance(parent);
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
	public static ArrayList<String> getCommandNames()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(queryCommandMap.keySet());
		
		return Array;
	}
	
	/***********************************************************************
	 * Return true if a command with the specified name is registered, false
	 * otherwise.
	 * 
	 ***********************************************************************/
	public static boolean commandExists(String commandName)  {
		return queryCommandMap.containsKey(commandName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void registerSource(CFWQuerySource source)  {
		String lowercaseName = source.uniqueName().trim().toLowerCase();
		if( querySourceMap.containsKey(lowercaseName) ) {
			new CFWLog(logger).severe("A JobTask with the name '"+source.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		querySourceMap.put(lowercaseName, source.getClass());
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeSource(String sourceName)  {
		querySourceMap.remove(sourceName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Removes a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, Class<? extends CFWQuerySource>> getSourceList()  {
		return querySourceMap;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<CFWQuerySource> createSourceInstances()  {
		ArrayList<CFWQuerySource> instanceArray = new ArrayList<CFWQuerySource>();
		
		for(Class<? extends CFWQuerySource> clazz : querySourceMap.values()) {
			try {
				CFWQuerySource instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	/***********************************************************************
	 * Get a new instance for the specified QuerySource.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static CFWQuerySource createSourceInstance(CFWQuery parent, String sourceName)  {
		
		CFWQuerySource instance = null;
		Class<? extends CFWQuerySource> clazz =  querySourceMap.get(sourceName.trim().toLowerCase());
		try {
			if(clazz != null) {
				instance = clazz.getConstructor(CFWQuery.class).newInstance(parent);
			}
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
	
	
	/***********************************************************************
	 * Get a list of all registered QuerySource s.
	 * 
	 ***********************************************************************/
	public static ArrayList<String> getSourceNames()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(querySourceMap.keySet());
		
		return Array;
	}
	
	/***********************************************************************
	 * Return true if a source with the specified name is registered, false
	 * otherwise.
	 * 
	 ***********************************************************************/
	public static boolean sourceExists(String sourceName)  {
		return querySourceMap.containsKey(sourceName.trim().toLowerCase());
	}
	
}

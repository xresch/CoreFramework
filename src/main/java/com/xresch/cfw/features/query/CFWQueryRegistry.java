package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryRegistry {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryRegistry.class.getName());
	
	// source name and class
	private static TreeMap<String, Class<? extends CFWQuerySource>> querySourceMap = new TreeMap<>();
	
	// command name and class
	private static TreeMap<String, Class<? extends CFWQueryCommand>> queryCommandMap = new TreeMap<>();
	
	// function name and class
	private static TreeMap<String, Class<? extends CFWQueryFunction>> queryFunctionMap = new TreeMap<>();
	
	// Cache source instances for internal checks
	private static TreeMap<String, CFWQuerySource> sourceInstanceCache = new TreeMap<>();

	//############################################################################################
	// SOURCE RELATED METHODS
	//############################################################################################

	/***********************************************************************
	 * Adds a CFWObject class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void registerSource(CFWQuerySource source)  {
		String lowercaseName = source.uniqueName().trim().toLowerCase();
		if( querySourceMap.containsKey(lowercaseName) ) {
			new CFWLog(logger).severe("A source with the name '"+source.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
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
	 * Return true if the user can use the selected source
	 * 
	 ***********************************************************************/
	public static boolean checkSourcePermission(String sourceName, User user)  {
		
		if(!sourceExists(sourceName)) { return false; }
		
		if(!sourceInstanceCache.containsKey(sourceName)) {
			sourceInstanceCache.put(sourceName, createSourceInstance(new CFWQuery(), sourceName));
		}
		
		CFWQuerySource sourceToCheck = sourceInstanceCache.get(sourceName);
		
		if(CFW.Context.Request.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN)) {
			return true;
		}
		return sourceToCheck.hasPermission(user);
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
	public static TreeMap<String, CFWQuerySource> createSourceInstances(CFWQuery parent)  {
		
		TreeMap<String, CFWQuerySource> instanceMap = new TreeMap<>();
		
		for(Entry<String, Class<? extends CFWQuerySource>> entry : querySourceMap.entrySet()) {
			try {
				String sourceName = entry.getKey();
				Class<? extends CFWQuerySource> clazz = entry.getValue();
				CFWQuerySource instance = clazz.getConstructor(CFWQuery.class).newInstance(parent);
				instanceMap.put(sourceName, instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Command: "+e.getMessage(), e);
			}
		}
		return instanceMap;
		
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
	
	//############################################################################################
	// COMMAND RELATED METHODS
	//############################################################################################

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
	 * Returns a list of commands by tags without aliases.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, ArrayList<CFWQueryCommand>> getCommandsByTags()  {
		TreeMap<String, ArrayList<CFWQueryCommand>> commandsByTags = new TreeMap<>();
		CFWQuery pseudoQuery = new CFWQuery();
		
		for(Entry<String, Class<? extends CFWQueryCommand>> entry : queryCommandMap.entrySet()) {
			CFWQueryCommand current = createCommandInstance(pseudoQuery, entry.getKey());
			
			// skip aliases
			if( !entry.getKey().equals(current.getUniqueName())) { continue; }
			
			for(String tag : current.getTags()) {
				if( !commandsByTags.containsKey(tag) ){
					commandsByTags.put(tag, new ArrayList<CFWQueryCommand>() );
				}
				commandsByTags.get(tag).add(current);
			}
		}
		
		return commandsByTags;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static TreeMap<String, CFWQueryCommand> createCommandInstances(CFWQuery parent)  {
		TreeMap<String, CFWQueryCommand> instanceMap = new TreeMap<>();
		
		for(Entry<String, Class<? extends CFWQueryCommand>> entry : queryCommandMap.entrySet()) {
			try {
				String commandName = entry.getKey();
				Class<? extends CFWQueryCommand> clazz = entry.getValue();
				CFWQueryCommand instance = clazz.getConstructor(CFWQuery.class).newInstance(parent);
				instanceMap.put(commandName, instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Command: "+e.getMessage(), e);
			}
		}
		return instanceMap;
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
	
	//############################################################################################
	// FUNCTION RELATED METHODS
	//############################################################################################

	/***********************************************************************
	 * Adds a CFWQueryFunction class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void registerFunction(CFWQueryFunction function)  {
		
		String lowercaseName = function.uniqueName().trim().toLowerCase();
		if( queryFunctionMap.containsKey(lowercaseName) ) {
			new CFWLog(logger).severe("A function with the name '"+function.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		queryFunctionMap.put(lowercaseName, function.getClass());
		
	}
	
	/***********************************************************************
	 * Removes a CFWQueryFunction class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeFunction(String functionName)  {
		queryFunctionMap.remove(functionName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Returns the map of function names and function classes.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, Class<? extends CFWQueryFunction>> getFunctionList()  {
		return queryFunctionMap;
	}
	
	
	/***********************************************************************
	 * 
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, ArrayList<CFWQueryFunction>> getFunctionsByTags()  {
		TreeMap<String, ArrayList<CFWQueryFunction>> functionsByTags = new TreeMap<>();
		CFWQuery pseudoQuery = new CFWQuery();
		
		for(Entry<String, Class<? extends CFWQueryFunction>> entry : queryFunctionMap.entrySet()) {
			CFWQueryFunction current = createFunctionInstance(pseudoQuery.getContext(), entry.getKey());
			
			for(String tag : current.getTags()) {
				if( !functionsByTags.containsKey(tag) ){
					functionsByTags.put(tag, new ArrayList<CFWQueryFunction>() );
				}
				functionsByTags.get(tag).add(current);
			}
		}
		
		return functionsByTags;
	}
	
	/***********************************************************************
	 * Get a list of Environment instances.
	 * 
	 ***********************************************************************/
	public static TreeMap<String, CFWQueryFunction> createFunctionInstances(CFWQuery parent)  {
		TreeMap<String, CFWQueryFunction> instanceMap = new TreeMap<>();
		
		for(Entry<String, Class<? extends CFWQueryFunction>> entry : queryFunctionMap.entrySet()) {
			try {
				String functionName = entry.getKey();
				Class<? extends CFWQueryFunction> clazz = entry.getValue();
				CFWQueryFunction instance = clazz.getConstructor(CFWQuery.class).newInstance(parent);
				instanceMap.put(functionName, instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Function: "+e.getMessage(), e);
			}
		}
		return instanceMap;
	}
	
	/***********************************************************************
	 * Get a new instance for the specified QueryFunction.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static CFWQueryFunction createFunctionInstance(CFWQueryContext context, String functionName)  {
		
		CFWQueryFunction instance = null;
		Class<? extends CFWQueryFunction> clazz =  queryFunctionMap.get(functionName.trim().toLowerCase());
		try {
			if(clazz != null) {
				instance = clazz.getConstructor(CFWQueryContext.class).newInstance(context);
			}
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
	
	
	/***********************************************************************
	 * Get a list of all registered QueryFunctions.
	 * 
	 ***********************************************************************/
	public static ArrayList<String> getFunctionNames()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(queryFunctionMap.keySet());
		
		return Array;
	}
	
	/***********************************************************************
	 * Return true if a function with the specified name is registered, false
	 * otherwise.
	 * 
	 ***********************************************************************/
	public static boolean functionExists(String functionName)  {
		return queryFunctionMap.containsKey(functionName.trim().toLowerCase());
	}
		
}

package com.xresch.cfw.features.api;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryAPI {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryAPI.class.getName());
	
	private static LinkedHashMap<String, APIDefinition> definitionArray = new LinkedHashMap<String, APIDefinition>();
	
	public static String getFullyQualifiedName(APIDefinition definition) {
		return definition.getApiName()+"-"+definition.getActionName();
	}
	public static String getFullyQualifiedName(String name, String action) {
		return name+"-"+action;
	}
	/***********************************************************************
	 * Adds a APIDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void add(APIDefinition definition)  {
		String fullname = getFullyQualifiedName(definition);
		if(!definitionArray.containsKey(fullname)) {
			APITokenPermissionDBMethods.oneTimeCreate(
					new APITokenPermission()
					.apiName(definition.getApiName())
					.actionName(definition.getActionName())
			);
			definitionArray.put(fullname,definition);
		}else {
			new CFWLog(logger)
				.warn("An API definition with name'"+fullname+"' was already defined. Appending a number to the name.");
			
			int i = 0;
			do {
				i++;
				definition.setApiName(definition.getApiName()+i);
			}while ( definitionArray.containsKey(getFullyQualifiedName(definition)) );
			
			definitionArray.put(getFullyQualifiedName(definition), definition);
		}
	}
	
	/***********************************************************************
	 * Adds a APIDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void addAll(ArrayList<APIDefinition> definitions)  {
		if(definitions != null) {
			for(APIDefinition definition : definitions) {
				CFWRegistryAPI.add(definition);
			}
		}
	}
	
	/***********************************************************************
	 * Removes a APIDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void remove(APIDefinition definition)  {
		String fullname = getFullyQualifiedName(definition);
		definitionArray.remove(fullname);
	}
	
	/***********************************************************************
	 * Returns a APIDefinition class for the given name.
	 * @param definition
	 ***********************************************************************/
	public static APIDefinition getDefinition(String apiName, String actionName)  {
		return definitionArray.get(getFullyQualifiedName(apiName, actionName));
	}
	
	/***********************************************************************
	 * Removes a APIDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static LinkedHashMap<String, APIDefinition> getAPIDefinitions()  {
		return definitionArray;
	}
	
	/***********************************************************************
	 * Returns all API definitions as JSON array.
	 * @param definition
	 ***********************************************************************/
	public static JsonArray getJSONArray()  {
		
		JsonArray array = new JsonArray();
 
		for(APIDefinition definition : definitionArray.values()) {
			array.add(definition.getJSON());
		}
		
		return array;
	}
	 
}

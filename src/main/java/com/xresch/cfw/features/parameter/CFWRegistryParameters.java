package com.xresch.cfw.features.parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryParameters {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryParameters.class.getName());
	
	private static LinkedHashMap<String, ParameterDefinition> definitionArray = new LinkedHashMap<String, ParameterDefinition>();
	
	/***********************************************************************
	 * Adds a ParameterDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void add(ParameterDefinition definition)  {
		if(!definitionArray.containsKey(definition.getParamUniqueName())) {
			definitionArray.put(definition.getParamUniqueName(),definition);
		}else {
			new CFWLog(logger)
				.severe("A parameter definition with name'"+definition.getParamUniqueName()+"' was already defined. Could not add the definition to the registry.", new Throwable());
		}
	}
	
	/***********************************************************************
	 * Adds a ParameterDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void addAll(ArrayList<ParameterDefinition> definitions)  {
		if(definitions != null) {
			for(ParameterDefinition definition : definitions) {
				CFWRegistryParameters.add(definition);
			}
		}
	}
	
	/***********************************************************************
	 * Removes a ParameterDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void remove(ParameterDefinition definition)  {
		definitionArray.remove(definition.getParamUniqueName());
	}
	
	/***********************************************************************
	 * Returns a ParameterDefinition class for the given name.
	 * @param definition
	 ***********************************************************************/
	public static ParameterDefinition getDefinition(String parameterLabel)  {
		return definitionArray.get(parameterLabel);
	}
	
	/***********************************************************************
	 * Returns a ParameterDefinition by its name.
	 * @param definition
	 ***********************************************************************/
	public static LinkedHashMap<String, ParameterDefinition> getParameterDefinitions()  {
		return definitionArray;
	}
	
}

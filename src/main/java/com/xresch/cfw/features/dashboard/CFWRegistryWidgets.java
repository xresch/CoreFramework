package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.AbstractHTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryWidgets {
	
	public static Logger logger = CFWLog.getLogger(CFWRegistryWidgets.class.getName());
	
	private static LinkedHashMap<String, WidgetDefinition> definitionArray = new LinkedHashMap<String, WidgetDefinition>();
	
	/***********************************************************************
	 * Adds a WidgetDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void add(WidgetDefinition definition)  {
		if(!definitionArray.containsKey(definition.getWidgetType())) {
			definitionArray.put(definition.getWidgetType(),definition);
			
			HashMap<Locale, FileDefinition> localeFiles = definition.getLocalizationFiles();
			if(localeFiles != null) {
				for(Entry<Locale, FileDefinition> entry : localeFiles.entrySet()) {
					CFW.Localization.registerLocaleFile(entry.getKey(), "/app/dashboard/view", entry.getValue());
				}
			}
		}else {
			new CFWLog(logger)
				.method("add")
				.severe("A widget with definition with name'"+definition.getWidgetType()+"' was already defined. Could not add the definition to the registry.", new Throwable());
		}
	}
	
	/***********************************************************************
	 * Adds a WidgetDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void addAll(ArrayList<WidgetDefinition> definitions)  {
		if(definitions != null) {
			for(WidgetDefinition definition : definitions) {
				CFWRegistryWidgets.add(definition);
			}
		}
	}
	
	/***********************************************************************
	 * Removes a WidgetDefinition class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void remove(WidgetDefinition definition)  {
		definitionArray.remove(definition.getWidgetType());
	}
	
	/***********************************************************************
	 * Returns a WidgetDefinition class for the given name.
	 * @param definition
	 ***********************************************************************/
	public static WidgetDefinition getDefinition(String widgetType)  {
		return definitionArray.get(widgetType);
	}
	
	/***********************************************************************
	 * Returns a WidgetDefinition by its name.
	 * @param definition
	 ***********************************************************************/
	public static LinkedHashMap<String, WidgetDefinition> getWidgetDefinitions()  {
		return definitionArray;
	}
	
	/***********************************************************************
	 * Add widget CSS and JS files based on user permissions
	 * @param response
	 ***********************************************************************/
	public static void addFilesToResponse(AbstractHTMLResponse response)  {
		
		FileAssembly javascript = new FileAssembly("js_assembly_widgets", "js");
		FileAssembly css = new FileAssembly("css_assembly_widgets", "css");
		
		for(WidgetDefinition definition : definitionArray.values()) {
			if(definition.hasPermission()) {
				javascript.addAll(definition.getJavascriptFiles());
				css.addAll(definition.getCSSFiles());
			}
		}
		
		response.addCSSAssembly(css);
		response.addJSBottomAssembly(javascript);
	}

}

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
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.AbstractHTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryWidgets {
	
	private static FileAssembly javascriptAssembly = null;
	private static FileAssembly cssAssembly = null;
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryWidgets.class.getName());
	
	private static LinkedHashMap<String, WidgetDefinition> definitionArray = new LinkedHashMap<String, WidgetDefinition>();
	
	
	/***********************************************************************
	 * Reset the assembly files.
	 ***********************************************************************/
	public static void  resetCachedFiles() {
		javascriptAssembly = null;
		cssAssembly = null;
	}
	
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
			
			resetCachedFiles();
			

		}else {
			new CFWLog(logger)
				.severe("A widget definition with name'"+definition.getWidgetType()+"' was already defined. Could not add the definition to the registry.", new Throwable());
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
		resetCachedFiles();
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
		
		// create once and then reuse
		if(javascriptAssembly == null) {
			javascriptAssembly = new FileAssembly("js_assembly_widgets", "js");
			cssAssembly = new FileAssembly("css_assembly_widgets", "css");
			for(WidgetDefinition definition : definitionArray.values()) {
				javascriptAssembly.addAll(definition.getJavascriptFiles());
				cssAssembly.addAll(definition.getCSSFiles());
			}
		}

		response.addCSSAssembly(cssAssembly);
		response.addJSBottomAssembly(javascriptAssembly);
	}

}

package com.xresch.cfw.features.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryMail {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryMail.class.getName());
	
	private static LinkedHashMap<String, CFWMailProvider> providerArray = new LinkedHashMap<String, CFWMailProvider>();
	

	/***********************************************************************
	 * Adds a CFWMailProvider class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void addProvider(CFWMailProvider definition)  {
		String uniqueID = definition.uniqueID();
		if(!providerArray.containsKey(uniqueID)) {
			providerArray.put(uniqueID,definition);
		}else {
			new CFWLog(logger)
				.warn("An CFWMailProvider with id '"+uniqueID+"' was already defined, additional registration attempt ignored.");
		}
	}
	
	/***********************************************************************
	 * Adds a CFWMailProvider class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void addAll(ArrayList<CFWMailProvider> providers)  {
		if(providers != null) {
			for(CFWMailProvider provider : providers) {
				CFWRegistryMail.addProvider(provider);
			}
		}
	}
	
	/***********************************************************************
	 * Removes a CFWMailProvider class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static void remove(CFWMailProvider definition)  {
		String fullname = definition.uniqueID();
		providerArray.remove(fullname);
	}
	
	/***********************************************************************
	 * Returns a CFWMailProvider class for the given name.
	 * @param definition
	 ***********************************************************************/
	public static CFWMailProvider getSelectedProvider()  {
		
		String providerName = CFW.DB.Config.getConfigAsString(
				  FeatureMail.CONFIG_CATEGORY
				, FeatureMail.CONFIG_MAILING_PROVIDER
			);
		
		return providerArray.get(providerName);
	}
	
	/***********************************************************************
	 * Returns a CFWMailProvider class for the given name.
	 * @param definition
	 ***********************************************************************/
	public static CFWMailProvider getProviderByName(String uniqueName)  {
		return providerArray.get(uniqueName);
	}
	
	/***********************************************************************
	 * Removes a CFWMailProvider class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static Map<String, CFWMailProvider> getCFWMailProviders()  {
		return new LinkedHashMap<>(providerArray);
	}
	
	/***********************************************************************
	 * Removes a CFWMailProvider class to the registry.
	 * @param definition
	 ***********************************************************************/
	public static ArrayList<String> getSelectOptionsList()  {
		ArrayList<String> options = new ArrayList<>();
		
		for(CFWMailProvider provider : providerArray.values()) {
			
			options.add(provider.uniqueID());
		}
		
		return options;
	}
	 
}

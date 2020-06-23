package com.pengtoolbox.cfw.features.dashboard;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryDashboard {
	private static Logger logger = CFWLog.getLogger(CFWRegistryDashboard.class.getName());
	
	private static LinkedHashMap<String, ManualPage> manualPages = new LinkedHashMap<String, ManualPage>();
	
	/***********************************************************************
	 * Adds a menuItem to the regular menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuPath were the menu should be added, or null for root
	 * @param menuitem to add
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static ManualPage addManualPage(String menuPath, ManualPage page)  {
		addManualPage(manualPages, page, menuPath);
		return page;
	}
	
	

	/***********************************************************************
	 * Adds a menuItem to one of the menus.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param pagePath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	private static void addManualPage(LinkedHashMap<String, ManualPage> targetPageList, ManualPage itemToAdd, String pagePath)  {
		//System.out.println("======= Path :"+menuPath+" ======== ");
		//-----------------------
		// Check Argument
		if(pagePath == null || pagePath.trim().length() == 0) {
			targetPageList.put(itemToAdd.getLabel(), itemToAdd);
			//System.out.println("Add "+item.getLabel());
			return;
		}
		
		//-----------------------
		// Handle Path
		String[] pathTokens = pagePath.split("\\Q|\\E");
		ManualPage parentItem = null;
		LinkedHashMap<String, ManualPage> currentSubPage = targetPageList;
		for(int i = 0; i < pathTokens.length; i++) {
			String currentToken = pathTokens[i].trim();
			
			//---------------------------
			// Handle Parent
			if(parentItem == null) {
				parentItem = targetPageList.get(currentToken);
				if(parentItem == null) {
					parentItem = new ManualPage(currentToken);
					targetPageList.put(currentToken, parentItem);
				}
			}else if(parentItem.getSubManualPages().containsKey(currentToken)) {
				parentItem = parentItem.getSubManualPages().get(currentToken);
			}
			if(i == pathTokens.length-1) {
				parentItem.addChild(itemToAdd);
				//System.out.println("add "+itemToAdd.getLabel()+" to subitems of: "+currentToken);
			}
		}
	}
	/***********************************************************************
	 * Returns the manual pages the user has permissions for.
	 ***********************************************************************/
	public static ManualPage getPageByPath(String pagePath)  {
		//-----------------------
		// Handle Path
		String[] pathTokens = pagePath.split("\\Q|\\E");
		ManualPage currentItem = null;
		
		if(pathTokens.length > 0) {
			currentItem = manualPages.get(pathTokens[0]);
			
			for(int i = 1; i < pathTokens.length; i++) {
				String currentToken = pathTokens[i].trim();
				currentItem = currentItem.getChildPagebyTitle(currentToken);
				//---------------------------
				// Handle Parent
				if(currentItem == null) {
					return null;
				}
			}
		}
		
		return currentItem;
	}
	/***********************************************************************
	 * Returns the manual pages the user has permissions for.
	 ***********************************************************************/
	public static JsonArray getManualPagesForUserAsJSON()  {
		
		JsonArray pages = new JsonArray();

		for(ManualPage page : manualPages.values()) {
			JsonObject object = page.toJSONObjectForMenu();
			if(object != null) {
				pages.add(object);
			}
		}

		return pages;
	}
	


	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String dumpManualPageHierarchy() {
		return new StringBuilder()
				.append(dumpManualHierarchy("", manualPages))
				.toString();
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static String dumpManualHierarchy(String currentPrefix, LinkedHashMap<String, ManualPage> manualPages) {
		
		//-----------------------------------
		//Create Prefix
		StringBuilder builder = new StringBuilder();
		
		ManualPage[] items = manualPages.values().toArray(new ManualPage[]{});
		int objectCount = items.length;
		for(int i = 0; i < objectCount; i++) {
			ManualPage current = items[i];
			builder.append(currentPrefix)
				   .append("|--> ")
				   .append(current.getLabel()).append("\n");
			if(objectCount > 1 && (i != objectCount-1)) {
				builder.append(dumpManualHierarchy(currentPrefix+"|  ", current.getSubManualPages()));
			}else{
				builder.append(dumpManualHierarchy(currentPrefix+"  ", current.getSubManualPages()));
			}
		}
		
		return builder.toString();
	}
	

}

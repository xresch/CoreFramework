package com.xresch.cfw.features.manual;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryManual {
	private static Logger logger = CFWLog.getLogger(CFWRegistryManual.class.getName());
	
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

		//-----------------------
		// Check Argument
		if(pagePath == null || pagePath.trim().length() == 0) {
			targetPageList.put(itemToAdd.getLabel(), itemToAdd);
			return;
		}
		
		//-----------------------
		// Handle Path
		String[] pathTokens = pagePath.split("\\Q|\\E");
		ManualPage parentItem = null;

		for(int i = 0; i < pathTokens.length; i++) {
			String currentToken = pathTokens[i].trim();
			
			//---------------------------
			// Handle Parent
			if(parentItem == null) {
				// Grab first page
				parentItem = targetPageList.get(currentToken);
				if(parentItem == null) {
					parentItem = new ManualPage(currentToken);
					targetPageList.put(currentToken, parentItem);
				}
			}else if(parentItem.getSubManualPages().containsKey(currentToken)) {
				// Grab child page
				parentItem = parentItem.getSubManualPages().get(currentToken);
			}else {
				//Create non-existing page
				ManualPage createdPage = new ManualPage(currentToken).content("&nbsp;");
				parentItem.addChild( createdPage );
				parentItem = createdPage;
			}
			
			//---------------------------
			// Add page if last token
			if(i == pathTokens.length-1) {
				parentItem.addChild(itemToAdd);
			}
		}
	}
	/***********************************************************************
	 * Returns the manual pages the user has permissions for.
	 * Returns null if page is not found.
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
	public static JsonArray getManualPagesForUserAsJSON(CFWSessionData sessionData)  {
		
		JsonArray pages = new JsonArray();

		for(ManualPage page : manualPages.values()) {
			JsonObject object = page.toJSONObjectForMenu(sessionData);
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

package com.pengtoolbox.cfw._main;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.manual.FeatureManual;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.BTFooter;
import com.pengtoolbox.cfw.response.bootstrap.BTMenu;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.cfw.response.bootstrap.MenuItemDivider;
import com.pengtoolbox.cfw.response.bootstrap.UserMenuItem;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWRegistryComponents {
	private static Logger logger = CFWLog.getLogger(CFWRegistryComponents.class.getName());
	
	private static LinkedHashMap<String, MenuItem> regularMenuItems = new LinkedHashMap<String, MenuItem>();
	private static LinkedHashMap<String, MenuItem> userMenuItems = new LinkedHashMap<String, MenuItem>();
	
	private static LinkedHashMap<String, MenuItem> adminMenuItems = new LinkedHashMap<String, MenuItem>();
	
	// Admin items of the Core Framework
	private static LinkedHashMap<String, MenuItem> adminMenuItemsCFW = new LinkedHashMap<String, MenuItem>();
	
	private static Class<?> defaultFooterClass = null;

	
	/***********************************************************************
	 * Adds a menuItem to the regular menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addRegularMenuItem(MenuItem item, String menuPath)  {
		addMenuItem(regularMenuItems, item, menuPath);
	}
	
	/***********************************************************************
	 * Adds a menuItem to the admin menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addAdminMenuItem(MenuItem itemToAdd, String menuPath)  {
		if(itemToAdd.getPermissions().size() == 0){
			new CFWLog(logger)
			.method("addAdminMenuItem")
			.severe("Coding Issue: Admin menu items need at least 1 permission.");
		}
		addMenuItem(adminMenuItems, itemToAdd, menuPath);
	}
	
	/***********************************************************************
	 * Adds a menuItem to the admin menu in the section of the CFW.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addAdminCFWMenuItem(MenuItem itemToAdd, String menuPath)  {
		if(itemToAdd.getPermissions().size() == 0){
			new CFWLog(logger)
			.method("addAdminCFWMenuItem")
			.severe("Coding Issue: Admin menu items need at least 1 permission.");
		}
		addMenuItem(adminMenuItemsCFW, itemToAdd, menuPath);
	}
	
	/***********************************************************************
	 * Adds a menuItem to the user menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addUserMenuItem(MenuItem itemToAdd, String menuPath)  {
		addMenuItem(userMenuItems, itemToAdd, menuPath);
	}
	
	/***********************************************************************
	 * Adds a menuItem to one of the menus.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	private static void addMenuItem(LinkedHashMap<String, MenuItem> targetItemList, MenuItem itemToAdd, String menuPath)  {
		//System.out.println("======= Path :"+menuPath+" ======== ");
		//-----------------------
		// Check Argument
		if(menuPath == null || menuPath.trim().length() == 0) {
			targetItemList.put(itemToAdd.getLabel(), itemToAdd);
			//System.out.println("Add "+item.getLabel());
			return;
		}
		
		//-----------------------
		// Handle Path
		String[] pathTokens = menuPath.split("\\Q|\\E");
		MenuItem parentItem = null;
		for(int i = 0; i < pathTokens.length; i++) {
			String currentToken = pathTokens[i].trim();
			
			//---------------------------
			// Handle Parent
			if(parentItem == null) {
				parentItem = targetItemList.get(currentToken);
				if(parentItem == null) {
					parentItem = new MenuItem(currentToken);
					targetItemList.put(currentToken, parentItem);
				}
			}else if(parentItem.getSubMenuItems().containsKey(currentToken)) {
				parentItem = parentItem.getSubMenuItems().get(currentToken);
			}
			if(i == pathTokens.length-1) {
				parentItem.addChild(itemToAdd);
				//System.out.println("add "+itemToAdd.getLabel()+" to subitems of: "+currentToken);
			}
		}
	}
	
	/***********************************************************************
	 * Create a instance of the menu.
	 * @return a Bootstrap Menu instance
	 ***********************************************************************/
	public static BTMenu createMenuInstance(boolean withUserMenus)  {
		
		BTMenu menu = new BTMenu();
		menu.setLabel(CFW.DB.Config.getConfigAsString(Configuration.MENU_TITLE));
		
		//======================================
		// Regular Menus
		//======================================
		for(MenuItem item : regularMenuItems.values() ) {
			menu.addChild(item.createCopy());
		}
		
		//======================================
		// User Menus
		//======================================
		if(withUserMenus) {
			
			//---------------------------
			// Admin Menu
			MenuItem adminParentMenu = new MenuItem("Admin").faicon("fas fa-tools");	
			menu.addChild(adminParentMenu);
			
			for(MenuItem item : adminMenuItems.values() ) {
				adminParentMenu.addChild(item.createCopy());
			}

			if(adminMenuItems.size() > 0) {
				adminParentMenu.addChild(new MenuItemDivider());
			}
			
			//---------------------------
			// Admin Menu Items CFW
			for(MenuItem item : adminMenuItemsCFW.values() ) {
				adminParentMenu.addChild(item.createCopy());
			}
			
			//---------------------------
			// Manual
			if(CFW.Registry.Manual.getManualPagesForUserAsJSON().size() > 0
			&& CFW.Context.Request.hasPermission(FeatureManual.PERMISSION_MANUAL)) {
				menu.addRightMenuItem(
						(MenuItem)new MenuItem("Manual", "{!cfw_core_manual!}") 
						.faicon("fas fa-book")
						.addPermission(FeatureManual.PERMISSION_MANUAL)
						.href("/app/manual")
				);
			};
			
			//---------------------------
			// User Menu
			UserMenuItem userParentMenu = new UserMenuItem(CFW.Context.Session.getSessionData());	
			menu.setUserMenuItem(userParentMenu);
			
			for(MenuItem item : userMenuItems.values() ) {
				userParentMenu.addChild(item.createCopy());
			}
			
			if(userMenuItems.size() > 0) {
				userParentMenu.addChild(new MenuItemDivider());
			}
		
			if(!CFW.Context.Request.getUser().isForeign()) {
				userParentMenu.addChild(new MenuItem("Change Password").faicon("fas fa-key").href("/app/changepassword"));
			}
			
			userParentMenu.addChild(new MenuItem("Logout").faicon("fas fa-sign-out-alt").href("/app/logout"));
			
		}
		return menu;
	}
	

	/***********************************************************************
	 * Set the class to be used as the default footer for your application.
	 * @param Class that extends from BTFooter
	 ***********************************************************************/
	public static void setDefaultFooter(Class<?> menuClass)  {
		
		if(BTFooter.class.isAssignableFrom(menuClass)) {
			defaultFooterClass = menuClass;
		}else {
			new CFWLog(logger).severe("Class is not a subclass of 'BTFooter': "+menuClass.getName());
		}
	}
	
	
	/***********************************************************************
	 * Create a instance of the footer.
	 * @return a Bootstrap Menu instance
	 ***********************************************************************/
	public static BTFooter createDefaultFooterInstance()  {
		
		if(defaultFooterClass != null) {
			try {
				Object menu = defaultFooterClass.newInstance();
				
				if(menu instanceof BTFooter) {
					return (BTFooter)menu;
				}else {
					throw new InstantiationException("Class not an instance of BTFooter");
				}
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+defaultFooterClass.getSimpleName()+"': "+e.getMessage(), e);
			}
		}
		
		return new BTFooter().setLabel("Set your custom menu class(extending BTFooter) using CFW.App.setDefaultFooter()! ");
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String dumpMenuItemHierarchy() {
		return new StringBuilder()
				.append(dumpMenuItemHierarchy("", regularMenuItems))
				.append(dumpMenuItemHierarchy("", adminMenuItems))
				.append(dumpMenuItemHierarchy("", adminMenuItemsCFW))
				.append(dumpMenuItemHierarchy("", userMenuItems))
				.toString();
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static String dumpMenuItemHierarchy(String currentPrefix, LinkedHashMap<String, MenuItem> menuItems) {
		
		//-----------------------------------
		//Create Prefix
		StringBuilder builder = new StringBuilder();
		
		MenuItem[] items = menuItems.values().toArray(new MenuItem[]{});
		int objectCount = items.length;
		for(int i = 0; i < objectCount; i++) {
			MenuItem current = items[i];
			builder.append(currentPrefix)
				   .append("|--> ")
				   .append(current.getLabel()).append("\n");
			if(objectCount > 1 && (i != objectCount-1)) {
				builder.append(dumpMenuItemHierarchy(currentPrefix+"|  ", current.getSubMenuItems()));
			}else{
				builder.append(dumpMenuItemHierarchy(currentPrefix+"  ", current.getSubMenuItems()));
			}
		}
		
		return builder.toString();
	}
	

}

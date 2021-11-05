package com.xresch.cfw._main;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTMenu;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.response.bootstrap.MenuItemDivider;
import com.xresch.cfw.response.bootstrap.UserMenuItem;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryComponents {
	private static Logger logger = CFWLog.getLogger(CFWRegistryComponents.class.getName());
	
	//-------------------------------
	// Menus
	
	//Top level menu items placed in the left of the navigation bar
	private static LinkedHashMap<String, MenuItem> regularMenuItems = new LinkedHashMap<>();
	
	//Items in the dropdown of the tools menu
	private static LinkedHashMap<String, MenuItem> toolsMenuItems = new LinkedHashMap<>();
	
	//Items in the dropdown of the admin menu
	private static LinkedHashMap<String, MenuItem> adminMenuItems = new LinkedHashMap<>();
	
	// Admin items of the Core Framework
	private static LinkedHashMap<String, MenuItem> adminMenuItemsCFW = new LinkedHashMap<>();
	
	//Top Level Menu Items in the button area next to the user menu
	private static LinkedHashMap<String, MenuItem> buttonMenuItems = new LinkedHashMap<>();
	
	//Items in the dropdown of the user menu
	private static LinkedHashMap<String, MenuItem> userMenuItems = new LinkedHashMap<>();
	
	
	//-------------------------------
	// Footer
	private static Class<?> defaultFooterClass = null;
	
	//-------------------------------
	// Global web resources
	private static FileAssembly assemblyGlobalJavascript = new FileAssembly("js_assembly_global", "js");
	private static FileAssembly assemblyGlobalCSS = new FileAssembly("css_assembly_global", "css");
	
	

	
	/***********************************************************************
	 * 
	 ***********************************************************************/ 
	public static void addGlobalCSSFile(FileDefinition.HandlingType type, String path, String filename){
		assemblyGlobalCSS.addFile(type, path, filename);
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/ 
	public static FileAssembly getGlobalCSS(){
		return assemblyGlobalCSS;
	}
	
	
	/***********************************************************************
	 * 
	 ***********************************************************************/ 
	public static  void addGlobalJavascript(FileDefinition.HandlingType type, String path, String filename){
		assemblyGlobalJavascript.addFile(type, path, filename);
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/ 
	public static FileAssembly getGlobalJavascripts(){
		return assemblyGlobalJavascript;
	}
	
	
	
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
			.severe("Coding Issue: Admin menu items need at least 1 permission.");
		}
		addMenuItem(adminMenuItems, itemToAdd, menuPath);
	}
	
	/***********************************************************************
	 * Adds a menuItem to the tools menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addToolsMenuItem(MenuItem itemToAdd, String menuPath)  {
		if(itemToAdd.getPermissions().size() == 0){
			new CFWLog(logger)
			.severe("Coding Issue: Admin menu items need at least 1 permission.");
		}
		addMenuItem(toolsMenuItems, itemToAdd, menuPath);
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
			.severe("Coding Issue: Admin menu items need at least 1 permission.");
		}
		addMenuItem(adminMenuItemsCFW, itemToAdd, menuPath);
	}
	
	
	
	
	/***********************************************************************
	 * Adds a menuItem to the buttons menu.
	 * Define the position of in the menu with the menuPath parameter. Use
	 * "|" to separate multiple menu labels.
	 * @param menuitem to add
	 * @param menuPath were the menu should be added, or null for root
	 * @param Class that extends from BTMenu
	 ***********************************************************************/
	public static void addButtonsMenuItem(MenuItem itemToAdd, String menuPath)  {
		itemToAdd.addCssClass("cfw-button-menuitem");
		addMenuItem(buttonMenuItems, itemToAdd, menuPath);
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
		
		//-----------------------
		// Check Argument
		if(menuPath == null || menuPath.trim().length() == 0) {
			targetItemList.put(itemToAdd.getMenuName(), itemToAdd);
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
			}
		}
	}
	
	/***********************************************************************
	 * Create a instance of the menu.
	 * @return a Bootstrap Menu instance
	 ***********************************************************************/
	public static BTMenu createMenuInstance(CFWSessionData sessionData, boolean withUserMenus)  {
		
		BTMenu menu = new BTMenu();
		menu.setLabel(CFW.DB.Config.getConfigAsString(FeatureConfiguration.CONFIG_MENU_TITLE));
		
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
			// Tools
			MenuItem toolsParentMenu = new MenuItem("Tools")
					.faicon("fas fa-tools")
					.addPermission("PSEUDO_PERMISSION_HIDE_BY_DEFAULT");	
			
			menu.addChild(toolsParentMenu);
			
			for(MenuItem item : toolsMenuItems.values() ) {
				toolsParentMenu.addChild(item.createCopy());
			}
			
			//---------------------------
			// Admin Menu
			MenuItem adminParentMenu = new MenuItem("Admin").faicon("fas fa-cogs");	
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
			// Button Menu			
			for(MenuItem item : buttonMenuItems.values() ) {
				menu.addRightMenuItem(item.createCopy());
			}
						
			//---------------------------
			// User Menu
			UserMenuItem userParentMenu = new UserMenuItem(sessionData);	
			menu.setUserMenuItem(userParentMenu);
			
			for(MenuItem item : userMenuItems.values() ) {
				userParentMenu.addChild(item.createCopy());
			}
			
			if(userMenuItems.size() > 0) {
				userParentMenu.addChild(new MenuItemDivider());
			}
		
			if(!sessionData.getUser().isForeign()) {
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
				   .append(current.getMenuName()).append("\n");
			if(objectCount > 1 && (i != objectCount-1)) {
				builder.append(dumpMenuItemHierarchy(currentPrefix+"|  ", current.getSubMenuItems()));
			}else{
				builder.append(dumpMenuItemHierarchy(currentPrefix+"  ", current.getSubMenuItems()));
			}
		}
		
		return builder.toString();
	}
	
	
	

}

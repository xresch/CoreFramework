package com.xresch.cfw.features.manual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ManualPage {
	
	private static Logger logger = CFWLog.getLogger(CFWRegistryManual.class.getName());
	
	private String title = "&nbsp;";
	private String faiconClasses = "";
	
	private String path = "";
	
	private FileDefinition content = null;
	
	// if any permissions match page will be accessible by the user
	// if no permission in the list page will be accessible by the user
	private HashSet<String> permissions = new HashSet<String>();
	private LinkedHashMap<String, ManualPage> childPages = new LinkedHashMap<String, ManualPage>();
	
	protected ManualPage parent = null;
	
	public ManualPage(String title) {
		
		if(title.contains("|")) {
			new CFWLog(logger)
			.severe("Title cannot contain '|'.", new Exception());
		}
		
		this.title = title;
		this.path = title;
	}
	
	public ManualPage(String label, HashSet<String> permissions) {
		this.title = label;
		this.permissions = permissions;
	}
			
	/***********************************************************************************
	 * Overrloaded addChild to handle sub menu items.
	 * @return this page. 
	 ***********************************************************************************/
	public ManualPage addChild(ManualPage childItem) {
		
		childPages.put(childItem.getLabel().trim(), childItem);
		this.addPermissions(childItem.getPermissions());
		
		childItem.setParent(this);

		return this;
	}
	
	/***********************************************************************************
	 * Overrloaded addChild to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public ManualPage getChildPagebyTitle(String title) {
		
		if(childPages.containsKey(title.trim())) {
			return childPages.get(title);
		}

		return null;
	}
	
	/***********************************************************************************
	 * Overrride to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public LinkedHashMap<String, ManualPage> getSubManualPages() {
		return childPages;
	}
	
	/***********************************************************************************
	 * Returns the Json data needed to build the navigation if the user has the required 
	 * permissions for the page
	 * @return String html for this item. 
	 ***********************************************************************************/
	public JsonObject toJSONObjectWithContent() {
		//----------------------------------
		// Check Permissions
		if(permissions.size() > 0) {

			boolean hasPermission = false;
			HashMap<String, Permission> usersPermissions = CFW.Context.Request.getUserPermissions();
			for(String permission : permissions) {
				if(usersPermissions.containsKey(permission)) {
					hasPermission = true;
					break;
				}
			}
			
			if(!hasPermission) {
				return null;
			}
		}

		//----------------------------------
		// Build JSON
		JsonObject result = new JsonObject();
		
		result.addProperty("title", title);
		result.addProperty("path", path);
		result.addProperty("faiconClasses", faiconClasses);
		result.addProperty("hasContent", content != null);
		result.addProperty("content", content.readContents());
		
		return result;
	}
	/***********************************************************************************
	 * Returns the Json data needed to build the navigation if the user has the required 
	 * permissions for the page
	 * @return String html for this item. 
	 ***********************************************************************************/
	public JsonObject toJSONObjectForMenu(CFWSessionData sessionData) {
		
		//----------------------------------
		// Check Permissions
		if(permissions.size() > 0) {

			boolean hasPermission = false;
			HashMap<String, Permission> usersPermissions = sessionData.getUserPermissions();

			for(String permission : permissions) {
				if(usersPermissions.containsKey(permission)) {
					hasPermission = true;
					break;
				}
			}
			
			if(!hasPermission) {
				return null;
			}
		}

		//----------------------------------
		// Build JSON
		JsonObject result = new JsonObject();
		
		result.addProperty("title", title);
		result.addProperty("path", path);
		result.addProperty("faiconClasses", faiconClasses);
		result.addProperty("hasContent", content != null);
		
		if(childPages.size() > 0) {
			JsonArray children = new JsonArray();
			for(ManualPage page : childPages.values()) {
				JsonObject object = page.toJSONObjectForMenu(sessionData);
				if(object != null) {
					children.add(object);
				}
			}
			
			result.add("children", children);
		}
		
		return result;

	}
	
	public ManualPage getParent() {
		return parent;
	}

	public void setParent(ManualPage parent) {
		this.parent = parent;
		this.path = this.resolvePath(null);
		for(ManualPage child : childPages.values()) {
			child.resolvePath(null);
		}
	}
	
	/***********************************************************************************
	 * Add the permission needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public ManualPage addPermission(String permission) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.add(permission);
		
		if(this.parent != null && parent instanceof ManualPage) {
			((ManualPage)parent).addPermission(permission);
		}
		
		return this;
	}
	
	
	/***********************************************************************************
	 * Add the permissions needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public ManualPage addPermissions(HashSet<String> permissionArray) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.addAll(permissionArray);
		
		if(this.parent != null && parent instanceof ManualPage) {
			((ManualPage)parent).addPermissions(permissionArray);
		}
		
		return this;
	}
	
	/*****************************************************************************
	 *  resolves the path of a page.
	 *  Use null to start resolving the path.
	 *****************************************************************************/
	public String resolvePath(String pagePath) {
		if(pagePath == null) {
			pagePath = title;
		}else {
			pagePath = title+"|"+pagePath;
		}
		
		if(this.parent != null) {
			return parent.resolvePath(pagePath);
		}
		return pagePath;
	}
	
	/***********************************************************************************
	 * 
	 * @return permissions
	 ***********************************************************************************/
	public HashSet<String> getPermissions( ) {
		return permissions;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String getLabel() {
		return title;
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public FileDefinition content() {
		return this.content;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
    public String getContentPlaintext() {
    	String html = "";
    	if(content != null) {
    		html = content.readContents();
    	}
    	
    	if(html == null) { html = ""; }
    	
        return Jsoup.parse(html).text();
        
    }
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public ManualPage content(String html) {
		this.content = new FileDefinition(html);
		updateSearchIndex();
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public ManualPage content(HandlingType type, String path, String filename) {
		this.content = new FileDefinition(type, path, filename);
		updateSearchIndex();
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public ManualPage content(FileDefinition fileDef) {
		this.content = fileDef;
		updateSearchIndex();
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	private ManualPage updateSearchIndex() {
		ManualSearchEngine.addPage(this);
		return this;
	}
	
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public ManualPage faicon(String faiconClasses) {
		this.faiconClasses = faiconClasses;
		return this;
	}
	
	
	
	

		
	

}

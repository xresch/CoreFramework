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
	
			
	/***********************************************************************************
	 * Overrloaded addChild to handle sub menu items.
	 * @return this page. 
	 ***********************************************************************************/
	public ManualPage addChild(ManualPage childItem) {
		
		childPages.put(childItem.getLabel().trim(), childItem);

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
	 * Returns the content as a JsonObject for this page.
	 * @return JsonObject
	 ***********************************************************************************/
	public JsonObject toJSONObjectWithContent() {
		
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

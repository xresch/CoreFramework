package com.xresch.cfw.response.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWHTMLItem {
	
	protected ArrayList<CFWHTMLItem> children = new ArrayList<CFWHTMLItem> ();
	protected ArrayList<CFWHTMLItem> oneTimeChildren = new ArrayList<CFWHTMLItem> ();
	
	protected CFWHTMLItemDynamic creator = null; 
	protected boolean hasChanged = true;
	
	protected StringBuilder html = null;
	
	protected LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	
	protected CFWHTMLItem parent = null;
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * Use the method getHTML to retrieve the html.
	 * When implementing this method, make sure that any changes to fields used to create
	 * the html trigger {@link #fireChange()}.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected abstract void createHTML(StringBuilder builder);
	
	/***********************************************************************************
	 * Get the HTML representation of this item.
	 * If the HTML was already created and no changes occure, this method will read it
	 * from memory.
	 * 
	 * @return String html for this item. 
	 ***********************************************************************************/
	public String getHTML() {
		
		//-----------------------------------------
		// Check for dynamic items
		if(creator != null) {
			this.setOneTimeChildren(creator.createDynamicItems());
		}
		
		//-----------------------------------------
		// Create HTML
		if(hasChanged || html == null) {
			hasChanged = false;
			html = new StringBuilder();
			createHTML(html);
		}
		
		//-----------------------------------------
		// Reset oneTimeChildren
		if(oneTimeChildren.size() > 0) {
			oneTimeChildren = new ArrayList<CFWHTMLItem> ();
			fireChange();
		}

		return html.toString();
	}
	
	protected void fireChange() {
		hasChanged = true;
		if(parent != null) parent.fireChange();
	}
	
	public CFWHTMLItem getParent() {
		return parent;
	}

	public void setParent(CFWHTMLItem parent) {
		this.parent = parent;
	}
	
	public CFWHTMLItem setChildren(ArrayList<CFWHTMLItem> children) {
		fireChange();
		this.children = children;
		return this;
	}
	
	public ArrayList<CFWHTMLItem> getChildren() {
		return children;
	}

	public CFWHTMLItem addChild(CFWHTMLItem childItem) {
		fireChange();
		childItem.setParent(this);
		this.children.add(childItem);
		return this;
	}
	
	public boolean removeChild(CFWHTMLItemMenuItem childItem) {
		fireChange();
		return this.children.remove(childItem);
	}
	
	public boolean containsChild(CFWHTMLItemMenuItem childItem) {
		return children.contains(childItem);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	
	
	public CFWHTMLItem setOneTimeChildren(ArrayList<CFWHTMLItem> children) {
		fireChange();
		this.oneTimeChildren = children;
		return this;
	}
	
	public ArrayList<CFWHTMLItem> getOneTimeChildren() {
		return oneTimeChildren;
	}

	public CFWHTMLItem addOneTimeChild(CFWHTMLItem childItem) {
		fireChange();
		childItem.setParent(this);
		this.oneTimeChildren.add(childItem);
		return this;
	}
	
	public boolean removeOneTimeChild(CFWHTMLItemMenuItem childItem) {
		fireChange();
		return this.oneTimeChildren.remove(childItem);
	}
	
	public boolean containsOneTimeChild(CFWHTMLItemMenuItem childItem) {
		return oneTimeChildren.contains(childItem);
	}
	
	public boolean hasOneTimeChildren() {
		return !oneTimeChildren.isEmpty();
	}

	/***********************************************************************************
	 * Add an attribute to the html tag.
	 * Adding a value for the same attribute multiple times will overwrite preceding values.
	 * @param name the name of the attribute.
	 * @param key the key of the attribute.
	 * @return instance for chaining
	 ***********************************************************************************/
	public CFWHTMLItem addAttribute(String name, String value) {
		fireChange();
		this.attributes.put(name, value);
		return this;
	}
	
	/***********************************************************************************
	 * Remove an attribute from the html tag.
	 * Adding a value for the same attribute multiple times will overwrite preceeding values.
	 * 
	 * @param name the name of the attribute.
	 * @return instance for chaining
	 ***********************************************************************************/
	public CFWHTMLItem removeAttribute(String name) {
		fireChange();
		this.attributes.remove(name);
		return this;
	}

	
	/***********************************************************************************
	 * Return the attributes.
	 * @return HashMap of attributes
	 ***********************************************************************************/
	public HashMap<String, String> getAttributes() {
		return attributes;
	}
	
	/***********************************************************************************
	 * Return the attributes.
	 * @return HashMap of attributes
	 ***********************************************************************************/
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	/***********************************************************************************
	 * Return the attributes as a String with concatenated key="value" pairs that can
	 * be inserted into a html tag.
	 * @return HashMap of attributes
	 ***********************************************************************************/
	public String getAttributesString() {
		StringBuilder builder = new StringBuilder(" ");
		for(Entry<String, String> entry : attributes.entrySet()) {
			if(entry.getValue() != null) {
				builder.append(entry.getKey())
				.append("=\"")
				.append(entry.getValue())
				.append("\" ");
			}
		}
		return builder.toString();
	}
	
	/***********************************************************************************
	 * Get the value of the attribute.
	 * @return String with the value, empty string if not defined.
	 ***********************************************************************************/
	public String getAttributeValue(String value) {
		
		String returnValue = "";
		if(attributes.containsKey(value)) {
			returnValue = attributes.get(value);
		}
		
		return returnValue;
	}
	
	/***********************************************************************************
	 * Get the value of the attribute and removes it from the attribute list.
	 * @return String with the value, empty string if not defined.
	 ***********************************************************************************/
	public String popAttributeValue(String value) {
		
		String returnValue = "";
		if(attributes.containsKey(value)) {
			returnValue = attributes.remove(value);
		}
		
		return returnValue;
	}
	
	/***********************************************************************************
	 * Set the CSS Class attribute.
	 * @param cssClass the css classes you want to set on this item.
	 ***********************************************************************************/
	public CFWHTMLItem addCssClass(String cssClass) {
		if(getAttributes().containsKey("class")) {
			String currentClasses = getAttributeValue("class");
			return addAttribute("class", currentClasses+" "+cssClass);
		}else {
			return addAttribute("class", cssClass);
		}
	}
	
	/***********************************************************************************
	 * Set the onclick attribute.
	 * @param onclick javascript code
	 ***********************************************************************************/
	public CFWHTMLItem onclick(String onclick) {
		return addAttribute("onclick", onclick);
	}

	/***********************************************************************************
	 * Set a dynamic creator that always creates the child items on the fly and will
	 * not be cached.
	 ***********************************************************************************/
	public CFWHTMLItem setDynamicCreator(CFWHTMLItemDynamic creator) {
		fireChange();
		this.creator = creator;
		creator.setParent(this);
		return this;
	}
		
}

package com.pengtoolbox.cfw.response.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class HierarchicalHTMLItem {
	
	protected ArrayList<HierarchicalHTMLItem> children = new ArrayList<HierarchicalHTMLItem> ();
	protected ArrayList<HierarchicalHTMLItem> oneTimeChildren = new ArrayList<HierarchicalHTMLItem> ();
	
	protected DynamicItemCreator creator = null; 
	protected boolean hasChanged = true;
	
	protected StringBuilder html = null;
	
	protected LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	
	protected HierarchicalHTMLItem parent = null;
	
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
			oneTimeChildren = new ArrayList<HierarchicalHTMLItem> ();
			fireChange();
		}

		return html.toString();
	}
	
	protected void fireChange() {
		hasChanged = true;
		if(parent != null) parent.fireChange();
	}
	
	public HierarchicalHTMLItem getParent() {
		return parent;
	}

	public void setParent(HierarchicalHTMLItem parent) {
		this.parent = parent;
	}
	
	public HierarchicalHTMLItem setChildren(ArrayList<HierarchicalHTMLItem> children) {
		fireChange();
		this.children = children;
		return this;
	}
	
	public ArrayList<HierarchicalHTMLItem> getChildren() {
		return children;
	}

	public HierarchicalHTMLItem addChild(HierarchicalHTMLItem childItem) {
		fireChange();
		childItem.setParent(this);
		this.children.add(childItem);
		return this;
	}
	
	public boolean removeChild(MenuItem childItem) {
		fireChange();
		return this.children.remove(childItem);
	}
	
	public boolean containsChild(MenuItem childItem) {
		return children.contains(childItem);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	
	
	public HierarchicalHTMLItem setOneTimeChildren(ArrayList<HierarchicalHTMLItem> children) {
		fireChange();
		this.oneTimeChildren = children;
		return this;
	}
	
	public ArrayList<HierarchicalHTMLItem> getOneTimeChildren() {
		return oneTimeChildren;
	}

	public HierarchicalHTMLItem addOneTimeChild(HierarchicalHTMLItem childItem) {
		fireChange();
		childItem.setParent(this);
		this.oneTimeChildren.add(childItem);
		return this;
	}
	
	public boolean removeOneTimeChild(MenuItem childItem) {
		fireChange();
		return this.oneTimeChildren.remove(childItem);
	}
	
	public boolean containsOneTimeChild(MenuItem childItem) {
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
	public HierarchicalHTMLItem addAttribute(String name, String value) {
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
	public HierarchicalHTMLItem removeAttribute(String name) {
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
	public HierarchicalHTMLItem addCssClass(String cssClass) {
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
	public HierarchicalHTMLItem onclick(String onclick) {
		return addAttribute("onclick", onclick);
	}

	/***********************************************************************************
	 * Set a dynamic creator that always creates the child items on the fly and will
	 * not be cached.
	 ***********************************************************************************/
	public HierarchicalHTMLItem setDynamicCreator(DynamicItemCreator creator) {
		fireChange();
		this.creator = creator;
		return this;
	}
		
}

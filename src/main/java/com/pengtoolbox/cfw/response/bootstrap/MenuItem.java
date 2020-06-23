package com.pengtoolbox.cfw.response.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.usermgmt.Permission;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class MenuItem extends HierarchicalHTMLItem {
	
	private String menuName = "&nbsp;";
	private String label = "&nbsp;";
	private String faiconClasses = "";
	private String alignRightClass = "";
	
	// if any permissions match item will be rendered
	// if null item will be rendered
	private HashSet<String> permissions = new HashSet<String>();
	private LinkedHashMap<String, MenuItem> childMenuItems = new LinkedHashMap<String, MenuItem>();
	
	public MenuItem(String name) {
		this.menuName = name;
		this.label = name;
		this.addAttribute("href", "#");
	}
	
	public MenuItem(String name, String label) {
		this.menuName = name;
		this.label = label;
		this.addAttribute("href", "#");
	}
	
	public MenuItem(String label, HashSet<String> permissions) {
		this.menuName = label;
		this.label = label;
		this.permissions = permissions;
		this.addAttribute("href", "#");
	}
			
	/***********************************************************************************
	 * Overrloaded addChild to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public MenuItem addChild(HierarchicalHTMLItem childItem) {
		super.addChild(childItem);
		
		if(childItem instanceof MenuItem) {
			childMenuItems.put(((MenuItem)childItem).getLabel(), (MenuItem)childItem);
			this.addPermissions(((MenuItem)childItem).getPermissions());
		}
		return this;
	}
	
	/***********************************************************************************
	 * Overrride to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public LinkedHashMap<String, MenuItem> getSubMenuItems() {
		return childMenuItems;
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected void createHTML(StringBuilder html) {
		
		//Check Login
//		SessionData session = CFW.Context.Request.getSessionData();
//		if( CFW.Properties.AUTHENTICATION_ENABLED == false ||
//			( session != null && session.isLoggedIn()) ) {
//		}
		
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
				return;
			}
		}
		
		//----------------------------------
		// Render Menu Item
		if(this.menuName.isEmpty()) {
			this.addCssClass("menu-item-icon-only");
		}
		String cssClass = this.popAttributeValue("class");
		
		if(!this.hasChildren() && !this.hasOneTimeChildren()) {
			html.append("\n<li class=\""+cssClass+"\">")
				.append("<a class=\"dropdown-item\" "+this.getAttributesString()+">")
					.append("<div class=\"cfw-fa-box\"><i class=\""+faiconClasses+"\"></i></div>")
					.append("<span class=\"cfw-menuitem-label\">")
						.append(label)
					.append("</span>")
				.append("</a></li>");   
		}else {
			String submenuClass = "";
			if(parent instanceof MenuItem) {
				submenuClass = "";
			}
			
			html.append("\n<li class=\"dropdown "+cssClass+"\">")
				.append("\n<a "+this.getAttributesString()+"class=\"dropdown-item dropdown-toggle\" id=\"cfwMenuDropdown\" data-toggle=\"dropdown\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">")
					.append("<div class=\"cfw-fa-box\"><i class=\""+faiconClasses+"\"></i></div>")
					.append("<span class=\"cfw-menuitem-label\">")
						.append(label)
					.append("</span>")  
				.append("<span class=\"caret\"></span></a>")   
				.append("\n<ul class=\"dropdown-menu dropdown-submenu "+submenuClass+alignRightClass+"\" aria-labelledby=\"cfwMenuDropdown\">");

			for(HierarchicalHTMLItem child : children) {
				html.append("\t"+child.getHTML());
			}
			
			for(HierarchicalHTMLItem child : oneTimeChildren) {
				if(child instanceof MenuItem) {
					html.append("\t"+((MenuItem)child).getHTML());
				}
			}
			html.append("\n</ul></li>");
		}
		
	}
	
	/***********************************************************************************
	 * Add the permission needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public MenuItem addPermission(String permission) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.add(permission);
		
		if(this.parent != null && parent instanceof MenuItem) {
			((MenuItem)parent).addPermission(permission);
		}
		
		return this;
	}
	
	
	/***********************************************************************************
	 * Add the permissions needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public MenuItem addPermissions(HashSet<String> permissionArray) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.addAll(permissionArray);
		
		if(this.parent != null && parent instanceof MenuItem) {
			((MenuItem)parent).addPermissions(permissionArray);
		}
		
		return this;
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
		return menuName;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public MenuItem setLabel(String label) {
		fireChange();
		this.menuName = label;
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public HierarchicalHTMLItem href(String href) {
		return addAttribute("href", href);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public MenuItem faicon(String faiconClasses) {
		this.faiconClasses = faiconClasses;
		return this;
	}

	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public void alignDropdownRight(boolean alignRight) {
		if(alignRight) {
			alignRightClass = "dropdown-menu-right";
		}else {
			alignRightClass = "";
		}
	}
	

	
	/***********************************************************************************
	 * 
	 * @param 
	 ***********************************************************************************/
	public MenuItem createCopy() {
		return copyInto(new MenuItem(""));
	}
	
	/***********************************************************************************
	 * 
	 * @param 
	 ***********************************************************************************/
	public MenuItem copyInto(MenuItem targetItem) {
		
		//------------------------------------
		// Copy Menu Fields
		targetItem.creator = this.creator;
		targetItem.menuName = this.menuName;
		targetItem.label = this.label;
		targetItem.alignRightClass = this.alignRightClass;
		targetItem.faiconClasses = this.faiconClasses;
		targetItem.permissions = this.permissions;
		
		for(HierarchicalHTMLItem child : children) {
			if(child instanceof MenuItem) {
				targetItem.addChild( ((MenuItem)child).createCopy());
			}else {
				targetItem.addChild(child);
			}
		}
		
		//------------------------------------
		// Copy HierarchicalHTMLItem Fields
		targetItem.oneTimeChildren = this.oneTimeChildren;
		targetItem.hasChanged = true;
		targetItem.html = null;
		
		for(Entry<String, String> entry : this.attributes.entrySet()) {
			targetItem.attributes.put(entry.getKey(), entry.getValue());
		}
		
		//don't copy parent - targetItem.parent = this.parent;
		
		return targetItem;
		
	}
	
	

}

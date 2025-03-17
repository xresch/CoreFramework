package com.xresch.cfw.response.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.Permission;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemMenuItem extends CFWHTMLItem {
	
	private String menuName = "&nbsp;";
	private String label = "&nbsp;";
	private String faiconClasses = "";
	private String alignRightClass = "";
	
	private boolean noIconSpace = false;
	
	// if any permissions match item will be rendered
	// if null item will be rendered
	private HashSet<String> permissions = new HashSet<String>();
	private LinkedHashMap<String, CFWHTMLItemMenuItem> childMenuItems = new LinkedHashMap<String, CFWHTMLItemMenuItem>();
	
	public CFWHTMLItemMenuItem(String name) {
		this.menuName = name;
		this.label = name;
		this.addAttribute("href", "#");
	}
	
	public CFWHTMLItemMenuItem(String name, String label) {
		this.menuName = name;
		this.label = label;
		this.addAttribute("href", "#");
	}
	
	public CFWHTMLItemMenuItem(String label, HashSet<String> permissions) {
		this.menuName = label;
		this.label = label;
		this.permissions = permissions;
		this.addAttribute("href", "#");
	}
			
	/***********************************************************************************
	 * Overrloaded addChild to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	@Override
	public CFWHTMLItemMenuItem addChild(CFWHTMLItem childItem) {
		super.addChild(childItem);
		
		if(childItem instanceof CFWHTMLItemMenuItem) {
			childMenuItems.put(((CFWHTMLItemMenuItem)childItem).getMenuName(), (CFWHTMLItemMenuItem)childItem);
			this.addPermissions(((CFWHTMLItemMenuItem)childItem).getPermissions());
		}
		return this;
	}
	
	/***********************************************************************************
	 * Overrride to handle sub menu items.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public LinkedHashMap<String, CFWHTMLItemMenuItem> getSubMenuItems() {
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
				if(usersPermissions != null && usersPermissions.containsKey(permission)) {
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
		
		// add class to li-element and not to the link-element
		String cssClass = this.popAttributeValue("class");
		
		String iconString = "";
		if(!noIconSpace) {
			iconString = "<div class=\"cfw-fa-box\"><i class=\""+faiconClasses+"\"></i></div>";
		}
		if(!this.hasChildren() && !this.hasOneTimeChildren()) {
			html.append("\n<li class=\""+cssClass+"\">")
				.append("<a class=\"dropdown-item\" "+this.getAttributesString()+">")
					.append(iconString)
					.append("<span class=\"cfw-menuitem-label\">")
						.append(label)
					.append("</span>")
				.append("</a></li>");   
		}else {
			String submenuClass = "";
			if(parent instanceof CFWHTMLItemMenuItem) {
				submenuClass = "";
			}
			
			html.append("\n<li class=\"dropdown "+cssClass+"\">")
				.append("\n<a "+this.getAttributesString()+"class=\"dropdown-item dropdown-toggle\" data-toggle=\"dropdown\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">")
					.append("<div class=\"cfw-fa-box\"><i class=\""+faiconClasses+"\"></i></div>")
					.append("<span class=\"cfw-menuitem-label\">")
						.append(label)
					.append("</span>")  
				.append("<span class=\"caret\"></span></a>")   
				.append("\n<ul class=\"dropdown-menu dropdown-submenu "+submenuClass+alignRightClass+"\" >");

			for(CFWHTMLItem child : children) {
				html.append("\t"+child.getHTML());
			}
			
			for(CFWHTMLItem child : oneTimeChildren) {
				if(child instanceof CFWHTMLItemMenuItem) {
					html.append("\t"+((CFWHTMLItemMenuItem)child).getHTML());
				}
			}
			html.append("\n</ul></li>");
		}
		
		//-------------------------------------
		// add class back in case of rerendering
		this.addAttribute("class", cssClass);
		
	}
	
	/***********************************************************************************
	 * Add the permission needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public CFWHTMLItemMenuItem addPermission(String permission) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.add(permission);
		
		if(this.parent != null && parent instanceof CFWHTMLItemMenuItem) {
			((CFWHTMLItemMenuItem)parent).addPermission(permission);
		}
		
		return this;
	}
	
	
	/***********************************************************************************
	 * Add the permissions needed to see this menu item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public CFWHTMLItemMenuItem addPermissions(HashSet<String> permissionArray) {
		if(permissions == null) {
			permissions = new HashSet<String>();
		}
		
		permissions.addAll(permissionArray);
		
		if(this.parent != null && parent instanceof CFWHTMLItemMenuItem) {
			((CFWHTMLItemMenuItem)parent).addPermissions(permissionArray);
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
	public String getMenuName() {
		return menuName;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public CFWHTMLItemMenuItem setMenuName(String menuName) {
		fireChange();
		this.menuName = menuName;
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public CFWHTMLItem href(String href) {
		return addAttribute("href", href);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public CFWHTMLItemMenuItem noIconSpace(boolean noIconSpace) {
		this.noIconSpace = noIconSpace;
		return this;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public CFWHTMLItemMenuItem faicon(String faiconClasses) {
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
	public CFWHTMLItemMenuItem createCopy() {
		return copyInto(new CFWHTMLItemMenuItem(""));
	}
	
	/***********************************************************************************
	 * 
	 * @param 
	 ***********************************************************************************/
	public CFWHTMLItemMenuItem copyInto(CFWHTMLItemMenuItem targetItem) {
		
		//------------------------------------
		// Copy Menu Fields
		targetItem.creator = this.creator;
		targetItem.menuName = this.menuName;
		targetItem.label = this.label;
		targetItem.alignRightClass = this.alignRightClass;
		targetItem.faiconClasses = this.faiconClasses;
		targetItem.permissions = this.permissions;
		
		for(CFWHTMLItem child : children) {
			if(child instanceof CFWHTMLItemMenuItem) {
				targetItem.addChild( ((CFWHTMLItemMenuItem)child).createCopy());
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

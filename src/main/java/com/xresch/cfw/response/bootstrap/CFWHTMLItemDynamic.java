package com.xresch.cfw.response.bootstrap;

import java.util.ArrayList;

public abstract class CFWHTMLItemDynamic {

	private CFWHTMLItem parent;
	
	/**********************************************************************
	 * Create and return you dynamic items.
	 * @return
	 **********************************************************************/
	public abstract ArrayList<CFWHTMLItem> createDynamicItems();
	
	
	public void setParent(CFWHTMLItem parent) { 
		this.parent = parent; 
	}
	
	
	public CFWHTMLItem getParent() { 
		return parent; 
	}
	
}

package com.pengtoolbox.cfw.response.bootstrap;

import java.util.ArrayList;

public abstract class DynamicItemCreator {

	/**********************************************************************
	 * Create and return you dynamic items.
	 * @return
	 **********************************************************************/
	public abstract ArrayList<HierarchicalHTMLItem> createDynamicItems();
	
}

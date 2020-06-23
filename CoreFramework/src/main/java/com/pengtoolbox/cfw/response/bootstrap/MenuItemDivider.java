package com.pengtoolbox.cfw.response.bootstrap;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class MenuItemDivider extends HierarchicalHTMLItem {
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public void createHTML(StringBuilder html) {
		html.append("\n<div class=\"dropdown-divider border-primary\"></div>");
	}
	

}

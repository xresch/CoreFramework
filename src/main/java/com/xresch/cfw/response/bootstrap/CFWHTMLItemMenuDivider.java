package com.xresch.cfw.response.bootstrap;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemMenuDivider extends CFWHTMLItem {
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public void createHTML(StringBuilder html) {
		html.append("\n<div class=\"dropdown-divider border-primary\"></div>");
	}
	

}

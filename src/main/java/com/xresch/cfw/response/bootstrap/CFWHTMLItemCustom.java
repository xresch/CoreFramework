package com.xresch.cfw.response.bootstrap;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemCustom extends CFWHTMLItem {
	
	private String htmlString = "&nbsp;";
	
	public CFWHTMLItemCustom(String htmlString) {
		this.htmlString = htmlString;
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public void createHTML(StringBuilder html) {
		
		html.append(htmlString);
		
	}

	public String getHtmlString() {
		return htmlString;
	}

	public CFWHTMLItemCustom setHtmlString(String htmlString) {
		fireChange();
		this.htmlString = htmlString;
		return this;
	}




	
	

	
	
	

}

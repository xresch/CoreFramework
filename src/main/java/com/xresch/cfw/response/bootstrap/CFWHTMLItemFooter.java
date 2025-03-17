package com.xresch.cfw.response.bootstrap;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemFooter extends CFWHTMLItem {
	
	private String label = "&nbsp;";
	

	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected void createHTML(StringBuilder html) {

		html.append("<div id=\"cfw-footer\" class=\"flex-default flex-column\">");
		
		if(this.hasChildren()) {
				
			for(CFWHTMLItem child : children) {
				html.append("\t"+child.getHTML());
			}
		}
		
		if(this.hasOneTimeChildren()) {
			
			for(CFWHTMLItem child : oneTimeChildren) {
				html.append("\t"+child.getHTML());
			}
		}
		
		html.append("</div>");
	}

	public String getLabel() {
		return label;
	}

	public CFWHTMLItemFooter setLabel(String label) {
		fireChange();
		this.label = label;
		return this;
	}

}

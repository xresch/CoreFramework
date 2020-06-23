package com.pengtoolbox.cfw.response.bootstrap;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class BTFooter extends HierarchicalHTMLItem {
	
	private String label = "&nbsp;";
	
	public BTFooter() {
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected void createHTML(StringBuilder html) {

		html.append("<div id=\"cfw-footer\" class=\"flex-default flex-column\">");
		
		if(this.hasChildren()) {
				
			for(HierarchicalHTMLItem child : children) {
				html.append("\t"+child.getHTML());
			}
		}
		
		if(this.hasOneTimeChildren()) {
			
			for(HierarchicalHTMLItem child : oneTimeChildren) {
				html.append("\t"+child.getHTML());
			}
		}
		
		html.append("</div>");
	}

	public String getLabel() {
		return label;
	}

	public BTFooter setLabel(String label) {
		fireChange();
		this.label = label;
		return this;
	}

}

package com.pengtoolbox.cfw.response.bootstrap;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class BTLink extends HierarchicalHTMLItem {
	
	private String label = "&nbsp;";
	
	public BTLink(String label, String href) {
		this.label = label;
		this.href(href);
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	public void createHTML(StringBuilder html) {
		
		if(!this.hasChildren() && !this.hasOneTimeChildren()) {
			html.append("<a "+this.getAttributesString()+">"+label+"</a>");   
		}else {

			html.append("<a "+this.getAttributesString()+">"+label+"</a>");   
			
			for(HierarchicalHTMLItem child : children) {
				if(child instanceof BTLink) {
					html.append("\t"+((BTLink)child).getHTML());
				}
			}
			
			for(HierarchicalHTMLItem child : oneTimeChildren) {
				if(child instanceof BTLink) {
					html.append("\t"+((BTLink)child).getHTML());
				}
			}
			
			html.append("\n</a>");
		}
		
	}

	public String getLabel() {
		return label;
	}

	public BTLink setLabel(String label) {
		fireChange();
		this.label = label;
		return this;
	}
	
	public HierarchicalHTMLItem href(String href) {
		return addAttribute("href", href);
	}



	
	

	
	
	

}

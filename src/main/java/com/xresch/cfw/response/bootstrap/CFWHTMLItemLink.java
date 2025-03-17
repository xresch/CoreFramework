package com.xresch.cfw.response.bootstrap;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemLink extends CFWHTMLItem {
	
	private String label = "&nbsp;";
	
	public CFWHTMLItemLink(String label, String href) {
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
			
			for(CFWHTMLItem child : children) {
				if(child instanceof CFWHTMLItemLink) {
					html.append("\t"+((CFWHTMLItemLink)child).getHTML());
				}
			}
			
			for(CFWHTMLItem child : oneTimeChildren) {
				if(child instanceof CFWHTMLItemLink) {
					html.append("\t"+((CFWHTMLItemLink)child).getHTML());
				}
			}
			
			html.append("\n</a>");
		}
		
	}

	public String getLabel() {
		return label;
	}

	public CFWHTMLItemLink setLabel(String label) {
		fireChange();
		this.label = label;
		return this;
	}
	
	public CFWHTMLItem href(String href) {
		return addAttribute("href", href);
	}



	
	

	
	
	

}

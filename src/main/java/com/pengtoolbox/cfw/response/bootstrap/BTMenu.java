package com.pengtoolbox.cfw.response.bootstrap;

import java.util.ArrayList;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.config.Configuration;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class BTMenu extends HierarchicalHTMLItem {
	
	private String label = "&nbsp;";
	private UserMenuItem userMenuItem = null;
	private ArrayList<MenuItem> rightMenuItems = new ArrayList<MenuItem>();
	
	public BTMenu() {
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected void createHTML(StringBuilder html) {
		
		html.append("\n<div id=\"menubar\" class=\"navbar\">");
		html.append("  <a class=\"navbar-brand navbarFlexibleSizeHack\">&nbsp;</a>");
		
		html.append("<nav class=\"navbar navbar-expand-md fixed-top navbar-dark\">");
		html.append("  <a class=\"navbar-brand\" href=\"#\">");
		
			String logopath = CFW.DB.Config.getConfigAsString(Configuration.LOGO_PATH);
			if(logopath != null && !logopath.isEmpty()) {
				html.append("<img id=\"cfw-logo\" src=\""+logopath+"\" />");
			}
			
		html.append(this.label+"</a>");
		html.append("  <button class=\"navbar-toggler\" type=\"button\" data-toggle=\"collapse\" data-target=\"#cfw-navbar-top\" aria-controls=\"cfw-navbar-top\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">");
		html.append("    <span class=\"navbar-toggler-icon\"></span>");
		html.append("  </button>");

		html.append("  <div class=\"collapse navbar-collapse\" id=\"cfw-navbar-top\">");
		html.append("    <ul class=\"navbar-nav mr-auto\">");
		

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
		html.append("\n</ul>");
		
		//-----------------------------
		// Right User menus
		html.append("\n<ul class=\"nav navbar-nav navbar-right\">");
			for(MenuItem item : rightMenuItems) {
				html.append(item.getHTML());
			}
			
			//-----------------------------
			// User Menu
			if(this.userMenuItem != null) {	
				html.append(userMenuItem.getHTML());
			}
		html.append("\n</ul>");
		
		html.append("\n</div></div></nav></div>");
	}

	public String getLabel() {
		return label;
	}

	public BTMenu setLabel(String label) {
		fireChange();
		this.label = label;
		return this;
	}

	public UserMenuItem getUserMenuItem() {
		return userMenuItem;
	}

	public BTMenu addRightMenuItem(MenuItem item) {
		fireChange();
		rightMenuItems.add(item);
		return this;
	}
	public BTMenu setUserMenuItem(UserMenuItem userMenuItem) {
		fireChange();
		this.userMenuItem = userMenuItem;
		return this;
	}
	
	

}

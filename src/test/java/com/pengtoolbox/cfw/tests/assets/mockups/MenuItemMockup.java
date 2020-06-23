package com.pengtoolbox.cfw.tests.assets.mockups;

import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

public class MenuItemMockup extends MenuItem {

	public MenuItemMockup(String label) {
		super(label);
		
		MenuItem subDropdown = new MenuItem("Sub Dropdown");
		
		subDropdown.addCssClass("dropdownClass")
				   .addChild(new MenuItem("Sub Subitem 1"))
				   .addChild(new MenuItem("Sub Subitem 2"));
		
		this.addChild(new MenuItem("href Subitem").href("./test/servlet"))
			.addChild(new MenuItem("onclick Subitem").href(null).onclick("draw('test');"))
			.addChild(new MenuItem("cssClass Subitem").addCssClass("mockup-class test-class"))
			.addChild(subDropdown);
	}

}

package com.xresch.cfw.tests.assets.mockups;

import com.xresch.cfw.response.bootstrap.MenuItem;

public class MockupMenuItem extends MenuItem {

	public MockupMenuItem(String label) {
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

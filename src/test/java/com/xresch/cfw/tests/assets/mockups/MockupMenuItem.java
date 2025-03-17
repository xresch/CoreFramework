package com.xresch.cfw.tests.assets.mockups;

import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;

public class MockupMenuItem extends CFWHTMLItemMenuItem {

	public MockupMenuItem(String label) {
		super(label);
		
		CFWHTMLItemMenuItem subDropdown = new CFWHTMLItemMenuItem("Sub Dropdown");
		
		subDropdown.addCssClass("dropdownClass")
				   .addChild(new CFWHTMLItemMenuItem("Sub Subitem 1"))
				   .addChild(new CFWHTMLItemMenuItem("Sub Subitem 2"));
		
		this.addChild(new CFWHTMLItemMenuItem("href Subitem").href("./test/servlet"))
			.addChild(new CFWHTMLItemMenuItem("onclick Subitem").href(null).onclick("draw('test');"))
			.addChild(new CFWHTMLItemMenuItem("cssClass Subitem").addCssClass("mockup-class test-class"))
			.addChild(subDropdown);
	}

}

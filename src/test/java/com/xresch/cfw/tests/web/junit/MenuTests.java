package com.xresch.cfw.tests.web.junit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenu;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.tests._master.WebTestMaster;
import com.xresch.cfw.tests.assets.mockups.MockupMenuItem;

public class MenuTests extends WebTestMaster{

	@Test
	public void testMenuRegistry() {
		
		//---------------------------
		// Test Menu Hierarchy
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Top Item"), null);
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("A"), "Top Item");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("B"), " Top Item | A ");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("C"), " Top Item | A | B");
		
		//---------------------------
		// Test Menu Hierarchy 2
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Top Item 2"), null);
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Item"), "Top Item 2");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Sub Item"), " Top Item 2 | Sub Item ");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Sub Item 2"), "Top Item 2 | Sub Item ");
		
		//---------------------------
		// Test Override
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Item"), "Top Item 2");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Sub Item"), " Top Item 2 | Sub Item ");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Sub Sub Item 2"), "Top Item 2 | Sub Item ");
		
		//---------------------------
		// Test addChild combo
		CFW.Registry.Components.addUserMenuItem(new CFWHTMLItemMenuItem("User Top")
				.addChild(new CFWHTMLItemMenuItem("User A")
							.addChild(new CFWHTMLItemMenuItem("User B"))
						 )
				
				, null);
		CFW.Registry.Components.addUserMenuItem(new CFWHTMLItemMenuItem("User C"), "User Top | User A | User B");
		
		//---------------------------
		// Dump and Check
		
		String dump = CFW.Registry.Components.dumpMenuItemHierarchy();
		System.out.println(dump);
		
		Assertions.assertTrue(dump.contains("|      |--> C"), 
				"Item C is present.");
		
		Assertions.assertTrue(dump.contains("    |--> Sub Sub Item"), 
				"Sub Sub Item is present and on correct level.");
		
		Assertions.assertTrue(dump.contains("    |--> Sub Sub Item 2"), 
				"Sub Sub Item 2 is present and on correct level.");
		
		Assertions.assertTrue(dump.contains("|--> User C"), 
				"User C is present and on correct level.");

		
		//---------------------------
		// Create and Check Menu
		CFWSessionData stubData = new CFWSessionData("sessionID");
		CFWHTMLItemMenu menu = CFW.Registry.Components.createMenuInstance(stubData, false);
		//System.out.println(CFW.Dump.dumpObject(menu));
		String html = menu.getHTML();
		System.out.println("========= HTML =========\n"+html);
		
	}
	
	
	@Test
	public void testBootstrapMenu() {
		
		CFWHTMLItemMenu menu = new CFWHTMLItemMenu().setLabel("TEST MENU");
		
		menu.addChild(new MockupMenuItem("Mockup Menu A"))
			.addChild(new MockupMenuItem("Mockup Menu B"))
			.addChild(new CFWHTMLItemMenuItem("Single Item").href("./singleitemlink"))
			.addOneTimeChild(new CFWHTMLItemMenuItem("OneTime Item"));

		String html = menu.getHTML();
		
		System.out.println(html);
		
		
		Assertions.assertTrue(html.contains(">TEST MENU</a>"), 
				"Menu Label is present.");
		
		Assertions.assertTrue(html.contains("<span class=\"cfw-menuitem-label\">Mockup Menu A</span>"), 
				"Mockup Menu A is set.");
		
		Assertions.assertTrue(html.contains("<span class=\"cfw-menuitem-label\">Mockup Menu B</span>"), 
				"Mockup Menu B is set.");
		
		Assertions.assertTrue(html.contains("href=\"./singleitemlink\""), 
				"Single Item is present.");
		
		Assertions.assertTrue(html.contains(">OneTime Item<"), 
				"OneTime Item is present.");
		
		String htmlNoOneTimeItem = menu.getHTML();
		
		Assertions.assertTrue(!htmlNoOneTimeItem.contains(">OneTime Item<"), 
				"OneTime Item was removed.");
		
	}
		
	@Test
	public void testMenuItem() {
		
		MockupMenuItem menu = new MockupMenuItem("Mockup Menu Item");
		
		String html = menu.getHTML();
		
		System.out.println(html);
		
		Assertions.assertTrue(html.contains("Mockup Menu Item</span><span class=\"caret\">"), 
				"Mockup Menu Item label is set");
		
	}
	
	
}

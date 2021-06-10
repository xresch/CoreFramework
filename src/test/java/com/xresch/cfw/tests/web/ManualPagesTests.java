package com.xresch.cfw.tests.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.tests._master.WebTestMaster;

public class ManualPagesTests extends WebTestMaster {

	@Test
	public void testMenuRegistry() {
		
		//---------------------------
		// Test Menu Hierarchy
		CFW.Registry.Manual.addManualPage(null, new ManualPage("Top Page"));
		CFW.Registry.Manual.addManualPage("Top Page", new ManualPage("A"));
		CFW.Registry.Manual.addManualPage("Top Page | A", new ManualPage("B"));
		CFW.Registry.Manual.addManualPage("Top Page | A | B", new ManualPage("C") );
		
		//---------------------------
		// Test Menu Hierarchy 2
		CFW.Registry.Manual.addManualPage(null, new ManualPage("Top Item 2"));
		CFW.Registry.Manual.addManualPage("Top Item 2", new ManualPage("Sub Item"));
		CFW.Registry.Manual.addManualPage(" Top Item 2 | Sub Item ", new ManualPage("Sub Sub Item"));
		CFW.Registry.Manual.addManualPage("Top Item 2 | Sub Item ", new ManualPage("Sub Sub Item 2"));
		
		//---------------------------
		// Test Override
		CFW.Registry.Manual.addManualPage("Top Item 2", new ManualPage("Sub Item"));
		CFW.Registry.Manual.addManualPage(" Top Item 2 | Sub Item ", new ManualPage("Sub Sub Item"));
		CFW.Registry.Manual.addManualPage("Top Item 2 | Sub Item ", new ManualPage("Sub Sub Item 2"));
		
		//---------------------------
		// Test addChild combo
		CFW.Registry.Manual.addManualPage(null
				, new ManualPage("User Top")
						.addChild(new ManualPage("User A")
									.addChild(new ManualPage("User B"))
								 ));
		
		CFW.Registry.Manual.addManualPage("User Top | User A | User B", new ManualPage("User C"));
		
		//---------------------------
		// Dump and Check
		
		String dump = CFW.Registry.Manual.dumpManualPageHierarchy();
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
		JsonArray pagesArray = CFW.Registry.Manual.getManualPagesForUserAsJSON(stubData);
		System.out.println("========= JSON =========\n"+pagesArray.toString());
		
	}

}

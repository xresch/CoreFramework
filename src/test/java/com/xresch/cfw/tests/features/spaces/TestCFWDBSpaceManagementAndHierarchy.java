package com.xresch.cfw.tests.features.spaces;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.spaces.Space;
import com.xresch.cfw.features.spaces.Space.SpaceFields;
import com.xresch.cfw.features.spaces.SpaceGroup;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWDBSpaceManagementAndHierarchy extends DBTestMaster {

	
	@BeforeAll
	public static void fillWithTestData() {
		
		//----------------------------------------
		// Create SpaceGroups
		CFW.DB.SpaceGroups.create(new SpaceGroup("SpaceGroupA"));
		SpaceGroup SpaceGroupA = CFW.DB.SpaceGroups.selectByName("SpaceGroupA");
		
		CFW.DB.SpaceGroups.create(new SpaceGroup("SpaceGroupB"));
		SpaceGroup SpaceGroupB = CFW.DB.SpaceGroups.selectByName("SpaceGroupB");
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		if(!CFW.DB.Spaces.checkSpaceExists("FacespaceB")) {
			CFW.DB.Spaces.create(
					new Space(SpaceGroupB.id(), "FacespaceB")
						.description("A spacy space for your face.")
						.isDeletable(true)
						.isRenamable(true)
			);
		}
		
		Space parentSpace = CFW.DB.Spaces.selectByName("FacespaceB");
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		for(int i = 0; i < 10; i++) {
			String spacename = "Subface"+i;
			if(!CFW.DB.Spaces.checkSpaceExists(spacename)) {
				
				Space subSpace = new Space(SpaceGroupB.id(), spacename)
					.description("A spacy subspace for other faces.")
					.isDeletable(true)
					.isRenamable(true);
				
				if(subSpace.setParent(parentSpace)) {
					CFW.DB.Spaces.create(subSpace);
					parentSpace = CFW.DB.Spaces.selectByName(spacename);
					System.out.println(parentSpace.dumpFieldsAsKeyValueString());
				}
			}
		}
		
	}

	@Test
	public void testCRUDSpaceGroup() {
		
		String spacegroupname = "Test Spacegroup";
		String spacegroupnameUpdated = "Test SpacegroupUPDATED";
		
		//--------------------------------------
		// Cleanup
		SpaceGroup spacegroupToDelete = CFW.DB.SpaceGroups.selectByName(spacegroupname);
		if(spacegroupToDelete != null) {
			CFW.DB.SpaceGroups.deleteByID(spacegroupToDelete.id());
		}

		spacegroupToDelete = CFW.DB.SpaceGroups.selectByName(spacegroupnameUpdated);
		if(spacegroupToDelete != null) {
			CFW.DB.SpaceGroups.deleteByID(spacegroupToDelete.id());
		}
		Assertions.assertFalse(CFW.DB.SpaceGroups.checkSpaceGroupExists(spacegroupname), "Config doesn't exists, checkConfigExists(String) works.");
		Assertions.assertFalse(CFW.DB.SpaceGroups.checkSpaceGroupExists(spacegroupToDelete), "Config doesn't exist, checkConfigExists(Config) works.");
		
		
		//--------------------------------------
		// CREATE
		CFW.DB.SpaceGroups.create(
				new SpaceGroup(spacegroupname)
				.description("Testdescription")
		);
		
		Assertions.assertTrue(CFW.DB.SpaceGroups.checkSpaceGroupExists(spacegroupname), "Config created successfully, checkConfigExists(String) works.");

		//--------------------------------------
		// SELECT BY NAME
		SpaceGroup spacegroup = CFW.DB.SpaceGroups.selectByName(spacegroupname);
		
		System.out.println("===== CONFIG =====");
		System.out.println(spacegroup.dumpFieldsAsKeyValueString());

		Assertions.assertNotNull(spacegroup);
		Assertions.assertEquals(spacegroup.name(), spacegroupname);
		Assertions.assertEquals(spacegroup.description(), "Testdescription");
		
		//--------------------------------------
		// UPDATE
		spacegroup.name(spacegroupnameUpdated)
			.description("Testdescription2");
		
		CFW.DB.SpaceGroups.update(spacegroup);
		
		//--------------------------------------
		// SELECT UPDATED CONFIG
		SpaceGroup updatedConfig = CFW.DB.SpaceGroups.selectByName(spacegroupnameUpdated);
		
		System.out.println("===== UPDATED CONFIG =====");
		System.out.println(updatedConfig.dumpFieldsAsKeyValueString());
		
		Assertions.assertNotNull(spacegroup);
		Assertions.assertEquals(spacegroup.name(), spacegroupnameUpdated);
		Assertions.assertEquals(spacegroup.description(), "Testdescription2");
		
		//--------------------------------------
		// SELECT BY ID
		SpaceGroup spacegroupByID = CFW.DB.SpaceGroups.selectByID(updatedConfig.id());
		
		Assertions.assertNotNull(spacegroupByID, "Config is selected by ID.");
		
		//--------------------------------------
		// DELETE
		CFW.DB.SpaceGroups.deleteByID(updatedConfig.id());
		Assertions.assertFalse(CFW.DB.SpaceGroups.checkSpaceGroupExists(spacegroupname));
				
	}
	
	@Test
	public void testSpaceHierarchy() {
		

		int spacegroupid = CFW.DB.SpaceGroups.selectByName(SpaceGroup.CFW_SPACEGROUP_TESTSPACE).id();
		
		//-----------------------------------------
		// Create MySpace Parent
		//-----------------------------------------
		if(!CFW.DB.Spaces.checkSpaceExists("MySpace")) {
			CFW.DB.Spaces.create(
					new Space(spacegroupid, "MySpace")
						.description("A space for spacing away.")
						.isDeletable(true)
						.isRenamable(true)
			);
		}
		
		Space parentSpace = CFW.DB.Spaces.selectByName("MySpace");
		
		//-----------------------------------------
		// Create MySpace Children
		//-----------------------------------------
		for(int i = 0; i < 10; i++) {
			String spacename = "SubSpace"+i;
			if(!CFW.DB.Spaces.checkSpaceExists(spacename)) {
				
				Space subSpace = new Space(spacegroupid, spacename)
					.description("A sub space for spacing away.")
					.isDeletable(true)
					.isRenamable(true);
				
				if(subSpace.setParent(parentSpace)) {
					CFW.DB.Spaces.create(subSpace);
					parentSpace = CFW.DB.Spaces.selectByName(spacename);
					System.out.println(parentSpace.dumpFieldsAsKeyValueString());
				}
			}
		}
		
		//-----------------------------------------
		// All subelements of MySpace including MySpace
		//-----------------------------------------
		Object[] fieldnames = 
				new Object[] {
					SpaceFields.NAME,
				};
				
		parentSpace = CFW.DB.Spaces.selectByName("MySpace");
		String csv = new CFWHierarchy<Space>(parentSpace)
				.setFilter(new CFWSQL(parentSpace).and(
							SpaceFields.FK_ID_SPACEGROUP.toString(), spacegroupid)
						)
				.createFetchHierarchyQuery(fieldnames)	
				.getAsCSV();
		
		System.out.println("============= HIERARCHY RESULTS =============");
		System.out.println(csv);
		Assertions.assertTrue(csv.contains("MySpace"), "Root element is in list.");
		Assertions.assertTrue(csv.contains("SubSpace9"), "Last subelement is in list.");
		
		//-----------------------------------------
		// All subelements of SubSpace6 including SubSpace6
		// filtered by space ID
		//-----------------------------------------
		parentSpace = CFW.DB.Spaces.selectByName("SubSpace6");
		csv = new CFWHierarchy<Space>(parentSpace)
				.setFilter(
						new CFWSQL(parentSpace).and(
						SpaceFields.FK_ID_SPACEGROUP.toString(), spacegroupid)
						)
				.createFetchHierarchyQuery(fieldnames)
				.getAsCSV();
	
		System.out.println("============= HIERARCHY RESULTS =============");
		System.out.println(csv);
		Assertions.assertTrue(csv.contains("SubSpace6"), "List contains selected start element is in list.");
		Assertions.assertTrue(csv.contains("SubSpace9"), "Last subelement is in list.");
		Assertions.assertTrue(!csv.contains("MySpace"), "Root element is NOT in list.");
		Assertions.assertTrue(!csv.contains("SubSpace5"), "Element before start element is NOT in list.");
		
		
		//-----------------------------------------
		// Fetch all with primaryID null
		//-----------------------------------------
		csv = new CFWHierarchy<Space>(new Space(2, "dummyWithIDNull"))
				.setFilter(
					new CFWSQL(null).and(
					SpaceFields.FK_ID_SPACEGROUP.toString(), spacegroupid)
				)
				.createFetchHierarchyQuery(fieldnames)
				.getAsCSV();
		
		System.out.println("============= HIERARCHY RESULTS =============");
		System.out.println(csv);
		Assertions.assertTrue(csv.contains("MySpace"), "Root element is in list.");
		Assertions.assertTrue(csv.contains("SubSpace9"), "Last subelement is in list.");
		
		//-----------------------------------------
		// Fetch all with primaryID null
		//-----------------------------------------

		String hierarchyDump =  new CFWHierarchy<Space>(new Space(2, "dummyWithIDNull"))
				.fetchAndCreateHierarchy(fieldnames)
				.dumpHierarchy(new String[] {SpaceFields.PK_ID.toString(), SpaceFields.NAME.toString()});
		
		System.out.println("============= HIERARCHY DUMP =============");
		System.out.println(hierarchyDump);
		
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*--> \\d+ - MySpace[\\S\\s]*"), "Root element is in list.");
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*--> \\d+ - FacespaceB[\\S\\s]*"), "Root element is in list.");
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*\\|                  \\|--> \\d+ - Subface8[\\S\\s]*"), "Hierarchy is visualized.");

		//-----------------------------------------
		// Fetch all with primaryID null
		//-----------------------------------------
		JsonArray array =  new CFWHierarchy<Space>(new Space(2, "dummyWithIDNull"))
				.fetchAndCreateHierarchy(fieldnames)
				.toJSONArray();
		
		System.out.println("============= JSON Array =============");
		System.out.println(CFW.JSON.toJSONPretty(array));
		
		//-----------------------------------------
		// Test Circular Reference Check 
		//-----------------------------------------
		Space subspace2 = CFW.DB.Spaces.selectByName("SubSpace2");
		Space subspace2WithHierarchy = new CFWHierarchy<>(subspace2)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		Space subspace5 = CFW.DB.Spaces.selectByName("SubSpace5");
		Space subspace5WithHierarchy = new CFWHierarchy<>(subspace5)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		Assertions.assertFalse(subspace2WithHierarchy.setParent(subspace2WithHierarchy), "Cannot set as it's own parent.");
		Assertions.assertFalse(subspace2WithHierarchy.setParent(subspace5WithHierarchy), "Cannot set parent as it would cause a circular reference.");
		
		//-----------------------------------------
		// Test Parent Slots Counts
		//-----------------------------------------
		int depth = subspace2.getHierarchyConfig().getMaxDepth();
		int used =  CFWHierarchy.getUsedParentSlotsCount(subspace2);
		int available =  CFWHierarchy.getAvailableParentSlotsCount(subspace2);
		
		System.out.println("============= Test ParentSlots Counter =============");
		System.out.println("depth:"+depth);
		System.out.println("used:"+used);
		System.out.println("available:"+available);
		
		Assertions.assertEquals(3, used, "Used Parent slots are 3.");
		Assertions.assertEquals(depth-used, available, "Available parent slots are "+(depth-used)+".");
		
		//-----------------------------------------
		// Test Child Depth Counter
		//-----------------------------------------
		
		// refresh data as previous fail might have messed things up
		subspace2WithHierarchy = new CFWHierarchy<>(subspace2)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		subspace5WithHierarchy = new CFWHierarchy<>(subspace5)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		System.out.println("============= Test Child Depth Counter =============");
		System.out.println(CFW.JSON.toJSONPretty( 
				new CFWHierarchy<Space>(subspace5)
					.fetchAndCreateHierarchy()
					.toJSONArray()
				)
			);

		Assertions.assertEquals(7, CFWHierarchy.getMaxDepthOfHierarchy(subspace2WithHierarchy, 0), "Max Depth is 8.");
		Assertions.assertEquals(4, CFWHierarchy.getMaxDepthOfHierarchy(subspace5WithHierarchy, 0), "Max Depth is 5.");
		
		//-----------------------------------------
		// Test Child Max Depth: Negative
		//-----------------------------------------
		Space subface7 = CFW.DB.Spaces.selectByName("Subface7");
		Space subface7WithHierarchy = new CFWHierarchy<>(subface7)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		Assertions.assertFalse(subspace5WithHierarchy.setParent(subface7WithHierarchy), "Subface 8 does not have enough free parent slots(has 2, needs 4).");
		
		//-----------------------------------------
		// Test Child Max Depth: Positive
		//-----------------------------------------
		
		// Refresh data as previous fail might have messed things up
		subspace5WithHierarchy = new CFWHierarchy<>(subspace5)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		Space subface4 = CFW.DB.Spaces.selectByName("Subface4");
		Space subface4WithHierarchy = new CFWHierarchy<>(subface4)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		
		Assertions.assertTrue(subspace5WithHierarchy.setParent(subface4WithHierarchy), "Subface 4 has enough free parent slots(has 5, needs 5).");
		
	}
		
}

package com.pengtoolbox.cfw.tests.db;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWHierarchy;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.db.spaces.Space;
import com.pengtoolbox.cfw.db.spaces.Space.SpaceFields;
import com.pengtoolbox.cfw.db.spaces.SpaceGroup;
import com.pengtoolbox.cfw.tests._master.DBTestMaster;

public class TestCFWDBSpaceManagement extends DBTestMaster {

	
	@BeforeClass
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

		Assertions.assertTrue(spacegroup != null);
		Assertions.assertTrue(spacegroup.name().equals(spacegroupname));
		Assertions.assertTrue(spacegroup.description().equals("Testdescription"));
		
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
		
		Assertions.assertTrue(spacegroup != null);
		Assertions.assertTrue(spacegroup.name().equals(spacegroupnameUpdated));
		Assertions.assertTrue(spacegroup.description().equals("Testdescription2"));
		
		//--------------------------------------
		// SELECT BY ID
		SpaceGroup spacegroupByID = CFW.DB.SpaceGroups.selectByID(updatedConfig.id());
		
		Assertions.assertTrue(spacegroupByID != null, "Config is selected by ID.");
		
		//--------------------------------------
		// DELETE
		CFW.DB.SpaceGroups.deleteByID(updatedConfig.id());
		Assertions.assertFalse(CFW.DB.SpaceGroups.checkSpaceGroupExists(spacegroupname));
				
	}
	
	@Test
	public void testSpaceHierarchy() {
		

		int spacegroupid = CFW.DB.SpaceGroups.selectByName(SpaceGroup.CFW_SPACEGROUP_TESTSPACE).id();
		
		//-----------------------------------------
		// 
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
		// 
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
		String[] fieldnames = 
				new String[] {
					SpaceFields.PK_ID.toString(),
					SpaceFields.NAME.toString(),
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
				.dumpHierarchy(fieldnames);
		
		System.out.println("============= HIERARCHY DUMP =============");
		System.out.println(hierarchyDump);
		
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*--> \\d+ - MySpace[\\S\\s]*"), "Root element is in list.");
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*--> \\d+ - FacespaceB[\\S\\s]*"), "Root element is in list.");
		Assertions.assertTrue(hierarchyDump.matches("[\\S\\s]*\\|                  \\|--> \\d+ - Subface8[\\S\\s]*"), "Hierarchy is visualized.");

	}
	
}

package com.xresch.cfw.tests.features.keyvaluepairs;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWDBKeyValuePairs extends DBTestMaster {

	
	@BeforeAll
	public static void fillWithTestData() {
		
	}

	@Test
	public void testCRUDKey() {
		
		String keyname = "Test Key";
		String keynameUpdated = "Test KeyUPDATED";
		
		//--------------------------------------
		// Cleanup
		KeyValuePair keyToDelete = CFW.DB.KeyValuePairs.selectByKey(keyname);
		if(keyToDelete != null) {
			CFW.DB.KeyValuePairs.deleteByID(keyToDelete.id());
		}

		keyToDelete = CFW.DB.KeyValuePairs.selectByKey(keynameUpdated);
		if(keyToDelete != null) {
			CFW.DB.KeyValuePairs.deleteByID(keyToDelete.id());
		}
		Assertions.assertFalse(CFW.DB.KeyValuePairs.checkKeyExists(keyname), "Key doesn't exists, checkKeyExists(String) works.");
		Assertions.assertFalse(CFW.DB.KeyValuePairs.checkKeyExists(keyToDelete), "Key doesn't exist, checkKeyExists(Key) works.");
		
		
		//--------------------------------------
		// CREATE
		CFW.DB.KeyValuePairs.create(
				new KeyValuePair("Test", keyname)
				.description("Testdescription")
				.value("A")
		);
		
		Assertions.assertTrue(CFW.DB.KeyValuePairs.checkKeyExists(keyname), "Key created successfully, checkKeyExists(String) works.");

		//--------------------------------------
		// SELECT BY NAME
		KeyValuePair keyValuePair = CFW.DB.KeyValuePairs.selectByKey(keyname);
		
		System.out.println("===== KEY VALUE PAIR =====");
		System.out.println(keyValuePair.dumpFieldsAsKeyValueString());

		Assertions.assertNotNull(keyValuePair);
		Assertions.assertEquals(keyValuePair.key(), keyname);
		Assertions.assertEquals(keyValuePair.value(), "A");
		Assertions.assertEquals(keyValuePair.description(), "Testdescription");
		
		//--------------------------------------
		// UPDATE
		keyValuePair.key(keynameUpdated)
			.description("Testdescription2")
			.value("B");
		
		CFW.DB.KeyValuePairs.update(keyValuePair);
		
		//--------------------------------------
		// SELECT UPDATED CONFIG
		KeyValuePair updatedKeyValue = CFW.DB.KeyValuePairs.selectByKey(keynameUpdated);
		
		System.out.println("===== UPDATED KEY VALUE =====");
		System.out.println(updatedKeyValue.dumpFieldsAsKeyValueString());
		
		Assertions.assertNotNull(keyValuePair);
		Assertions.assertEquals(keyValuePair.key(), keynameUpdated);
		Assertions.assertEquals(keyValuePair.value(), "B");
		Assertions.assertEquals(keyValuePair.description(), "Testdescription2");
		
		//--------------------------------------
		// SELECT BY ID
		KeyValuePair keyByID = CFW.DB.KeyValuePairs.selectByID(updatedKeyValue.id());
		
		Assertions.assertNotNull(keyByID, "Key is selected by ID.");
		
		//--------------------------------------
		// Read from cache
		String cachedValue = CFW.DB.KeyValuePairs.getValueAsString(keynameUpdated);
		Assertions.assertEquals(cachedValue, "B", "Current value is read from cache.");
		
		//--------------------------------------
		// DELETE
		CFW.DB.KeyValuePairs.deleteByID(updatedKeyValue.id());
		Assertions.assertFalse(CFW.DB.KeyValuePairs.checkKeyExists(keyname));
				
	}
	
}

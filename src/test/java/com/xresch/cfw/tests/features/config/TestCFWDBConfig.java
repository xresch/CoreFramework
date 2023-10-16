package com.xresch.cfw.tests.features.config;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWDBConfig extends DBTestMaster {

	
	@BeforeAll
	public static void fillWithTestData() {
		
	}

	@Test
	public void testCRUDConfig() {
		
		String configCategory = "Test";
		String configName = "Test Config";
		String configNameUpdated = "Test ConfigUPDATED";
		
		//--------------------------------------
		// Cleanup
		Configuration configToDelete = CFW.DB.Config.selectBy(configCategory, configName);
		if(configToDelete != null) {
			CFW.DB.Config.deleteByID(configToDelete.id());
		}

		configToDelete = CFW.DB.Config.selectBy(configCategory, configNameUpdated);
		if(configToDelete != null) {
			CFW.DB.Config.deleteByID(configToDelete.id());
		}
		Assertions.assertFalse(CFW.DB.Config.checkConfigExists(configCategory,configName), "Config doesn't exists, checkConfigExists(String, String) works.");
		Assertions.assertFalse(CFW.DB.Config.checkConfigExists(configToDelete), "Config doesn't exist, checkConfigExists(Config) works.");
		
		
		//--------------------------------------
		// CREATE
		CFW.DB.Config.create(
				new Configuration(configCategory, configName)
				.description("Testdescription")
				.type(FormFieldType.TEXT)
				.options(new String[] {"A", "B", "C"})
				.value("A")
		);
		
		Assertions.assertTrue(CFW.DB.Config.checkConfigExists(configCategory, configName), "Config created successfully, checkConfigExists(String) works.");

		//--------------------------------------
		// SELECT BY NAME
		Configuration config = CFW.DB.Config.selectBy(configCategory, configName);
		
		System.out.println("===== CONFIG =====");
		System.out.println(config.dumpFieldsAsKeyValueString());

		Assertions.assertNotNull(config);
		Assertions.assertEquals(config.name(), configName);
		Assertions.assertEquals(config.description(), "Testdescription");
		Assertions.assertEquals(config.type(), "TEXT");
		
		//--------------------------------------
		// UPDATE
		config.name(configNameUpdated)
			.description("Testdescription2")
			.value("B")
			.options(new String[] {"A", "B", "C", "D"});
		
		CFW.DB.Config.update(config);
		
		//--------------------------------------
		// SELECT UPDATED CONFIG
		Configuration updatedConfig = CFW.DB.Config.selectBy(configCategory, configNameUpdated);
		
		System.out.println("===== UPDATED CONFIG =====");
		System.out.println(updatedConfig.dumpFieldsAsKeyValueString());
		
		Assertions.assertNotNull(config);
		Assertions.assertEquals(config.name(), configNameUpdated);
		Assertions.assertEquals(config.description(), "Testdescription2");
		
		//--------------------------------------
		// SELECT BY ID
		Configuration configByID = CFW.DB.Config.selectByID(updatedConfig.id());
		
		Assertions.assertNotNull(configByID, "Config is selected by ID.");
		
		//--------------------------------------
		// Read from cache
		String cachedValue = CFW.DB.Config.getConfigAsString(configCategory, configNameUpdated);
		Assertions.assertEquals(cachedValue, "B", "Current value is read from cache.");
		
		//--------------------------------------
		// DELETE
		CFW.DB.Config.deleteByID(updatedConfig.id());
		Assertions.assertFalse(CFW.DB.Config.checkConfigExists(configCategory, configName));
				
	}
	
}

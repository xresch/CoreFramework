package com.xresch.cfw.tests.utils;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.csv.CFWCSV;

public class TestsCFWJson {
	
	public static final String INTERNAL_RESOURCES_PATH = "com/pengtoolbox/cfw/resources";
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testHashMapFromJson() {
		
		LinkedHashMap<String,String> result = CFW.JSON.fromJsonLinkedHashMap("{"
						+"\"key\": \"label\","
						+"\"12\": \"object\","
						+"\"name\": \"value\""
				+"}");
		
		Assertions.assertEquals(result.size(), 3, "3 entries are found in the map.");
		Assertions.assertEquals(result.get("key"), "label", "value is mapped correctly.");
		Assertions.assertEquals(result.get("12"), "object", "value is mapped correctly.");
		Assertions.assertEquals(result.get("name"), "value", "value is mapped correctly.");
	}
	
}

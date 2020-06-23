package com.pengtoolbox.cfw.tests.various;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.pengtoolbox.cfw._main.CFW;

public class TestsCFWJson {
	
	public static final String INTERNAL_RESOURCES_PATH = "com/pengtoolbox/cfw/resources";
	
	@Test
	public void testHashMapFromJson() {
		
		LinkedHashMap<String,String> result = CFW.JSON.fromJsonLinkedHashMap("{"
						+"\"key\": \"label\","
						+"\"12\": \"object\","
						+"\"name\": \"value\""
				+"}");
		
		Assertions.assertTrue(result.size() == 3, "3 entries are found in the map.");
		Assertions.assertTrue(result.get("key").equals("label"), "value is mapped correctly.");
		Assertions.assertTrue(result.get("12").equals("object"), "value is mapped correctly.");
		Assertions.assertTrue(result.get("name").equals("value"), "value is mapped correctly.");
	}
	
}

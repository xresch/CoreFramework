package com.xresch.cfw.tests.utils;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

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
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testJsonFromCSV() {
		
		JsonObject object;
				
		JsonArray array = CFW.JSON.fromCSV("""
				category,entity,ATTributes,value
				gatling,callAPI,{},123
				gatling2,callURL,"{a: null, b: 1, c: true, d: \\"three\\" }",765
				"""
				, ","
				, true
				, true
			);
		
		Assertions.assertEquals(array.size(), 2, "2 entries are found in array.");
		
		//---------------------------------
		// 
		//---------------------------------
		object = array.get(0).getAsJsonObject(); 
		
		Assertions.assertEquals("gatling", object.get("category").getAsString());
		Assertions.assertEquals("callAPI", object.get("entity").getAsString());
		Assertions.assertEquals(true, object.get("attributes").isJsonObject());
		Assertions.assertEquals("123", object.get("value").getAsString());
		
		//---------------------------------
		// 
		//---------------------------------
		object = array.get(1).getAsJsonObject(); 
		
		Assertions.assertEquals("gatling2", object.get("category").getAsString());
		Assertions.assertEquals("callURL", object.get("entity").getAsString());
		Assertions.assertEquals(true, object.get("attributes").isJsonObject());
		Assertions.assertEquals("765", object.get("value").getAsString());

		Assertions.assertEquals(true, object.get("attributes").getAsJsonObject().get("a").isJsonNull());
		Assertions.assertEquals(1, object.get("attributes").getAsJsonObject().get("b").getAsInt());
		Assertions.assertEquals(true, object.get("attributes").getAsJsonObject().get("c").getAsBoolean());
		Assertions.assertEquals("three", object.get("attributes").getAsJsonObject().get("d").getAsString());
	}
	
}

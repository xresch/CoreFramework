package com.xresch.cfw.tests.utils;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.csv.CFWCSV;

public class TestCFWCSV {
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testSplitCSVQuotesAware() {
		
		ArrayList<String> splitted;
		int i ;
		//---------------------------------
		// Single Chars
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(",", "a,b,c");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals("b", splitted.get(++i));
		Assertions.assertEquals("c", splitted.get(++i));
		
		//---------------------------------
		// Blanks
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(",", " a ,\" b \", c ");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals(" b ", splitted.get(++i));
		Assertions.assertEquals("c", splitted.get(++i));
		
		//---------------------------------
		// Skipped Value
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware("#", "#a# #b##c#");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(7, splitted.size());
		Assertions.assertEquals(null, splitted.get(++i));
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals(null, splitted.get(++i));
		Assertions.assertEquals("b", splitted.get(++i));
		Assertions.assertEquals(null, splitted.get(++i));
		Assertions.assertEquals("c", splitted.get(++i));
		Assertions.assertEquals(null, splitted.get(++i));
		
		
		//---------------------------------
		// Multiple Chars
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(";", "abc;d;xyz;1234");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(4, splitted.size());
		Assertions.assertEquals("abc", splitted.get(++i));
		Assertions.assertEquals("d", splitted.get(++i));
		Assertions.assertEquals("xyz", splitted.get(++i));
		Assertions.assertEquals("1234", splitted.get(++i));
		
		//---------------------------------
		// With Quotes
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(";", """
				"abc";"d";"xyz";"1234"
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(4, splitted.size());
		Assertions.assertEquals("abc", splitted.get(++i));
		Assertions.assertEquals("d", splitted.get(++i));
		Assertions.assertEquals("xyz", splitted.get(++i));
		Assertions.assertEquals("1234", splitted.get(++i));
		
		//---------------------------------
		// Mixed Quotes and Multi-Char Separator
		//---------------------------------
		splitted = CFWCSV.splitCSVQuotesAware("---", """
				"abc"---d---"xyz"---"1234"---@@@
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(5, splitted.size());
		Assertions.assertEquals("abc", splitted.get(++i));
		Assertions.assertEquals("d", splitted.get(++i));
		Assertions.assertEquals("xyz", splitted.get(++i));
		Assertions.assertEquals("1234", splitted.get(++i));
		Assertions.assertEquals("@@@", splitted.get(++i));
		
		//---------------------------------
		// Mixed Quotes, Escaped Quotes and Multi-Char Separator
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware("---", """
				"\\"abc\\""---d---"xyz"---"12\\"34"---@@@
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(5, splitted.size());
		Assertions.assertEquals("\"abc\"", splitted.get(++i));
		Assertions.assertEquals("d", splitted.get(++i));
		Assertions.assertEquals("xyz", splitted.get(++i));
		Assertions.assertEquals("12\"34", splitted.get(++i));
		Assertions.assertEquals("@@@", splitted.get(++i));
		
		//---------------------------------
		// Two quotes at beginning
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(";", """
				;;1 ; 2 ; 3
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(5, splitted.size());
		Assertions.assertEquals(null, splitted.get(++i));
		Assertions.assertEquals(null, splitted.get(++i));
		Assertions.assertEquals("1", splitted.get(++i));
		Assertions.assertEquals("2", splitted.get(++i));
		Assertions.assertEquals("3", splitted.get(++i));
		
		//---------------------------------
		// Blanks between quotes
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(";", """
				"1" ; "2" ; "3"
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("1", splitted.get(++i));
		Assertions.assertEquals("2", splitted.get(++i));
		Assertions.assertEquals("3", splitted.get(++i));
		
		//---------------------------------
		// Empty Quotes
		//---------------------------------
		splitted = CFW.CSV.splitCSVQuotesAware(";", """
				""; "" ; 1 ;2; ""
				""");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(5, splitted.size());
		Assertions.assertEquals("", splitted.get(++i));
		Assertions.assertEquals("", splitted.get(++i));
		Assertions.assertEquals("1", splitted.get(++i));
		Assertions.assertEquals("2", splitted.get(++i));
		Assertions.assertEquals("", splitted.get(++i));
	
	}
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testJsonFromCSV() {
		
		JsonObject object;
				
		JsonArray array = CFW.CSV.toJsonArray("""
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

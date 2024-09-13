package com.xresch.cfw.tests.utils;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;

public class TestCFWUtilsText {
	

	@Test
	public void testSplitCSVQuotesAware() {
		
		ArrayList<String> splitted;
		int i ;
		//---------------------------------
		// Single Chars
		//---------------------------------
		splitted = CFW.Utils.Text.splitCSVQuotesAware(",", "a,b,c");
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals("b", splitted.get(++i));
		Assertions.assertEquals("c", splitted.get(++i));
		
		//---------------------------------
		// Multiple Chars
		//---------------------------------
		splitted = CFW.Utils.Text.splitCSVQuotesAware(";", "abc;d;xyz;1234");
		
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
		splitted = CFW.Utils.Text.splitCSVQuotesAware(";", """
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
		splitted = CFW.Utils.Text.splitCSVQuotesAware("---", """
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
		splitted = CFW.Utils.Text.splitCSVQuotesAware("---", """
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
	}
	
	
}

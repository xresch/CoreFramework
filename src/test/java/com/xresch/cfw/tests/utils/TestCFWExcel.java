package com.xresch.cfw.tests.utils;

import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.files.CFWFiles;

public class TestCFWExcel {
	
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testJsonFromExcel() {
				
		String packageName = "com.xresch.cfw.tests.utils.testdata".replaceAll("\\.", "/");
		String resourcePath = packageName + "/excel_read_test.xlsx";
		InputStream is = CFWFiles.class.getClassLoader().getResourceAsStream(resourcePath);
		
		JsonArray array = CFW.Excel.readExcelSheetAsJsonArray(is, null, false);
		
		Assertions.assertEquals(array.size(), 4, "4 rows read from excel");
		
		Assertions.assertEquals(CFW.JSON.toJSONPretty(array), 
				"""
[
  {
    "C": "FIRSTNAME",
    "D": "LASTNAME",
    "E": "LIKES_TIRAMISU",
    "F": "AGE",
    "G": "INDEX",
    "H": "NULL",
    "I": "FORMULA",
    "J": "ERROR"
  },
  {
    "C": "Aurora",
    "D": "Björnsson",
    "E": true,
    "F": 24,
    "G": 0,
    "H": null,
    "I": "0",
    "J": "#DIV/0!"
  },
  {
    "C": "Hera",
    "D": "Viklund",
    "E": false,
    "F": 33,
    "G": 1,
    "H": null,
    "I": "33",
    "J": "#DIV/0!"
  },
  {
    "C": "Freya",
    "D": "Scheiwillera",
    "E": true,
    "F": 42,
    "G": 2,
    "H": null,
    "I": "84",
    "J": "#DIV/0!"
  }
]""");
		
	}

	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testJsonFromExcel_FirstRowAsHeader() {
		
		boolean firstRowAsHeader = true;
		
		String packageName = "com.xresch.cfw.tests.utils.testdata".replaceAll("\\.", "/");
		String resourcePath = packageName + "/excel_read_test.xlsx";
		InputStream is = CFWFiles.class.getClassLoader().getResourceAsStream(resourcePath);
		
		JsonArray array = CFW.Excel.readExcelSheetAsJsonArray(is, null, firstRowAsHeader);
		
		Assertions.assertEquals(array.size(), 3, "3 rows read from excel");
		
		System.out.println(CFW.JSON.toJSONPretty(array));
		Assertions.assertEquals(CFW.JSON.toJSONPretty(array), 
				"""
[
  {
    "FIRSTNAME": "Aurora",
    "LASTNAME": "Björnsson",
    "LIKES_TIRAMISU": true,
    "AGE": 24,
    "INDEX": 0,
    "NULL": null,
    "FORMULA": "0",
    "ERROR": "#DIV/0!"
  },
  {
    "FIRSTNAME": "Hera",
    "LASTNAME": "Viklund",
    "LIKES_TIRAMISU": false,
    "AGE": 33,
    "INDEX": 1,
    "NULL": null,
    "FORMULA": "33",
    "ERROR": "#DIV/0!"
  },
  {
    "FIRSTNAME": "Freya",
    "LASTNAME": "Scheiwillera",
    "LIKES_TIRAMISU": true,
    "AGE": 42,
    "INDEX": 2,
    "NULL": null,
    "FORMULA": "84",
    "ERROR": "#DIV/0!"
  }
]""");
		
	}	
	
	
}

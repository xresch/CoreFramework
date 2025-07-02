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
		
		JsonArray array = CFW.Excel.readExcelSheetAsJsonArray(is, null);
		
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
				
//		//---------------------------------
//		// 
//		//---------------------------------
//		object = array.get(0).getAsJsonObject(); 
//		
//		Assertions.assertEquals("gatling", object.get("category").getAsString());
//		Assertions.assertEquals("callAPI", object.get("entity").getAsString());
//		Assertions.assertEquals(true, object.get("attributes").isJsonObject());
//		Assertions.assertEquals("123", object.get("value").getAsString());
		
	}
	
	
	
}

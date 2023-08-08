package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.Calendar;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.tests._master.DBTestMaster;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWQueryCommands extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static final String PACKAGE = "com.xresch.cfw.tests.features.query.testdata";
	
	private static long earliest = new Instant().minus(1000*60*30).getMillis();
	private static long latest = new Instant().getMillis();
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE);

		context.setEarliest(earliest);
		context.setLatest(latest);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSpecialCaseNewLineAfterNegativeNumber() throws IOException {
		
		String queryString = 
				"| source random type=numbers records=100 \r\n"
				+ "| filter \r\n"
				+ "		(null - 10)	== -10 	\r\n"
				+ "	AND (null + 10)	== 10";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		// No filtering occurs as both are true
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());

	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testChart() throws IOException {
		
		String queryString = "| source random records=10 type=series\r\n" + 
				"| chart \r\n" + 
				"	by=[WAREHOUSE, ITEM]\r\n" + 
				"	x=TIME \r\n" + 
				"	y=COUNT";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		// No filtering occurs as all filter are commented out
		CFWQueryResult queryResults = resultArray.get(0);
		JsonObject displaySettings = queryResults.getDisplaySettings();
		
		Assertions.assertTrue(displaySettings.has("as"));
		Assertions.assertEquals("chart", displaySettings.get("as").getAsString());
		
		Assertions.assertTrue(displaySettings.has("by"));
		Assertions.assertEquals("[\"WAREHOUSE\",\"ITEM\"]", displaySettings.get("by").toString());
		
		Assertions.assertTrue(displaySettings.has("x"));
		Assertions.assertEquals("TIME", displaySettings.get("x").getAsString());
		
		Assertions.assertTrue(displaySettings.has("y"));
		Assertions.assertEquals("COUNT", displaySettings.get("y").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testComments() throws IOException {
		
		String queryString = "| source random records=100\r\n"
				+ "			   # filter LIKES_TIRAMISU==null\r\n"
				+ "			| comment filter LIKES_TIRAMISU==true | tail 100 | off filter LIKES_TIRAMISU==false | top 100 #filter LIKES_TIRAMISU==true";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		// No filtering occurs as all filter are commented out
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCrates() throws IOException {
		
		int minuteRounding = 4;
		String queryString = "| source random records=1000\r\n" + 
				"| crates by=VALUE 		type=number		step=10		name=NUM\r\n" + 
				"| crates by=FIRSTNAME 	type=alpha 		step=3		name=ALPHA\r\n" + 
				"| crates by=TIME		type=time		step="+minuteRounding+"	name=TIMEROUNDED\r\n"  
			;
		
		
		//--------------------------------
		// Test type=number
		String queryStringNum = queryString+"| sort NUM";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringNum, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		Assertions.assertEquals("0 - 10", queryResults.getRecord(0).get("NUM").getAsString());
				
		//--------------------------------
		// Test type=alpha
		String queryStringAlpha = queryString+"| sort ALPHA";
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringAlpha, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		Assertions.assertEquals("A - C", queryResults.getRecord(0).get("ALPHA").getAsString());
		
		//--------------------------------
		// Test type=time
		String queryStringTime = queryString+"| sort TIMEROUNDED";
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringTime, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		for(int i = 0; i < 100 && i < queryResults.getRecordCount(); i++) {
			long roundedTime = queryResults.getRecord(0).get("TIMEROUNDED").getAsLong();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(roundedTime);
			int roundedMinutes = calendar.get(Calendar.MINUTE);
			int truncatedSeconds  = calendar.get(Calendar.SECOND);
			int truncatedMillis  = calendar.get(Calendar.MILLISECOND);
			Assertions.assertEquals(0, roundedMinutes % 4, "Minutes rounded to 4 minutes");
			Assertions.assertEquals(0, truncatedSeconds, "Seconds are truncated");
			Assertions.assertEquals(0, truncatedMillis, "Milliseconds are truncated");
		}
		
		//--------------------------------
		// Test multiplier=2
		String queryStringMultiplier = "| source random records=1000\r\n" + 
				"| crates by=VALUE 		type=number		step=10		multiplier=2\r\n" + 
				"| uniq CRATE | sort CRATE"  
			;
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringMultiplier, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(5, queryResults.getRecordCount());
		Assertions.assertEquals("0 - 10", queryResults.getRecord(0).get("CRATE").getAsString());
		Assertions.assertEquals("11 - 20", queryResults.getRecord(1).get("CRATE").getAsString());
		Assertions.assertEquals("21 - 40", queryResults.getRecord(2).get("CRATE").getAsString());
		Assertions.assertEquals("41 - 80", queryResults.getRecord(3).get("CRATE").getAsString());
		Assertions.assertEquals("81 - 160", queryResults.getRecord(4).get("CRATE").getAsString());
			
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testDisplayAs() throws IOException {
		
		//---------------------------------
		// Create and Execute Query
		String queryString = "	| source random records=1\r\n"
				+ "	| rename URL = url\r\n"
				+ "	| rename LAST_LOGIN = 'Last Login'\r\n"
				+ "	| display as=panels  \r\n"
				+ "	    visiblefields=[FIRSTNAME, LASTNAME, 'Last Login', url] \r\n"
				+ "		titlefields=[INDEX, FIRSTNAME, LASTNAME, LOCATION]\r\n"
				+ "		titleformat='{0}: {2} {1} (Location: {3})'"
				;
		
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());

//		===== EXPECTED RESULT ====
//      Resulting object 
//		displaySettings: {
//		    "as": "panels",
//		    "visiblefields": [
//		        "FIRSTNAME",
//		        "LASTNAME",
//		        "Last Login",
//		        "url"
//		    ],
//		    "titlefields": [
//		        "INDEX",
//		        "FIRSTNAME",
//		        "LASTNAME",
//		        "LOCATION"
//		    ],
//		    "titleformat": "{0}: {2} {1} (Location: {3})"
//		}

		JsonObject displaySettings = resultArray.get(0).getDisplaySettings();

		Assertions.assertEquals("panels", displaySettings.get("as").getAsString());
		Assertions.assertEquals("{0}: {2} {1} (Location: {3})", displaySettings.get("titleformat").getAsString());
		
		Assertions.assertEquals(4, displaySettings.get("visiblefields").getAsJsonArray().size());
		Assertions.assertEquals("FIRSTNAME", displaySettings.get("visiblefields").getAsJsonArray().get(0).getAsString());
		Assertions.assertEquals("LASTNAME", displaySettings.get("visiblefields").getAsJsonArray().get(1).getAsString());
		Assertions.assertEquals("Last Login", displaySettings.get("visiblefields").getAsJsonArray().get(2).getAsString());
		Assertions.assertEquals("url", displaySettings.get("visiblefields").getAsJsonArray().get(3).getAsString());
		
		Assertions.assertEquals(4, displaySettings.get("titlefields").getAsJsonArray().size());
		Assertions.assertEquals("INDEX", displaySettings.get("titlefields").getAsJsonArray().get(0).getAsString());
		Assertions.assertEquals("FIRSTNAME", displaySettings.get("titlefields").getAsJsonArray().get(1).getAsString());
		Assertions.assertEquals("LASTNAME", displaySettings.get("titlefields").getAsJsonArray().get(2).getAsString());
		Assertions.assertEquals("LOCATION", displaySettings.get("titlefields").getAsJsonArray().get(3).getAsString());
	}
	

	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testDistinct_Dedup_Uniq() throws IOException {
		
		//---------------------------------
		String queryString = "| source random records=1000 | distinct LIKES_TIRAMISU"
				+ ";| source random records=1000 | dedup LIKES_TIRAMISU"
				+ ";| source random records=1000 | uniq LIKES_TIRAMISU"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 3 results for distinct, dedup and uniq
		Assertions.assertEquals(3, resultArray.size());
		
		// Distinct by LIKES_TIRAMISU results in 3 rows with true, false and null
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		// Same for alias dedup
		queryResults = resultArray.get(1);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		// Same for alias uniq
		queryResults = resultArray.get(2);
		Assertions.assertEquals(3, queryResults.getRecordCount());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testExecute() throws IOException {
		
		//---------------------------------
		String queryString = "| globals multidisplay=2\r\n" + 
				"| source random records = 10\r\n" + 
				"| keep TIME\r\n" + 
				";\r\n" + 
				"# offset earliest/latest by one day, alternatively you can use earliestSet() or latestSet()\r\n" + 
				"| execute timeframeoffset(0,-1, \"d\") \r\n" + 
				"| source random records = 10\r\n" + 
				"| keep TIME"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 results for 
		Assertions.assertEquals(2, resultArray.size());
		
		//-----------------------------
		// result with offset
		CFWQueryResult queryResults = resultArray.get(0);
		long noOffsetMillis = queryResults.getQueryContext().getEarliestMillis();
		
		//-----------------------------
		// result with offset
		CFWQueryResult offsetQueryResults = resultArray.get(1);
		long offsetMillis = offsetQueryResults.getQueryContext().getEarliestMillis();
		
		float diffDays = CFWTimeUnit.d.difference(offsetMillis, noOffsetMillis);
		float diffRounded = Math.round(diffDays);

		//-----------------------------
		// Check result
		Assertions.assertEquals(1, diffRounded, "Offset should be one day.");
	
	}
	
	


	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFilterNullValues() throws IOException {
		
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFilterNullValues.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		// No filtering occurs as all filter conditions are true
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10000, queryResults.getRecordCount());
	
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFormatField_SingleField() throws IOException {
		
		//---------------------------------
		String queryString = "| source random | formatfield VALUE=[postfix,' $']"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 1 query results
		Assertions.assertEquals(1, resultArray.size());

//		===== EXPECTED RESULT ====		
//	    "fieldFormats": {
//	        "VALUE": [
//	            [
//	                "postfix",
//	                " $"
//	            ]
//	        ]
//	    }

		
		// 
		JsonObject displaySettings = resultArray.get(0).getDisplaySettings();
		JsonObject fieldFormats = displaySettings.get("fieldFormats").getAsJsonObject();
		
		Assertions.assertEquals(1, fieldFormats.get("VALUE").getAsJsonArray().size());
		Assertions.assertEquals(2, fieldFormats.get("VALUE").getAsJsonArray().get(0).getAsJsonArray().size());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFormatField_TwoFieldsMultipleFormats() throws IOException {
		
		//---------------------------------
		String queryString = "| source random \r\n"
				+ "| formatfield \r\n"
				+ "	INDEX=align,right  \r\n"
				+ "	VALUE=[prefix,'Mighty Balance: ']  VALUE=[postfix,' $']  VALUE=[threshold,0,10,20,30,40]  VALUE=uppercase"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 1 query results
		Assertions.assertEquals(1, resultArray.size());
		
//		===== EXPECTED RESULT ====
//		"fieldFormats": {
//		    "INDEX": [
//		      ["align","right"]
//		    ],
//		    "VALUE": [
//		      ["prefix", "Mighty Balance: "],
//		      [ "postfix"," $"],
//		      ["threshold",0,10,20,30,40,"bg"],
//		      ["uppercase"]
//		    ]
//		}


		
		// 
		JsonObject displaySettings = resultArray.get(0).getDisplaySettings();
		JsonObject fieldFormats = displaySettings.get("fieldFormats").getAsJsonObject();
		
		Assertions.assertEquals(1, fieldFormats.get("INDEX").getAsJsonArray().size());
		Assertions.assertEquals(2, fieldFormats.get("INDEX").getAsJsonArray().get(0).getAsJsonArray().size());
		
		Assertions.assertEquals(4, fieldFormats.get("VALUE").getAsJsonArray().size());
		Assertions.assertEquals(2, fieldFormats.get("VALUE").getAsJsonArray().get(0).getAsJsonArray().size());
		Assertions.assertEquals(2, fieldFormats.get("VALUE").getAsJsonArray().get(1).getAsJsonArray().size());
		Assertions.assertEquals(7, fieldFormats.get("VALUE").getAsJsonArray().get(2).getAsJsonArray().size());
		Assertions.assertEquals(1, fieldFormats.get("VALUE").getAsJsonArray().get(3).getAsJsonArray().size());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFormatField_ArrayFieldsArrayFormats() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random type=numbers\r\n"
				+ "| formatfield \r\n"
				+ "	[THOUSANDS,FLOAT,BIG_DECIMAL]=[\r\n"
				+ "			 [separators]\r\n"
				+ "			,['threshold', 0, 1000, 1000^2, 1000^3, 1000^4, 'text']\r\n"
				+ "		]"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 1 query results
		Assertions.assertEquals(1, resultArray.size());
		
		
//		===== EXPECTED RESULT ====
//		"fieldFormats": {
//			"THOUSANDS": [
//				["separators","'","3"],
//				["threshold",0,1000,1000000,1000000000,1000000000000,"text"]
//			],"FLOAT": [
//				["separators","'","3"],
//				["threshold",0,1000,1000000,1000000000,1000000000000,"text"]
//			],"BIG_DECIMAL": [
//				["separators","'","3"],
//				["threshold",0,1000,1000000,1000000000,1000000000000,"text"]
//			]
//		}

		JsonObject displaySettings = resultArray.get(0).getDisplaySettings();
		JsonObject fieldFormats = displaySettings.get("fieldFormats").getAsJsonObject();
		
		Assertions.assertEquals(2, fieldFormats.get("THOUSANDS").getAsJsonArray().size());
		Assertions.assertEquals(3, fieldFormats.get("THOUSANDS").getAsJsonArray().get(0).getAsJsonArray().size());
		Assertions.assertEquals(7, fieldFormats.get("THOUSANDS").getAsJsonArray().get(1).getAsJsonArray().size());
		//arithmetics are evaluated
		Assertions.assertEquals(1000000, 
				fieldFormats.get("THOUSANDS").getAsJsonArray().get(1)
											.getAsJsonArray().get(3).getAsInt()
			);
		
		
		Assertions.assertEquals(2, fieldFormats.get("FLOAT").getAsJsonArray().size());
		Assertions.assertEquals(3, fieldFormats.get("FLOAT").getAsJsonArray().get(0).getAsJsonArray().size());
		Assertions.assertEquals(7, fieldFormats.get("FLOAT").getAsJsonArray().get(1).getAsJsonArray().size());
		//arithmetics are evaluated
		Assertions.assertEquals(1000000000, 
				fieldFormats.get("FLOAT").getAsJsonArray().get(1)
											.getAsJsonArray().get(4).getAsInt()
			);
		
		Assertions.assertEquals(2, fieldFormats.get("BIG_DECIMAL").getAsJsonArray().size());
		Assertions.assertEquals(3, fieldFormats.get("BIG_DECIMAL").getAsJsonArray().get(0).getAsJsonArray().size());
		Assertions.assertEquals(7, fieldFormats.get("BIG_DECIMAL").getAsJsonArray().get(1).getAsJsonArray().size());
		//arithmetics are evaluated
		Assertions.assertEquals(1000000000000l, 
				fieldFormats.get("BIG_DECIMAL").getAsJsonArray().get(1)
											.getAsJsonArray().get(5).getAsLong()
			);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFormatrecord() throws IOException {
		
		//---------------------------------
		String queryString = "| source random records=100\r\n" + 
				"| formatrecord \r\n" + 
				"	[(FIRSTNAME ~='^He'), \"#332288\"] \r\n" + 
				"	[(VALUE >= 60), \"red\", \"yellow\"] \r\n" + 
				"	[(LIKES_TIRAMISU==true), \"green\"] \r\n" + 
				"	[true, \"cfw-gray\"]\r\n" + 
				"| formatfield LIKES_TIRAMISU=none"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 3 results for distinct, dedup and uniq
		Assertions.assertEquals(1, resultArray.size());
		
//		ALL RECORDS SHOULD CONTAIN FOLLOWING FIELDS
//		{
//		     ...
//			 "_bgcolor": "red",
//			 "_textcolor": "yellow"
//		},
		
		//-----------------------------------
		// Iterate Results Check has correct format
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		for(int i = 0; i < 100 && i < queryResults.getRecordCount(); i++) {
			JsonObject current = queryResults.getRecord(i);
			
			if(!current.get("FIRSTNAME").isJsonNull()
			&& current.get("FIRSTNAME").getAsString().startsWith("He")) {
				Assertions.assertEquals("#332288", current.get("_bgcolor").getAsString(), "Record has field _bgcolor=#332288");
			}else if(current.get("VALUE").getAsInt() >= 60) {
				Assertions.assertEquals("red", current.get("_bgcolor").getAsString(), "Record has field _bgcolor=#332288");
				Assertions.assertEquals("yellow", current.get("_textcolor").getAsString(), "Record has field _bgcolor=red and _textcolor=yellow");
			}else if(!current.get("LIKES_TIRAMISU").isJsonNull() 
					&& current.get("LIKES_TIRAMISU").getAsBoolean()) {
				Assertions.assertEquals("green", current.get("_bgcolor").getAsString(), "Record has field _bgcolor=green");
			}else {
				Assertions.assertEquals("cfw-gray", current.get("_bgcolor").getAsString(), "Record has field _bgcolor=cfw-gray");
			}
			
		}
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testGlobals() throws IOException {
			
		//---------------------------------
		String queryString = 
				  "| globals multidisplay=4 myCustomProperty=MyCustomValue\r\n"
				+ "| metadata name='Default Table' firstQueryProp='hello' | source random type=default records=5 | display as=table \r\n"
				+ ";\r\n"
				+ "| metadata name='Bigger Number Table' secondQueryProp='world' | source random type=numbers records=5 | display as=biggertable "
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 query results
		Assertions.assertEquals(2, resultArray.size());

//		===== EXPECTED IN BOTH RESULTS ====		
//		globals: {
//		  "earliest": 1640365794703,
//		  "latest": 1645722594704,
//		  "myCustomProperty": "MyCustomValue"
//		}
//		metadata: {
//		    "name": "Default Table"
//		}

		//------------------------------
		// Check First Query Result
		JsonObject globals  = resultArray.get(0).getGlobals();

		//Assertions.assertTrue(globals.get("earliest").isJsonPrimitive());
		//Assertions.assertTrue(globals.get("latest").isJsonPrimitive());
		Assertions.assertEquals("MyCustomValue", globals.get("myCustomProperty").getAsString());
		
		//------------------------------
		// Check Second Query Result
		globals  =  resultArray.get(1).getGlobals();

		//Assertions.assertTrue(globals.get("earliest").isJsonPrimitive());
		//Assertions.assertTrue(globals.get("latest").isJsonPrimitive());
		Assertions.assertEquals("MyCustomValue", globals.get("myCustomProperty").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testKeep() throws IOException {
			
		//---------------------------------
		String queryString = 
				  "| source random records=1\r\n" + 
				  "| keep INDEX, TIME, FIRSTNAME"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecord(0);
		
		Assertions.assertEquals(3, result.size(), "Record has only tree fields");
		Assertions.assertTrue(result.has("INDEX"), "Record has field INDEX");
		Assertions.assertTrue(result.has("TIME"), "Record has field TIME");
		Assertions.assertTrue(result.has("FIRSTNAME"), "Record has field FIRSTNAME");

	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMetadata() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| globals multidisplay=4 myCustomProperty=MyCustomValue\r\n"
				+ "| metadata name='Default Table' firstQueryProp='hello' | source random type=default records=5 | display as=table \r\n"
				+ ";\r\n"
				+ "| metadata name='Bigger Number Table' secondQueryProp='world' | source random type=numbers records=5 | display as=biggertable "
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 query results
		Assertions.assertEquals(2, resultArray.size());

//		===== EXPECTED IN BOTH RESULTS ====		
//		globals: {
//		  "earliest": 1640365794703,
//		  "latest": 1645722594704,
//		  "myCustomProperty": "MyCustomValue"
//		}
//		metadata: {
//		    "name": "Default Table"
//		}

		//------------------------------
		// Check First Query Result
		JsonObject metadata = resultArray.get(0).getMetadata();
		
		Assertions.assertEquals("Default Table", metadata.get("name").getAsString());
		Assertions.assertEquals("hello", metadata.get("firstQueryProp").getAsString());
		
		//------------------------------
		// Check Second Query Result
		metadata = resultArray.get(1).getMetadata();
		
		Assertions.assertEquals("Bigger Number Table", metadata.get("name").getAsString());
		Assertions.assertEquals("world", metadata.get("secondQueryProp").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| globals multidisplay=3 # display 3 results in one row\r\n" + 
				  "| metadata name=\"mimicThis\"\r\n" + 
				  "| source random records = 1\r\n" + 
				  "; # End of First Query\r\n" + 
				  "| source random records = 2\r\n" + 
				  "; # End of Second Query\r\n" + 
				  "| source random records = 4\r\n" + 
				  "| mimic name=\"mimicThis\" # copies and executes the first query named 'mimicThis'\r\n" + 
				  "| source random records = 8"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(3, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(2);
		Assertions.assertEquals(13, queryResults.getRecordCount(), "Has 13 records from records = 1 + 4 + 8 = 13");
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testNullTo() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=10 type=various\r\n" + 
				  "| set UUID=null \r\n" + 
				  "| nullto value=\"n/a\" fields=[ALWAYS_NULL]"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecord(0);
		
		Assertions.assertTrue(firstRecord.get("UUID").isJsonNull(), "Field UUID is null");
		Assertions.assertEquals("n/a", firstRecord.get("ALWAYS_NULL").getAsString(), "Field ALWAYS_NULL is n/a");

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRemove() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=1\r\n" + 
				  "| remove INDEX, TIME, FIRSTNAME"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecord(0);
		
		Assertions.assertEquals(8, result.size(), "Record has only tree fields");
		Assertions.assertFalse(result.has("INDEX"), "Record has field INDEX");
		Assertions.assertFalse(result.has("TIME"), "Record has field TIME");
		Assertions.assertFalse(result.has("FIRSTNAME"), "Record has field FIRSTNAME");

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRename() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=1\r\n" + 
				  "| rename INDEX=ROW"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecord(0);
		
		Assertions.assertEquals(11, result.size(), "Record has only tree fields");
		Assertions.assertFalse(result.has("INDEX"), "Record has field INDEX");
		Assertions.assertTrue(result.has("ROW"), "Record has field TIME");

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultCompare() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=1000 	\r\n" + 
				  "| filter FIRSTNAME == \"Aurora\"		\r\n" + 
				  "| keep FIRSTNAME, VALUE		\r\n" + 
				  "| stats	by=FIRSTNAME	COUNT=count(VALUE)		VALUE=avg(VALUE)\r\n" + 
				  "; \r\n" + 
				  "| mimic\r\n" + 
				  "; \r\n" + 
				  "| resultcompare by=FIRSTNAME"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecord(0);
		
		Assertions.assertTrue(firstRecord.has("FIRSTNAME"));
		Assertions.assertTrue(firstRecord.has("COUNT_A"));
		Assertions.assertTrue(firstRecord.has("COUNT_B"));
		Assertions.assertTrue(firstRecord.has("COUNT_Diff"));
		Assertions.assertTrue(firstRecord.has("COUNT_%"));
		Assertions.assertTrue(firstRecord.has("VALUE_A"));
		Assertions.assertTrue(firstRecord.has("VALUE_B"));
		Assertions.assertTrue(firstRecord.has("VALUE_Diff"));
		Assertions.assertTrue(firstRecord.has("VALUE_%"));
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultConcat() throws IOException {
		
		//---------------------------------
		String queryString = 
				"| metadata name = \"Result One\"\r\n" + 
				"| source random records=10\r\n" + 
				";\r\n" + 
				"| metadata name = \"Result Two\"\r\n" + 
				"| source random records=20\r\n" + 
				";\r\n" + 
				"| metadata name = \"Result Three\"\r\n" + 
				"| source random records=40\r\n" + 
				";\r\n" + 
				"| metadata name = \"Concatenated 1&3\"\r\n" + 
				"| resultconcat \"Result One\", \"Result Three\""
						;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(2, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult firstQueryResults = resultArray.get(0);
		Assertions.assertEquals("Result Two", firstQueryResults.getMetadata("name").getAsString(), "Name of first result is 'Result Two', as 'Result One' was merged with 'Result Three'.");
		Assertions.assertEquals(20, firstQueryResults.getRecordCount());
		
		//------------------------------
		// Check Second Query Result
		CFWQueryResult secondQueryResults = resultArray.get(1);
		Assertions.assertEquals("Concatenated 1&3", secondQueryResults.getMetadata("name").getAsString(), "Name of second result is 'Concatenated 1&3'.");
		Assertions.assertEquals(50, secondQueryResults.getRecordCount());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultCopy() throws IOException {
		
		//---------------------------------
		String queryString = 
				"| globals multidisplay=2\r\n" + 
				"| metadata name = \"Original\" \r\n" + 
				"| source random records=4\r\n" + 
				"| keep FIRSTNAME, VALUE\r\n" + 
				"| display menu=false \r\n" + 
				";\r\n" + 
				"| metadata name = \"Copy\"\r\n" + 
				"| resultcopy  #copy data of all previous queries\r\n" + 
				"| display as=panels menu=false"
						;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(2, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult firstQueryResults = resultArray.get(0);
		Assertions.assertEquals("Original", firstQueryResults.getMetadata("name").getAsString(), "Name of first result is 'Result Two', as 'Result One' was merged with 'Result Three'.");
		Assertions.assertEquals(4, firstQueryResults.getRecordCount());
		
		//------------------------------
		// Check Second Query Result
		CFWQueryResult secondQueryResults = resultArray.get(1);
		Assertions.assertEquals("Copy", secondQueryResults.getMetadata("name").getAsString(), "Name of second result is 'Concatenated 1&3'.");
		Assertions.assertEquals(4, secondQueryResults.getRecordCount());
		
		//------------------------------
		// Compare
		JsonObject firstObject = firstQueryResults.getRecord(0);
		JsonObject secondObject = secondQueryResults.getRecord(0);
		Assertions.assertEquals(
				firstObject.get("FIRSTNAME").getAsString(), 
				secondObject.get("FIRSTNAME").getAsString(),
				"FIRSTNAME is equal"
			);
		Assertions.assertEquals(
				firstObject.get("VALUE").getAsBoolean(), 
				secondObject.get("VALUE").getAsBoolean(),
				"VALUE is equal"
			);
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSet() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=10 type=series\r\n" + 
				  "| keep WAREHOUSE, ITEM, COUNT\r\n" + 
				  "| set \r\n" + 
				  "		BOOL=true\r\n" + 
				  "		NUMBER=123\r\n" + 
				  "		STRING=\"AAAHHH!!!\"\r\n" + 
				  "		OBJECT={a: \"1\", b: \"2\"}\r\n" + 
				  "		ARRAY=[1, true, \"three\"]\r\n" + 
				  "		NULL=null\r\n" + 
				  "		FIELDVAL=COUNT\r\n" + 
				  "		EXPRESSION=(FIELDVAL*FIELDVAL)\r\n" + 
				  "		FUNCTION=count()"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(30, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecord(0);
		
		//------------------------------
		// Check fields are set
		int FIELDVAL = firstRecord.get("FIELDVAL").getAsInt();
		Assertions.assertEquals(true, firstRecord.get("BOOL").getAsBoolean(), "BOOL is present");
		Assertions.assertEquals(123, firstRecord.get("NUMBER").getAsInt(), "NUMBER is present");
		Assertions.assertEquals("AAAHHH!!!", firstRecord.get("STRING").getAsString(), "STRING is present");
		Assertions.assertEquals("{\"a\":\"1\",\"b\":\"2\"}", CFW.JSON.toJSON(firstRecord.get("OBJECT")) , "OBJECT is present");
		Assertions.assertEquals("[1,true,\"three\"]", CFW.JSON.toJSON(firstRecord.get("ARRAY")) , "ARRAY is present");
		Assertions.assertEquals(true, firstRecord.get("NULL").isJsonNull() , "NULL is present");
		Assertions.assertEquals(firstRecord.get("COUNT").getAsInt(), FIELDVAL , "FIELDVAL is present and equals COUNT");
		Assertions.assertEquals( (FIELDVAL * FIELDVAL) , firstRecord.get("EXPRESSION").getAsInt() , "EXPRESSION is present");
		Assertions.assertEquals( 0 , firstRecord.get("FUNCTION").getAsInt() , "FUNCTION is present and count() returned 0.");
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSort() throws IOException {
		
		//---------------------------------
		String queryString = 
				"| source json data ='[\r\n" + 
				"	{val: 4444} ,{val: 22} ,{val: 333} ,{val: 1}\r\n" + 
				"]'\r\n" + 
				"| sort val"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(4, queryResults.getRecordCount());
		
		int i=-1;
		Assertions.assertEquals(1, queryResults.getRecord(++i).get("val").getAsInt());
		Assertions.assertEquals(22, queryResults.getRecord(++i).get("val").getAsInt());
		Assertions.assertEquals(333, queryResults.getRecord(++i).get("val").getAsInt());
		Assertions.assertEquals(4444, queryResults.getRecord(++i).get("val").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void test_Tail() throws IOException {
		
		//---------------------------------
		String queryString = "| source random records=1000 | tail 123"
				+ ";| source random records=1000 | tail 321"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 results for last and tail
		Assertions.assertEquals(2, resultArray.size());
		
		// last returns 123 results
		CFWQueryResult queryResults = resultArray.get(0);
		JsonArray queryResultsArray = queryResults.getRecords();
		Assertions.assertEquals(123, queryResultsArray.size());
		Assertions.assertEquals(999-122, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(999, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
		// tail returns 321 results 
		queryResults = resultArray.get(1);
		queryResultsArray = queryResults.getRecords();
		Assertions.assertEquals(321, queryResultsArray.size());
		Assertions.assertEquals(999-320, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(999, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void test_Top() throws IOException {
		
		//---------------------------------
		String queryString = "| source random records=1000 | top 123"
				+ ";| source random records=1000 | top 321"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 results for first and top
		Assertions.assertEquals(2, resultArray.size());
		
		// First returns 123 results
		CFWQueryResult queryResults = resultArray.get(0);
		JsonArray queryResultsArray = queryResults.getRecords();
		Assertions.assertEquals(123, queryResultsArray.size());
		Assertions.assertEquals(0, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(122, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
		// top returns 321 results
		queryResults = resultArray.get(1);
		queryResultsArray = queryResults.getRecords();
		Assertions.assertEquals(321, queryResultsArray.size());
		Assertions.assertEquals(0, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(320, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
	}
		
	
	
}

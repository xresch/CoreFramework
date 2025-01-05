package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.math.BigDecimal;
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
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.tests._master.DBTestMaster;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWQueryCommands extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static final String PACKAGE_COMMANDS = "com.xresch.cfw.tests.features.query.testdata.commands";
	
	private static long earliest = new Instant().minus(1000*60*30).getMillis();
	private static long latest = new Instant().getMillis();
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE_COMMANDS);

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
	public void testAggregate() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[FIRSTNAME, VALUE]
	["Aurora", 11]
	["Aurora", 22]
	["Aurora", 33]
	["Hera", 77]
	["Hera", 66]
	["Hera", 55]
	["Hera", 44]
| aggregate 
	by=[FIRSTNAME]      
	count() 			# Aurora: 3  / Hera: 4
	SUM=sum(VALUE)		# Aurora: 66  / Hera: 242
	MIN=min(VALUE)		# Aurora: 11  / Hera: 44
	MAX=max(VALUE)		# Aurora: 33  / Hera: 77
	FIRST=first(VALUE) 	# Aurora: 11  / Hera: 77
	LAST=last(VALUE)	# Aurora: 33  / Hera: 44  
| sort FIRSTNAME
			""";

		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(7, queryResults.getRecordCount());
		
		// Check Aurora Records
		for(int i = 0; i < 3; i++) {
			JsonObject record = queryResults.getRecordAsObject(i);
					
			Assertions.assertEquals("Aurora", record.get("FIRSTNAME").getAsString());
			Assertions.assertEquals(3, record.get("count()").getAsInt());
			Assertions.assertEquals(66, record.get("SUM").getAsInt());
			Assertions.assertEquals(11, record.get("MIN").getAsInt());
			Assertions.assertEquals(33, record.get("MAX").getAsInt());
			Assertions.assertEquals(11, record.get("FIRST").getAsInt());
			Assertions.assertEquals(33, record.get("LAST").getAsInt());
		}
		
		
		// Check Aurora Records
		for(int i = 3; i < 7; i++) {
			JsonObject record = queryResults.getRecordAsObject(i);
					
			Assertions.assertEquals("Hera", record.get("FIRSTNAME").getAsString());
			Assertions.assertEquals(4, record.get("count()").getAsInt());
			Assertions.assertEquals(242, record.get("SUM").getAsInt());
			Assertions.assertEquals(44, record.get("MIN").getAsInt());
			Assertions.assertEquals(77, record.get("MAX").getAsInt());
			Assertions.assertEquals(77, record.get("FIRST").getAsInt());
			Assertions.assertEquals(44, record.get("LAST").getAsInt());
		}
				
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testAggregate_NoGrouping() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[FIRSTNAME, VALUE]
	["Aurora", 11]
	["Aurora", 22]
	["Aurora", 33]
	["Hera", 77]
	["Hera", 66]
	["Hera", 55]
	["Hera", 44]
| aggregate     
	count() 			# 7
	SUM=sum(VALUE)		# 308
	MIN=min(VALUE)		# 11
	MAX=max(VALUE)		# 77
	FIRST=first(VALUE) 	# 11
	LAST=last(VALUE)	# 44
| sort FIRSTNAME
				""";
				
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(7, queryResults.getRecordCount());
		
		// Check All Records
		for(int i = 0; i < 7; i++) {
			JsonObject record = queryResults.getRecordAsObject(i);
					
			Assertions.assertEquals(7, record.get("count()").getAsInt());
			Assertions.assertEquals(308, record.get("SUM").getAsInt());
			Assertions.assertEquals(11, record.get("MIN").getAsInt());
			Assertions.assertEquals(77, record.get("MAX").getAsInt());
			Assertions.assertEquals(11, record.get("FIRST").getAsInt());
			Assertions.assertEquals(44, record.get("LAST").getAsInt());
		}
			
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testChart() throws IOException {
		
		String queryString = """
| source random records=10 type=series
| chart
	by=[WAREHOUSE, ITEM]
	x=TIME 
	y=COUNT
				""";
		
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
		
		String queryString = """
| source random records=100
# filter LIKES_TIRAMISU==null
| comment filter LIKES_TIRAMISU==true | tail 100 | off filter LIKES_TIRAMISU==false | top 100 #filter LIKES_TIRAMISU==true"
			""";
		
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
		
		String queryString = """
| source random records=1000
| crates by=VALUE 		type=number		step=10		name=NUM
| crates by=FIRSTNAME 	type=alpha 		step=3		name=ALPHA
| crates by=TIME		type=time		step=4	name=TIMEROUNDED
				""";
		
		
		//--------------------------------
		// Test type=number
		String queryStringNum = queryString+"| sort NUM";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringNum, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		Assertions.assertEquals("0 - 10", queryResults.getRecordAsObject(0).get("NUM").getAsString());
				
		//--------------------------------
		// Test type=alpha
		String queryStringAlpha = queryString+"| sort ALPHA";
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringAlpha, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		Assertions.assertEquals("A - C", queryResults.getRecordAsObject(0).get("ALPHA").getAsString());
		
		//--------------------------------
		// Test type=time
		String queryStringTime = queryString+"| sort TIMEROUNDED";
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringTime, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(1000, queryResults.getRecordCount());
		for(int i = 0; i < 100 && i < queryResults.getRecordCount(); i++) {
			long roundedTime = queryResults.getRecordAsObject(0).get("TIMEROUNDED").getAsLong();
			
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
		String queryStringMultiplier = """
| source random records=1000
| crates 
	by=VALUE 		
	type=number		
	step=10		
	multiplier=2
| uniq CRATE | sort CRATE 
				""";
		
		resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryStringMultiplier, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		queryResults = resultArray.get(0);
		Assertions.assertEquals(5, queryResults.getRecordCount());
		Assertions.assertEquals("0 - 10", queryResults.getRecordAsObject(0).get("CRATE").getAsString());
		Assertions.assertEquals("11 - 20", queryResults.getRecordAsObject(1).get("CRATE").getAsString());
		Assertions.assertEquals("21 - 40", queryResults.getRecordAsObject(2).get("CRATE").getAsString());
		Assertions.assertEquals("41 - 80", queryResults.getRecordAsObject(3).get("CRATE").getAsString());
		Assertions.assertEquals("81 - 160", queryResults.getRecordAsObject(4).get("CRATE").getAsString());
			
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testDisplayAs() throws IOException {
		
		//---------------------------------
		// Create and Execute Query
		String queryString = """
| source random records=1
| rename URL = url
| rename LAST_LOGIN = 'Last Login'
| display as=panels  
	visiblefields=[FIRSTNAME, LASTNAME, 'Last Login', url] 
	titlefields=[INDEX, FIRSTNAME, LASTNAME, LOCATION]
	titleformat='{0}: {2} {1} (Location: {3})'
				""";
		
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
		String queryString = """
 | source random records=1000 | distinct LIKES_TIRAMISU
 ;
 | source random records=1000 | dedup LIKES_TIRAMISU
 ;
 | source random records=1000 | uniq LIKES_TIRAMISU
				 """
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
		String queryString ="""
| globals multidisplay=2
| source random records = 10
| keep TIME
;
# offset earliest/latest by one day, alternatively you can use earliestSet() or latestSet()
| execute timeframeoffset(0,-1, "d") 
| source random records = 10
| keep TIME
				"""
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
		
		String queryString = """
| source random type=numbers records=10000 limit=10000
| filter 
	#in equal and not equal comparison, null is handled as null and only equal to itself
	null == null
	AND (null != null) == false 
	AND (null != "anythingElse") == true
	AND (null != false) == true
	AND (null != 123) == true
	# Division by null or zero will evaluate to null
	AND (100 / 0) 	== null
	AND (1 / null) 	== null
	AND (0 / 0) 	== null
	# in arithmetic expressions, null will be converted to zero
	AND (null / 1) 	== 0
	AND (0 / 100) 	== 0
	AND (null * 10)	== 0
	AND (10 * null)	== 0
	AND (null - 10)	== -10
	AND (10 - null)	== 10	
	AND (null + 10)	== 10
	AND (10 + null)	== 10
	# for lower/greater than comparison, null will be converted to zero
	AND (null < 1)	== true
	AND (null <= 1)	== true
	AND (1 > null)	== true
	AND (1 >= null)	== true
	# results of regex operator is always false if either side is null 
	AND ("myString" ~= null) == false
	AND (null ~= ".*") == false
	AND (null ~= null) == false
	# results of AND/OR with either side null 
	AND (null AND true) == false
	AND (null OR true) == true 
				""";
				
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
		String queryString = """
| source random 
| formatfield 
	INDEX=align,right 
	VALUE=[prefix,'Mighty Balance: ']  VALUE=[postfix,' $']  VALUE=[threshold,0,10,20,30,40]  VALUE=uppercase
				""";
		
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
		String queryString = """
| source random type=numbers
| formatfield 
	[THOUSANDS,FLOAT,BIG_DECIMAL]=[
			 [separators]
			,['threshold', 0, 1000, 1000^2, 1000^3, 1000^4, 'text']
		]
				""";
		
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
	public void testFormatLink() throws IOException {
		
		//---------------------------------
		String queryString = """
				| record
					[NAME, URL, ID]
					["Aurora", "http://example.aurora.com", 42]
				| formatlink
					newtab=false
					style="color: white;"
					NAME = URL
					ID = "http://www.acmeprofile.com/profile?id="+42
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 1 query results
		Assertions.assertEquals(1, resultArray.size());

		//-----------------------------------
		// Assert Result
		CFWQueryResult result = resultArray.get(0);
		Assertions.assertEquals(1, result.getRecordCount());
		
		//-----------------------------------
		// Assert fieldformats
		JsonObject displaySettings = result.getDisplaySettings();
		JsonObject fieldFormats = displaySettings.get("fieldFormats").getAsJsonObject();
		
		Assertions.assertEquals(1, fieldFormats.get("NAME").getAsJsonArray().size());
		Assertions.assertEquals(1, fieldFormats.get("ID").getAsJsonArray().size());
		
		//-----------------------------------
		// Assert record
		JsonObject object = result.getRecordAsObject(0);
		
		Assertions.assertEquals(""" 
				{"format":"link","label":"Aurora","url":"http://example.aurora.com","newtab":false,"style":"color: white;"}""", object.get("NAME").toString() );
		Assertions.assertEquals("""
				{"format":"link","label":"42","url":"http://www.acmeprofile.com/profile?id=42","newtab":false,"style":"color: white;"}""", object.get("ID").toString() );
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFormatrecord() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=100
| formatrecord 
	[(FIRSTNAME ~='^He'), \"#332288\"] 
	[(VALUE >= 60), \"red\", \"yellow\"] 
	[(LIKES_TIRAMISU==true), \"green\"] 
	[true, \"cfw-gray\"]
| formatfield LIKES_TIRAMISU=none
				""";
		
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
			JsonObject current = queryResults.getRecordAsObject(i);
			
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
		String queryString = """
				| globals multidisplay=4 myCustomProperty=MyCustomValue
				| metadata name='Default Table' firstQueryProp='hello' | source random type=default records=5 | display as=table
				;
				| metadata name='Bigger Number Table' secondQueryProp='world' | source random type=numbers records=5 | display as=table
				""";
		
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
		String queryString = """
				  | source random records=1
				  | keep INDEX, TIME, FIRSTNAME
				  """
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecordAsObject(0);
		
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
		String queryString = """
				  | globals multidisplay=4 myCustomProperty=MyCustomValue
				| metadata name='Default Table' firstQueryProp='hello' | source random type=default records=5 | display as=table
				;
				| metadata name='Bigger Number Table' secondQueryProp='world' | source random type=numbers records=5 | display as=table
				""";
		
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
		String queryString = """
| globals multidisplay=3 # display 3 results in one row
| metadata name="mimicThis"
| source random records = 1
; 

| source random records = 2
; 

| source random records = 4
| mimic name="mimicThis" # copies and executes the first query named 'mimicThis'
| source random records = 8
				"""; 
				
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
	public void testMimic_CallingMimic() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[NAME, VALUE]
	["Raurova", 42]
; 
| mimic 
| record
	[NAME, VALUE]
	["Rakesh", 99]
; 
| mimic 
# test removing all mimic commands, Raurova will not be in last result:
#  NAME    	VALUE
#  Rakesh	99				
				"""; 
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(3, resultArray.size());
		
		//------------------------------
		// Check Third Result
		CFWQueryResult queryResults = resultArray.get(2);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
			//------------------------------
			// Second Record
			JsonObject record = queryResults.getRecordAsObject(0);
			
			Assertions.assertEquals("Rakesh", record.get("NAME").getAsString());
			Assertions.assertEquals(99, record.get("VALUE").getAsInt());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic_DataBeforeMimic() throws IOException {
		
		//---------------------------------
		String queryString = """
| metadata
	name="format"  
	template=true # mark this query as a template
| formatfield
	VALUE = ['threshold', 44, 55, 66, 77, 88, "bg"]
| sort NAME
; 

#############################################
| record
	[NAME, VALUE]
	["Raurova", 44]
	["Rakesh", 77]
| mimic name="format" # insert formater
; 
#  test if data before mimic is still going through pipeline when no record manipulation command is specified
#  NAME    	VALUE
#  Rakesh	77
#  Raurova  44				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check Third Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
			//------------------------------
			// First Record
			JsonObject record = queryResults.getRecordAsObject(0);
				
			Assertions.assertEquals("Rakesh", record.get("NAME").getAsString());
			Assertions.assertEquals(77, record.get("VALUE").getAsInt());
			
			//------------------------------
			// Second Record
			record = queryResults.getRecordAsObject(1);
			
			Assertions.assertEquals("Raurova", record.get("NAME").getAsString());
			Assertions.assertEquals(44, record.get("VALUE").getAsInt());

	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic_MultipleCallsAndTemplate() throws IOException {
		
		//---------------------------------
		String queryString = """
| meta name="One" template=true
| record
	[NAME, VALUE]
	["Raurova", 44]
; 
| meta name="Two"  template=true
| record
	[NAME, VALUE]
	["Rakesh", 8008]
; 
| mimic name="Two"
| mimic name="One"
#  test multiple mimics and template mechanism:
#  NAME    	VALUE
#  Rakesh	8008
#  Raurova  44				
				 """;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check Third Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
			//------------------------------
			// First Record
			JsonObject record = queryResults.getRecordAsObject(0);
				
			Assertions.assertEquals("Rakesh", record.get("NAME").getAsString());
			Assertions.assertEquals(8008, record.get("VALUE").getAsInt());
			
			//------------------------------
			// Second Record
			record = queryResults.getRecordAsObject(1);
			
			Assertions.assertEquals("Raurova", record.get("NAME").getAsString());
			Assertions.assertEquals(44, record.get("VALUE").getAsInt());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic_RemoveLastCommand() throws IOException {
		
		//---------------------------------
		String queryString = """
| metadata 
	name="TEST_QUERY"
	firstname = "Hana"
	value = 42
| record
	[NAME, VALUE]
	[meta(firstname), meta(value)]
	["Rakesh", 88]

| filter NAME == "Hana"
; 


| source random records = 10
; 


| mimic name="TEST_QUERY" remove=["filter"]
# test removing last command in query(keeps Rakesh), & keep metadata(keeps Hana), result:
#  NAME    	VALUE
#  Hana		42
#  Rakesh	88
				"""; 
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(3, resultArray.size());
		
		//------------------------------
		// Check Third Result
		CFWQueryResult queryResults = resultArray.get(2);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
		//------------------------------
		// First Record
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals("Hana", record.get("NAME").getAsString());
		Assertions.assertEquals(42, record.get("VALUE").getAsInt());
		
		//------------------------------
		// Second Record
		record = queryResults.getRecordAsObject(1);
		
		Assertions.assertEquals("Rakesh", record.get("NAME").getAsString());
		Assertions.assertEquals(88, record.get("VALUE").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic_RemoveFirstCommand() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records = 10
| metadata 
	name="TEST_QUERY"
	firstname = "Hana"
	value = 42
| record
	[NAME, VALUE]
	[meta(firstname), meta(value)]
	["Rakesh", 88]

| filter NAME == "Hana"
; 

| mimic name="TEST_QUERY" remove=["source"]
# check remove first command in query, first and only record will be:
# NAME 	VALUE
# Hana	42				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(2, resultArray.size());
		
		//------------------------------
		// Check 2nd Result
		CFWQueryResult queryResults = resultArray.get(1);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
			//------------------------------
			// First Record
			JsonObject record = queryResults.getRecordAsObject(0);
			
			Assertions.assertEquals("Hana", record.get("NAME").getAsString());
			Assertions.assertEquals(42, record.get("VALUE").getAsInt());
		

		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMimic_RemoveMultipleCommands() throws IOException {
		

		String queryString = """
| source random records = 2
| metadata 
	name="TEST_QUERY"
	firstname = "Hana"
	value = 42
| source random records = 4
| record
	[NAME, VALUE]
	[meta(firstname), meta(value)]
	["Rakesh", 88]
| filter NAME == "Rakesh"
| source random records = 8
| filter NAME == "Hana"
| source random records = 16
; 

| mimic remove=["source", "filter"]
# test removing all source and filter commands, result:
#  NAME    	VALUE
#  Hana		42
#  Rakesh	88				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(2, resultArray.size());
		
		//------------------------------
		// Check Third Result
		CFWQueryResult queryResults = resultArray.get(1);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
			//------------------------------
			// First Record
			JsonObject record = queryResults.getRecordAsObject(0);
				
			Assertions.assertEquals("Hana", record.get("NAME").getAsString());
			Assertions.assertEquals(42, record.get("VALUE").getAsInt());
			
			//------------------------------
			// Second Record
			record = queryResults.getRecordAsObject(1);
			
			Assertions.assertEquals("Rakesh", record.get("NAME").getAsString());
			Assertions.assertEquals(88, record.get("VALUE").getAsInt());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testNullTo() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=10 type=various
| set UUID=null
| nullto value="n/a" fields=[ALWAYS_NULL]
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecordAsObject(0);
		
		Assertions.assertTrue(firstRecord.get("UUID").isJsonNull(), "Field UUID is null");
		Assertions.assertEquals("n/a", firstRecord.get("ALWAYS_NULL").getAsString(), "Field ALWAYS_NULL is n/a");

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testParamdefaults() throws IOException {
		
		//---------------------------------
		String queryString = """
| paramdefaults 
	text="default" 
	hello="hello world"
	nullValue=null
	emptyString=' ' 
| source empty
| set
	TEXT_VALUE = param('text') # returns 'default'
	HELLO_VALUE = param('hello') # returns 'hello world'
	UNDEF = param('notAParam') # returns null
	TAKE_THIS= param('againNoParam', 'takeThis') # returns 'takeThis'
	NULL=param('nullValue') # returns null
	NULL_SUBSTITUTED=param('nullValue', 'insteadOfNull') # returns 'insteadOfNull'
	EMPTY=param('emptyString') # returns ' '
	EMPTY_SUBSTITUTED=param('emptyString', 'insteadOfEmpty') # returns 'insteadOfEmpty'				
				 """; 
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("default", record.get("TEXT_VALUE").getAsString());
		Assertions.assertEquals("hello world", record.get("HELLO_VALUE").getAsString());
		Assertions.assertTrue(record.get("UNDEF").isJsonNull());
		Assertions.assertEquals("takeThis", record.get("TAKE_THIS").getAsString());
		Assertions.assertTrue(record.get("NULL").isJsonNull());
		Assertions.assertEquals("insteadOfNull", record.get("NULL_SUBSTITUTED").getAsString());
		Assertions.assertEquals(" ", record.get("EMPTY").getAsString());
		Assertions.assertEquals("insteadOfEmpty", record.get("EMPTY_SUBSTITUTED").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRecord() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[FIRSTNAME, VALUE] # plus AddedField-2 and AddedField-3
	["Aurora", 11]
	["Alejandra", [1,2,3] ]
	["Roberto", {hello: "world"} ]
	["Hera", 8008, null, true]
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//=================================================
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(4, queryResults.getRecordCount());
		
		
		//------------------------------------
		// Check 1st  Record
		JsonObject record = queryResults.getRecordAsObject(0);
			
		Assertions.assertEquals("Aurora", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(11, record.get("VALUE").getAsInt());
		Assertions.assertEquals(null, record.get("AddedField-2"));
		Assertions.assertEquals(null, record.get("AddedField-3"));

		//------------------------------------
		// Check 2nd  Record
		record = queryResults.getRecordAsObject(1);
			
		Assertions.assertEquals("Alejandra", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(true, record.get("VALUE").isJsonArray());
		Assertions.assertEquals(null, record.get("AddedField-2"));
		Assertions.assertEquals(null, record.get("AddedField-3"));
		
		//------------------------------------
		// Check 3rd  Record
		record = queryResults.getRecordAsObject(2);
			
		Assertions.assertEquals("Roberto", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(true, record.get("VALUE").isJsonObject());
		Assertions.assertEquals(null, record.get("AddedField-2"));
		Assertions.assertEquals(null, record.get("AddedField-3"));
		
		//------------------------------------
		// Check 4th  Record
		record = queryResults.getRecordAsObject(3);
			
		Assertions.assertEquals("Hera", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(8008, record.get("VALUE").getAsInt());
		Assertions.assertEquals(true, record.get("AddedField-2").isJsonNull());
		Assertions.assertEquals(true, record.get("AddedField-3").getAsBoolean());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRecord_NamesFalse() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set 
	FIRSTNAME = "Hejdi"
	VALUE = 8.8008
| record names=false
	["Laura", 42]
	["Victora", 99]
							""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//=================================================
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		
		//------------------------------------
		// Check 1st  Record
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals("Hejdi", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals("8.8008", record.get("VALUE").getAsString());
		
		//------------------------------------
		// Check 2nd  Record
		record = queryResults.getRecordAsObject(1);
		
		Assertions.assertEquals("Laura", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(42, record.get("VALUE").getAsInt());
		
		//------------------------------------
		// Check 3rd  Record
		record = queryResults.getRecordAsObject(2);
		
		Assertions.assertEquals("Victora", record.get("FIRSTNAME").getAsString());
		Assertions.assertEquals(99, record.get("VALUE").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRemove() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=1
| remove INDEX, TIME, FIRSTNAME
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(7, result.size());
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
		String queryString = """
| source random records=1
| rename INDEX=ROW
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject result = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(10, result.size());
		Assertions.assertFalse(result.has("INDEX"), "Record has field INDEX");
		Assertions.assertTrue(result.has("ROW"), "Record has field TIME");

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultCompare() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=1000 
| filter FIRSTNAME == "Aurora"
| keep FIRSTNAME, VALUE
| stats	by=FIRSTNAME	COUNT=count(VALUE)		VALUE=avg(VALUE)
; 
| mimic
; 
| resultcompare by=FIRSTNAME
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecordAsObject(0);
		
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
		String queryString = """
| metadata name = "Result One" 
| source random records=10 
; 
| metadata name = "Result Two" 
| source random records=20 
; 
| metadata name = "Result Three" 
| source random records=40 
; 
| metadata name = "Concatenated 1&3" 
| resultconcat "Result One", "Result Three"				
						""";
		
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
		String queryString = """
| globals multidisplay=2 
| metadata name = "Original"  
| source random records=4 
| keep FIRSTNAME, VALUE 
| display menu=false  
; 
| metadata name = "Copy" 
| resultcopy  #copy data of all previous queries 
| display as=panels menu=false
						""";
		
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
		JsonObject firstObject = firstQueryResults.getRecordAsObject(0);
		JsonObject secondObject = secondQueryResults.getRecordAsObject(0);
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
	public void testResultJoin_Inner() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[ID, NAME]
	[1, "Zeus"]
	[2, "Aurora"]
	[3, "Hera"]
;
| record
	[ID, CATEGORY]
	[2, "A"]
	[3, "H"]
	[4, "X"]
;
| meta title=true name="Joined" 
| resultjoin
	on=ID
	join="inner"
						""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResult = resultArray.get(0);
		Assertions.assertEquals(2, queryResult.getRecordCount());
		
		EnhancedJsonObject record = queryResult.getRecord(0);
		Assertions.assertEquals("2", record.get("ID").getAsString());
		Assertions.assertEquals("Aurora", record.get("NAME").getAsString());
		Assertions.assertEquals("A", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(1);
		Assertions.assertEquals("3", record.get("ID").getAsString());
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals("H", record.get("CATEGORY").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultJoin_Left() throws IOException {
		
		//---------------------------------
		String queryString = """
				| record
	[ID, NAME]
	[1, "Zeus"]
	[2, "Aurora"]
	[3, "Hera"]
	;
	| record
	[ID_RIGHT, CATEGORY]
	[2, "A"]
	[3, "H"]
	[4, "X"]
	;
	| meta title=true name="Joined" 
	| resultjoin
	on=[ID, ID_RIGHT]
	join="left"
						""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResult = resultArray.get(0);
		Assertions.assertEquals(3, queryResult.getRecordCount());
		
		EnhancedJsonObject record = queryResult.getRecord(0);
		Assertions.assertEquals("2", record.get("ID").getAsString());
		Assertions.assertEquals("Aurora", record.get("NAME").getAsString());
		Assertions.assertEquals("A", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(1);
		Assertions.assertEquals("3", record.get("ID").getAsString());
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals("H", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(2);
		Assertions.assertEquals("1", record.get("ID").getAsString());
		Assertions.assertEquals("Zeus", record.get("NAME").getAsString());
		Assertions.assertEquals(null, record.get("CATEGORY"));
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultJoin_Right() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[ID, NAME]
	[1, "Zeus"]
	[2, "Aurora"]
	[3, "Hera"]
;
| record
	[ID, CATEGORY]
	[2, "A"]
	[3, "H"]
	[4, "X"]
;
| meta title=true name="Joined" 
| resultjoin
	on=(ID == ID)
	join="right"
		""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResult = resultArray.get(0);
		Assertions.assertEquals(3, queryResult.getRecordCount());
		
		EnhancedJsonObject record = queryResult.getRecord(0);
		Assertions.assertEquals("2", record.get("ID").getAsString());
		Assertions.assertEquals("Aurora", record.get("NAME").getAsString());
		Assertions.assertEquals("A", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(1);
		Assertions.assertEquals("3", record.get("ID").getAsString());
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals("H", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(2);
		Assertions.assertEquals("4", record.get("ID").getAsString());
		Assertions.assertEquals(null, record.get("NAME"));
		Assertions.assertEquals("X", record.get("CATEGORY").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultJoin_NamedAndRemoveFalse() throws IOException {
		
		//---------------------------------
		String queryString = """
| meta name="myLeft"
| record
	[ID, NAME]
	[1, "Zeus"]
	[2, "Aurora"]
	[3, "Hera"]
;
| meta name="myRight"
| record
	[ID, CATEGORY]
	[2, "A"]
	[3, "H"]
	[4, "X"]
;
| source random records=1
;
| meta title=true name="Joined" 
| resultjoin 
	left="myLeft" 
	right="myRight"
	on=ID
	join="left"
	remove=false
	""";
		
		CFWQueryResultList resultList = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(4, resultList.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResult = resultList.get(3);
		Assertions.assertEquals(3, queryResult.getRecordCount());
		
		EnhancedJsonObject record = queryResult.getRecord(0);
		Assertions.assertEquals("2", record.get("ID").getAsString());
		Assertions.assertEquals("Aurora", record.get("NAME").getAsString());
		Assertions.assertEquals("A", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(1);
		Assertions.assertEquals("3", record.get("ID").getAsString());
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals("H", record.get("CATEGORY").getAsString());
		
		record = queryResult.getRecord(2);
		Assertions.assertEquals("1", record.get("ID").getAsString());
		Assertions.assertEquals("Zeus", record.get("NAME").getAsString());
		Assertions.assertEquals(null, record.get("CATEGORY"));
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testResultRemove() throws IOException {
		
		//---------------------------------
		String queryString = """
| metadata name = "config" 
| globals recordCount=7
;
| metadata name = "one" title=true
| source random records=globals(recordCount)
;
| metadata name = "two" title=true
| source random records=globals(recordCount)*2
;
| metadata name = "three" title=true
| source random records=globals(recordCount)*3
| resultremove "config" "two" # only one result left

						""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(2, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(7, queryResults.getRecordCount());
		
		//------------------------------
		// Check Second Query Result
		queryResults = resultArray.get(1);
		Assertions.assertEquals(21, queryResults.getRecordCount());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSet() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=10 
| keep VALUE 
| set  
		BOOL=true 
		NUMBER=123 
		STRING="AAAHHH!!!" 
		OBJECT={a: "1", b: "2"} 
		ARRAY=[1, true, "three"] 
		NULL=null 
		FIELDVAL=VALUE 
		EXPRESSION=(FIELDVAL*FIELDVAL) 
		FUNCTION=count()
							""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject firstRecord = queryResults.getRecordAsObject(0);
		
		//------------------------------
		// Check fields are set
		int FIELDVAL = firstRecord.get("FIELDVAL").getAsInt();
		Assertions.assertEquals(true, firstRecord.get("BOOL").getAsBoolean(), "BOOL is present");
		Assertions.assertEquals(123, firstRecord.get("NUMBER").getAsInt(), "NUMBER is present");
		Assertions.assertEquals("AAAHHH!!!", firstRecord.get("STRING").getAsString(), "STRING is present");
		Assertions.assertEquals("{\"a\":\"1\",\"b\":\"2\"}", CFW.JSON.toJSON(firstRecord.get("OBJECT")) , "OBJECT is present");
		Assertions.assertEquals("[1,true,\"three\"]", CFW.JSON.toJSON(firstRecord.get("ARRAY")) , "ARRAY is present");
		Assertions.assertEquals(true, firstRecord.get("NULL").isJsonNull() , "NULL is present");
		Assertions.assertEquals(firstRecord.get("VALUE").getAsInt(), FIELDVAL , "FIELDVAL is present and equals VALUE");
		Assertions.assertEquals( (FIELDVAL * FIELDVAL) , firstRecord.get("EXPRESSION").getAsInt() , "EXPRESSION is present");
		Assertions.assertEquals( 0 , firstRecord.get("FUNCTION").getAsInt() , "FUNCTION is present and count() returned 0.");
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSort() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data ='[
	{val: 4444} ,{val: 22} ,{val: 333} ,{val: 1}
]' 
| sort val
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(4, queryResults.getRecordCount());
		
		int i=-1;
		Assertions.assertEquals(1, queryResults.getRecordAsObject(++i).get("val").getAsInt());
		Assertions.assertEquals(22, queryResults.getRecordAsObject(++i).get("val").getAsInt());
		Assertions.assertEquals(333, queryResults.getRecordAsObject(++i).get("val").getAsInt());
		Assertions.assertEquals(4444, queryResults.getRecordAsObject(++i).get("val").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSource_Random() throws IOException {
		
		//---------------------------------
		String queryString = 
				  "| source random records=1"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertTrue(record.has("ID"), "Record has field ID");
		Assertions.assertTrue(record.has("TIME"), "Record has field TIME");
		Assertions.assertTrue(record.has("FIRSTNAME"), "Record has field FIRSTNAME");

	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSource_Text_Each() throws IOException {
		
		//---------------------------------
		String queryString = 
				"""
| source text 
	each=["Freya"
		,"Hera"
		,"Eclypsia" ]
	text="Hello " + sourceEach()
				"""
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("Hello Freya", record.get("part").getAsString());
		
		record = queryResults.getRecordAsObject(1);
		Assertions.assertEquals("Hello Hera", record.get("part").getAsString());
		
		record = queryResults.getRecordAsObject(2);
		Assertions.assertEquals("Hello Eclypsia", record.get("part").getAsString());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testStats() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data='[
	  {ITEM: "Pineapple", CLASS: "A", VALUE:"1"}
	, {ITEM: "Pineapple", CLASS: "A", VALUE:"22"}
	, {ITEM: "Pineapple", CLASS: "A", VALUE:"333"}
	, {ITEM: "Pineapple", CLASS: "B", VALUE:"4"}
	, {ITEM: "Strawrrrberry", CLASS: "C", VALUE:"333"}
	, {ITEM: "Strawrrrberry", CLASS: "D", VALUE:"100"}
	, {ITEM: "Strawrrrberry", CLASS: "D", VALUE:"100"}
	, {ITEM: "Strawrrrberry", CLASS: "D", VALUE:"100"}
]'
| stats by=[ITEM, CLASS] 
	count() 
	min(VALUE) 
	AVG=avg(VALUE) 
	MAX=max(VALUE)
	SUM=sum(VALUE)
	MEDIAN=median(VALUE)
	"90th"=perc(VALUE,90)
| sort ITEM, CLASS
			
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(4, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertTrue(record.has("ITEM"), "Record has field");
		Assertions.assertTrue(record.has("CLASS"), "Record has field");
		Assertions.assertTrue(record.has("count()"), "Record has field");
		Assertions.assertTrue(record.has("min(VALUE)"), "Record has field");
		Assertions.assertTrue(record.has("AVG"), "Record has field");
		Assertions.assertTrue(record.has("MAX"), "Record has field");
		Assertions.assertTrue(record.has("SUM"), "Record has field");
		Assertions.assertTrue(record.has("MEDIAN"), "Record has field");
		Assertions.assertTrue(record.has("90th"), "Record has field");
		
		Assertions.assertEquals(3, record.get("count()").getAsInt());
		Assertions.assertEquals(1, record.get("min(VALUE)").getAsInt());
		Assertions.assertEquals(119, Math.round( record.get("AVG").getAsFloat() ));
		Assertions.assertEquals(333, record.get("MAX").getAsInt());
		Assertions.assertEquals(356, record.get("SUM").getAsInt());
		Assertions.assertEquals(22, record.get("MEDIAN").getAsInt());
		Assertions.assertEquals(333, record.get("90th").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testUnbox() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`[
{product="Table", 
	properties: { legs:{ count: 4, length: 80}, lengthUnit: "cm" }, 
	costs: {
		dollaresPerCM: 0.2, 
		sellerFees: [
			{seller: "Super Market", feeUSD: 30},
			{seller: "Jane's Furniture", feeUSD: 50},
			{seller: "Mega Store", feeUSD: 25} ]
}},
{product="Chair", 
	properties: { legs:{ count: 4, length: 50}, lengthUnit: "cm" }, 
	costs: {
		dollaresPerCM: 0.1, 
		sellerFees: [
			{seller: "Super Market", feeUSD: 25},
			{seller: "Jane's Furniture", feeUSD: 37},
			{seller: "Mega Store", feeUSD: 55} ]
}}
]`
| unbox 
	replace=true # remove existing fields
	product                # keep product field
	properties.legs.count  # unboxed to count 
	properties.legs.length # unboxed to length
	costs.dollaresPerCM    # unboxed to dollaresPerCM
	costs.sellerFees       # unbox array of objects
| unbox
	sellerFees # unbox the previously unboxed objects
| remove sellerFees
| set totalCostUSD = (count * length * dollaresPerCM) + feeUSD
| formatfield totalCostUSD=[
	['threshold', 0, 60, 70, 80, 90, "bg"]
	, ['align', "right"]
	, ['postfix', " USD"]
]				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(6, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		System.out.println(record.toString());
		Assertions.assertTrue(record.has("product"), "Record has field");
		Assertions.assertTrue(record.has("count"), "Record has field");
		Assertions.assertTrue(record.has("length"), "Record has field");
		Assertions.assertTrue(record.has("dollaresPerCM"), "Record has field");
		Assertions.assertTrue(record.has("seller"), "Record has field");
		Assertions.assertTrue(record.has("feeUSD"), "Record has field");
		Assertions.assertTrue(record.has("totalCostUSD"), "Record has field");
		
		Assertions.assertEquals("Table", record.get("product").getAsString());
		Assertions.assertEquals(4, record.get("count").getAsInt());
		Assertions.assertEquals(80, record.get("length").getAsInt());
		Assertions.assertEquals("0.2", record.get("dollaresPerCM").toString());
		Assertions.assertEquals("Super Market", record.get("seller").getAsString());
		Assertions.assertEquals(30, record.get("feeUSD").getAsInt());
		Assertions.assertEquals(94, record.get("totalCostUSD").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void test_Tail() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=1000 | tail 123
;
| source random records=1000 | tail 321
				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 results for last and tail
		Assertions.assertEquals(2, resultArray.size());
		
		// last returns 123 results
		CFWQueryResult queryResults = resultArray.get(0);
		JsonArray queryResultsArray = queryResults.getRecordsAsJsonArray();
		Assertions.assertEquals(123, queryResultsArray.size());
		Assertions.assertEquals(999-122, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(999, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
		// tail returns 321 results 
		queryResults = resultArray.get(1);
		queryResultsArray = queryResults.getRecordsAsJsonArray();
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
		String queryString = """
| source random records=1000 | top 123
;
| source random records=1000 | top 321

				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// 2 results for first and top
		Assertions.assertEquals(2, resultArray.size());
		
		// First returns 123 results
		CFWQueryResult queryResults = resultArray.get(0);
		JsonArray queryResultsArray = queryResults.getRecordsAsJsonArray();
		Assertions.assertEquals(123, queryResultsArray.size());
		Assertions.assertEquals(0, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(122, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
		// top returns 321 results
		queryResults = resultArray.get(1);
		queryResultsArray = queryResults.getRecordsAsJsonArray();
		Assertions.assertEquals(321, queryResultsArray.size());
		Assertions.assertEquals(0, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(320, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void test_Skip() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records = 10
| set INDEX = INDEX + 1
| skip 
;
| source random records = 10
| set INDEX = INDEX + 1
| skip 3 
	first = true
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(2, resultArray.size());
		
		//---------------------------
		// First Result
		CFWQueryResult queryResults = resultArray.get(0);
		JsonArray queryResultsArray = queryResults.getRecordsAsJsonArray();
		
		Assertions.assertEquals(9, queryResultsArray.size());
		Assertions.assertEquals(1, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(9, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
		//---------------------------
		// Second Result
		queryResults = resultArray.get(1);
		queryResultsArray = queryResults.getRecordsAsJsonArray();
		
		Assertions.assertEquals(7, queryResultsArray.size());
		Assertions.assertEquals(4, queryResultsArray.get(0).getAsJsonObject().get("INDEX").getAsInt());
		Assertions.assertEquals(10, queryResultsArray.get(queryResultsArray.size()-1).getAsJsonObject().get("INDEX").getAsInt());
		
	}
		
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testUndefTo() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[NAME		, VALUE	, VALUE_B]
	["Marketa"]
	["Hera"		, 2		, "a"]
| undefto value="n/a"
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//============================================
		// First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
		//------------------------------
		// First Record
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals("Marketa", record.get("NAME").getAsString());
		Assertions.assertEquals("n/a", record.get("VALUE").getAsString());
		Assertions.assertEquals("n/a", record.get("VALUE_B").getAsString());
		
		//------------------------------
		// Second Record
		record = queryResults.getRecordAsObject(1);
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals("2", record.get("VALUE").getAsString());
		Assertions.assertEquals("a", record.get("VALUE_B").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testUndefTo_Fields() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[NAME		, VALUE	, VALUE_B]
	["Marketa"]
	["Hera"		, null		, "a"]
| undefto value="n/a" fields=VALUE_B
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());

		//============================================
		// First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
		//------------------------------
		// First Record
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals("Marketa", record.get("NAME").getAsString());
		Assertions.assertEquals(false, record.has("VALUE"));
		Assertions.assertEquals("n/a", record.get("VALUE_B").getAsString());
		
		//------------------------------
		// Second Record
		record = queryResults.getRecordAsObject(1);
		Assertions.assertEquals("Hera", record.get("NAME").getAsString());
		Assertions.assertEquals(true, record.get("VALUE").isJsonNull());
		Assertions.assertEquals("a", record.get("VALUE_B").getAsString());

	}
	
}

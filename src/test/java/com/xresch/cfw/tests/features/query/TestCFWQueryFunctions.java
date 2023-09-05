package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.tests._master.DBTestMaster;
import com.xresch.cfw.tests.assets.CFWTestUtils;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWQueryFunctions extends DBTestMaster{
	
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
	public void testAbs() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	NEGATIV=-33\r\n" + 
				"	ABSOLUTE=abs(NEGATIV) \r\n" + 
				"	ZERO=abs() \r\n" + 
				"	ZERO_AGAIN=abs(null)\r\n" + 
				"	STRING_ZERO=abs('returns0')\r\n" + 
				"	BOOL_ZERO=abs(true)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(-33, record.get("NEGATIV").getAsInt());
		Assertions.assertEquals(33, record.get("ABSOLUTE").getAsInt());
		Assertions.assertEquals(0, record.get("ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("ZERO_AGAIN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("BOOL_ZERO").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testAvg() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionAvg.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
					
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("2.500", record.get("AVG_ARRAY").toString());
		Assertions.assertEquals(2, record.get("AVG_ARRAY_NULLS").getAsInt());
		Assertions.assertEquals("2.500", record.get("AVG_OBJECT").toString());
		Assertions.assertEquals(2, record.get("AVG_OBJECT_NULLS").getAsInt());
		Assertions.assertEquals(77, record.get("AVG_NUMBER").getAsInt());
		Assertions.assertTrue(record.get("AVG_ZERO").isJsonNull());
		Assertions.assertTrue(record.get("AVG_NULL").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_A").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_B").isJsonNull());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testAvg_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionAvg_Aggr.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("2.500", record.get("AVG").toString());
		Assertions.assertEquals(2, record.get("AVG_NONULL").getAsInt());
		Assertions.assertEquals("1.500", record.get("AVG_NULLS").toString());
		Assertions.assertEquals("32.39506", record.get("AVG_FLOAT").toString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCeil() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	POSITIVE=ceil(124.34567)\r\n" + 
				"	NEGATIVE=ceil(-42.34567)\r\n"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(125, record.get("POSITIVE").getAsInt());
		Assertions.assertEquals(-42, record.get("NEGATIVE").getAsInt());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testClone() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set ARRAY=[55,66] OBJECT={x: 77, y:\"eightyeight\"}\r\n" + 
				"| set\r\n" + 
				"	ARRAY_CLONE=clone(ARRAY)\r\n" + 
				"	OBJECT_CLONE=clone(OBJECT)\r\n" + 
				"	STRING_CLONE=clone(\"uhyeahclonemerighttherebaby\")\r\n" + 
				"	NUMBER_CLONE=clone(8008)\r\n" + 
				"	BOOL_CLONE=clone(true)\r\n" + 
				"	NULL_CLONE=clone(null)\r\n"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		JsonArray arrayClone =  record.get("ARRAY_CLONE").getAsJsonArray();
		JsonObject objectClone =  record.get("OBJECT_CLONE").getAsJsonObject();
		
		Assertions.assertEquals(55, arrayClone.get(0).getAsInt());
		Assertions.assertEquals(66, arrayClone.get(1).getAsInt());
		Assertions.assertEquals(77, objectClone.get("x").getAsInt());
		Assertions.assertEquals("eightyeight", objectClone.get("y").getAsString());
		Assertions.assertEquals("uhyeahclonemerighttherebaby", record.get("STRING_CLONE").getAsString());
		Assertions.assertEquals(8008, record.get("NUMBER_CLONE").getAsInt());
		Assertions.assertEquals(true, record.get("BOOL_CLONE").getAsBoolean());
		Assertions.assertEquals(true, record.get("NULL_CLONE").isJsonNull());
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Strings() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Strings.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("Aurora", record.get("FIRSTNAME").getAsString());
		
		Assertions.assertEquals(false, 	record.get("S1").getAsBoolean());
		Assertions.assertEquals(true,  	record.get("S2").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B4").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("N1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N4").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N6").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N7").getAsBoolean());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Numbers() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Numbers.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(42, record.get("NUMBER").getAsInt());
		
		Assertions.assertEquals(true, 	record.get("N1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N5").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("S1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S4").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S6").getAsBoolean());
		
		Assertions.assertEquals(false, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B2").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B4").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Booleans() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Booleans.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(true, record.get("BOOLEAN").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B2").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B4").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("S1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S5").getAsBoolean());
		
		Assertions.assertEquals(false, 	record.get("N1").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N2").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N4").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Arrays() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Arrays.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);

		Assertions.assertEquals(true, 	record.get("S1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("S2").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S4").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B6").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("N1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N6").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N7").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("Z1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("Z2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("Z3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("Z4").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Objects() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Objects.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		
		Assertions.assertEquals(true, 	record.get("S1").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S2").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("S3").getAsBoolean());

		Assertions.assertEquals(true, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("B4").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("N1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N3").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("N4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("N6").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("Z1").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("Z2").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testContains_Nulls() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionContains_Nulls.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		
		Assertions.assertEquals(true, 	record.get("A1").isJsonNull());
		Assertions.assertEquals(true, 	record.get("A2").isJsonNull());
		
		Assertions.assertEquals(true, 	record.get("B1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("B2").getAsBoolean());
		
		Assertions.assertEquals(true, 	record.get("C1").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("C2").getAsBoolean());
		Assertions.assertEquals(true, 	record.get("C3").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("C4").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("C5").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("C6").getAsBoolean());
		Assertions.assertEquals(false, 	record.get("C7").getAsBoolean());
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCos() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	RADIANS=0.872665 # radians for 50 degress\r\n" + 
				"	DEGREES=50\r\n" + 
				"	COS_RADIANS=round(cos(RADIANS),3) \r\n" + 
				"	COS_DEGREES=round(cos(DEGREES,true),3) \r\n" + 
				"	# all following return 0\r\n" + 
				"	NOTHING_RETURNS_ZERO=cos() \r\n" + 
				"	RETURNS_ZERO_AGAIN=cos(null)\r\n" + 
				"	STRING_ZERO=cos('returns-0')\r\n" + 
				"	BOOL_ZERO=cos(true)\r\n" + 
				""
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("0.872665", record.get("RADIANS").getAsString());
		Assertions.assertEquals(50, record.get("DEGREES").getAsInt());
		Assertions.assertEquals("0.643", record.get("COS_RADIANS").getAsString());
		Assertions.assertEquals("0.643", record.get("COS_DEGREES").getAsString());
		Assertions.assertEquals(0, record.get("NOTHING_RETURNS_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("RETURNS_ZERO_AGAIN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("BOOL_ZERO").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCount() throws IOException {
		
		//---------------------------------
		String queryString = 
				"| source empty records=10\r\n" + 
				"| set \r\n" + 
				"	#return a number that increases by 1 every call\r\n" + 
				"	INDEX=count()\r\n" + 
				"	COUNT_ARRAY=count([null, 1, true, \"three\"])\r\n" + 
				"	COUNT_OBJECT=count({a: null, b: 1, c: true, d: \"three\"})\r\n" + 
				"	COUNT_STRING=count(\"test\")\r\n" + 
				"	COUNT_NUMBER=count(5)\r\n" + 
				"	COUNT_BOOL=count(true)\r\n" + 
				"	COUNT_NULL=count(null)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(0, record.get("INDEX").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_ARRAY").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_OBJECT").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_STRING").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_NUMBER").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_BOOL").getAsInt());
		Assertions.assertEquals(true, record.get("COUNT_NULL").isJsonNull());
		
		//------------------------------
		// Check 2nd Query Result
		JsonObject secondRecord = queryResults.getRecord(1);
		Assertions.assertEquals(1, secondRecord.get("INDEX").getAsInt());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCountnulls() throws IOException {
		
		//---------------------------------
		String queryString = 
				"| source json data=`[\r\n" + 
				"	 {index: 0, array: [1,2,3], object: {a: 0, b: 1, c: 3 } }\r\n" + 
				"	,{index: 1, array: [null,null,3, null,5, null], object: {a: null, b: 22, c: null} }\r\n" + 
				"]`\r\n" + 
				"| set \r\n" + 
				"	COUNT_IS_ONE=countnulls(null) # returns 1\r\n" + 
				"	NULLS_IN_ARRAY=countnulls(array) # returns 0/4\r\n" + 
				"	NULLS_IN_OBJECT=countnulls(object) # returns 0/2\r\n" + 
				"	# every other value will result in count 0\r\n" + 
				"	NUMBER=countnulls(index)\r\n" + 
				"	BOOLEAN=countnulls(true)\r\n" + 
				"	STRING=countnulls(\"some_string\")\r\n" + 
				"	# no params will result in returning null\r\n" + 
				"	UNCOUNTABLE=countnulls()"
						;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(1, record.get("COUNT_IS_ONE").getAsInt());
		Assertions.assertEquals(0, record.get("NULLS_IN_ARRAY").getAsInt());
		Assertions.assertEquals(0, record.get("NULLS_IN_OBJECT").getAsInt());
		Assertions.assertEquals(0, record.get("NUMBER").getAsInt());
		Assertions.assertEquals(0, record.get("BOOLEAN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING").getAsInt());
		Assertions.assertEquals(true, record.get("UNCOUNTABLE").isJsonNull());
		
		//------------------------------
		// Check 2nd Query Result
		JsonObject secondRecord = queryResults.getRecord(1);
		Assertions.assertEquals(1, secondRecord.get("COUNT_IS_ONE").getAsInt());
		Assertions.assertEquals(4, secondRecord.get("NULLS_IN_ARRAY").getAsInt());
		Assertions.assertEquals(2, secondRecord.get("NULLS_IN_OBJECT").getAsInt());
		Assertions.assertEquals(0, secondRecord.get("NUMBER").getAsInt());
		Assertions.assertEquals(0, secondRecord.get("BOOLEAN").getAsInt());
		Assertions.assertEquals(0, secondRecord.get("STRING").getAsInt());
		Assertions.assertEquals(true, secondRecord.get("UNCOUNTABLE").isJsonNull());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testDecode() throws IOException {
		
		//---------------------------------
		String original = "Space Lodash_ Equals= Other<>äÖü!?";
		String encoded = CFW.HTTP.encode(original);
		
		String queryString = 
				"| source empty records=1 \r\n" + 
				"| set \r\n" + 
				"	# encode a string\r\n" + 
				"	ENCODED=encode('"+original+"')\r\n" + 
				"	DECODED=decode(ENCODED)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(original, record.get("DECODED").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testEarliest_and_EarliestSet() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionEarliest.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -60);
		
		Assertions.assertEquals(1, resultArray.size());
									
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("1693223296188", record.get("epoch").getAsString());
		Assertions.assertEquals("1693223296188", record.get("epochNull").getAsString());
		Assertions.assertEquals("2023-08-28T11:48:16", record.get("formatted").getAsString());
		Assertions.assertEquals("2023-08-28", record.get("yearDayMonth").getAsString());
		Assertions.assertEquals("11:48:16", record.get("utcTime").getAsString());
		Assertions.assertEquals("12:48:16", record.get("clientTime").getAsString());
		Assertions.assertEquals("188", record.get("Milliseconds").getAsString());
		Assertions.assertEquals("Mon / Monday", record.get("DayName").getAsString());
		Assertions.assertEquals("Aug / August", record.get("MonthName").getAsString());
		Assertions.assertEquals("+01:00 / +0100 / GMT+01:00", record.get("Timezones").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testEncode() throws IOException {
		
		//---------------------------------
		String original = "Space Lodash_ Equals= Other<>äÖü!?";
		String encoded = CFW.HTTP.encode(original);
		
		String queryString = 
				"| source empty records=1 \r\n" + 
				"| set \r\n" + 
				"	# encode a string\r\n" + 
				"	ENCODED=encode('"+original+"')\r\n" + 
				"	DECODED=decode(ENCODED)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(encoded, record.get("ENCODED").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testExtract() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionExtract.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("8b7b-437c", record.get("MIDDLE").getAsString());
		Assertions.assertEquals("double-u-double-u-double-u", record.get("HOST").getAsString());
		Assertions.assertEquals("282c65a0-8b7b-437c-904", record.get("ID_FROM_URL").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFields() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionFields.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		// ALL_FIELDS = fields() #["A","B","C","ALL_FIELDS","FILTERED_FIELDS"] - contains all as command detects fieldnames before executing 
		// FILTERED_FIELDS = fields([FILTERED_FIELDS, ALL_FIELDS, B]) #["A","C"]
						
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("[\"A\",\"B\",\"C\",\"ALL_FIELDS\",\"FILTERED_FIELDS\"]", record.get("ALL_FIELDS").toString());
		Assertions.assertEquals("[\"A\",\"C\"]", record.get("FILTERED_FIELDS").toString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFloor() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	POSITIVE=floor(124.34567)\r\n" + 
				"	NEGATIVE=floor(-42.34567)\r\n"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(124, record.get("POSITIVE").getAsInt());
		Assertions.assertEquals(-43, record.get("NEGATIVE").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testLatest_and_LatestSet() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionLatest.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
									
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("1651212296155", record.get("epoch").getAsString());
		Assertions.assertEquals("1651212296155", record.get("epochNull").getAsString());
		Assertions.assertEquals("2022-04-29T06:04:56", record.get("formatted").getAsString());
		Assertions.assertEquals("2022-04-29", record.get("yearDayMonth").getAsString());
		Assertions.assertEquals("06:04:56", record.get("utcTime").getAsString());
		Assertions.assertEquals("08:04:56", record.get("clientTime").getAsString());
		Assertions.assertEquals("155", record.get("Milliseconds").getAsString());
		Assertions.assertEquals("Fri / Friday", record.get("DayName").getAsString());
		Assertions.assertEquals("Apr / April", record.get("MonthName").getAsString());
		Assertions.assertEquals("+02:00 / +0200 / GMT+02:00", record.get("Timezones").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testLength() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionLength.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(0, record.get("LENGTH_NOPARAM").getAsInt());
		Assertions.assertEquals(true, record.get("LENGTH_NULL").isJsonNull());
		Assertions.assertEquals(3, record.get("LENGTH_ARRAY").getAsInt());
		Assertions.assertEquals(4, record.get("LENGTH_OBJECT").getAsInt());
		Assertions.assertEquals(6, record.get("LENGTH_STRING").getAsInt());
		Assertions.assertEquals(5, record.get("LENGTH_BOOL").getAsInt());
		Assertions.assertEquals(4, record.get("LENGTH_INT").getAsInt());
		Assertions.assertEquals(5, record.get("LENGTH_NEGATIVE").getAsInt());
		Assertions.assertEquals(7, record.get("LENGTH_FLOAT").getAsInt());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMax() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMax.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(4, record.get("MAX_ARRAY").getAsInt());
		Assertions.assertEquals(33, record.get("MAX_OBJECT").getAsInt());
		Assertions.assertEquals(77, record.get("MAX_NUMBER").getAsInt());
		Assertions.assertTrue(record.get("MAX_ZERO").isJsonNull());
		Assertions.assertTrue(record.get("MAX_NULL").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_A").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_B").isJsonNull());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMax_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMax_Aggr.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(4, record.get("MAX").getAsInt());
		Assertions.assertEquals(6, record.get("MAX_VALUE").getAsInt());
		Assertions.assertEquals("99.123456", record.get("MAX_FLOAT").toString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMedian() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMedian.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("2.5", record.get("MEDIAN_ARRAY").toString());
		Assertions.assertEquals(2, record.get("MEDIAN_ARRAY_NULLS").getAsInt());
		Assertions.assertEquals("3", record.get("MEDIAN_OBJECT").toString());
		Assertions.assertEquals(2, record.get("MEDIAN_OBJECT_NULLS").getAsInt());
		Assertions.assertEquals(77, record.get("MEDIAN_NUMBER").getAsInt());
		Assertions.assertTrue(record.get("MEDIAN_ZERO").isJsonNull());
		Assertions.assertTrue(record.get("MEDIAN_NULL").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_A").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_B").isJsonNull());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMedian_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMedian_Aggr.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("3", record.get("MEDIAN").toString());
		Assertions.assertEquals("7.5", record.get("MEDIAN_NONULL").toString());
		Assertions.assertEquals("6.5", record.get("MEDIAN_NULLS").toString());
		Assertions.assertEquals("3.995", record.get("MEDIAN_FLOAT").toString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMeta() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMeta.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(true, record.get("LIGHTS_ON").getAsBoolean());
		Assertions.assertEquals("Jane", record.get("LOVELY_PERSON").getAsString());
		Assertions.assertEquals(42, record.get("THE_WINNER_IS").getAsInt());
		Assertions.assertEquals(true, record.get("MOTHING_HERE").isJsonNull());
		Assertions.assertEquals("[\"throw\",\"the\",\"ball\",\"woof!\"]", record.get("REQUIRED_ACTION").toString());
		Assertions.assertEquals("{\"I\":\"am\",\"your\":\"god\",\"pet\":\"and worship me already so that this example stops getting utterly long!\"}", record.get("REQUEST_FOR_OBEDIENCE").toString());
		Assertions.assertEquals(87, record.get("OBEDIENCE_SCORE").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMin() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMin.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(1, record.get("MIN_ARRAY").getAsInt());
		Assertions.assertEquals(0, record.get("MIN_OBJECT").getAsInt());
		Assertions.assertEquals(77, record.get("MIN_NUMBER").getAsInt());
		Assertions.assertTrue(record.get("MIN_ZERO").isJsonNull());
		Assertions.assertTrue(record.get("MIN_NULL").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_A").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_B").isJsonNull());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMin_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionMin_Aggr.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(1, record.get("MIN").getAsInt());
		Assertions.assertEquals(4, record.get("MIN_VALUE").getAsInt());
		Assertions.assertEquals("1.33333333", record.get("MIN_FLOAT").toString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testNow() throws IOException {
		
		//---------------------------------
		String queryString =
				  "| source empty records=1\r\n"
				+ "| set \r\n"
				+ "	NOW = now()\r\n"
				+ "	NOW_OFFSET = now(null, -1, \"h\")\r\n"
				+ "	NOW_FORMAT = now(\"YYYY-MM-dd\")"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		// truncate to minutes to make this test work, except in cases when the minute changes.
		long presentTime = CFWTimeUnit.m.truncate(new Date().getTime());
		long presentTimeOffset = CFWTimeUnit.h.offset(presentTime, -1);
		
		ZonedDateTime zonedTime = CFW.Time.zonedTimeFromEpoch(presentTime);

		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		String dateFormatted = CFW.Time.formatDate(zonedTime, "YYYY-MM-dd", queryResults.getQueryContext().getTimezoneOffsetMinutes());
		String message = "If the assertion failed, the test might have executed exactly on the minute.";
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(presentTime, CFWTimeUnit.m.truncate( record.get("NOW").getAsLong()), message );
		Assertions.assertEquals(presentTimeOffset, CFWTimeUnit.m.truncate( record.get("NOW_OFFSET").getAsLong()), message);
		Assertions.assertEquals(dateFormatted, record.get("NOW_FORMAT").getAsString(), message);
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testNullto() throws IOException {
		
		//---------------------------------
		String dangerZone = "!!! Danger Zone(Bio Hazard) - do not enter!!!";
		String theQueriesID = "This query identifies as a killer virus. It's pronouns are Ah/choo!!!";
		String queryString =
				"| source empty records=1\r\n"
				+ "| set \r\n"
				+ "	EMPTY = null\r\n"
				+ "	THE_QUERIES_ID = \""+theQueriesID+"\"\r\n"
				+ "	NULL_AGAIN = null\r\n"
				+ "	NULL_STRING = 'null'\r\n"
				+ "	STAYS_NULL = null	\r\n"
				+ "| nullto \r\n"
				+ "	   fields=[EMPTY, THE_QUERIES_ID , NULL_AGAIN, NULL_STRING ] # exclude STAYS_NULL \r\n"
				+ "	   value=\""+dangerZone+"\""
			;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(dangerZone, record.get("EMPTY").getAsString() );
		Assertions.assertEquals(theQueriesID, record.get("THE_QUERIES_ID").getAsString() );
		Assertions.assertEquals(dangerZone, record.get("NULL_AGAIN").getAsString() );
		Assertions.assertEquals("null", record.get("NULL_STRING").getAsString() );
		Assertions.assertEquals(true, record.get("STAYS_NULL").isJsonNull() );
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testPerc() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionPerc.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
			
		
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		//------------------------------
		// Check First Query Result
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(5, record.get("50Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90PercNulls").getAsInt());
		Assertions.assertEquals(77, record.get("70PercObject").getAsInt());
		Assertions.assertEquals(77, record.get("70PercObjectNulls").getAsInt());
		Assertions.assertEquals(0, record.get("PERC_NUMBER").getAsInt());
		
		Assertions.assertTrue(record.get("PERC_NONE").isJsonNull());
		Assertions.assertTrue(record.get("PERC_NULL").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_A").isJsonNull());
		Assertions.assertTrue(record.get("UNSUPPORTED_B").isJsonNull());
		
		//------------------------------
		// Check Second Query Result
		record = queryResults.getRecord(1);
		Assertions.assertEquals(5, record.get("50Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90Perc").getAsInt());
		Assertions.assertEquals(8, record.get("90PercNulls").getAsInt());
		Assertions.assertEquals(77, record.get("70PercObject").getAsInt());
		Assertions.assertEquals(44, record.get("70PercObjectNulls").getAsInt());
		Assertions.assertEquals(1, record.get("PERC_NUMBER").getAsInt());
		
		//------------------------------
		// Check Third Query Result
		record = queryResults.getRecord(2);
		Assertions.assertEquals(5, record.get("50Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90PercNulls").getAsInt());
		Assertions.assertEquals(33, record.get("70PercObject").getAsInt());
		Assertions.assertEquals(33, record.get("70PercObjectNulls").getAsInt());
		Assertions.assertEquals(3, record.get("PERC_NUMBER").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testPerc_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionPerc_Aggr.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(8, record.get("PERC").getAsInt());
		Assertions.assertEquals(4, record.get("PERC_NONULL").getAsInt());
		Assertions.assertEquals(3, record.get("PERC_NULLS").getAsInt());
		Assertions.assertEquals("8.5653", record.get("PERC_FLOAT").toString());
		
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRandom() throws IOException {
		
		//---------------------------------
		String queryString =
				"| source empty records= 100\r\n"
				+ "| set\r\n"
				+ "  PERCENT = random()\r\n"
				+ "  ZERO_ONE = random(0,1)\r\n"
				+ "  ONE = random(1,1)\r\n"
				+ "  HUNDRED = random(100) #second param is default 100 \r\n"
				+ "  MINUS = random(-10, 10)\r\n"
				+ "  HALF_TO_FULL_MILLION = random((10^6)/2, 10^6)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecord(0);
			CFWTestUtils.assertIsBetween(0, 100, record.get("PERCENT").getAsInt() );
			CFWTestUtils.assertIsBetween(0, 1, record.get("ZERO_ONE").getAsInt() );
			Assertions.assertEquals(1, record.get("ONE").getAsInt() );
			Assertions.assertEquals(100, record.get("HUNDRED").getAsInt() );
			CFWTestUtils.assertIsBetween(-10, 10, record.get("MINUS").getAsInt() );
			CFWTestUtils.assertIsBetween(500000, 1000000, record.get("HALF_TO_FULL_MILLION").getAsInt() );
		}
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRandomFloat() throws IOException {
		
		//---------------------------------
		String queryString =
				"| source empty records= 100\r\n"
				+ "| set\r\n"
				+ "  PERCENT= randomFloat() # zero to 1\r\n"
				+ "  ZERO_FIFTY = randomFloat(0,50)\r\n"
				+ "  ONE = randomFloat(1) #second param is default 1\r\n"
				+ "  NINTY_NINE= randomFloat(99, 99) \r\n"
				+ "  MINUS = randomFloat(-10, 10)\r\n"
				+ "  HALF_TO_FULL_MILLION = randomFloat((10^6)/2, 10^6)"
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecord(0);
			CFWTestUtils.assertIsBetween(0, 1, record.get("PERCENT").getAsInt() );
			CFWTestUtils.assertIsBetween(0, 55, record.get("ZERO_FIFTY").getAsInt() );
			Assertions.assertEquals(1, record.get("ONE").getAsInt() );
			Assertions.assertEquals(99, record.get("NINTY_NINE").getAsInt() );
			CFWTestUtils.assertIsBetween(-10, 10, record.get("MINUS").getAsInt() );
			CFWTestUtils.assertIsBetween(500000, 1000000, record.get("HALF_TO_FULL_MILLION").getAsInt() );
		}
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRandomFrom() throws IOException {
		
		//---------------------------------
		String queryString =
				  "| source empty records= 100\r\n"
				+ "| set\r\n"
				+ "  NULL = randomFrom() # null\r\n"
				+ "  A_OR_B= randomFrom([\"A\", \"B\"])\r\n"
				+ "  ONE = randomFrom(1) # return same value\r\n"
				+ "  A_OR_B_OR_ONE = randomFrom([A_OR_B, ONE]) "
				+ "  X_OR_Y = randomFrom({X: 22, Y: 33}) "
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecord(0);
			Assertions.assertEquals(true, record.get("NULL").isJsonNull() );
			CFWTestUtils.assertIsEither(record.get("A_OR_B").getAsString(), "A", "B" );
			Assertions.assertEquals(1, record.get("ONE").getAsInt() );
			CFWTestUtils.assertIsEither(record.get("A_OR_B_OR_ONE").getAsString(), "A", "B", "1" );
			CFWTestUtils.assertIsEither(record.get("X_OR_Y").getAsString(), "X", "Y");
		}
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testReplace() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionReplace.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
				
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals(true, record.get("NULL").isJsonNull());
		Assertions.assertEquals("Alejandra 1234 #!<>?=() 1234", record.get("SAME").getAsString());
		Assertions.assertEquals("Alejandra #!<>?=()", record.get("REMOVE").getAsString());
		Assertions.assertEquals("Alejandro Sanchez !<>?=() 1234", record.get("REPLACE").getAsString());
		Assertions.assertEquals("Alejandra 42 #!<>?=() 42", record.get("REPLACE_MULTI").getAsString());
		Assertions.assertEquals("maybe", record.get("BOOL").getAsString());
		Assertions.assertEquals("1-eight-1-eight-1-eight-", record.get("NUMBER").getAsString());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRound() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionRound.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("0", record.get("ZERO").getAsString());
		Assertions.assertEquals("42", record.get("INT_DOWN").getAsString());
		Assertions.assertEquals("43", record.get("INT_UP").getAsString());
		Assertions.assertEquals("55.56", record.get("PRECISION_TWO").getAsString());
		Assertions.assertEquals("44.444", record.get("PRECISION_THREE").getAsString());
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSin() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	RADIANS=0.872665 # radians for 50 degress\r\n" + 
				"	DEGREES=50\r\n" + 
				"	SIN_RADIANS=round(sin(RADIANS),3) \r\n" + 
				"	SIN_DEGREES=round(sin(DEGREES,true),3) \r\n" + 
				"	# all following return 0\r\n" + 
				"	NOTHING_RETURNS_ZERO=sin() \r\n" + 
				"	RETURNS_ZERO_AGAIN=sin(null)\r\n" + 
				"	STRING_ZERO=sin('returns-0')\r\n" + 
				"	BOOL_ZERO=sin(true)\r\n" + 
				""
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("0.872665", record.get("RADIANS").getAsString());
		Assertions.assertEquals(50, record.get("DEGREES").getAsInt());
		Assertions.assertEquals("0.766", record.get("SIN_RADIANS").getAsString());
		Assertions.assertEquals("0.766", record.get("SIN_DEGREES").getAsString());
		Assertions.assertEquals(0, record.get("NOTHING_RETURNS_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("RETURNS_ZERO_AGAIN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("BOOL_ZERO").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTan() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	RADIANS=0.872665 # radians for 50 degress\r\n" + 
				"	DEGREES=50\r\n" + 
				"	TAN_RADIANS=round(tan(RADIANS),3) \r\n" + 
				"	TAN_DEGREES=round(tan(DEGREES,true),3) \r\n" + 
				"	# all following return 0\r\n" + 
				"	NOTHING_RETURNS_ZERO=tan() \r\n" + 
				"	RETURNS_ZERO_AGAIN=tan(null)\r\n" + 
				"	STRING_ZERO=tan('returns-0')\r\n" + 
				"	BOOL_ZERO=tan(true)\r\n" + 
				""
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("0.872665", record.get("RADIANS").getAsString());
		Assertions.assertEquals(50, record.get("DEGREES").getAsInt());
		Assertions.assertEquals("1.192", record.get("TAN_RADIANS").getAsString());
		Assertions.assertEquals("1.192", record.get("TAN_DEGREES").getAsString());
		Assertions.assertEquals(0, record.get("NOTHING_RETURNS_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("RETURNS_ZERO_AGAIN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING_ZERO").getAsInt());
		Assertions.assertEquals(0, record.get("BOOL_ZERO").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTimeformat() throws IOException {
		
		//---------------------------------
		String queryString = CFW.Files.readPackageResource(PACKAGE, "query_testFunctionTimeformat.txt");
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecord(0);
		Assertions.assertEquals("2023-09-04T12:15:01", record.get("formatted").getAsString());
		Assertions.assertEquals("2023-09-04", record.get("yearDayMonth").getAsString());
		Assertions.assertEquals("12:15:01", record.get("utcTime").getAsString());
		Assertions.assertEquals("14:15:01", record.get("clientTime").getAsString());
		Assertions.assertEquals("889", record.get("millis").getAsString());
		Assertions.assertEquals("Mon / Monday", record.get("DayName").getAsString());
		Assertions.assertEquals("Sep / September", record.get("MonthName").getAsString());
		Assertions.assertEquals("+02:00 / +0200 / GMT+02:00", record.get("Timezones").getAsString());
		Assertions.assertEquals(true, record.get("epochNoParams").isJsonNull());
		Assertions.assertEquals(true, record.get("epochFormatOnly").isJsonNull());
		Assertions.assertEquals(true, record.get("epochFormatNull").isJsonNull());
		Assertions.assertEquals(true, record.get("epochTimeNull").isJsonNull());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTimeframeOffset() throws IOException {
		
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
	
}

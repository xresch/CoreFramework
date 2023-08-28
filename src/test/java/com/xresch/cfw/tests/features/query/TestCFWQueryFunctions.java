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
	public void testSin() throws IOException {
		
		//---------------------------------
		String queryString = "| source empty records=1\r\n" + 
				"| set\r\n" + 
				"	RADIANS=0.872665 # radians for 50 degress\r\n" + 
				"	DEGREES=50\r\n" + 
				"	SIN_RADIANS=round(sin(RADIANS),3) \r\n" + 
				"	SIN_DEGREES=round(sin(DEGREES,true),3) \r\n" + 
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

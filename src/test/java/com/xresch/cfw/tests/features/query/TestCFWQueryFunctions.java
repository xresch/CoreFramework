package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;

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
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.tests._master.DBTestMaster;
import com.xresch.cfw.tests.assets.CFWTestUtils;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWQueryFunctions extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static final String PACKAGE_FUNCTIONS = "com.xresch.cfw.tests.features.query.testdata.functions";
	
	private static long earliest = new Instant().minus(1000*60*30).getMillis();
	private static long latest = new Instant().getMillis();
	
	@BeforeAll
	public static void setup() {
		
		//FeatureQuery feature = new FeatureQuery();
		//feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE_FUNCTIONS);

		context.setEarliest(earliest);
		context.setLatest(latest);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testAbs() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1 
| set 
	NEGATIV=-33 
	ABSOLUTE=abs(NEGATIV)  
	ZERO=abs()  
	ZERO_AGAIN=abs(null) 
	STRING_ZERO=abs('returns0') 
	BOOL_ZERO=abs(true)				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	 {index: 77
	 , array: [1,2,3,4, null, true, "string"]
	 , object: {a: 1, b: 2, c: 3, d: 4, e: null, f: true, string: "ignored"} }

]
`
| set 
	# return average of numbers
	AVG_ARRAY=avg(array)
	# treat nulls as zero in the statistics, ignore other types
	AVG_ARRAY_NULLS=avg(array,true)
	# returns the average of all numbers of all fields
	AVG_OBJECT=avg(object)
	# treat nulls as zero in the statistics
	AVG_OBJECT_NULLS=avg(object,true)
	# if input is a single number, returns that number
	AVG_NUMBER=avg(index)
	# following will return null
	AVG_ZERO=avg()
	AVG_NULL=avg(null)
	UNSUPPORTED_A=avg(true)
	UNSUPPORTED_B=avg("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
					
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 7.12345678}
	 ,{count: 3, value: 3, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	AVG=avg(count)
	AVG_NONULL=avg(value)
	AVG_NULLS=avg(value,true)
	AVG_FLOAT=avg(float,true,5)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("2.500", record.get("AVG").toString());
		Assertions.assertEquals(2, record.get("AVG_NONULL").getAsInt());
		Assertions.assertEquals("1.500", record.get("AVG_NULLS").toString());
		Assertions.assertEquals("32.39506", record.get("AVG_FLOAT").toString());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCase() throws IOException { // well this test case is testing case, therefore the name testCase()
		
		//---------------------------------
		String queryString = """
| source empty 
| set

	SIMPLE_A = case( true,  "One beer for Franky!") # returns "One beer for Franky!"
	SIMPLE_B = case( true,  1, true, 2 ) # returns 1
	SIMPLE_C = case( false, 1, true, 2 ) # returns 2
	
	SLIGHTLY_COMPLEX = case( 
			( 2 > 1 AND 3 < 1), "no"
			( 2 > 1 OR 3 < 1), "YES!"
			) # returns "YES!"

	NULL = case() # returns null
	
	MISSING_A = case( (1 == 1) ) # returns null
	
	MISSING_B = case( 
			(1 == 2), true
			, (1 == 2) 
		) # returns null as value for 2nd condition is undefined
		
	NOT_BOOLEAN = case(
					"Panna Cotta", 1
					, true		 , 2
				) # returns 2 as first argument is not a condition but an Italian dessert(tasty, but not true) 
	
	BOOLEAN_NUMBER_A = case(
					  0   , 1
					, true, 42
				) # returns 42 as first condition is 0 (interpreted as boolean false)
	
	BOOLEAN_NUMBER_B = case(
					  8.8  , 88
					, true , 42
				) # returns 88 as first condition is non-zero (interpreted as boolean true)
							
	VALUE_IS_CONDITION=case(true, (99 >= 50)) # returns true
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("One beer for Franky!",  record.get("SIMPLE_A").getAsString());
		Assertions.assertEquals(1,  record.get("SIMPLE_B").getAsInt());
		Assertions.assertEquals(2,  record.get("SIMPLE_C").getAsInt());
		Assertions.assertEquals("YES!",  record.get("SLIGHTLY_COMPLEX").getAsString());
		
		Assertions.assertEquals(true, record.get("NULL").isJsonNull());
		Assertions.assertEquals(true, record.get("MISSING_A").isJsonNull());
		Assertions.assertEquals(true, record.get("MISSING_B").isJsonNull());
		
		Assertions.assertEquals(2, record.get("NOT_BOOLEAN").getAsInt());
		Assertions.assertEquals(42, record.get("BOOLEAN_NUMBER_A").getAsInt());
		Assertions.assertEquals(88, record.get("BOOLEAN_NUMBER_B").getAsInt());
		Assertions.assertEquals(true, record.get("VALUE_IS_CONDITION").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCeil() throws IOException {
		
		//---------------------------------
		String queryString ="""
| source empty records=1 
| set 
	POSITIVE=ceil(124.34567) 
	NEGATIVE=ceil(-42.34567)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(125, record.get("POSITIVE").getAsInt());
		Assertions.assertEquals(-42, record.get("NEGATIVE").getAsInt());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testClone() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1 
| set ARRAY=[55,66] OBJECT={x: 77, y:"eightyeight"} 
| set 
	ARRAY_CLONE=clone(ARRAY) 
	OBJECT_CLONE=clone(OBJECT) 
	STRING_CLONE=clone("uhyeahclonemerighttherebaby") 
	NUMBER_CLONE=clone(8008) 
	BOOL_CLONE=clone(true) 
	NULL_CLONE=clone(null)				
				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set FIRSTNAME="Aurora"
| set
	# Strings with Strings
	'S1' = contains(FIRSTNAME, "er") #false
	'S2' = contains("Her name is Aurora.", FIRSTNAME) # true
	
	# Strings with Booleans
	'B1' = contains("this is true", true) # true
	'B2' = contains("this is TRUE too", true) # true, as booleans are compared case insensitive
	'B3' = contains("this is not false, so it must be true", false) # true, as it contains false
	'B4' = contains("this is false", true) # false, "true" is not found
	
	# Strings with Numbers
	'N1' = contains("tiramisu counter: 42", 42) # true, string contains "42"	
	'N2' = contains("abc_123456_xyz", 5) # true, string contains "5"	
	'N3' = contains("we need 5.4 tiramisu", 5.4) # true
	'N4' = contains("Nightmare: -99999.9 Tiramisu", -99999.9) # true
	'N5' = contains("Nightmare: -99999.9 Tiramisu", -999) # true as well
	'N6' = contains("Nightmare: -99999.9 Tiramisu", 0.99) # false, "0.99" not found
	'N7' = contains("Reality: 1 Tiramisu", 12) # "12" is not found
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set NUMBER = 42
| set
	# Numbers with Numbers(are evaluated as strings)
	'N1' = contains(NUMBER, 42) # true
	'N2' = contains(123456, 34) # true, as compared as strings
	'N3' = contains(-789, -7) # true
	'N4' = contains(999.99, 9.9) # true
	'N5' = contains(8000.88, 1.8) # false
	
	# Numbers with Strings(are evaluated as strings)
	'S1' = contains(42, "42") # true
	'S2' = contains(123456, "34") # true, as compared as strings
	'S3' = contains(-789, "-7") # true
	'S4' = contains(999.99, "9.9") # true
	'S5' = contains(8000.88, "0.8") # false
	'S6' = contains(8000.88, "bla") # false
	
	# Numbers with Booleans(always false)
	'B1' = contains(42, true) # false
	'B2' = contains(1010101, false) # false
	'B3' = contains(0, false) # false
	'B4' = contains(1, true) # false				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set BOOLEAN = true
| set
	# Booleans with Booleans
	'B1' = contains(BOOLEAN, true) # true
	'B2' = contains(false, false) # true
	'B3' = contains(true, false) # false
	'B4' = contains(false , true) # false

	# Booleans with Strings
	'S1' = contains(true, "true") # true
	'S2' = contains(true, "TruE") # true
	'S3' = contains(false , "false") # also true
	'S4' = contains(false , "als") # true
	'S5' = contains(true , "maybe") # false
	
	# Booleans with Numbers(always false)
	'N1' = contains(true, 1) # false
	'N2' = contains(false, 0) # false
	'N3' = contains(true, 42) # false
	'N4' = contains(true, "0") # false				
		""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set
	# Arrays with Strings
	'S1' = contains(["a", "b", "c"], "a") # true
	'S2' = contains([true, "test", 123], "test") # true
	'S3' = contains(["a", "b", "c"], "A") # false, case sensitive
	'S4' = contains([false, "Hello World", 88], "Hello") # false, partial string
	'SX' = '||' # separator for table
	
	# Arrays with Booleans
	'B1' = contains([true, false], false) # true, array contains false
	'B2' = contains([1, true, "3"], true) # true, array contains true
	'B3' = contains([true, "false", 123], false) # true, string 'false' is considered boolean
	'B4' = contains([false, "TRUE", 123], true) # true, case insensitive
	'B5' = contains([true, true, true], false) # false, array dies not contain false
	'B6' = contains([], true) # false, array is empty
	'BX' = '||' # separator for table
	
	# Arrays with Numbers
	'N1' = contains([1, 2, 3], 2) # true
	'N2' = contains([4, 5, 6], "5") # true, ignores types
	'N3' = contains(["7", "8", "9"], 8) # true
	'N4' = contains([-2.2, -1.1, 0.01], -1.1) # true
	'N5' = contains([1 ,2 ,3], 0) # false
	'N6' = contains([-99.9, -88.8, 7.7], -9.9) # false
	'N7' = contains([], 0.99) # false
	'NX' = '||' # separator for table
	
	# Arrays with Null values
	'Z1' = contains([1, null, 3], null) # true
	'Z2' = contains([9, "null", 99], null) # true, ignores types
	'Z3' = contains(["a", null, "c"], "null") # true
	'Z4' = contains([], null) # false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);

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
		String queryString = """
| source empty records=1
| set
	# Objects with Strings
	'S1' = contains({"x": 1, "y": 2}, "x") # true, object contains field "x"
	'S2' = contains({"abc_xyz": 123}, "abc_") # false, only matches on full member names
	'S3' = contains({"1": "test", "2": "b"}, "test") # false, only checks member names, not values
	'SX' = '||' # separator for table
	
	# Objects with Booleans
	'B1' = contains({"true":  4242}, true) # true, object contains field "true"
	'B2' = contains({"false": 2424}, false) # true
	'B3' = contains({"TRUE":  4444}, true) # true, case insensitive
	'B4' = contains({"eurt":  2222}, true) # false
	'BX' = '||' # separator for table
	
	# Objects with Numbers
	'N1' = contains({"1":   'a'}, 1) # true
	'N2' = contains({"2":   'a'}, "2") # true
	'N3' = contains({"3.3": 'a'}, 3.3) # true
	'N4' = contains({"-4.4": 'a'}, -4.4) # true
	'N5' = contains({"-5.55":'a'}, -5.5) # false
	'N6' = contains({}, -0.99) # false
	'NX' = '||' # separator for table
	
	# Objects with Null values
	'Z1' = contains({"null":   'a'}, null) # true
	'Z2' = contains({"Null":   'a'}, null) # false, case sensitive
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
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
		String queryString = """
| source empty records=1
| set
	NULL_FIELD=null
| set
	# following returns null, as one or both of the required arguments are null
	'A1' = contains()
	'A2' = contains(NULL_FIELD)
	
	# following are true if value is either null or "null"(string)
	'B1' = contains(NULL_FIELD, null)
	'B2' = contains(NULL_FIELD, "null")
	
	# nulls compared
	'C1' = contains(null, null) # true
	'C2' = contains("contains null", null) # true
	'C3' = contains(null, "ll") # true, "null" contains "ll"
	'C4' = contains("NULL and Null are false", null) # false, only "null" is considered real null
	'C5' = contains("ll", null) # false, "ll" does not contain "null"
	'C6' = contains(null, true) # false on any boolean
	'C7' = contains(null, 0) # false on any number				
		""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
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
		String queryString = """
| source empty records=1 
| set 
	RADIANS=0.872665 # radians for 50 degress 
	DEGREES=50 
	COS_RADIANS=round(cos(RADIANS),3)  
	COS_DEGREES=round(cos(DEGREES,true),3)  
	# all following return 0 
	NOTHING_RETURNS_ZERO=cos()  
	RETURNS_ZERO_AGAIN=cos(null) 
	STRING_ZERO=cos('returns-0') 
	BOOL_ZERO=cos(true) 
					
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=10 
| set  
	#return a number that increases by 1 every call 
	INDEX=count() 
	COUNT_ARRAY=count([null, 1, true, "three"]) 
	COUNT_OBJECT=count({a: null, b: 1, c: true, d: "three"}) 
	COUNT_STRING=count("test") 
	COUNT_NUMBER=count(5) 
	COUNT_BOOL=count(true) 
	COUNT_NULL=count(null)				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(0, record.get("INDEX").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_ARRAY").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_OBJECT").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_STRING").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_NUMBER").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_BOOL").getAsInt());
		Assertions.assertEquals(true, record.get("COUNT_NULL").isJsonNull());
		
		//------------------------------
		// Check 2nd Query Result
		JsonObject secondRecord = queryResults.getRecordAsObject(1);
		Assertions.assertEquals(1, secondRecord.get("INDEX").getAsInt());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCount_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = """
source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 7.12345678}
	 ,{count: 3, value: 3, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	ALL=count() # returns 4
	COUNT_NONULL=count(value) # returns 3
	COUNT_NULLS=count(value,true) # returns 4
	COUNT_FLOAT=count(float) # returns 4""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(4, record.get("ALL").getAsInt());
		Assertions.assertEquals(3, record.get("COUNT_NONULL").getAsInt());
		Assertions.assertEquals(4,  record.get("COUNT_NULLS").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_FLOAT").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCountif() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 7.12345678}
	 ,{count: 3, value: 3, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| set
	ALL=countif() # returns 1,2,3,4
	COUNT_BIG=countif(count > 2) # returns 0,0,1.2
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		int index = 0;
		CFWQueryResult queryResults = resultArray.get(index);
		Assertions.assertEquals(4, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(1, record.get("ALL").getAsInt());
		Assertions.assertEquals(0, record.get("COUNT_BIG").getAsInt());

		
		//------------------------------
		// Check 2nd Query Result
		record = queryResults.getRecordAsObject(++index);
		Assertions.assertEquals(2, record.get("ALL").getAsInt());
		Assertions.assertEquals(0, record.get("COUNT_BIG").getAsInt());
		
		//------------------------------
		// Check 3rd Query Result
		record = queryResults.getRecordAsObject(++index);
		Assertions.assertEquals(3, record.get("ALL").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_BIG").getAsInt());
		
		//------------------------------
		// Check 4th Query Result
		record = queryResults.getRecordAsObject(++index);
		Assertions.assertEquals(4, record.get("ALL").getAsInt());
		Assertions.assertEquals(2, record.get("COUNT_BIG").getAsInt());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCountIf_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 7.12345678}
	 ,{count: 3, value: 3, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	ALL=countif() # returns 4
	COUNT_TRUE=countif(true) # returns 4
	COUNT_BIG=countif( count > 2 ) # returns 2
	COUNT_NULLS=countif( value == null ) # returns 1
	COUNT_FLOAT=countif(float > 5 AND float < 90) # returns 2
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(4, record.get("ALL").getAsInt());
		Assertions.assertEquals(4, record.get("COUNT_TRUE").getAsInt());
		Assertions.assertEquals(2,  record.get("COUNT_BIG").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_NULLS").getAsInt());
		Assertions.assertEquals(2, record.get("COUNT_FLOAT").getAsInt());

	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testCountnulls() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`[ 
	 {index: 0, array: [1,2,3], object: {a: 0, b: 1, c: 3 } } 
	,{index: 1, array: [null,null,3, null,5, null], object: {a: null, b: 22, c: null} } 
]` 
| set  
	COUNT_IS_ONE=countnulls(null) # returns 1 
	NULLS_IN_ARRAY=countnulls(array) # returns 0/4 
	NULLS_IN_OBJECT=countnulls(object) # returns 0/2 
	# every other value will result in count 0 
	NUMBER=countnulls(index) 
	BOOLEAN=countnulls(true) 
	STRING=countnulls("some_string") 
	# no params will result in returning null 
	UNCOUNTABLE=countnulls()
				
						""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(2, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(1, record.get("COUNT_IS_ONE").getAsInt());
		Assertions.assertEquals(0, record.get("NULLS_IN_ARRAY").getAsInt());
		Assertions.assertEquals(0, record.get("NULLS_IN_OBJECT").getAsInt());
		Assertions.assertEquals(0, record.get("NUMBER").getAsInt());
		Assertions.assertEquals(0, record.get("BOOLEAN").getAsInt());
		Assertions.assertEquals(0, record.get("STRING").getAsInt());
		Assertions.assertEquals(true, record.get("UNCOUNTABLE").isJsonNull());
		
		//------------------------------
		// Check 2nd Query Result
		JsonObject secondRecord = queryResults.getRecordAsObject(1);
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
	public void testCountnulls_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: null}
	 ,{count: 3, value: 3, float: null}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	ALL=countnulls() # returns 4
	COUNT_NULLS=countnulls(value) # returns 1
	COUNT_NULLS_FLOAT=countnulls(float) # returns 2
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(4, record.get("ALL").getAsInt());
		Assertions.assertEquals(1, record.get("COUNT_NULLS").getAsInt());
		Assertions.assertEquals(2,  record.get("COUNT_NULLS_FLOAT").getAsInt());

		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testDecode() throws IOException {
		
		//---------------------------------
		String original = "Space Lodash_ Equals= Other<>äÖü!?";
		String encoded = CFW.HTTP.encode(original);
		
		String queryString = """
				| source empty records=1  
				| set  
					# encode a string 
					ENCODED=encode('%s') 
					DECODED=decode(ENCODED)
				""".formatted(original)
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(original, record.get("DECODED").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testEarliest_and_EarliestSet() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = """
| source empty records=1
| globals format="yyyy-MM-dd'T'HH:mm:ss" # use globals or metadata to store formats and reuse them
| execute earliestSet(1693223296188)
| set
	epoch=earliest() # 1693223296188
	epochNull=earliest(null) # 1693223296188
	formatted=earliest(globals(format)) # 2023-08-28T11:48:16
	yearDayMonth=earliest("yyyy-MM-dd") #2023-08-28
	utcTime=earliest("HH:mm:ss", false)   # 11:48:16
	clientTime=earliest("HH:mm:ss", true) # 12:48:16
	Milliseconds=earliest("SSS")		 # 	188
	DayName=earliest("E / EEEE")         # Mon / Monday
	MonthName=earliest("MMM / MMMM")     # 	Aug / August
	Timezones=earliest("z / Z / ZZZZ", true)   # +01:00 / +0100 / GMT+01:00
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -60);
		
		Assertions.assertEquals(1, resultArray.size());
									
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		
		String queryString = """
				| source empty records=1  
				| set  
					# encode a string 
					ENCODED=encode('%s') 
					DECODED=decode(ENCODED)
				""".formatted(original)
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(encoded, record.get("ENCODED").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testExtract() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1
| set
	ID = '282c65a0-8b7b-437c-904' 
	URL = 'http://www.double-u-double-u-double-u.com/wewewe?id='+ID
| set
	MIDDLE=extract(ID, ".*?-(.*?-.*?)-.*") # returns 8b7b-437c
	HOST=extract(URL, ".*?www.(.*?).com.*") # returns double-u-double-u-double-u
	ID_FROM_URL=extract(URL, ".*?www.(.*?).com/.*?id=(.*)", 1) # returns 282c65a0-8b7b-437c-904
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("8b7b-437c", record.get("MIDDLE").getAsString());
		Assertions.assertEquals("double-u-double-u-double-u", record.get("HOST").getAsString());
		Assertions.assertEquals("282c65a0-8b7b-437c-904", record.get("ID_FROM_URL").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testExtractBounds() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records = 1
| keep ID
| set
	ID_PART 		= extractBounds(ID, '-', '-') 			# returns array with two parts, e.g. ["af14", "48ff"]
	START_END 		= extractBounds('-'+ID+'-', '-', '-') 	# Tip: add bounds to begin and end to match full string 
	NUMBER 			= extractBounds('<a href="./page/3">Next</a>', 'page/', '">Next') 		# returns "3"
	NUMBER_AS_WELL 	= extractBounds('<a href="./page/3">Next</a>', 'page/', '">Next')[0] 	# returns first result("3"), works also if not an array
	ARRAY 			= extractBounds('<p> <p>A</p> <p>BB</p> <p>CCC</p> </p>', '<p>', '</p>')# returns ["A", "BB", "CCC"]
	PARTIAL_MATCH	= extractBounds('a} {bb} ccc} {dddd}', '{', '}') # returns ["bb", "dddd"]
	NOT_FOUND 		= extractBounds('a b c', 'd', 'e') 	# returns null
	MISSING_PARAMS 	= extractBounds(' 1 2 ' , ' ') 		# returns null
	INPUT_NULL		= extractBounds(null , '1', '2') 	# returns null
	""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(2, record.get("ID_PART").getAsJsonArray().size());
		Assertions.assertEquals(4, record.get("START_END").getAsJsonArray().size());
		Assertions.assertEquals("3", record.get("NUMBER").getAsString());
		Assertions.assertEquals("3", record.get("NUMBER_AS_WELL").getAsString());
		Assertions.assertEquals("[\"A\",\"BB\",\"CCC\"]", record.get("ARRAY").toString());
		Assertions.assertEquals("[\"bb\",\"dddd\"]", record.get("PARTIAL_MATCH").toString());
		Assertions.assertEquals(true, record.get("NOT_FOUND").isJsonNull());
		Assertions.assertEquals(true, record.get("MISSING_PARAMS").isJsonNull());
		Assertions.assertEquals(true, record.get("INPUT_NULL").isJsonNull());


		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFields() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1
| set
	A = "22"
	B = "33"
	C = "44"
| set 
	ALL_FIELDS = fields() #["A", "B", "C", "ALL_FIELDS", "FILTERED_FIELDS"] - contains all as command detects fieldnames before executing 
	FILTERED_FIELDS = fields([FILTERED_FIELDS, ALL_FIELDS, B]) #["A", "C"]
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		// ALL_FIELDS = fields() #["A","B","C","ALL_FIELDS","FILTERED_FIELDS"] - contains all as command detects fieldnames before executing 
		// FILTERED_FIELDS = fields([FILTERED_FIELDS, ALL_FIELDS, B]) #["A","C"]
						
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("[\"A\",\"B\",\"C\",\"ALL_FIELDS\",\"FILTERED_FIELDS\"]", record.get("ALL_FIELDS").toString());
		Assertions.assertEquals("[\"A\",\"C\"]", record.get("FILTERED_FIELDS").toString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testFloor() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1
| set
	POSITIVE=floor(124.34567)
	NEGATIVE=floor(-42.34567)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(124, record.get("POSITIVE").getAsInt());
		Assertions.assertEquals(-43, record.get("NEGATIVE").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testGlobals() throws IOException {
		
		//---------------------------------
		String queryString = """
| globals 
	id=22
	name="Jane" 
| source empty records=1
| set
	ID   = globals(id)
	NAME = globals(name)
;
| source empty records=1
| set
	ID   = globals(id)
	NAME = globals(name)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(2, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(22, record.get("ID").getAsInt());
		Assertions.assertEquals("Jane", record.get("NAME").getAsString());
		
		//------------------------------
		// Check 2nd Query Result
		queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(22, record.get("ID").getAsInt());
		Assertions.assertEquals("Jane", record.get("NAME").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIf() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1
| set 
	LIKES_TIRAMISU = true
| set 
	SERVE = if(LIKES_TIRAMISU,"Tiramisu", "Ice Cream") # returns Tiramisu
	EMPTY_STRING = if(false,"Yes") # returns ""
	FALSE =if((1 == 2), true, false) # returns false
	NULL=if()  # returns null
	NULL_B=if(null)  # returns null if only one argument
	STRING_ONE=if("not a boolean") # returns null if only one argument
	STRING_TWO=if("not a boolean", 1, 2) # returns 2 as first param not boolean true
	ZERO_IS_FALSE=if(0, true, false) # returns false
	ONE_IS_TRUE=if(1, true, false) # returns true
	OTHERNUM_IS_TRUE=if(2, true, false) # returns true
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);

		Assertions.assertEquals("Tiramisu", record.get("SERVE").getAsString());
		Assertions.assertEquals("", record.get("EMPTY_STRING").getAsString());
		Assertions.assertEquals(false, record.get("FALSE").getAsBoolean());
		Assertions.assertEquals(true, record.get("NULL").isJsonNull());
		Assertions.assertEquals(true, record.get("NULL_B").isJsonNull());
		Assertions.assertEquals(true, record.get("STRING_ONE").isJsonNull());
		Assertions.assertEquals(2, record.get("STRING_TWO").getAsInt());
		Assertions.assertEquals(false, record.get("ZERO_IS_FALSE").getAsBoolean());
		Assertions.assertEquals(true, record.get("ONE_IS_TRUE").getAsBoolean());
		Assertions.assertEquals(true, record.get("OTHERNUM_IS_TRUE").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIndexOf_Strings() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	FUN_FACT ='The unicorn is the national animal of Scotland.'
| set
	############## SEARCH IN STRINGS  ###############
	'S0'  = indexof('not found', "hiding in the hiding hole") ## returns -1
	'S1'  = indexof(FUN_FACT , "e") # returns 2
	'S2'  = indexof(FUN_FACT , "e", 2) # returns 2
	'S3'  = indexof(FUN_FACT , "e", 3) # returns 17
	'S4'  = indexof('1234567890.6536', 45) ## returns 3
	'S5'  = indexof('34567890.6536', 90.65) ## returns 6
	'S6'  = indexof("test special... $?!% ...characters", "$?!% .") # returns 16
	'S7'  = indexof('object... {"x":1,"y":2}', {x:1, y:2}) ## returns 10
	'S8'  = indexof('array... [null,1,true,"three"]', [null, 1, true, "three"]) ## returns 9
	'S9'  = indexof('Null... null ', null) ## returns 8
	'S10'  = indexof(null, 'll') ## returns -1
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals("The unicorn is the national animal of Scotland.", record.get("FUN_FACT").getAsString());
		Assertions.assertEquals(-1, record.get("S0").getAsInt());
		Assertions.assertEquals(2, record.get("S1").getAsInt());
		Assertions.assertEquals(2, record.get("S2").getAsInt());
		Assertions.assertEquals(17, record.get("S3").getAsInt());
		Assertions.assertEquals(3, record.get("S4").getAsInt());
		Assertions.assertEquals(6, record.get("S5").getAsInt());
		Assertions.assertEquals(16, record.get("S6").getAsInt());
		Assertions.assertEquals(10, record.get("S7").getAsInt());
		Assertions.assertEquals(9, record.get("S8").getAsInt());
		Assertions.assertEquals(8, record.get("S9").getAsInt());
		Assertions.assertEquals(-1, record.get("S10").getAsInt());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIndexOf_Numbers() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	############## SEARCH IN NUMBERS ###############
	# numbers will be converted to strings
	'N0'  = indexof(12345, 7) ## returns -1
	'N1'  = indexof(123456789, 45) # returns 3
	'N2'  = indexof(987.6543 , 7.0) # returns 2
	'N3'  = indexof(78787878.78 , 78, 3) # returns 4
	'N4'  = indexof(5605605605.6056, ".") ## returns 10
	'N5'  = indexof(123012301.230, ".2") ## returns 9
	'N6'  = indexof(0, null) # returns -1
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(-1, record.get("N0").getAsInt());
		Assertions.assertEquals(3, record.get("N1").getAsInt());
		Assertions.assertEquals(2, record.get("N2").getAsInt());
		Assertions.assertEquals(4, record.get("N3").getAsInt());
		Assertions.assertEquals(10, record.get("N4").getAsInt());
		Assertions.assertEquals(9, record.get("N5").getAsInt());
		Assertions.assertEquals(-1, record.get("N6").getAsInt());
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIndexOf_Booleans() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	############## SEARCH IN NUMBERS ###############
	# booleans will be converted to strings
	'B0'  = indexof(true, false) ## returns -1
	'B1'  = indexof(true, true) # returns 0
	'B2'  = indexof(true, 'true') # returns 0
	'B3'  = indexof(false, 'false') # returns 0
	'B4'  = indexof(false, "lse") ## returns 2
	'B5'  = indexof(false, "not found") ## returns -1
	'B6'  = indexof(true, null) # returns -1
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(-1, 	record.get("B0").getAsInt());
		Assertions.assertEquals(0, 		record.get("B1").getAsInt());
		Assertions.assertEquals(0, 		record.get("B2").getAsInt());
		Assertions.assertEquals(0, 		record.get("B3").getAsInt());
		Assertions.assertEquals(2, 		record.get("B4").getAsInt());
		Assertions.assertEquals(-1, 	record.get("B5").getAsInt());
		Assertions.assertEquals(-1, 	record.get("B6").getAsInt());

	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIndexOf_Arrays() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	############## SEARCH IN ARRAYS ###############
	# the search is type-sensitive when searching in arrays (for performance reasons)
	'A0'  = indexof([1,2,3], 9) ## returns -1
	'A1'  = indexof([5,6,7], 6) # returns 1
	'A2'  = indexof([null, 1, true, "three"], true) # returns 2
	'A3'  = indexof([null, 1, true, "three"], 'true') # returns -1
	'A4'  = indexof([null, 1, true, "three"], null) # returns 0
	'A5'  = indexof([null, 1, true, "three"], 'null') # returns -1
	'A6'  = indexof([null, 1, true, "three"], 'three') # returns 3
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(-1, 	record.get("A0").getAsInt());
		Assertions.assertEquals(1, 		record.get("A1").getAsInt());
		Assertions.assertEquals(2, 		record.get("A2").getAsInt());
		Assertions.assertEquals(-1, 	record.get("A3").getAsInt());
		Assertions.assertEquals(0, 		record.get("A4").getAsInt());
		Assertions.assertEquals(-1, 	record.get("A5").getAsInt());
		Assertions.assertEquals(3, 		record.get("A6").getAsInt());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIndexOf_Objects() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	############## SEARCH IN OBJECTS ###############
	# the search is type-sensitive when searching in object(for performance reasons)
	# return value is either the name of the fields containing the value, or null if not found
	'JX'  = "|J|" ## visual separator
	'J0'  = indexof({x: 1, y:2, z:3}, 9) ## returns null
	'J1'  = indexof({x: 5, y:6, z:7}, 6) # returns 'y'
	'J2'  = indexof({'zero':'a', 'one':'b', 'two':'c', 'three':'d'}, 'c') # returns 'two'
	'J3'  = indexof({'zero':'a', 'one':'b', 'two':'c', 'three':'d'}, 'x') # returns null
	'J4'  = indexof({'zero': null, 'one':'b', 'two':'c'}, null) # returns 'zero'
	'J5'  = indexof({'zero': null, 'one':'b', 'two':'c'}, 'null') # returns null
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, 	record.get("J0").isJsonNull());
		Assertions.assertEquals("y", 	record.get("J1").getAsString());
		Assertions.assertEquals("two", 	record.get("J2").getAsString());
		Assertions.assertEquals(true, 	record.get("J3").isJsonNull());
		Assertions.assertEquals("zero", record.get("J4").getAsString());
		Assertions.assertEquals(true, 	record.get("J5").isJsonNull());
				
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsArray() throws IOException {
		
		String queryString = """
| source empty records=1
| set ARRAY_FIELD = [null, 1, true, "three"]
| set
	EMPTY = isArray() 				# returns null
	NULL = isArray(null) 			# returns false
	FIELD = isArray(ARRAY_FIELD) 	# returns true
	ARRAY = isArray([1, 2, 3]) 		# returns true
	ARRAY_EMPTY = isArray([]) 		# returns true
	STRING = isArray('["tiramisu"]')# returns false
	BOOLEAN = isArray(true) 		# returns false
	NUMBA = isArray(42) 			# returns false
	OBJECT = isArray( {"z": 34} ) 	# returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(false, record.get("NULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("FIELD").getAsBoolean());
		Assertions.assertEquals(true, record.get("ARRAY").getAsBoolean());
		Assertions.assertEquals(true, record.get("ARRAY_EMPTY").getAsBoolean());
		Assertions.assertEquals(false, record.get("STRING").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBA").getAsBoolean());
		Assertions.assertEquals(false, record.get("OBJECT").getAsBoolean());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsBoolean() throws IOException {
		
		String queryString = """
| source random records=1
| keep FIRSTNAME, LIKES_TIRAMISU
| set LIKES_TIRAMISU = true
| set
	EMPTY = isBoolean() 				# returns null
	NULL = isBoolean(null) 				# returns false
	FIELD = isBoolean(LIKES_TIRAMISU) 	# returns true
	BOOL_TRUE = isBoolean(true) 		# returns true
	BOOL_FALSE = isBoolean(false) 		# returns true
	STRING_A = isBoolean("false") 	 	# returns true
	STRING_B = isBoolean("true", false) # returns false
	NUMBA = isBoolean(42) 				# returns false
	OBJECT = isBoolean( {value: true} ) # returns false
	ARRAY = isBoolean( [true] ) 		# returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(false, record.get("NULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("FIELD").getAsBoolean());
		Assertions.assertEquals(true, record.get("BOOL_TRUE").getAsBoolean());
		Assertions.assertEquals(true, record.get("BOOL_FALSE").getAsBoolean());
		Assertions.assertEquals(true, record.get("STRING_A").getAsBoolean());
		Assertions.assertEquals(false, record.get("STRING_B").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBA").getAsBoolean());
		Assertions.assertEquals(false, record.get("OBJECT").getAsBoolean());
		Assertions.assertEquals(false, record.get("ARRAY").getAsBoolean());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsNull() throws IOException {
		
		String queryString = """
| source random records=1
| set
	EMPTY= isNull() 				 	 # returns null
	ISNULL = isNull(null) 				 # returns true
	ISNULL_FIELD = isNull(LIKES_TIRAMISU) # returns true or false
	ISEMPTY_A = isNull("") 				 # returns false
	ISEMPTY_B = isNull(trim("  	")) 	 # returns false
	NOTEMPTY = isNull(" ") 				 # returns false
	BOOLEAN = isNull(true) 				 # returns false
	NUMBER= isNull(123) 				 # returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);

		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(true, record.get("ISNULL").getAsBoolean());
		Assertions.assertEquals(false, record.get("ISEMPTY_A").getAsBoolean());
		Assertions.assertEquals(false, record.get("ISEMPTY_B").getAsBoolean());
		Assertions.assertEquals(false, record.get("NOTEMPTY").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBER").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsNullOrEmpty() throws IOException {
		
		String queryString = """
				| source empty records=1
				| set
	EMPTY= isNullOrEmpty() 						 # returns null
	ISNULL = isNullOrEmpty(null) 				 # returns true
	ISEMPTY_A = isNullOrEmpty("") 				 # returns true
	ISEMPTY_B = isNullOrEmpty(trim("  	")) 	 # returns true
	NOTEMPTY = isNullOrEmpty(" ") 				 # returns false
	BOOLEAN = isNullOrEmpty(true) 				 # returns false
	NUMBER= isNullOrEmpty(123) 					 # returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(true, record.get("ISNULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("ISEMPTY_A").getAsBoolean());
		Assertions.assertEquals(true, record.get("ISEMPTY_B").getAsBoolean());
		Assertions.assertEquals(false, record.get("NOTEMPTY").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBER").getAsBoolean());
		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsNumber() throws IOException {
		
		String queryString = """
| source random records=1
| keep FIRSTNAME, INDEX
| set
	EMPTY= isNumber() 				 	# returns null
	NULL = isNumber(null) 				# returns false
	FIELD = isNumber(INDEX) 			# returns true
	NUMBA = isNumber(42) 				# returns true
	STRING_A = isNumber("88") 	 		# returns true
	STRING_B = isNumber("88", false) 	# returns false
	BOOLEAN = isNumber(true) 			# returns false
	OBJECT = isNumber({value: 8008}) 	# returns false
	ARRAY = isNumber([222]) 			# returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(false, record.get("NULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("FIELD").getAsBoolean());
		Assertions.assertEquals(true, record.get("NUMBA").getAsBoolean());
		Assertions.assertEquals(true, record.get("STRING_A").getAsBoolean());
		Assertions.assertEquals(false, record.get("STRING_B").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("OBJECT").getAsBoolean());
		Assertions.assertEquals(false, record.get("ARRAY").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsObject() throws IOException {
		
		String queryString = """
| source empty records=1
| set OBJECT_FIELD = {a: null, b: 1, c: true, d: "three" }
| set
	EMPTY = isObject() 				# returns null
	NULL = isObject(null) 			# returns false
	FIELD = isObject(OBJECT_FIELD) 	# returns true
	OBJECT = isObject({a: 22}) 		# returns true
	OBJECT_EMPTY = isObject({}) 	# returns true
	STRING = isObject('{b: 99}') 	# returns false
	BOOLEAN = isObject(true) 		# returns false
	NUMBA = isObject(42) 			# returns false
	ARRAY = isObject( [true] ) 		# returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(false, record.get("NULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("FIELD").getAsBoolean());
		Assertions.assertEquals(true, record.get("OBJECT").getAsBoolean());
		Assertions.assertEquals(true, record.get("OBJECT_EMPTY").getAsBoolean());
		Assertions.assertEquals(false, record.get("STRING").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBA").getAsBoolean());
		Assertions.assertEquals(false, record.get("ARRAY").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsString() throws IOException {
		
		String queryString = """
| source empty records=1
| set STRING_FIELD = "I am a String"
| set
	EMPTY = isString() 				# returns null
	NULL = isString(null) 			# returns false
	FIELD = isString(STRING_FIELD ) # returns true
	STRING = isString('Yay!')		# returns false
	BOOLEAN = isString(true) 		# returns false
	NUMBA = isString(42) 			# returns false
	OBJECT = isString( {"z": 34} ) 	# returns false
	ARRAY = isString([1, 2, 3]) 	# returns false
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(true, record.get("EMPTY").isJsonNull());
		Assertions.assertEquals(false, record.get("NULL").getAsBoolean());
		Assertions.assertEquals(true, record.get("FIELD").getAsBoolean());
		Assertions.assertEquals(true, record.get("STRING").getAsBoolean());
		Assertions.assertEquals(false, record.get("BOOLEAN").getAsBoolean());
		Assertions.assertEquals(false, record.get("NUMBA").getAsBoolean());
		Assertions.assertEquals(false, record.get("OBJECT").getAsBoolean());
		Assertions.assertEquals(false, record.get("ARRAY").getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testIsUndef() throws IOException {
		
		String queryString = """
				| record
					[NAME, VALUE, LOCATION]
					["Treya", 42]
				| set 
					EVAL_VALUE = isUndef(VALUE)
					EVAL_LOCATION = isUndef(LOCATION)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		
		Assertions.assertEquals(false, record.get("EVAL_VALUE").getAsBoolean());
		Assertions.assertEquals(true, record.get("EVAL_LOCATION").getAsBoolean());

		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testLatest_and_LatestSet() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = """
| source empty records=1
| globals format="yyyy-MM-dd'T'HH:mm:ss" # use globals or metadata to store formats and reuse them
| execute latestSet(1651212296155)
| set
	epoch=latest() # 1651212296155
	epochNull=latest(null) # 1651212296155
	formatted=latest(globals(format)) # 2022-04-29T06:04:56
	yearDayMonth=latest("yyyy-MM-dd") # 2022-04-29
	utcTime=latest("HH:mm:ss", false)   # 06:04:56
	clientTime=latest("HH:mm:ss", true) # 07:04:56
	Milliseconds=latest("SSS")		   # 155
	DayName=latest("E / EEEE")         # Fri / Friday
	MonthName=latest("MMM / MMMM")     # Apr / April
	Timezones=latest("z / Z / ZZZZ", true)   # +01:00 / +0100 / GMT+01:00
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
									
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set 
	LENGTH_NOPARAM=length() # returns 0
	LENGTH_NULL=length(null) # returns null
	LENGTH_ARRAY=length([1, true, "three"]) # returns array size = 3
	LENGTH_OBJECT=length({one: 1, two: true, three: "three", four: 4}) # returns object member count = 4
	# all following return length of the string represenation of the value
	LENGTH_STRING=length("string") # returns 6
	LENGTH_BOOL=length(false) # returns 5
	LENGTH_INT=length(1234) # returns 4
	LENGTH_NEGATIVE=length(-1234) # returns 5
	LENGTH_FLOAT=length(1234.56) # returns 7
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
	public void testLiteral() throws IOException {
		
		//---------------------------------
		// Initialize
		String queryString = """
| source empty 
| set NAME = "Aurorania"
| set
	FIELDVALUE = "NAME" # Aurorania
	LITERAL = literal("NAME")  # NAME
	THE_NAME_AGAIN = "NAME" # Aurorania				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("Aurorania", record.get("NAME").getAsString());
		Assertions.assertEquals("Aurorania", record.get("FIELDVALUE").getAsString());
		Assertions.assertEquals("NAME", record.get("LITERAL").getAsString());
		Assertions.assertEquals("Aurorania", record.get("THE_NAME_AGAIN").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMax() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	 {index: 77
	 , array: [1,2,3,4, null, true, "string"]
	 , object: {a: 1, b: 2, c: 33, d: 4, e: null, f: true, string: "ignored"} }

]
`
| set 
	MAX_ARRAY=max(array) # returns 4
	MAX_OBJECT=max(object) # returns 33
	MAX_NUMBER=max(index) # returns same number = 77
	# all other return null
	MAX_ZERO=max()
	MAX_NULL=max(null)
	UNSUPPORTED_A=max(true)
	UNSUPPORTED_B=max("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	  {count: 1, value: 6, float: 1.33333333}
	 ,{count: 2, value: 5, float: 7.12345678}
	 ,{count: 3, value: 4, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	MAX=max(count)
	MAX_VALUE=max(value)
	MAX_FLOAT=max(float,true,5)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	 {index: 77
	 , array: [1,2,3,4, null, true, "string"]
	 , object: {a: 1, b: 2, c: 5, d: 4, e: null, f: true, string: "ignored"} }

]
`
| set 
	MEDIAN_ARRAY=median(array) # return 2.5
	MEDIAN_ARRAY_NULLS=median(array,true) # return 2
	MEDIAN_OBJECT=median(object) # return 3
	MEDIAN_OBJECT_NULLS=median(object,true) # return 2
	MEDIAN_NUMBER=median(index) # return 77
	MEDIAN_ZERO=median()
	MEDIAN_NULL=median(null)
	UNSUPPORTED_A=median(true)
	UNSUPPORTED_B=median("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("3", record.get("MEDIAN_ARRAY").toString());
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
		String queryString = """
| source json data=`
[
	  {count: 1, value: 9, float: 1.23}
	 ,{count: 2, value: 8, float: 2.34}
	 ,{count: 3, value: 7, float: 3.40}
	 ,{count: 4, value: 6, float: 4.55}
	 ,{count: 5, value: null, float: 6.77}
	 ,{count: null, value: null, float: 7.88}

]
`
| stats 
	MEDIAN=median(count)
	MEDIAN_NONULL=median(value)
	MEDIAN_NULLS=median(value,true)
	MEDIAN_FLOAT=median(float,true,5)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("3", record.get("MEDIAN").toString());
		Assertions.assertEquals("8", record.get("MEDIAN_NONULL").toString());
		Assertions.assertEquals("7", record.get("MEDIAN_NULLS").toString());
		Assertions.assertEquals("3.98", record.get("MEDIAN_FLOAT").getAsString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMeta() throws IOException {
		
		//---------------------------------
		String queryString = """
| metadata
	mySwitch=true
	herName='Jane'
	hisNumber=42
	yourNull=null
	theDogsArray=['throw', 'the', 'ball', 'woof!']
	theCatsObject={'I': 'am', 'your': 'god', 'pet': 'and worship me already so that this example stops getting utterly long!'}
| source empty records=1
| set
	LIGHTS_ON=meta(mySwitch) 
	LOVELY_PERSON=meta(herName)
	THE_WINNER_IS=meta(hisNumber)
	MOTHING_HERE=meta(yourNull)
	REQUIRED_ACTION=meta(theDogsArray)
	REQUEST_FOR_OBEDIENCE=meta(theCatsObject)
	OBEDIENCE_SCORE=meta(hisNumber) + 45 
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	 {index: 77
	 , array: [2,3,4,1, null, true, "string"]
	 , object: {a: 4, b: 2, c: 33, d: 0, e: null, f: true, string: "ignored"} }

]
`
| set 
	MIN_ARRAY=min(array) # returns 1
	MIN_OBJECT=min(object) # returns 0
	MIN_NUMBER=min(index) # returns same number = 77
	# all other return null
	MIN_ZERO=min()
	MIN_NULL=min(null)
	UNSUPPORTED_A=min(true)
	UNSUPPORTED_B=min("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source json data=`
[
	  {count: 1, value: 6, float: 1.33333333}
	 ,{count: 2, value: 5, float: 7.12345678}
	 ,{count: 3, value: 4, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	MIN=min(count)
	MIN_VALUE=min(value)
	MIN_FLOAT=min(float,true,5)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set
	NOW = now()
	NOW_OFFSET = now(null, -1, "h")
	NOW_FORMAT = now("YYYY-MM-dd")
	
			""";
		
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
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set 
EMPTY = null
THE_QUERIES_ID = "%s"
NULL_AGAIN = null
NULL_STRING = 'null'
STAYS_NULL = null	
| nullto 
   fields=[EMPTY, THE_QUERIES_ID , NULL_AGAIN, NULL_STRING ] # exclude STAYS_NULL 
   value="%s"
				""".formatted(theQueriesID, dangerZone)
			;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
	public void testParam() throws IOException {
		
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
	public void testPerc() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	 {index: 0, array: [1,2,3,4,5,6,7,8,9,10]
	 		  , object: {a:11, b:22, c:33, d:44, e:55, f:66, g:77, h:88, i:99, j:111 } 
	 }
	,{index: 1, array: [10,9,8,7,6,5,4,3,2,1,null,null,null,null,null,null,null,null,null,null,null,null,null]
			  , object: {a:11, b:22, c:33, d:44, e:55, f:66, g:77, h:88, i:99, j:111, k:null, l:null,m:null,n:null,o:null,p:null,q:null,r:null,s:null,t:null,u:null } 
	 }
	,{index: 3, array: [1,2,3,4,5,6,7,8,9,10, "StringsAndBooleans_ignored"]
			  , object: {a: 11, b: 22, c: 33, string: "ignored"} }
]
`
| set 
	'50Perc'=perc(array)	# returns 5 / 5 / 5
	'90Perc'=perc(array, 90) # returns 9 / 9 / 9
	'90PercNulls'=perc(array, 90, true) # returns 9 / 8 / 9
	'70PercObject'=perc(object, 70) # returns 77 / 77 / 33
	'70PercObjectNulls'=perc(object, 70,true) # Returns 77 / 44 / 33
	PERC_NUMBER=perc(index, 90) # returns 0 / 1 / 3
	# all below return null
	PERC_NONE=perc()
	PERC_NULL=perc(null)
	UNSUPPORTED_A=perc(true)
	UNSUPPORTED_B=perc("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
			
		
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		//------------------------------
		// Check First Query Result
		JsonObject record = queryResults.getRecordAsObject(0);
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
		record = queryResults.getRecordAsObject(1);
		Assertions.assertEquals(5, record.get("50Perc").getAsInt());
		Assertions.assertEquals(9, record.get("90Perc").getAsInt());
		Assertions.assertEquals(8, record.get("90PercNulls").getAsInt());
		Assertions.assertEquals(77, record.get("70PercObject").getAsInt());
		Assertions.assertEquals(44, record.get("70PercObjectNulls").getAsInt());
		Assertions.assertEquals(1, record.get("PERC_NUMBER").getAsInt());
		
		//------------------------------
		// Check Third Query Result
		record = queryResults.getRecordAsObject(2);
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
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 2.12345678}
	 ,{count: 3, value: 3, float: 3.123456}
	 ,{count: 4, value: 4, float: 4.65365}
	 ,{count: 5, value: 5, float: 5.76476}
	 ,{count: 6, value: null, float: 6.7647}
	 ,{count: 7, value: null, float: 7.989896}
	 ,{count: 8, value: null, float: 8.5653}
	 ,{count: 9, value: null, float: 9.6525}
	 ,{count: 10, value: null, float: 10.65635}

]
`
| stats 
	PERC=perc(count, 80) # returns 8
	PERC_NONULL=perc(value, 80) # returns 4
	PERC_NULLS=perc(value, 80, true) # returns 3
	PERC_FLOAT=perc(float, 80) # returns 8.5653
			""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString ="""
| source empty records= 100
| set
PERCENT = random()
ZERO_ONE = random(0,1)
ONE = random(1,1)
HUNDRED = random(100) #second param is default 100 
MINUS = random(-10, 10)
HALF_TO_FULL_MILLION = random((10^6)/2, 10^6)
				"""
				;
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString ="""
| source empty records= 100
| set
  PERCENT= randomFloat() # zero to 1
  ZERO_FIFTY = randomFloat(0,50)
  ONE = randomFloat(1) #second param is default 1
  NINTY_NINE= randomFloat(99, 99) 
  MINUS = randomFloat(-10, 10)
  HALF_TO_FULL_MILLION = randomFloat((10^6)/2, 10^6)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecordAsObject(0);
			CFWTestUtils.assertIsBetween(0, 1, record.get("PERCENT").getAsFloat() );
			CFWTestUtils.assertIsBetween(0, 55, record.get("ZERO_FIFTY").getAsFloat() );
			Assertions.assertEquals(1, record.get("ONE").getAsFloat() );
			Assertions.assertEquals(99, record.get("NINTY_NINE").getAsFloat() );
			CFWTestUtils.assertIsBetween(-10, 10, record.get("MINUS").getAsFloat() );
			CFWTestUtils.assertIsBetween(500000, 1000000, record.get("HALF_TO_FULL_MILLION").getAsFloat() );
		}
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testRandomFrom() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records= 100
| set
  NULL = randomFrom() # null
  A_OR_B= randomFrom([\"A\", \"B\"])
  ONE = randomFrom(1) # return same value
  A_OR_B_OR_ONE = randomFrom([A_OR_B, ONE])
  X_OR_Y = randomFrom({X: 22, Y: 33})				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Iterate all Query Results
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(100, queryResults.getRecordCount());
		
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records = 1
| set VALUE="Alejandra 1234 #!<>?=() 1234"
| set
	NULL = replace() # returns null
	SAME = replace(VALUE) # returns Alejandra 1234 #!<>?=() 1234
	REMOVE = replace(VALUE, " 1234") # returns Alejandra #!<>?=()
	REPLACE = replace(VALUE, "a 1234 #", "o Sanchez ") # returns Alejandro Sanchez !<>?=() 1234
	REPLACE_MULTI = replace(VALUE, "1234", "42") # returns Alejandra 42 #!<>?=() 42
	# booleans and number inputs will be treated as string
	BOOL = replace(true, "true", "maybe") # returns maybe
	NUMBER = replace(181818, "8", "-eight-") # returns 1-eight-1-eight-1-eight-
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
				
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| set FLOATYFLOAT = 42.4
| set
	ZERO=round() 
	INT_DOWN=round(FLOATYFLOAT) 
	INT_UP=round(42.5) 
	PRECISION_TWO=round(55.555,2) 
	PRECISION_THREE=round(44.44444444,3) 
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
					
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1 
| set 
	RADIANS=0.872665 # radians for 50 degress 
	DEGREES=50 
	SIN_RADIANS=round(sin(RADIANS),3)  
	SIN_DEGREES=round(sin(DEGREES,true),3)  
	# all following return 0 
	NOTHING_RETURNS_ZERO=sin()  
	RETURNS_ZERO_AGAIN=sin(null) 
	STRING_ZERO=sin('returns-0') 
	BOOL_ZERO=sin(true) 
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
	public void testSplit() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[TESTFIELD]
	["Tira Misu"]
| set 
	DEFAULT = split() 					# returns null
	SAME = split(TESTFIELD) 			# returns ["Tira Misu"]
	FIELD = split(TESTFIELD, " ") 		# returns ["Tira", "Misu"]
	REGEX = split("A.B;C.D", "[.;]") 	# returns ["A", "B", "C", "D"]
	NUMBER = split(44.33, "[.]") 		# returns ["44", "33"]
	BOOL = split(false, "l") 			# returns ["fa", "se"]
	ARRAY = split([1, 2, '3,4'], ",") 	# returns ["[1", "2", "\"3", "4\"]"]
	OBJECT = split({a: true}, ":") 		# returns ["[1", "2", "\"3", "4\"]"]
	NULL = split(null, "l") 			# returns ["{\"a\"", "true}"]
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(true							, record.get("DEFAULT").isJsonNull());
		Assertions.assertEquals("[\"Tira Misu\"]"				, record.get("SAME").toString());
		Assertions.assertEquals("[\"Tira\",\"Misu\"]"			, record.get("FIELD").toString());
		Assertions.assertEquals("[\"A\",\"B\",\"C\",\"D\"]"		, record.get("REGEX").toString());
		Assertions.assertEquals("[\"44\",\"33\"]"				, record.get("NUMBER").toString());
		Assertions.assertEquals("[\"fa\",\"se\"]"				, record.get("BOOL").toString());
		Assertions.assertEquals("[\"[1\",\"2\",\"\\\"3\",\"4\\\"]\"]" , record.get("ARRAY").toString());
		Assertions.assertEquals("[\"{\\\"a\\\"\",\"true}\"]"	, record.get("OBJECT").toString());
		Assertions.assertEquals(true							, record.get("NULL").isJsonNull());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testStdev() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	 {index: 0, array: [1,2,3], object: {a: 0, b: -1, c: 3 } }
	,{index: 1, array: [1,null,3], object: {a: 11, b: 22, c: null} }
	,{index: 2, array: [null,null,3], object: {a: null, b: 22, c: null} }
	,{index: 3, array: [null,null,null], object: {a: null, b: null, c: null} }
	,{index: 3, array: [1,2,3, "StringsAndBooleans_ignored"], object: {a: 11, b: 22, c: 33, string: "ignored"} }
]
`
| set 
	# returns the standard deviation(population) for the values in the array 
	STDEV_ARRAY=stdev(array)
	# returns the standard deviation(population) for the values in the array, nulls will be threated as zero
	STDEV_ARRAY_NULLS=stdev(array, true)
	# returns the standard deviation(population) for the values of all fields in the object, nulls will be ignored, precision of the result will be 5
	STDEV_OBJECT=stdev(object, false, 5)
	# returns the standard deviation(sample) for the values of all fields in the object, nulls will be threated as zeros, precision of the result will be 2
	STDEV_OBJECT_NULLS=stdev(object, true, 2, false)
	# if input is a single number, returns 0, as there is no deviation from a single number
	STDEV_NUMBER=stdev(index)
	# following will return null
	STDEV_ZERO=stdev()
	STDEV_NULL=stdev(null)
	UNSUPPORTED_A=stdev(true)
	UNSUPPORTED_B=stdev("some_string")
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(5, queryResults.getRecordCount());
		
		//------------------------------
		// Check First Query Result
		int i = -1;
		JsonObject record = queryResults.getRecordAsObject(++i);
		Assertions.assertEquals("0.816", record.get("STDEV_ARRAY").getAsString());
		Assertions.assertEquals("0.816", record.get("STDEV_ARRAY_NULLS").getAsString());
		Assertions.assertEquals("1.69967", record.get("STDEV_OBJECT").getAsString());
		Assertions.assertEquals("2.08", record.get("STDEV_OBJECT_NULLS").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_NUMBER").getAsString());

		Assertions.assertTrue(record.get("STDEV_ZERO").isJsonNull());
		Assertions.assertEquals(0, record.get("STDEV_NULL").getAsInt());
		Assertions.assertEquals(0, record.get("UNSUPPORTED_A").getAsInt());
		Assertions.assertEquals(0, record.get("UNSUPPORTED_B").getAsInt());
		
		//------------------------------
		// Check Second Query Result
		record = queryResults.getRecordAsObject(++i);
		Assertions.assertEquals("1.000", record.get("STDEV_ARRAY").getAsString());
		Assertions.assertEquals("1.247", record.get("STDEV_ARRAY_NULLS").getAsString());
		Assertions.assertEquals("5.50000", record.get("STDEV_OBJECT").getAsString());
		Assertions.assertEquals("11.00", record.get("STDEV_OBJECT_NULLS").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_NUMBER").getAsString());

		//------------------------------
		// Check Third Query Result
		record = queryResults.getRecordAsObject(++i);
		Assertions.assertEquals("0.000", record.get("STDEV_ARRAY").getAsString());
		Assertions.assertEquals("1.414", record.get("STDEV_ARRAY_NULLS").getAsString());
		Assertions.assertEquals("0.00000", record.get("STDEV_OBJECT").getAsString());
		Assertions.assertEquals("12.70", record.get("STDEV_OBJECT_NULLS").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_NUMBER").getAsString());
		
		//------------------------------
		// Check Forth Query Result
		record = queryResults.getRecordAsObject(++i);
		Assertions.assertEquals("0.000", record.get("STDEV_ARRAY").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_ARRAY_NULLS").getAsString());
		Assertions.assertEquals("0.00000", record.get("STDEV_OBJECT").getAsString());
		Assertions.assertEquals("0.00", record.get("STDEV_OBJECT_NULLS").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_NUMBER").getAsString());
		
		
		//------------------------------
		// Check Fifth Query Result
		record = queryResults.getRecordAsObject(++i);
		Assertions.assertEquals("0.816", record.get("STDEV_ARRAY").getAsString());
		Assertions.assertEquals("0.816", record.get("STDEV_ARRAY_NULLS").getAsString());
		Assertions.assertEquals("8.98146", record.get("STDEV_OBJECT").getAsString());
		Assertions.assertEquals("11.00", record.get("STDEV_OBJECT_NULLS").getAsString());
		Assertions.assertEquals("0.000", record.get("STDEV_NUMBER").getAsString());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testStdev_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 2.12345678}
	 ,{count: 3, value: 3, float: 3.123456}
	 ,{count: 4, value: 4, float: 4.65365}
	 ,{count: 5, value: 5, float: 5.76476}
	 ,{count: 6, value: null, float: 6.7647}
	 ,{count: 7, value: null, float: 7.989896}
	 ,{count: 8, value: null, float: 8.5653}
	 ,{count: 9, value: null, float: 9.6525}
	 ,{count: 10, value: null, float: 10.65635}

]
`
| stats 
	"STDEV"=stdev(value) # ignore nulls, 3 decimal precision
	"STDEV2DEC"=stdev(value, true, 2) # count nulls as 0, 2 decimals
	"STDEV_ZERO_DEC"=stdev(value, false, 0) # ignore nulls, no decimals
	"STDEV_SAMPLE"=stdev(value, false, 5, false) # ignore nulls, 5 decimals, use sample formula
			""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("1.414", record.get("STDEV").getAsString());
		Assertions.assertEquals("1.80", record.get("STDEV2DEC").getAsString());
		Assertions.assertEquals("1", record.get("STDEV_ZERO_DEC").getAsString());
		Assertions.assertEquals("1.58114", record.get("STDEV_SAMPLE").getAsString());	
		
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSumIf() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=10
| set
	# all return null as not supported
	NULL_A = sumif(VALUE, (VALUE > 50) ) 
	NULL_B = sumif(VALUE) 
	NULL_C = sumif()
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(10, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(true, record.get("NULL_A").isJsonNull());
		Assertions.assertEquals(true, record.get("NULL_B").isJsonNull());
		Assertions.assertEquals(true, record.get("NULL_C").isJsonNull());

		
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSumIf_Aggr() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json data=`
[
	  {count: 1, value: 1, float: 1.33333333}
	 ,{count: 2, value: 2, float: 7.12345678}
	 ,{count: 3, value: 3, float: 99.123456}
	 ,{count: 4, value: null, float: 22}

]
`
| stats 
	SUM_ZERO=sumif() # returns 0
	SUM_ALL=sumif(count) # returns 10
	SUM_NONULL=sumif(value) # returns 6
	SUM_BIGFLOAT=sumif(float, float > 10 ) # returns 	121.123456
	SUM_FLOAT=sumif(float, (float > 5 AND float < 90) ) # returns 29.12345678
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("0", record.get("SUM_ZERO").getAsString());
		Assertions.assertEquals("10", record.get("SUM_ALL").getAsString());
		Assertions.assertEquals("6", record.get("SUM_NONULL").getAsString());
		Assertions.assertEquals("121.123456", record.get("SUM_BIGFLOAT").getAsString());
		Assertions.assertEquals("29.12345678", record.get("SUM_FLOAT").getAsString());
		
	}
	

	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTan() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1 
| set 
	RADIANS=0.872665 # radians for 50 degress 
	DEGREES=50 
	TAN_RADIANS=round(tan(RADIANS),3)  
	TAN_DEGREES=round(tan(DEGREES,true),3)  
	# all following return 0 
	NOTHING_RETURNS_ZERO=tan()  
	RETURNS_ZERO_AGAIN=tan(null) 
	STRING_ZERO=tan('returns-0') 
	BOOL_ZERO=tan(true) 				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
		String queryString = """
| source empty records=1
| globals 
	epochMillis=1693829701889 # 2023-09-04T12:15:01
	format="yyyy-MM-dd'T'HH:mm:ss" # use globals or metadata to store formats and reuse them
	
| set
	formatted=timeformat(globals(format), globals(epochMillis)) # returns 2023-09-04T12:15:01
	yearDayMonth=timeformat("yyyy-MM-dd", globals(epochMillis)) # returns 2023-09-04
	utcTime=timeformat("HH:mm:ss", globals(epochMillis))   # returns 12:15:01
	clientTime=timeformat("HH:mm:ss", globals(epochMillis), true) # returns 14:15:01
	millis=timeformat("SSS", globals(epochMillis))		 # returns 889
	DayName=timeformat("E / EEEE", globals(epochMillis))         # # returns "Mon / Monday"
	MonthName=timeformat("MMM / MMMM", globals(epochMillis))     # # returns "Sep / September"
	Timezones=timeformat("z / Z / ZZZZ", globals(epochMillis), true)   # returns "+02:00 / +0200 / GMT+02:00"
	# all following return null
	epochNoParams=timeformat() 
	epochFormatOnly=timeformat("yyyy-MM-dd")
	epochFormatNull=timeformat(null, globals(epochMillis)) 
	epochTimeNull=timeformat("yyyy-MM-dd", null) 				
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
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
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTimeround() throws IOException {
		
		//---------------------------------
		String queryString = """
| global
	format = "yyyy-MM-dd HH:mm:ss"
| source empty
| set
	TIME = timeparse(globals(format), "2022-07-26 21:17:03") 
	ONE_MIN = timeround(TIME) # returns 2022-07-26 22:17:00
	FIVE_MIN = timeround(TIME, 5, 'm') # returns 2022-07-26 22:15:00
	TWO_HOURS = timeround(TIME, 2, 'h') # returns 2022-07-26 22:00:00
	SEVEN_DAYS = timeround(TIME, 7, 'd') # returns 2022-07-29 00:00:00
	ZE_MONTH = timeround(TIME, 1, 'M') # returns 2022-06-30 00:00:00
| set # format on server-side to have no issues with timezone differences
	TIME = timeformat(globals(format), TIME)
	ONE_MIN = timeformat(globals(format), ONE_MIN)
	FIVE_MIN = timeformat(globals(format), FIVE_MIN)
	TWO_HOURS = timeformat(globals(format), TWO_HOURS)
	SEVEN_DAYS = timeformat(globals(format), SEVEN_DAYS)
	ZE_MONTH = timeformat(globals(format), ZE_MONTH)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
							
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("2022-07-26 21:17:03", record.get("TIME").getAsString());
		Assertions.assertEquals("2022-07-26 21:17:00", record.get("ONE_MIN").getAsString());
		Assertions.assertEquals("2022-07-26 21:15:00", record.get("FIVE_MIN").getAsString());
		Assertions.assertEquals("2022-07-26 22:00:00", record.get("TWO_HOURS").getAsString());
		Assertions.assertEquals("2022-07-29 00:00:00", record.get("SEVEN_DAYS").getAsString());
		Assertions.assertEquals("2022-06-30 00:00:00", record.get("ZE_MONTH").getAsString());
	
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTimetruncate() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty records=1
| metadata name="Test"
| set
	TIME = 1682016652888  # millis for 2023-04-20 18:50:52.888 UTC
	SEC = timetruncate(TIME, "s") # millis for 2023-04-20 18:50:52.000 UTC
	MIN = timetruncate(TIME, "m") # millis for 2023-04-20 18:50:00.000 UTC
	HOUR= timetruncate(TIME, "h") # millis for 2023-04-20 18:00:00.000 UTC
	DAY = timetruncate(TIME, "d") # millis for 2023-04-20 00:00:00.000 UTC
	MONTH = timetruncate(TIME, "M") # millis for 2023-03-31 00:00:00 UTC
	YEAR = timetruncate(TIME, "y") # millis for 2023-03-31 00:00:00 UTC
| set #using timeformat with clienttimezone = false to show UTC
	TIME = timeformat("yyyy-MM-dd HH:mm:ss.SSS", TIME, false)
	SEC = timeformat("yyyy-MM-dd HH:mm:ss.SSS", SEC, false)
	MIN = timeformat("yyyy-MM-dd HH:mm:ss", MIN, false)
	HOUR= timeformat("yyyy-MM-dd HH:mm", HOUR, false)
	DAY = timeformat("yyyy-MM-dd HH:mm", DAY, false)
	MONTH = timeformat("yyyy-MM-dd HH:mm", MONTH, false)
	YEAR = timeformat("yyyy-MM-dd HH:mm", YEAR, false)
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("2023-04-20 18:50:52.888", record.get("TIME").getAsString());
		Assertions.assertEquals("2023-04-20 18:50:52.000", record.get("SEC").getAsString());
		Assertions.assertEquals("2023-04-20 18:50:00", record.get("MIN").getAsString());
		Assertions.assertEquals("2023-04-20 18:00", record.get("HOUR").getAsString());
		Assertions.assertEquals("2023-04-20 00:00", record.get("DAY").getAsString());
		Assertions.assertEquals("2023-03-31 00:00", record.get("MONTH").getAsString());
		Assertions.assertEquals("2022-12-31 00:00", record.get("YEAR").getAsString());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testUserdata() throws IOException {

		//---------------------------------
		// Le Query
		String queryString = """
| source empty
| set
	currentuser = userdata() # return an object with data of current user
	byUsername = userdata("testuser") # return data by username
	byEmail = userdata("test@te.st") # return data by email
	username = userdata().username # directly get the username
	id = userdata().id # directly get the username
	firstname = userdata().firstname # directly get the username
	lastname = userdata().lastname # directly get the username
	email = userdata().email # directly get the username
				""";
		
		//---------------------------------
		// Prepare ze Session
		CFWSessionData data = new CFWSessionData("pseudoSessionID");
		
		User user = new User()
						.id(42)
						.username("testuser")
						.firstname("Testina")
						.lastname("Testonia")
						.email("test@te.st");
		
		data.setUser(user);
		
		CFW.Context.Request.setSessionData(data);
		
		//---------------------------------
		// Executione
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, -120);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals(true, record.get("currentuser").isJsonObject());
		System.out.println(CFW.JSON.toJSON(record.get("currentuser")));
		Assertions.assertEquals(true, record.get("byUsername").isJsonObject());
		Assertions.assertEquals(true, record.get("byEmail").isJsonObject());
		Assertions.assertEquals(42, record.get("id").getAsInt());
		Assertions.assertEquals("testuser", record.get("username").getAsString());
		Assertions.assertEquals("Testina", record.get("firstname").getAsString());
		Assertions.assertEquals("Testonia", record.get("lastname").getAsString());
		Assertions.assertEquals("test@te.st", record.get("email").getAsString());
		
	}
	
}

package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartJsonMemberAccess;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWQueryParts extends DBTestMaster {
	
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static long earliest = new Instant().minus(1000*60*30).getMillis();
	private static long latest = new Instant().getMillis();
	
	@BeforeAll
	public static void setup() {
		CFW.Files.addAllowedPackage("com.xresch.cfw.tests.features.query.testdata");
				
		context.setEarliest(earliest);
		context.setLatest(latest);
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartValue() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		//-------------------------------
		// Null
		//-------------------------------
		QueryPartValue part = QueryPartValue.newNull();

		Assertions.assertTrue(part.isNull());
		Assertions.assertFalse(part.isBoolean());
		Assertions.assertFalse(part.isBooleanString());
		Assertions.assertFalse(part.isString());
		Assertions.assertFalse(part.isJson());
		Assertions.assertFalse(part.isInteger());
		Assertions.assertFalse(part.isNumber());
		Assertions.assertEquals(null, part.getAsNumber());
		Assertions.assertEquals(null, part.getAsString());
		Assertions.assertEquals(false, part.getAsBoolean());
		Assertions.assertEquals(JsonNull.INSTANCE, part.getAsJsonElement());
		
		//-------------------------------
		// Number
		//-------------------------------
		part = QueryPartValue.newNumber(new BigDecimal("12.1"));

		Assertions.assertFalse(part.isNull());
		Assertions.assertFalse(part.isBoolean());
		Assertions.assertFalse(part.isBooleanString());
		Assertions.assertFalse(part.isString());
		Assertions.assertFalse(part.isJson());
		Assertions.assertFalse(part.isInteger());
		Assertions.assertTrue(part.isNumber());
		Assertions.assertEquals(12.1f, part.getAsNumber().floatValue());
		Assertions.assertEquals("12.1", part.getAsString());
		Assertions.assertEquals(true, part.getAsBoolean());
		Assertions.assertEquals(12.1f, part.getAsJsonElement().getAsFloat());
	
		//-------------------------------
		// Integer
		//-------------------------------
		part = QueryPartValue.newNumber(8008);
		
		Assertions.assertFalse(part.isNull());
		Assertions.assertFalse(part.isBoolean());
		Assertions.assertFalse(part.isBooleanString());
		Assertions.assertFalse(part.isString());
		Assertions.assertFalse(part.isJson());
		Assertions.assertTrue(part.isInteger());
		Assertions.assertTrue(part.isNumber());
		Assertions.assertEquals(8008, part.getAsNumber().intValue());
		Assertions.assertEquals("8008", part.getAsString());
		Assertions.assertEquals(true, part.getAsBoolean());
		Assertions.assertEquals(8008, part.getAsJsonElement().getAsInt());
		
		//-------------------------------
		// Boolean
		//-------------------------------
		part = QueryPartValue.newBoolean(false);

		Assertions.assertFalse(part.isNull());
		Assertions.assertTrue(part.isBoolean());
		Assertions.assertFalse(part.isBooleanString());
		Assertions.assertFalse(part.isString());
		Assertions.assertFalse(part.isJson());
		Assertions.assertFalse(part.isInteger());
		Assertions.assertFalse(part.isNumber());
		Assertions.assertEquals(false, part.getAsBoolean());
		Assertions.assertEquals("false", part.getAsString());
		Assertions.assertEquals(0, part.getAsNumber());
		Assertions.assertEquals(false, part.getAsJsonElement().getAsBoolean());
		
		//-------------------------------
		// String
		//-------------------------------
		part = QueryPartValue.newString("False");

		Assertions.assertFalse(part.isNull());
		Assertions.assertFalse(part.isBoolean());
		Assertions.assertTrue(part.isBooleanString());
		Assertions.assertTrue(part.isString());
		Assertions.assertFalse(part.isJson());
		Assertions.assertFalse(part.isInteger());
		Assertions.assertFalse(part.isNumber());
		Assertions.assertEquals("False", part.getAsString());
		Assertions.assertEquals(false, part.getAsBoolean());
		// NumberFormatException
		// Assertions.assertEquals(0, part.getAsNumber());
		Assertions.assertEquals(false, part.getAsJsonElement().getAsBoolean());
		Assertions.assertEquals("False", part.getAsJsonElement().getAsString());
		//-------------------------------
		// Json Element
		//-------------------------------
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		
		part = QueryPartValue.newJson(object);

		Assertions.assertFalse(part.isNull());
		Assertions.assertFalse(part.isBoolean());
		Assertions.assertFalse(part.isBooleanString());
		Assertions.assertFalse(part.isString());
		Assertions.assertTrue(part.isJson());
		Assertions.assertFalse(part.isInteger());
		Assertions.assertFalse(part.isNumber());
		Assertions.assertEquals("value", part.getAsJsonElement().getAsJsonObject().get("key").getAsString());
		Assertions.assertEquals("{\"key\":\"value\"}", part.getAsString());
		// UnsupportedOperationException
		// Assertions.assertEquals(false, part.getAsBoolean());
		// Assertions.assertEquals(0, part.getAsNumber());
		// Assertions.assertEquals(false, part.getAsJson().getAsBoolean());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartArray() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		
		//-------------------------------
		// Check Array Values
		//-------------------------------
		ArrayList<QueryPart> parts = new ArrayList<>();
		parts.add(QueryPartValue.newNull());
		parts.add(QueryPartValue.newBoolean(false));
		parts.add(QueryPartValue.newNumber(42));
		
		QueryPartArray arrayPart = new QueryPartArray(context, parts);
		
		arrayPart.add(QueryPartValue.newString("YEEEHAAAA!!!"));
		
		JsonArray array = arrayPart.getAsJsonArray(new EnhancedJsonObject(), true);
		
		Assertions.assertFalse(arrayPart.isIndex(null));
		Assertions.assertEquals(4, array.size());
		
		Assertions.assertTrue(array.get(0).isJsonNull());
		Assertions.assertFalse(array.get(1).getAsBoolean());
		Assertions.assertEquals(42, array.get(2).getAsInt());
		Assertions.assertEquals("YEEEHAAAA!!!", array.get(3).getAsString());
		
		//-------------------------------
		// Check Index
		//-------------------------------
		arrayPart = new QueryPartArray(context);
		arrayPart.add(QueryPartValue.newNumber(8008));
		
		Assertions.assertTrue(arrayPart.isIndex(null));
		Assertions.assertEquals(8008, arrayPart.getIndex(null));
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_getValue() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		JsonObject object = 
				CFW.JSON.fromJson(
						CFW.Files.readPackageResource("com.xresch.cfw.tests.features.query.testdata", "testQueryPartJsonMemberAccess_Object.json")
				).getAsJsonObject();
		
		QueryPart level1, level2, level3;


		//-------------------------------
		// Check MemberAccess Values
		//-------------------------------
		level1 = QueryPartValue.newString("memba");
		level2 = QueryPartValue.newString("submemba");
		
		QueryPart memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, level2);
		
		Assertions.assertEquals("memba.submemba", memberAccessPart.determineValue(null).getAsString());
		
		Assertions.assertEquals("Itse mee, Mario!", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
				
		//-------------------------------
		// Check third level
		//-------------------------------
		level1 = QueryPartValue.newString("memba");
		level2 = QueryPartValue.newString("anothermemba");
		level3 = QueryPartValue.newString("numba");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		Assertions.assertEquals(42, 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsInteger()
			);
		
		//-------------------------------
		// Check Array Access
		//-------------------------------
		level1 = QueryPartValue.newString("array");
		level2 = new QueryPartArray(context, 1);

		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, level2);
		
		Assertions.assertEquals("B", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
		
		//-------------------------------
		// Check Object Array, Third Level
		//-------------------------------
		level1 = QueryPartValue.newString("objectArray");
		level2 = new QueryPartArray(context, 1);
		level3 = QueryPartValue.newString("key");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		Assertions.assertEquals("valueB", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_setValue() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		JsonObject object = 
				CFW.JSON.fromJson(
						CFW.Files.readPackageResource("com.xresch.cfw.tests.features.query.testdata", "testQueryPartJsonMemberAccess_Object.json")
				).getAsJsonObject();
		
		QueryPart level1, level2, level3;


		//-------------------------------
		// Check Set Value
		//-------------------------------
		level1 = QueryPartValue.newString("memba");
		level2 = QueryPartValue.newString("submemba");
		
		QueryPart memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, level2);
		
		Assertions.assertEquals("memba.submemba", memberAccessPart.determineValue(null).getAsString());
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Itse not-e mee, itse Luigi!"));
		
		Assertions.assertEquals("Itse not-e mee, itse Luigi!", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
				
		//-------------------------------
		// Set value on third level
		//-------------------------------
		level1 = QueryPartValue.newString("memba");
		level2 = QueryPartValue.newString("anothermemba");
		level3 = QueryPartValue.newString("numba");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive(99.9f));
		
		Assertions.assertEquals(99.9f, 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsFloat()
			);
		
		//-------------------------------
		// Set Value in Array
		//-------------------------------
		level1 = QueryPartValue.newString("array");
		level2 = new QueryPartArray(context, 1);

		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, level2);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Bla"));
		
		Assertions.assertEquals("Bla", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
		
		//-------------------------------
		// Set value in Object Array, Third Level
		//-------------------------------
		level1 = QueryPartValue.newString("objectArray");
		level2 = new QueryPartArray(context, 1);
		level3 = QueryPartValue.newString("key");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
				QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
			);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Be the value"));
		
		Assertions.assertEquals("Be the value", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
		
		//-------------------------------
		// Set value in Object Array, Third Level
		//-------------------------------
		level1 = QueryPartValue.newString("objectArray");
		level2 = new QueryPartArray(context, 1);
		level3 = QueryPartValue.newString("key");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Be the value"));
		
		Assertions.assertEquals("Be the value", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
				
		//-------------------------------
		// Set value in not existing context
		//-------------------------------
		level1 = QueryPartValue.newString("newEntry");
		level2 = QueryPartValue.newString("newSubEntry");
		level3 = QueryPartValue.newString("key");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("a new value"));
		
		Assertions.assertEquals("a new value", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
		
		//-------------------------------
		// Set value in not existing array
		//-------------------------------
		level1 = QueryPartValue.newString("newArray");
		level2 = new QueryPartArray(context, 0);
		level3 = QueryPartValue.newString("key");
		
		memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, 
					QueryPartJsonMemberAccess.createMemberAccess(context, level2, level3)
				);
		
		((QueryPartJsonMemberAccess)memberAccessPart).setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("a new value"));
		
		Assertions.assertEquals("a new value", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_usingFunctions() throws IOException {
		
		//---------------------------------
		String queryString = """
| paramdefaults
	object = {"test": "hello"}
	array = ["hello", "World"]
	favoriteField = "favorite"
	objectField = "object"
| source empty
| set
	OBJECT = param(object).test 				# returns "hello"
	OBJECT_B = param(object)[test]				# returns "hello"
	OBJECT_C = param(object).[test]				# returns "hello"
	ARRAY = param(array)[1] 					# returns "World"
	ARRAY_B = param(array).[1] 					# returns "World"
	temp = {"favorite": "tiramisu", "object": { "sub": 42  } }
	FUNC_ACCESS = temp.param(favoriteField)			# returns "tiramisu"
	FUNC_SUBACCESS = temp.param(objectField).sub	# returns 42	
				""";
		
		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Query Result
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);
		Assertions.assertEquals("hello", record.get("OBJECT").getAsString());
		Assertions.assertEquals("hello", record.get("OBJECT_B").getAsString());
		Assertions.assertEquals("hello", record.get("OBJECT_C").getAsString());
		Assertions.assertEquals("World", record.get("ARRAY").getAsString());
		Assertions.assertEquals("World", record.get("ARRAY_B").getAsString());
		Assertions.assertEquals("tiramisu", record.get("FUNC_ACCESS").getAsString());
		Assertions.assertEquals(42, record.get("FUNC_SUBACCESS").getAsInt());

		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_accessMemberFromArray() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json
	data = {
		array: [ 
			{ name: "Alejandra", subarray: [
				{subname: "Martinez"}
			] }
		],
		arrayInArray: [
			["one", "two"]
		],
		object: {"favorite": "tiramisu", "object": { "sub": 42  } }
		
	}
| set
	name = array[0].name	# returns Alejandra
	subname = array[0].subarray[0].subname	# returns Martinez
	multiarray = arrayInArray[0][1]	# returns two
	functionArray = literal( ["x","y","z"] )[1] 	# returns y
	fieldObjectArray = literal( [{tira: "misu"}] )[0]	# returns 	{"tira":"misu"}
	functionArrayMember = literal( [{tira: "misu"}] )[0].tira	# returns misu
	functionField =  literal( {tira: "misu2"} )[tira]	# returns misu2
	functionDotField = literal( {tira: "misu3"} ).[tira]	# returns misu3
	arrayDotFunction = array[0].literal("name")	# returns Alejandra
	arrayDotFunctionExtreme = array[0].literal("subarray")[0].subname	# returns Martinez
				""";
		
		CFWQueryResultList resultArray = 
				new CFWQueryExecutor()
					.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		//------------------------------
		// Check First Query Result
		queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		Assertions.assertEquals(1, resultArray.size());
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);		
		Assertions.assertEquals("Alejandra", record.get("name").getAsString() );
		Assertions.assertEquals("Martinez", record.get("subname").getAsString() );
		Assertions.assertEquals("two", record.get("multiarray").getAsString() );
		Assertions.assertEquals("y", record.get("functionArray").getAsString() );
		Assertions.assertEquals("{\"tira\":\"misu\"}", record.get("fieldObjectArray").toString() );
		Assertions.assertEquals("misu", record.get("functionArrayMember").getAsString() );
		Assertions.assertEquals("misu2", record.get("functionField").getAsString() );
		Assertions.assertEquals("misu3", record.get("functionDotField").getAsString() );
		Assertions.assertEquals("Alejandra", record.get("arrayDotFunction").getAsString() );
		Assertions.assertEquals("Martinez", record.get("arrayDotFunctionExtreme").getAsString() );
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_accessMemberFromArrayAndConcat() throws IOException {
		
		//---------------------------------
		String queryString = """
| source json
	data = {
		array: [ 
			{ name: "Hera", subarray: [
				{subname: "Jones"}
			] }
		],
		arrayInArray: [
			["one", "two"]
		],
		object: {"favorite": "tiramisu", "object": { "sub": 42  } }
		
	}
| set
	front = "@" + array[0].name	# returns @Hera
	back = array[0].name + "@" 	# returns Hera@
	both = "@" + array[0].name + "@" 	# returns @Hera@
	subname = "@" + array[0].subarray[0].subname + "@" # returns @Jones@
	multiarray = "@" + arrayInArray[0][1] + "@"	# returns @two@
	functionArray = "@" + literal( ["x","y","z"] )[1] + "@" 	# returns @y@
	fieldObjectArray = "@" + literal( [{tira: "misu"}] )[0] + "@" # returns 	@{"tira":"misu"}@
	functionArrayMember = "@" + literal( [{tira: "misu"}] )[0].tira + "@" 	# returns @misu@
	functionField =  "@" + literal( {tira: "misu1"} ).tira + "@" 	# returns @misu1@
	functionFieldArray =  "@" + literal( {tira: "misu2"} )[tira] + "@" 	# returns @misu2@
	functionDotField = "@" + literal( {tira: "misu3"} ).[tira] + "@" 	# returns @misu3@
	arrayDotFunction = "@" +  array[0].literal("name") + "@" 	# returns @Hera@
	arrayDotFunctionExtreme = "@" + array[0].literal("subarray")[0].subname + "@"	# returns @Jones@
				""";
		
		CFWQueryResultList resultArray = 
				new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		//------------------------------
		// Check First Query Result
		queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		Assertions.assertEquals(1, resultArray.size());
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);		
		Assertions.assertEquals("@Hera", record.get("front").getAsString() );
		Assertions.assertEquals("Hera@", record.get("back").getAsString() );
		Assertions.assertEquals("@Hera@", record.get("both").getAsString() );
		Assertions.assertEquals("@Jones@", record.get("subname").getAsString() );
		Assertions.assertEquals("@two@", record.get("multiarray").getAsString() );
		Assertions.assertEquals("@y@", record.get("functionArray").getAsString() );
		Assertions.assertEquals("@{\"tira\":\"misu\"}@", record.get("fieldObjectArray").getAsString() );
		Assertions.assertEquals("@misu@", record.get("functionArrayMember").getAsString() );
		Assertions.assertEquals("@misu1@", record.get("functionField").getAsString() );
		Assertions.assertEquals("@misu2@", record.get("functionFieldArray").getAsString() );
		Assertions.assertEquals("@misu3@", record.get("functionDotField").getAsString() );
		Assertions.assertEquals("@Hera@", record.get("arrayDotFunction").getAsString() );
		Assertions.assertEquals("@Jones@", record.get("arrayDotFunctionExtreme").getAsString() );
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartJsonMemberAccess_accessArrayAndAssignValue() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records = 3
| keep FIRSTNAME, LASTNAME
| globals ARRAY = []
| set 
	g(ARRAY)[count()] = "A"+count() 
	length = length( g(ARRAY) )
	value =  g(ARRAY)[count()]
				""";
		
		CFWQueryResultList resultArray = 
				new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		//------------------------------
		// Check First Query Result
		queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		Assertions.assertEquals(1, resultArray.size());
		Assertions.assertEquals(3, queryResults.getRecordCount());
		
		int i = -1;
		//------------------------------
		// First Record
		JsonObject record = queryResults.getRecordAsObject(++i);		
		Assertions.assertEquals(1, record.get("length").getAsInt() );
		Assertions.assertEquals("A0", record.get("value").getAsString() );
		
		//------------------------------
		// 2nd Record
		record = queryResults.getRecordAsObject(++i);		
		Assertions.assertEquals(2, record.get("length").getAsInt() );
		Assertions.assertEquals("A1", record.get("value").getAsString() );
		
		//------------------------------
		// 3rd Record
		record = queryResults.getRecordAsObject(++i);		
		Assertions.assertEquals(3, record.get("length").getAsInt() );
		Assertions.assertEquals("A2", record.get("value").getAsString() );

		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartAssignment() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		JsonObject object = 
				CFW.JSON.fromJson(
						CFW.Files.readPackageResource("com.xresch.cfw.tests.features.query.testdata", "testQueryPartJsonMemberAccess_Object.json")
				).getAsJsonObject();
		
		QueryPart level1, level2, level3;
		QueryPartAssignment assignment;

		//-------------------------------
		// Check Set Value to MemberName(String)
		//-------------------------------
		level1 = QueryPartValue.newString("newMemba");
		

		assignment = new QueryPartAssignment(context
				, level1
				, QueryPartValue.newString("Itse not-e mee, itse Luigi!")
			);
		
		assignment.assignToJsonObject(new EnhancedJsonObject(object));
		
		Assertions.assertEquals("Itse not-e mee, itse Luigi!", 
				object.get(level1.toString()).getAsString()
			);
		
		//-------------------------------
		// Check Set Value to MemberName(Integer)
		//-------------------------------
		level1 = QueryPartValue.newNumber(10);
		

		assignment = new QueryPartAssignment(context
				, level1
				, QueryPartValue.newBoolean(false)
			);
		
		assignment.assignToJsonObject(new EnhancedJsonObject(object));
		
		System.out.println(CFW.JSON.toJSON(object));
		Assertions.assertEquals(false, 
				object.get(level1.toString()).getAsBoolean()
			);
		
		
		//-------------------------------
		// Check Set Value to MemberName(Boolean)
		//-------------------------------
		level1 = QueryPartValue.newBoolean(true);
		

		assignment = new QueryPartAssignment(context
				, level1
				, QueryPartValue.newNumber(123)
			);
		
		assignment.assignToJsonObject(new EnhancedJsonObject(object));
		
		System.out.println(CFW.JSON.toJSON(object));
		Assertions.assertEquals(123, 
				object.get(level1.toString()).getAsInt()
			);
		
		//-------------------------------
		// Check Set Value to JsonMemberAccess
		//-------------------------------
		level1 = QueryPartValue.newString("memba");
		level2 = QueryPartValue.newString("submemba");
		
		QueryPart memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, level1, level2);
		
		Assertions.assertEquals("memba.submemba", memberAccessPart.determineValue(null).getAsString());
		
		assignment = new QueryPartAssignment(context, memberAccessPart, QueryPartValue.newString("Itse not-e mee, itse Luigi!"));
		
		assignment.assignToJsonObject(new EnhancedJsonObject(object));
		
		Assertions.assertEquals("Itse not-e mee, itse Luigi!", 
				memberAccessPart.determineValue(
					new EnhancedJsonObject(object)
				).getAsString()
			);
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartBinaryExpressionCompareStrings() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		QueryPartBinaryExpression expression;
		QueryPartValue evaluationResult;
		
		//-------------------------------
		// Positive Test Expression ==
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testEqual"), 
						CFWQueryTokenType.OPERATOR_EQUAL_EQUAL,
						QueryPartValue.newString("testEqual"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
	
		//-------------------------------
		// Negative Test Expression ==
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testNotEqual"), 
						CFWQueryTokenType.OPERATOR_EQUAL_EQUAL,
						QueryPartValue.newString("test!="));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression !=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testEqual"), 
						CFWQueryTokenType.OPERATOR_EQUAL_NOT,
						QueryPartValue.newString("testEqual"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
	
		//-------------------------------
		// Negative Test Expression !=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testNotEqual"), 
						CFWQueryTokenType.OPERATOR_EQUAL_NOT,
						QueryPartValue.newString("test!="));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression = (Contains)
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testContains"), 
						CFWQueryTokenType.OPERATOR_EQUAL,
						QueryPartValue.newString("estCo"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
	
		//-------------------------------
		// Negative Test Expression = (Contains)
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("testContains"), 
						CFWQueryTokenType.OPERATOR_EQUAL,
						QueryPartValue.newString("notC"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartBinaryExpressionCompareStringAndNumber() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		QueryPartBinaryExpression expression;
		QueryPartValue evaluationResult;
		
		//-------------------------------
		// Positive Test Expression ==
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_EQUAL,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression == Reverse
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newNumber(1234.5), 
						CFWQueryTokenType.OPERATOR_EQUAL_EQUAL,
						QueryPartValue.newString("1234.5"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Negative Test Expression ==
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_EQUAL,
						QueryPartValue.newNumber(9234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		//=================================================================
		
		//-------------------------------
		// Positive Test Expression !=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_NOT,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
	
		//-------------------------------
		// Negative Test Expression !=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_NOT,
						QueryPartValue.newNumber(9234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//=================================================================
		
		//-------------------------------
		// Positive Test Expression >= 
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER,
						QueryPartValue.newNumber(1234.1));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression >= Equals
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression >= Both Strings
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER,
						QueryPartValue.newString("1234.1"));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Negative Test Expression >=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER,
						QueryPartValue.newNumber(1234.9));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		//=================================================================
		
		//-------------------------------
		// Positive Test Expression <= 
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.1"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Positive Test Expression <= Equals
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Negative Test Expression <=
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER,
						QueryPartValue.newNumber(1234.1));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		
		//=================================================================
		
		//-------------------------------
		// Positive Test Expression >
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_GREATERTHEN,
						QueryPartValue.newNumber(1234.1));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Negative Test Expression > (Equals)
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_GREATERTHEN,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
				
		//=================================================================
		
		//-------------------------------
		// Positive Test Expression < 
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.1"), 
						CFWQueryTokenType.OPERATOR_LOWERTHEN,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------
		// Negative Test Expression < (Equals)
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("1234.5"), 
						CFWQueryTokenType.OPERATOR_LOWERTHEN,
						QueryPartValue.newNumber(1234.5));
		
		evaluationResult = expression.determineValue(null);
		
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartBinaryExpressionCalculateStringAndNumber() throws IOException {
		
		CFWQueryContext context = new CFWQueryContext();
		
		QueryPartBinaryExpression expression;
		QueryPartValue evaluationResult;
		
		//-------------------------------
		// Test Expression +
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("156.459"), 
						CFWQueryTokenType.OPERATOR_PLUS,
						QueryPartValue.newNumber(123300.33));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isNumber());
		Assertions.assertEquals(123456.789,evaluationResult.getAsDouble());
		
		//-------------------------------
		// Test Expression -
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("234567.89"), 
						CFWQueryTokenType.OPERATOR_MINUS,
						QueryPartValue.newNumber(111111.11));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isNumber());
		Assertions.assertEquals(123456.78, evaluationResult.getAsDouble());
		
		//-------------------------------
		// Test Expression *
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("-345.176"), 
						CFWQueryTokenType.OPERATOR_MULTIPLY,
						QueryPartValue.newNumber(6.42));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isNumber());
		Assertions.assertEquals(-2216.02992, evaluationResult.getAsDouble());
		
		//-------------------------------
		// Test Expression /
		//-------------------------------
		expression =
				new QueryPartBinaryExpression(context,
						QueryPartValue.newString("-2216.02992"), 
						CFWQueryTokenType.OPERATOR_DIVIDE,
						QueryPartValue.newNumber(-345.176));
		
		evaluationResult = expression.determineValue(null);
		
		Assertions.assertTrue(evaluationResult.isNumber());
		Assertions.assertEquals(6.42, evaluationResult.getAsDouble());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartBinaryExpressionOperatorPrecedence() throws IOException {
		
		//---------------------------------
		String queryString = """
| source empty
| set
	addition 		= 1 + 2 + 4.5	# returns 7.5
	subtraction 	= 10 - 4 - 2.5	# returns 3.5
	addsub 			= 12 + 4 - 3.5 	# returns 12.5
	multiplication 	= 2 * 2 * 3  	# returns 12
	division		= 90 / 3 / 6  	# returns 5
	addmulti		= 2 + 4 * 8   	# returns 34
	adddiv			= 2 + 8 / 4   	# returns 4
	submulti		= 2 - 4 * 8   	# returns -30
	subdiv			= 2 - 8 / 4   	# returns 0
	mixed			= 2*8 - 2*3  	# returns 10
	modulofirst		= 5 % 6 - 3		# returns 2
	modulolast		= 5 - 6 % 4  	# returns 3
	power			= 2 ^ 3 ^ 3     # returns 512
	powermulti		= 2 ^ 3 * 4     # returns 32
	powerplus		= 2 ^ 3 + 4     # returns 12
	powerpluspower	= 2^3 + 4^4     # returns 264
	group			= (2 + 8) / 2   # returns 5
	group2			= (2 * 8) - 3   # returns 13
	extreme 		= 2^(2 * 2 + 1) 
					  * 
					  (4/2) - 1 	# returns 63 >> (2^5) * 2 = 32*2 - 1 
				""";
		
		CFWQueryResultList resultArray = 
				new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		//------------------------------
		// Check First Query Result
		queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		Assertions.assertEquals(1, resultArray.size());
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);		
		Assertions.assertEquals("7.5", record.get("addition").getAsString() );
		Assertions.assertEquals("3.5", record.get("subtraction").getAsString() );
		Assertions.assertEquals("12.5", record.get("addsub").getAsString() );
		Assertions.assertEquals(12  , record.get("multiplication").getAsInt() );
		Assertions.assertEquals(5   , record.get("division").getAsInt() );
		Assertions.assertEquals(34  , record.get("addmulti").getAsInt() );
		Assertions.assertEquals(4   , record.get("adddiv").getAsInt() );
		Assertions.assertEquals(-30 , record.get("submulti").getAsInt() );
		Assertions.assertEquals(0   , record.get("subdiv").getAsInt() );
		Assertions.assertEquals(10  , record.get("mixed").getAsInt() );
		Assertions.assertEquals(2   , record.get("modulofirst").getAsInt() );
		Assertions.assertEquals(3   , record.get("modulolast").getAsInt() );
		Assertions.assertEquals(512 , record.get("power").getAsInt() );
		Assertions.assertEquals(32  , record.get("powermulti").getAsInt() );
		Assertions.assertEquals(12  , record.get("powerplus").getAsInt() );
		Assertions.assertEquals(264 , record.get("powerpluspower").getAsInt() );
		Assertions.assertEquals(5   , record.get("group").getAsInt() );
		Assertions.assertEquals(13  , record.get("group2").getAsInt() );



		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testQueryPartBinaryExpression_WithBraces() throws IOException {
		
		//---------------------------------
		String queryString = """
| record
	[GROUP, VALUE] 
	['A', 1024]
| set 
	MINUS_BRACE 		= ( VALUE - 4 )				# returns 1020
	MINUS_BRACE_REVERSE = (  2034 - VALUE )			# returns 1010
	MINUS_BRACKET		= [ VALUE - 24 ] 			# returns [1000]
	MINUS_BRACKET_BRACE = [ ( VALUE - 34 ) ]		# returns [990]
	MINUS_MADNESS 		= ([ ((( VALUE - 44 ))) ])	# returns [980]
	
	PLUS_BRACE 			= ( VALUE + 6 )				# returns 1030
	PLUS_BRACE_REVERSE 	= (  16 + VALUE )			# returns 1040
	PLUS_BRACKET 		= [ VALUE + 26 ] 			# returns [1050]
	PLUS_BRACKET_BRACE 	= [ ( VALUE + 36 ) ]		# returns [1060]
	PLUS_MADNESS 		= ([ ((( VALUE + 46 ))) ])	# returns [1070]
	
	DIVIDE_BRACE 			= ( VALUE / 1024 )			# returns 1
	DIVIDE_BRACE_REVERSE 	= (  2048 / VALUE )			# returns 2
	DIVIDE_BRACKET 			= [ VALUE / 512 ] 			# returns [2]
	DIVIDE_BRACKET_BRACE 	= [ ( VALUE / 256 ) ]		# returns [4]
	DIVIDE_MADNESS 			= ([ ((( VALUE / 128 ))) ])	# returns [8]

	MULTIPLY_BRACE 			= ( VALUE * 1 )					# returns 1024
	MULTIPLY_BRACE_REVERSE 	= (  0.5 * VALUE )				# returns 512
	MULTIPLY_BRACKET 		= [ VALUE * 0.25 ] 				# returns [256]
	MULTIPLY_BRACKET_BRACE 	= [ ( VALUE * 0.125 ) ]			# returns [128]
	MULTIPLY_MADNESS		= ([ ((( VALUE * 0.0625 ))) ])	# returns [64]

	MODULO_BRACE 			= ( VALUE % 924 )			# returns 100
	MODULO_BRACE_REVERSE 	= (  200 % VALUE )			# returns 200
	MODULO_BRACKET 			= [ VALUE % 724 ] 			# returns [300]
	MODULO_BRACKET_BRACE 	= [ ( VALUE % 624 ) ]		# returns [400]
	MODULO_MADNESS 			= ([ ((( VALUE % 524 ))) ])	# returns [500]
				""";
		
		CFWQueryResultList resultArray = 
				new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		CFWQueryResult queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		//------------------------------
		// Check First Query Result
		queryResults = resultArray.get(0);
		System.out.println(CFW.JSON.toJSONPretty(queryResults.getRecords()));
		
		Assertions.assertEquals(1, resultArray.size());
		Assertions.assertEquals(1, queryResults.getRecordCount());
		
		JsonObject record = queryResults.getRecordAsObject(0);		
		Assertions.assertEquals("1020", record.get("MINUS_BRACE").getAsString());
		Assertions.assertEquals("1010", record.get("MINUS_BRACE_REVERSE").getAsString());
		Assertions.assertEquals("[1000]", CFW.JSON.toJSON(record.get("MINUS_BRACKET")));
		Assertions.assertEquals("[990]", CFW.JSON.toJSON(record.get("MINUS_BRACKET_BRACE")));
		Assertions.assertEquals("[980]", CFW.JSON.toJSON(record.get("MINUS_MADNESS")));
		
		Assertions.assertEquals("1030", record.get("PLUS_BRACE").getAsString());
		Assertions.assertEquals("1040", record.get("PLUS_BRACE_REVERSE").getAsString());
		Assertions.assertEquals("[1050]", CFW.JSON.toJSON(record.get("PLUS_BRACKET")));
		Assertions.assertEquals("[1060]", CFW.JSON.toJSON(record.get("PLUS_BRACKET_BRACE")));
		Assertions.assertEquals("[1070]", CFW.JSON.toJSON(record.get("PLUS_MADNESS")));
		
		Assertions.assertEquals("1.000", record.get("DIVIDE_BRACE").getAsString());
		Assertions.assertEquals("2.000", record.get("DIVIDE_BRACE_REVERSE").getAsString());
		Assertions.assertEquals("[2.000]", CFW.JSON.toJSON(record.get("DIVIDE_BRACKET")));
		Assertions.assertEquals("[4.000]", CFW.JSON.toJSON(record.get("DIVIDE_BRACKET_BRACE")));
		Assertions.assertEquals("[8.000]", CFW.JSON.toJSON(record.get("DIVIDE_MADNESS")));
		
		Assertions.assertEquals("1024", record.get("MULTIPLY_BRACE").getAsString());
		Assertions.assertEquals("512.0", record.get("MULTIPLY_BRACE_REVERSE").getAsString());
		Assertions.assertEquals("[256.00]", CFW.JSON.toJSON(record.get("MULTIPLY_BRACKET")));
		Assertions.assertEquals("[128.000]", CFW.JSON.toJSON(record.get("MULTIPLY_BRACKET_BRACE")));
		Assertions.assertEquals("[64.0000]", CFW.JSON.toJSON(record.get("MULTIPLY_MADNESS")));
		
		Assertions.assertEquals("100", record.get("MODULO_BRACE").getAsString());
		Assertions.assertEquals("200", record.get("MODULO_BRACE_REVERSE").getAsString());
		Assertions.assertEquals("[300]", CFW.JSON.toJSON(record.get("MODULO_BRACKET")));
		Assertions.assertEquals("[400]", CFW.JSON.toJSON(record.get("MODULO_BRACKET_BRACE")));
		Assertions.assertEquals("[500]", CFW.JSON.toJSON(record.get("MODULO_MADNESS")));
        
	}
	
	
}

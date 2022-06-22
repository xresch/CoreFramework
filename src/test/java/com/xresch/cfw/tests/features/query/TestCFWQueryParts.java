package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartJsonMemberAccess;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class TestCFWQueryParts {
	
	@BeforeAll
	public static void setup() {
		CFW.Files.addAllowedPackage("com.xresch.cfw.tests.features.query.testdata");
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
		part = QueryPartValue.newNumber(12.1);

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
		
		Assertions.assertFalse(arrayPart.isIndex());
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
		
		Assertions.assertTrue(arrayPart.isIndex());
		Assertions.assertEquals(8008, arrayPart.getIndex());
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
		
		QueryPartJsonMemberAccess memberAccessPart = new QueryPartJsonMemberAccess(context, level1, level2);
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
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

		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, level2);
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
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
		
		QueryPartJsonMemberAccess memberAccessPart = new QueryPartJsonMemberAccess(context, level1, level2);
		
		Assertions.assertEquals("memba.submemba", memberAccessPart.determineValue(null).getAsString());
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Itse not-e mee, itse Luigi!"));
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
				);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive(99.9f));
		
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

		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, level2);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Bla"));
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
				);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Be the value"));
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
				);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("Be the value"));
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
				);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("a new value"));
		
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
		
		memberAccessPart = new QueryPartJsonMemberAccess(context, level1, 
					new QueryPartJsonMemberAccess(context, level2, level3)
				);
		
		memberAccessPart.setValueOfMember(new EnhancedJsonObject(object), new JsonPrimitive("a new value"));
		
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
		
		QueryPartJsonMemberAccess memberAccessPart = new QueryPartJsonMemberAccess(context, level1, level2);
		
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
	
	
}

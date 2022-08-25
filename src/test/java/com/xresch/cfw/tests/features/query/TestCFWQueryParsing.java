package com.xresch.cfw.tests.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.CFWQueryParser.CFWQueryParserContext;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWQueryParsing extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static String jsonTestData;
	private static String sourceString ;
	private static final String PACKAGE = "com.xresch.cfw.tests.features.query.testdata";
	
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE);
		jsonTestData =	CFW.Files.readPackageResource(PACKAGE, "SourceJsonTestdata.json");
		jsonTestData = jsonTestData.replace("'", "\'");
		sourceString = "source json data='"+jsonTestData+"'";

		context.setEarliest(new Instant().minus(1000*60*30).getMillis());
		context.setLatest(new Instant().getMillis());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private void printResults(String title, ArrayList<CFWQueryToken> results) {
		System.out.println("\n======================= "+title+" =======================");
		for(CFWQueryToken token : results) {
			System.out.println(CFW.JSON.toJSON(token));
		}
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSimpleSourceQuery() throws ParseException {
		
		//String queryString = "source random records=100";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(sourceString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testSimpleSourceQuery", results);
		
		CFWQueryParser parser = new CFWQueryParser(sourceString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());
		
		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		query.setContext(context);
		
		query.execute(-1, false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		int count = 0;
		while(!query.isFullyDrained()) {
			
			while(!queue.isEmpty()) {
				count++;
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
		}
		
		Assertions.assertEquals(100, count);
		
		System.out.println();
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testPipedCommands() throws ParseException {
		
		String queryString = sourceString+" | distinct FIRSTNAME LIKES_TIRAMISU trim=true";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testPipedCommands", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());
		
		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		query.setContext(context);
		
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
		query.execute(-1, false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		int count = 0;
		while(!query.isFullyDrained()) {
			
			while(!queue.isEmpty()) {
				count++;
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
		}
		
		Assertions.assertEquals(69, count);
		
		System.out.println();
		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testMultipleQueries() throws ParseException {
		
		String queryString = sourceString+" | distinct FIRSTNAME ;"
							+sourceString+" | distinct LIKES_TIRAMISU ";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testMultipleQueries", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(2, queryList.size());
		
		//------------------------
		// Execute Queries
		
		int index = 1;
		for(CFWQuery query : queryList) {
			
			query.setContext(context);
			
			System.out.println("-------- Query "+index+" ----------");
			ArrayList<CFWQueryCommand> commandList = query.getCommandList();
			
			Assertions.assertEquals(2, commandList.size());
			
			query.execute(-1, false);
			
			LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
			int count = 0;
			while(!query.isFullyDrained()) {
				
				while(!queue.isEmpty()) {
					count++;
					System.out.println(
						CFW.JSON.toJSON(
							queue.poll().getWrappedObject()
						)
					); 
				}
			}
			if(index == 1) {	
				Assertions.assertEquals(44, count);
			}else {
				Assertions.assertEquals(2, count);
			}
			
			index++;
		}

		
	}	

	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testArray() throws ParseException {
		
		String queryString = sourceString+" | dedup FIRSTNAME, LASTNAME, LIKES_TIRAMISU, TIME";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testArray", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());

		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		query.setContext(context);
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
		query.execute(-1, false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		int count = 0;
		while(!query.isFullyDrained()) {
			
			while(!queue.isEmpty()) {
				count++;
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
		}
		
		Assertions.assertEquals(100, count);
		
		System.out.println();

		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testArraySquareBraces() throws ParseException {
		
		String queryString = sourceString+" | display as=table titlefields=[FIRSTNAME, LASTNAME, LIKES_TIRAMISU, TIME]";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testArray", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());

		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		query.setContext(context);
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
		query.execute(-1, false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		int count = 0;
		while(!query.isFullyDrained()) {
			
			while(!queue.isEmpty()) {
				count++;
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
		}
		
		Assertions.assertEquals(100, count);
		
		System.out.println();

		
	}	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testArrayAccess() throws ParseException {
		
		String queryString = sourceString+" | set ARRAY_A=[1,2,3]  ARRAY_ACCESS=ARRAY_A[0]";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testArrayAccess", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		Assertions.assertEquals(1, queryList.size());

		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		query.setContext(context);
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
		query.execute(-1, false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		int count = 0;
		while(!query.isFullyDrained()) {
			
			while(!queue.isEmpty()) {
				count++;
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
		}
		
		Assertions.assertEquals(100, count);
		
		System.out.println();

		
	}	
	
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testArrayWithBinaryExpression() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		ArrayList<QueryPart> parsedParts;
		QueryPart parsedPart;
		//-------------------------------------------------
		// Test Parsing Expressions
		String queryString = "[ 42, myString == 'testString', myNumber < 999, 3 * myNumber  ] ";
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		parsedParts = parser.parseQueryParts();
		Assertions.assertEquals(1, parsedParts.size());
		
		parsedPart = parsedParts.get(0);
		Assertions.assertTrue(parsedPart instanceof QueryPartArray);
		
		System.out.println("===== testArrayWithBinaryExpression Debug  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(parsedPart.createDebugObject(enhanced)) );
		
		//-------------------------------------------------
		// Test Evaluate == Expression
		evaluationResult = ((QueryPartArray)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isJsonArray());
		
		JsonArray resultArray = evaluationResult.getAsJsonArray();
		System.out.println("===== testArrayWithBinaryExpression Array  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(resultArray) );
		
		Assertions.assertEquals(42, resultArray.get(0).getAsInt());
		Assertions.assertEquals(true, resultArray.get(1).getAsBoolean());
		Assertions.assertEquals(true, resultArray.get(2).getAsBoolean());
		Assertions.assertEquals(99, resultArray.get(3).getAsInt());
	}	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testArraysWithinArrays() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		ArrayList<QueryPart> parsedParts;
		QueryPart parsedPart;
		//-------------------------------------------------
		// Test Parsing Expressions
		String queryString = "[[], [1] ,[1,2], 42 , [3,\"3\", '3']] ";
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		parsedParts = parser.parseQueryParts();
		Assertions.assertEquals(1, parsedParts.size());
		
		parsedPart = parsedParts.get(0);
		Assertions.assertTrue(parsedPart instanceof QueryPartArray);
		
		System.out.println("===== testArraysWithinArrays Debug  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(parsedPart.createDebugObject(enhanced)) );
		
		//-------------------------------------------------
		// Test Evaluate == Expression
		evaluationResult = ((QueryPartArray)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isJsonArray());
		
		JsonArray resultArray = evaluationResult.getAsJsonArray();
		System.out.println("===== testArraysWithinArrays Array  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(resultArray) );
		
		Assertions.assertEquals(5, resultArray.size());
		Assertions.assertEquals(0, resultArray.get(0).getAsJsonArray().size());
		Assertions.assertEquals(1, resultArray.get(1).getAsJsonArray().size());
		Assertions.assertEquals(2, resultArray.get(2).getAsJsonArray().size());
		Assertions.assertEquals(42, resultArray.get(3).getAsInt());
		Assertions.assertEquals(3, resultArray.get(4).getAsJsonArray().size());
		

	}	

	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testBinaryExpression() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		ArrayList<QueryPart> parsedParts;
		QueryPart parsedPart;
		//-------------------------------------------------
		// Test Parsing Expressions
		String queryString = "myString==testString   myString!='testString'  22>=myNumber   myNumber<='44'   3 * myNumber   10^5 ";
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		parsedParts = parser.parseQueryParts();
		//System.out.println("============= testBinaryExpression parsedPart ===============");
		//System.out.println( CFW.JSON.toJSON(parsedPart.createDebugObject(enhanced)) );
		
		Assertions.assertEquals(6, parsedParts.size());
		
		int i = 0;
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		//-------------------------------------------------
		// Test Evaluate == Expression
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------------------------
		// Test Evaluate != Expression
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
				
		//-------------------------------------------------
		// Test Evaluate >= Expression
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		//-------------------------------------------------
		// Test Evaluate <= Expression 
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		//-------------------------------------------------
		// Test Evaluate * Expression (multiply)
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isInteger());
		Assertions.assertEquals(99, evaluationResult.getAsInteger());
		
		//-------------------------------------------------
		// Test Evaluate ^ Expression (power)
		parsedPart = parsedParts.get(i++);
		Assertions.assertTrue(parsedPart instanceof QueryPartBinaryExpression);
		
		evaluationResult = ((QueryPartBinaryExpression)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isInteger());
		Assertions.assertEquals(100000, evaluationResult.getAsInteger());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testBinaryExpressionKeywords() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		ArrayList<QueryPart> parsedParts;
		String queryString;
		QueryPart binaryExpression;
		CFWQueryParser parser;
		
		//-------------------------------------------------
		// Test Parsing OR Expressions
		queryString = "myString==notEqualString OR myNumber>22 ";
		
		parser = new CFWQueryParser(queryString, true)
				.enableTracing();
		
		parsedParts = parser.parseQueryParts();
		
		System.out.println("===== parsedParts OR =====");
		System.out.println(CFW.JSON.toJSONPrettyDebugOnly(parser.getTraceResults()));

		Assertions.assertEquals(1, parsedParts.size());
		
		binaryExpression = parsedParts.get(0);
		System.out.println("===== binaryExpression Debug OR =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(binaryExpression.createDebugObject(enhanced)) );

		
		
		Assertions.assertTrue(binaryExpression instanceof QueryPartBinaryExpression);
		
		//-------------------------------------------------
		// Test Evaluate OR Expression
		evaluationResult = ((QueryPartBinaryExpression)binaryExpression).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
		
		
		//-------------------------------------------------
		// Test Parsing AND Expressions
		queryString = "myString==notEqualString and myNumber>22 ";
		
		parser = new CFWQueryParser(queryString, true)
				.enableTracing();
		
		parsedParts = parser.parseQueryParts();
		
		System.out.println("===== parsedParts AND =====");
		System.out.println(CFW.JSON.toJSONPrettyDebugOnly(parser.getTraceResults()));

		Assertions.assertEquals(1, parsedParts.size());
		
		binaryExpression = parsedParts.get(0);
		System.out.println("===== binaryExpression Debug AND =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(binaryExpression.createDebugObject(enhanced)) );

		
		
		Assertions.assertTrue(binaryExpression instanceof QueryPartBinaryExpression);
		
		//-------------------------------------------------
		// Test Evaluate AND Expression
		evaluationResult = ((QueryPartBinaryExpression)binaryExpression).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertFalse(evaluationResult.getAsBoolean());
		
		//-------------------------------------------------
		// Test Parsing AND Expressions
		queryString = "not myNumber<22 ";
		
		parser = new CFWQueryParser(queryString, true)
				.enableTracing();
		
		parsedParts = parser.parseQueryParts();
		
		System.out.println("===== parsedParts NOT =====");
		System.out.println(CFW.JSON.toJSONPrettyDebugOnly(parser.getTraceResults()));

		Assertions.assertEquals(1, parsedParts.size());
		
		binaryExpression = parsedParts.get(0);
		System.out.println("===== binaryExpression Debug NOT =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(binaryExpression.createDebugObject(enhanced)) );

		
		
		Assertions.assertTrue(binaryExpression instanceof QueryPartBinaryExpression);
		
		//-------------------------------------------------
		// Test Evaluate AND Expression
		evaluationResult = ((QueryPartBinaryExpression)binaryExpression).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		Assertions.assertTrue(evaluationResult.getAsBoolean());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testGroupWithBinaryExpression() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		QueryPart parsedPart;
		ArrayList<QueryPart> parsedParts;
		//-------------------------------------------------
		// Test Parsing Expressions
		String queryString = " ( myString == 'testString' ( myNumber < 999 OR myString=='not my string') ) ";
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		parsedParts = parser.parseQueryParts();
		Assertions.assertEquals(1, parsedParts.size());
		parsedPart = parsedParts.get(0);
		Assertions.assertTrue(parsedPart instanceof QueryPartGroup);
		
		System.out.println("===== testGroupWithBinaryExpression Debug  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(parsedPart.createDebugObject(enhanced)) );
		
		
		//-------------------------------------------------
		// Test Evaluate == Expression
		evaluationResult = ((QueryPartGroup)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		
		Assertions.assertEquals(true, evaluationResult.getAsBoolean());
	}	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testGroupWithBinaryExpression2() throws ParseException {
		
		JsonObject object = new JsonObject();
		object.addProperty("myString", "testString");
		object.addProperty("myNumber", 33);
		
		EnhancedJsonObject enhanced = new EnhancedJsonObject(object);
		
		QueryPartValue evaluationResult;
		QueryPart parsedPart;
		ArrayList<QueryPart> parsedParts;
		//-------------------------------------------------
		// Test Parsing Expressions
		String queryString = " ( ( myNumber < 999 OR myString=='not my string') myString == 'testString' ) ";
		
		CFWQueryParser parser = new CFWQueryParser(queryString, true);
		
		parsedParts = parser.parseQueryParts();
		Assertions.assertEquals(1, parsedParts.size());
		parsedPart = parsedParts.get(0);
		Assertions.assertTrue(parsedPart instanceof QueryPartGroup);
		
		System.out.println("===== testGroupWithBinaryExpression Debug  =====");
		System.out.println( CFW.JSON.toJSONPrettyDebugOnly(parsedPart.createDebugObject(enhanced)) );
		
		//-------------------------------------------------
		// Test Evaluate == Expression
		evaluationResult = ((QueryPartGroup)parsedPart).determineValue(enhanced);
		
		Assertions.assertTrue(evaluationResult.isBoolean());
		
		Assertions.assertEquals(true, evaluationResult.getAsBoolean());
	}	
}

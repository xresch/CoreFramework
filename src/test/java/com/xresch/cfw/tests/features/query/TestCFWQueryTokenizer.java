package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

public class TestCFWQueryTokenizer {
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static String jsonTestData;
	private static final String PACKAGE = "com.xresch.cfw.tests.features.query.testdata";
	
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE);
		jsonTestData =	CFW.Files.readPackageResource(PACKAGE, "SourceJsonTestdata.json");
		jsonTestData = jsonTestData.replace("'", "\'");
		

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
	public void testTokenizerBasics() throws IOException {
		

		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(" hello \"double quotes\" 'single quotes' 423 -33.431 true '42' false null ", true);
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Basic Test", results);
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(0).type());
		Assertions.assertEquals("hello", 								results.get(0).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, 	results.get(1).type());
		Assertions.assertEquals("double quotes", 						results.get(1).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_SINGLE_QUOTES, 	results.get(2).type());
		Assertions.assertEquals("single quotes",						results.get(2).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER, 		results.get(3).type());
		Assertions.assertEquals("423", 									results.get(3).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER,		results.get(4).type());
		Assertions.assertEquals("-33.431", 								results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_BOOLEAN, 		results.get(5).type());
		Assertions.assertEquals("true", 								results.get(5).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_SINGLE_QUOTES, 	results.get(6).type());
		Assertions.assertEquals("42", 									results.get(6).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_BOOLEAN, 		results.get(7).type());
		Assertions.assertEquals("false", 								results.get(7).value());
		
		Assertions.assertEquals(CFWQueryTokenType.NULL, 				results.get(8).type());
		Assertions.assertEquals("null", 								results.get(8).value());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTokenizerSplit() throws IOException {
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer("001 | part ABC | \"double quotes\" ", true)
				 .splitBy("[\\|]");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Split Test", results);
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER, 		results.get(0).type());
		Assertions.assertEquals( "001", 								results.get(0).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SPLIT, 				results.get(1).type());
		Assertions.assertEquals("|", 									results.get(1).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(2).type());
		Assertions.assertEquals("part", 								results.get(2).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(3).type());
		Assertions.assertEquals("ABC", 									results.get(3).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SPLIT, 				results.get(4).type());
		Assertions.assertEquals("|", 									results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, 	results.get(5).type());
		Assertions.assertEquals("double quotes", 						results.get(5).value());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTokenizerKeywordsCaseSensitive() throws IOException {
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(" \"my string\" AND 'another string' OR identifier_A NOT 42 and functionName(param)", true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Keywords Case Sensitive Test", results);
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, 	results.get(0).type());
		Assertions.assertEquals("my string", 							results.get(0).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(1).type());
		Assertions.assertEquals("AND", 									results.get(1).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_SINGLE_QUOTES, 	results.get(2).type());
		Assertions.assertEquals("another string", 						results.get(2).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(3).type());
		Assertions.assertEquals("OR", 									results.get(3).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(4).type());
		Assertions.assertEquals("identifier_A", 						results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(5).type());
		Assertions.assertEquals("NOT", 									results.get(5).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER, 		results.get(6).type());
		Assertions.assertEquals("42", 									results.get(6).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(7).type());
		Assertions.assertEquals("and", 									results.get(7).value());

		Assertions.assertEquals(CFWQueryTokenType.FUNCTION_NAME, 		results.get(8).type());
		Assertions.assertEquals("functionName", 						results.get(8).value());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTokenizerKeywordsCaseInsensitive() throws IOException {
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(" \"my string\" and 'another string' OR identifier NOT 42 and", false)
				 .keywords("AND", "or", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Keywords Case Insensitive Test", results);
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, 	results.get(0).type());
		Assertions.assertEquals("my string", 							results.get(0).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(1).type());
		Assertions.assertEquals("and",									results.get(1).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_SINGLE_QUOTES, 	results.get(2).type());
		Assertions.assertEquals("another string", 						results.get(2).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD,				results.get(3).type());
		Assertions.assertEquals("OR", 									results.get(3).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(4).type());
		Assertions.assertEquals("identifier", 							results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(5).type());
		Assertions.assertEquals("NOT", 									results.get(5).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER, 		results.get(6).type());
		Assertions.assertEquals("42", 									results.get(6).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(7).type());
		Assertions.assertEquals("and", 									results.get(7).value());

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testTokenizerSignsAndOperators() throws IOException {
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(", () [] +- */ ! &| <> = != <= >= . ; == ", false);
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Keywords Case Insensitive Test", results);
		
		int i = 0;
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_COMMA, 			results.get(i).type());
		Assertions.assertEquals(",", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_ROUND_OPEN, 		results.get(++i).type());
		Assertions.assertEquals("(",									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_ROUND_CLOSE, 	results.get(++i).type());
		Assertions.assertEquals(")", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_SQUARE_OPEN, 		results.get(++i).type());
		Assertions.assertEquals("[",									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_SQUARE_CLOSE, 	results.get(++i).type());
		Assertions.assertEquals("]", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_PLUS,		results.get(++i).type());
		Assertions.assertEquals("+", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_MINUS, 		results.get(++i).type());
		Assertions.assertEquals("-", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_MULTIPLY, 	results.get(++i).type());
		Assertions.assertEquals("*", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_DIVIDE, 		results.get(++i).type());
		Assertions.assertEquals("/", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_NOT, 		results.get(++i).type());
		Assertions.assertEquals("!", 									results.get(i).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_AND, 		results.get(++i).type());
		Assertions.assertEquals("&", 									results.get(i).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_OR, 			results.get(++i).type());
		Assertions.assertEquals("|", 									results.get(i).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_LOWERTHEN, 	results.get(++i).type());
		Assertions.assertEquals("<", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_GREATERTHEN, results.get(++i).type());
		Assertions.assertEquals(">", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL, 		results.get(++i).type());
		Assertions.assertEquals("=", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_NOT, 	results.get(++i).type());
		Assertions.assertEquals("!=", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, results.get(++i).type());
		Assertions.assertEquals("<=", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, 	results.get(++i).type());
		Assertions.assertEquals(">=", 									results.get(i).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_DOT, 		results.get(++i).type());
		Assertions.assertEquals(".", 									results.get(i).value());

		Assertions.assertEquals(CFWQueryTokenType.SIGN_SEMICOLON, 		results.get(++i).type());
		Assertions.assertEquals(";", 									results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, results.get(++i).type());
		Assertions.assertEquals("==", 									results.get(i).value());
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSourceSyntax() throws IOException {
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer("source json data=\'"+jsonTestData+"\' ", false);
		System.out.println("source json data=\""+jsonTestData+"\"");
		
//		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer("source json data='[\r\n"
//		        + "  {\"FIRSTNAME\":\"Mars\",\"LASTNAME\":\"Lundgren\",\"LOCATION\":\"Vaikuntha\",\"ID\":\"hayoCpHhhlJXjryl\",\"LIKES_TIRAMISU\":true,\"LAST_LOGIN\":1642783356902,\"URL\":\"http://www.example.url/mightyperson?id\\u003dhayoCpHhhlJXjryl\",\"VALUE\":7,\"TIME\":1642151589524}\r\n"
//				+ ", {\"FIRSTNAME\":\"Apollo\",\"LASTNAME\":\"Lindgren\",\"LOCATION\":\"Ayotha Amirtha Gangai\",\"ID\":\"gzKSwqQsalQKKXIL\",\"LIKES_TIRAMISU\":false,\"LAST_LOGIN\":1641126356902,\"URL\":\"http://www.example.url/mightyperson?id\\u003dgzKSwqQsalQKKXIL\",\"VALUE\":15,\"TIME\":1642151607524} \r\n"
//				+ "\r\n]'", false);
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testSourceSyntax", results);
		
		int i = 0;
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(i).type());
		Assertions.assertEquals("source", 								results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(++i).type());
		Assertions.assertEquals("json", 								results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(++i).type());
		Assertions.assertEquals("data", 								results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL, 		results.get(++i).type());
		Assertions.assertEquals("=", 								results.get(i).value());
		
		Assertions.assertEquals(CFWQueryTokenType.TEXT_SINGLE_QUOTES, 		results.get(++i).type());
		//Assertions.assertEquals("[1,2,3]", 								results.get(i).value());
	}
	
	
}

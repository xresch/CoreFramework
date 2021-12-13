package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

public class TestCFWQueryTokenizer {
	
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
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(" \"my string\" AND 'another string' OR identifier_A NOT 42 and", true)
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
		Assertions.assertEquals("identifier_A", 							results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.KEYWORD, 				results.get(5).type());
		Assertions.assertEquals("NOT", 									results.get(5).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_NUMBER, 		results.get(6).type());
		Assertions.assertEquals("42", 									results.get(6).value());
		
		Assertions.assertEquals(CFWQueryTokenType.LITERAL_STRING, 		results.get(7).type());
		Assertions.assertEquals("and", 									results.get(7).value());

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
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(", () +- */ ! &| <> = != <= >= . ; == ", false);
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("Keywords Case Insensitive Test", results);
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_COMMA, 			results.get(0).type());
		Assertions.assertEquals(",", 									results.get(0).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_OPEN, 		results.get(1).type());
		Assertions.assertEquals("(",									results.get(1).value());
		
		Assertions.assertEquals(CFWQueryTokenType.SIGN_BRACE_CLOSE, 	results.get(2).type());
		Assertions.assertEquals(")", 									results.get(2).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_PLUS,		results.get(3).type());
		Assertions.assertEquals("+", 									results.get(3).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_MINUS, 		results.get(4).type());
		Assertions.assertEquals("-", 									results.get(4).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_MULTIPLY, 	results.get(5).type());
		Assertions.assertEquals("*", 									results.get(5).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_DIVIDE, 		results.get(6).type());
		Assertions.assertEquals("/", 									results.get(6).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_NOT, 		results.get(7).type());
		Assertions.assertEquals("!", 									results.get(7).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_AND, 		results.get(8).type());
		Assertions.assertEquals("&", 									results.get(8).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_OR, 			results.get(9).type());
		Assertions.assertEquals("|", 									results.get(9).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_LOWERTHEN, 	results.get(10).type());
		Assertions.assertEquals("<", 									results.get(10).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_GREATERTHEN, results.get(11).type());
		Assertions.assertEquals(">", 									results.get(11).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL, 		results.get(12).type());
		Assertions.assertEquals("=", 									results.get(12).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_NOT, 	results.get(13).type());
		Assertions.assertEquals("!=", 									results.get(13).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, results.get(14).type());
		Assertions.assertEquals("<=", 									results.get(14).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, 	results.get(15).type());
		Assertions.assertEquals(">=", 									results.get(15).value());

		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_DOT, 		results.get(16).type());
		Assertions.assertEquals(".", 									results.get(16).value());

		Assertions.assertEquals(CFWQueryTokenType.SIGN_SEMICOLON, 		results.get(17).type());
		Assertions.assertEquals(";", 									results.get(17).value());
		
		Assertions.assertEquals(CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, results.get(18).type());
		Assertions.assertEquals("==", 									results.get(18).value());
	}
	
	
}

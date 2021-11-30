package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.tutorial.CFWQueryTokenizer;
import com.xresch.cfw.features.query.tutorial.CFWQueryTokenizer.QueryToken;
import com.xresch.cfw.features.query.tutorial.CFWQueryTokenizer.CFWQueryTokenType;

public class TestParserTutorial {
	
	

	@Test
	public void testTokenizer() throws IOException {
		
		//--------------------------------------------------
		// Basic Test
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(" hello \"double quotes\" 'single quotes' 423 33.431 '42' ");
		
		ArrayList<QueryToken> results = tokenizer.getAllTokens();
		printResults("Basic Test", results);
		
		Assertions.assertEquals(results.get(0).type(), CFWQueryTokenType.LITERAL_STRING);
		Assertions.assertEquals(results.get(0).value(), "hello");
		
		Assertions.assertEquals(results.get(1).type(), CFWQueryTokenType.TEXT_DOUBLE_QUOTES);
		Assertions.assertEquals(results.get(1).value(), "double quotes");
		
		Assertions.assertEquals(results.get(2).type(), CFWQueryTokenType.TEXT_SINGLE_QUOTES);
		Assertions.assertEquals(results.get(2).value(), "single quotes");
		
		Assertions.assertEquals(results.get(3).type(), CFWQueryTokenType.LITERAL_NUMBER);
		Assertions.assertEquals(results.get(3).value(), "423");
		
		Assertions.assertEquals(results.get(4).type(), CFWQueryTokenType.LITERAL_NUMBER);
		Assertions.assertEquals(results.get(4).value(), "33.431");
		
		Assertions.assertEquals(results.get(5).type(), CFWQueryTokenType.TEXT_SINGLE_QUOTES);
		Assertions.assertEquals(results.get(5).value(), "42");

		//--------------------------------------------------
		// Split Test
		 tokenizer = new CFWQueryTokenizer("001 | part ABC | \"double quotes\" ")
				 .splitBy("[\\|]");
		
		 results = tokenizer.getAllTokens();
		printResults("Split Test", results);
		
		Assertions.assertEquals(results.get(0).type(), CFWQueryTokenType.LITERAL_NUMBER);
		Assertions.assertEquals(results.get(0).value(), "001");
		
		Assertions.assertEquals(results.get(1).type(), CFWQueryTokenType.OPERATOR_SPLIT);
		Assertions.assertEquals(results.get(1).value(), "|");
		
		Assertions.assertEquals(results.get(2).type(), CFWQueryTokenType.LITERAL_STRING);
		Assertions.assertEquals(results.get(2).value(), "part");
		
		Assertions.assertEquals(results.get(3).type(), CFWQueryTokenType.LITERAL_STRING);
		Assertions.assertEquals(results.get(3).value(), "ABC");
		
		Assertions.assertEquals(results.get(4).type(), CFWQueryTokenType.OPERATOR_SPLIT);
		Assertions.assertEquals(results.get(4).value(), "|");
		
		Assertions.assertEquals(results.get(5).type(), CFWQueryTokenType.TEXT_DOUBLE_QUOTES);
		Assertions.assertEquals(results.get(5).value(), "double quotes");

	}
	
	
	private void printResults(String title, ArrayList<QueryToken> results) {
		System.out.println("======================= "+title+" =======================");
		for(QueryToken token : results) {
			System.out.println(CFW.JSON.toJSON(token));
		}
	}

}

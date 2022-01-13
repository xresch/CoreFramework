package com.xresch.cfw.tests.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

public class TestCFWQueryParser {
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@BeforeAll
	public static void setup() {
		CFW.Files.addAllowedPackage("com.xresch.cfw.tests.features.query.testdata");
		FeatureQuery feature = new FeatureQuery();
		feature.register();
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
	public void testParsingSimpleSourceQuery() throws ParseException {
		
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer("source random records=22", true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testQueryPartArray", results);
		
		CFWQueryParser parser = new CFWQueryParser("source random records=22");
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());
		
		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		
		query.execute(false);
		
		LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
		while(!query.isComplete()) {
			
			while(!queue.isEmpty()) {
				System.out.println(
					CFW.JSON.toJSON(
						queue.poll().getWrappedObject()
					)
				); 
			}
			
		}
		
		System.out.println();
		


	}
	
	

	
}

package com.xresch.cfw.tests.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;

public class TestCFWQueryParsing {
	
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
		
		String queryString = "source random records=10";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testQueryPartArray", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
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
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testParsingPipedCommands() throws ParseException {
		
		String queryString = "source random records=20 | distinct FIRSTNAME LIKES_TIRAMISuuU trim=true";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testParsingPipedCommands", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());
		
		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
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
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testParsingMultipleQueries() throws ParseException {
		
		String queryString = "source random records=4 | distinct FIRSTNAME ;"
							+" source random records=7 | distinct LASTNAME";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testParsingMultipleQueries", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(2, queryList.size());
		
		//------------------------
		// Execute Queries
		
		int index = 1;
		for(CFWQuery query : queryList) {

			System.out.println("-------- Query "+index+" ----------");
			ArrayList<CFWQueryCommand> commandList = query.getCommandList();
			
			Assertions.assertEquals(2, commandList.size());
			
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
			
			index++;
		}

		
	}	

	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testParsingQueryPartArray() throws ParseException {
		
		String queryString = "source random records=4 | dedup FIRSTNAME, LASTNAME, LIKES_TIRAMISU, TIME";
		
		CFWQueryTokenizer tokenizer = new CFWQueryTokenizer(queryString, true)
				 .keywords("AND", "OR", "NOT");
		
		ArrayList<CFWQueryToken> results = tokenizer.getAllTokens();
		printResults("testParsingQueryPartArray", results);
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		Assertions.assertEquals(1, queryList.size());

		//------------------------
		// Execute Query
		
		CFWQuery query = queryList.get(0);
		ArrayList<CFWQueryCommand> commandList = query.getCommandList();
		
		Assertions.assertEquals(2, commandList.size());
		
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

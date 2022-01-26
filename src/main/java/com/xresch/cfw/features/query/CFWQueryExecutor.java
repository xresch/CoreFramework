package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryParser;

public class CFWQueryExecutor {
	
	/****************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results
	 * @throws InterruptedException 
	 ****************************************************************/
	public static JsonArray parseAndExecuteAll(String queryString, long earliest, long latest) throws ParseException, InterruptedException {
		
		//String queryString = "source random records=100";
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
		ArrayList<CFWQuery> queryList = parser.parse();
				
		//------------------------
		// Iterate All Queries
		
		JsonArray returnValue = new JsonArray();
		int index = 0;
		for(CFWQuery query : queryList) {
			
			//--------------------------------
			// Prepare and Execute
			CFWQueryContext context = query.getContext();
			context.setEarliest(earliest);
			context.setLatest(latest);
						
			query.execute(false);
			
			//--------------------------------
			// Read Results
			LinkedBlockingQueue<EnhancedJsonObject> queue = query.getLastQueue();
			int resultCount = 0;
			
			JsonObject queryResults = new JsonObject();
			JsonArray results = new JsonArray();
			
			while(!query.isFullyDrained()) {
				
				while(!queue.isEmpty()) {
					resultCount++;
					results.add(queue.poll().getWrappedObject());	
				}
				
				//TODO
				//Thread.currentThread().wait(100);
				
			}
			queryResults.addProperty("resultCount", resultCount);
			queryResults.add("results", results);
			
			returnValue.add(queryResults);
			index++;
		}
		
		return returnValue;
	}

}

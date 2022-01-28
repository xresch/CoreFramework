package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryExecutor {
	
	private int resultCount = 0;
	
	/****************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results
	 * @throws InterruptedException 
	 ****************************************************************/
	public JsonArray parseAndExecuteAll(String queryString, long earliest, long latest) throws ParseException, InterruptedException {
		
		//String queryString = "source random records=100";
		
		CFWQueryParser parser = new CFWQueryParser(queryString);
		
		ArrayList<CFWQuery> queryList = parser.parse();
		
		
		//------------------------
		// Iterate All Queries
		
		JsonArray returnValue = new JsonArray();
		int index = 0;
		for(CFWQuery query : queryList) {
			
			//--------------------------------
			// Prepare Context
			CFWQueryContext queryContext = query.getContext();
			queryContext.setEarliest(earliest);
			queryContext.setLatest(latest);
				
			//--------------------------------
			// Add Result Sink
			resultCount = 0;
			JsonObject queryResults = new JsonObject();
			JsonArray results = new JsonArray();
			
			query.add(new PipelineAction<EnhancedJsonObject, EnhancedJsonObject>() {
							
				@Override
				public void execute(PipelineActionContext context) throws Exception {
					LinkedBlockingQueue<EnhancedJsonObject> inQueue = getInQueue();
					
					System.out.println("while inner");

					while(!inQueue.isEmpty()) {
						System.out.println("w");
						resultCount++;

						JsonObject object = inQueue.poll().getWrappedObject();
						if(object != null) {
							System.out.println(resultCount);
							results.add(object);
						}else {
							queryContext.addMessage(MessageType.WARNING, "Data might be incomplete due to reached memory limits.");
						}
					}
						
						
					this.setDoneIfPreviousDone();
					
					
				}
			});
			
			//--------------------------------
			// Execute query and Wait for Complete
			try {
				query.execute(true);
			}catch(NullPointerException e) {
				System.out.println("caught null");
			}
			
			queryResults.addProperty("resultCount", resultCount);
			queryResults.add("results", results);
			
			returnValue.add(queryResults);
			index++;
		}
		
		return returnValue;
	}

}

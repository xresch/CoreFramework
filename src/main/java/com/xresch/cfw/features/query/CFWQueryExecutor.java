package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryExecutor {
	
	private static Logger logger = CFWLog.getLogger(CFWQueryExecutor.class.getName());
	
	private int resultCount = 0;
	private boolean checkSourcePermissions = true;
	
	/****************************************************************
	 * Enable or disable if the users permission for a source should be
	 * verified or not.
	 ****************************************************************/
	public CFWQueryExecutor checkSourcePermissions(boolean checkSourcePermissions) {
		this.checkSourcePermissions = checkSourcePermissions;
		return this;
	}
	
	/****************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results, or null in
	 * case of errors. 
	 * 
	 * @throws InterruptedException 
	 ****************************************************************/
	public JsonArray parseAndExecuteAll(String queryString, long earliest, long latest) {
		
		//------------------------
		// Parse The Query
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		
		try {
			CFWQueryParser parser = new CFWQueryParser(queryString, checkSourcePermissions);
			queryList = parser.parse();
			
		}catch (NumberFormatException e) {
			new CFWLog(logger).severe("Error Parsing a number:"+e.getMessage(), e);
			return null;
		} catch (ParseException e) {
			CFW.Messages.addErrorMessage(e.getMessage());
			return null;
		}  catch (OutOfMemoryError e) {
			new CFWLog(logger).severe("Not enough memory to complete query. Try reducing the amount of data processed.", e);
			return null;
		} catch (IndexOutOfBoundsException e) {
			new CFWLog(logger).severe("Query Parsing: "+e.getMessage(), e);
			return null;
		}catch (Exception e) {
			new CFWLog(logger).severe("Unexpected error when parsing the query: "+e.getMessage(), e);
			return null;
		}
		
		//------------------------
		// Iterate All Queries
		
		JsonArray returnValue = new JsonArray();
		
		int index = 0;
		for(CFWQuery query : queryList) {
			
			//--------------------------------
			// Check Limits
			if(query.isSourceLimitReached()) { continue; }
			if(query.isCommandLimitReached()) { continue; }
			
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
					
					while(!inQueue.isEmpty()) {
						resultCount++;

						JsonObject object = inQueue.poll().getWrappedObject();
						if(object != null) {
							results.add(object);
						}else {
							queryContext.addMessage(MessageType.WARNING, "Data might be incomplete due to reached limits.");
						}
					}
						
					this.setDoneIfPreviousDone();
					
					
				}
			});
			
			//--------------------------------
			// Execute query and Wait for Complete
			long startMillis;
			long execMillis = -1;
			try {
				startMillis = System.currentTimeMillis();
					query.execute(true);
				execMillis = System.currentTimeMillis() - startMillis;
			}catch(NullPointerException e) {
				queryContext.addMessage(MessageType.ERROR, "Query ran into an issue: NullpointerException.");
			}
			
			//--------------------------------
			// Create Response
			queryResults.addProperty("resultCount", resultCount);
			queryResults.addProperty("execTimeMillis", execMillis);
			
			queryResults.add("detectedFields", queryContext.getFieldnamesAsJsonArray() );
			queryResults.add("metadata", query.getContext().getMetadata());
			queryResults.add("displaySettings", query.getContext().getDisplaySettings());
			queryResults.add("results", results);
			
			returnValue.add(queryResults);
			index++;
		}
		
		return returnValue;
	}

}

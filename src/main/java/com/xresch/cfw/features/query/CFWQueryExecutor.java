package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
	private boolean checkPermissions = true;
	
	/****************************************************************
	 * Enable or disable if the users permissions should be
	 * verified or not.
	 ****************************************************************/
	public CFWQueryExecutor checkPermissions(boolean checkPermissions) {
		this.checkPermissions = checkPermissions;
		return this;
	}
	
	/****************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results, or null in
	 * case of errors. 
	 * Takes the max execution time from the in-app configuration for
	 * limiting query execution time.
	 * 
	 * @params queryString the query to execute
	 * @param earliest time in epoch millis
	 * @param latest time in epoch millis
	 * @param timezoneOffsetMinutes TODO
	 * @param timezoneOffsetMinutes offset for the clients time zone.
	 * You can use the following in javascript to get this:
	 * 		var timeZoneOffset = new Date().getTimezoneOffset();
	 * 
	 ****************************************************************/
	public JsonArray parseAndExecuteAll(String queryString, long earliest, long latest, int timezoneOffsetMinutes) {
		
		//------------------------
		// Parse The Query
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		
		CFWQueryParser parser = new CFWQueryParser(queryString, checkPermissions);
		try {
			//tracing is experimental, might lead to errors
			//parser.enableTracing();
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
		}finally {
			//System.out.println(parser.getTraceResults());
		}
		
		//------------------------
		// Iterate All Queries
		JsonArray returnValue = new JsonArray();
		
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
			queryContext.setTimezoneOffsetMinutes(timezoneOffsetMinutes);
			queryContext.checkPermissions(checkPermissions);
				
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
				long maxExecTime = CFW.DB.Config.getConfigAsLong(FeatureQuery.CONFIG_QUERY_EXEC_LIMIT);
				query.execute(maxExecTime, true);
				
				execMillis = System.currentTimeMillis() - startMillis;
			}catch(NullPointerException e) {
				queryContext.addMessage(MessageType.ERROR, "Query ran into an issue: NullpointerException.");
			}
			
			//--------------------------------
			// Handle Globals
//			JsonObject queryGlobals = queryContext.getGlobals();
//			for(Entry<String, JsonElement> entry : queryGlobals.entrySet()){
//				multiQueryGlobals.add(entry.getKey(), entry.getValue());
//			}
			
			//--------------------------------
			// Create Response
			queryResults.addProperty("resultCount", resultCount);
			queryResults.addProperty("execTimeMillis", execMillis);
			
			queryResults.add("globals", queryContext.getGlobals());
			queryResults.add("detectedFields", queryContext.getFieldnamesAsJsonArray() );
			queryResults.add("metadata", query.getContext().getMetadata());
			queryResults.add("displaySettings", query.getContext().getDisplaySettings());
			queryResults.add("results", results);
			
			returnValue.add(queryResults);

		}
		
		return returnValue;
	}

}

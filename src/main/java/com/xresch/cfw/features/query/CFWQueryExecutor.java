package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
/**************************************************************************************************************
 * The Class used to execute CFWQL queries.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryExecutor {
	
	private static Logger logger = CFWLog.getLogger(CFWQueryExecutor.class.getName());
	
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
	public CFWQueryResultList parseAndExecuteAll(String queryString, CFWTimeframe timeframe) {
		return this.parseAndExecuteAll(
				  queryString
				, timeframe.getEarliest()
				, timeframe.getLatest()
				, timeframe.getClientTimezoneOffset()
				);
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
	 * You can use CFWTimeframe with data from browser, or the following 
	 * in javascript to get this value:
	 * 		var timeZoneOffset = new Date().getTimezoneOffset();
	 * 
	 ****************************************************************/
	public CFWQueryResultList parseAndExecuteAll(String queryString, long earliest, long latest, int timezoneOffsetMinutes) {
		
		CFWQueryContext baseQueryContext = new CFWQueryContext();
		baseQueryContext.setEarliest(earliest);
		baseQueryContext.setLatest(latest);
		baseQueryContext.setTimezoneOffsetMinutes(timezoneOffsetMinutes);
		baseQueryContext.checkPermissions(checkPermissions);
		
		return this.parseAndExecuteAll(baseQueryContext, queryString, null, null);
	}
	
	/****************************************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results, or null in
	 * case of errors. 
	 * Takes the max execution time from the in-app configuration for
	 * limiting query execution time.
	 * If both initialQueue and resultQueue are defined, modifications of baseQueryContext will be
	 * written to the original context, not to a clone(needed for command 'mimic'). 
	 * 
	 * @params baseQueryContext the context used for executing the query
	 * @params queryString the query to execute
	 * @params initialQueue (optional) the initial queue that should be passed to the first command of the query
	 * @params resultQueue (optional) the queue where the results should be written to.
	 * 		   If this is null, the results are written to the returned CFWQueryResultList.
	 * 		   If this is set, results will be written to the resultQueue.
	 ****************************************************************************************/
	public CFWQueryResultList parseAndExecuteAll(
			  CFWQueryContext baseQueryContext
			, String queryString
			, LinkedBlockingQueue<EnhancedJsonObject> initialQueue
			, LinkedBlockingQueue<EnhancedJsonObject> resultQueue
			) {
		
		//--------------------------------
		// Define Context
		boolean cloneContext = true;
		if(initialQueue != null && resultQueue != null) {
			cloneContext = false;
		}
		
		ArrayList<CFWQuery> queryList = parseQuery(queryString, baseQueryContext, cloneContext);
		if(queryList == null) {
			return baseQueryContext.getResultList();
		}else {
			return this.executeAll(queryList, initialQueue, resultQueue);
		}
	}
	/****************************************************************************************
	 * Parses the query string and executes all queries.
	 * Returns a Json Array containing the Query Results, or null in
	 * case of errors. 
	 * Takes the max execution time from the in-app configuration for
	 * limiting query execution time.
	 * If both initialQueue and resultQueue are defined, modifications of baseQueryContext will be
	 * written to the original context, not to a clone(needed for command 'mimic'). 
	 * 
	 * @params queryList the queries to execute in order.
	 * @params initialQueue (optional) the initial queue that should be passed to the first command of the query
	 * @params resultQueue (optional) the queue where the results should be written to.
	 * 		   If this is null, the results are written to the returned CFWQueryResultList.
	 * 		   If this is set, results will be written to the resultQueue.
	 ****************************************************************************************/
	public CFWQueryResultList executeAll(
			  ArrayList<CFWQuery> queryList
			, LinkedBlockingQueue<EnhancedJsonObject> initialQueue
			, LinkedBlockingQueue<EnhancedJsonObject> resultQueue
			) {
		
		//--------------------------------
		// Handle Empty List
		if(queryList.isEmpty()) {
			return new CFWQueryResultList();
		}
		
		//--------------------------------
		// Get Result List
		CFWQueryResultList resultArray = queryList.get(0).getContext().getResultList();
		
		//======================================
		// Set initial Queue
		if(initialQueue != null && !queryList.isEmpty()) {
			CFWQuery firstQuery = queryList.get(0);
			ArrayList<CFWQueryCommand> commands = firstQuery.getCopyOfCommandList();
			if(!commands.isEmpty()) {
				CFWQueryCommand firstCommand = commands.get(0);
				firstCommand.setInQueue(initialQueue);
			}
		}
		
		//======================================
		// Iterate All Queries
		for(CFWQuery query : queryList) {

			//--------------------------------
			// Check Limits
			if(query.isSourceLimitReached()) { continue; }
			if(query.isCommandLimitReached()) { continue; }
			
			//--------------------------------
			// Add Result Sink
			JsonArray results = new JsonArray();
			CFWQueryContext queryContext = query.getContext();

			query.add(new PipelineAction<EnhancedJsonObject, EnhancedJsonObject>() {
							
				@Override
				public void execute(PipelineActionContext context) throws Exception {
					LinkedBlockingQueue<EnhancedJsonObject> inQueue = getInQueue();
					
					while(!inQueue.isEmpty()) {
						EnhancedJsonObject enhancedObject = inQueue.poll();
						JsonObject object = enhancedObject.getWrappedObject();
						if(object != null) {
							if(resultQueue == null) {
								results.add(object);
							}else {
								resultQueue.add(enhancedObject);
							}
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
			}catch(Exception e) {
				queryContext.addMessage(MessageType.ERROR, "Query ran into an issue:"+e.getMessage());
			}

			//--------------------------------
			// Handle Globals
//			JsonObject queryGlobals = queryContext.getGlobals();
//			for(Entry<String, JsonElement> entry : queryGlobals.entrySet()){
//				multiQueryGlobals.add(entry.getKey(), entry.getValue());
//			}

			//--------------------------------
			// Create Response
			if(resultQueue == null) {
				
				CFWQueryResult queryResult = new CFWQueryResult(queryContext);
				queryResult.setExecTimeMillis(execMillis);
				queryResult.setResults(results);
				
				resultArray.addResult(queryResult);
			}

		}
		return resultArray;
	}

	/****************************************************************************************
	 * Parses the query string and handles exceptions.
	 * If an exception occurs, this method returns null and writes a debug result to the 
	 * given CFWQueryContext.
	 * Use the following in case you want to access the debug result: 
	 * <pre><code>	if(queryList == null) {
		return baseQueryContext.getResultList();
	}
	 * </code></pre>
	 ****************************************************************************************/
	public ArrayList<CFWQuery> parseQuery(String queryString, CFWQueryContext baseQueryContext, boolean cloneContext) {
		//======================================
		// Parse The Query
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		CFWQueryParser parser = new CFWQueryParser(queryString, checkPermissions, baseQueryContext, cloneContext);
		
		try {
			//tracing is experimental, might lead to errors
			//parser.enableTracing();
			queryList = parser.parse();

		}catch (NumberFormatException e) {
			new CFWLog(logger).severe("Error Parsing a number:"+e.getMessage(), e);
			parserDebugState(baseQueryContext, parser);
			return null;
		} catch (ParseException e) {
			CFW.Messages.addErrorMessage(e.getMessage());
			parserDebugState(baseQueryContext, parser);
			return null;
		}  catch (OutOfMemoryError e) {
			// should not happen again
			new CFWLog(logger).severe("Out of memory while parsing query. Please check your syntax.", e);
			parserDebugState(baseQueryContext, parser);
			return null;
		} catch (IndexOutOfBoundsException e) {
			new CFWLog(logger).severe("Query Parsing: "+e.getMessage(), e);
			parserDebugState(baseQueryContext, parser);
			return null;
		}catch (Exception e) {
			new CFWLog(logger).severe("Unexpected error when parsing the query: "+e.getMessage(), e);
			parserDebugState(baseQueryContext, parser);
			return null;
		}finally {

		}
		return queryList;
	}

	
	private CFWQueryResultList parserDebugState(CFWQueryContext baseQueryContext, CFWQueryParser parser) {
		
		CFWQueryResultList resultArray = baseQueryContext.getResultList();
		
		JsonArray detectedFields = new JsonArray();
		detectedFields.add("KEY");
		detectedFields.add("VALUE");
		
		CFWQueryResult debugState = new CFWQueryResult(new CFWQueryContext())
					.setResults(parser.getParserState())
					.setDetectedFields(detectedFields)
					;
		
		resultArray.addResult(debugState);
		
		return resultArray;
	}

}

package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.query.sources.CFWQuerySourceStored;
import com.xresch.cfw.features.query.store.CFWStoredQuery;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandStored extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "stored";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandStored.class.getName());
	
	private ArrayList<QueryPart> parts;

	private ArrayList<String> fieldsToMove = new ArrayList<>();
	private CFWStoredQuery storedQuery = null;
	private JsonObject params = null;
	private ArrayList<String> commandsToRemove = new  ArrayList<String>();
	
	private LinkedBlockingQueue<EnhancedJsonObject> queuingQueue = new LinkedBlockingQueue<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandStored(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME};
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_CODING);
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Inserts executes all the commands of the selected stored query in this place.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" query=<query> [params=<params>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
					+"<li><b>query:&nbsp;</b>The stored query that should be executed (use Ctrl + Space for autocomplete).</li>"
					+"<li><b>params:&nbsp;</b>(Optional)The parameters for the stored query (use Ctrl + Space for autocomplete).</li>"
					+"<li><b>remove:&nbsp;</b>(Optional)Names of commands that should be removed.</li>"
				+"</ul>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_"+COMMAND_NAME+".html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
				
		this.parts = parts;
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		CFWQuerySourceStored.autocompleteStoredQuery(result, helper);
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		QueryPartValue queryValue = null;
		//--------------------------------------
		// Iterate Parts
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)part;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPartValue assignmentValue = assignment.determineValue(null);
				
				if(assignmentName != null) {
					assignmentName = assignmentName.trim().toLowerCase();
					
					switch(assignmentName) {
					
						case "query":  queryValue = assignmentValue; break;
						case "params": params = assignmentValue.getAsJsonObject();  break;
						case "remove": commandsToRemove = assignmentValue.getAsStringArray(); break;
						
						default: throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
					}

				}
			}
		}
		
		//--------------------------------------
		// Get Query ID
		Integer queryID = null;
		
		if(queryValue != null) {
			
			if(queryValue.isJsonObject()) {
				JsonObject queryObject = queryValue.getAsJsonObject();
				
				if(queryObject.has("id")) {
					queryID = queryObject.get("id").getAsInt();
				}else {
					throw new IllegalArgumentException(COMMAND_NAME+": Please specify a query ID.");
				}
			}else if(queryValue.isNumberOrNumberString()) {
				queryID = queryValue.getAsInteger();
			}else {
				throw new IllegalArgumentException(COMMAND_NAME+": Unsupported type for parameter 'query':"+queryValue.type().name());
			}
			
		}else {
			throw new IllegalArgumentException(COMMAND_NAME+": Please specify a query.");
		}
			
		//----------------------------------
		// Check Permissions
		// Done for the current query, checks
		// if the user can access the stored query
		CFWQueryContext context = this.getParent().getContext();
		
		if(context.checkPermissions()
		&& ! CFW.DB.StoredQuery.hasUserAccessToStoredQuery(queryID)
		){
			throw new ParseException(COMMAND_NAME + ": You are not allowed to use the specified stored query.", -1);
		}
		
		//----------------------------------
		// Retrieve the query
		if(queryID != null) {
			storedQuery = CFW.DB.StoredQuery.selectByID(queryID);
		}

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		if(this.isPreviousDone()) {

			//=====================================
			// Execute Stored Query
			String queryString = storedQuery.query();
			CFWQueryExecutor executor = new CFWQueryExecutor();
			
			ArrayList<CFWQuery> queryList = executor.parseQuery(queryString, this.parent.getContext(), false);
			if(queryList == null) {
				outQueue.addAll(inQueue);
			}else {
				
				//--------------------------
				// Filter Commands
				for(CFWQuery query : queryList) {
					for(String commandName : commandsToRemove) {
						query.removeCommandsByName(commandName);
					}
					
					// add a set command at the end to have at least one command that reads records
					// else result might be empty if a user executes a query that does not read the records.
					query.addCommand(new CFWQueryCommandSet(query));
				}
				
				//--------------------------
				// Execute
				executor.executeAll(queryList, this.inQueue, this.outQueue);

			}
			
			this.setDone();
		}
	}

}

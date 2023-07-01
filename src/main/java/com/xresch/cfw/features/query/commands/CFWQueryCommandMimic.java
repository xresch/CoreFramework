package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandMimic extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "mimic";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandMimic.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	
	private String queryName = null;
	private ArrayList<String> commandsToRemove = new  ArrayList<String>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandMimic(CFWQuery parent) {
		super(parent);
		commandsToRemove.add(CFWQueryCommandMetadata.COMMAND_NAME);
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
	public String descriptionShort() {
		return "Mimics the behavior of another query.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" name=<queryName> remove=<commandNames>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			  "<p><b>queryName:&nbsp;</b>(Optional) Name of the query to be mimicked. Names are set with metadata command. If none is given, the query preceeding this one is used.</p>"
			+ "<p><b>commandNames:&nbsp;</b>(Optional) String or array of command names to be removed. (Default='metadata')</p>"
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
		//------------------------------------------
		// Get Parameters
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);

			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction()  throws Exception {
		//------------------------------------------
		// Get Parameters
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();
				if		 (assignmentName.equals("name")) {		queryName = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("remove")) {	commandsToRemove = assignmentValue.getAsStringArray(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported argument.", -1);
				}
				
			}
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
				
		if(this.isPreviousDone()) {
			
			//=====================================
			// Get Result to Mimic
			CFWQueryResult resultToMimic = null;
			
			if(!Strings.isNullOrEmpty(queryName)) {
				//--------------------------
				// Take by name
				resultToMimic = this.parent.getContext().getResultByName(queryName);
			}else {
				//--------------------------
				// Take previous
				CFWQueryResultList resultList = this.parent.getContext().getResultList();
				if(resultList.size() != 0) {
					resultToMimic = resultList.get(resultList.size()-1);
				}
			}
			
			//=====================================
			// Execute Mimicry
			String queryString = resultToMimic.getQueryContext().getOriginalQueryString();
			CFWQueryExecutor executor = new CFWQueryExecutor();
			
			ArrayList<CFWQuery> queryList = executor.parseQuery(queryString, this.parent.getContext(), false);
			if(queryList == null) {
				this.parent.cancelExecution();
			}else {
				//--------------------------
				// Filter Commands
				for(CFWQuery query : queryList) {
					for(String commandName : commandsToRemove) {
						query.removeCommandsByName(commandName);
					}
				}
				//--------------------------
				// Execute
				executor.executeAll(queryList, this.inQueue, this.outQueue);

			}
			
			this.setDone();
		}
		
	}

}

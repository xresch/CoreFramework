package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandMimic extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandMimic.class.getName());
	
	CFWQuerySource source = null;
	String queryName = null;
	ArrayList<String> commandsToRemove = new  ArrayList<String>();
	HashSet<String> encounters = new HashSet<>();
	
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
		return new String[] {"mimic"};
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
		return "mimic name=<queryName> remove=<commandNames>";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_mimic.html");
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
						//--------------------------------------------------
						// Resolve Fieldname=Function
						QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
						String assignmentName = assignment.getLeftSideAsString(null);
						QueryPartValue assignmentValue = ((QueryPartAssignment) currentPart).determineValue(null);
						
						if(assignmentName != null) {
							assignmentName = assignmentName.trim().toLowerCase();
							if		 (assignmentName.equals("name")) {			queryName = assignmentValue.getAsString(); }
							else if	 (assignmentName.equals("remove")) {	commandsToRemove = assignmentValue.getAsStringArray(); }
			
							else {
								parser.throwParseException("mimic: Unsupported argument.", currentPart);
							}
							
						}
						
					}else {
						parser.throwParseException("mimic: Only assignment expressions(key=value) allowed.", currentPart);
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
	public void initializeAction() {
		// nothing todo
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

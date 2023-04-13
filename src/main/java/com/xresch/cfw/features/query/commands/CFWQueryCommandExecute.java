package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryCommandExecute extends CFWQueryCommand {
	

	ArrayList<QueryPartFunction> functions = new ArrayList<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandExecute(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"execute", "exec"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Executes one or multiple functions.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "execute function(params...) [function(params...) ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>function:&nbsp;</b>A function call to be executed.</p>"
			  +"<p><b>params:&nbsp;</b>Parameters of the function.</p>"
			  +"</p>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_execute.html");
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
			
			if(currentPart instanceof QueryPartFunction) {
				functions.add((QueryPartFunction)currentPart);
			}else {
				parser.throwParseException("execute: Only functions calls are allowed.", currentPart);
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
	public void execute(PipelineActionContext context) throws Exception {
		
		//-------------------------------
		// Executed Functions once
		EnhancedJsonObject tempobject = new EnhancedJsonObject();
		
		for(QueryPartFunction function : functions) {
			System.out.println("execute function: "+function.determineValue(tempobject));
			function.determineValue(tempobject);
		}
		
		//execute once only
		functions.clear();
		
		//-------------------------------
		// Pass through incoming records
		while(keepPolling()) {
			outQueue.add(inQueue.poll());
		}
		
		this.setDoneIfPreviousDone();
	
	}

}

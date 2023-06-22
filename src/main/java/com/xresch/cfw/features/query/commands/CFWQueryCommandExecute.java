package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandExecute extends CFWQueryCommand {
	

	private ArrayList<QueryPartFunction> functions = new ArrayList<>();
	private boolean isExecuted = false;
	
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
		return "Executes one or multiple functions exactly once. This command does not have access to any data of any records. ";
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
	
	/****************************************************************************
	 * Override to make the inQueue the outQueue
	 ****************************************************************************/
	@Override
	public PipelineAction<EnhancedJsonObject, EnhancedJsonObject> setOutQueue(LinkedBlockingQueue<EnhancedJsonObject> out) {

		this.inQueue = out;
		
		if(previousAction != null) {
			previousAction.setOutQueue(out);
		}
		
		return this;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		//-------------------------------
		//execute functions only once
		if(isExecuted) {
			this.setDoneIfPreviousDone();
			return;
		}
		
		//-------------------------------
		// Executed Functions
		EnhancedJsonObject tempobject = new EnhancedJsonObject();
		
		for(QueryPartFunction function : functions) {
			function.determineValue(tempobject);
		}
		
		functions.clear();
		isExecuted = true;
	
	}

}

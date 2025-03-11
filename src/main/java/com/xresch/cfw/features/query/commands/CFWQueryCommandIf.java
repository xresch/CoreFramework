package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandIf extends _CFWQueryCommandFlowControl {
	
	public static final String COMMAND_NAME = "if";

	private _CFWQueryCommandFlowControl targetCommand;
	private QueryPartGroup evaluationGroup;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandIf(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
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
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Executes or skips a block of commands based on conditions.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname><operator><value> [<fieldname><operator><value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return _CFWQueryCommon.getFilterOperatorDescipriontHTML();
		
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
		
		if(evaluationGroup == null) {
			evaluationGroup = new QueryPartGroup(parent.getContext());
		}
		
		_CFWQueryCommon.createFilterEvaluatiooGroup(parser, parts, COMMAND_NAME, evaluationGroup);;
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// TODO Auto-generated method stub
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		//----------------------------------------
		// Find Target Command
		PipelineAction<?, ?> nextAction = this.getNextAction();
		
		// used to skip nested if-statement blocks
		int openIfBlockCounter = 0;
		
		while(nextAction != null) {
			
			if(nextAction instanceof _CFWQueryCommandFlowControl) {
				
				if(nextAction instanceof CFWQueryCommandIf) {
					openIfBlockCounter++;
				}else if(openIfBlockCounter > 0
					&& nextAction instanceof CFWQueryCommandEnd) {
					openIfBlockCounter--;
				}else if(openIfBlockCounter == 0) {
					// else, elseif, or end
					targetCommand = (_CFWQueryCommandFlowControl)nextAction;
					break;
				}
			}
			
			nextAction =  nextAction.getNextAction();
		}
		
		
		if(targetCommand == null) {
			throw new Exception(COMMAND_NAME+": could not find a matching end or target for the if command(else, elseif or end).");
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void putIntoFlowControlQueue(EnhancedJsonObject object) throws Exception {
		// if statement will just take anything into 
		this.inQueue.put(object);
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			if(evaluationGroup == null || evaluationGroup.size() == 0) {
				outQueue.add(record);
			}else {

				QueryPartValue evalResult = evaluationGroup.determineValue(record);
				
				if(evalResult.getAsBoolean()) {
					//if matches give to next command in pipeline
					outQueue.add(record);
				}else {
					// if not matched give to else, elseif or end
					targetCommand.putIntoFlowControlQueue(record);
				}
				
			}
		}
		
		if(this.isPreviousDone()) {
			targetCommand.putIntoFlowControlQueue(null);
			this.setDone();
		}
	
	}

}

package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
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
public class CFWQueryCommandElse extends _CFWQueryCommandFlowControl {
	
	public static final String COMMAND_NAME = "else";

	private _CFWQueryCommandFlowControl targetCommand;
	private _CFWQueryCommandFlowControl endCommand;
	private QueryPartGroup evaluationGroup;
	
	protected LinkedBlockingQueue<EnhancedJsonObject> flowControlQueue = new LinkedBlockingQueue<>();
	protected boolean lastObjectReceived =  false;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandElse(CFWQuery parent) {
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
		return "Creates an else or elseif block as part of an if-statement.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" [<fieldname><operator><value> ...]";
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
		// Find Target elseif / else / end
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
		
		//----------------------------------------
		// Find end command
		nextAction = this.getNextAction();
		openIfBlockCounter = 0;
		
		while(nextAction != null) {
			
			if(nextAction instanceof _CFWQueryCommandFlowControl) {
				
				if(nextAction instanceof CFWQueryCommandIf) {
					openIfBlockCounter++;
				}else if(openIfBlockCounter > 0
					&& nextAction instanceof CFWQueryCommandEnd) {
					openIfBlockCounter--;
				}else if(openIfBlockCounter == 0
				&& nextAction instanceof CFWQueryCommandEnd) {
					endCommand = (_CFWQueryCommandFlowControl)nextAction;
					break;
				}
			}
			
			nextAction =  nextAction.getNextAction();
			
		}
		
		//----------------------------------------
		// Find end command
		if(targetCommand == null && endCommand != null) {
			targetCommand = endCommand;
		}
		
		//----------------------------------------
		// Check for missing target
		if(targetCommand == null) {
			throw new Exception(COMMAND_NAME+": could not find a matching end or target for the elseif-block(else, elseif or end).");
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void putIntoFlowControlQueue(EnhancedJsonObject object) throws Exception {
		if(object != null) {
			flowControlQueue.put(object);
		}else {
			lastObjectReceived = true;
		}
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//----------------------------------
		// From Previous Command 
		while(keepPolling()) {
			// already handled, give to end command
			endCommand.putIntoFlowControlQueue(inQueue.poll());
			
		}
		
		//----------------------------------
		// From Previous Flow Control 
		while(!flowControlQueue.isEmpty()) {
			
			EnhancedJsonObject record = flowControlQueue.poll();
			
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
		
		
		
		if(lastObjectReceived && this.isPreviousDone()) {
			targetCommand.putIntoFlowControlQueue(null);
			this.setDone();
		}
		
		
	}

}

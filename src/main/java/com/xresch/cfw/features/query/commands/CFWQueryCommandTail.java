package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.collect.EvictingQueue;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandTail extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "tail";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandTail.class.getName());
	
	private int numberOfRecords = 100;
	
	private EvictingQueue<EnhancedJsonObject> sizedQueue;

	private int recordCounter = 0;
	private ArrayList<QueryPart> parts;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandTail(CFWQuery parent) {
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
	public String descriptionShort() {
		return "Takes the last N records and ignores the rest.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" [<number>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>number:&nbsp;</b>The number of records to pass to the next command.(Optional, default is 100) </p>";
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
		// keep default

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		//------------------------------------------
		// Get Fieldnames
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartValue) {
				
				QueryPartValue parameter = (QueryPartValue)part;

				if(parameter.isInteger()) {
					numberOfRecords = parameter.getAsInteger();
					
					// ignore the rest
					break;
				}
				
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isNumberOrNumberString()) {
					numberOfRecords = value.getAsInteger();
				}else if(!value.isNull()) {
					throw new ParseException(COMMAND_NAME+": parameter must be an integer value.", part.position());
				}
			}	
		}
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		if(sizedQueue == null) {
			sizedQueue = EvictingQueue.create(numberOfRecords);
		}
		
		while(keepPolling()) {
			recordCounter++;
			sizedQueue.add(inQueue.poll());
		}
		
		if(isPreviousDone() && inQueue.isEmpty()) {
			
			while(!sizedQueue.isEmpty()) {
				outQueue.add(sizedQueue.poll());
			}
			
			this.setDone();
		}
				
	}

}

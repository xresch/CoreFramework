package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandSkip extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "skip";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandTop.class.getName());
	
	private int recordsToSkip = 1;
	private int recordCounter = 1;
	
	private LinkedBlockingQueue<EnhancedJsonObject> bufferingQueue = new LinkedBlockingQueue<>();
	
	private boolean isFirst = false;
	
	private ArrayList<QueryPart> parts;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandSkip(CFWQuery parent) {
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
	public String descriptionShort() {
		return "Takes the first N records and ignores the rest.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" [<recordsToSkip>] [first=<first>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>recordsToSkip:&nbsp;</b>(Optional) The number of records to skip.(Default: 1) </li>"
					+"<li><b>first:&nbsp;</b>(Optional) Set this to true to skip the first records instead of the last ones.(Default: false) </li>"
				+"<ul>"
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
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				
				if(paramName.toLowerCase().equals("first")) {
					isFirst = parameter.getRightSide().determineValue(null).getAsBoolean();
				}else {
					throw new ParseException(COMMAND_NAME+": Unknown parameter '"+paramName+"'.", part.position());
				}
				
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isNumberOrNumberString()) {
					recordsToSkip = value.getAsInteger();
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
		
		//---------------------------------
		// Do the Thing
		if( ! isFirst ) {
			while(keepPolling()) {
				//-------------------------------
				// Skip the Last N records
				bufferingQueue.add(inQueue.poll());
				if(recordsToSkip < bufferingQueue.size()) {
					outQueue.add(bufferingQueue.poll());
				}
			}
		}else {
			while(keepPolling()) {
				//-------------------------------
				// Skip the First N records
				
				EnhancedJsonObject current = inQueue.poll();
				if(recordsToSkip < recordCounter ) {
					outQueue.add(current);
				}
				recordCounter++;
			}
		}

		//---------------------------------
		// Set Done
		if(isPreviousDone() && inQueue.isEmpty()) {
			this.setDone();
		}
		
	}

}

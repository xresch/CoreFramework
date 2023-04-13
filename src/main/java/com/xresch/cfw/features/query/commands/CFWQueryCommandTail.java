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
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandTail.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> fieldnames = new ArrayList<>();
	
	int numberOfRecords = 100;
	
	EvictingQueue<EnhancedJsonObject> sizedQueue;

	
	int recordCounter = 0;
	
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
		return new String[] {"tail", "last"};
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
		return "tail [<number>]";
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
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_tail.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
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
				throw new ParseException("tail: parameter must be an integer value.", part.position());
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

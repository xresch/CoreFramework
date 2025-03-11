package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandMove extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "move";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandMove.class.getName());
	
	private ArrayList<QueryPart> parts;

	private ArrayList<String> fieldsToMove = new ArrayList<>();
	private String beforeField = null;
	private String afterField = null;
	
	private LinkedBlockingQueue<EnhancedJsonObject> queuingQueue = new LinkedBlockingQueue<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandMove(CFWQuery parent) {
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
		tags.add(_CFWQueryCommon.TAG_FORMAT);
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Moves the specified fields before or after a defined field.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" before=<before> after=<after> <fieldname> [, <fieldname>, <fieldname>...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				+"<li><b>before:&nbsp;</b>The name of the field where the other fields should be placed before.</li>"
				+"<li><b>after:&nbsp;</b>The name of the field where the other fields should be placed after.</li>"
				+"<li><b>fieldname:&nbsp;</b> Names of the fields that should be moved.</li>"
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
		
		//------------------------------------------
		// Get Fieldnames
		if(parts.size() == 0) {
			throw new ParseException(COMMAND_NAME+": please specify at least one fieldname.", -1);
		}
		
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
					
						case "before": beforeField = assignmentValue.getAsString(); 
						case "after":  afterField = assignmentValue.getAsString(); 
						break;
						
						default: throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
					}

				}
			}else if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;
				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						fieldsToMove.add(element.getAsString());
					}
				}
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isJsonArray()) {
					fieldsToMove.addAll(value.getAsStringArray());
				}else if(!value.isNull()) {
					fieldsToMove.add(value.getAsString());
				}
			}
		}
		
		//--------------------------------------
		// Sanitize
		
		// ignore after if both are specificed
		if(beforeField != null && afterField != null) {
			afterField = null;
		}
		
		// remove before after from fields to move
		fieldsToMove.remove(beforeField);
		fieldsToMove.remove(afterField);
			
		
		

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//-----------------------------------
		// Read all the records an store them
		// temporarily, making sure all 
		// fieldnames have been populated
		while(keepPolling()) {
			queuingQueue.add(inQueue.poll());	
		}
		
		//-----------------------------------
		// After All Previous Done
		if(this.isPreviousDone()) {
		
			//--------------------------------
			// Move Fields
			if( !fieldsToMove.isEmpty()) {
				LinkedHashSet<String> fieldnames = this.fieldnameGetAll();
				fieldnames.removeAll(fieldsToMove);		
				if(afterField != null && fieldnames.contains(afterField)) {
					
					this.fieldnameClearAll();
					
					for(String current : fieldnames) {
						if(!current.equals(afterField)) {
							this.fieldnameAdd(current);
						}else {
							this.fieldnameAdd(afterField);
							this.fieldnameAddAll(fieldsToMove);
						}
					}
								
				}else if(beforeField != null && fieldnames.contains(beforeField)) {
					
					this.fieldnameClearAll();
					for(String current : fieldnames) {
						if( !current.equals(beforeField) ) {
							this.fieldnameAdd(current);
						}else {
							this.fieldnameAddAll(fieldsToMove);
							this.fieldnameAdd(beforeField);
						}
					}
					
				}
			}
			
			//--------------------------------
			// Send Records to out Queue
			outQueue.addAll(queuingQueue);	
			
			this.setDone();
		
		}
	}

}

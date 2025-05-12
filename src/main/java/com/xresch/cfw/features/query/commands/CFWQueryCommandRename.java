package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandRename extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "rename";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandRename.class.getName());
	
	private ArrayList<QueryPart> parts;
	
	private boolean isRenamed = false;
	
	// key:oldname / value: newname
	private JsonObject fieldnameMap = new JsonObject();
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandRename(CFWQuery parent) {
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
		tags.add(_CFWQueryCommon.TAG_FORMAT);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Renames the specified fields.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname>=<newname> [, <fieldname>=<newname> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b>The current name of the field.</p>"
			  +"<p><b>newname:&nbsp;</b>The new name of the field.</p>"
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
		//------------------------------------------
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
		
		for(QueryPart currentPart : parts) {

			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				
				String oldName = assignment.getLeftSideAsString(null);
				QueryPartValue newNamePart = assignment.getRightSide().determineValue(null);
				if(newNamePart.isString()) {
					String newName = newNamePart.getAsString();
					
					if(newName == null) {
						throw new ParseException(COMMAND_NAME+": New name cannot be null.", assignment.position());
					}
					if(CFW.Security.containsSequence(newName, "<", ">", "\"", "&")) {
						throw new ParseException(COMMAND_NAME+": New name cannot contain the following characters: < > \" &", assignment.position());
					}
					fieldnameMap.addProperty(oldName, newName);
				}
								
			}else {
				throw new ParseException(COMMAND_NAME+": Only key value pairs are allowed.", -1);
			}
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//-----------------------------
		// Do this here to support if/else commands
		if(!isRenamed) {
			for(Entry<String, JsonElement> entry : fieldnameMap.entrySet()) {
				this.fieldnameRename(entry.getKey(), entry.getValue().getAsString());
			}
			
			isRenamed = true;
		}
		
		//-----------------------------
		// Do renaming
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
				
			for(String oldName : fieldnameMap.keySet()) {
				if( record.has(oldName) ) {
					record.add( fieldnameMap.get(oldName).getAsString(), record.remove(oldName) );
				}
			}
			
			outQueue.add(record);
			
		}
				
		this.setDoneIfPreviousDone();
	
	}

}

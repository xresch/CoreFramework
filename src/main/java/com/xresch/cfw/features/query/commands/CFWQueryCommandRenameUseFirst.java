package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class CFWQueryCommandRenameUseFirst extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "renameUseFirst";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandRenameUseFirst.class.getName());
	
	private ArrayList<QueryPart> parts;
	
	private boolean isFirstLineRead = false;
	
	// key:oldname / value: newname
	private JsonObject fieldnameMap = new JsonObject();
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandRenameUseFirst(CFWQuery parent) {
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
		return "Renames all the fields by using the first row's values as the names.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+"";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p>This command does not take any parameters.</p>";
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
		
		// nothing todo
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
	
		//-----------------------------
		// Get Old and New Names
		// Also do this here to support if/else commands
		HashMap<String, Integer> occurrenceCounter = new HashMap<>();
		
		if(!isFirstLineRead && ! inQueue.isEmpty() ) {
			
			isFirstLineRead = true;
			
			EnhancedJsonObject record = inQueue.poll();
			for(Entry<String, JsonElement> entry : record.getWrappedObject().entrySet()) {
				String oldName = entry.getKey();
				String newName = QueryPartValue.newFromJsonElement(entry.getValue()).getAsString();
				
				if( ! occurrenceCounter.containsKey(newName) ) {
					fieldnameMap.addProperty(oldName, newName);
					occurrenceCounter.put(newName, 1);
				}else {
					
					int occurrence = occurrenceCounter.get(newName);

					occurrence++;
					
					fieldnameMap.addProperty(oldName, newName+"("+occurrence+")" );
					occurrenceCounter.put(newName, occurrence);
				}
			}
			
			//------------------------
			// Set New Names
			for(Entry<String, JsonElement> entry : fieldnameMap.entrySet()) {
				this.fieldnameRename(entry.getKey(), entry.getValue().getAsString());
			}

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

package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandRename extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandRename.class.getName());
	
	CFWQuerySource source = null;
	
	// key:oldname / value: newname
	JsonObject fieldnameMap = new JsonObject();
		
	int recordCounter = 0;
	
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
		return new String[] {"rename"};
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
		return "rename <fieldname>=<newname> [, <fieldname>=<newname> ...]";
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
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_rename.html");
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
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				
				String oldName = assignment.getLeftSideAsString(null);
				QueryPartValue newNamePart = assignment.getRightSide().determineValue(null);
				if(newNamePart.isString()) {
					String newName = newNamePart.getAsString();
					
					if(newName == null) {
						throw new ParseException("rename: New name cannot be null.", assignment.position());
					}
					if(CFW.Security.containsSequence(newName, "<", ">", "\"", "&")) {
						throw new ParseException("rename: New name cannot contain the following characters: < > \" &", assignment.position());
					}
					fieldnameMap.addProperty(oldName, newName);
				}
								
			}else {
				parser.throwParseException("rename: Only key value pairs are allowed.", currentPart);
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
	public void initializeAction() {
		for(Entry<String, JsonElement> entry : fieldnameMap.entrySet()) {
			this.fieldnameRename(entry.getKey(), entry.getValue().getAsString());
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		
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

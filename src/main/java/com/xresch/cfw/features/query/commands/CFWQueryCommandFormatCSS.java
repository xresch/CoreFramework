package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryResult;
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
public class CFWQueryCommandFormatCSS extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "formatcss";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatCSS.class.getName());
	
	private ArrayList<QueryPartAssignment> displaySettingsParts = new ArrayList<>();
	private QueryPartAssignment fieldsPart = null; 
	private QueryPartAssignment stylePart = null; 
	private QueryPartAssignment widthPart = null; 
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatCSS(CFWQuery parent) {
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
		return "Formats the specified fields with the defined css style.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" fields=<fields> style=<style>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return """
			  <ul>
			  	<li><b>fields:&nbsp;</b>Array of the fieldnames these display settings should be applied too.</li>
			  	<li><b>style:&nbsp;</b>The CSS style that should be applied to the field.</li>
			  </ul>
			  """
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
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment zePart = (QueryPartAssignment)currentPart;
				String partName = zePart.getLeftSideAsString(null);
				
				if(partName.toLowerCase().equals("fields")) {
					fieldsPart = zePart;
				}else if(partName.toLowerCase().equals("style")) {
					stylePart = zePart;
				}else {
					this.displaySettingsParts.add((QueryPartAssignment)currentPart);
					continue;
				}

			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
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
	public void initializeAction() throws Exception {
		ArrayList<String> fieldnames = fieldsPart.determineValue(null).getAsStringArray(); // determineValue(null): do not convert fieldnames to field values 
		CFWQueryCommandFormatField.addFormatterByName(this.getQueryContext(), fieldnames, "special"); 
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
				
				//-------------------------------------
				// Get Values
				ArrayList<String> fieldnames = fieldsPart.determineValue(null).getAsStringArray(); // determineValue(null): do not convert fieldnames to field values 
				
				String style = "";
				if(stylePart != null) {
					style = stylePart.determineValue(record).getAsString(); 
				}

				
				//-------------------------------------
				// Iterate Fieldnames
				String label = null;
				for(String fieldname : fieldnames) {
					
					//-------------------------------------
					// Create Label
					if(!record.has(fieldname)) {
						continue;
					}
										
					//-------------------------------------
					// Create object for Special Formatter
					JsonObject specialObject = new JsonObject();

					specialObject.addProperty("format", "css");
					specialObject.addProperty("style", style);
					specialObject.add("value", record.get(fieldname));
					
					//-------------------------------------
					// Replace Value 
					record.add(fieldname, specialObject);

				}
				
			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}

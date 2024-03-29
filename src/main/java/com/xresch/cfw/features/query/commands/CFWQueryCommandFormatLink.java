package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
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
public class CFWQueryCommandFormatLink extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "formatlink";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatLink.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	private QueryPartAssignment newtabPart = null; 
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatLink(CFWQuery parent) {
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
		return "Formats the specified fields as links.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname>=<url> [, <fieldname>=<url> ...] newtab=<newtab>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b>The  name of the field that should me made into a link.</p>"
			  +"<p><b>url:&nbsp;</b>The URL that should be opened by the link.</p>"
			  +"<p><b>newtab:&nbsp;</b>Define if the URL should be opened in a new tab (Default:true).</p>"
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
				if(!zePart.getLeftSideAsString(null).equals("newtab")) {
					this.assignmentParts.add((QueryPartAssignment)currentPart);
				}else {
					newtabPart = zePart;
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
		
		HashSet<String> fieldnames = new HashSet<>();
		
		for(QueryPartAssignment assignment : assignmentParts) {

			QueryPart leftside = assignment.getLeftSide();
			QueryPartValue leftValue = leftside.determineValue(null);
			
			fieldnames.addAll(leftValue.getAsStringArray());
			
		}
		
		CFWQueryCommandFormatField.addFormatterByName(this.getQueryContext(), fieldnames, "special"); 

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
				
			for(QueryPartAssignment assignment : assignmentParts) {

				//-------------------------------------
				// Get Values
				QueryPart fieldnamePart = assignment.getLeftSide();
				ArrayList<String> fieldnames = fieldnamePart.determineValue(null).getAsStringArray(); // determineValue(null): do not convert fieldnames to field values 
				String url = assignment.getRightSide()
										.determineValue(record)
										.convertFieldnameToFieldvalue(record)
										.getAsString();
				
				//-------------------------------------
				// Iterate Fieldnames
				String label = null;
				for(String fieldname : fieldnames) {
					
					//-------------------------------------
					// Create Label
					if(record.has(fieldname)) {
						JsonElement field = record.get(fieldname);
						if(field.isJsonPrimitive()) {
							label = CFW.JSON.toString(field);
						}
					}
					
					if(label == null) {
						label = url;
					}
					
					//-------------------------------------
					// Create object for Special Formatter
					JsonObject specialObject = new JsonObject();
					boolean newtab = true;
					if(newtabPart != null) { newtab = newtabPart.determineValue(record).getAsBoolean(); }
					
					specialObject.addProperty("format", "link");
					specialObject.addProperty("label", label);
					specialObject.addProperty("url", url);
					specialObject.addProperty("newtab", newtab);
					
					//-------------------------------------
					// Replace Value 
					record.add(fieldname, specialObject);

				}
				
			}

			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}

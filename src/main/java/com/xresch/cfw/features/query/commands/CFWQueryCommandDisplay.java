package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
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
public class CFWQueryCommandDisplay extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "display";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandDisplay.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandDisplay(CFWQuery parent) {
		super(parent);
		this.isManipulativeCommand(false);
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
		return "Defines how to display the results.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" as=<asOption> [visiblefields=<arrayOfFieldnames>] [titlefields=<arrayOfFieldnames>]"
				+"\n[titleformat=<titleformat>] [menu=<showMenu>] [zoom=<zoomNumber>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>asOption:&nbsp;</b>One of the following options:</p>"
					+"<ul>"
					+"<li>table</li>"
					+"<li>panels</li>"
					+"<li>cards</li>"
					+"<li>tiles</li>"
					+"<li>statustiles</li>"
					+"<li>statuslist</li>"
					+"<li>statusbar</li>"
					+"<li>statusbarreverse</li>"
					
					+"<li>statusmap</li>"
					
					+"<li>title</li>"
					+"<li>csv</li>"
					+"<li>json</li>"
					+"<li>xml</li>"
					+"</ul>"
				+"<p><b>arrayOfFieldnames:&nbsp;</b>(Optional) Array of the fieldnames.</p>"	
				+"<p><b>titleformat:&nbsp;</b>(Optional) Format of the title. Use '{0}', '{1}'... as placeholders for field values.</p>"	
				+"<p><b>showMenu:&nbsp;</b>(Optional) True or false to toggle the menu and pagination.</p>"	
				+"<p><b>zoomNumber:&nbsp;</b>(Optional) Integer value, zoom in percent to resize the displayed data.</p>"	
				+"<p><b>settings:&nbsp;</b>(Optional) Json Object containing more options for the selected display type.</p>"	
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
	
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);

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
		
		//--------------------------------------
		// Do this here to make the command removable for command 'mimic'
		JsonObject displaySettings = this.getParent().getContext().getDisplaySettings();
	
		for(QueryPartAssignment assignment : assignmentParts) {

			String propertyName = assignment.getLeftSideAsString(null);

			QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
			if(valuePart.isString()) {
				String value = valuePart.getAsString();
				value = CFW.Security.sanitizeHTML(value);
				displaySettings.addProperty(propertyName, value);
			}else {
				valuePart.addToJsonObject(propertyName, displaySettings);
			}
		}
				
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		// Do nothing, inQueue is the same as outQueue
		this.setDoneIfPreviousDone();
	
	}

}

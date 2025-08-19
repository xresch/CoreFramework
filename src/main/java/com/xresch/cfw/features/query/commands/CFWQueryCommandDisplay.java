package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
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
public class CFWQueryCommandDisplay extends CFWQueryCommand {
	
	
	public static final String DESCIRPTION_SYNTAX = " as=<asOption> [menu=<menu>] [pagination=<pagination>]  [visiblefields=<visiblefields>] [titlefields=<titlefields>]"
			+"\n[titleformat=<titleformat>]  [zoom=<zoomNumber>]";
	
	public static final String DESCRIPTION_SYNTAX_DETAILS = "<ul>"
			+"<!-- placeholder -->"
			+"<li><b>asOption:&nbsp;</b>Defines how the data should be displayed. One of the following options:"
				+"<ul>"
					+"<li>table</li>"
					+"<li>panels</li>"
					+"<li>properties</li>"
					+"<li>cards</li>"
					+"<li>tiles</li>"
					+"<li>tileandbar</li>"
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
			+"</li>"
			+"<li><b>menu:&nbsp;</b>(Optional) Defines how the menu should be displayed. One of the following options:"
				+"<ul>"
					+"<li>default (or true)</li>"
					+"<li>button</li>"
					+"<li>none (or false)</li>"
				+"</ul>"
			+"</li>"
			+"<li><b>pagination:&nbsp;</b>(Optional) Defines how the pagination should be displayed. One of the following options:"
				+"<ul>"
				+"<li>both (or true)</li>"
				+"<li>top</li>"
				+"<li>bottom</li>"
				+"<li>none (or false)</li>"
				+"</ul>"
			+"</li>"
			+"<li><b>titlefields:&nbsp;</b>(Optional) Array of the fieldnames used for title.</li>"	
			+"<li><b>titleformat:&nbsp;</b>(Optional) Format of the title. Use '{0}', '{1}'... as placeholders for field values.</li>"	
			+"<li><b>visiblefields:&nbsp;</b>(Optional) Array of the fieldnames that should be visible.</li>"	
			+"<li><b>zoomNumber:&nbsp;</b>(Optional) Integer value, zoom in percent to resize the displayed data.</li>"	
			+"<li><b>sizes:&nbsp;</b>(Optional) An array of page sizes.</li>"	
			+"<li><b>defaultsize:&nbsp;</b>(Optional) The default selected page size.</li>"	
			+"<li><b>sticky:&nbsp;</b>(Optional) Makes the headers of tables stick when scrolling and keep them visible.</li>"	
			+"<li><b>download:&nbsp;</b>(Optional) Toggle the data download button in the menu. This button does not work with 'menu=button' (Default:false).</li>"	
			+"<li><b>store:&nbsp;</b>(Optional) Toggle the data store button in the menu. This button does not work with 'menu=button' (Default:false).</li>"	
			+"<li><b>settings:&nbsp;</b>(Optional) Json Object containing more options for the selected display type.</li>";

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
		return "Defines how to display the results.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+DESCIRPTION_SYNTAX;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return  DESCRIPTION_SYNTAX_DETAILS	
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
		//--------------------------------
		// All Paramneters
		AutocompleteList list = new AutocompleteList();
		result.addList(list);
		
		list.addItem(
			helper.createAutocompleteItem("", 
"\n\t"+
				  """
as 				= table
	menu			= true # false, button
	pagination		= true # false, top, bottom
	download 		= true
	#titlefields 	= [FIELDNAME, FIELDNAME] 
	#titleformat   	= "{0}: {2} {1} ({3})"
	#visiblefields 	= [FIELDNAME, FIELDNAME] 
	#zoom			= 100 # percentage
	#sizes			= [10, 25, 50, 100, 500, 1000] # page sizes
	#defaultsize		= 50 # default page size
	#settings 		= {} # experimental option
				  """
					, "All Parameters"
					, "Template that contains all parameters"
				)
		);
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

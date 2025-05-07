package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
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
public class CFWQueryCommandFormatQuery extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "formatquery";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatQuery.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	private QueryPart field = null; 
	private QueryPart icon = null; 
	private QueryPart label = null; 
	private QueryPart query = null; 
	//private QueryPart earliest = null; 
	//private QueryPart latest = null; 
	private QueryPart title = null; 

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatQuery(CFWQuery parent) {
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
		return "Formats the specified fields as links.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <param>=<value> <param>=<value> ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return """
			  <ul>
			  	<li><b>query:&nbsp;</b>The query that should be executed on button click.</li>
			  	<li><b>field:&nbsp;</b>(Optional) The name of the new field that will contain the button to execute the query.(Default: 'More')</li>
			  	<li><b>icon:&nbsp;</b>(Optional) The fontawesome icon for the button. ( Default: 'fa-plus-circle')</li>
			  	<li><b>label:&nbsp;</b>(Optional) The label for the button. (Default: '')</li>
			  	<li><b>title:&nbsp;</b>(Optional) The title of the modal panel.(Default: 'Details')</li>
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
		
		//------------------------------------------
		// Get Parameters
		//------------------------------------------
		for(QueryPartAssignment zePart : assignmentParts) {
			String partName = zePart.getLeftSideAsString(null).toLowerCase();
			QueryPart rightSide = zePart.getRightSide();
			//QueryPartValue rightSideValue = rightSide.determineValue(null);
			
			switch(partName) {
				case "field":		field = rightSide; 		break;
				case "icon":		icon = rightSide; 		break;
				case "label":		label = rightSide; 		break;
				case "query":		query = rightSide; 		break;
				//case "earliest":	earliest = rightSide; 	break;
				//case "latest":		latest = rightSide; 	break;
				case "title":		title = rightSide; 		break;

				default:
					throw new IllegalArgumentException(COMMAND_NAME+": unknown parameter '"+partName+"'.");
					
			}
			
		}
		
		//------------------------------------------
		// Set Defaults
		//------------------------------------------
		
		if(field == null) {		field = QueryPartValue.newString("More"); }
		if(icon == null) {		icon = QueryPartValue.newString("fa-plus-circle"); }
		if(label == null) {		label = QueryPartValue.newNull(); }
		if(query == null) {		query = QueryPartValue.newString(""); }
		//if(earliest == null){	earliest = QueryPartValue.newNumber( this.getQueryContext().getEarliestMillis() ); }
		//if(latest == null) {	latest = QueryPartValue.newNumber( this.getQueryContext().getLatestMillis() ); }
		if(title == null) {		title  = QueryPartValue.newString("Details"); }

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		HashSet<String> newFields = new HashSet<>(); 
		
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
				
			//-------------------------------------
			// Get Fieldname
			
			String fieldname = null;
			if(field != null) {
				fieldname = field.determineValue(record).getAsString(); 
			}else {
				fieldname = "Details";
			}
			
			newFields.add(fieldname);
			
			//-------------------------------------
			// Get Values

			
			
			//-------------------------------------
			// Create object for Special Formatter
			JsonObject specialObject = new JsonObject();

			specialObject.addProperty("format", "subquery");
			specialObject.addProperty("icon", icon.determineValue(record).getAsString());
			specialObject.addProperty("label", label.determineValue(record).getAsString());
			specialObject.addProperty("query", query.determineValue(record).getAsString());
			//specialObject.addProperty("earliest", earliest.determineValue(record).getAsNumber());
			//specialObject.addProperty("latest", latest.determineValue(record).getAsNumber());
			specialObject.addProperty("title", title.determineValue(record).getAsString());
			
			//-------------------------------------
			// Replace Value 
			
			record.add(fieldname, specialObject);

			outQueue.add(record);
			
		}

		//---------------------------------
		// Add new Fields
		if(isPreviousDone()) {
			
			for(String field : newFields) {
				CFWQueryCommandFormatField.addFormatterByName(this.getQueryContext(), field, "special"); 
				this.fieldnameAdd(field);
			}
			
			this.setDoneIfPreviousDone();
		}
		
		
	
	}

}

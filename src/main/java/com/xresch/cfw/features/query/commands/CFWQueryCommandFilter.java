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

public class CFWQueryCommandFilter extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFilter.class.getName());
	
	CFWQuerySource source = null;
	
	int recordCounter = 0;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFilter(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"filter", "grep"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Filters the record based on field values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "filter <fieldname><operator><value> [<fieldname><operator><value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b>The name of the field to evaluate the value against.</p>"
			  +"<p><b>value:&nbsp;</b>The value to check.</p>"
			  +"<p><b>operator:&nbsp;</b>The operator, any of:</p>"
			  +"<ul>"
			  + "	<li><b>'=':&nbsp;</b> Checks if the field contains the value.</li>"
			  + "	<li><b>'==':&nbsp;</b> Checks if the values are equal.</li>"
			  + "	<li><b>'!=':&nbsp;</b> Checks if the values are not equal.</li>"
			  + "	<li><b>'&lt;=':&nbsp;</b> Checks if the field value is smaller or equals.</li>"
			  + "	<li><b>'&gt;=':&nbsp;</b> Checks if the field value is greater or equals.</li>"
			  + "	<li><b>'&lt;':&nbsp;</b>  Checks if the field value is smaller.</li>"
			  + "	<li><b>'&gt;':&nbsp;</b>  Checks if the field value is greater.</li>"
			  //+ "	<li><b>AND:&nbsp;</b> Used to combine two or more conditions. Condition matches only if both sides are true.</li>"
			  //+ "	<li><b>OR:&nbsp;</b>  Used to combine two or more conditions. Condition matches if either side is true.</li>"
			  + "</ul>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_filter.html");
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
						throw new ParseException("filter: fieldname name cannot be null.", assignment.position());
					}
					if(CFW.Security.containsSequence(newName, "<", ">", "\"", "&")) {
						throw new ParseException("rename: New name cannot contain the following characters: < > \" &", assignment.position());
					}
					//fieldnameMap.addProperty(oldName, newName);
				}
								
			}else {
				parser.throwParseException("filter: Only binary expressions allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Filter by fields by using binary operators(== != >= <= > <).<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
				
//			for(String oldName : fieldnameMap.keySet()) {
//				if( record.has(oldName) ) {
//					record.add( fieldnameMap.get(oldName).getAsString(), record.remove(oldName) );
//				}
//			}
//			
			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}

package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;

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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryCommandFormatRecord extends CFWQueryCommand {
	
	private static final String FIELDNAME_TEXT_STYLE = "_textcolor";

	private static final String FIELDNAME_BG_STYLE = "_bgcolor";

	ArrayList<ArrayList<QueryPart>> conditions = new ArrayList<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatRecord(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"formatrecord", "recordformat"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Formats the record based on field values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "formatrecord [<condition>, <bgcolor>, <textcolor>] ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>condition:&nbsp;</b>The condition to be true for the record to be colored.</p>"
			  +"<p><b>bgcolor:&nbsp;</b>The color to apply for the background.</p>"
			  +"<p><b>textcolor:&nbsp;</b>The color to apply for the text.(Optional, default is white)</p>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_formatrecord.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// set Style fields
		JsonObject displaySettings = this.getParent().getContext().getDisplaySettings();
		displaySettings.addProperty("bgstylefield", FIELDNAME_BG_STYLE);
		displaySettings.addProperty("textstylefield", FIELDNAME_TEXT_STYLE);
		
	
		//------------------------------------------
		// Get Parameters
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartArray) {
				ArrayList<QueryPart> conditionDefinition = ((QueryPartArray)currentPart).getAsParts();
				if(conditionDefinition.size() > 0) {
					conditions.add(conditionDefinition);
				}
				
				
			}else {
				parser.throwParseException("formatrecord: Only array expression allowed.", currentPart);
			}
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Create conditions using binary operators(== != >= <= > <).<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			if(conditions == null || conditions.size() == 0) {
				outQueue.add(record);
			}else {
				
				for(ArrayList<QueryPart> conditionDefinition : conditions) {

					QueryPart condition = conditionDefinition.get(0);
					QueryPartValue evalResult = condition.determineValue(record);

					if(evalResult.isBoolOrBoolString()) {

						if(evalResult.getAsBoolean()) {

							//-------------------------------
							// Get BG Color
							String bgcolor = ""; 	
							if(conditionDefinition.size() > 1) { 
								bgcolor = conditionDefinition.get(1).determineValue(record).getAsString();
							}
							//-------------------------------
							// Get Text Color
							String textcolor = "white";
							if(conditionDefinition.size() > 2) { 
								textcolor = conditionDefinition.get(2).determineValue(record).getAsString();
							}
							
							record.addProperty(FIELDNAME_BG_STYLE, bgcolor);
							record.addProperty(FIELDNAME_TEXT_STYLE, textcolor);
							break;
						}
					}
				}
				
				outQueue.add(record);
			}
		
		}
		
		this.setDoneIfPreviousDone();
	
	}

}

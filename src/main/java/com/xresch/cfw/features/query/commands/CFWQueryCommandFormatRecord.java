package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandFormatRecord extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "formatrecord";
	private static final String FIELDNAME_TEXT_STYLE = "_textcolor";
	private static final String FIELDNAME_BG_STYLE = "_bgcolor";

	private ArrayList<ArrayList<QueryPart>> conditions = new ArrayList<>();
	
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
		return new String[] {COMMAND_NAME, "recordformat"};
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
		return COMMAND_NAME+" [<condition>, <bgcolor>, <textcolor>] ...";
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
			
			if(currentPart instanceof QueryPartArray) {
				ArrayList<QueryPart> conditionDefinition = ((QueryPartArray)currentPart).getAsParts();
				if(conditionDefinition.size() > 0) {
					conditions.add(conditionDefinition);
				}
				
			}else {
				parser.throwParseException(COMMAND_NAME+": Only array expression allowed.", currentPart);
			}
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		//--------------------------------
		// 5 Color Templates
		AutocompleteList list = new AutocompleteList();
		list.title("5 Colors");
		result.addList(list);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 20), \"cfw-green\"] \r\n"
				+ "	[(VALUE < 40), \"cfw-limegreen\"]  \r\n"
				+ "	[(VALUE < 60), \"cfw-yellow\"] \r\n"
				+ "	[(VALUE < 80), \"cfw-orange\"] \r\n"
				+ "	[true, \"cfw-red\"] \r\n"
				, "Low to High"
				, "Low values are good, high values are bad."
				)
		);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 20), \"cfw-red\"] \r\n"
				+ "	[(VALUE < 40), \"cfw-orange\"]  \r\n"
				+ "	[(VALUE < 60), \"cfw-yellow\"] \r\n"
				+ "	[(VALUE < 80), \"cfw-limegreen\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, " Low to High - Reverse Colors"
				, "Low values are bad, high values are good."
				)
		);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE >= 80), \"cfw-red\"] \r\n"
				+ "	[(VALUE >= 60), \"cfw-orange\"]  \r\n"
				+ "	[(VALUE >= 40), \"cfw-yellow\"] \r\n"
				+ "	[(VALUE >= 20), \"cfw-limegreen\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, "High to Low"
				, "High values are bad, low values are good."
				)
		);
		
		list.addItem(
				helper.createAutocompleteItem("", 
					  "	[(VALUE == null), \"cfw-gray\"] \r\n"
					+ "	[(VALUE >= 80), \"cfw-green\"] \r\n"
					+ "	[(VALUE >= 60), \"cfw-limegreen\"]  \r\n"
					+ "	[(VALUE >= 40), \"cfw-yellow\"] \r\n"
					+ "	[(VALUE >= 20), \"cfw-orange\"] \r\n"
					+ "	[true, \"cfw-red\"] \r\n"
					, "High to Low - Reverse Colors"
					, "High values are good, low values are bad."
					)
			);
		
		//--------------------------------
		// 3 Color Templates
		list = new AutocompleteList();
		list.title("3 Colors");
		result.addList(list);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 33), \"cfw-green\"] \r\n"
				+ "	[(VALUE < 66), \"cfw-yellow\"] \r\n"
				+ "	[true, \"cfw-red\"] \r\n"
				, "Low to High"
				, "Low values are good, high values are bad."
				)
		);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 33), \"cfw-red\"] \r\n"
				+ "	[(VALUE < 66), \"cfw-yellow\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, " Low to High - Reverse Colors"
				, "Low values are bad, high values are good."
				)
		);
			
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE >= 66), \"cfw-red\"] \r\n"
				+ "	[(VALUE >= 33), \"cfw-yellow\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, "High to Low"
				, "High values are bad, low values are good."
				)
		);
		
		list.addItem(
				helper.createAutocompleteItem("", 
					  "	[(VALUE == null), \"cfw-gray\"] \r\n"
					+ "	[(VALUE >= 66), \"cfw-green\"] \r\n"
					+ "	[(VALUE >= 33), \"cfw-yellow\"] \r\n"
					+ "	[true, \"cfw-red\"] \r\n"
					, "High to Low - Reverse Colors"
					, "High values are good, low values are bad."
					)
			);
		
		//--------------------------------
		// 2 Color Templates
		list = new AutocompleteList();
		list.title("2 Colors");
		result.addList(list);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 50), \"cfw-green\"] \r\n"
				+ "	[true, \"cfw-red\"] \r\n"
				, "Low to High"
				, "Low values are good, high values are bad."
				)
		);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE < 50), \"cfw-red\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, " Low to High - Reverse Colors"
				, "Low values are bad, high values are good."
				)
		);
			
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE >= 50), \"cfw-red\"] \r\n"
				+ "	[true, \"cfw-green\"] \r\n"
				, "High to Low"
				, "High values are bad, low values are good."
				)
		);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
				+ "	[(VALUE >= 50), \"cfw-green\"] \r\n"
				+ "	[true, \"cfw-red\"] \r\n"
				, "High to Low - Reverse Colors"
				, "High values are good, low values are bad."
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
		displaySettings.addProperty("bgstylefield", FIELDNAME_BG_STYLE);
		displaySettings.addProperty("textstylefield", FIELDNAME_TEXT_STYLE);
				
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

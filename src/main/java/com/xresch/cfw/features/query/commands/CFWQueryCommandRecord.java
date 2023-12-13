package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandRecord extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "record";

	private ArrayList<ArrayList<QueryPart>> recordArray = new ArrayList<>();
	
	private LinkedHashSet<String> fieldnamesSet = null;
	private boolean names = true;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandRecord(CFWQuery parent) {
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
		return "Creates one or more records and adds them to the end of the queue.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" names=<firstIsNames> [<value>, <value>, <value>, ...] ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>firstIsNames:&nbsp;</b>If true, the first array will contain the field names. If false, uses the detected fieldnames.(Default: true)</p>"
			  +"<p><b>value:&nbsp;</b>The values .</p>"
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
				ArrayList<QueryPart> records = ((QueryPartArray)currentPart).getAsParts();
				if(records.size() > 0) {
					recordArray.add(records);
				}
				
			}else if(currentPart instanceof QueryPartAssignment) {
				
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String propertyName = assignment.getLeftSideAsString(null);
				QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
				
				if(propertyName != null && propertyName.trim().equalsIgnoreCase("names")) {
					if(valuePart.isBoolOrBoolString()) {
						names = valuePart.getAsBoolean();
					}
				}else {
					parser.throwParseException(COMMAND_NAME+": Paramter '"+propertyName+"' not supported.", currentPart);
				}
			}else  {
				parser.throwParseException(COMMAND_NAME+": Only names=<true/false> array expressions allowed.", currentPart);
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
//		AutocompleteList list = new AutocompleteList();
//		list.title("5 Colors");
//		result.addList(list);
//		
//		list.addItem(
//			helper.createAutocompleteItem("", 
//				  "	[(VALUE == null), \"cfw-gray\"] \r\n"
//				+ "	[(VALUE < 20), \"cfw-green\"] \r\n"
//				+ "	[(VALUE < 40), \"cfw-limegreen\"]  \r\n"
//				+ "	[(VALUE < 60), \"cfw-yellow\"] \r\n"
//				+ "	[(VALUE < 80), \"cfw-orange\"] \r\n"
//				+ "	[true, \"cfw-red\"] \r\n"
//				, "Low to High"
//				, "Low values are good, high values are bad."
//				)
//		);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		if(names == true && recordArray.size() > 0) {
			ArrayList<QueryPart> firstRecord = recordArray.get(0);
			recordArray.remove(0);
			fieldnamesSet = new LinkedHashSet<String>();
			
			for(QueryPart namePart : firstRecord) {
				String value = namePart.determineValue(null).getAsString();
				if(value == null) { value = "null"; }
				fieldnamesSet.add(value);
			}
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			outQueue.add(inQueue.poll());
		}
		
		if(this.isPreviousDone()) {
			
			if(fieldnamesSet == null) {
				fieldnamesSet = this.getQueryContext().getFinalFieldnames();
			}
			
			ArrayList<String> fieldnamesArray = new ArrayList<String>();
			fieldnamesArray.addAll(fieldnamesSet);
			
			for(ArrayList<QueryPart>  record : recordArray) {
				
				EnhancedJsonObject newRecord = new EnhancedJsonObject();
				
				for(int i = 0; i < record.size(); i++) {
					
					QueryPartValue valuePart = record.get(i).determineValue(null);
					String fieldname = (i < fieldnamesArray.size()) ? fieldnamesArray.get(i) : "AddedField-"+i;
					newRecord.add(fieldname, valuePart.getAsJsonElement());
					
				}
				
				this.fieldnameAddAll(newRecord);
				this.outQueue.add(newRecord);
				
			}
			
			this.setDone();
		}

	
	}

}

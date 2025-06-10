package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartJsonMemberAccess;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandParseCSV extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "parsecsv";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandParseCSV.class.getName());
	
	private QueryPartArray fieldsToParse;
	
	private ArrayList<QueryPart> parts;
	
	private String separator = ",";
	private String header = null;
	
	private boolean isFirstRecord = true;
	private ArrayList<String> customHeadersArray = null;
	
	// Contains fieldname and the headers for the field
	private HashMap<String,ArrayList<String>> fieldHeadersMap = new HashMap<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandParseCSV(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
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
		tags.add(_CFWQueryCommon.TAG_ARRAYS);
		tags.add(_CFWQueryCommon.TAG_OBJECTS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Replaces records by unboxing one or more of their fields. Uses the value of the fields as new records.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldnameOrPath> [separator=<separator>] [header=<header>] ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>fieldnameOrPath:&nbsp;</b> Fieldnames or JSON paths that should be used as replacement.</li>"
			 	  +"<li><b>separator:&nbsp;</b>(Optional) The separator used in the CSV data.(Default: ',')</li>"
			 	  +"<li><b>header:&nbsp;</b> (Optional) A custom header line for the data (Default: null)</li>"
		 	  +"</ul>"
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
		
		this.parts = parts;
					
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
		
		CFWQueryContext queryContext = this.parent.getContext();
		fieldsToParse = new QueryPartArray(queryContext);
		
		//------------------------------------------
		// Get Fieldnames
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				if(paramName != null && paramName.equals("separator")) {
					QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
					if(paramValue.isString()) {
						this.separator = paramValue.getAsString();
					}
				}
				
				String partName = ((QueryPartAssignment)part).getLeftSideAsString(null).toLowerCase();
				QueryPart rightSide = ((QueryPartAssignment)part).getRightSide();
				//QueryPartValue rightSideValue = rightSide.determineValue(null);
				
				switch(partName) {
					case "separator":	separator = rightSide.determineValue(null).getAsString(); 	break;
					case "header":		header = rightSide.determineValue(null).getAsString();  	break;
					default:
						throw new IllegalArgumentException(COMMAND_NAME+": unknown parameter '"+partName+"'.");
						
				}
				
			}else if(part instanceof QueryPartArray) {

				for(QueryPart element : ((QueryPartArray)part).getAsParts()) {
					
					if(element instanceof QueryPartValue
					|| element instanceof QueryPartJsonMemberAccess) {
						fieldsToParse.add(element);
					}else { /* ignore */ }
					
				}
				
			}else if(part instanceof QueryPartValue
				  || part instanceof QueryPartJsonMemberAccess) {
				fieldsToParse.add(part);
			}else if(part instanceof QueryPartFunction) {
				fieldsToParse.add(part.determineValue(null));
				
			}else { 
				/* ignore */
			}
		}
		
		//------------------------------------
		// getCustomHeaders
		if(header != null &&  ! header.isBlank()) {
			customHeadersArray = CFW.CSV.splitCSVQuotesAware(separator, header);
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//------------------------------------
		// Get Headers from First Record
		if(isFirstRecord) {
			
			if(keepPolling()) {
				
				isFirstRecord = false;
				
				if(customHeadersArray != null) {
					//------------------------------------
					// Use Custom Header For All
					EnhancedJsonObject record = inQueue.peek(); // do not remove 
					for(QueryPart part : fieldsToParse.getAsParts()) {
						
						String fieldname = part.determineValue(record).getAsString();
						
						fieldHeadersMap.put(fieldname, customHeadersArray);
					}
				}else {

					//------------------------------------
					// Parse for everyValue
					EnhancedJsonObject record = inQueue.peek(); // do not remove, yet
					for(QueryPart part : fieldsToParse.getAsParts()) {
						String fieldname = part.determineValue(record).getAsString();
						
						if(record.has(fieldname)) {
							
							QueryPartValue value = QueryPartValue.newFromJsonElement( record.get(fieldname) );
							String headersString = value.getAsString();
							if(headersString != null
							&& ! headersString.contains("\n")) { //ignore multiline
								inQueue.poll(); // remove record as it is a header
								ArrayList<String> fieldHeaders = CFW.CSV.splitCSVQuotesAware(separator, headersString);
								fieldHeadersMap.put(fieldname, fieldHeaders);
							}
							
						}
					}
				}
			}
			
			return;
		}
		
		//------------------------------------
		// Iterate Data
		while(keepPolling()) {
			
			//------------------------------------
			// Get Original Record
			EnhancedJsonObject record = inQueue.poll();

			//------------------------------------
			// Parse for everyValue
			for(QueryPart part : fieldsToParse.getAsParts()) {
				String fieldname = part.determineValue(record).getAsString();
				
				if(record.has(fieldname)) {
					
					QueryPartValue value = QueryPartValue.newFromJsonElement( record.get(fieldname) );
					ArrayList<String> fieldHeaders = fieldHeadersMap.get(fieldname);
					if(value.isString()) {
						String csvString = value.getAsString();
						
						JsonElement parsedValue = null;
						if(fieldHeaders != null) { // might be null when multiline
							parsedValue = CFW.CSV.toJsonElement(csvString, separator, fieldHeaders, true);
						}else {
							
							if( ! csvString.contains("\n") || customHeadersArray != null ) {
								parsedValue = CFW.CSV.toJsonElement(csvString, separator, customHeadersArray, true);
							}else {
								// use headers from multiline CSV
								parsedValue = CFW.CSV.toJsonElement(csvString, separator, false, true);
							}
						}
						
						record.add(fieldname, parsedValue);
					}
				}
				
			}
			
			outQueue.add(record);
			
		}
		
		
		this.setDoneIfPreviousDone();
	}
		



}

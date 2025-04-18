package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import com.google.gson.JsonElement;
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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandSort extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "sort";

	private ArrayList<String> fieldnames = new ArrayList<>();
	
	private boolean isReverseOrder = false;
	private boolean isReverseNulls = false;
	
	private ArrayList<EnhancedJsonObject> objectListToSort = new ArrayList<>();
	private ArrayList<QueryPart> parts;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandSort(CFWQuery parent) {
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
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sorts the values based on the specified fields.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname> [, <fieldname> ...] [reverse=false] [reversenulls=false]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b> Names of the fields that should be used for sorting.</p>"
		 	  +"<p><b>reverse:&nbsp;</b> Set to true to reverse the sort order.</p>"
			  +"<p><b>reversenulls:&nbsp;</b> Set to true to list null values first.</p>"
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
	public void initializeAction() throws Exception {
		
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				if(paramName != null) {
					
					//----------------------------------
					// Parameter Reverse
					if(paramName.equals("reverse")) {
						QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
						if(paramValue.isBoolOrBoolString()) {
							this.isReverseOrder = paramValue.getAsBoolean();
						}
					}
					
					//----------------------------------
					// Parameter ReverseNulls
					if(paramName.equals("reversenulls")) {
						QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
						if(paramValue.isBoolOrBoolString()) {
							this.isReverseNulls = paramValue.getAsBoolean();
						}
					}
				}
				
			}else if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;

				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						fieldnames.add(element.getAsString());
					}
				}
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isJsonArray()) {
					fieldnames.addAll(value.getAsStringArray());
				}else if(!value.isNull()) {
					fieldnames.add(value.getAsString());
				}
			}
		}
		
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
	
		//-------------------------------------
		// Create Sorted List
		while(keepPolling()) {
			objectListToSort.add(inQueue.poll());
		}

		if(isPreviousDone()) {

			//-------------------------------------
			// Sort the List List
			
			Comparator<EnhancedJsonObject> comparator = new Comparator<EnhancedJsonObject>() {
				@Override
				public int compare(EnhancedJsonObject o1, EnhancedJsonObject o2) {
					
					//ComparisonChain chain = ComparisonChain.start();
					
					int compareResult = 0;
					for(String fieldname : fieldnames) {
						
						compareResult = _CFWQueryCommon.compareByFieldname(o1, o2, fieldname, isReverseNulls);

						if(compareResult != 0) {
							break;
						}
					}
					return compareResult;
				}
			};
			
			if(isReverseOrder) {
				comparator = comparator.reversed();
			}
			
			objectListToSort.sort(comparator);

			//-------------------------------------
			// Push Sorted List to outQueue
			for(EnhancedJsonObject object : objectListToSort) {
				
				//System.out.println("out: "+object.get(fieldnames.get(0)));
				outQueue.add(object);
			}

			this.setDone();
		}
				
	}

}

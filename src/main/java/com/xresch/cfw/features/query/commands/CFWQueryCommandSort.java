package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.JsonElement;
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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandSort extends CFWQueryCommand {
	
	CFWQuerySource source = null;
	ArrayList<String> fieldnames = new ArrayList<>();
	
	private boolean isReverseOrder = false;
	private boolean isReverseNulls = false;
	
	ArrayList<EnhancedJsonObject> objectListToSort = new ArrayList<>();
	
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
		return new String[] {"sort"};
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
		return "sort <fieldname> [, <fieldname> ...] [reverse=false] [reversenulls=false]";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_sort.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Fieldnames and params
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
				if(!value.isNull()) {
					fieldnames.add(value.getAsString());
				}
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Specify the fieldnames of the fields that should be used for sorting.<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//-------------------------------------
		// Create Sorted List
		while(keepPolling()) {
			System.out.println("poll");
			objectListToSort.add(inQueue.poll());
		}
		
		if(isPreviousDone()) {
			//-------------------------------------
			// Sort the List List
			
			int nullsSmaller = (isReverseNulls) ? 1 : -1;
			int nullsBigger = (isReverseNulls) ? -1 : 1;
			
			Comparator<EnhancedJsonObject> comparator = new Comparator<EnhancedJsonObject>() {
				@Override
				public int compare(EnhancedJsonObject o1, EnhancedJsonObject o2) {
					
					//ComparisonChain chain = ComparisonChain.start();
					
					int compareResult = 0;
					for(String fieldname : fieldnames) {
						
						QueryPartValue value1 = QueryPartValue.newFromJsonElement(o1.get(fieldname));
						QueryPartValue value2 = QueryPartValue.newFromJsonElement(o2.get(fieldname));

						if(value1.isNumberOrNumberString() && value2.isNumberOrNumberString()) {
							compareResult = value1.getAsBigDecimal().compareTo(value2.getAsBigDecimal());
						}else if(value1.isBoolOrBoolString() && value2.isBoolOrBoolString()) {
							compareResult = Boolean.compare(value1.getAsBoolean(), value2.getAsBoolean());
						}else{
							if(value1.isNull()) {
								if(value2.isNull()) { compareResult = 0; }
								else				{ compareResult = nullsSmaller; }
							}else if(value2.isNull()) {
								 compareResult = nullsBigger; 
							}else {
								compareResult = value1.getAsString().compareTo(value2.getAsString());
							}
						}
						
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

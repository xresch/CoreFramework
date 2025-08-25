package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

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
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandTopN extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "topN";

	private ArrayList<String> fieldnames = new ArrayList<>();
	
	private Integer N = 10;
	private String valueField = null;
	private String labelField = null;
	private boolean others = true;
	private boolean isReverseOrder = false;
	private boolean isReverseNulls = false;
	
	private ArrayList<EnhancedJsonObject> objectListToSort = new ArrayList<>();
	private ArrayList<QueryPart> parts;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandTopN(CFWQuery parent) {
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
		tags.add(_CFWQueryCommon.TAG_AGGREGATION);
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
		return COMMAND_NAME+" <fieldname> [, <fieldname> ...] [reverse=true] ";
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
				QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
				if(paramName != null) {
					switch(paramName.trim().toLowerCase()) {
						case "n": 				this.N = paramValue.getAsInteger(); break;
						case "value": 			this.valueField = paramValue.getAsString(); break;
						case "label": 			this.labelField = paramValue.getAsString(); break;
						case "others":	 		this.others = paramValue.getAsBoolean(); break;
						case "reverse": 		this.isReverseOrder = paramValue.getAsBoolean(); break;
						case "reversenulls": 	this.isReverseNulls = paramValue.getAsBoolean(); break;

					}
				}
			}
		}
		
		//------------------------------
		// Sanitize
		if(	this.N == null 
		|| this.N < 1) {
			this.N = 10;
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

					compareResult = _CFWQueryCommon.compareByFieldname(o1, o2, valueField, isReverseNulls);

					return compareResult;
				}
			};
			
			// not reversed as default is top-down
			if( ! isReverseOrder ) {
				comparator = comparator.reversed();
			}
			
			objectListToSort.sort(comparator);
			
			//-------------------------------------
			// Push TopN outQueue

			for(int i = 0 ; i < N && i < objectListToSort.size(); i++) {
				outQueue.add(objectListToSort.get(i));
			}

			//-------------------------------------
			// Summarize everything else
			if(others 
			&& objectListToSort.size() > N) {
				
				BigDecimal sum = CFW.Math.ZERO;
				for(int i = N; i < objectListToSort.size()-1; i++) {
					EnhancedJsonObject current = objectListToSort.get(i);
					QueryPartValue value = QueryPartValue.newFromJsonElement(current.get(valueField));
					
					if(value.isNumberOrNumberString()) {
						sum = sum.add(value.getAsBigDecimal());
					}
				}
				
				EnhancedJsonObject others = new EnhancedJsonObject();
				others.addProperty(valueField, sum);
				others.addProperty(labelField, "Others");
				outQueue.add(others);
			}
			this.setDone();
		}
				
	}

}

package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/**************************************************************************************************************
 * 
 * @author Joel Läubin
 * @author Reto Scheiwiller 
 * 
 * (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryFunctionExtractBounds extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "extractBounds";

	public CFWQueryFunctionExtractBounds(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return FUNCTION_NAME;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(CFWQueryFunction.TAG_STRINGS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(stringOrFieldname, leftBound, rightBound)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Extract one or multiple values from a string between a right and left bound.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
					+"<li><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a string should be searched.</li>"
					+"<li><b>leftBound:&nbsp;</b>The left bound for the extraction.</li>"
					+"<li><b>rightBound:&nbsp;</b>The right bound for the extraction..</li>"
				+"</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_"+FUNCTION_NAME+".html");
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean supportsAggregation() {
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		//-------------------------
		// Validate Param Count
		int paramCount = parameters.size();
		if(paramCount < 3) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Get value
		QueryPartValue valueToSearch = parameters.get(0);
		if(valueToSearch.isNull()) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Get left
		QueryPartValue leftBound = parameters.get(1);
		if(leftBound.isNullOrEmptyString()) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Get right
		QueryPartValue rightBound = parameters.get(2);
		if(rightBound.isNullOrEmptyString()) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Prepare Variables

		String searchThis = valueToSearch.getAsString();
		String left = leftBound.getAsString();
		String right = rightBound.getAsString();
		boolean boundsEquals = left.equals(right);

		//-------------------------
		// Start Index
		int startIndex = 1;
		if(boundsEquals) {
			startIndex = searchThis.indexOf(left, 0)+1;
		}
		
		//-------------------------
		// Extraction Loop
		// Search first for the rightBound and then backwards for the leftBound.
		// This will prevent to run into false positives on the left bound and getting unexpected long extractions.
		ArrayList<String> results = new ArrayList<>();
		int rightIndex = searchThis.indexOf(right, startIndex); // start at one, as right index cannot be at the start of the string
		while(rightIndex != -1) {
			
			//----------------------------
			// Find Left
			int leftIndex = searchThis.lastIndexOf(left, rightIndex-1);
			
			//----------------------------
			// Extract
			if(leftIndex != -1) {
				leftIndex += left.length();
				String extracted = searchThis.substring(leftIndex, rightIndex);
				results.add(extracted);
			}
			
			//----------------------------
			// Prepare next Round
			searchThis = searchThis.substring(rightIndex); // substring to remove already found results.
			rightIndex = searchThis.indexOf(right, 1);
		}
		
		if(results.isEmpty()) {
			return QueryPartValue.newNull();
		}else if(results.size() == 1) {
			return QueryPartValue.newString(results.get(0));
		}
		
		return QueryPartValue.newFromStringArray(results);
	}

}

package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionFieldsMatching extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "fieldsMatching";

	private static final HashMap<String, Pattern> patternCache = new HashMap<>();
	
	public CFWQueryFunctionFieldsMatching(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(regex, regex...)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns an arrayof fieldnames from the detected fieldnames that match any of the specified regular expressions.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			"<ul>"
				+"<li><b>regex:&nbsp;</b>The regular expression to match.</li>"
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
	public boolean receiveStringParamsLiteral() {
		return true;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		// Make sure to keep the Order
		LinkedHashSet<String> matches = new LinkedHashSet<>();
		
		HashSet<String> detectedFieldnames = this.context.getFinalFieldnames();
		for(QueryPartValue regex : parameters) {
			String regexString = regex.getAsString();
			
			//-----------------------
			// Get Pattern
			if( ! patternCache.containsKey(regexString) ) {
				patternCache.put(regexString, Pattern.compile(regexString) );
			}
			Pattern p  = patternCache.get(regexString);
			
			//-----------------------
			// Find Matches
			for( String fieldname : detectedFieldnames){
				if(p.matcher(fieldname).find()) {
					
					matches.add(fieldname);
				};
			}
		}
		
		//-----------------------
		// Return Matches
		JsonArray array = new JsonArray();
		for(String fieldname : matches) {
			array.add(fieldname);
		}
		
		return QueryPartValue.newJson(array);
	}

}

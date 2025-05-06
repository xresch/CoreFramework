package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.Base64;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionEncodeBase64 extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "encodeBase64";

	public CFWQueryFunctionEncodeBase64(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_STRINGS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(stringToEncode)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Encodes a string to Base64.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringToEncode:&nbsp;</b>The string that should be encoded.</p>"
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
		
		//----------------------------------
		// Return empty string if no params
		if(parameters.size() == 0) { return QueryPartValue.newString(""); }
		
		//----------------------------------
		// Get String
		String stringToEncode = parameters.get(0).getAsString();
		
		if(stringToEncode == null) { 
			return QueryPartValue.newString(""); 
		}else {
			
			return QueryPartValue.newString(
					Base64.getUrlEncoder().encodeToString(stringToEncode.getBytes())
				);
		}
		
	}

}

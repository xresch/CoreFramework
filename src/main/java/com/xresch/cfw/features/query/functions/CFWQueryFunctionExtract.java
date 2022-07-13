package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class CFWQueryFunctionExtract extends CFWQueryFunction {

	
	public CFWQueryFunctionExtract(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "extract";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "extract(stringOrFieldname, regex)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Extract a value with regular expressions.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a string should be searched.</p>"
			  +"<p><b>regex:&nbsp;</b>The regex string to use.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_extract.html");
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
		
		int paramCount = parameters.size();
		if(paramCount < 2) {
			return QueryPartValue.newNull();
		}
		
		
		QueryPartValue valueToSearch = parameters.get(0);
		if(valueToSearch.isNull()) {
			return QueryPartValue.newNull();
		}
		
		QueryPartValue regex = parameters.get(1);
		if(regex.isNullOrEmptyString()) {
			return QueryPartValue.newNull();
		}
		
		Pattern p = Pattern.compile(regex.getAsString());
		Matcher m = p.matcher(valueToSearch.getAsString());
		if(m.matches()) {
		    return QueryPartValue.newString(m.group(1));
		}
		
	
		return QueryPartValue.newNull();
	}

}

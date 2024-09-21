package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class _CFWQueryCommandCommon {

	
	/***********************************************************************************************
	 * Used for sorting fields
	 ***********************************************************************************************/
	public static int compareByFieldname(EnhancedJsonObject o1, EnhancedJsonObject o2, String fieldname, boolean reverseNulls) {
		int compareResult;
		int nullsSmaller = (reverseNulls) ? 1 : -1;
		int nullsBigger = (reverseNulls) ? -1 : 1;
		
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
				compareResult = CFW.Utils.Text.compareStringsAlphanum(value1.getAsString(), value2.getAsString());
			}
		}
		return compareResult;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static String getFilterOperatorDescipriontHTML() {
		return "<li><b>fieldname:&nbsp;</b>The name of the field to evaluate the value against.</li>"
			  +"<li><b>value:&nbsp;</b>The value to check.</li>"
			  +"<li><b>operator:&nbsp;</b>The operator, any of:"
				  +"<ul>"
				  + "	<li><b>'==':&nbsp;</b> Checks if the values are equal.</li>"
				  + "	<li><b>'!=':&nbsp;</b> Checks if the values are not equal.</li>"
				  + "	<li><b>'~=':&nbsp;</b> Checks if the field matches a regular expression.</li>"
				  + "	<li><b>'&lt;=':&nbsp;</b> Checks if the field value is smaller or equals.</li>"
				  + "	<li><b>'&gt;=':&nbsp;</b> Checks if the field value is greater or equals.</li>"
				  + "	<li><b>'&lt;':&nbsp;</b>  Checks if the field value is smaller.</li>"
				  + "	<li><b>'&gt;':&nbsp;</b>  Checks if the field value is greater.</li>"
				  + "	<li><b>AND:&nbsp;</b> Used to combine two or more conditions. Condition matches only if both sides are true.</li>"
				  + "	<li><b>OR:&nbsp;</b>  Used to combine two or more conditions. Condition matches if either side is true.</li>"
				  + "</ul>"
			  + "</li>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static void createFilterEvaluatiooGroup(CFWQueryParser parser
			, ArrayList<QueryPart> parts
			, String CommandName
			, QueryPartGroup evaluationGroup
				) throws ParseException {


		//------------------------------------------
		// Get Parameters
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			// BinaryExpressions, Groups and Booleans
			if(QueryPartGroup.partEvaluatesToBoolean(currentPart)) {
				evaluationGroup.add(currentPart);
								
			}else if(currentPart instanceof QueryPartArray) {
				
				createFilterEvaluatiooGroup(
						parser
						, ((QueryPartArray)currentPart).getAsParts()
						, CommandName
						, evaluationGroup
					);
				
			}else if(currentPart instanceof QueryPartAssignment) { 
				parser.throwParseException(CommandName+": Single equal '=' is not supported. Please use '==' instead.", currentPart);
			}else {
				parser.throwParseException(CommandName+": Only binary expressions allowed.", currentPart);
			}
		}
		
	}
	
}

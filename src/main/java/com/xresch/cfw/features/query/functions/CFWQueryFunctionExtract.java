package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

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
		return "extract(stringOrFieldname, regex, groupIndex)";
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
			  +"<p><b>groupIndex:&nbsp;</b> (Optional) The index of the group to extract the value from (Default: 0).</p>"
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
		
		//-------------------------
		// Validate Param Count
		int paramCount = parameters.size();
		if(paramCount < 2) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Get value
		QueryPartValue valueToSearch = parameters.get(0);
		if(valueToSearch.isNull()) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Get expression
		QueryPartValue regex = parameters.get(1);
		if(regex.isNullOrEmptyString()) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------
		// Extract
		int groupIndex = 0;
		if(paramCount > 2) {
			QueryPartValue indexValue = parameters.get(2);
			if(indexValue.isInteger()) {
				groupIndex = indexValue.getAsInteger();
			}
		}
		
		//-------------------------
		// Extract
		Pattern p = Pattern.compile(regex.getAsString(), Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(valueToSearch.getAsString());
		if(m.matches()) {
			if(m.groupCount() > groupIndex && groupIndex >= -1) {
				return QueryPartValue.newString(m.group(groupIndex+1));
			}else {
				this.getContext().addMessage(MessageType.WARNING, "extract: could not match group with index: "+groupIndex);
				QueryPartValue.newNull();
			}
		}
		
		return QueryPartValue.newNull();
	}

}

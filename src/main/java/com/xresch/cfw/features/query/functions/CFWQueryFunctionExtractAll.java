package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
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
public class CFWQueryFunctionExtractAll extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "extractAll";

	public CFWQueryFunctionExtractAll(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(stringOrFieldname, regex, groupIndex)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Extract all matching values with regular expressions.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
					+"<li><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a string should be searched.</li>"
					+"<li><b>regex:&nbsp;</b>The regex string to use.</li>"
					+"<li><b>groupIndex:&nbsp;</b> (Optional) The index of the group to extract the value from (Default: 0).</li>"
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
		
		JsonArray array = new JsonArray();
		
		while (m.find()) {
			
			if(m.groupCount() > groupIndex && groupIndex >= -1) {
				array.add( m.group(groupIndex+1) );
			}else {
				this.getContext().addMessage(MessageType.WARNING, FUNCTION_NAME+": could not match group with index: "+groupIndex);
				QueryPartValue.newNull();
			}
		}
		
		
		return QueryPartValue.newJson(array);
	}

}

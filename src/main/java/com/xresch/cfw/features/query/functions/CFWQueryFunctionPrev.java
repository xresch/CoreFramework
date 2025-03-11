package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
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
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionPrev extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "prev";

	ArrayList<QueryPartValue> previousList = new ArrayList<>();
	
	public CFWQueryFunctionPrev(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_MATH);
		return tags;
	}
	
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(fieldname, offset, default)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a previous value given to this function.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			+"<li><b>fieldname:&nbsp;</b>The number you want the absolute value for.</li>"
			+"<li><b>offset:&nbsp;</b>The number of values you want to go back. If there are not enough values will returns null. (Default: 1)</li>"
			+"<li><b>default:&nbsp;</b>The value that should be returned if there are not enough values yet. (Default: null)</li>"
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
		
		//-------------------------------------------
		// Check Has Params
		int paramCount = parameters.size();
		if(paramCount == 0) {
			return QueryPartValue.newNull();
		}
		
		//-------------------------------------------
		// Add Current Value
		QueryPartValue currentValue = parameters.get(0);
		previousList.add(currentValue);
		
		//-------------------------------------------
		// Get Offset
		int offset = 1;
		if(paramCount > 1) {
			offset = parameters.get(1).getAsInteger();
		}

		//-------------------------------------------
		// Return Previous
		if(previousList.size() > offset) {
			return previousList.get( previousList.size() - 1 - offset );
		}
		
		//-------------------------------------------
		// Return Null or Default
		if(paramCount > 2) {
			return parameters.get(2);
		}else {
			return QueryPartValue.newNull();
		}
	}

}

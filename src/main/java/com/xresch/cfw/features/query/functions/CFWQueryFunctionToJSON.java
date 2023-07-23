package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;

public class CFWQueryFunctionToJSON extends CFWQueryFunction {

	
	public CFWQueryFunctionToJSON(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "tojson";
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(CFWQueryFunction.TAG_CODING);
		tags.add(CFWQueryFunction.TAG_STRINGS);
		tags.add(CFWQueryFunction.TAG_OBJECTS);
		tags.add(CFWQueryFunction.TAG_ARRAYS);
		return tags;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "tojson(valueOrFieldname)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a value as a json object or array.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>valueOrFieldname:&nbsp;</b>The value to convert to a JSON value.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_tojson.html");
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
		if(paramCount == 0) {
			return QueryPartValue.newNull();
		}

		QueryPartValue valueToConvert = parameters.get(0);
		
		if(valueToConvert.isNullOrEmptyString()) {
			return QueryPartValue.newFromJsonElement(new JsonObject());
		}
		
		if(valueToConvert.isString()) {
			String stringValue = valueToConvert.getAsString();
			if(stringValue.startsWith("{") || stringValue.startsWith("[") ) {
				return QueryPartValue.newFromJsonElement(
						CFW.JSON.stringToJsonElement(stringValue)
					);
			}else {
				return QueryPartValue.newJson(valueToConvert.getAsJsonElement());
			}
		}
		
		
		// return null in other cases
		return QueryPartValue.newJson(valueToConvert.getAsJsonElement());
	}

}

package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionArrayLookup extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "arrayLookup";

	public CFWQueryFunctionArrayLookup(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_CODING);
		tags.add(_CFWQueryCommon.TAG_OBJECTS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(object, keyFieldname, keyValue, lookupFieldname)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the value associated with the key, or null if not found.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>array:&nbsp;</b>The array of objects to lookup the value in.</li>"
			  +"<li><b>keyFieldname:&nbsp;</b>The name of the field which should match the keyValue.</li>"
			  +"<li><b>keyValue:&nbsp;</b>The key to lookup.</li>"
			  +"<li><b>lookupFieldname:&nbsp;</b>The name of the field whose value should be returned.</li>"
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
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters, ArrayList<QueryPart> unevalParams) {
		
		//----------------------------------
		// Return same value if not second param
		if(parameters.size() >= 4) { 
			
			QueryPartValue arrayPart = parameters.get(0); 
			String keyFieldname = parameters.get(1).getAsString(); 
			String keyValue = parameters.get(2).getAsString(); 
			String lookupFieldname = parameters.get(3).getAsString(); 
			
			if( ! arrayPart.isJsonArray()) { return QueryPartValue.newNull(); }
			
			JsonArray array = arrayPart.getAsJsonArray();
			
			for(JsonElement element : array) {
				
				if( ! element.isJsonObject() ) { continue; }
				
				JsonObject record = element.getAsJsonObject();
				
				if(record.has(keyFieldname)) {
					
					String value = QueryPartValue.newFromJsonElement( record.get(keyFieldname) ).getAsString();
					
					if( ( keyValue == null && value == null )
					||  ( keyValue != null && keyValue.equals(value) )
					){
						if(record.has(lookupFieldname)) {
							return QueryPartValue.newFromJsonElement( record.get(lookupFieldname) );
						} else {
							return QueryPartValue.newNull();
						}
					}
				}
			}
				
		}
		
		//----------------------------------
		// Return null if not enough params
		return QueryPartValue.newNull();
		
	}
}

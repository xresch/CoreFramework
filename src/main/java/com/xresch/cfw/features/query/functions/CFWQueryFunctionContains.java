package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionContains extends CFWQueryFunction {
	
	public CFWQueryFunctionContains(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "contains";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(CFWQueryFunction.TAG_ARRAYS);
		tags.add(CFWQueryFunction.TAG_OBJECTS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "contains(valueOrFieldname, itemToFind)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Checks whether a item is present in a value or not.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>valueOrFieldname:&nbsp;</b>(Optional)The value or fieldname to bechecked by the contains operation.</p>"
			 + "<p><b>itemToFind:&nbsp;</b>The item that should be searched in the value.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_contains.html");
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
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		boolean result = false;
		
		if(parameters.size() <= 1) {
			return QueryPartValue.newNull();
		}else {
			
			QueryPartValue valueToCheck = parameters.get(0);
			QueryPartValue itemToFind = parameters.get(1);
			
			if(itemToFind.isJson()) {
				this.context.addMessageError("contains(): Error - Second parameter cannot be an Object or Array.");
				return QueryPartValue.newBoolean(false);
			}
			
			switch(valueToCheck.type()) {
				
				//--------------------------------------
				// Check String
				case STRING:
				case NUMBER:
					result = itemToFind.containedInElement(new JsonPrimitive(valueToCheck.getAsString()));
					return QueryPartValue.newBoolean(result);
				
				case BOOLEAN:
					result = itemToFind.containedInElement(new JsonPrimitive(valueToCheck.getAsBoolean()));
					return QueryPartValue.newBoolean(result);
					
				case NULL:
					result = itemToFind.containedInElement(JsonNull.INSTANCE);
					return QueryPartValue.newBoolean(result);
				
				//--------------------------------------
				// Check JSON
				case JSON:
					
					if(valueToCheck.isJsonArray()) {
						
						//------------------------------
						// Check Array
						JsonArray array = valueToCheck.getAsJsonArray();
						
						for(JsonElement current : array) {
							result = itemToFind.equalsElement(current);
							if(result == true) {
								return QueryPartValue.newBoolean(true);
							}
						}	

					}else if(valueToCheck.isJsonObject()) {
						//-----------------------------
						// Check Object
						JsonObject objectToCheck = valueToCheck.getAsJsonObject();
						for(String current : objectToCheck.keySet()) {
							result = itemToFind.equalsElement(new JsonPrimitive(current));
							if(result == true) {
								return QueryPartValue.newBoolean(true);
							}
						}

					}
					break;
					
				default:
					this.context.addMessageError("contains(): Error - theoretically unreachable case reached.");
					break;
			
			}

		}
		
		return QueryPartValue.newBoolean(result);
	}



}

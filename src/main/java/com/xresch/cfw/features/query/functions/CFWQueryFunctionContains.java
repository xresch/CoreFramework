package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

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
				return QueryPartValue.newString("contains(): Error - Second parameter cannot be a JsonObject or Array.");
			}


			
			if(valueToCheck.isJsonArray()) {
				
				//--------------------------------------
				// Check if array contains the item
				//--------------------------------------
				JsonArray array = valueToCheck.getAsJsonArray();
				
				forloop:
				for(JsonElement current : array) {
					switch(itemToFind.type()) {
						case BOOLEAN:
							if(current.isJsonPrimitive()) {
								JsonPrimitive primitive = current.getAsJsonPrimitive();
								if(primitive.isBoolean()) {
									if(primitive.getAsBoolean() == itemToFind.getAsBoolean()) {
										result = true;
										break forloop;
									}
								}else if(primitive.isString()) {
									if(primitive.getAsString().equalsIgnoreCase(itemToFind.getAsString()+"") ) {
										result = true;
										break forloop;
									}
								}
							}
							break;
													
							
						case NULL:
							if(current.isJsonNull()
							|| ( current.isJsonPrimitive() && current.getAsString().equals("null") )  
							) {
								result = true;
								break forloop;
							}
							break;
							
							
						case NUMBER:
							if(current.isJsonPrimitive()) {
								JsonPrimitive primitive = current.getAsJsonPrimitive();
								if(primitive.isNumber()) {
									if(primitive.getAsNumber() == itemToFind.getAsNumber()) {
										result = true;
										break forloop;
									}
								}else if(primitive.isString()) {
									if(primitive.getAsString().equals(itemToFind.getAsString()) ) {
										result = true;
										break forloop;
									}
								}
							}
							break;
							
							
						case STRING:
							if(current.isJsonPrimitive()) {
								JsonPrimitive primitive = current.getAsJsonPrimitive();
								if(primitive.isString()) {
									if(primitive.getAsString().equals(itemToFind.getAsString()) ) {
										result = true;
										break forloop;
									}
								}else if(primitive.isBoolean()) {
									if(itemToFind.getAsString().equals(primitive.getAsBoolean()+"") ) {
										result = true;
										break forloop;
									}
								}else if(primitive.isNumber()) {
									if(itemToFind.getAsString().equals(primitive.getAsNumber()+"") ) {
										result = true;
										break forloop;
									}
								}
							}
							break;
						
							
						case JSON: // Not supported
							break;
							
						default: 
							this.context.addMessage(MessageType.WARNING, "contains(): Warning - Code block reached that should not have been reached.");
						break;
					
					}
				}	

			}else if(valueToCheck.isJsonObject()) {
				return QueryPartValue.newNumber(valueToCheck.getAsJsonObject().entrySet().size());
			}else if(valueToCheck.isNull()) {
				return QueryPartValue.newNull();
			}else {
				return QueryPartValue.newNumber(1);
			}
		}
		
		return QueryPartValue.newBoolean(result);
	}

}

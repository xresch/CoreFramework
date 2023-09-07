package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionIndexOf extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "indexof";

	public CFWQueryFunctionIndexOf(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_STRINGS);
		tags.add(CFWQueryFunction.TAG_ARRAYS);
		tags.add(CFWQueryFunction.TAG_OBJECTS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(stringOrFieldname, searchString[, beginIndex])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the index of the first occurence for for the searched string.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a value should be searched.</p>"
			  +"<p><b>searchString:&nbsp;</b>The string to search for.</p>"
			  +"<p><b>beginIndex:&nbsp;</b>(Optional)The index to start the search from. This parameter is ignored if the first parameter value is an array or object.</p>"
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
		
		QueryPartValue valuetoSearchIn = parameters.get(0);
		int paramCount = parameters.size();
		
		//----------------------------------
		// Check Param Count
		if(paramCount > 1) { 
			//----------------------------------
			// Get value to search
			QueryPartValue searchThisValue = parameters.get(1);

			//===========================================
			// Handle Array
			//===========================================
			if(valuetoSearchIn.isJsonArray()) {
				JsonArray arraytoSearchIn = valuetoSearchIn.getAsJsonArray();
				return findIndexOfArrayItem(searchThisValue, arraytoSearchIn);
			}
			
			//===========================================
			// Handle Object
			//===========================================
			if(valuetoSearchIn.isJsonObject()) {
				JsonObject objectToSearchIn = valuetoSearchIn.getAsJsonObject();
				return findMembernameOfObjectValue(searchThisValue, objectToSearchIn);
			}
			
			//===========================================
			// Handle everything else as String
			//===========================================
			//----------------------------------
			// Get String
			String initialString = valuetoSearchIn.getAsString();
			if(Strings.isNullOrEmpty(initialString)) { return QueryPartValue.newNumber(-1); }
			
			//----------------------------------
			// Get Begin Index
			Integer beginIndex = null;
			if(paramCount > 2 && parameters.get(2).isNumberOrNumberString()) {
				beginIndex = parameters.get(2).getAsInteger();

				if(beginIndex >= initialString.length()) {
					beginIndex = initialString.length();
				}
			}
			
			//----------------------------------
			// Get IndexOf
			String searchString = searchThisValue.getAsString();
			if (searchString == null) { searchString = "null"; };
			
			if(beginIndex == null) { 
				return QueryPartValue.newNumber(initialString.indexOf(searchString)); 
			}else {
				return QueryPartValue.newNumber(initialString.indexOf(searchString, beginIndex)); 
			}
		}
		
		//----------------------------------
		// Return -1 in other cases
		return QueryPartValue.newNumber(-1);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public QueryPartValue findIndexOfArrayItem(QueryPartValue searchValue, JsonArray array) {
		
		// Implemented this way to make it more performant
		// multiple for loops in if statements instead of one for loop with multiple if statements
		
		//--------------------------
		// Search Strings
		if(searchValue.isString()) {
			String searchString = searchValue.getAsString();
			int i = 0;
			for( ; i < array.size(); i++  ) {
				JsonElement e = array.get(i);
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isString()
					&& searchString.equals(primitive.getAsString())) {
						return QueryPartValue.newNumber(i);
					}
				}else if( e.isJsonObject() || e.isJsonArray() ) {
					// compare array and objets as strings
					if( searchString.equals(e.toString()) ) {
						return QueryPartValue.newNumber(i);
					}
				}
			}
			
			return QueryPartValue.newNumber(-1);
		}
		
		//--------------------------
		// Search number
		if(searchValue.isNumber()) {
			BigDecimal searchNumber = searchValue.getAsBigDecimal();
			int i = 0;
			for( ; i < array.size(); i++  ) {
				JsonElement e = array.get(i);
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isNumber()
					&& searchNumber.compareTo(primitive.getAsBigDecimal()) == 0) {
						return QueryPartValue.newNumber(i);
					}
				}
			}
			return QueryPartValue.newNumber(-1);
		}
		
		//--------------------------
		// Search Boolean
		if(searchValue.isBoolean()) {
			boolean searchBoolean = searchValue.getAsBoolean();
			int i = 0;
			for( ; i < array.size(); i++  ) {
				JsonElement e = array.get(i);
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isBoolean()
					&& searchBoolean == primitive.getAsBoolean()) {
						return QueryPartValue.newNumber(i);
					}
				}
			}
			return QueryPartValue.newNumber(-1);
		}
		
		//--------------------------
		// Search Null
		if(searchValue.isNull()) {
			int i = 0;
			for( ; i < array.size(); i++  ) {
				JsonElement e = array.get(i);
				if(e.isJsonNull()) {
					return QueryPartValue.newNumber(i);
				}
			}
			return QueryPartValue.newNumber(-1);
		}
		
		//--------------------------
		// Search Json
		if(searchValue.isJson()) {
			
			//--------------------------
			// Search Object
			if(searchValue.isJsonObject()) {
				JsonObject object = searchValue.getAsJsonObject();
				String searchString = object.toString();
				int i = 0;
				for( ; i < array.size(); i++  ) {
					JsonElement e = array.get(i);
					if(e.isJsonObject()) {
						String currentString = e.toString();
						
						if( searchString.equals(currentString) ) {
							return QueryPartValue.newNumber(i);
						}
					}else if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						// value in array might be a string representation of an object
						if( searchString.equals(e.getAsString()) ) {
							return QueryPartValue.newNumber(i);
						}
					}
				}
				
				return QueryPartValue.newNumber(-1);
			}
			
			//--------------------------
			// Search Array
			if(searchValue.isJsonArray()) {
				JsonArray arrayToSearch = searchValue.getAsJsonArray();
				String searchString = arrayToSearch.toString();
				int i = 0;
				for( ; i < array.size(); i++  ) {
					JsonElement e = array.get(i);
					if(e.isJsonArray()) {
						String currentString = e.toString();
						
						if( searchString.equals(currentString) ) {
							return QueryPartValue.newNumber(i);
						}
					}else if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						// value in array might be a string representation of an array
						if( searchString.equals(e.getAsString()) ) {
							return QueryPartValue.newNumber(i);
						}
					}
				}
				
				return QueryPartValue.newNumber(-1);
			}
		}
		
		//--------------------------
		// not found
		return QueryPartValue.newNumber(-1);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public QueryPartValue findMembernameOfObjectValue(QueryPartValue searchValue, JsonObject object) {
		
		// Implemented this way to make it more performant
		// multiple for loops in if statements instead of one for loop with multiple if statements
		Set<Entry<String, JsonElement>> entrySet = object.entrySet();
		
		//--------------------------
		// Search Strings
		if(searchValue.isString()) {
			String searchString = searchValue.getAsString();
			
			for(Entry<String, JsonElement> entry : entrySet ) {
				JsonElement e = entry.getValue();
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isString()
					&& searchString.equals(primitive.getAsString())) {
						return QueryPartValue.newString(entry.getKey());
					}
				}else if( e.isJsonObject() || e.isJsonArray() ) {
					// compare array and objets as strings
					if( searchString.equals(e.toString()) ) {
						return QueryPartValue.newString(entry.getKey());
					}
				}
			}
			
			return QueryPartValue.newNull();
		}
		
		//--------------------------
		// Search Number
		if(searchValue.isNumber()) {
			BigDecimal searchNumber = searchValue.getAsBigDecimal();
			
			for(Entry<String, JsonElement> entry : entrySet ) {
				JsonElement e = entry.getValue();
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isNumber()
					&& searchNumber.compareTo(primitive.getAsBigDecimal()) == 0) {
						return QueryPartValue.newString(entry.getKey());
					}
				}
			}
			
			return QueryPartValue.newNull();
		}
		
		//--------------------------
		// Search Boolean
		if(searchValue.isBoolean()) {
			boolean searchBoolean = searchValue.getAsBoolean();
			
			for(Entry<String, JsonElement> entry : entrySet ) {
				JsonElement e = entry.getValue();
				if(e.isJsonPrimitive()) {
					JsonPrimitive primitive = e.getAsJsonPrimitive();
					
					if(primitive.isBoolean()
					&& searchBoolean == primitive.getAsBoolean()) {
						return QueryPartValue.newString(entry.getKey());
					}
				}
			}
			
			return QueryPartValue.newNull();
		}
		
		//--------------------------
		// Search Null
		if(searchValue.isNull()) {
			for(Entry<String, JsonElement> entry : entrySet ) {
				JsonElement e = entry.getValue();
				if(e.isJsonNull()) {
					return QueryPartValue.newString(entry.getKey());
				}
			}
			
			return QueryPartValue.newNull();
		}
	
		//--------------------------
		// Search Json
		if(searchValue.isJson()) {
			
			//--------------------------
			// Search Object
			if(searchValue.isJsonObject()) {
				JsonObject searchObject = searchValue.getAsJsonObject();
				String searchString = searchObject.toString();
				
				for(Entry<String, JsonElement> entry : entrySet ) {
					JsonElement e = entry.getValue();
					if(e.isJsonObject()) {
						String currentString = e.toString();
						
						if( searchString.equals(currentString) ) {
							return QueryPartValue.newString(entry.getKey());
						}
					}else if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						// value in array might be a string representation of an object
						if( searchString.equals(e.getAsString()) ) {
							return QueryPartValue.newString(entry.getKey());
						}
					}
				}
				
				return QueryPartValue.newNull();
			}
			
			//--------------------------
			// Search Array
			if(searchValue.isJsonArray()) {
				JsonArray arrayToSearch = searchValue.getAsJsonArray();
				String searchString = arrayToSearch.toString();
				
				for(Entry<String, JsonElement> entry : entrySet ) {
					JsonElement e = entry.getValue();
					if(e.isJsonArray()) {
						String currentString = e.toString();
						
						if( searchString.equals(currentString) ) {
							return QueryPartValue.newString(entry.getKey());
						}
					}else if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
						// value in array might be a string representation of an array
						if( searchString.equals(e.getAsString()) ) {
							return QueryPartValue.newString(entry.getKey());
						}
					}
				}
				
				return QueryPartValue.newNull();
			}
			
		}
		
		//--------------------------
		// Not Found
		return QueryPartValue.newNumber(-1);
	}

}

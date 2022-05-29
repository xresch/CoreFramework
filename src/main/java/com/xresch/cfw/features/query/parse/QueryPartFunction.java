package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * QueryPart that will hold the following expressions:
 *  - A List of Binary expressions. AND is implicitly added between expressions if multiple expressions
 *  	are given: 
 *    (someValue != anotherValue [implicit AND] ( myfield == "value"  myNumber < 22 ...) [implicit AND] a < b )
 *    
 *  - An array of various parts, will result in an array when evaluated
 *  	- 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartFunction extends QueryPart {
	
	private CFWQueryContext context;
	private ArrayList<QueryPart> functionParameters = new ArrayList<>();
	private String functionName = null;
	private CFWQueryFunction function = null;
	
	
	/******************************************************************************************************
	 * 
	 * @throws ParseException if function is unknown
	 ******************************************************************************************************/
	public QueryPartFunction(CFWQueryContext context, String functionName, QueryPart functionParameter) throws ParseException {
		this.context=context;
		this.functionName = functionName;
		this.function = getFunctionInstance();
		this.add(functionParameter);
	}
	
	
	/******************************************************************************************************
	 * 
	 * @throws ParseException if function is unknown
	 ******************************************************************************************************/
	public QueryPartFunction(CFWQueryContext context, String functionName, QueryPart... functionParams) throws ParseException {
		this.context=context;
		this.functionName = functionName;
		this.function = getFunctionInstance();
		for(QueryPart part : functionParams) {
			this.add(part);
		}
	}
	
	/******************************************************************************************************
	 * 
	 * @throws ParseException if function is unknown
	 ******************************************************************************************************/
	public QueryPartFunction(CFWQueryContext context, String functionName, QueryPartGroup paramGroup) throws ParseException {
		this.context=context;
		this.functionName = functionName;
		this.function = getFunctionInstance();
		
		ArrayList<QueryPart> partsArray = paramGroup.getQueryPartsArray();

		for(QueryPart part : partsArray) {
			if(part instanceof QueryPartArray) {
				functionParameters.addAll( ((QueryPartArray)part).getAsParts() );
			}else {
				this.add(part);
			}	
		}
	}
	
	
	/******************************************************************************************************
	 * Returns the number of elements in the group.
	 * @throws ParseException 
	 * 
	 ******************************************************************************************************/
	private CFWQueryFunction getFunctionInstance() throws ParseException {

		CFWQueryFunction instance = CFW.Registry.Query.createFunctionInstance(this.context, functionName);
		if(instance == null) {
			throw new ParseException("There is no such method with the name '"+functionName+"'", -1);
		}
		
		return instance;
	}
	
	/******************************************************************************************************
	 * Returns the number of elements in the group.
	 * 
	 ******************************************************************************************************/
	public int paramCount() {
		return functionParameters.size(); 		
	}
		
	/******************************************************************************************************
	 * Adds a query part. If the query part is a QueryPartArray, the parts in that array are merged into
	 * this array.
	 * 
	 ******************************************************************************************************/
	public QueryPartFunction add(QueryPart part) {

		functionParameters.add(part);
		
		return this;
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		//------------------------------------
		//Evaluate params to QueryPartValue 
		ArrayList<QueryPartValue> parameterValues = new ArrayList<>();

		for(QueryPart param : functionParameters) {
			parameterValues.add(param.determineValue(object));
		}
		
		//------------------------------------
		//execute Function 
		return function.execute(object, parameterValues);
		
	}
	
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	protected ArrayList<QueryPart> getParameters() {
		return functionParameters;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	protected String getFunctionName() {
		return functionName;
	}
	
	
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty("partType", "Function");
		
		int i = 0;
		for(QueryPart part : functionParameters) {
			if(part != null) {
				debugObject.add("Parameter["+i+"]", part.createDebugObject(object));
			}else {
				debugObject.add("Parameter["+i+"]", null);
			}
			i++;
		}
		
		return debugObject;
	}

	
	
	
	
	

}

package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartBinaryExpression extends QueryPart {
	
	private QueryPart leftside;
	private CFWQueryTokenType type;
	private QueryPart rightside = null;
		
	/******************************************************************************************************
	 * Create an instance of the Binary Expression.
	 * In case of an expression that has only one side(e.g. OPERATOR_NOT), leave left side null and add value 
	 * to the right side.
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartBinaryExpression(CFWQueryContext context, QueryPart leftside, CFWQueryTokenType type, QueryPart rightside) {
		super(context);
		this.leftside = leftside;
		this.type = type;
		this.rightside = rightside;
	}
		
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getLeftSide() {
		return leftside;
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation as a string.
	 * @return a string or null
	 ******************************************************************************************************/
	public String getLeftSideAsString(EnhancedJsonObject object) {
		return leftside.determineValue(object).getAsString();
	}
	
	/******************************************************************************************************
	 * Returns the right side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getRightSide() {
		return rightside;
	}

	/******************************************************************************************************
	 * Evaluates the binary expression and returns the resulting value.
	 * If the object parameter is not null, first checks if either side of the expression is a fieldname. 
	 * In case the field is present, uses the fields value for the evaluation.
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		//-----------------------------------------
		// Evaluate Left Side
		QueryPartValue leftValue;
		if(leftside != null) { 
			leftValue = leftside.determineValue(object);
		}else {
			leftValue = QueryPartValue.newNull(this.context());
		}

		
		//-----------------------------------------
		// Evaluate Right Side
		QueryPartValue rightValue;
		if(rightside != null) { 
			rightValue = rightside.determineValue(object);
		}else {
			rightValue = QueryPartValue.newNull(this.context());
		}
		
		//-----------------------------------------
		// Leftside get value from object 
		if(object != null) {
			if(leftValue.isString()) {
				String potentialFieldname = leftValue.getAsString();
				if(object.has(potentialFieldname)) {
					leftValue = QueryPartValue.newFromJsonElement(this.context(), object.get(potentialFieldname));
				}
			}
		}
		
		//-----------------------------------------
		// Rightside get value from object 
		if(object != null) {
			if(rightValue.isString()) {
				String potentialFieldname = rightValue.getAsString();
				if(object.has(potentialFieldname)) {
					rightValue = QueryPartValue.newFromJsonElement(this.context(), object.get(potentialFieldname));
				}
			}
		}
		
		QueryPartValue evaluatedExpression = evaluateBinaryExpression(leftValue, rightValue);	
		
		return evaluatedExpression;
	}
	
	
	/******************************************************************************************************
	 * Returns "N/A" if not evaluateable, else returns value
	 *
	 ******************************************************************************************************/
	private QueryPartValue evaluateBinaryExpression(QueryPartValue leftValue, QueryPartValue rightValue){
		
		//boolean bothStrings = leftValue.isString() && rightValue.isString();
		boolean bothNumbers = leftValue.isNumberOrNumberString() && rightValue.isNumberOrNumberString();
		boolean bothBoolean = leftValue.isBoolOrBoolString() && rightValue.isBoolOrBoolString();;
		
		JsonElement evaluationResult = null;
		switch(type) {
			case OPERATOR_AND:	
				if(bothBoolean) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBoolean() && rightValue.getAsBoolean());
				}else {
					evaluationResult = new JsonPrimitive(false);
				}
			break;
								
			case OPERATOR_OR:
				if(bothBoolean) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBoolean() || rightValue.getAsBoolean());
				}else {
					evaluationResult = new JsonPrimitive(false);
				}
			break;
			
			case OPERATOR_NOT:
				if(rightValue.isBoolOrBoolString()) {
					evaluationResult = new JsonPrimitive(!rightValue.getAsBoolean());
				}else {
					//in any other case use right value as string
					evaluationResult = rightValue.getAsJson();
				}
			break;	
				
			case OPERATOR_EQUAL:
				if(leftValue.isString()) {
					evaluationResult = new JsonPrimitive(leftValue.getAsString().contains(rightValue.getAsString()));
				}else if(bothNumbers) {
					evaluationResult = new JsonPrimitive(0 == leftValue.getAsDouble().compareTo(rightValue.getAsDouble()));
				}else if(bothBoolean) {
					evaluationResult = new JsonPrimitive(0 == leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}
			break;
			
			case OPERATOR_EQUAL_EQUAL:
				if(leftValue.isString()) {
					evaluationResult = new JsonPrimitive(leftValue.getAsString().equals(rightValue.getAsString()));
				}else if(bothNumbers) {
					evaluationResult = new JsonPrimitive(0 == leftValue.getAsDouble().compareTo(rightValue.getAsDouble()));
				}else if(bothBoolean) {
					evaluationResult = new JsonPrimitive(0 == leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}
			break;
			
			case OPERATOR_EQUAL_NOT:
				if(leftValue.isString()) {
					evaluationResult = new JsonPrimitive(!leftValue.getAsString().equals(rightValue.getAsString()));
				}else if(bothNumbers) {
					evaluationResult = new JsonPrimitive(0 != leftValue.getAsDouble().compareTo(rightValue.getAsDouble()));
				}else if(bothBoolean) {
					evaluationResult = new JsonPrimitive(0 != leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}
			break;
			
			case OPERATOR_EQUAL_OR_GREATER:
				if(bothNumbers) {
					System.out.println("leftValue.getAsDouble(): "+leftValue.getAsDouble());
					System.out.println("rightValue.getAsDouble(): "+rightValue.getAsDouble());
					System.out.println("eval: "+(leftValue.getAsDouble() >= rightValue.getAsDouble()));
					evaluationResult = new JsonPrimitive(leftValue.getAsDouble() >= rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_EQUAL_OR_LOWER:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsDouble() <= rightValue.getAsDouble());
				}
			break;
			
			
			case OPERATOR_GREATERTHEN:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsDouble() > rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_LOWERTHEN:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsDouble() < rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_DIVIDE: 
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBigDecimal().divide(rightValue.getAsBigDecimal()));
				}
			break;
			
			case OPERATOR_MINUS:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBigDecimal().subtract(rightValue.getAsBigDecimal()));
				}
			break;
			
			case OPERATOR_MULTIPLY:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBigDecimal().multiply(rightValue.getAsBigDecimal()));
				}
			break;
			
			case OPERATOR_PLUS:
				if(bothNumbers) {
					evaluationResult = new JsonPrimitive(leftValue.getAsBigDecimal().add(rightValue.getAsBigDecimal()));
				}
			break;
			
			default:
				break;
		
		}
		
		if(evaluationResult != null) {
			return QueryPartValue.newFromJsonElement(this.context(), evaluationResult);
		}else {
			return QueryPartValue.newNull(this.context());
		}
	}
		
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty("partType", "Binary");
		
		if(leftside != null) {
			debugObject.add("leftside", leftside.createDebugObject(object) );
		} else {
			debugObject.add("leftside", JsonNull.INSTANCE);
		}
		
		
		debugObject.addProperty("binaryType", type.toString());
		
		
		
		if(rightside != null) {
			debugObject.add("rightside", rightside.createDebugObject(object));
		}else {
			debugObject.add("rightside", JsonNull.INSTANCE);
		}
		debugObject.addProperty("determinedValue", determineValue(object).toString());
		return debugObject;
	}

}

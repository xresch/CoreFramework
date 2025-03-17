package com.xresch.cfw.features.query.parse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartBinaryExpression extends QueryPart implements LeftRightEvaluatable {
	
	private QueryPart leftside;
	private CFWQueryTokenType type;
	private QueryPart rightside = null;
	
	private static BigDecimal BIG_ZERO = new BigDecimal(0);
	
	private CFWQueryContext context;
	
	/******************************************************************************************************
	 * Creates a clone of the QueryPart.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPart clone() {
		
		QueryPart cloneLeft = leftside.clone();
		QueryPart cloneRight = rightside.clone();
		
		QueryPartBinaryExpression clone = new QueryPartBinaryExpression(context, cloneLeft, type, cloneRight);
		clone.parent = this.parent;
		
		return clone;
	}
	
	/******************************************************************************************************
	 * Create an instance of the Binary Expression.
	 * In case of an expression that has only one side(e.g. OPERATOR_NOT), leave left side null and add value 
	 * to the right side.
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartBinaryExpression(CFWQueryContext context, QueryPart leftside, CFWQueryTokenType type, QueryPart rightside) {
		
		//-------------------------------------
		// Hack Operator Precedence
		if(rightside instanceof QueryPartBinaryExpression) {
			
			QueryPartBinaryExpression rightExpression = (QueryPartBinaryExpression)rightside; 
			
			int thisPrecedence = getOperatorPrecedence(type);
			int rightPrecedence = getOperatorPrecedence(rightExpression.getOperatorType());
			if(thisPrecedence != -1 
			&& rightPrecedence != -1
			) {
				
				//--------------------------------------------
				// Switch Precedence 
				// Make evaluate left to right
				// e.g 1 - 1 - 1 >> (1 - 1) - 1
				if(thisPrecedence >= rightPrecedence) {
					
					rightside = rightExpression.getRightSide();
					QueryPart rightLeft = rightExpression.getLeftSide();
					
					leftside = new QueryPartBinaryExpression(context, leftside, type, rightLeft);
					
					type = rightExpression.getOperatorType();
					
				}
				
			}
		}
		
		//-------------------------------------
		// Set the Values
		this.context = context;
		this.leftside = leftside;
		this.type = type;
		this.rightside = rightside;
		
	}
		
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public static int getOperatorPrecedence(CFWQueryTokenType type) {
		
		switch(type) {
//			case OPERATOR_REGEX:			
//				return 5;
//				
//			case OPERATOR_AND:				
//				return 4;
//			
//			case OPERATOR_EQUAL_EQUAL:		
//			case OPERATOR_EQUAL_NOT:		
//				return 4;
//				
//			case OPERATOR_EQUAL_OR_GREATER:	
//			case OPERATOR_EQUAL_OR_LOWER:	
//			case OPERATOR_GREATERTHEN:		
//			case OPERATOR_LOWERTHEN:
//				return 1;
//			
				
			case OPERATOR_POWER:			
				return 3;
			
			case OPERATOR_MULTIPLY:			
			case OPERATOR_DIVIDE:			
			case OPERATOR_MODULO:			
				 return 2;
				 
			case OPERATOR_NOT:           
			case OPERATOR_PLUS:				
			case OPERATOR_MINUS:			
				 return 1;
				 
			default: 
				return -1;
		}
	
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
	 * Returns the right side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public CFWQueryTokenType getOperatorType() {
		return type;
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
			leftValue = QueryPartValue.newNull();
		}

		
		//-----------------------------------------
		// Evaluate Right Side
		QueryPartValue rightValue;
		if(rightside != null) { 
			rightValue = rightside.determineValue(object);
		}else {
			rightValue = QueryPartValue.newNull();
		}
		
		//-----------------------------------------
		// Leftside get value from object 
		leftValue = leftValue.convertFieldnameToFieldvalue(object);

		
		//-----------------------------------------
		// Rightside get value from object 
		rightValue = rightValue.convertFieldnameToFieldvalue(object);
				
		QueryPartValue evaluatedExpression = evaluateBinaryExpression(context, leftValue, rightValue);	
		
		return evaluatedExpression;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue evaluateLeftRightValues(EnhancedJsonObject leftObject
										 , EnhancedJsonObject rightObject) 
										 throws Exception {
		
		//-----------------------------------------
		// Evaluate Left Side
		QueryPartValue leftValue;
		if(leftside != null) { 
			if(leftside instanceof LeftRightEvaluatable) {
				leftValue = ((LeftRightEvaluatable)leftside).evaluateLeftRightValues(leftObject, rightObject);
			}else {
				leftValue = leftside.determineValue(leftObject);
			}
		}else {
			leftValue = QueryPartValue.newNull();
		}
		
		//-----------------------------------------
		// Evaluate Right Side
		QueryPartValue rightValue;
		if(rightside != null) { 
			if(rightside instanceof LeftRightEvaluatable) {
				rightValue = ((LeftRightEvaluatable)rightside).evaluateLeftRightValues(leftObject, rightObject);
			}else {
				rightValue = rightside.determineValue(rightObject);
			}
		}else {
			rightValue = QueryPartValue.newNull();
		}

		
		//-----------------------------------------
		// Leftside get value from object 
		leftValue = leftValue.convertFieldnameToFieldvalue(leftObject);

		//-----------------------------------------
		// Rightside get value from object 
		rightValue = rightValue.convertFieldnameToFieldvalue(rightObject);
				
		QueryPartValue evaluatedExpression = evaluateBinaryExpression(context, leftValue, rightValue);	
		
		return evaluatedExpression;
	
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private boolean bothNumbers(QueryPartValue leftValue, QueryPartValue rightValue){
		return leftValue.isNumberOrNumberString() && rightValue.isNumberOrNumberString();
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private boolean bothBooleans(QueryPartValue leftValue, QueryPartValue rightValue){
		return leftValue.isBoolOrBoolString() && rightValue.isBoolOrBoolString();
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private boolean eitherBoolean(QueryPartValue leftValue, QueryPartValue rightValue){
		return leftValue.isBoolOrBoolString() || rightValue.isBoolOrBoolString();
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private void nullToZero(QueryPartValue leftValue, QueryPartValue rightValue){
		leftValue.nullToZero();
		rightValue.nullToZero();
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private boolean bothNull(QueryPartValue leftValue, QueryPartValue rightValue){
		return leftValue.isNull() && rightValue.isNull();
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	private boolean eitherNull(QueryPartValue leftValue, QueryPartValue rightValue){
		return leftValue.isNull() || rightValue.isNull();
	}
	
	/******************************************************************************************************
	 * Returns "null" if not evaluatable, else returns value.
	 * If both values are null 
	 *
	 ******************************************************************************************************/
	private QueryPartValue evaluateBinaryExpression(CFWQueryContext context, QueryPartValue leftValue, QueryPartValue rightValue){
			
		JsonElement evalResult = null;
		
		BigDecimal rightDeci;
		BigDecimal leftDeci;
		
		switch(type) {
			case OPERATOR_AND:	
				if(bothBooleans(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBoolean() && rightValue.getAsBoolean());
				}else {
					evalResult = new JsonPrimitive(false);
				}
			break;
								
			case OPERATOR_OR:
				if(eitherBoolean(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBoolean() || rightValue.getAsBoolean());
				}else {
					evalResult = new JsonPrimitive(false);
				}
			break;
			
			case OPERATOR_NOT:
				if(rightValue.isBoolOrBoolString()) {
					evalResult = new JsonPrimitive(!rightValue.getAsBoolean());
				}else {
					//in any other case use right value as string
					evalResult = rightValue.getAsJsonElement();
				}
			break;	
				
			case OPERATOR_EQUAL:
				if(leftValue.isString()) {
					evalResult = new JsonPrimitive(leftValue.getAsString().contains(rightValue.getAsString()));
				}else if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 == leftValue.getAsDouble().compareTo(rightValue.getAsDouble()));
				}else if(bothBooleans(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 == leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}
			break;
			
			case OPERATOR_EQUAL_EQUAL:

				if(leftValue.isString() || rightValue.isString()) {
					if(!leftValue.isNull()) {
						evalResult = new JsonPrimitive(leftValue.getAsString().equals(rightValue.getAsString()));
					}else {
						evalResult = new JsonPrimitive(false);
					}
				}else if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 == leftValue.getAsBigDecimal().compareTo(rightValue.getAsBigDecimal()));
				}else if(bothBooleans(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 == leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}else if(bothNull(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(true);
				} 
			break;
			
			case OPERATOR_EQUAL_NOT:
				if(bothNull(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(false);
				}else if (eitherNull(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(true);
				}else if(leftValue.isString() || rightValue.isString()) {
					evalResult = new JsonPrimitive(!leftValue.getAsString().equals(rightValue.getAsString()));
				}else if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 != leftValue.getAsBigDecimal().compareTo(rightValue.getAsBigDecimal()));
				}else if(bothBooleans(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(0 != leftValue.getAsBoolean().compareTo(rightValue.getAsBoolean()));
				}else if (bothNull(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(false);
				}
			break;
			
			case OPERATOR_EQUAL_OR_GREATER:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsDouble() >= rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_EQUAL_OR_LOWER:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsDouble() <= rightValue.getAsDouble());
				}
			break;
			
			
			case OPERATOR_REGEX:
				if(rightValue.isNull() || leftValue.isNull()) { 
					evalResult = new JsonPrimitive(false); 
				}else {
					try{
						Pattern pattern = Pattern.compile(rightValue.getAsString());
						evalResult = new JsonPrimitive(pattern.matcher(leftValue.getAsString()).find());
					}catch(Exception e) {
						context.addMessage(MessageType.ERROR, e.getMessage());
					}
				}
			break;
			
			
			case OPERATOR_GREATERTHEN:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsDouble() > rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_LOWERTHEN:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsDouble() < rightValue.getAsDouble());
				}
			break;
			
			case OPERATOR_PLUS:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBigDecimal().add(rightValue.getAsBigDecimal()));
				}else {
					//Do concatination
					evalResult = new JsonPrimitive(leftValue.getAsString() + rightValue.getAsString());
				}
			break;
			
			case OPERATOR_MINUS:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBigDecimal().subtract(rightValue.getAsBigDecimal()));
				}
			break;
			
			case OPERATOR_MULTIPLY:
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBigDecimal().multiply(rightValue.getAsBigDecimal()));
				}
			break;
						
			case OPERATOR_DIVIDE:
				nullToZero(leftValue, rightValue);
				leftDeci = leftValue.getAsBigDecimal();
				rightDeci = rightValue.getAsBigDecimal();
				if(rightDeci.compareTo(BIG_ZERO) == 0) {   
					
					evalResult = JsonNull.INSTANCE;
				}else {
					if(bothNumbers(leftValue, rightValue)) {
						try {
							int biggerScale = leftDeci.scale();
							
							// this is needed to support very small numbers
							if(biggerScale < rightDeci.scale()) { biggerScale = rightDeci.scale(); }
							// this is needed to get actual precision when dividing numbers
							biggerScale += 3;
							
							evalResult = new JsonPrimitive(leftDeci.divide(rightDeci, biggerScale, RoundingMode.HALF_UP));
						}catch(Exception e) {
							context.addMessage(MessageType.ERROR, e.getMessage());
						}
					}
				}
			break;
			
			case OPERATOR_MODULO:
				nullToZero(leftValue, rightValue);
				leftDeci = leftValue.getAsBigDecimal();
				rightDeci = rightValue.getAsBigDecimal();
				if(rightDeci.compareTo(BIG_ZERO) == 0) {   
					
					evalResult = JsonNull.INSTANCE;
				}else {
					if(bothNumbers(leftValue, rightValue)) {
						try {
							evalResult = new JsonPrimitive(leftDeci.remainder(rightDeci));
						}catch(Exception e) {
							context.addMessage(MessageType.ERROR, e.getMessage());
						}
					}
				}
				break;
			
			case OPERATOR_POWER: 
				nullToZero(leftValue, rightValue);
				if(bothNumbers(leftValue, rightValue)) {
					evalResult = new JsonPrimitive(leftValue.getAsBigDecimal().pow(rightValue.getAsInteger()));
				}
			break;

			
			default:
				break;
		
		}
		
		if(evalResult != null) {
			return QueryPartValue.newFromJsonElement(evalResult);
		}else {
			return QueryPartValue.newNull();
		}
	}
		
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty(QueryPart.FIELD_PARTTYPE, "Binary");
		
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
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public void setParentCommand(CFWQueryCommand parent) {
		
		this.parent = parent;
		if(leftside != null) {	this.leftside.setParentCommand(parent); }
		if(rightside != null) {	this.rightside.setParentCommand(parent); }
		
	}

}

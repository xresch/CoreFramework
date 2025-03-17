package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * QueryPart that will hold the function expressions.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartFunction extends QueryPart {
	
	private static final Logger logger = CFWLog.getLogger(QueryPartFunction.class.getName());
	
	private CFWQueryContext context;
	private ArrayList<QueryPart> functionParameters = new ArrayList<>();
	private String functionName = null;
	private CFWQueryFunction internalfunctionInstance = null;
	
	// instance id and instance
	private LinkedHashMap<String, CFWQueryFunction> managedInstances = new LinkedHashMap<>();
	
	
	/******************************************************************************************************
	 * Creates a clone of the QueryPart.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartFunction clone() {
		
		ArrayList<QueryPart> clonedFunctionParams = new ArrayList<>();
		for(QueryPart part : functionParameters) {
			clonedFunctionParams.add(part.clone());
		}
		
		
		QueryPartFunction clone;
		try {
			clone = new QueryPartFunction(context, functionName, clonedFunctionParams);
			clone.parent = this.parent;
			return clone;
		} catch (ParseException e) {
			// Let's assume this will never happen... ever!!!
			// Assumption based on the fact that function was already successfully parsed.
			new CFWLog(logger).severe("Error cloning function: "+e.getMessage(), e);
		}
		
		return null;
	}
		
	
	/******************************************************************************************************
	 * 
	 * @throws ParseException if function is unknown
	 ******************************************************************************************************/
	public QueryPartFunction(CFWQueryContext context, String functionName, ArrayList<QueryPart> functionParams) throws ParseException {
		this.context=context;
		this.functionName = functionName;
		this.internalfunctionInstance = getFunctionInstance();
		
		if(this.internalfunctionInstance.validateQueryParts(functionParams, context.checkPermissions())) {
			
			for(QueryPart part : functionParams) {
				if(part instanceof QueryPartArray) {
					QueryPartArray array = (QueryPartArray)part;
					if(!array.isEmbracedArray()) {
						functionParameters.addAll( ((QueryPartArray)part).getAsParts() );
					}else {
						functionParameters.add(array);
					}
				}else {
					this.add(part);
				}	
			}
		}
	}
	
	/******************************************************************************************************
	 * 
	 * @throws ParseException if function is unknown
	 ******************************************************************************************************/
	public QueryPartFunction(CFWQueryContext context, String functionName, QueryPartGroup paramGroup) throws ParseException {
		this(context,functionName, paramGroup.getQueryPartsArray());
	}
	
	
	/******************************************************************************************************
	 * Returns the number of elements in the group.
	 * @throws ParseException 
	 * 
	 ******************************************************************************************************/
	public String createManagedInstance() {

		CFWQueryFunction instance = CFW.Registry.Query.createFunctionInstance(this.context, functionName);
		if(instance == null) {
			context.addMessage(MessageType.ERROR, "There is no such method with the name '"+functionName+"'");
		}
		
		instance.setParentCommand(this.parent);
		
		String instanceID = CFW.Random.randomStringAlphaNumerical(32);
		managedInstances.put(instanceID, instance);
		return instanceID;
	}
	
	/******************************************************************************************************
	 * Returns the number of elements in the group.
	 * @throws ParseException 
	 * 
	 ******************************************************************************************************/
	public CFWQueryFunction getFunctionInstance() throws ParseException {

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
	public String getDefaultLabel() {
		
		StringBuilder builder = new StringBuilder(functionName);
		
		builder.append("(");
			for(int i=0; i < functionParameters.size(); i++) {
				builder.append(functionParameters.get(i).determineValue(null).getAsString());
				
				if(i < functionParameters.size()-1) {
					builder.append(",");
				}
			}
		builder.append(")");
		return builder.toString(); 		
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
	 * 
	 ******************************************************************************************************/
	public ArrayList<QueryPartValue> prepareParameters(EnhancedJsonObject object, boolean receiveStringParamsLiteral) {
		ArrayList<QueryPartValue> parameterValues = new ArrayList<>();

		//----------------------------
		// Iterate Function Parameters
		for(QueryPart param : functionParameters) {
			
			//----------------------------
			// Use Literal Values 
			if(receiveStringParamsLiteral 
			&& param instanceof QueryPartValue) {
				parameterValues.add(param.determineValue(object));
				continue;
			}
			
			//----------------------------
			// Substitute Fieldnames with Values
			if(param instanceof QueryPartValue) {
				param = ((QueryPartValue)param).convertFieldnameToFieldvalue(object);
			}
			
			//----------------------------
			// All other
			parameterValues.add(param.determineValue(object));
			
		}
		
		return parameterValues;
	}
	
	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	public void aggregateFunctionInstance(String instanceID, EnhancedJsonObject object) {
		CFWQueryFunction  function = managedInstances.get(instanceID);
		aggregateFunctionInstance(function, object);
	}
	
	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	private void aggregateFunctionInstance(CFWQueryFunction functionInstance, EnhancedJsonObject object) {
		//------------------------------------
		//Evaluate params to QueryPartValue 
		ArrayList<QueryPartValue> parameterValues = prepareParameters(object, functionInstance.receiveStringParamsLiteral());
		
		//------------------------------------
		//execute Function 
		functionInstance.aggregate(object, parameterValues);
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	public QueryPartValue executeFunctionInstance(String instanceID, EnhancedJsonObject object) {
		CFWQueryFunction  function = managedInstances.get(instanceID);
		return executeFunctionInstance(function, object);
	}
	
	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	private QueryPartValue executeFunctionInstance(CFWQueryFunction functionInstance, EnhancedJsonObject object) {
		
		
		ArrayList<QueryPartValue> parameterValues = prepareParameters(object, functionInstance.receiveStringParamsLiteral() );
		
		//------------------------------------
		//execute Function 
		return functionInstance.execute(object, parameterValues);
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		internalfunctionInstance.setParentCommand(this.parent);
		
		return executeFunctionInstance(internalfunctionInstance, object);
		
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
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty(QueryPart.FIELD_PARTTYPE, "Function");
		
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
	
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public void setParentCommand(CFWQueryCommand parent) {
		
		this.parent = parent;
		
		for(QueryPart part : functionParameters) {
			if(part != null) { part.setParentCommand(parent); }
		}
		
	}

	
	
	
	
	

}

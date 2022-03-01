package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartSubquery extends QueryPart {
	
	private ArrayList<CFWQuery> queryList;
	private String leftside;
	private QueryPart value = null;
		
	/******************************************************************************************************
	 * @throws ParseException
	 ******************************************************************************************************/
	private QueryPartSubquery(CFWQueryContext parentContext, String query) throws ParseException {
		super();
		CFWQueryParser parser = new CFWQueryParser(query, true);
		
		queryList = parser.parse();
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public String getLeftSide() {
		return leftside;
	}

	/******************************************************************************************************
	 * Returns the value of the .
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		//TODO return results as JSON array part.
		return value.determineValue(null);
	}
	
	/******************************************************************************************************
	 * Returns the value of the assignment.
	 * 
	 ******************************************************************************************************/
	public void executeSubquery(LinkedBlockingQueue<EnhancedJsonObject> outQueue) {
		
		//TODO execute query write back to inQueue of parent query
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();

		debugObject.addProperty("partType", "Subquery");
		
		//TODO
//		debugObject.add("leftside", leftside.createDebugObject(object));
//		debugObject.add("rightside", leftside.createDebugObject(object));

		return debugObject;
	}
	

}

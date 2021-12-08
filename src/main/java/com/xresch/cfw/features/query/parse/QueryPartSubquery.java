package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
		super(parentContext);
		CFWQueryParser parser = new CFWQueryParser(query);
		
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
	public QueryPartValue determineValue() {
		//TODO return results as JSON array part.
		return value.determineValue();
	}
	
	/******************************************************************************************************
	 * Returns the value of the assignment.
	 * 
	 ******************************************************************************************************/
	public void executeSubquery(LinkedBlockingQueue<EnhancedJsonObject> outQueue) {
		
		//TODO execute query write back to inQueue of parent query
	}
	
	

}

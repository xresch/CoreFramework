package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.EnhancedJsonObject;

public class QueryPartSubquery extends QueryPart {
	
	private ArrayList<CFWQuery> queryList;
	private String leftside;
	private QueryPart value = null;
		
	/******************************************************************************************************
	 * @throws ParseException
	 ******************************************************************************************************/
	private QueryPartSubquery(String query) throws ParseException {
		
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

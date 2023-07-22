package com.xresch.cfw.features.query;

import java.util.ArrayList;

import com.google.gson.JsonArray;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryResultList {
	

	private ArrayList<CFWQueryResult> resultList = new ArrayList<>();
	
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResultList() {
		
	}
	
	/****************************************************
	 * Returns an array of resultObjects.
	 * Use toJsonRecords() to get all records of all 
	 * results in a single array.
	 ****************************************************/
	public JsonArray toJson() {
		
		JsonArray array = new JsonArray();
		
		for(CFWQueryResult result : resultList) {
			array.add(result.toJson());
		}
		return array;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonArray toJsonRecords() {
		
		JsonArray array = new JsonArray();
		
		for(CFWQueryResult result : resultList) {
			array.addAll(result.getResults());
		}
		return array;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public int size() {
		return resultList.size();
	}
	
	/****************************************************
	 * Returns the result at the specified position in this list.
	 ****************************************************/
	public CFWQueryResult get(int index) {
		return resultList.get(index);
	}
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResultList addResult(CFWQueryResult result) {
		if(result == null) {
			return this;
		}
		
		resultList.add(result);
		return this;
	}
	
	/****************************************************
	 * Return true if the list contained the specified element
	 ****************************************************/
	public boolean removeResult(CFWQueryResult result) {
		if(result == null) {
			return false;
		}
		
		return resultList.remove(result);
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public ArrayList<CFWQueryResult> getResultList() {
		return resultList;
	}


}

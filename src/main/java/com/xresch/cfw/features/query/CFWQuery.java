package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.xresch.cfw.pipeline.Pipeline;
/**************************************************************************************************************
 * 
 * <CFWQuery> ::= <CFWCommand>+ 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuery extends Pipeline<EnhancedJsonObject, EnhancedJsonObject>{
	
	private ArrayList<CFWQueryCommand> commandList = new ArrayList<>();
	private HashSet<String> fieldnames = new HashSet<>();
	
	private long earliest = 0;
	private long latest = 0;
	
	
	/***********************************************************************************************
	 * Add a query command to the query.
	 ***********************************************************************************************/
	public void addCommand(CFWQueryCommand command){
		commandList.add(command);
		
		this.add(command);
	}

	
	/***********************************************************************************************
	 * Get the earliest time for this query.
	 ***********************************************************************************************/
	public long getEarliest() {
		return earliest;
	}
	
	/***********************************************************************************************
	 * Set the earliest time for this query.
	 ***********************************************************************************************/
	public CFWQuery setEarliest(long earliest) {
		this.earliest = earliest;
		return this;
	}

	/***********************************************************************************************
	 * Get the latest time for this query.
	 ***********************************************************************************************/
	public long getLatest() {
		return latest;
	}

	/***********************************************************************************************
	 * Set the latest time for this query.
	 ***********************************************************************************************/
	public CFWQuery setLatest(long latest) {
		this.latest = latest;
		return this;
	}

	/***********************************************************************************************
	 * Set the latest time for this query.
	 ***********************************************************************************************/
	public CFWQuery addFieldnames(Set<String> names) {
		fieldnames.addAll(names);
		return this;
	}

}

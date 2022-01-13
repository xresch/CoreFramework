package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
		
	private CFWQueryContext context = new CFWQueryContext();
	
	/***********************************************************************************************
	 * Add a query command to the query.
	 ***********************************************************************************************/
	public void addCommand(CFWQueryCommand command){
		commandList.add(command);
		
		this.add(command);
	}
	
	
	/***********************************************************************************************
	 * Get the context of this query
	 ***********************************************************************************************/
	public CFWQueryContext getContext() {
		return context;
	}
	
	/***********************************************************************************************
	 * Set the context of this query
	 ***********************************************************************************************/
	public CFWQuery setContext(CFWQueryContext context) {
		this.context = context;
		return this;
	}
	
	/***********************************************************************************************
	 * Get the commands of this query
	 ***********************************************************************************************/
	public ArrayList<CFWQueryCommand> getCommandList() {
		return commandList;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery addFieldnames(Set<String> names) {
		fieldnames.addAll(names);
		return this;
	}

}

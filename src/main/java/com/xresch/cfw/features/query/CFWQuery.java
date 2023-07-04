package com.xresch.cfw.features.query;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.commands.CFWQueryCommandComment;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMetadata;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.pipeline.Pipeline;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * <CFWQuery> ::= <CFWCommand>+ 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuery extends Pipeline<EnhancedJsonObject, EnhancedJsonObject>{
	
	private ArrayList<CFWQueryCommand> commandList = new ArrayList<>();

	private CFWQueryContext context = new CFWQueryContext();
	
	/***********************************************************************************************
	 * Create a new query with a new Context.
	 ***********************************************************************************************/
	public  CFWQuery(){
		this(new CFWQueryContext(), false);
	}
	
	/***********************************************************************************************
	 * Add a query command to the query.
	 * @param doCloneContext if true, creates a clone of the original, if false uses as is
	 ***********************************************************************************************/
	public  CFWQuery(CFWQueryContext contextToUse, boolean doCloneContext){
		if(doCloneContext) {
			context = contextToUse.createClone(true);
		}else {
			context = contextToUse;
		}
	}
	
	
	/***********************************************************************************************
	 * Add a query command to the query.
	 ***********************************************************************************************/
	public void addCommand(CFWQueryCommand command){
		commandList.add(command);
		
		this.add(command);
	}
	
	/***********************************************************************************************
	 * Removes commands by name.
	 ***********************************************************************************************/
	public void removeCommandsByName(String commandName){
		
		// avoid java.util.ConcurrentModificationException
		ArrayList<CFWQueryCommand> removeList = new ArrayList<>();
		
		for(CFWQueryCommand command : commandList) {
			
			if(command.isCommandName(commandName)) {
				removeList.add(command);
			}
		}
		
		for(CFWQueryCommand command : removeList) {
			this.remove(command);
			commandList.remove(command);
		}
		
	}
	
	/***********************************************************************************************
	 * Checks if the query has reached the configured record limit by summarizing all source limits.
	 ***********************************************************************************************/
	public boolean isSourceLimitReached() {
		
		int sumOfSourceLimits = 0;
		
		for(CFWQueryCommand command : commandList) {
			if(command instanceof CFWQueryCommandSource) {
				sumOfSourceLimits +=   ((CFWQueryCommandSource)command).getLimit();
			}
		}
		
		
		int maxRecords = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_QUERY_RECORD_LIMIT);
		if(sumOfSourceLimits > maxRecords) {
			context.addMessage(MessageType.ERROR, "Sum of all source limits cannot exceed "+maxRecords+" per query.");
			return true;
		}
		
		return false;
	}
	
	/***********************************************************************************************
	 * Checks if the query has reached the configured commands limit per query.
	 ***********************************************************************************************/
	public boolean isCommandLimitReached() {
		
		int count = 0;
		
		for(CFWQueryCommand command : commandList) {
			if( !(command instanceof CFWQueryCommandComment) 
			&&  !(command instanceof CFWQueryCommandMetadata)) {
				count++;
			}
		}
		
		
		int maxCommands = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_QUERY_COMMAND_LIMIT);
		if(count > maxCommands) {
			context.addMessage(MessageType.ERROR, "Number of commands is limited to "+maxCommands+" per query.");
			return true;
		}
		
		return false;
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
	 * Returns a copy of the list of commands of this query.
	 * Copy is returned to prevent modifications to the original list.
	 * Us the methods addCommand() and removeCommand() to manipulate the list.
	 * The instances of the commands are originals and not copies and can be modified.
	 ***********************************************************************************************/
	public ArrayList<CFWQueryCommand> getCopyOfCommandList() {
		ArrayList<CFWQueryCommand> copy = new ArrayList<CFWQueryCommand>();
		copy.addAll(commandList);
		return copy;
	}


	
}

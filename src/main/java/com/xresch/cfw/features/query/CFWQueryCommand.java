package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryCommand extends PipelineAction<EnhancedJsonObject, EnhancedJsonObject> {

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommand.class.getName());
	
	private boolean isManipulativeCommand = true;
	
	private CFWQueryCommandSource previousSource = null;
	private boolean isPreviousSourceInitialized = false;
	
	protected CFWQuery parent;
	
	public CFWQueryCommand(CFWQuery parent) {
		this.parent = parent;
	}
	
	/***********************************************************************************************
	 * Return the unique name and aliases of the command.
	 ***********************************************************************************************/
	public abstract String[] uniqueNameAndAliases();
	
	/***********************************************************************************************
	 * Return the unique name of the command.
	 ***********************************************************************************************/
	public String getUniqueName() {
		return this.uniqueNameAndAliases()[0];
	}
	

	/***********************************************************************************************
	 * Returns true if the parameter is a name or alias of this command(case insensitive).
	 ***********************************************************************************************/
	public boolean isCommandName(String commandName) {
		String[] names = this.uniqueNameAndAliases();
		commandName = commandName.trim().toLowerCase();
		for(String name : names) {
			if(name.trim().toLowerCase().equals(commandName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/***********************************************************************************************
	 * Returns true if the parameter is a name or alias of this command(case insensitive).
	 ***********************************************************************************************/
	public CFWQueryContext getQueryContext() {
		if(parent != null) {
			return parent.getContext();
		}
		return null;
	}
	
	/***********************************************************************************************
	 * Return an arrayList with Tags
	 ***********************************************************************************************/
	public abstract TreeSet<String> getTags();
	
	
	/***********************************************************************************************
	 * Return a short description that can be shown in content assist and will be used as intro text
	 * in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionShort();
	
	/***********************************************************************************************
	 * Return the syntax as a single line. This will be shown in the manual and in content assist.
	 * Will be added in the manual under the header " <h2>Syntax</h2>" as a "<pre><code>" element. 
	 ***********************************************************************************************/
	public abstract String descriptionSyntax();
	
	/***********************************************************************************************
	 * If you want to add further details to the syntax section.
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionSyntaxDetailsHTML();
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();

	/***********************************************************************************************
	 * This method receives the query parts associated with this command during parsing.
	 * When implementing this method you want to iterate over the parts array and check if the part
	 * are of the QueryPart Subclasses that are supported by your command.
	 * If you encounter anything you cannot work with you can either ignore it or throw an exception
	 * using parser.throwException().
	 * 
	 * IMPORTANT:
	 *   - To make this method work properly when using the command "mimic", the query context should
	 * not be changed in this method.
	 *   - To make this method work properly with function globals() and meta(), no calls to 
	 *     QueryPart.determineValue() should be made.
	 *   - If you need to do any of the above you can store the QueryParts in a field and override 
	 *     the method initializeAction(). 
	 * 
	 * Following is an example implementation that finds fieldnames and the parameter "customParam":
	 * <pre><code>
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		//------------------------------------------
		// Get Parameters
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);

			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
			}
		}	
	}
	
	public void initializeAction()  throws Exception {
		//------------------------------------------
		// Get Parameters
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();
				if		 (assignmentName.equals("name")) {		queryName = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("remove")) {	commandsToRemove = assignmentValue.getAsStringArray(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported argument.", -1);
				}
				
			}
		}
	}
	 </code></pre>
	 * 
	 ***********************************************************************************************/
	public abstract void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts)  throws ParseException;
	
	
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper);
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
	
	/***********************************************************************************************
	 * Returns the first Source command found that is preceeding this command. Can be null
	 ***********************************************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWQueryCommandSource getPreviousSourceCommand() {
		
		PipelineAction previousAction = this.getPreviousAction();
		if(previousSource == null && !isPreviousSourceInitialized) {
			
			while(previousSource == null && previousAction != null) {
				if(previousAction instanceof CFWQueryCommandSource) {
					previousSource = (CFWQueryCommandSource)previousAction;
					break;
				}else {
					previousAction = previousAction.getPreviousAction();
				}	
			}
			
			isPreviousSourceInitialized = true;
			
		}
		
		return previousSource;
	}
	
	/***********************************************************************************************
	 * Returns the next Source command found that is following this command. Can be null
	 ***********************************************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWQueryCommandSource getNextSourceCommand() {
		
		PipelineAction nextAction = this.getNextAction();
		CFWQueryCommandSource source = null;
		while(source == null && nextAction != null) {
			if(nextAction instanceof CFWQueryCommandSource) {
				source = (CFWQueryCommandSource)nextAction;
				break;
			}else {
				nextAction = nextAction.getNextAction();
			}
				
		}
		return source;
	}
	
	/***********************************************************************************************
	 * Returns the last action in the pipeline of type CFWQueryCommand. 
	 * Can return null if the executing command is the last action.
	 ***********************************************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWQueryCommand getLastCommand() {
		
		CFWQueryCommand lastCommand = null;
		
		PipelineAction nextCommand = this.getNextAction();
		while(nextCommand != null) {
			
			if(nextCommand instanceof CFWQueryCommand) {
				lastCommand = (CFWQueryCommand)nextCommand;
				break;
			}
			
			nextCommand = nextCommand.getNextAction();
				
		}
		return lastCommand;
	}
	
	/***********************************************************************************************
	 * Internal use to manage changes to fieldnames.
	 ***********************************************************************************************/
	private CFWQueryFieldnameManager getSourceFieldmanager() {
		
		CFWQueryCommandSource localPreviousSource = this.getPreviousSourceCommand();

		if(localPreviousSource == null) { return this.parent.getContext().contextFieldnameManager;} 
//		}else {
//			this.parent.getContext()localPreviousSource.contextField
//		}
		return localPreviousSource.getFieldManager();
	}
	
	/***********************************************************************************************
	 * Add a fieldname
	 * @return 
	 ***********************************************************************************************/
	protected LinkedHashSet<String> fieldnameGetAll() {
		
		//-----------------------------------
		// Add Change to source fieldmanager
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		
		return sourceFieldmanager.getFinalFieldList();
		
	}
	
	/***********************************************************************************************
	 * Add a fieldname
	 ***********************************************************************************************/
	protected void fieldnameAdd(String fieldname) {
		
		//-----------------------------------
		// Add Change to source fieldmanager
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			sourceFieldmanager.add(fieldname);
		}
	}
	
	/***********************************************************************************************
	 * Add all fieldnames, except for fieldnames that start with an underscore.
	 ***********************************************************************************************/
	protected void fieldnameAddAll(JsonArray array) {
		
		//-----------------------------------
		// Add Change to Source fieldmanager
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			for(JsonElement e : array) {
				if(e != null 
				&& e.isJsonPrimitive()
				&& e.getAsJsonPrimitive().isString()
				) {
					sourceFieldmanager.add(e.getAsString());
				}
			}
		}
		
	}
	/***********************************************************************************************
	 * Add all fieldnames, except for fieldnames that start with an underscore.
	 ***********************************************************************************************/
	protected void fieldnameAddAll(ArrayList<String> array) {
		
		//-----------------------------------
		// Add Change to Source fieldmanager
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			for(String fieldname : array) {
				sourceFieldmanager.add(fieldname);
				
			}
		}
		
	}
	
	/***********************************************************************************************
	 * Add all fieldnames, except for fieldnames that start with an underscore.
	 ***********************************************************************************************/
	protected void fieldnameAddAll(EnhancedJsonObject record) {
		
		//-----------------------------------
		// Add Change to Source fieldmanager
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			
			sourceFieldmanager.addall(record.keySet());
		}
		
	}
	
	/***********************************************************************************************
	 * Remove a Fieldname
	 ***********************************************************************************************/
	protected void fieldnameRename(String fieldname, String newName) {
		
		//-----------------------------------
		// Add Change to source
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
		
		if(sourceFieldmanager != null) {
			sourceFieldmanager.rename(fieldname, newName);
		}
		
	}
	
	/***********************************************************************************************
	 * Remove a Fieldname
	 ***********************************************************************************************/
	protected void fieldnameRemove(String fieldname) {
		
		//-----------------------------------
		// Add Change to source
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			sourceFieldmanager.remove(fieldname);
		}
		
	}
	
	/***********************************************************************************************
	 * Keep the listed Fieldnames
	 ***********************************************************************************************/
	protected void fieldnameKeep(String... fieldnames) {
		
		//-----------------------------------
		// Add Change to source
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			sourceFieldmanager.keep(fieldnames);
		}
		
	}
	
	/***********************************************************************************************
	 * Removes all existing fieldnames
	 ***********************************************************************************************/
	protected void fieldnameClearAll() {
		
		//-----------------------------------
		// Add Change to source
		CFWQueryFieldnameManager sourceFieldmanager = this.getSourceFieldmanager();
			
		if(sourceFieldmanager != null) {
			sourceFieldmanager.clear();
		}
		
	}
	
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public boolean isManipulativeCommand() {
		return isManipulativeCommand;
	}
	
	/****************************************************************************
	 * If a command does not manipulate any data in the queue, set this 
	 * flag to false in the constructor. 
	 * In this case the inQueue and outQueue of the command will be set 
	 * the same, what will pass the data directly from the previous command
	 * to the next command.
	 * 
	 ****************************************************************************/
	public void isManipulativeCommand(boolean value) {
		this.isManipulativeCommand = value;
	}
	
	/****************************************************************************
	 * Overridden as this command does not manipulate any data.
	 * Make the InQueue the same as the out Queue to directly give the input
	 * from the previous command to the next command.
	 ****************************************************************************/
	@Override
	public PipelineAction<EnhancedJsonObject, EnhancedJsonObject> setInQueue(LinkedBlockingQueue<EnhancedJsonObject> in) {
		
		if(isManipulativeCommand){
			this.inQueue = in;
		}else{
			this.setOutQueue(in);
		}
		return this;
	}
	
	/****************************************************************************
	 * Overridden as this command does not manipulate any data.
	 * Make the InQueue the same as the out Queue to directly give the input
	 * from the previous command to the next command.
	 * 
	 ****************************************************************************/
	@Override
	public PipelineAction<EnhancedJsonObject, EnhancedJsonObject> setOutQueue(LinkedBlockingQueue<EnhancedJsonObject> out) {

		if(isManipulativeCommand){
			this.outQueue = out;
		}else{
			this.inQueue = out;
			
			PipelineAction<?, EnhancedJsonObject> previousAction = this.getPreviousAction();
			if(previousAction != null
			&& (previousAction.getOutQueue() == null 
			   || !previousAction.getOutQueue().equals(out)) // prevent StackOverflow
		    ){ 
				previousAction.setOutQueue(out); 
			}
			
			PipelineAction<EnhancedJsonObject, ?> nextAction = this.getNextAction();
			if(nextAction != null
			&& ( nextAction.getInQueue() == null
			    || !nextAction.getInQueue().equals(out)) // prevent StackOverflow
			){ 
				nextAction.setInQueue(out); 
			}
		}
		
		return this;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void handleException(Throwable e) {
		new CFWLog(logger).severe(e.getMessage(), e);
	} 
	
	/****************************************************************************
	 * Override Run method to add handling for Memory issues
	 ****************************************************************************/
	@Override
	public void run() {
		try {
			super.run();
			
		} catch (CFWQueryMemoryException e) { 
			//this.parent.getContext().addMessage(MessageType.ERROR, "Query did not complete. Seems the memory limit was reached.");
			new CFWLog(logger).severe("Query ran into a memory exception.", e);
		} catch (NullPointerException e) { 
			//this.parent.getContext().addMessage(MessageType.ERROR, "A Null pointer exception occured.");
			new CFWLog(logger).severe("Query ran into a null pointer exception.", e);
		} catch (Exception e) { 
			//this.parent.getContext().addMessage(MessageType.ERROR, "An unexpected error occured: "+e.getMessage());
			new CFWLog(logger).silent(true).severe(e.getMessage(), e);
		}
		
	}
}

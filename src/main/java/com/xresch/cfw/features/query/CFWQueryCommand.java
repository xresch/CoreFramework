package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryCommand extends PipelineAction<EnhancedJsonObject, EnhancedJsonObject> {

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommand.class.getName());
	
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

		if(localPreviousSource == null) { return null;} 
		
		return localPreviousSource.getFieldManager();
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
	
	
	
	
	
	/****************************************************************************
	 * Override Run method to add handling for Memory issues
	 ****************************************************************************/
	@Override
	public void run() {
		try {
			super.run();
			
		} catch (CFWQueryMemoryException e) { 
			this.parent.getContext().addMessage(MessageType.ERROR, "Query did not complete. Seems the memory limit was reached.");
			new CFWLog(logger).silent(true).severe("Query ran into a memory exception.");
		} catch (NullPointerException e) { 
			this.parent.getContext().addMessage(MessageType.ERROR, "A Null pointer exception occured..");
			new CFWLog(logger).silent(true).severe("Query ran into a null pointer exception.");
		} catch (Exception e) { 
			this.parent.getContext().addMessage(MessageType.ERROR, "An unexpected error occured: "+e.getMessage());
			new CFWLog(logger).silent(true).severe("Query ran into an unexpected error.", e);
		}
		
	}
}

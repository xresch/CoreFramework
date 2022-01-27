package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;

import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.pipeline.PipelineAction;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryCommand extends PipelineAction<EnhancedJsonObject, EnhancedJsonObject> {

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
		
}

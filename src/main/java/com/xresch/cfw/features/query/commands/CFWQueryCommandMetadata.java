package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandMetadata extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandMetadata.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> fieldnames = new ArrayList<>();
		
	int recordCounter = 0;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandMetadata(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"metadata", "meta"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sets metadata of the query.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "metadata <key>=<value> [, <key>=<value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>key:&nbsp;</b>The key of the metadata.</p>"
			  +"<p><b>value:&nbsp;</b>The value of the metadata.</p>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_metadata.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Parameters
		
		JsonObject metaObject = this.getParent().getContext().getMetadata();
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				
				String propertyName = assignment.getLeftSideAsString(null);
				QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
				valuePart.addToJsonObject(propertyName, metaObject);
			
			}else {
				parser.throwParseException("metadata: Only parameters(key=value) are allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default

	}

	
	/****************************************************************************
	 * Override to make the inQueue the outQueue
	 ****************************************************************************/
	@Override
	public PipelineAction<EnhancedJsonObject, EnhancedJsonObject> setOutQueue(LinkedBlockingQueue<EnhancedJsonObject> out) {

		this.inQueue = out;
		
		if(previousAction != null) {
			previousAction.setOutQueue(out);
		}
		
		return this;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		// Do nothing, inQueue is the same as outQueue
		this.setDoneIfPreviousDone();
	
	}

}

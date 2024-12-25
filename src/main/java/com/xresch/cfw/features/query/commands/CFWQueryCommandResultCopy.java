package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandResultCopy extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "resultcopy";

	private ArrayList<QueryPart> parts;

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandResultCopy.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> resultnames = new ArrayList<>();
		
	HashSet<String> encounters = new HashSet<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCopy(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates a copy of completed results of previous queries.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <resultname> [, <resultname>, <resultname>...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>resultname:&nbsp;</b>(Optional) Names of the results to be copied. Names are set with metadata command. If none is given, all are copied.</p>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_"+COMMAND_NAME+".html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Parameters
		this.parts = parts;		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() {
		//------------------------------------------
		// Get Fieldnames
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				// unsupported
			}else if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;

				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						resultnames.add(element.getAsString());
					}
				}
			}else {
				QueryPartValue value = part.determineValue(null);
				if(!value.isNull()) {
					resultnames.add(value.getAsString());
				}
			}
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//------------------------------
		// Read Records of current Query
		while(keepPolling()) {
			outQueue.add(inQueue.poll());
		}
		
		//------------------------------
		// Read Records of Results		
		if(isPreviousDone() && inQueue.isEmpty()) {
			
			CFWQueryResultList resultsToCopy;
			
			//------------------------------
			// Get Results To Copy		
			if(resultnames.isEmpty()) {
				resultsToCopy = this.getQueryContext().getResultList();
			}else {
				resultsToCopy = new CFWQueryResultList();
				for(String name : resultnames) {
					resultsToCopy.addResult(
							this.getQueryContext().getResultByName(name)
						);
				}
			}
			
			//------------------------------
			// Copy	
			for(int i = 0; i < resultsToCopy.size(); i++) {
				
				CFWQueryResult current = resultsToCopy.get(i);
				if( current.isMetadataValueTrue(CFWQueryCommandMetadata.META_PROPERTY_TEMPLATE) ) {
					continue;
				}

				//----------------------------
				// Handle Detected Fields
				this.fieldnameAddAll(current.getDetectedFields());		
				
				//----------------------------
				// Iterate Results
				current.getRecords().forEach(new Consumer<EnhancedJsonObject>() {

					@Override
					public void accept(EnhancedJsonObject e) {
						
						outQueue.add(e.clone());
					}
				});
				
			}
			
			this.setDone();
		}
		
	}

}

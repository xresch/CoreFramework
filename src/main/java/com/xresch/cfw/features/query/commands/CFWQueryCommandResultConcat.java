package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class CFWQueryCommandResultConcat extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "resultconcat";

	private ArrayList<QueryPart> parts;

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandResultConcat.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> resultnames = new ArrayList<>();
		
	HashSet<String> encounters = new HashSet<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultConcat(CFWQuery parent) {
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
		return "Concatenates completed result of previous queries into one. Removes the original results.";
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
		return "<p><b>resultname:&nbsp;</b>(Optional) Names of the results  to be concatenated. Names are set with metadata command. If none is given, all are merged.</p>";
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
		// Read Records of current Query
		ArrayList<CFWQueryResult> concatenateddResults = new ArrayList<>();
		
		if(isPreviousDone() && inQueue.isEmpty()) {
			
			CFWQueryResultList previousResults = this.parent.getContext().getResultList();
			for(int i = 0; i < previousResults.size(); i++) {
				
				CFWQueryResult current = previousResults.get(i);
				JsonElement name = current.getMetadata().get("name");
				String nameString = (name != null && !name.isJsonNull()) ? name.getAsString() : null;
				
				//----------------------------
				// Add Result
				if(resultnames.isEmpty()
				|| (nameString != null && resultnames.contains(nameString))
				) {
					//----------------------------
					// Handle Detected Fields
					this.fieldnameAddAll(current.getDetectedFields());		
					concatenateddResults.add(current);
					
					//----------------------------
					// Iterate Results
					current.getResults().forEach(new Consumer<JsonElement>() {

						@Override
						public void accept(JsonElement e) {
							outQueue.add(
									new EnhancedJsonObject(e.getAsJsonObject())
								);
						}
					});
					
					
				}
				
			}
			
			//----------------------------
			// Remove Merged Results
			for(CFWQueryResult result : concatenateddResults) {
				 previousResults.removeResult(result);
			}
			
			this.setDone();
		}
		
	}

}

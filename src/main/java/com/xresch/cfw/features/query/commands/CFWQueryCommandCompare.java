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

public class CFWQueryCommandCompare extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandCompare.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> resultnames = new ArrayList<>();
		
	HashSet<String> encounters = new HashSet<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandCompare(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"compare"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Compares completed results of previous queries.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "compare [results=resultnamesArray]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>resultnamesArray:&nbsp;</b>(Optional) Names of the results that should be compared.</p>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_compare.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Fieldnames
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				
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
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() {
		// nothing todo
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
		ArrayList<CFWQueryResult> mergedResults = new ArrayList<>();
		
		if(isPreviousDone() && inQueue.isEmpty()) {
			
			CFWQueryResultList previousResults = this.parent.getContext().getResultList();
			
			CFWQueryResult last = previousResults.get(previousResults.size()-1);
			CFWQueryResult secondLast = previousResults.get(previousResults.size()-2);
			
			previousResults.removeResult(last);
			previousResults.removeResult(secondLast);
			
			ArrayList<String> idFields = new ArrayList<>();
			idFields.add("FIRSTNAME");
			idFields.add("LASTSTNAME");
			
			CFWQueryResult compared = 
					new CFWQueryCommandCompareMethods()
						.identifierFields(idFields)
						.compareQueryResults(secondLast, last);
						;
						
			previousResults.addResult(compared);
			
			this.setDone();
		}
		
	}

}

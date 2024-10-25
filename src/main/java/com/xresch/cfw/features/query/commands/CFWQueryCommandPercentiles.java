package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandPercentiles extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "percentiles";

	private ArrayList<String> fieldnames = new ArrayList<>();
	
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	private ArrayList<EnhancedJsonObject> objectListToSort = new ArrayList<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandPercentiles(CFWQuery parent) {
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
		return "Calculates percentile values based on the specified fields.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <targetfield>=<valuefield> [ <targetfield>=<valuefield> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>targetfield:&nbsp;</b> Name of the new field.</li>"
		 	  +"<li><b>valuefield:&nbsp;</b> Name of the field containing the values the percentiles should be calculated for.</li>"
		 	  + "</ul>"
			  ;
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
	
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);
			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
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
	public void initializeAction() throws Exception {
		
		for(QueryPartAssignment assignment : assignmentParts) {

				String newName = assignment.getLeftSideAsString(null);

				if(newName == null) {
					throw new ParseException(COMMAND_NAME+": New name cannot be null.", assignment.position());
				}
				
				if(CFW.Security.containsSequence(newName, "<", ">", "\"", "&")) {
					throw new ParseException(COMMAND_NAME+": New name cannot contain the following characters: < > \" &", assignment.position());
				}
				
				fieldnames.add(newName);

		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
	
		//-------------------------------------
		// Create Sorted List
		while(keepPolling()) {
			objectListToSort.add(inQueue.poll());
		}

		if(isPreviousDone()) {

			for(QueryPartAssignment assignment : assignmentParts) {

				//-------------------------------------
				// Sort the List List
				String fieldname = assignment.getRightSide().determineValue(null).getAsString();
				
				Comparator<EnhancedJsonObject> comparator = new Comparator<EnhancedJsonObject>() {
					@Override
					public int compare(EnhancedJsonObject o1, EnhancedJsonObject o2) {
						
						return _CFWQueryCommon.compareByFieldname(o1, o2, fieldname, false);

					}
				};
				
				objectListToSort.sort(comparator);
				
				//-------------------------------------
				// Add percentile value
				String targetField = assignment.getLeftSideAsString(null);
				int count = objectListToSort.size();
				for(int i = 0; i < count; i++  ) {

					BigDecimal percentile = new BigDecimal( (100f / count) * (i+1));
					percentile = percentile.setScale(3, RoundingMode.HALF_UP);
					objectListToSort.get(i).addProperty(targetField, percentile);
				}
			}

			//-------------------------------------
			// Push to outQueue
			for(EnhancedJsonObject object : objectListToSort) {
				
				//System.out.println("out: "+object.get(fieldnames.get(0)));
				outQueue.add(object);
			}

			this.fieldnameAddAll(fieldnames);
			this.setDone();
		}
				
	}



}

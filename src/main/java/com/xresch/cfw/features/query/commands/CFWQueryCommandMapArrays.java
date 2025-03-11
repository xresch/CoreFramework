package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandMapArrays extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "maparrays";

	private ArrayList<String> fieldnames = new ArrayList<>();
	
	private String newName;
	private boolean doReplace = false;

	private ArrayList<QueryPart> parts;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandMapArrays(CFWQuery parent) {
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
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_ARRAYS);
		tags.add(_CFWQueryCommon.TAG_OBJECTS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Maps the values of multiple arrays by index into a new array of objects.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname> [, <fieldname> ...] [name=<name>] [replace=<replace>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "</ul>"
				  +"<li><b>fieldname:&nbsp;</b> Names of the fields containing the arrays that should be mapped by index.</li>"
			 	  +"<li><b>name:&nbsp;</b> (Optional) Name of the target field. (Default: 'maparrays')</li>"
			 	  +"<li><b>replace:&nbsp;</b> (Optional) Toogle if the original fields should be replaced.(Default: false)</li>"
		 	  +"</ul>"
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
	public void initializeAction() throws Exception {
		
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				if(paramName != null) {
					
					//----------------------------------
					// Parameter name
					if(paramName.equals("name")) {
						QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
						if(paramValue.isString()) {
							this.newName = paramValue.getAsString();
						}
					}
					//----------------------------------
					// Parameter replace
					else if(paramName.equals("replace")) {
						QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
						if(paramValue.isBoolOrBoolString()) {
							this.doReplace = paramValue.getAsBoolean();
						}
					}
					
				}
				
			}else if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;

				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						fieldnames.add(element.getAsString());
					}
				}
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isJsonArray()) {
					fieldnames.addAll(value.getAsStringArray());
				}else if(!value.isNull()) {
					fieldnames.add(value.getAsString());
				}
			}
		}
		
		//----------------------------------
		// Sanitize
		if(newName == null) {
			newName = "maparrays";
		}
		
		this.fieldnameAdd(newName);
		
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
	
		//-------------------------------------
		// Create Sorted List
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			JsonArray newObjectArray = new JsonArray();
			//----------------------------
			// Iterate Fieldnames
			for(String fieldname : fieldnames) {
				JsonElement potentialArray = record.get(fieldname);
				if(potentialArray != null 
				&& record.get(fieldname).isJsonArray()) {
					//----------------------------
					// Iterate Array
					JsonArray currentArray = potentialArray.getAsJsonArray();
					for(int k = 0; k < currentArray.size(); k++ ) {
						//----------------------------
						// Get Object at current index
						// or create new one
						JsonObject targetObject;
						if(k < newObjectArray.size()) {
							targetObject = newObjectArray.get(k).getAsJsonObject();
						}else {
							targetObject = new JsonObject();
							newObjectArray.add(targetObject);
						}
						
						targetObject.add(fieldname, currentArray.get(k));
					}
				}
				
				//----------------------------
				// Replace
				if(doReplace) {
					record.remove(fieldname);
					this.fieldnameRemove(fieldname);
				}
				
			}
			
			//----------------------------
			// Add Result
			record.add(newName, newObjectArray);
			outQueue.add(record);
			
		}

		//-------------------------------------
		// Done
		this.setDoneIfPreviousDone();

			
				
	}

}

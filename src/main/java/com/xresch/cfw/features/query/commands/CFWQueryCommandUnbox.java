package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartJsonMemberAccess;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandUnbox extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandUnbox.class.getName());
	
	CFWQuerySource source = null;
	QueryPartArray unboxFields;
	
	private boolean doReplaceOriginal = true;
	
	HashSet<String> newFieldnames = new HashSet<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandUnbox(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"unbox", "unwrap"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Replaces records by unboxing one or more of their fields. Uses the value of the fields as new records.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "unbox <fieldnameOrPath> [, <fieldnameOrPath> ...] [replace=<boolean>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldnameOrPath:&nbsp;</b> Fieldnames or JSON paths that should be used as replacement.</p>"
		 	  +"<p><b>replace:&nbsp;</b> Toogle if the original fields should be replaced.(Default: true)</p>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_unbox.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		CFWQueryContext queryContext = this.parent.getContext();
		unboxFields = new QueryPartArray(queryContext);
		
		//------------------------------------------
		// Get Fieldnames
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment parameter = (QueryPartAssignment)part;
				String paramName = parameter.getLeftSide().determineValue(null).getAsString();
				if(paramName != null && paramName.equals("replace")) {
					QueryPartValue paramValue = parameter.getRightSide().determineValue(null);
					if(paramValue.isBoolOrBoolString()) {
						this.doReplaceOriginal = paramValue.getAsBoolean();
					}
				}
				
			}else if(part instanceof QueryPartArray) {

				for(QueryPart element : ((QueryPartArray)part).getAsParts()) {
					
					if(element instanceof QueryPartValue
					|| element instanceof QueryPartJsonMemberAccess) {
						unboxFields.add(element);
					}else { /* ignore */ }
					
				}
				
			}else if(part instanceof QueryPartValue
				  || part instanceof QueryPartJsonMemberAccess) {
				unboxFields.add(part);

			}else { 
				/* ignore */
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Specify the fieldnames of the fields that should be used for deduplication.<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		while(keepPolling()) {
			
			ArrayList<EnhancedJsonObject> newRecordsArray = new ArrayList<EnhancedJsonObject>();
			
			//------------------------------------
			// Get Original Record
			EnhancedJsonObject originalRecord = inQueue.poll();
			if(!doReplaceOriginal) {
				newRecordsArray.add(originalRecord);
			}else {
				this.fieldnameClearAll();
				newRecordsArray.add(new EnhancedJsonObject());
			}
			
			//------------------------------------
			// Unbox into New Record
			
			// if array >> add new records
			// if other add value by name
			
			
			
			for(QueryPart part : unboxFields.getAsParts()) {
				
				//------------------------------------
				// Handle Single Fieldname
				if(part instanceof QueryPartValue) {
					String fieldname = ((QueryPartValue)part).getAsString();
					JsonElement newRecordValue = originalRecord.get(fieldname);
					addJsonElementToRecords(fieldname, newRecordValue, newRecordsArray);
					
				//------------------------------------
				// Handle JSON Member Access
				}else if(part instanceof QueryPartJsonMemberAccess) {
					QueryPartJsonMemberAccess access = (QueryPartJsonMemberAccess)part;
					QueryPartValue newRecordValue = access.determineValue(originalRecord);
					
					String fieldname = access.determineValue(null).getAsString();
					fieldname = fieldname.substring(fieldname.lastIndexOf(".")+1);
					addJsonElementToRecords(fieldname, newRecordValue.getAsJsonElement(), newRecordsArray);
				}else { 
					/*ignore*/ 
				}
			}
			
			//------------------------------------
			// Write New Records
			boolean isFirst = true;
			for(EnhancedJsonObject record : newRecordsArray) {
				if(isFirst) {
					this.fieldnameAddAll(record);
					isFirst = false;
				}
				outQueue.add(record);
			}
		}
		

		
		this.setDoneIfPreviousDone();
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private void addJsonElementToRecords(String fieldname, JsonElement newRecordValue, ArrayList<EnhancedJsonObject> recordsArray) {
		
		if(newRecordValue == null) {
			return;
		}
		
		if(newRecordValue.isJsonObject()) {
			
			for(EnhancedJsonObject existingRecord : recordsArray) {
				existingRecord.addAll(newRecordValue.getAsJsonObject());
			}
			
		}else if(newRecordValue.isJsonArray()) {
			
			ArrayList<EnhancedJsonObject> recordsCreatedForArray = new ArrayList<>();
			
			for(EnhancedJsonObject existingRecord : recordsArray) {
				
				for(JsonElement entry : newRecordValue.getAsJsonArray()) {
					
					EnhancedJsonObject recordForEntry = new EnhancedJsonObject();
					recordForEntry.addAll(existingRecord);
					recordsCreatedForArray.add(recordForEntry);
					
					recordForEntry.add(fieldname, entry);
					
				}
			}
			recordsArray.clear();
			recordsArray.addAll(recordsCreatedForArray);
		}else {
			
			for(EnhancedJsonObject existingRecord : recordsArray) {
				existingRecord.add(fieldname, newRecordValue);
			}

		}

	}
	



}

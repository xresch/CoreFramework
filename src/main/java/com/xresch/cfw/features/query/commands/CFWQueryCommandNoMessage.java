package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandNoMessage extends CFWQueryCommand {
	
	ArrayList<String> messageTypesLowerCase = new ArrayList<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandNoMessage(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"nomessage"};
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Removes messages that have already been generated by previous commands.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "nomessage <messageType>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		
		
		return "<p><b>messageType:&nbsp;</b>Message type to be removed: "+CFW.JSON.toJSON(MessageType.values())+" </p>"
			  +"</p>"
				; 
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_nomessage.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Fieldnames
		
		if(parts.size() == 0) {
			throw new ParseException("nomessage: please specify at least one message type.", -1);
		}
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;

				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						messageTypesLowerCase.add(element.getAsString().toLowerCase());
					}
				}
			}else {
				QueryPartValue value = part.determineValue(null);
				if(!value.isNull()) {
					messageTypesLowerCase.add(value.getAsString().toLowerCase());
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
	public void execute(PipelineActionContext context) throws Exception {
				
		// wait until done to only remove messages that have been created by previous commands
		if(isPreviousDone()) {
			
			//------------------------------------
			// Handle messages
			LinkedHashMap<String, CFWHTMLItemAlertMessage> alertMap = CFW.Context.Request.getAlertMap();
			
			for(Entry<String, CFWHTMLItemAlertMessage> entry : alertMap.entrySet()) {
				MessageType type = entry.getValue().getType();
				
				if(messageTypesLowerCase.contains(type.toString().toLowerCase())) {
					alertMap.remove(entry.getKey());
				}
				
			}
			
			//------------------------------------
			// Push records forward in pipeline
			while(keepPolling()) {			
				outQueue.add(inQueue.poll());
			}
			this.setDone();
		}
		
		
	
	}

}

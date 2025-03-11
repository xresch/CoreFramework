package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
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
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandMessage extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "message";

	public HashMap<MessageType, String> messageMap = new HashMap<>();
	
	private ArrayList<ArrayList<QueryPart>> conditions = new ArrayList<>();
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	public String separator = " / ";
	public String mode = "first";
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandMessage(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
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
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates messages for the user based on conditions. Checks every record but will only add one message per type";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+"mode=<mode> separator=<separator> [<condition>, <type>, <message>] ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>mode:&nbsp;</b>The mode the messages should be generated, one of:"
					+"<ul>"
						+"<li><b>first:&nbsp;</b> (Default) Only take the first message for every message type, ignore any other message.</li>"
						+"<li><b>all:&nbsp;</b> Concatenate all messages of the same type..</li>"
					+"</ul>"
			  + "</li>"
			  +"<li><b>separator:&nbsp;</b>Separator put between contactenated messages.(default: ' / ')</li>"
			  +"<li><b>condition:&nbsp;</b>The condition to be true for the message to be thrown.</li>"
			  +"<li><b>type:&nbsp;</b>The type of the message, one of"
					  +"<ul>"
						+"<li><b>INFO:&nbsp;</b> Message will be displayed in blue.</li>"
						+"<li><b>SUCCESS:&nbsp;</b> Message will be displayed in green.</li>"
						+"<li><b>WARNING:&nbsp;</b> Message will be displayed in orange.</li>"
						+"<li><b>ERROR:&nbsp;</b> Message will be displayed in red.</li>"
					+"</ul>"
			  + "</li>"
			  +"<li><b>message:&nbsp;</b>The text of the message</li>"
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

		//------------------------------------------
		// Get Parameters
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartArray) {
				ArrayList<QueryPart> conditionDefinition = ((QueryPartArray)currentPart).getAsParts();
				if(conditionDefinition.size() > 0) {
					conditions.add(conditionDefinition);
				}
				
			}else if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);
			} else {
			
				parser.throwParseException(COMMAND_NAME+": Only array and assignment expressions allowed.", currentPart);
			}
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		//--------------------------------
		// 5 Color Templates
		AutocompleteList list = new AutocompleteList();
		result.addList(list);
		
		list.addItem(
			helper.createAutocompleteItem("", 
				  "	mode='all'\r\n" + 
				  "	separator=\" --- \"\r\n" + 
				  "	[(VALUE > 98), \"ERROR\", \"Value is: \"+VALUE]\r\n" + 
				  "	[(VALUE > 96), \"WARNING\", \"Value is: \"+VALUE]\r\n" + 
				  "	[(VALUE > 94), \"INFO\", \"Value is: \"+VALUE]\r\n" + 
				  "	[(VALUE > 80), \"SUCCESS\", \"Value is: \"+VALUE]"
				, "Message Template"
				, "A template to get you started."
				)
		);
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String propertyName = assignment.getLeftSideAsString(null);

			QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
			
			propertyName = propertyName.trim().toLowerCase();
			if     (propertyName.equals("mode") && valuePart.isString() ) { mode = valuePart.getAsString().trim().toLowerCase(); } 
			else if(propertyName.equals("separator") && valuePart.isString() ) { separator = valuePart.getAsString(); }
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			if(conditions == null || conditions.size() == 0) {
				outQueue.add(record);
			}else {
				
				for(ArrayList<QueryPart> conditionDefinition : conditions) {

					QueryPart condition = conditionDefinition.get(0);
					QueryPartValue evalResult = condition.determineValue(record);

					if(evalResult.isBoolOrBoolString()) {

						if(evalResult.getAsBoolean()) {

							//-------------------------------
							// Get Type
							String typeString = "";
							if(conditionDefinition.size() > 1) { 
								typeString = conditionDefinition.get(1).determineValue(record).getAsString();
							}
							
							if(typeString == null) {
								continue;
							}
							
							typeString = typeString.trim().toUpperCase();
							MessageType type = MessageType.INFO;
							if(MessageType.hasMessageType(typeString)) {
								type = MessageType.valueOf(typeString);
							}
							
							if(mode.equals("first") && messageMap.containsKey(type)) {
								// only send one message for each type
								continue;
							}
							
							//-------------------------------
							// Get Message
							String message = null;
							if(conditionDefinition.size() > 2) { 
								message = conditionDefinition.get(2).determineValue(record).getAsString();
							}
							
							if(message != null) {
								String existingMessage = messageMap.get(type);
								
								message = (existingMessage == null) ? message : existingMessage+separator+message;
								messageMap.put(type, message);
							}
							
							break;
						}
					}
				}
				
				outQueue.add(record);
			}
		
		}
		
		if(this.isPreviousDone() && this.inQueue.isEmpty()) {
			
			for(Entry<MessageType, String> entry : messageMap.entrySet()) {
				CFW.Messages.addMessage(entry.getKey(), entry.getValue());
			}
			this.setDone();
		}
		
	
	}

}

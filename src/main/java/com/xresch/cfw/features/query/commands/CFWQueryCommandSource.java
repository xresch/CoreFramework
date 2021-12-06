package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandSource extends CFWQueryCommand {

	CFWQuerySource source = null;
	CFWObject paramsForSource = null;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandSource(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "source";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String shortDescription() {
		return "Choose the source to read the data from.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String syntax() {
		return "source <sourcename> [param1=xxx param2=xyz ...]";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Name
		QueryPart namePart = parts.get(0);
		QueryPartValue nameValue = namePart.determineValue();
		
		if( nameValue.isNull() || !nameValue.isString()) {
			parser.throwParseException("source: expected source name.", namePart);
		}
		
		String sourceName = nameValue.getAsString().trim();
		
		//------------------------------------------
		// Get Source
		
		if(!CFW.Registry.Query.sourceExists(sourceName)) {
			parser.throwParseException("source: the source does not exist: '"+sourceName+"'", namePart);
		}
		
		this.source = CFW.Registry.Query.createSourceInstance(sourceName);
		
		//------------------------------------------
		// Get Parameters
		
		JsonObject parameters = new JsonObject();
		
		for(int i = 1; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String paramName = assignment.getLeftSide();
				
				QueryPartValue paramValue = namePart.determineValue();
				
				switch(paramValue.type()) {
					case STRING:	parameters.addProperty(paramName, paramValue.getAsString());
									break;
									
					case NUMBER:	parameters.addProperty(paramName, paramValue.getAsNumber());
									break;
				
					case BOOLEAN:	parameters.addProperty(paramName, paramValue.getAsBoolean());
									break;
									
					case JSON:		parameters.add(paramName, paramValue.getAsJson());
									break;
	
					default:		break;
				
				}
				
			}else {
				parser.throwParseException("source: only parameters(key=value) are allowed'"+sourceName+"'", currentPart);
			}
		}
			
		//------------------------------------------
		// Map to Parameters Object
		this.paramsForSource = source.getParameters();
		if(!paramsForSource.mapJsonFields(parameters)) {
			
			for(CFWField field : paramsForSource.getFields().values()) {
				ArrayList<String> invalidMessages = field.getInvalidationMessages();
				if(!invalidMessages.isEmpty()) {
					throw new ParseException(invalidMessages.get(0), -1);
				}
			}
			
			throw new ParseException("Unknown error for source command '"+this.uniqueName()+"'", -1);
		}

		
		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		this.source.execute(paramsForSource, outQueue);

		this.setDoneIfPreviousDone();
	}

}

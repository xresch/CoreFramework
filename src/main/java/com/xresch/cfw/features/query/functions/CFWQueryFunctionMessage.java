package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionMessage extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "message";
	
	// Contains unique message
	public HashSet<String> messageSet = new HashSet<>();
	
	public CFWQueryFunctionMessage(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return FUNCTION_NAME;
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
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(message, type)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Adds a message to the output.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
					+"<li><b>message:&nbsp;</b>The value for the message.</li>"
					+"<li><b>type:&nbsp;</b>The type of the message, one of:"
					   +"<ul>"
						+"<li><b>INFO:&nbsp;</b> Message will be displayed in blue.</li>"
						+"<li><b>SUCCESS:&nbsp;</b> Message will be displayed in green.</li>"
						+"<li><b>WARNING:&nbsp;</b> Message will be displayed in orange.</li>"
						+"<li><b>ERROR:&nbsp;</b> Message will be displayed in red.</li>"
					   +"</ul>"
					+"</li>"
				+"</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_"+FUNCTION_NAME+".html");
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean supportsAggregation() {
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		int paramCount = parameters.size();
		if(paramCount < 1) {
			return QueryPartValue.newNull();
		}

		//-------------------------------
		// Get Type
		MessageType type = MessageType.INFO;
		if(paramCount > 1) {
			String typeString = parameters.get(1).getAsString();
			
			if(typeString != null) {
				typeString = typeString.trim().toUpperCase();
				
				if(MessageType.hasMessageType(typeString)) {
					type = MessageType.valueOf(typeString);
				}
			}

		}
		
		//-------------------------------
		// Get Message
		QueryPartValue messagePart = parameters.get(0);
		String message = parameters.get(0).getAsString();

		if(message == null) {
			return QueryPartValue.newNull();
		}

		if(message != null) {
			String messageID = "" + type + message;
			
			if( !messageSet.contains(messageID) ){
				messageSet.add(messageID);
				message = CFW.Security.escapeHTMLEntities(message);
				CFW.Messages.addMessage(type, message);
				return messagePart;
			}
		}
		
		return QueryPartValue.newNull();
	}

}

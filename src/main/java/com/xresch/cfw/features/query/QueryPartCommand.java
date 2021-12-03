package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
/**************************************************************************************************************
 * 
 * <QueryPartCommand> ::= <commandName> (<LITERAL>|<KEYWORD>|<EXPRESSION>)+
 * 
 * 		<commandName> ::= <LITERAL_STRING>
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartCommand implements Evaluatable, Parseable{
	
	ArrayList<Evaluatable> commandList = new ArrayList<Evaluatable>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue evaluate(ArrayList<LinkedHashMap<String, Object>> objectlist, Object... params) {

		return null;
		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void parse(CFWQueryParserContext context) throws ParseException {
		
		//------------------------------------
		// Check Has More Tokens
		if(!context.hasMoreTokens()) {
			context.throwParseException("Query cannot end with a pipe symbol", 0);
		}
		
		//------------------------------------
		// Expect LITERAL_STRING
		// Command Name

		if(context.lookahead().isString()) {
			
			//------------------------------------
			// Get Command
			CFWQueryToken token = context.consumeToken();
			
			while(context.hasMoreTokens()) {
				
				QueryPartCommand command = new QueryPartCommand();
				commandList.add(command);
				
				command.parse(context);
			}
		}else {
			context.throwParseException("Expected command name.", 0);
		}
		
	
		
	}


}

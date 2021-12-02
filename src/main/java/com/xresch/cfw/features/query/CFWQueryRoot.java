package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
/**************************************************************************************************************
 * 
 * <CFWQuery> ::= <CFWCommand>+ 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryRoot implements Parseable{
	
	ArrayList<QueryPartCommand> commandList = new ArrayList<>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void parse(CFWQueryParserContext context) throws ParseException {
		
		//<commandName> (<LITERAL>|<KEYWORD>|<EXPRESSION>)+		
		
		while(context.hasMoreTokens()) {
			CFWQueryToken token = context.consumeToken();
			
			QueryPartCommand command = new QueryPartCommand();
			commandList.add(command);
			
			command.parse(context);
		}
		
	}


}

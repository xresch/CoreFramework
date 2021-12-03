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
public class CFWQuery {
	
	ArrayList<CFWQueryCommand> commandList = new ArrayList<>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public void addCommand(CFWQueryCommand command){
		
		commandList.add(command);
		
	}


}

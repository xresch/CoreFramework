package com.xresch.cfw.features.query;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw.pipeline.Pipeline;
/**************************************************************************************************************
 * 
 * <CFWQuery> ::= <CFWCommand>+ 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuery extends Pipeline<JsonObject, JsonObject>{
	
	ArrayList<CFWQueryCommand> commandList = new ArrayList<>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public void addCommand(CFWQueryCommand command){
		
		commandList.add(command);
		
	}


}

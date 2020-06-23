package com.pengtoolbox.cfw.cli;

import java.util.ArrayList;


/**************************************************************************************************************
 *  This exception is thrown when arguments could not be read correctly.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/

public class ArgumentsException extends Exception {

	private static final long serialVersionUID = 1L;
	private String argument = "";
	private ArrayList<String> messageArray = new ArrayList<String>();

	//####################################################################################
	// CONSTRUCTORS
	//####################################################################################
	
	public ArgumentsException(String... messages) {
		
		for(String message : messages){
			this.messageArray.add(message);
		}
	}

	public ArgumentsException(ArrayList<String> stringArray) {

		this.messageArray.addAll(stringArray);
	}
	
	//####################################################################################
	// GETTERS & SETTERS
	//####################################################################################
	
	@Override
	public String getMessage(){
		
		if(messageArray.size() == 1){
			return messageArray.get(0);
		}else{
		
			StringBuilder sb = new StringBuilder();
			sb.append("the following issues with arguments were detected:\n");
			
			for(String message : messageArray){
				sb.append(message);
				sb.append("\n");
			}
			
			return sb.toString();
		}
		
	}
	public String getArgument() {
		return argument;
	}

	public void setArgument(String argument) {
		this.argument = argument;
	}

}
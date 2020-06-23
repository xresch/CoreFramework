package com.pengtoolbox.cfw.validation;

import java.util.ArrayList;

/**************************************************************************************************************
 * The ValidatorEngine is used to do run multiple validators at once.
 * The validators are added to the validator engine and can be exuted 
 * all at once or filtered by their corresponding tags.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ValidationEngine {
	
	private ArrayList<IValidator> validatorArray = new ArrayList<IValidator>();
	private String invalidMessages = "";
	
	//####################################################################################
	// CONSTRUCTORS
	//####################################################################################
	public ValidationEngine(){
		
	}
	
	//####################################################################################
	// CLASS METHODS
	//####################################################################################
	/*************************************************************************
	 * Executes all validators added to the engine.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validateAll(){
		
		boolean isValid = true;
		invalidMessages = "";
		StringBuffer messages = new StringBuffer();
		
		for(IValidator validator : validatorArray){
			
			if(!validator.validate()){
				messages.append("- ");
				messages.append(validator.getInvalidMessage());
				messages.append("\n");
				
				isValid=false;
			}
		}
		
		if(!isValid){
			invalidMessages = messages.toString();
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Executes all validators with a tag matching the given regular
	 * expression.
	 * 
	 * @param regex a regular expression
	 * @return true if all executed validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validateByTags(String regex){
		
		boolean isValid = true;
		invalidMessages = "";
		StringBuffer messages = new StringBuffer();
		
		for(IValidator validator : validatorArray){
			
			if(validator.getTag().matches(regex)){
				if(!validator.validate()){
					messages.append("- ");
					messages.append(validator.getInvalidMessage());
					messages.append("\n");
					
					isValid=false;
				}
			}
		}
		
		if(!isValid){
			invalidMessages = messages.toString();
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Adds the vaildator to the engine.
	 * 
	 * @param validator the validator to add
	 *************************************************************************/ 
	public void addValidator(IValidator validator){
		validatorArray.add(validator);
	}
	
	/*************************************************************************
	 * Removes the validator from the engine.
	 * 
	 * @param validator the validator to add.
	 *************************************************************************/ 
	public void removeValidator(IValidator validator){
		validatorArray.remove(validator);
	}

	//####################################################################################
	// GETTERS & SETTERS
	//####################################################################################
	/*************************************************************************
	 * Returns all the InvalidMessages from the last validation execution. 
	 *************************************************************************/ 
	public String getInvalidMessages() {
		return invalidMessages;
	}
	
	

}

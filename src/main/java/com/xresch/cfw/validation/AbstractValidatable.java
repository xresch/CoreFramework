package com.xresch.cfw.validation;

import java.util.ArrayList;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class AbstractValidatable<T> implements IValidatable<T> {
	
	
	private ArrayList<IValidator> validatorArray = new ArrayList<IValidator>();
	private String label = "";
	protected T value;
	
	private ArrayList<String> invalidMessages;
	
	/*************************************************************************
	 * Executes all validators added to this instance and validates the current
	 * value.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validate(){
		
		boolean isValid = true;
		invalidMessages = new ArrayList<String>();
		
		for(IValidator validator : validatorArray){
			
			if(!validator.validate(value)){
				invalidMessages.add(validator.getInvalidMessage());
				isValid=false;
			}
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Executes all validators added to the instance of this class.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validateValue(Object value){
		
		boolean isValid = true;
		invalidMessages = new ArrayList<String>();
		
		for(IValidator validator : validatorArray){
			
			if(!validator.validate(value)){
				invalidMessages.add(validator.getInvalidMessage());
				
				isValid=false;
			}
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Returns all the InvalidMessages from the last validation execution. 
	 *************************************************************************/ 
	public ArrayList<String> getInvalidMessages() {
		return invalidMessages;
	}
	
	public IValidatable<T> addValidator(IValidator validator) {
		if(!validatorArray.contains(validator)) {
			validatorArray.add(validator);
		}
		
		return this;
	}

	public boolean removeValidator(IValidator o) {
		return validatorArray.remove(o);
	}
	
	public IValidatable<T> setLabel(String propertyName) {
		this.label = propertyName;
		return this;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean setValueValidated(T value) {
		if(this.validateValue(value)) {
			this.value = value;
			return true;
		}
		return false;
	}
	
	public T getValue() {
		return value;
	}
	
}

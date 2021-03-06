package com.xresch.cfw.cli;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.validation.AbstractValidatable;

/**************************************************************************************************************
 * The ArgumentDefinition represents an argument with a key value pair.
 * It contains the default value, syntax and a description of the argument.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ArgumentDefinition extends AbstractValidatable<String> {
	
	private String syntax = "";
	private String defaultValue = null;
	private String description = "";
	private boolean isVMArgument = false;
	
	//####################################################################################
	// CONSTRUCTORS
	//####################################################################################
	public ArgumentDefinition(String propertyName){
		
		this.setLabel(propertyName);
		
	}
	
	public ArgumentDefinition(String propertyName, String syntax, String defaultValue, String description, boolean isVMArgument){
		
		this.setLabel(propertyName);
		
		this.syntax = syntax;
		this.defaultValue = defaultValue;
		this.description = description;
		this.isVMArgument = isVMArgument;
	}
	
	
	//####################################################################################
	// GETTERS & SETTERS
	//####################################################################################
	
	
	public ArgumentDefinition syntax(String syntax) {
		this.syntax = syntax;
		return this;
	}
	
	public String getSyntax() {
		return syntax;
	}
	
	public ArgumentDefinition defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public String getValue() {
		if(!CFW.Validation.isNullOrEmptyString(super.getValue())) {
			return super.getValue();
		}else {
			return this.getDefaultValue();
		}
	}

	public ArgumentDefinition description(String description) {
		this.description = description;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isVMArgument() {
		return isVMArgument;
	}
	
	
}

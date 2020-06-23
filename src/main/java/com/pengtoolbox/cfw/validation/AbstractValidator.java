package com.pengtoolbox.cfw.validation;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.utils.Ternary;


/**************************************************************************************************************
 * The AbstractArgumentValidator provides some default implementation of the 
 * methods defined by the IArgumentValidator interface.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class AbstractValidator implements IValidator {

	protected IValidatable<?> validateable;
	private String invalidMessage="";
	private String tag="";
	private boolean isNullAllowed = true;
	
	public AbstractValidator(IValidatable<?> validatable){
		this.validateable = validatable;
		validatable.addValidator(this);
	}
	
	public AbstractValidator(){
	}
	
	
	@Override
	public IValidator setValidateable(IValidatable<?> argument) {
		this.validateable = argument;
		return this;
	}

	@Override
	public IValidatable<?> getValidatable() {
		return validateable;
	}
	
	@Override
	public String getInvalidMessage(){
		return invalidMessage;
	}
	
	@Override
	public IValidator setInvalidMessage(String message){
		this.invalidMessage = message;
		return this;
	}
		
	@Override
	public boolean validate() {
		IValidatable<?> validatable = this.getValidatable();
		if(validatable != null) {
			return validate(validatable.getValue());
		}else {
			return false;
		}
	}

	public String getTag() {
		return tag;
	}

	public IValidator setTag(String tag) {
		this.tag = tag;
		return this;
	}

	public boolean isNullAllowed() {
		return isNullAllowed;
	}

	public IValidator setNullAllowed(boolean isNullAllowed) {
		this.isNullAllowed = isNullAllowed;
		return this;
	}

	public Ternary validateNullEmptyAllowed(Object value) {
		
		if(this.isNullAllowed() && CFW.Validation.isNullOrEmptyString(value)) {
			return Ternary.TRUE;
		}else if(CFW.Validation.isNullOrEmptyString(value)) {
			this.setInvalidMessage("The value of "+validateable.getName()+" should not be null or empty string.");
			return Ternary.FALSE;
		}
		return Ternary.DONTCARE;
	}
	
	
	
	

}

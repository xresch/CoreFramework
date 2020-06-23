package com.pengtoolbox.cfw.validation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class NotNullOrEmptyValidator extends AbstractValidator {

	public NotNullOrEmptyValidator(IValidatable<?> validatable) {
		super(validatable);
		// TODO Auto-generated constructor stub
	}
	
	public NotNullOrEmptyValidator() {
	}

	@Override
	public boolean validate(Object value) {
		
		if(value != null && !value.equals("")){
			return true;
		}else{
			this.setInvalidMessage("The field "+validateable.getName()+" cannot be empty.");
			return false;
		}
		
	}

}

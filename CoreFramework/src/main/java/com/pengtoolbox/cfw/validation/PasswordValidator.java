package com.pengtoolbox.cfw.validation;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PasswordValidator extends AbstractValidator {
	
	public PasswordValidator(IValidatable<?> validatable){
		super(validatable);
	}
	
	public PasswordValidator(){}
	
	@Override
	public boolean validate(Object value) {
		
		if( !(value.toString().length() >= 8) ) {
			this.setInvalidMessage("The value of "+validateable.getName()+
					" must at least be 8 characters long.");
			return false;
		}
		
		if(   value.toString().matches(".*[A-Z]+.*")
		   && value.toString().matches(".*[a-z]+.*")
		   && value.toString().matches(".*[^A-Za-z]+.*") ){
			return true;
		}else{
			this.setInvalidMessage("The value of "+validateable.getName()+
			" must contain at least one uppercase, one lowercase and one special character or number.");
			
			return false;
		}
	}


}

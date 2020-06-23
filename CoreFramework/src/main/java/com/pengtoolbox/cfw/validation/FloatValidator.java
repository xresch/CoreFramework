package com.pengtoolbox.cfw.validation;

import com.pengtoolbox.cfw.utils.Ternary;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FloatValidator extends AbstractValidator {

	
	public FloatValidator(IValidatable<?> validateable) {
		super(validateable);
		// TODO Auto-generated constructor stub
	}
	
	public FloatValidator() {}

	@Override
	public boolean validate(Object value) {
		
		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		if(value instanceof Integer) {
			return true;
		}
		if(value instanceof String) {

			try {
				Float.parseFloat((String)value);
				return true;
			}catch(NumberFormatException e){
				this.setInvalidMessage("The value of "+validateable.getName()+" is not an Integer value.(value='"+value+"')");
				return false;
			}

		}
		this.setInvalidMessage("The value of "+validateable.getName()+" is not an Integer value.(value='"+value+"')");
		return false;
	}
	
}

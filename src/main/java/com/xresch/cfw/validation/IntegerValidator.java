package com.xresch.cfw.validation;

import com.xresch.cfw.utils.Ternary;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class IntegerValidator extends AbstractValidator {

	
	public IntegerValidator(IValidatable<?> validateable) {
		super(validateable);
		// TODO Auto-generated constructor stub
	}
	
	public IntegerValidator() {}

	@Override
	public boolean validate(Object value) {
		
		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		if(value instanceof Integer) {
			return true;
		}
		if(value instanceof String) {

			try {
				Integer.parseInt((String)value);
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

package com.xresch.cfw.validation;

import com.xresch.cfw.utils.Ternary;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DoubleValidator extends AbstractValidator {

	
	public DoubleValidator(IValidatable<?> validateable) {
		super(validateable);
		// TODO Auto-generated constructor stub
	}
	
	public DoubleValidator() {}

	@Override
	public boolean validate(Object value) {
		
		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		if(value instanceof Integer) {
			return true;
		}
		if(value instanceof String) {

			try {
				Double.parseDouble((String)value);
				return true;
			}catch(NumberFormatException e){
				this.setInvalidMessage("The value of "+validateable.getLabel()+" is not a decimal number.(value='"+value+"')");
				return false;
			}

		}
		this.setInvalidMessage("The value of "+validateable.getLabel()+" is not a decimal number.(value='"+value+"')");
		return false;
	}
	
}

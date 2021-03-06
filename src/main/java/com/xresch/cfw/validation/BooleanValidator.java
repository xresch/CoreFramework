package com.xresch.cfw.validation;

import com.xresch.cfw.utils.Ternary;

/**************************************************************************************************************
 * The BooleanArgumentValidator will validate if the value of the ArgumentDefinition
 * is a string representation of "true" or "false".
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class BooleanValidator extends AbstractValidator {

	
	public BooleanValidator(IValidatable<?> validateable) {
		super(validateable);
		// TODO Auto-generated constructor stub
	}
	
	public BooleanValidator() {}

	@Override
	public boolean validate(Object value) {
		
		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		if(value instanceof Boolean) {
			return true;
		}
		if(value instanceof String) {

			if(((String)value).trim().toLowerCase().matches("true|false")){
				return true;
			}else{
				this.setInvalidMessage("The value of "+validateable.getLabel()+" is not a boolean value.(value='"+value+"')");
				return false;
			}
		}
		this.setInvalidMessage("The value of "+validateable.getLabel()+" is not a boolean value.(value='"+value+"')");
		return false;
	}
	
}

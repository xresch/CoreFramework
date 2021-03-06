package com.xresch.cfw.validation;
import java.sql.Date;
import java.sql.Timestamp;

import com.xresch.cfw.utils.Ternary;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class EpochOrTimeValidator extends AbstractValidator {

	
	public EpochOrTimeValidator(IValidatable<?> validateable) {
		super(validateable);
		// TODO Auto-generated constructor stub
	}
	
	public EpochOrTimeValidator() {}

	@Override
	public boolean validate(Object value) {
		
		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		if(value instanceof Date
		|| value instanceof Timestamp) {
			return true;
		}
		
		if(value instanceof Long) {
			return true;
		}
		
		if(value instanceof String) {

			try {
				Long.parseLong((String)value);
				return true;
			}catch(NumberFormatException e){
				this.setInvalidMessage("The value of "+validateable.getLabel()+" is not an Integer value.(value='"+value+"')");
				return false;
			}

		}
		
		this.setInvalidMessage("The value of "+validateable.getLabel()+" is not an Integer value.(value='"+value+"')");
		return false;
	}
	
}

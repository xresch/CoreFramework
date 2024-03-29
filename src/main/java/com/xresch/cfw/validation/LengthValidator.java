package com.xresch.cfw.validation;

import com.xresch.cfw.utils.Ternary;


/**************************************************************************************************************
 * The StringLengthArgumentValidator will validate if the value of the ArgumentDefinition
 * has a certain length in a minimum and maximum range(set -1 for unlimited).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class LengthValidator extends AbstractValidator {

	private int minLength;
	private int maxLength;

	public LengthValidator(IValidatable<?> validatable, int minLength, int maxLength) {
		super(validatable);
		this.minLength = minLength;
		this.maxLength = maxLength;
		
		if(minLength > 0) {
			this.setNullAllowed(false);
		}
	}
	
	public LengthValidator(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
		
		if(minLength > 0) {
			this.setNullAllowed(false);
		}
	}
	
	@Override
	public boolean validate(Object value) {

		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		String string = "";
		if (value instanceof String) {
			string = ((String)value);
		}else if (value instanceof Number) {
			string = ((Number)value).toString();
		}
		
		int length = string.length();
		if(   (string.length() >= minLength || minLength == -1) 
		   && (string.length() <= maxLength || maxLength == -1) ){
			return true;
		}else{
			if(minLength == -1){
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" should be at maximum "+maxLength+" characters long.(length='"+length+"')");
			}else if(maxLength == -1){
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" should be at least "+minLength+" characters long.(length='"+length+"')");
			}else {
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" should be between "+minLength+" and "+maxLength+" characters long.(length='"+length+"')");
			}
			
			return false;
		}
		
	}

}

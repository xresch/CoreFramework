package com.pengtoolbox.cfw.validation;

import com.pengtoolbox.cfw.utils.Ternary;


/**************************************************************************************************************
 * The StringLengthArgumentValidator will validate if the value of the ArgumentDefinition
 * has a certain lenght in a minimum and maximum range.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class NumberRangeValidator extends AbstractValidator {

	private int minValue;
	private int maxValue;

	public NumberRangeValidator(IValidatable<?> validatable, int minValue, int maxValue) {
		super(validatable);
		this.minValue = minValue;
		this.maxValue = maxValue;
		
		if(minValue > 0) {
			this.setNullAllowed(false);
		}
	}
	
	public NumberRangeValidator(int minValue, int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		
		if(minValue > 0) {
			this.setNullAllowed(false);
		}
	}
	
	@Override
	public boolean validate(Object value) {

		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		Double number = 0d;
		if (value instanceof String) {
			number = Double.parseDouble((String)value);
		}else if (value instanceof Number) {
			number = ((Number)value).doubleValue();
		}
		
		if(   (number >= minValue || minValue == -1) 
		   && (number <= maxValue || maxValue == -1) ){
			return true;
		}else{
			if(minValue == -1){
				this.setInvalidMessage("The value of "+validateable.getName()+
						" can have a maximum value of "+maxValue+".");
			}else if(maxValue == -1){
				this.setInvalidMessage("The value of "+validateable.getName()+
						" should be at least "+minValue+".");
			}else {
				this.setInvalidMessage("The value of "+validateable.getName()+
						" should be between "+minValue+" and "+maxValue+".");
			}
			
			return false;
		}
		
	}

}

package com.xresch.cfw.validation;

import java.math.BigDecimal;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.Ternary;


/**************************************************************************************************************
 * The StringLengthArgumentValidator will validate if the value of the ArgumentDefinition
 * has a certain lenght in a minimum and maximum range.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class NumberRangeValidator extends AbstractValidator {

	private BigDecimal minValue;
	private BigDecimal maxValue;

	public NumberRangeValidator(IValidatable<?> validatable, Number minValue, Number maxValue) {
		super(validatable);
		this.minValue = new BigDecimal(minValue.doubleValue()).stripTrailingZeros();
		this.maxValue = new BigDecimal(maxValue.doubleValue()).stripTrailingZeros();
		
		if(minValue.doubleValue() > 0) {
			this.setNullAllowed(false);
		}
	}
	
	public NumberRangeValidator(int minValue, int maxValue) {
		this.minValue = new BigDecimal(minValue).stripTrailingZeros();
		this.maxValue = new BigDecimal(maxValue).stripTrailingZeros();
		
		if(minValue > 0) {
			this.setNullAllowed(false);
		}
	}
	public NumberRangeValidator(Number minValue, Number maxValue) {
		this.minValue = new BigDecimal(minValue.doubleValue()).stripTrailingZeros();
		this.maxValue = new BigDecimal(maxValue.doubleValue()).stripTrailingZeros();
		
		if(minValue.doubleValue() > 0) {
			this.setNullAllowed(false);
		}
	}
	
	@Override
	public boolean validate(Object value) {

		Ternary result = validateNullEmptyAllowed(value);
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		//--------------------------------------
		// Get Value to Compare
		
		
		BigDecimal number = BigDecimal.ZERO;
		if(value instanceof BigDecimal) { 		number = (BigDecimal)value; }
		else if (value instanceof String) {		number = new BigDecimal((String)value); }
		else if (value instanceof Integer) { number = new BigDecimal((Integer)value);}
		else if (value instanceof Long) { number = new BigDecimal((Long)value);}
		else if (value instanceof Number) { number = new BigDecimal( ((Number)value).doubleValue() ); }
				
		if(   (number.compareTo(minValue) >= 0 || minValue.compareTo(CFW.Math.BIGDEC_NEG_ONE) == 0) 
		   && (number.compareTo(maxValue) <= 0 || maxValue.compareTo(CFW.Math.BIGDEC_NEG_ONE) == 0) ){
			return true;
		}else{
			if(minValue.compareTo(CFW.Math.BIGDEC_NEG_ONE) == 0){
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" can have a maximum value of "+maxValue+".");
			}else if(maxValue.compareTo(CFW.Math.BIGDEC_NEG_ONE) == 0){
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" should be at least "+minValue+".");
			}else {
				this.setInvalidMessage("The value of "+validateable.getLabel()+
						" should be between "
							+String.format("%.2f", minValue.doubleValue())
						+" and "
							+String.format( "%.2f", maxValue.doubleValue() )
						+".(value = '"+String.format( "%.2f", number.doubleValue() )+"')");
			}
			
			return false;
		}
		
	}

}

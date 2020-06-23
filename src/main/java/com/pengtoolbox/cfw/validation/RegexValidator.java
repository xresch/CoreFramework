package com.pengtoolbox.cfw.validation;

/**************************************************************************************************************
 * The RegexValidator will validate if value.toString() is matching the given 
 * regular expression.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class RegexValidator extends AbstractValidator {

	private String pattern="";
	
	public RegexValidator(IValidatable<?> validatable, String pattern){
		super(validatable);
		this.pattern = pattern;
	}
	
	@Override
	public boolean validate(Object value) {
		
		if(value.toString().matches(pattern)){
			return true;
		}else{
			StringBuffer sb = new StringBuffer();
			sb.append("The value of the argument ");
			sb.append(this.getValidatable().getName());
			sb.append(" did not match the pattern '");
			sb.append(pattern);
			sb.append("'.(value='");
			sb.append(value);
			sb.append("')");
			
			this.setInvalidMessage(sb.toString());
			
			return false;
		}
	}


}

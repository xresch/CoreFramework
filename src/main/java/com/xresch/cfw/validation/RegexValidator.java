package com.xresch.cfw.validation;

/**************************************************************************************************************
 * The RegexValidator will validate if value.toString() is matching the given 
 * regular expression.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class RegexValidator extends AbstractValidator {

	private String pattern="";
	
	public RegexValidator(IValidatable<?> validatable, String pattern){
		super(validatable);
		this.pattern = pattern;
	}
	
	public RegexValidator(String pattern){
		this.pattern = pattern;
	}
	
	@Override
	public boolean validate(Object value) {
		
		if(value.toString().matches(pattern)){
			return true;
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append("The value of  ");
			sb.append(this.getValidatable().getLabel());
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

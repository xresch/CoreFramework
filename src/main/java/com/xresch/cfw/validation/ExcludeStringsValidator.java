package com.xresch.cfw.validation;

/**************************************************************************************************************
 * The ExcludeStringsValidator will validate if value.toString() does not contain any of the given strings.
 * Value is valid if none of the specified strings is found.
 * Value is invalid if any of the specified strings is found.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class ExcludeStringsValidator extends AbstractValidator {

	private String[] stringArray;
	
	public ExcludeStringsValidator(IValidatable<?> validatable, String[] pattern){
		super(validatable);
		this.stringArray = pattern;
	}
	
	public ExcludeStringsValidator(String[] charArray){
		this.stringArray = charArray;
	}
	
	@Override
	public boolean validate(Object value) {
		
		if(value == null) {
			return true;
		}
		
		String valueString = value.toString();
		
		for(String current : stringArray) {
			if(valueString.contains(current)) {
				StringBuilder sb = new StringBuilder();
				sb.append("The value of  ");
				sb.append(this.getValidatable().getLabel());
				sb.append(" cannot include any of the following strings: '");
				
				String arrayString = String.join("\", \"", stringArray);
				sb.append("\""+arrayString+"\"");
				
				this.setInvalidMessage(sb.toString());
				
				return false;
			};
		}
		
		return true;
	}


}

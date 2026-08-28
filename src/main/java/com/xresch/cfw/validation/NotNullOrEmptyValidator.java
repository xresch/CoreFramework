package com.xresch.cfw.validation;

import java.util.Collection;
import java.util.Map;

/**************************************************************************************************************
 * This validator checks if a value is not null or empty.
 * It can operate on String, Maps and Collections.
 * Other types will only be checked on Null, but not on Empty.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class NotNullOrEmptyValidator extends AbstractValidator {

	private boolean validateStringsAsJson = false;
	public NotNullOrEmptyValidator(IValidatable<?> validatable) {
		super(validatable);
		// TODO Auto-generated constructor stub
	}
	
	public NotNullOrEmptyValidator() {
	}
	
	/**********************************************************
	 * Will validate Strings also as JSON. If the string
	 * is "{}" or "[]" then the value is also considered empty.
	 * @return instance for chaining
	 **********************************************************/
	public NotNullOrEmptyValidator validateStringsAsJson(boolean toggle) {
		validateStringsAsJson = toggle;
		return this;
	};

	@Override
	public boolean validate(Object value) {
		
		boolean isValid = true; 

		//---------------------------
		// Check is null
		if(value == null) { isValid = false; }
		
		//---------------------------
		// Else Check Strings
		else if( String.class.isAssignableFrom(value.getClass()) ){
			
			isValid = value.equals("") 
					? false
					: ! (   
						    validateStringsAsJson 
						 && ( value.equals("{}") 
						 || value.equals("[]") )
						);
			
		}
		
		//---------------------------
		// Else Check Maps
		else if(Map.class.isAssignableFrom(value.getClass())) {
			isValid = ((Map<?,?>)value).size() != 0;
		}
		
		//---------------------------
		// Else Check Collections
		else if(Collection.class.isAssignableFrom(value.getClass())) {
			isValid = ((Collection<?>)value).size() != 0;
		}

		if( !isValid ) {
			this.setInvalidMessage("The field "+validateable.getLabel()+" cannot be empty.");
		}
		
		return isValid;
		
	}

}

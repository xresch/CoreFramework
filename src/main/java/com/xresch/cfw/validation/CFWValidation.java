package com.xresch.cfw.validation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWValidation {

	/***************************************************************************
	 * Check if the object is not null or not an empty string.
	 * @param value
	 * @return
	 ***************************************************************************/
	public static boolean isNullOrEmptyString(Object value) {
		
		if(value == null) return true;
		
		if(value instanceof String && ((String)value).isEmpty()) {
			return true;
		}
		
		return false;
	}
}

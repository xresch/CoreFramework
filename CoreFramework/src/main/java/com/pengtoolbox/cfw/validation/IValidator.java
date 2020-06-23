package com.pengtoolbox.cfw.validation;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public interface IValidator {
	
	public boolean validate(Object value);
	public boolean validate();
	public String getInvalidMessage();
	public IValidator setInvalidMessage(String message);
	public IValidator setValidateable(IValidatable<?> validateable);
	public IValidatable<?> getValidatable();
	public IValidator setTag(String tag);
	public String getTag();
	
	/**********************************************************************************
	 * Check if null is allowed.
	 * @return true or false
	 **********************************************************************************/
	public boolean isNullAllowed();
	
	/**********************************************************************************
	 * Set if Null values are allowed.
	 * Empty Strings will be considered null.
	 * 
	 * @param isNullAllowed
	 * @return the instance for chaining.
	 **********************************************************************************/
	public IValidator setNullAllowed(boolean isNullAllowed);
}

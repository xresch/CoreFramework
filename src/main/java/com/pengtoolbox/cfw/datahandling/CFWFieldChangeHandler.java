package com.pengtoolbox.cfw.datahandling;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class CFWFieldChangeHandler<T> {

	/*********************************************************************************
	 * This method is executed before the value of a field is changed.
	 * You can take actions before the value is changed, and you can as well 
	 * prohibit the change by returning false
	 * @param oldValue
	 * @param newValue
	 * @return return true if the value should be changed, false otherwise.
	 *********************************************************************************/
	public abstract boolean handle(T oldValue, T newValue);
}

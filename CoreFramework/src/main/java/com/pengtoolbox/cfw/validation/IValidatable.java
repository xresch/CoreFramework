package com.pengtoolbox.cfw.validation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public interface IValidatable<T> {
	
	public abstract boolean validate();
	public abstract IValidatable<T> setName(String propertyName);
	public abstract String getName();
	public abstract boolean setValueValidated(T value);
	public abstract T getValue();

	public IValidatable<T> addValidator(IValidator e);

	public boolean removeValidator(IValidator o);
	
}

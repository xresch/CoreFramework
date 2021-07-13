package com.xresch.cfw.validation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public interface IValidatable<T> {
	
	public abstract boolean validate();
	public abstract IValidatable<T> setLabel(String propertyName);
	public abstract String getLabel();
	public abstract boolean setValueValidated(T value);
	public abstract T getValue();

	public IValidatable<T> addValidator(IValidator e);

	public boolean removeValidator(IValidator o);
	
}

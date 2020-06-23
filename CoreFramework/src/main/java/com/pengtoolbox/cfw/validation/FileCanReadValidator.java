package com.pengtoolbox.cfw.validation;

import java.io.File;

import com.pengtoolbox.cfw.utils.Ternary;



/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FileCanReadValidator extends AbstractValidator {

	
	public FileCanReadValidator(IValidatable<?> validatable) {
		super(validatable);
	}

	@Override
	public boolean validate(Object value) {
				
		Ternary result = validateNullEmptyAllowed(value);
		if(result == Ternary.FALSE ) this.setInvalidMessage("The following proerty is mandatory: '"+getValidatable().getName()+"'");
		if(result != Ternary.DONTCARE ) return result.toBoolean();
		
		File file = null;
		
		if(value instanceof String) {
			file = new File((String)value);
		}else if(value instanceof File) {
			file = (File)value;
		}
		
		
		if(file.canRead()){
			return true;
		}else {
			this.setInvalidMessage("File cannot be read: '"+value+"'");
			return false;
		}
	}

}

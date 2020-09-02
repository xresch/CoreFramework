package com.xresch.cfw.validation;

import java.io.File;

import com.xresch.cfw.utils.Ternary;



/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
		} else {
			this.setInvalidMessage("Unsupported type for FileCanWriteValidator: '" + value.getClass().getName() + "'");
			return false;
		}
		
		
		if(file.canRead()){
			return true;
		}else {
			this.setInvalidMessage("File cannot be read: '"+value+"'");
			return false;
		}
	}

}

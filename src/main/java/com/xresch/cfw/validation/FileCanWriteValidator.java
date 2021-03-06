package com.xresch.cfw.validation;

import java.io.File;
import java.io.IOException;

import com.xresch.cfw.utils.Ternary;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0
 *          International
 **************************************************************************************************************/
public class FileCanWriteValidator extends AbstractValidator {

	public FileCanWriteValidator(IValidatable<?> validatable) {
		super(validatable);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean validate(Object value) {

		Ternary result = validateNullEmptyAllowed(value);
		if (result != Ternary.DONTCARE)
			return result.toBoolean();

		File file = null;

		if (value instanceof String) {
			file = new File((String) value);
		} else if (value instanceof File) {
			file = (File) value;
		} else {
			this.setInvalidMessage("Unsupported type for FileCanWriteValidator: '" + value.getClass().getName() + "'");
			return false;
		}

		if (file.exists()) {
			if (!file.canWrite()) {
				this.setInvalidMessage("File cannot be written: '" + file.getAbsolutePath() + "'");
				return false;
			}
		} else {
			boolean success = true;
			try {
				success &= file.mkdirs();
				success &= file.createNewFile();
				success &= file.delete();
			} catch (IOException e) {
				this.setInvalidMessage("File cannot be written: '" + file.getAbsolutePath() + "'");
				return false;
			}
			return success;
		}

		return false;
	}
}

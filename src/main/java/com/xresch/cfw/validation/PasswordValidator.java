package com.xresch.cfw.validation;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class PasswordValidator extends AbstractValidator {
	
	public PasswordValidator(IValidatable<?> validatable){
		super(validatable);
	}
	
	public PasswordValidator(){}
	
	@Override
	public boolean validate(Object value) {
		
		String category = FeatureUserManagement.CONFIG_CATEGORY_PW_POLICY;
		//--------------------------------
		// Is Check Enabled
		if(!CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_ISENABLED)) {
			return true;
		}
		
		String invalidMessage = "";
		boolean isValid = true; 
		//--------------------------------
		// Check Minimum Length
		int minLength = CFW.DB.Config.getConfigAsInt(category, FeatureUserManagement.CONFIG_PWPOLICY_MINLENGTH);
		if( value.toString().length() < minLength ) {
			invalidMessage += "minimum "+minLength+" characters, ";
			isValid = false;
		}
		
		//--------------------------------
		// Check Lowercase Letter
		if(CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_LOWERCASE)) {
			if( !value.toString().matches(".*[a-z]+.*") ){
				invalidMessage += "1 lowercase letter, ";
				isValid = false;
			}
		}
		
		//--------------------------------
		// Check Uppercase Letter
		if(CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_UPPERCASE)) {
			if( !value.toString().matches(".*[A-Z]+.*") ){
				invalidMessage += "1 uppercase letter, ";
				isValid = false;
			}
		}
		
		//--------------------------------
		// Check Number and Special Char
		if(CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_SPECIALORNUM)) {
			if( !value.toString().matches(".*[^A-Za-z]+.*") ){
				invalidMessage += "1 number or special character, ";
				isValid = false;
			}
		}else {
			//--------------------------------
			// Check Number Letter
			if(CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_NUMBER)) {
				if( !value.toString().matches(".*[0-9]+.*") ){
					invalidMessage += "1 number, ";
					isValid = false;
				}
			}
			
			//--------------------------------
			// Check Special 
			if(CFW.DB.Config.getConfigAsBoolean(category, FeatureUserManagement.CONFIG_PWPOLICY_SPECIAL)) {
				if( !value.toString().matches(".*[^A-Za-z0-9]+.*") ){
					invalidMessage += "1 special character, ";
					isValid = false;
				}
			}
		}
		
		if(!isValid) {
			this.setInvalidMessage("Password does not match the requirements: "
					+invalidMessage.substring(0, invalidMessage.length()-2)
				);
		}
		
		return isValid;
	}


}

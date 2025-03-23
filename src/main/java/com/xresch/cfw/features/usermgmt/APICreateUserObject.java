package com.xresch.cfw.features.usermgmt;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.PasswordValidator;

public class APICreateUserObject extends CFWObject{
	
	private User user;
	public static final String INITIAL_PASSWORD = "INITIAL_PASSWORD";
	
	private CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, INITIAL_PASSWORD)
			.setDescription("(Optional)The initial password for the user. If the created user is a foreign user, this value is ignored and a random, unretrievable password will be set.")
			.disableSanitization()
			.addValidator(new LengthValidator(1, 255))
			.addValidator(new PasswordValidator());
			
	public APICreateUserObject() {
		user = new User();
		this.addField(user.getField(UserFields.USERNAME.toString()));
		this.addField(user.getField(UserFields.FIRSTNAME.toString()));
		this.addField(user.getField(UserFields.LASTNAME.toString()));
		this.addField(user.getField(UserFields.EMAIL.toString()));
		this.addField(user.getField(UserFields.IS_FOREIGN.toString()));
		this.addField(password);
	}
	
	public User getUserWithPassword() { 
		if(user.isForeign()) {
			String randomPW = CFW.Random.stringAlphaNumSpecial(24);
			user.setNewPassword(randomPW, randomPW);
		}else {
			String newPW = this.password.getValue();
			user.setNewPassword(newPW, newPW);
		}
		
		return user; 
	}

}
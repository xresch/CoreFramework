package com.xresch.cfw.features.core.auth;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DBLoginProvider implements LoginProviderInterface {
	
	@Override
	public User checkCredentials(String username, String password) {

		User user = CFW.DB.Users.selectByUsernameOrMail(username);
		
		if(user != null && user.passwordValidation(password)) {
			return user;
		}
		
		return null;
	}
	
}

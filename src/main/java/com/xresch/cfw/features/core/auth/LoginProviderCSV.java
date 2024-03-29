package com.xresch.cfw.features.core.auth;

import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class LoginProviderCSV implements LoginProviderInterface {
	
	private static Logger logger = CFWLog.getLogger(ServletLogin.class.getName());
	private static HashMap<String, String> userCredentials = null;
	
	public LoginProviderCSV() {
		
		if(userCredentials == null) {
			LoginProviderCSV.loadCredentials();
		}
	}
	
	@Override
	public User checkCredentials(String username, String password) {
		
		if(CFW.DB.Users.checkUsernameExists(username)) {
			//--------------------------------
			// Check User in DB			
			User user = CFW.DB.Users.selectByUsernameOrMail(username);
			if(user.isForeign()) {
				String passwordFromFile = userCredentials.get(username);
				if(password.equals(passwordFromFile)) {
					return user;
				}
			}else {
				if(user.passwordValidation(password)) {
					return user;
				}
			}
		}else {
			//--------------------------------
			// Create user if password is correct
			String passwordFromFile = userCredentials.get(username);
			
			if(password.equals(passwordFromFile))
			{
				User newUser = new User(username)
						.isForeign(true)
						.status("Active");
				
				CFW.DB.Users.create(newUser);
				User userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
				
				CFW.DB.UserRoleMap.addRoleToUser(userFromDB, CFW.DB.Roles.CFW_ROLE_USER, true);
				
				return userFromDB;
			}
		}
		
		return null;
	}
	
	private static void loadCredentials() {

		//------------------------------
		// Load File
		String credentials = CFW.Files.getFileContent(null, CFW.Properties.AUTHENTICATION_CSV_FILE);
		
		if(credentials == null) {
			new CFWLog(logger).severe("Credential file could not be loaded: "+CFW.Properties.AUTHENTICATION_CSV_FILE);
			return;
		}
		
		//------------------------------
		// Read Credentials
		String[] lines = credentials.split("\n|\r\n");
		
		userCredentials = new HashMap<String, String>();
		for(String line : lines) {
			String[] userAndPW = line.split(";");
			if(userAndPW.length == 2) {
				userCredentials.put(userAndPW[0], userAndPW[1]);
			}else {
				new CFWLog(logger).severe("Error loading user credentials.");
			}
		}
	}

}

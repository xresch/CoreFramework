package com.pengtoolbox.cfw.login;

import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.features.usermgmt.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class LoginFacade {

	private static LoginFacade INSTANCE;
	
	private static LoginProvider provider;
	
	private LoginFacade() {
		
		switch(CFWProperties.AUTHENTICATION_METHOD.trim().toUpperCase()) {
		
			case "CSV": 	provider = new CSVLoginProvider();
				 			break;
				 			
			case "LDAP": 	provider = new LDAPLoginProvider();
 							break;
 			
			case "DB": 		provider = new DBLoginProvider();
				break;
 							
			default:		throw new RuntimeException("Unknown authentication method'"+CFWProperties.AUTHENTICATION_METHOD+"', please review the config file.");
		}
	}
	
	public static LoginFacade getInstance() {
		
		if(INSTANCE == null) {
			INSTANCE = new LoginFacade();
		}
		
		return INSTANCE;
	}
	
	/******************************************************************************
	 * Check if the username password exists and has to return a user object which
	 * can be found in the Database.
	 * In case of foreign login providers like LDAP, users that do not exist in the
	 * DB have to be created by this method.
	 * 
	 * @param username
	 * @param password
	 * @return user object fetched from the database with CFW.DB.Users.select*(),
	 *         or null if the login failed.
	 ******************************************************************************/
	public User checkCredentials(String username, String password) {
		return provider.checkCredentials(username, password);
	}
	
	
	
	
}

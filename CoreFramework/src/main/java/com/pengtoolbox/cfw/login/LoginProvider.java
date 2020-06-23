package com.pengtoolbox.cfw.login;

import com.pengtoolbox.cfw.features.usermgmt.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public interface LoginProvider {

	/******************************************************************************
	 * Check if the username password exists and has to return a User object which
	 * can be found in the Database.
	 * In case of foreign login providers like LDAP, users that do not exist in the
	 * DB have to be created by this method.
	 * 
	 * @param username
	 * @param password
	 * @return user object fetched from the database with CFW.DB.Users.select*(),
	 *         or null if the login failed.
	 ******************************************************************************/
	public User checkCredentials(String username, String password);
}

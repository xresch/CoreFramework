package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformationResponse;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationRequest;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationResponseParser;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class LoginUtils {

	private static LoginProviderInterface provider = null;
	
	private LoginUtils() {
		// Hide Constructor
	}
	
	
	/******************************************************************************
	 * Returns the configured login provider.
	 * 
	 ******************************************************************************/
	private static LoginProviderInterface getProvider() {
		
		if(provider == null) {
			switch(CFWProperties.AUTHENTICATION_METHOD.trim().toUpperCase()) {
				
				case "CSV": 	provider = new LoginProviderCSV();
					 			break;
					 			
				case "LDAP": 	provider = new LoginProviderLDAP();
									break;
					
				case "DB": 		provider = new LoginProviderDatabase();
					break;
									
				default:		throw new RuntimeException("Unknown authentication method'"+CFWProperties.AUTHENTICATION_METHOD+"', please review the config file.");
			}
		}
		
		return provider;
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
	public static User checkCredentials(String username, String password) {
		LoginProviderInterface provider = getProvider();
		if(provider != null) {
			return provider.checkCredentials(username, password);
		}
		return null;
	}
	
	/******************************************************************************
	 * Logs the user in and redirects to the next page after login.
	 * @param HttpServletRequest request
	 * @param HttpServletResponse response
	 * @param User user to login
	 * @param tedirectTo URL to redirect or null for default 
	 * @return boolean true if successfully logged in, false otherwise
	 ******************************************************************************/
	public static boolean loginUserAndCreateSession(HttpServletRequest request, HttpServletResponse response, User user, String redirectTo) {
		
		if(user != null 
		&& user.status() != null 
		&& user.status().toUpperCase().equals("ACTIVE")) {
			//Login success
			CFWSessionData data = CFW.Context.Request.getSessionData(); 
			data.resetUser();
			data.setUser(user);
			data.setClientIP(CFW.HTTP.getClientIP(request));
			data.triggerLogin();
			
			if(redirectTo == null || redirectTo.isEmpty()) {
				redirectTo = CFW.Context.App.getApp().getDefaultURL();
			}
			CFW.HTTP.redirectToURL(response, redirectTo);
			return true; 
		}	
		
		return false;
	}
		
	
	/******************************************************************************
	 * 
	 * @param username the name of the user to fetch or create if not exists
	 * @param emailString email of the user or null
	 * @param firstnameString firstname of the user or null
	 * @param lastnameString lastname of the user or null
	 * @param isForeign TODO
	 ******************************************************************************/
	public static User fetchUserCreateIfNotExists(
			  String username
			, String emailString
			, String firstnameString
			, String lastnameString
			, boolean isForeign) {
		
    	//------------------------------
    	// Create User in DB if not exists
    	User userFromDB = null;
    	if(!CFW.DB.Users.checkUsernameExists(username)) {

	    	User newUser = new User(username)
					.isForeign(isForeign)
					.status("Active")
					.email(emailString)
					.firstname(firstnameString)
					.lastname(lastnameString);

			CFW.DB.Users.create(newUser);
			userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
			
			CFW.DB.UserRoleMap.addRoleToUser(userFromDB, CFW.DB.Roles.CFW_ROLE_USER, true);
    	}else{
    		userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
    		
    		//-----------------------------
    		// Update mail if necessary
    		if( (emailString != null && userFromDB.email() == null)
    		 || (emailString != null && !userFromDB.email().equals(emailString) ) ) {
    			userFromDB.email(emailString);
    			userFromDB.update(UserFields.EMAIL.toString());
    		}
    		
    		//-----------------------------
    		// Update firstname if necessary
    		if( (firstnameString != null && userFromDB.firstname() == null)
    		 || (firstnameString != null && !userFromDB.firstname().equals(firstnameString) ) ) {
    			userFromDB.firstname(firstnameString);
    			userFromDB.update(UserFields.FIRSTNAME.toString());
    		}
    		
    		//-----------------------------
    		// Update lastname if necessary
    		if( (lastnameString != null && userFromDB.lastname() == null)
    		 || (lastnameString != null && !userFromDB.lastname().equals(lastnameString) ) ) {
    			userFromDB.lastname(lastnameString);
    			userFromDB.update(UserFields.LASTNAME.toString());
    		}
    	}
    	
		return userFromDB;
	}
		
}

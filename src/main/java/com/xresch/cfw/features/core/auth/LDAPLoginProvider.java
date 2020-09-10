package com.xresch.cfw.features.core.auth;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class LDAPLoginProvider implements LoginProviderInterface {

	@Override
	public User checkCredentials(String username, String password) {

		if(CFW.DB.Users.checkUsernameExists(username)) {
			//--------------------------------
			// Check User in DB			
			User user = CFW.DB.Users.selectByUsernameOrMail(username);
			if(user.isForeign()) {
				return authenticateAgainstLDAP(username, password);
			}else {
				if(user.passwordValidation(password)) {
					return user;
				}
			}
		}else {
			//--------------------------------
			// Create User if password is correct
			
			return authenticateAgainstLDAP(username, password);

		}
		
		return null;
	}
		
	/**********************************************************************
	 * Authenticate against the LDAP defined in the cfw.properties.
	 * @param username
	 * @param password
	 * @return true if credentials are valid, false otherwise
	 **********************************************************************/
	private User authenticateAgainstLDAP(String username, String password) {
		Properties props = new Properties(); 
		InitialDirContext context = null;
		String userInNamespace = "";
		
		try {
		    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		    props.put(Context.PROVIDER_URL, CFWProperties.LDAP_URL);
		    props.put(Context.SECURITY_PRINCIPAL, CFWProperties.LDAP_USER);
		    props.put(Context.SECURITY_CREDENTIALS, CFWProperties.LDAP_PASSWORD);
	
		    if(CFW.Properties.LDAP_SSL_ENABLED) {
		    	props.put(Context.SECURITY_PROTOCOL, "ssl");
		    }
		    
		    context = new InitialDirContext(props);
	
		    SearchControls ctrls = new SearchControls();
		    ctrls.setReturningAttributes(new String[] {});
		    ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	
		    NamingEnumeration<javax.naming.directory.SearchResult> answers = context.search(CFWProperties.LDAP_SEARCHBASE, "("+CFWProperties.LDAP_ATTRIBUTE_USERNAME+"=" + username + ")", ctrls);
		    if(answers.hasMore()) {
		    	
		    	//------------------------------
		    	// Read LDAP Attributes
		    	javax.naming.directory.SearchResult result = answers.nextElement();
		    	
		    	userInNamespace = result.getNameInNamespace();
		    	Attributes attr = context.getAttributes(userInNamespace);
		    	
		    	//-------------------------------
		    	// Attempt another authentication, now with the user
	            Properties authEnv = new Properties();
	            authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	            authEnv.put(Context.PROVIDER_URL, CFWProperties.LDAP_URL);
	            authEnv.put(Context.SECURITY_PRINCIPAL, userInNamespace);
	            authEnv.put(Context.SECURITY_CREDENTIALS, password);
	            // fails if wrong password
	            new InitialDirContext(authEnv);
	            
	            
		    	//-------------------------------
		    	// 
		    	Attribute mail = attr.get(CFW.Properties.LDAP_ATTRIBUTE_EMAIL);
		    	String emailString = null;
		    	if(mail != null) {
		    		emailString = mail.get(0).toString();
		    	}
		    	
		    	Attribute firstname = attr.get(CFW.Properties.LDAP_ATTRIBUTE_FIRSTNAME);
		    	String firstnameString = null;
		    	if(firstname != null) {
		    		firstnameString = firstname.get(0).toString();
		    	}
		    	
		    	Attribute lastname = attr.get(CFW.Properties.LDAP_ATTRIBUTE_LASTNAME);
		    	String lastnameString = null;
		    	if(lastname != null) {
		    		lastnameString = lastname.get(0).toString();
		    	}
		    	
		    	//System.out.println("user: "+user);
		    	//System.out.println("MAIL: "+mail);
		    	
		    	//------------------------------
		    	// Create User in DB if not exists
		    	User user = LoginUtils.fetchUserCreateIfNotExists(username, emailString, firstnameString, lastnameString);
		    	return user;
		    }else {
		    	return null;
		    }
	
		}catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    
	}
	
}

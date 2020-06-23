package com.pengtoolbox.cfw.login;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class LDAPLoginProvider implements LoginProvider {

	public LDAPLoginProvider() {}
	
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
		    	User userFromDB = null;
		    	if(!CFW.DB.Users.checkUsernameExists(username)) {

			    	User newUser = new User(username)
							.isForeign(true)
							.status("Active")
							.email(emailString)
							.firstname(firstnameString)
							.lastname(lastnameString);

					CFW.DB.Users.create(newUser);
					userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
					
					CFW.DB.UserRoleMap.addUserToRole(userFromDB, CFW.DB.Roles.CFW_ROLE_USER, true);
		    	}else{
		    		userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
		    		
		    		//-----------------------------
		    		// Update mail if necessary
		    		if( (mail != null && userFromDB.email() == null)
		    		 || (mail != null && !userFromDB.email().equals(mail.get(0)) ) ) {
		    			userFromDB.email(emailString);
		    			userFromDB.update(UserFields.EMAIL.toString());
		    		}
		    		
		    		//-----------------------------
		    		// Update firstname if necessary
		    		if( (firstname != null && userFromDB.firstname() == null)
		    		 || (firstname != null && !userFromDB.firstname().equals(firstname.get(0)) ) ) {
		    			userFromDB.firstname(firstnameString);
		    			userFromDB.update(UserFields.FIRSTNAME.toString());
		    		}
		    		
		    		//-----------------------------
		    		// Update lastname if necessary
		    		if( (lastname != null && userFromDB.lastname() == null)
		    		 || (lastname != null && !userFromDB.lastname().equals(lastname.get(0)) ) ) {
		    			userFromDB.lastname(lastnameString);
		    			userFromDB.update(UserFields.LASTNAME.toString());
		    		}
		    	}
		    	
				return userFromDB;
		    }else {
		    	return null;
		    }
	
		}catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    
	}
	
}

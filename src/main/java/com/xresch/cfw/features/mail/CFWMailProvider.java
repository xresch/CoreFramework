package com.xresch.cfw.features.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public abstract class CFWMailProvider {
	
	
	/***************************************************************
	 * Unique identifier for this provider, which will be used in the
	 * configuration to select the provider. Never change this value
	 * once implemented.
	 * @return String unique ID
	 ***************************************************************/
	public abstract String uniqueID();
	
	/***************************************************************
	 * Used to enable and disable the sending of emails.
	 * @return true if mailing is enabled, false otherwise
	 ***************************************************************/
	public abstract boolean isMailingEnabled();
	
	/***************************************************************
	 * Returns the email used for no-reply messages.
	 * 
	 * @return the email used as the no-reply email
	 ***************************************************************/
	public abstract String getNoReplyMail();
	
	/***************************************************************
	 * Returns the email used for messages users can reply to.
	 * 
	 * @return the email used as a reply email
	 ***************************************************************/
	public abstract String getReplyMail();
	
	/***************************************************************
	 * Creates a session for sending mails.
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	public abstract Session createSession();
	
	/***************************************************************
	 * Creates a session for sending mails.
	 * @param host
	 * @param port
	 * @param authMethod either NONE | SSL | TLS
	 * @param loginEmail
	 * @param password
	 * 
	 * @return session 
	 * 
	 ***************************************************************/
	public static Session createSession(
			  String host
			, int port
			, String authMethod
			, String loginEmail
			, String password
			) {
		
		
		//------------------------------
		//General Properties
		Properties props = new Properties();
		props.put("mail.smtp.host", host); 
		props.put("mail.smtp.port", port); 
		
		authMethod = authMethod.toUpperCase();
		
		//------------------------------
		// TLS Authentication
		if(authMethod.equals("TLS")) {
			props.put("mail.smtp.auth", "true"); //enable authentication
			props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
			
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail, password);
				}
			};
			
			return Session.getInstance(props, auth);
		}
		
		//------------------------------
		// SSL Authentication
		if(authMethod.equals("SSL")){
			
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
			props.put("mail.smtp.ssl.checkserveridentity", "true"); 
			props.put("mail.smtp.auth", "true"); 
			props.put("mail.smtp.port", port); 
			
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail, password);
				}
			};
			
			return Session.getInstance(props, auth);
		}
		
		//------------------------------
		// No Authentication
		return Session.getInstance(props, null);
	}
}

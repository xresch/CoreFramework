package com.xresch.cfw.features.mail;

import javax.mail.Session;

import com.xresch.cfw._main.CFW;

public class CFWMailProviderProperties extends CFWMailProvider {
	
	public static final String UNIQUE_ID = "cfw.properties";
	
	/***************************************************************
	 * Unique identifier for this provider, which will be used in the
	 * configuration to select the provider. Never change this value
	 * once implemented.
	 * @return String unique ID
	 ***************************************************************/
	public String uniqueID() {
		return UNIQUE_ID;
	}
		
	/***************************************************************
	 * Used to enable and disable the sending of emails.
	 * @return true if mailing is enabled, false otherwise
	 ***************************************************************/
	@Override
	public boolean isMailingEnabled() {
		return CFW.Properties.MAIL_ENABLED;
	}

	/***************************************************************
	 * Returns the email used for no-reply messages.
	 * 
	 * @return the email used as the no-reply email
	 ***************************************************************/
	@Override
	public String getNoReplyMail() {
		return CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY;
	}
	
	/***************************************************************
	 * Returns the email used for messages users can reply to.
	 * 
	 * @return the email used as a reply email
	 ***************************************************************/
	@Override
	public String getReplyMail() {
		return CFW.Properties.MAIL_SMTP_FROMMAIL_REPLY;
	}

	/***************************************************************
	 * Creates a session for sending mails.
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	@Override
	public Session createSession() {
		
		//------------------------------
		// Check if mailing is enabled
		if( ! isMailingEnabled()) { return null; }

		//------------------------------
		// Get Variables
		final String host = CFW.Properties.MAIL_SMTP_HOST;
		final int port = CFW.Properties.MAIL_SMTP_PORT;
		final String authMethod = CFW.Properties.MAIL_SMTP_AUTHENTICATION.trim().toUpperCase();
		final String loginEmail = CFW.Properties.MAIL_SMTP_LOGIN_MAIL;
		final String password = CFW.Properties.MAIL_SMTP_LOGIN_PASSWORD; 
		
		return createSession(host, port, authMethod, loginEmail, password);
			
		
	}

}

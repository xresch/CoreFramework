package com.xresch.cfw.features.mail;

import javax.mail.Session;

public interface CFWMailProvider {
	
	
	/***************************************************************
	 * Unique identifier for this provider, which will be used in the
	 * configuration to select the provider. Never change this value
	 * once implemented.
	 * @return String unique ID
	 ***************************************************************/
	public String uniqueID();
	
	/***************************************************************
	 * Used to enable and disable the sending of emails.
	 * @return true if mailing is enabled, false otherwise
	 ***************************************************************/
	public boolean isMailingEnabled();
	
	/***************************************************************
	 * Returns the email used for no-reply messages.
	 * 
	 * @return the email used as the no-reply email
	 ***************************************************************/
	public String getNoReplyMail();
	
	/***************************************************************
	 * Returns the email used for messages users can reply to.
	 * 
	 * @return the email used as a reply email
	 ***************************************************************/
	public String getReplyMail();
	
	/***************************************************************
	 * Creates a session for sending mails.
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	public Session createSession();
}

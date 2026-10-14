package com.xresch.cfw.features.mail;

import java.util.logging.Logger;

import javax.mail.Session;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.logging.SysoutInterceptor;

public class CFWMailProviderDatabase extends CFWMailProvider {
	
	public static final Logger logger = CFWLog.getLogger(CFWMailProvider.class.getName());
	
	public static final String UNIQUE_ID = "database";
	
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
		return  CFW.DB.Config.getConfigAsBoolean(
				  FeatureMail.CONFIG_CATEGORY
				, FeatureMail.CONFIG_MAIL_ENABLED
			);
	}

	/***************************************************************
	 * Returns the email used for no-reply messages.
	 * 
	 * @return the email used as the no-reply email
	 ***************************************************************/
	@Override
	public String getNoReplyMail() {
		return  CFW.DB.Config.getConfigAsString(
				  FeatureMail.CONFIG_CATEGORY
				, FeatureMail.CONFIG_MAIL_FROMMAIL_NOREPLY
			);
	}
	
	/***************************************************************
	 * Returns the email used for messages users can reply to.
	 * 
	 * @return the email used as a reply email
	 ***************************************************************/
	@Override
	public String getReplyMail() {
		return CFW.DB.Config.getConfigAsString(
				  FeatureMail.CONFIG_CATEGORY
				, FeatureMail.CONFIG_MAIL_FROMMAIL_REPLY
			);
	}

	/***************************************************************
	 * Creates a session for sending mails.
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	@Override
	public Session createSession() {
		
		try {
			//------------------------------
			// Check if mailing is enabled
			if( ! isMailingEnabled() ) { return null; }
	
			//------------------------------
			// Get Variables
			final String host 		= CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_HOST);
			final int port 			= CFW.DB.Config.getConfigAsInt(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_PORT);
			final String authMethod = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_AUTHENTICATION);
			final String loginEmail = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_LOGIN_MAIL);
			final String password 	= CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_LOGIN_PASSWORD);
			
			return createSession(host, port, authMethod, loginEmail, password);
		}catch(Throwable e){
			new CFWLog(logger).warn("Error while creating Mailing Session: "+e.getMessage(), e);
		}
		
		return null;
		
	}

}

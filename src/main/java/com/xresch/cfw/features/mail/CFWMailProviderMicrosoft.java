package com.xresch.cfw.features.mail;

import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public class CFWMailProviderMicrosoft extends CFWMailProvider {

	public static final Logger logger =
			CFWLog.getLogger(CFWMailProviderMicrosoft.class.getName());

	public static final String UNIQUE_ID = "Microsoft";

	//============================================================
	// OAuth Token Cache
	//============================================================

	private String cachedAccessToken = null;
	private Date cachedTokenExpiration = null;

	// Refresh token 5 minutes before expiration.
	private static final long TOKEN_EXPIRATION_BUFFER_MS =
			5 * 60 * 1000;


	/***************************************************************
	 * Unique identifier for this provider.
	 * @return String unique ID
	 ***************************************************************/
	@Override
	public String uniqueID() {
		return UNIQUE_ID;
	}


	/***************************************************************
	 * Used to enable and disable the sending of emails.
	 * @return true if mailing is enabled, false otherwise
	 ***************************************************************/
	@Override
	public boolean isMailingEnabled() {
		return CFW.DB.Config.getConfigAsBoolean(
				FeatureMail.CONFIG_CATEGORY,
				FeatureMail.CONFIG_MAIL_ENABLED
		);
	}


	/***************************************************************
	 * Returns the email used for no-reply messages.
	 ***************************************************************/
	@Override
	public String getNoReplyMail() {
		return CFW.DB.Config.getConfigAsString(
				FeatureMail.CONFIG_CATEGORY,
				FeatureMail.CONFIG_MAIL_FROMMAIL_NOREPLY
		);
	}


	/***************************************************************
	 * Returns the email used for messages users can reply to.
	 ***************************************************************/
	@Override
	public String getReplyMail() {
		return CFW.DB.Config.getConfigAsString(
				FeatureMail.CONFIG_CATEGORY,
				FeatureMail.CONFIG_MAIL_FROMMAIL_REPLY
		);
	}


	/***************************************************************
	 * Creates a session for sending mails.
	 *
	 * Authentication is performed using Microsoft OAuth 2.0
	 * and the XOAUTH2 SMTP authentication mechanism.
	 *
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	@Override
	public Session createSession() {

		try {

			//--------------------------------------
			// Check if mailing is enabled
			if (!isMailingEnabled()) {
				return null;
			}

			//--------------------------------------
			// Get SMTP settings
			final String host = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_HOST);
			final int port = CFW.DB.Config.getConfigAsInt(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_PORT);
			final String loginEmail = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_LOGIN_MAIL);
			
			//--------------------------------------
			// Get Microsoft OAuth settings
			final String tenantID = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_OAUTH_TENANTID);
			final String clientID = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_OAUTH_CLIENTID);
			final String clientSecret = CFW.DB.Config.getConfigAsString(FeatureMail.CONFIG_CATEGORY, FeatureMail.CONFIG_MAIL_OAUTH_CLIENTSECRET);

			//--------------------------------------
			// Configure JavaMail
			java.util.Properties props = new Properties();

			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", String.valueOf(port));

			props.put("mail.smtp.auth", "true");

			// STARTTLS
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.starttls.required", "true");

			// OAuth 2.0 / XOAUTH2
			props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
			props.put("mail.smtp.auth.login.disable", "true");
			props.put("mail.smtp.auth.plain.disable", "true");
			
			props.put("mail.debug", "true");
			
			//--------------------------------------
			// Create session
			Session session = Session.getInstance(
					props,
					new Authenticator() {

						@Override
						protected PasswordAuthentication
								getPasswordAuthentication() {

							try {
								String accessToken = getAccessToken( tenantID, clientID, clientSecret );
								return new PasswordAuthentication(loginEmail, accessToken);
							} catch (Exception e) {
								new CFWLog(logger)
									.severe( "Error while obtaining Microsoft OAuth token: "+ e.getMessage(),e);
								return null;
							}
						}
					}
			);

			return session;

		} catch (Throwable e) {
			new CFWLog(logger)
				.warn("Error while creating Microsoft Mailing Session: "+ e.getMessage(), e);
		}

		return null;
	}


	/***************************************************************
	 * Obtains an OAuth 2.0 access token from Microsoft Entra ID.
	 *
	 * Uses the client credentials flow.
	 *
	 * The token is cached and reused until shortly before expiration.
	 *
	 * @return OAuth access token
	 ***************************************************************/
	private synchronized String getAccessToken( String tenantID, String clientID, String clientSecret) throws Exception {

		//--------------------------------------
		// Check cached token
		if(cachedAccessToken != null
		&& cachedTokenExpiration != null) {

			long expirationTime = cachedTokenExpiration.getTime() - TOKEN_EXPIRATION_BUFFER_MS;

			if (System.currentTimeMillis() < expirationTime) {
				return cachedAccessToken;
			}
		}

		//--------------------------------------
		// Create confidential client application
		ConfidentialClientApplication app =
				ConfidentialClientApplication.builder(
					clientID,
					ClientCredentialFactory.createFromSecret( clientSecret)
				)
				.authority("https://login.microsoftonline.com/" + tenantID)
				.build();

		//--------------------------------------
		// Request Exchange Online access token
		ClientCredentialParameters parameters =
				ClientCredentialParameters.builder(
						Collections.singleton("https://outlook.office365.com/.default")
				).build();

		//--------------------------------------
		// Acquire token
		IAuthenticationResult result = app.acquireToken(parameters).get();

		//--------------------------------------
		// Cache token
		cachedAccessToken = result.accessToken();
		cachedTokenExpiration = result.expiresOnDate();

		return cachedAccessToken;
	}

}
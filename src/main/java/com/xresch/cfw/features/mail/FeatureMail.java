package com.xresch.cfw.features.mail;

import java.util.ArrayList;

import com.github.tomakehurst.wiremock.core.Options;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureMail extends CFWAppFeature {
		

	public static final String CONFIG_CATEGORY = "Mailing";
	public static final String CONFIG_MAILING_PROVIDER = "Mail Provider";
	
	/** Choose if you want the application to send mails. */
	public static String CONFIG_MAIL_ENABLED = "Mail: Enabled";
	
	/** The smtp host used for sending mails. */
	public static String CONFIG_MAIL_HOST = "Mail: SMTP Host";
	
	/** The port of the smtp server. */
	public static String CONFIG_MAIL_PORT = "Mail: SMTP Port";
	
	/** The method used to authenticate against the smtp server, either of:  NONE | TLS | SSL. */
	public static String CONFIG_MAIL_AUTHENTICATION = "Mail: SMTP Authentication";
	
	/** The email used for login to the smtp server . */
	public static String CONFIG_MAIL_LOGIN_MAIL = "Mail: SMTP Login eMail";
	
	/** The password used for login to the smtp server.  */
	public static String CONFIG_MAIL_LOGIN_PASSWORD = "Mail: SMTP Login Password";
	
	/** The eMail used as the from-address for repliable mails. */
	public static String CONFIG_MAIL_FROMMAIL_REPLY = "Mail: From Reply";
	
	/** The eMail used as the from-address for no-reply mails.*/
	public static String CONFIG_MAIL_FROMMAIL_NOREPLY = "Mail: From No-Reply";
	
	@Override
	public void register() {


    	//----------------------------------
    	// Register Mail Providers
		CFW.Registry.Mail.addProvider(new CFWMailProviderProperties());
		CFW.Registry.Mail.addProvider(new CFWMailProviderDatabase());
		
	}

	@Override
	public void initializeDB() {
		//============================================================
		// CONFIGURATION
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAILING_PROVIDER)
				.description("The provider used for the mailing configuration.")
				.type(FormFieldType.SELECT)
				.options(CFW.Registry.Mail.getSelectOptionsList())
				.value(CFWMailProviderProperties.UNIQUE_ID)
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_ENABLED)
				.description("Enable or disable the mailing if the selected mail provider is 'database'.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_HOST)
				.description("The SMTP host for the provider 'database'.")
				.type(FormFieldType.TEXT)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_PORT)
				.description("The port for the SMTP host of the provider 'database'. (Standard Ports: 25/NoAuth, 587/TLS, 465/SSL)")
				.type(FormFieldType.NUMBER)
				.value("25")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		ArrayList<String> authOptions = new ArrayList<String>();
		authOptions.add("NONE");
		authOptions.add("TLS");
		authOptions.add("SSL");
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_AUTHENTICATION)
				.description("The authentication method used to login on the SMTP host.")
				.type(FormFieldType.SELECT)
				.options(authOptions)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_LOGIN_MAIL)
				.description("The email used to login on the SMTP host.")
				.type(FormFieldType.TEXT)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_LOGIN_PASSWORD)
				.description("The password used to login on the SMTP host.")
				.type(FormFieldType.PASSWORD)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_FROMMAIL_REPLY)
				.description("The email that is used for sending mails that a user can reply to.")
				.type(FormFieldType.TEXT)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_MAIL_FROMMAIL_NOREPLY)
				.description("The email that is used for sending mails that a user cannot reply to.")
				.type(FormFieldType.TEXT)
				.value("")
		);

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	

	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
}

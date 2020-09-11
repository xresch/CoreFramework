package com.xresch.cfw.features.core.auth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * Servlet which will handle the response from the identity provider(IdP).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSAML2Utils
{

	private static final Logger logger = CFWLog.getLogger(ServletSAML2Utils.class.getName());
			
	/******************************************************************
	 *
	 ******************************************************************/
	public static Saml2Settings getSAMLSettings() throws FileNotFoundException, IOException {
		
		Saml2Settings settings = null;

		//------------------------------------
		// Create Auth and Login
		SettingsBuilder builder = new SettingsBuilder();
		Properties props = new Properties();
		props.load(new FileInputStream(CFW.Properties.AUTHENTICATION_SAML2_CONFIGFILE));
		
		settings = builder.fromProperties(props).build();
		
		return settings;
	}
}
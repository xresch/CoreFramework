package com.xresch.cfw.features.core.auth.saml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.xresch.cfw.features.core.auth.SSOProviderSettings;
import com.xresch.cfw.features.core.auth.SSOProviderSettingsManagement;
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
	public static Saml2Settings getSAMLSettings(HttpServletRequest request) throws FileNotFoundException, IOException {
		

		//------------------------------------
		// Get SSO Provider ID
		HttpSession session = request.getSession();
		Object ssoProviderIDObject = session.getAttribute(SSOProviderSettings.PROPERTY_SSO_PROVIDER_ID);
		String providerIDString = null;
		if(ssoProviderIDObject instanceof String) { providerIDString = (String)ssoProviderIDObject; }
		
		Saml2Settings settings = null;

		//------------------------------------
		// Create Auth and Login

//		Properties props = new Properties();
//		
//		try(FileInputStream configStream = new FileInputStream(CFW.Properties.AUTHENTICATION_SAML2_CONFIGFILE)){
//			props.load(configStream);
//		}
		
		SSOProviderSettingsSAML provider = (SSOProviderSettingsSAML)SSOProviderSettingsManagement.getEnvironment(Integer.parseInt(providerIDString));
		Properties props = provider.createSAMLProperties(request);
		SettingsBuilder builder = new SettingsBuilder();
		settings = builder.fromProperties(props).build();
		
		return settings;
	}
}
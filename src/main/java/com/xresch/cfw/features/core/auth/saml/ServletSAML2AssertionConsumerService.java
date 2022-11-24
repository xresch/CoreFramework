package com.xresch.cfw.features.core.auth.saml;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.servlet.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Constants;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.auth.LoginUtils;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * Servlet which will handle the response from the identity provider(IdP).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSAML2AssertionConsumerService extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletSAML2AssertionConsumerService.class.getName());
		
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		
    }
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			//------------------------------------
			// Create Auth and Process Response
			Saml2Settings settings = ServletSAML2Utils.getSAMLSettings();
			Auth auth = new Auth(settings, request, response);

			auth.processResponse();
			
			Map<String, List<String>> attributes = auth.getAttributes();
			String nameId = auth.getNameId();
			String nameIdFormat = auth.getNameIdFormat();
			String sessionIndex = auth.getSessionIndex();
			String nameidNameQualifier = auth.getNameIdNameQualifier();
			String nameidSPNameQualifier = auth.getNameIdSPNameQualifier();
			

			//------------------------------------
			// Check is Authentication successful
			if (!auth.isAuthenticated()) {
				new CFWLog(logger).severe("SAML: Not authenticated", new Throwable());
			}
	
			//------------------------------------
			// Handle Failing Auth
			List<String> errors = auth.getErrors();
			if (!errors.isEmpty()) {
				new CFWLog(logger).severe("SAML: Errors"+ String.join(", ", errors), new Throwable());
	
				if (auth.isDebugActive()) {
					String errorReason = auth.getLastErrorReason();
					if (errorReason != null && !errorReason.isEmpty()) {
						new CFWLog(logger).severe(auth.getLastErrorReason(), new Throwable());
					}
				}
				
				new CFWLog(logger)
					.custom("nameId", nameId)
					.custom("nameIdFormat", nameIdFormat)
					.custom("sessionIndex", sessionIndex)
					.custom("nameidNameQualifier", nameidNameQualifier)
					.custom("nameidSPNameQualifier", nameidSPNameQualifier)
					.custom("attributes", attributes.toString())
					.severe("SAML Login Error");
				
				return;
			}

			
			//------------------------------------
			// Do Login User
			User user;
			if(nameIdFormat.contentEquals(Constants.NAMEID_EMAIL_ADDRESS)) {
				String eMail = nameId;
				nameId = nameId.split("@")[0];
				user = LoginUtils.fetchUserCreateIfNotExists(nameId, eMail, null, null, true);
			}else {
				user = LoginUtils.fetchUserCreateIfNotExists(nameId, null, null, null, true);
			}
			
			
			new CFWLog(logger)
				.custom("nameId", nameId)
				.custom("nameIdFormat", nameIdFormat)
				.custom("sessionIndex", sessionIndex)
				.custom("nameidNameQualifier", nameidNameQualifier)
				.custom("nameidSPNameQualifier", nameidSPNameQualifier)
				.custom("attributes", attributes.toString())
				.finer("SAML Login Success");
					
			
			String redirectTo = request.getParameter("RelayState");
			
			if (redirectTo == null 
			|| redirectTo.isEmpty() 
			|| redirectTo.equals(ServletUtils.getSelfRoutedURLNoQuery(request)) 
			|| redirectTo.contains("/saml2/login")) { // We don't want to be redirected to SAML2 login
				redirectTo = CFW.Context.App.getApp().getDefaultURL();
			}
			
	    	//------------------------------
	    	// Create User in DB if not exists
	    	LoginUtils.loginUserAndCreateSession(request, response, user, redirectTo);

		} catch (SettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
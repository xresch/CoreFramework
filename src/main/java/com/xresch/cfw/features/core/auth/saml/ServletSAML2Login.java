package com.xresch.cfw.features.core.auth.saml;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * The first servlet to call for the SAML authentication.
 * Will trigger the request to the identity provider(IdP).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSAML2Login extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletSAML2Login.class.getName());
		
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		doPost( request, response );
    }
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			//------------------------------------
			// Create Auth and Login
			Saml2Settings settings = ServletSAML2Utils.getSAMLSettings();
			Auth auth = new Auth(settings, request, response);
			
			String redirectToURL = request.getParameter("url");
			
			if(redirectToURL != null) {
				auth.login(redirectToURL);
			}else {
				auth.login(CFW.Context.App.getApp().getDefaultURL());
			}

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
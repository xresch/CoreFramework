package com.xresch.cfw.features.core.auth.saml;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * The first servlet to call for the SAML authentication.
 * Will trigger the request to the identity provider(IdP).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSAML2Metadata extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletSAML2Metadata.class.getName());
		
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
			// Create Auth 
			Saml2Settings settings = ServletSAML2Utils.getSAMLSettings();
			Auth auth = new Auth(settings, request, response);
			settings = auth.getSettings();
			settings.setSPValidationOnly(true);
			String metadata = settings.getSPMetadata();
			
			//------------------------------------
			// Create Response
			List<String> errors = Saml2Settings.validateMetadata(metadata);
			if (errors.isEmpty()) {
				response.setContentType("text/xml; charset=UTF-8");
				PlaintextResponse plaintext = new PlaintextResponse();
				plaintext.getContent().append(metadata);
			} else {
				response.setContentType("text/html; charset=UTF-8");
				PlaintextResponse plaintext = new PlaintextResponse();
				plaintext.getContent().append(metadata);
				for (String error : errors) {
					plaintext.getContent().append("<p>"+error+"</p>");
				}
			}

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
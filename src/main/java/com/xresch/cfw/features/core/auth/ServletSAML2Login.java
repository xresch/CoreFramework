package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onelogin.saml2.Auth;
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
		
		//------------------------------------
		// Create Settings
//		Map<String, Object> samlData = new HashMap<>();
//		samlData.put("onelogin.saml2.sp.entityid", "http://localhost:8080/java-saml-tookit-jspsample/metadata.jsp");
//		samlData.put("onelogin.saml2.sp.assertion_consumer_service.url", new URL("http://localhost:8080/java-saml-tookit-jspsample/acs.jsp"));
//		samlData.put("onelogin.saml2.security.want_xml_validation",true);
//		samlData.put("onelogin.saml2.sp.x509cert", "myX509CertInstance");
//
//		SettingsBuilder builder = new SettingsBuilder();
//		Saml2Settings settings = builder.fromValues(samlData).build();
//		
		//------------------------------------
		// Create Settings
		Auth auth;
		try {
			auth = new Auth(request, response);
			auth.login();
			//auth.login(request.getContextPath() + "/redirect_servlet");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
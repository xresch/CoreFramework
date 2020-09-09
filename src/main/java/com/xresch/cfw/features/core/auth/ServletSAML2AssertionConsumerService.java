package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.util.Collection;
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
		
		Auth auth;
		try {
			auth = new Auth(request, response);

			auth.processResponse();
	
			if (!auth.isAuthenticated()) {
				new CFWLog(logger).severe("SAML: Not authenticated", new Throwable());
			}
	
			List<String> errors = auth.getErrors();
	
			if (!errors.isEmpty()) {
				new CFWLog(logger).severe("SAML: Errors"+ String.join(", ", errors), new Throwable());
	
				if (auth.isDebugActive()) {
					String errorReason = auth.getLastErrorReason();
					if (errorReason != null && !errorReason.isEmpty()) {
						new CFWLog(logger).severe(auth.getLastErrorReason(), new Throwable());
					}
				}
	
			} else {
				
				Map<String, List<String>> attributes = auth.getAttributes();
				String nameId = auth.getNameId();
				String nameIdFormat = auth.getNameIdFormat();
				String sessionIndex = auth.getSessionIndex();
				String nameidNameQualifier = auth.getNameIdNameQualifier();
				String nameidSPNameQualifier = auth.getNameIdSPNameQualifier();
				
				System.out.println("attributes: "+ attributes);
				System.out.println("nameId: "+ nameId);
				System.out.println("nameIdFormat: "+ nameIdFormat);
				System.out.println("sessionIndex: "+ sessionIndex);
				System.out.println("nameidNameQualifier: "+ nameidNameQualifier);
				System.out.println("nameidSPNameQualifier: "+ nameidSPNameQualifier);
				
	
				String relayState = request.getParameter("RelayState");
	
				if (relayState != null && !relayState.isEmpty() && !relayState.equals(ServletUtils.getSelfRoutedURLNoQuery(request)) &&
					!relayState.contains("/dologin.jsp")) { // We don't want to be redirected to login.jsp neither
					response.sendRedirect(request.getParameter("RelayState"));
				} else {
					
	
					if (!attributes.isEmpty()) {
							
						Collection<String> keys = attributes.keySet();
						for(String name :keys){
							System.out.println("<tr><td>" + name + "</td><td>");
							List<String> values = attributes.get(name);
							for(String value :values) {
								System.out.println("<li>" + value + "</li>");
							}
		
							System.out.println("</td></tr>");
						}
				
					}
	
				}
			}
		} catch (SettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
package com.xresch.cfw.handler;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.SessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.utils.CFWHttp;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class AuthenticationHandler extends HandlerWrapper
{
	private String securePath;
	private String defaultURL;
	
	public AuthenticationHandler(String securePath, String defaultURL) {
		
		this.securePath = securePath;
		this.defaultURL = defaultURL;
			
	}
	
	@Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
		//##################################
    	// Handle unsecured servlets
    	//##################################
    	if(!CFWProperties.AUTHENTICATION_ENABLED) {
    		loginAsAnonymous(target, baseRequest, request, response);
    		return;
    	}
    		
		//##################################
    	// Handle Unsecured servlets
    	//##################################
		if(!request.getRequestURI().toString().startsWith(securePath)) {
			this._handler.handle(target, baseRequest, request, response);
			return;
		}
		
		//##################################
    	// Handle Unsecured servlets
    	//##################################
		if(!request.getRequestURI().toString().startsWith(securePath)) {
			this._handler.handle(target, baseRequest, request, response);
			return;
		}
		
		//##################################
    	// Handle API Token Requests
    	//##################################
		String token = CFW.HTTP.getCFWAPIToken(request);

    	if(!Strings.isNullOrEmpty(token)) {
    		SessionData data = CFW.Context.Request.getSessionData(); 
    		User tokenUser = new User("apitoken["+CFW.Security.maskString(token, 50)+"]");
    		data.setUser(tokenUser);
    		data.isLoggedIn(true);
    		this._handler.handle(target, baseRequest, request, response);
    		
    		//-----------------------------------
    		// Cleanup session setting timout
    		request.getSession().setMaxInactiveInterval(CFW.DB.Config.getConfigAsInt(FeatureUserManagement.CONFIG_SESSIONTIMEOUT_API));
    	}
    	
    	//##################################
    	// Handle Secured Servlets
    	//##################################
		SessionData data = CFW.Context.Request.getSessionData(); 
    	if(data.isLoggedIn()) {
        	//##################################
        	// Call Wrapped Handler
        	//##################################
        	this._handler.handle(target, baseRequest, request, response);
    		
        	AbstractResponse template = CFW.Context.Request.getResponse();
    		if(template instanceof HTMLResponse) {
    			
    			((HTMLResponse)template).addJavascriptData("userid", data.getUser().id());
    		}
    	}else {
    		handleLogin(target, baseRequest, request, response);
    	}
	
    }
	
	
	/**************************************************************************************
	 * Handle the login for the application.
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 **************************************************************************************/
	private void handleLogin( String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response ) throws IOException,
                                          ServletException{
		
		if(request.getRequestURI().toString().endsWith("/login")
		   || request.getRequestURI().toString().contains("/login;jsessionid")) {
			this._handler.handle(target, baseRequest, request, response);
		}else {
			
			String query = "";
			if(request.getQueryString() != null && !request.getQueryString().equals("#")) {
				query = "?"+request.getQueryString();
			}
			if(!CFWProperties.AUTHENTICATION_SAML2_ENABLED) {
				CFW.HTTP.redirectToURL(response, "/app/login?url="+CFW.HTTP.encode(request.getRequestURI()+query));
			}else {
				CFW.HTTP.redirectToURL(response, "/cfw/saml2/login?url="+CFW.HTTP.encode(request.getRequestURI()+query));
			}
		}
	}
	
	/**************************************************************************************
	 * If authentication is disabled log in as anonymous.
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 **************************************************************************************/
	private void loginAsAnonymous( String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response ) throws IOException,
                                          ServletException{
		//---------------------------------
		// Login as anonymous
		if(CFW.Context.Request.getUser() == null) {
			CFW.Context.Request.getSessionData().setUser(CFW.DB.Users.selectByUsernameOrMail("anonymous"));
			CFW.Context.Session.getSessionData().triggerLogin();
		}
		
		this._handler.handle(target, baseRequest, request, response);
    	AbstractResponse template = CFW.Context.Request.getResponse();
		if(template instanceof HTMLResponse) {
			((HTMLResponse)template).addJavascriptData("userid", CFW.Context.Request.getUser().id());
		}
	}
}
package com.pengtoolbox.cfw.handlers;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.response.AbstractResponse;
import com.pengtoolbox.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class AuthenticationHandler extends HandlerWrapper
{
	private String securePath;
	private String defaultURL;
	
	public AuthenticationHandler(String securePath, String defaultURL) {
		
		this.securePath = securePath;
		this.defaultURL = defaultURL;
			
	}

    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
    	
    	if(CFWProperties.AUTHENTICATION_ENABLED) {
    		
    		//##################################
        	// Handle unsecured servlets
        	//##################################
    		String uri = request.getRequestURI().toString();

    		if(!request.getRequestURI().toString().startsWith(securePath)) {
    			this._handler.handle(target, baseRequest, request, response);
    			return;
    		}
    		
    		
    		//##################################
        	// Get Session
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
        		if(request.getRequestURI().toString().endsWith("/login")
        		   || request.getRequestURI().toString().contains("/login;jsessionid")) {
        			this._handler.handle(target, baseRequest, request, response);
        		}else {
        			String query = "";
        			if(request.getQueryString() != null && !request.getQueryString().equals("#")) {
        				query = "?"+request.getQueryString();
        			}
        			CFW.HTTP.redirectToURL(response, "/app/login?url="+URLEncoder.encode(request.getRequestURI()+query));
        		}
        	}
	
    	}else {
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
}
package com.xresch.cfw.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import com.xresch.cfw._main.CFW;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class RedirectDefaultPageHandler extends HandlerWrapper {
	private String defaultURL;
	
	public RedirectDefaultPageHandler(String defaultURL) {
		this.defaultURL = defaultURL;
	}
	@Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
		
		//##################################
    	// Handle unsecured servlets
    	//##################################
		
		String uri = request.getRequestURI().toString();

		if(uri.equals("")
		|| uri.equals("/")
		|| uri.equals("/app")
		|| uri.equals("/app/")) {
			CFW.HTTP.redirectToURL(response, defaultURL);
			baseRequest.setHandled(true);
			return;
		}else {
			if(_handler != null) {
				this._handler.handle(target, baseRequest, request, response);
			}
		}
		
		
    }
	
}

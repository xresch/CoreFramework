package com.xresch.cfw.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;

import com.xresch.cfw._main.CFWProperties;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class HTTPSRedirectHandler extends SecuredRedirectHandler {

	@Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
		if(CFWProperties.HTTP_ENABLED && CFWProperties.HTTP_REDIRECT_TO_HTTPS) {
			super.handle(target, baseRequest, request, response);
		}
    }
	
}

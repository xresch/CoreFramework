package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletShutdown extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
	
		String token = request.getParameter("token");
		
		if(token != null && token.contentEquals(CFW.Properties.APPLICATION_ID)) {
			
			CFWApplicationExecutor executor = CFW.Context.App.getApp();
			
			new Thread() {
				 public void run()
		            {
		                executor.stop(); 
		            }
			}.start();
			
		}
    }
}
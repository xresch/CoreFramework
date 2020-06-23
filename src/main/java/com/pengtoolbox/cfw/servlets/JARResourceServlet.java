package com.pengtoolbox.cfw.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class JARResourceServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		String pkg = request.getParameter("pkg");		
		String file = request.getParameter("file");
		
		
		byte[] fontContent = CFW.Files.readPackageResourceAsBytes(pkg, file);
		if(fontContent != null) {
			
			response.addHeader("Cache-Control", "max-age="+CFWProperties.BROWSER_RESOURCE_MAXAGE);
			response.setStatus(HttpServletResponse.SC_OK);
			
			response.getOutputStream().write(fontContent);

	        //response.setContentType("application/font-"+fontType);
	        
	        
	        
	    }else {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    }
		//done by RequestHandler
		//CFW.writeLocalized(request, response);
    }
}
package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletJARResource extends HttpServlet
{
	private static final Logger logger = CFWLog.getLogger(ServletJARResource.class.getName());
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException                              
    {
		String pkg = request.getParameter("pkg");		
		String file = request.getParameter("file");
		
		
		byte[] fontContent = CFW.Files.readPackageResourceAsBytes(pkg, file);
		if(fontContent != null) {
			
			response.addHeader("Cache-Control", "max-age="+CFW.DB.Config.getConfigAsInt(FeatureCore.CONFIG_BROWSER_RESOURCE_MAXAGE));
			response.setStatus(HttpServletResponse.SC_OK);
			
			try {
				response.getOutputStream().write(fontContent);
			}catch(IOException e) {
				new CFWLog(logger)
					.severe("Error writing response.", e);
			}
	        //response.setContentType("application/font-"+fontType);
	        
	        
	        
	    }else {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    }
		//done by RequestHandler
		//CFW.writeLocalized(request, response);
    }
}
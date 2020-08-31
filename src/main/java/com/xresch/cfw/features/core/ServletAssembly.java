package com.xresch.cfw.features.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletAssembly extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {

		//-----------------------
		// Set Cache Control
		response.addHeader("Cache-Control", "max-age="+CFWProperties.BROWSER_RESOURCE_MAXAGE);
		
		//-----------------------
		// Fetch Assembly
		String assemblyName = request.getParameter("name");
		PlaintextResponse plain = new PlaintextResponse();
		
		if(FileAssembly.hasAssembly(assemblyName)) {
			FileAssembly assembly = FileAssembly.getAssemblyFromCache(assemblyName);
			
			//-----------------------
			// Check ETag
			String etag = request.getHeader("If-None-Match");
			// gzip handler will append "--gzip", therefore check on starts with
			if(etag != null && etag.startsWith(""+assembly.getEtag())) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			//-----------------------
			// Return Assembly
			plain.getContent().append(assembly.getAssemblyContent());
			response.addHeader("ETag", ""+assembly.getEtag());
			
	        response.setContentType(assembly.getContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        
	    }else {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    }
		
		//Done by RequestHandler
		//CFW.writeLocalized(request, response);
    }
}
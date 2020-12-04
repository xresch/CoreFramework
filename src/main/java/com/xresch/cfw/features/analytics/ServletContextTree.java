package com.xresch.cfw.features.analytics;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletContextTree extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS)) {
			
			HTMLResponse html = new HTMLResponse("Servlet Context Tree");
			StringBuilder content = html.getContent();

			
			//------------------------------
			// Add Content

			content.append("<h1>Servlet Context Tree</h1>");		
			content.append("<div>");	
			
			content.append(
					CFW.Context.App.getApp()
					.dumpServletContext()
					.replaceAll("\\r\\n|\\n", "<br>")
					.replaceAll(" ", "&nbsp;"));
			content.append("</div>");	

	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
}
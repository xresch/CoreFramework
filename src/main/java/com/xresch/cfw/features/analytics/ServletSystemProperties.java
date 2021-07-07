package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSystemProperties extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			HTMLResponse html = new HTMLResponse("System Properties");
			StringBuilder content = html.getContent();

			
			//------------------------------
			// Add Content
			content.append("<h1>System Properties</h1>");		
			content.append("<div class=\"table-responsive\">");	
			
			content.append("<table class=\"table table-sm table-striped\">");
			content.append("<thead><tr><th>Name</th><th>Name</th></tr></thead>");
			content.append("<tbody>");
				for(Entry<Object, Object> entry : System.getProperties().entrySet()) {
					
					content.append("<tr>")
						.append("<td>"+entry.getKey()+"</td>")
						.append("<td>"+entry.getValue()+"</td>")
					.append("</tr>");
				}
			content.append("</tbody></table>");
			
	        response.setStatus(HttpServletResponse.SC_OK);
		
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
}
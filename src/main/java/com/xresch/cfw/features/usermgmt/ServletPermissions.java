package com.xresch.cfw.features.usermgmt;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletPermissions extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletPermissions.class.getName());
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		String doAudit = request.getParameter("audit");
		
		if(Strings.isNullOrEmpty(doAudit)) {
	    	JSONResponse json = new JSONResponse();
	    	
	    	StringBuilder nameArray = new StringBuilder("[");
	    	for(String permissionName : CFW.Context.Request.getUserPermissions().keySet()) {
	    		nameArray.append("\"").append(permissionName).append("\",");
	    	}
	    	//remove last comma
	    	if(nameArray.length() > 1) { nameArray.deleteCharAt(nameArray.length()-1);}
	    	
	    	nameArray.append("]");
	
	    	json.getContent().append(nameArray.toString());
	    	
	    	response.setStatus(200);
		}else {
			
			HTMLResponse html = new HTMLResponse("User Permissions");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_common.js");
						
			
			html.addJavascriptCode("cfw_usermgmt_auditUser(" + CFW.Context.Request.getUserID() + ", true);");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		}
    }
	
}
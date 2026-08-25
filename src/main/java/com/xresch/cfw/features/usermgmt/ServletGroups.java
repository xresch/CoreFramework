package com.xresch.cfw.features.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class ServletGroups extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		HTMLResponse html = new HTMLResponse("Groups");
		
		if(CFW.Context.Request.hasPermission(FeatureUserManagement.PERMISSION_GROUPS_USER)) {
			
			ServletUserManagementAPI.makeCreateGroupForm(true);
			
			FeatureSpaces.addSpacesCommonJS(html);
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_common.js");
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_groups.js");
						
			html.addJavascriptCode("cfw_usermgmt_groups_initialDraw();");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
}
package com.xresch.cfw.features.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.PasswordValidator;

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
			
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_common.js");
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_groups.js");
						
			html.addJavascriptCode("cfw_usermgmt_groups_initialDraw({tab: 'mygroups'});");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
}
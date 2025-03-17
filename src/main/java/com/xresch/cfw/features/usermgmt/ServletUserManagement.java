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
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.PasswordValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletUserManagement extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		HTMLResponse html = new HTMLResponse("User Management");
		
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(FeatureUserManagement.PERMISSION_USER_MANAGEMENT)) {
			
			ServletUserManagementAPI.makeCreateGroupForm(false);
			ServletUserManagementAPI.makeCreateRoleForm();
			ServletUserManagementAPI.makeCreateUserForm();
			
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_common.js");
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_admin.js");
			
			content.append(CFW.Files.readPackageResource(FeatureUserManagement.PACKAGE_RESOURCE, "cfw_usermgmt_admin.html"));
			
			html.addJavascriptCode("cfw_usermgmt_initialDraw({tab: 'users'});");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
}
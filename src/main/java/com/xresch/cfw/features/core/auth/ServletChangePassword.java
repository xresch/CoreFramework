package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletChangePassword extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletChangePassword.class.getName());
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLog log = new CFWLog(logger).method("doGet");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("Login");
		StringBuilder content = html.getContent();
		content.append(CFW.Files.readPackageResource(FeatureCore.RESOURCE_PACKAGE + ".html", "changepassword.html"));
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
    }
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		CFWLog log = new CFWLog(logger).method("doPost");
		log.info(request.getRequestURL().toString());
		
		//--------------------------
		// Get passwords
		String oldpassword = request.getParameter("oldpassword");
		String newpassword = request.getParameter("newpassword");
		String repeatpassword = request.getParameter("repeatpassword");
		
		if(oldpassword == null || oldpassword.isEmpty()
		|| newpassword == null || newpassword.isEmpty()
		|| repeatpassword == null || repeatpassword.isEmpty()) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Please Provide a value in each password field.");
		}else {
			User currentUser = CFW.Context.Request.getUser();
			
			if(currentUser.changePassword(oldpassword, newpassword, repeatpassword)){
				boolean isUpdateSuccessful = CFW.DB.Users.update(currentUser);
				if(isUpdateSuccessful) {
					CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Password changed successfully.");
				}
			}
		}
		
		HTMLResponse html = new HTMLResponse("Change Password");
		StringBuilder content = html.getContent();
		content.append(CFW.Files.readPackageResource(FeatureCore.RESOURCE_PACKAGE + ".html", "changepassword.html"));
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
	}
}
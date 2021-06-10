package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletLogin extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletLogin.class.getName());
	
	
	protected void createLoginPage( HttpServletRequest request, HttpServletResponse response ) {
		HTMLResponse html = new HTMLResponse("Login");
		StringBuilder content = html.getContent();
		
		String loginHTML = CFW.Files.readPackageResource(FeatureCore.RESOURCE_PACKAGE + ".html", "login.html");
		
		String url = request.getParameter("url");
		url = CFW.Security.sanitizeHTML(url);
		
		if(url == null) { url = "";}
		loginHTML = loginHTML.replace("urlvalue", url);
		
		content.append(loginHTML);
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		createLoginPage(request, response);
        
    }
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//--------------------------
		// Get Credentials
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		
		//--------------------------
		// Check authorization
		if(username == null || password == null){
			
			CFWContextRequest.addAlertMessage(MessageType.ERROR, "Please specify username and password.");
			createLoginPage(request, response);
			return; 
			
		}else {
			User user = LoginUtils.checkCredentials(username, password);
			
			String redirectTo = request.getParameter("url"); 
			boolean loginSuccess = LoginUtils.loginUserAndCreateSession(request, response, user, redirectTo);
			if(!loginSuccess) {
				//Login Failure
				createLoginPage(request, response);
				CFWContextRequest.addAlertMessage(MessageType.ERROR, "Username or password invalid.");
			}
		}
		
		

	}
}
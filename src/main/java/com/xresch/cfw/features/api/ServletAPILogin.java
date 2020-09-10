package com.xresch.cfw.features.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw._main.SessionData;
import com.xresch.cfw.features.core.auth.LoginUtils;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletAPILogin extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletAPILogin.class.getName());
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PlaintextResponse plaintext = new PlaintextResponse();
		plaintext.getContent().append("ERROR: GET method is not supported. Please use POST instead.");
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

		PlaintextResponse plaintext = new PlaintextResponse();
		//--------------------------
		// Check authorization
		if(username == null || password == null){
			
			plaintext.getContent().append("ERROR: Please specify username and password as post parameters.");
			return; 
			
		}else {
			User user = LoginUtils.checkCredentials(username, password);
			
			if(user != null) {
				
				if(user.status() != null && user.status().toUpperCase().equals("ACTIVE")) {
					//--------------------------------
					//Login success
					SessionData data = CFW.Context.Request.getSessionData(); 
					data.resetUser();
					data.setUser(user);
					
					//--------------------------------
					// Create session in other context
					if(CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)) {
						data.triggerLogin();
						plaintext.getContent().append("CFWSESSIONID="+CFW.Context.Request.getRequest().getSession().getId());
					}else {
						plaintext.getContent().append("ERROR: Access Denied.");
					}
					return; 
				}
				
				
			}else {
				plaintext.getContent().append("ERROR: Access is Denied.");
			}
			
		}
		
		//Login Failure
		plaintext.getContent().append("ERROR: Username or password invalid.");

	}
}
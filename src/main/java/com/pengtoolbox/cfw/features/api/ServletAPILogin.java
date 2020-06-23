package com.pengtoolbox.cfw.features.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.login.LoginFacade;
import com.pengtoolbox.cfw.response.PlaintextResponse;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletAPILogin extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletAPILogin.class.getName());
	
	public ServletAPILogin() {
	
	}
	
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
		
		CFWLog log = new CFWLog(logger).method("doPost");
		log.info(request.getRequestURL().toString());
		
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
			User user = LoginFacade.getInstance().checkCredentials(username, password);
			
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
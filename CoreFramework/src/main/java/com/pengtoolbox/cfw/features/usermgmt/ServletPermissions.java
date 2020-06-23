package com.pengtoolbox.cfw.features.usermgmt;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletPermissions extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger logger = CFWLog.getLogger(ServletPermissions.class.getName());
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
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
    }
	
}
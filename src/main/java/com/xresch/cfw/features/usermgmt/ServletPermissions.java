package com.xresch.cfw.features.usermgmt;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletPermissions extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletPermissions.class.getName());
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
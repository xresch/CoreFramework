package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWRandom.RandomDataType;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;
import com.xresch.cfw.utils.web.CFWModifiableHTTPRequest;

/**************************************************************************************************************
 * This servlet is used to handle forms that have a BTFormHandler defined.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletRandom extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletRandom.class.getName());
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		JSONResponse json = new JSONResponse();
		
		//----------------------------
		// Determine Records
		String recordsString = request.getParameter("records");
		 
		int records = 100;
		if( ! Strings.isNullOrEmpty(recordsString) ) { 
			records = Integer.parseInt(recordsString); 
		}
		
		//----------------------------
		// Determine Series
		String seriesString = request.getParameter("seriesCount");
		 
		int seriesCount = 3;
		if( ! Strings.isNullOrEmpty(seriesString) ) { 
			seriesCount = Integer.parseInt(seriesString); 
		}
		
		
		//----------------------------
		// Get type
		String type = request.getParameter("type");
		
		
		if(  Strings.isNullOrEmpty(type) ) { 
			type = "default";
		}
		
		type = type.trim().toUpperCase();
		if( !RandomDataType.has(type) ) {
			CFW.Messages.addErrorMessage(": Unknown type '"+type+"'");
			json.setSuccess(false);
			return;
		}
		
		RandomDataType dataType = RandomDataType.valueOf(type);
		
		//----------------------------
		// Determine latest
		String latestString = request.getParameter("latest");
		 
		long latest = System.currentTimeMillis();
		if( ! Strings.isNullOrEmpty(latestString) ){ 
			latest = Long.parseLong(latestString); 
		}
		
		//----------------------------
		// Determine earliest
		String earliestString = request.getParameter("earliest");
		 
		long earliest = CFWTimeUnit.h.offset(latest, -1);
		if( ! Strings.isNullOrEmpty(earliestString) ){ 
			earliest = Long.parseLong(earliestString); 
		}
		

		//----------------------------
		// Generate Random Data
		json.setPayload( 
				CFW.Random.records(records, dataType, seriesCount, earliest, latest)
			);
				
		
    	
    }
	
}
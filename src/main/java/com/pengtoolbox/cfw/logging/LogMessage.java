package com.pengtoolbox.cfw.logging;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;


/**************************************************************************************************************
 * This class represents a log message.
 * It helps to make the logging asynchronous.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class LogMessage {
	
	protected long starttimeNanos = -1;
	protected long endtimeNanos = -1;
	protected long durationMillis = -1;
	protected long deltaStartMillis = -1;

	protected HttpServletRequest request; 
	protected String webURL;
	protected String queryString;
	protected String requestID;
	protected String userID = "unknown";
	protected int estimatedResponseSizeChars;
	
	protected String sessionID;
	
	protected String sourceClass;
	protected String sourceMethod;
	
	protected String exception;
	
	protected LinkedHashMap<String,String> customEntries = null;
	
	protected LogMessage(CFWLog log) {
		
		this.starttimeNanos = log.starttimeNanos;
		this.endtimeNanos = log.endtimeNanos;
		this.durationMillis = log.durationMillis;
		this.deltaStartMillis = log.deltaStartMillis;

		this.request = log.request; 
		this.webURL = log.webURL; 
		this.queryString = log.queryString; 
		this.requestID = log.requestID; 
		this.userID = log.userID; 
		this.estimatedResponseSizeChars = log.estimatedResponseSizeChars; 
		
		this.sessionID = log.sessionID; 
		
		this.sourceClass = log.sourceClass; 
		this.sourceMethod = log.sourceMethod; 
		
		this.exception = log.exception; 
		
		this.customEntries = log.customEntries;
	}

}

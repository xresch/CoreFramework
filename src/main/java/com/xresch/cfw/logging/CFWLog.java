package com.xresch.cfw.logging;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw._main.SessionData;
import com.xresch.cfw.response.AbstractHTMLResponse;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWLog {
	
	protected Logger logger;
	
	private static boolean isLoggingInitialized = false;
	
	protected long tempStartNanos = -1;
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
	protected boolean silent = false;

	public CFWLog(Logger logger){
		this.logger = logger;
	}
	
	public CFWLog method(String method){
		
		this.sourceMethod = method;
		
		return this;
	}
	
	public CFWLog custom(String key, String value) {
		
		if(customEntries == null) {
			customEntries = new LinkedHashMap<String,String>();
		}
		
		customEntries.put(key, value);
		
		return this;
		
	}

	/***********************************************************************
	 * Make the next log silent. No messages will be propagated to the UI.
	 ***********************************************************************/
	public CFWLog silent(boolean isSilent) {
		this.silent = isSilent;
		return this;
	}
	

	/***********************************************************************
	 * Initializes the logging.
	 ***********************************************************************/
	public static void initializeLogging() {
		File logFolder = new File("./log");
		if(!logFolder.isDirectory()) {
			logFolder.mkdir();
		}
		
		System.setProperty("java.util.logging.config.file", "./config/logging.properties");
		
		//-------------------------------------------
		// Make sure the config is loaded
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("./config/logging.properties"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isLoggingInitialized = true;
		
	}
	
	/***********************************************************************
	 * Returns a Logger.
	 *  
	 * @return Logger 
	 *   
	 ***********************************************************************/
	public static Logger getLogger(String name){
		
		if(!isLoggingInitialized) {
			initializeLogging();
		}
		
		return Logger.getLogger(name);
	}
	
	
	/***********************************************************************
	 * Starts a duration measurement, to end the measurement and write a 
	 * duration log call end().
	 * This measurement can only be nested if you use different Instances of 
	 * OMLogger.
	 *  
	 * @return OMLogger this instance
	 *   
	 ***********************************************************************/
	public CFWLog start(){
		
		//save to temp variable to not mess up calls to other log methods 
		//than end()
		tempStartNanos = System.nanoTime();
		return this;
	}
	
	/***********************************************************************
	 * Starts a duration measurement with custom starttime, to end the 
	 * measurement and write a duration log call end().
	 * This measurement can only be nested if you use different Instances of 
	 * OMLogger.
	 *  
	 * @return OMLogger this instance
	 *   
	 ***********************************************************************/
	public CFWLog start(long startNanos){
		
		//save to temp variable to not mess up calls to other log methods 
		//than end()
		tempStartNanos = startNanos;
		return this;
	}
	
	/***********************************************************************
	 * Ends a measurement and logs a duration log with level INFO.
	 * 
	 ***********************************************************************/
	public void end(){
		
		//
		starttimeNanos = tempStartNanos;
		this.log(Level.INFO, "Duration Log", null);
				
	}
	
	/***********************************************************************
	 * Ends a measurement and logs a duration log with level INFO.
	 * 
	 ***********************************************************************/
	public void end(String message){
		
		//
		starttimeNanos = tempStartNanos;
		this.log(Level.INFO, message, null);
				
	}
	
	public void all(String message){this.log(Level.ALL, message, null);}
	public void config(String message){this.log(Level.CONFIG, message, null);}
	public void finest(String message){this.log(Level.FINEST, message, null);}
	public void finer(String message){this.log(Level.FINER, message, null);}
	public void fine(String message){this.log(Level.FINE, message, null);}
	public void info(String message){this.log(Level.INFO, message, null);}
	
	public void warn(String message){this.log(Level.WARNING, message, null);}
	public void warn(String message, Throwable throwable){this.log(Level.WARNING, message, throwable);}
	
	public void severe(String message){this.log(Level.SEVERE, message, null);}
	public void severe(String message, Throwable e){this.log(Level.SEVERE, message, e);}

	/***********************************************************************
	 * This is the main log method that handles all the logging.
	 * 
	 ***********************************************************************/
	public void log(Level level, String message, Throwable throwable){
		//check logging level before proceeding
		if(logger != null && logger.isLoggable(level)){
			
			//-------------------------
			// Calculate Time
			//-------------------------
			endtimeNanos = System.nanoTime();
			
			if(starttimeNanos != -1){
				durationMillis = (endtimeNanos - starttimeNanos) / 1000000;
			}
			
			this.sourceClass = logger.getName();
			
			//-------------------------
			// Handle Throwable
			//-------------------------
			if(throwable != null){
				
				StringBuffer buffer = new StringBuffer();
				buffer.append(throwable.getClass());
				buffer.append(": ");
				buffer.append(throwable.getMessage());
				
				for(StackTraceElement element : throwable.getStackTrace()){
					buffer.append(" <br/>  at ");
					buffer.append(element);
				}
				
				this.exception = buffer.toString();
			
			}
			
			//-------------------------
			// Handle Request
			//-------------------------
			request = CFW.Context.Request.getRequest();
			if(request != null){

				this.webURL = request.getRequestURI();
				this.requestID = (String)request.getAttribute(CFW.REQUEST_ATTR_ID);
				this.queryString = request.getQueryString();
				this.sessionID = request.getSession().getId();
				
				if(CFWProperties.AUTHENTICATION_ENABLED) {
					SessionData data = CFW.Context.Request.getSessionData(); 
					if(data.isLoggedIn()) {
						this.userID = data.getUser().username();
					}
				}else {
					this.userID = "anonymous";
				}
					
				//--------------------------------
				// Delta Start
				if(starttimeNanos != -1){
					this.deltaStartMillis = (starttimeNanos - (long)request.getAttribute(CFW.REQUEST_ATTR_STARTNANOS) ) / 1000000;
				}else{
					this.deltaStartMillis = (endtimeNanos - (long)request.getAttribute(CFW.REQUEST_ATTR_STARTNANOS) ) / 1000000;
				}
				
				//----------------------------------------
				// Current response size
				AbstractResponse template = CFW.Context.Request.getResponse();
				if(template != null){
					this.estimatedResponseSizeChars = template.getEstimatedSizeChars();
				}
				
				//----------------------------------------
				// Handle alert messages
				if(!silent 
				&& (level.equals(Level.SEVERE) || level.equals(Level.WARNING)) ){
					
					MessageType alertType = ( level == Level.SEVERE ? MessageType.ERROR : MessageType.WARNING );
					CFW.Context.Request.addAlertMessage(alertType, message);
					
					if(template instanceof AbstractHTMLResponse){
						if(this.exception != null){
							((AbstractHTMLResponse)template).addSupportInfo("Exception: ", this.exception);
						}
					}
				}
				
			}
			
			//-------------------------
			// Log Message
			//-------------------------
			logger.logp(level, sourceClass, sourceMethod, message, new LogMessage(this));
				
		}
		
		//-------------------------
		// Reset
		//-------------------------
		reset();
	}
	
	protected void reset() {
		this.exception = null;
		this.starttimeNanos = -1;
		this.durationMillis = -1;
		this.deltaStartMillis = -1;
		this.customEntries = null;
	}
	
	

}

package com.xresch.cfw.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.CLI;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.logging.SysoutInterceptor.SysoutType;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWLog {
		
	protected Logger logger;
	
	private static boolean isLoggingInitialized = false;
	
	protected boolean isMinimal = false;
	protected boolean isContextless = false;
	protected long tempStartMillis = -1;
	protected long starttimeMillis = -1;
	protected long endtimeMillis = -1;
	protected long durationMillis = -1;
	protected long deltaStartMillis = -1;
	protected int estimatedResponseSizeChars = -1;
	
	protected HttpServletRequest request; 
	protected String webURL = "";
	protected String queryString = "";
	protected String requestID  = "";
	protected String userID = "";

	
	protected String sessionID  = "";
	protected String sourceClass  = "";
	protected String sourceMethod  = "";
	
	protected String exception;
	
	protected LinkedHashMap<String,String> customEntries = null;
	protected boolean silent = false;

	
	private static final Counter logCounter = Counter.build()
	         .name("cfw_logs_total")
	         .help("Number of log events occured.")
	         .labelNames("level")
	         .register();
	
	// private static final HashMap<Level, Counter> levelCounters = new HashMap<>();
	
	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public CFWLog(Logger logger){
		this.logger = logger;
		
		if(logger != null){
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StackTraceElement cfwLogInstantiatingMethod = stacktrace[2];
			sourceMethod = cfwLogInstantiatingMethod.getMethodName();
			sourceClass = cfwLogInstantiatingMethod.getClassName();
		}
		
	}
	
	/***********************************************************************
	 * Change the auto detected source method to a custom value.
	 ***********************************************************************/
	public CFWLog method(String method){
		
		this.sourceMethod = method;
		return this;
	}
	
	/***********************************************************************
	 * Change the auto detected source class to a custom value.
	 ***********************************************************************/
	public CFWLog clazz(String clazz){
		
		this.sourceClass = clazz;
		return this;
	}
	
	/***********************************************************************
	 * Add a custom field to the log. The objects "value.toString()" method
	 * will be used to get the value.
	 ***********************************************************************/
	public CFWLog custom(String key, Object value) {
		
		if(customEntries == null) {
			customEntries = new LinkedHashMap<String,String>();
		}
		
		if(value != null) {
			customEntries.put(key, value.toString());
		}else {
			customEntries.put(key, "");
		}
		
		
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
	 * Make the next log minimal. Only the timestamp, level and the message will be
	 * printed.
	 ***********************************************************************/
	public CFWLog minimal(boolean isMinimal) {
		this.isMinimal = isMinimal;
		return this;
	}
	
	/***********************************************************************
	 * ignores request based data. useful for batch jobs started by user
	 * but then running in the background.
	 ***********************************************************************/
	public CFWLog contextless(boolean isContextless) {
		this.isContextless = isContextless;
		return this;
	}

	/***********************************************************************
	 * Initializes the logging.
	 ***********************************************************************/
	@SuppressWarnings("resource")
	public static void initializeLogging() {
		
		//-------------------------------------------
		// Create log Directory
		String logfolder = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_LOGFOLDER);

		File logFolder = new File(logfolder);
		
		if(!logFolder.isDirectory()) {
			logFolder.mkdirs();
		}
		
		//-------------------------------------------
		// Add Interceptors
		// Causes Deadlock, to be investigated
	    //new SysoutInterceptor(SysoutType.SYSOUT, System.out);
	    //new SysoutInterceptor(SysoutType.SYSERR, System.err);

		//-------------------------------------------
		// Set Properties Path
	    String configPath = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER)+File.separator+"logging.properties";
	    String defaultConfigPath = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER_DEFAULT)+File.separator+"logging.properties";
	    
	    if(!configPath.equals(defaultConfigPath)) {
	    	if(!CFW.Files.isFile(configPath)) {
	    		try {
	    			new File(configPath).mkdirs();
	    			
					Files.copy(Paths.get(defaultConfigPath), 
							Paths.get(configPath), 
							StandardCopyOption.REPLACE_EXISTING);
					
				} catch (IOException e) {
					System.err.println("Couldn't copy logging.properties");
				}
	    	}
	    }
	    
		System.setProperty("java.util.logging.config.file", configPath);
		
		//-------------------------------------------
		// Make sure the config is loaded
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream(configPath));
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
		tempStartMillis = System.currentTimeMillis();
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
	public CFWLog start(long startMillis){
		
		//save to temp variable to not mess up calls to other log methods 
		//than end()
		tempStartMillis = startMillis;
		return this;
	}
	
	/***********************************************************************
	 * Ends a measurement and logs a duration log with level FINE.
	 * 
	 ***********************************************************************/
	public void end(){
		
		//
		starttimeMillis = tempStartMillis;
		this.log(Level.FINE, "Duration[ms]", null);
				
	}
	
	/***********************************************************************
	 * Ends a measurement and logs a duration log with the specified level.
	 * 
	 ***********************************************************************/
	public void end(Level level){
		starttimeMillis = tempStartMillis;
		this.log(level, "Duration[ms]", null);		
	}
	
	/***********************************************************************
	 * Ends a measurement and logs a duration log with the specified level
	 * and message.
	 * 
	 ***********************************************************************/
	public void end(Level level, String message){
		
		starttimeMillis = tempStartMillis;
		this.log(level, message, null);
				
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
	public void off(String message){this.log(Level.OFF, message, null);}
	
	
	/***********************************************************************
	 * Create an audit entry with level OFF to always log the audit logs.
	 * @param action the action for the audit message(e.g. CREATE, UPDATE, DELETE)
	 * @param itemClass the class of the CFWObject
	 * @param message the log message, for example details about the affected item 
	 ***********************************************************************/
	public void audit(String action, Class<? extends CFWObject> itemClass, String message){
		this.custom("auditAction", action);
		this.custom("auditItem", itemClass.getSimpleName());
		this.log(Level.OFF, message, null);
	}
	/***********************************************************************
	 * Create an audit entry with level OFF to always log the audit logs.
	 * @param action the action for the audit message(e.g. CREATE, UPDATE, DELETE)
	 * @param item the item affected by the action (e.g. User, Role, Dashboard...)
	 * @param message the log message, for example details about the affected item 
	 ***********************************************************************/
	public void audit(String action, String item, String message){
		this.custom("auditAction", action);
		this.custom("auditItem", item);
		this.log(Level.OFF, message, null);
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public void audit(String auditAction, CFWObject object, String[] auditLogFieldnames) {
		
		if(auditLogFieldnames != null) {
			StringBuilder logMessage = new StringBuilder();
			for(String fieldname : auditLogFieldnames) {
				logMessage
					.append(fieldname+": ")
					.append(object.getField(fieldname).getValue())
					.append(", ");
			}
			
			audit(auditAction, 
					object.getClass().getSimpleName(), 
					logMessage.substring(0, logMessage.length()-2));
		}	
	}
	
	/***********************************************************************
	 * This is the main log method that handles all the logging.
	 * 
	 ***********************************************************************/
	public void log(Level level, String message, Throwable throwable){
		//check logging level before proceeding
		if(logger != null && logger.isLoggable(level)){
			logCounter.labels("TOTAL").inc();
			logCounter.labels(level.toString()).inc();
			//-------------------------
			// Calculate Time
			//-------------------------
			endtimeMillis = System.currentTimeMillis();
			
			if(starttimeMillis != -1){
				durationMillis = (endtimeMillis - starttimeMillis);
			}
			
			//-------------------------
			// Handle Throwable
			//-------------------------
			if(throwable != null){
				
				StringBuilder buffer = new StringBuilder();
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
			if(!isContextless) {
				request = CFW.Context.Request.getRequest();
			}
			
			if(request != null){

				this.webURL = request.getRequestURI();
				this.requestID = (String)request.getAttribute(CFW.REQUEST_ATTR_ID);
				this.queryString = request.getQueryString();
				// Get session id from context, as following might create a StackOverflow on log level FINE: request.getSession().getId();
				this.sessionID = CFW.Context.Session.getSessionID();
				

				CFWSessionData data = CFW.Context.Request.getSessionData(); 
				if(data != null && data.isLoggedIn()) {
					this.userID = data.getUser().username();
				}

				//--------------------------------
				// Delta Start
				long requestStartMillis = CFW.Context.Request.getRequestStartMillis();
				if(starttimeMillis != -1){
					this.deltaStartMillis = (starttimeMillis - requestStartMillis);
				}else{
					this.deltaStartMillis = (endtimeMillis - requestStartMillis );
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
		this.starttimeMillis = -1;
		this.durationMillis = -1;
		this.deltaStartMillis = -1;
		this.customEntries = null;
	}
	
	

}

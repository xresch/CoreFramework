package com.xresch.cfw.handler;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.Context;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class RequestHandler extends HandlerWrapper
{
	private static Logger logger = CFWLog.getLogger(RequestHandler.class.getName());
		
	/**********************************************************
	 * 
	 **********************************************************/
	@Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
    	
    	//==========================================
    	// Initialize
    	//==========================================
    	
		// Used to calculate deltaStart by OMLogger.log()
    	// minus 1ms to be always first
    	long startMillis = System.currentTimeMillis()-1;
    	CFW.Context.Request.setRequestStartMillis(startMillis);
    	
    	CFWLog log = new CFWLog(logger).start(startMillis);
    	
    	CFW.Context.Request.setRequest(request);
    	CFW.Context.Request.setHttpServletResponse(response);
    	

    	    	
    	//---------------------------------------
    	//ReqestID used in logging
    	String requestID = request.getHeader(CFW.REQUEST_ATTR_ID);
    	if(requestID == null){
    		requestID = UUID.randomUUID().toString();
    	}
    	
    	request.setAttribute(CFW.REQUEST_ATTR_ID, requestID);
    	
    	//==========================================
    	// Get Session
    	//==========================================
    	HttpSession session = request.getSession();
    	
    	// check outside the loop first to avoid synchronizing when it is not needed
    	if(session.getAttribute(CFW.SESSION_DATA) == null) {
    		CFWSessionData data = new CFWSessionData(session.getId());
    		session.setAttribute(CFW.SESSION_DATA, data);
    	};
    	
    	CFW.Context.Request.setSessionData((CFWSessionData)session.getAttribute(CFW.SESSION_DATA));
    	
    	//==========================================
    	// Set Session Timeout
    	//==========================================
    	// workaround maxInactiveInterval=-1 issue
    	// Do not call getMaxInactiveInterval() if
    	// value unchanged to not make the session
    	// Dirty and not cause store Session to DB
    	int currentInactiveInterval = session.getMaxInactiveInterval();
    	if(CFW.Context.Session.getSessionData().isLoggedIn()) {
    		int sessionTimeout = CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_USERS);
    		if(sessionTimeout != currentInactiveInterval) {
    			session.setMaxInactiveInterval(sessionTimeout);
    		}
    	}else {
    		if(request.getRequestURI().equals("/metrics")) {
    			
    			int sessionTimeout = CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_API);
        		if(sessionTimeout != currentInactiveInterval) {
        			session.setMaxInactiveInterval(sessionTimeout);
        		}
    		}else {
    			int sessionTimeout = CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_VISITORS);
        		if(sessionTimeout != currentInactiveInterval) {
        			session.setMaxInactiveInterval(sessionTimeout);
        		}
    		}
    	}
    	
    	
    	//==========================================
    	// Trace Log
    	//==========================================
    	if(logger.isLoggable(Level.FINEST)) {
    		CFWLog traceLog = new CFWLog(logger);
    		Enumeration<String> headers = request.getHeaderNames();
    		while(headers.hasMoreElements()) {
    			String headerName = headers.nextElement();
    			traceLog.custom(headerName.toLowerCase(), request.getHeader(headerName));
    		}
    			
    		traceLog.finest("Request Headers Tracelog");
    	}
    	
    	//==========================================
    	// Before
    	//==========================================    	
		try {
	    		    		    	
	    	//==========================================
	    	// Call Wrapped Handler
	    	//==========================================
	    	
	    	String userAgent = request.getHeader("User-Agent");
	    	if(userAgent == null
	    	|| ( !userAgent.contains("MSIE ") 
	    		&& !userAgent.contains("Trident/")) ) {
	
	    		this._handler.handle(target, baseRequest, request, response);
	    		
	    	}else {
	    		PlaintextResponse plain = new PlaintextResponse();
	    		plain.getContent().append("This application does not work with Internet Explorer. Please use a modern browser like Chrome, Edge or Safari.");
	    	}
		}catch(Throwable e) {
			//==========================================
			// Handle error Overwriting Response
			//==========================================
			response.setStatus(500);
			new CFWLog(logger).silent(true).severe("Unhandled Exception occured.", e);
		
			this.createErrorResponse(request, e);

		}finally {
	    	//==========================================
	    	// After
	    	//==========================================
	    	CFWDB.forceCloseRemainingConnections();
	    	
	    	if(!request.getRequestURI().startsWith("/stream")
	    	&& !request.getRequestURI().startsWith("/app/stream")
	    	){
	    		CFW.Localization.writeLocalized(request, response);
	    	}
	    	log.end();
    	
	    	baseRequest.setHandled(true);
	    	Context.Request.clearRequestContext();
		}
    }

	/**********************************************************
	 * 
	 **********************************************************/
	private void createErrorResponse(HttpServletRequest request, Throwable e) {
		//AbstractResponse elGrandeResponse = Context.Request.getResponse();
		// if(elGrandeResponse instanceof HTMLResponse) 
		
		HTMLResponse errorResponse = new HTMLResponse("Error Occured");
			
		StringBuilder builder = new StringBuilder();
		builder.append("<h1>Whoops!</h1>");
		builder.append("<p>An unexpected error occured. Here some things you can check or try:</p>");
		builder.append("<ul>");
		builder.append(		"<li><b>Outdated Bookmark:&nbsp;</b> If you used a boockmark check if the link is still valid.</li>");
		builder.append(		"<li><b>Required Permissions:&nbsp;</b> If you clicked a link someone shared with you, you might not have enough permissions.</li>");
		builder.append(		"<li><b>Go to Home:&nbsp;</b>Try to access the home page of the application: <a target=\"_blank\" href=\""+CFW.HTTP.getServerURL(request)+"\">Open Home Page</a></li>");
		builder.append(		"<li><b>Contact Support:&nbsp;</b> Contact your application support and include below error details in your request.</li>");
		builder.append("</ul>");
		
		builder.append("<h1>Error Details</h1>");
		builder.append("<ul>");
		builder.append(		"<li><b>System Time:&nbsp;</b>"+CFW.Time.currentTimestamp()+"</li>");
		builder.append(		"<li><b>Method:&nbsp;</b>"+request.getMethod()+"</li>");
		builder.append(		"<li><b>URL:&nbsp;</b>"+request.getRequestURL()+"</li>");
		builder.append(		"<li><b>Query:&nbsp;</b>"+request.getQueryString()+"</li>");
		if(CFW.Context.Request.getUser() !=null) {
			builder.append(		"<li><b>User:&nbsp;</b>"+CFW.Context.Request.getUser().createUserLabel()+"</li>");
		}
		builder.append(		"<li><b>Session ID:&nbsp;</b>"+CFW.Context.Session.getSessionID()+"</li>");
		builder.append(		"<li><b>Request ID:&nbsp;</b>"+request.getAttribute(CFW.REQUEST_ATTR_ID)+"</li>");
		builder.append(		"<li><b>Message:&nbsp;</b>"+e.getMessage()+"</li>");
		builder.append(		"<li><b>Error Class:&nbsp;</b>"+e.getClass().getName()+"</li>");
		
		builder.append(		"<li><b>Stack Trace:&nbsp;</b>");
			builder.append("<ul>");
			for(StackTraceElement element : e.getStackTrace()) {
				builder.append("<li>"+element.toString()+"</li>");
			}
			builder.append("</ul>");
		builder.append(		"</li>");
		builder.append("</ul>");
		errorResponse.setContent(builder);
	}
	
	
}
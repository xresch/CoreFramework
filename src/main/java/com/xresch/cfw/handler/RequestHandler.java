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
import com.xresch.cfw._main.SessionData;
import com.xresch.cfw._main.SessionTracker;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class RequestHandler extends HandlerWrapper
{
	private static Logger logger = CFWLog.getLogger(RequestHandler.class.getName());
	
	private static Object syncLock = new Object();
	
	@Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
    	
    	//##################################
    	// Initialize
    	//##################################
    	
		// Used to calculate deltaStart by OMLogger.log()
    	// minus 1ms to be always first
    	long startNanos = System.nanoTime()-1000000;
    	CFW.Context.Request.setRequestStartNanos(startNanos);
    	
    	CFWLog log = new CFWLog(logger).start(startNanos);
    	
    	CFW.Context.Request.setRequest(request);
    	CFW.Context.Request.setHttpServletResponse(response);
    	

    	    	
    	//---------------------------------------
    	//ReqestID used in logging
    	String requestID = request.getHeader(CFW.REQUEST_ATTR_ID);
    	if(requestID == null){
    		requestID = UUID.randomUUID().toString();
    	}
    	
    	request.setAttribute(CFW.REQUEST_ATTR_ID, requestID);
    	
    	//##################################
    	// Get Session
    	//##################################
    	HttpSession session = request.getSession();
    	
    	// check outside the loop first to avoid synchronizing when it is not needed
    	if(session.getAttribute(CFW.SESSION_DATA) == null) {
    		SessionData data = new SessionData(session.getId());
    		session.setAttribute(CFW.SESSION_DATA, data);
    	};
    	
    	CFW.Context.Request.setSessionData((SessionData)session.getAttribute(CFW.SESSION_DATA));
    	//workaround maxInactiveInterval=-1 issue
    	session.setMaxInactiveInterval(CFW.Properties.SESSION_TIMEOUT);
    	
    	//##################################
    	// Trace Log
    	//##################################
    	if(logger.isLoggable(Level.FINEST)) {
    		CFWLog traceLog = new CFWLog(logger);
    		Enumeration<String> headers = request.getHeaderNames();
    		while(headers.hasMoreElements()) {
    			String headerName = headers.nextElement();
    			traceLog.custom(headerName.toLowerCase(), request.getHeader(headerName));
    		}
    			
    		traceLog.finest("Request Headers Tracelog");
    	}
    	
    	//##################################
    	// Before
    	//##################################    	
		try {
	    		    		    	
	    	//##################################
	    	// Call Wrapped Handler
	    	//##################################
	    	
	    	String userAgent = request.getHeader("User-Agent");
	    	if(userAgent == null
	    	|| ( !userAgent.contains("MSIE ") 
	    		&& !userAgent.contains("Trident/")) ) {
	
	    		this._handler.handle(target, baseRequest, request, response);
	    		
	    	}else {
	    		PlaintextResponse plain = new PlaintextResponse();
	    		plain.getContent().append("This application does not work with Internet Explorer. Please use a modern browser like Chrome, Edge or Safari.");
	    	}
		}catch(Exception e) {
			new CFWLog(logger).severe("Unhandled Exception occured.", e);
			throw e;
		}finally {
	    	//##################################
	    	// After
	    	//##################################
	    	CFWDB.forceCloseRemainingConnections();
	    	
	    	CFW.Localization.writeLocalized(request, response);
	    	log.end();
    	
	    	baseRequest.setHandled(true);
	    	Context.Request.clearRequestContext();
		}
    }
}
package com.xresch.cfw.features.core.acme;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

//@WebServlet("/.well-known/acme-challenge/*")
public class ACMEChallengeServlet extends HttpServlet {
   
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(CFW.class.getName());
	
    /******************************************************************
     * 
     ******************************************************************/
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
    	new CFWLog(logger).info("ACME: Challenge Request received. ");
    	
    	String token = req.getPathInfo().substring(1);
        String response = CFWACMEClient.getChallenges().get(token);
        if (response != null) {
            resp.setContentType("text/plain");
            resp.getWriter().write(response);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    

}
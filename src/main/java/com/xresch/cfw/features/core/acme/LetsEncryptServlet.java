package com.xresch.cfw.features.core.acme;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@WebServlet("/.well-known/acme-challenge/*")
public class LetsEncryptServlet extends HttpServlet {
   

    /******************************************************************
     * 
     ******************************************************************/
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
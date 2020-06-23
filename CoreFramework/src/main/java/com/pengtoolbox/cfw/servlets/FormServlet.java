package com.pengtoolbox.cfw.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;

/**************************************************************************************************************
 * This servlet is used to handle forms that have a BTFormHandler defined.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FormServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger logger = CFWLog.getLogger(FormServlet.class.getName());
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		String id = request.getParameter("id");
		String summernoteID = request.getParameter("summernoteid");
		
		CFWForm form = CFW.Context.Session.getForm(id);
		
    	JSONResponse json = new JSONResponse();
    	json.useGlobaleLocale(true);
    	
    	if(form == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.method("doGet")
	    		.severe("The form with ID '"+id+"' could not be found. Try to refresh the page");
    		return;
    	}
		
    	if(summernoteID == null) {
    		//------------------------------
    		// Return Form
    		JsonObject payload = new JsonObject();
        	payload.addProperty("html", form.getHTML());
        	
        	json.getContent().append(payload.toString());
    	}else {
    		//------------------------------
    		// Return summernote editor 
    		// content
    		JsonObject payload = new JsonObject();
    		CFWField summernoteField = form.getField(summernoteID);
    		if(summernoteField != null) {
    			payload.addProperty("html", form.getField(summernoteID).getValue().toString());
    		}else {
    			payload.addProperty("html", "");
    		}
        	
        	json.getContent().append(payload.toString());
    	}
    	
    }
	
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
    	String formID = request.getParameter(CFWForm.FORM_ID);
    	CFWForm form = CFW.Context.Session.getForm(formID);
    	
    	JSONResponse json = new JSONResponse();
    	json.useGlobaleLocale(true);
    	
    	if(form == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.method("doGet")
	    		.severe("The form with ID '"+formID+"' could not be found.");
    		return;
    	}
    	    	
    	form.getFormHandler().handleForm(request, response, form, form.getOrigin());
	}
}
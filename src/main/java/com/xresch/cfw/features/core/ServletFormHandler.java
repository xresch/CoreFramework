package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWMultiForm;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * This servlet is used to handle forms that have a BTFormHandler defined.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletFormHandler extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletFormHandler.class.getName());
	
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		String id = request.getParameter("id");
		String summernoteID = request.getParameter("summernoteid");
		
		CFWForm form = CFW.Context.Session.getForm(id);
		
    	JSONResponse json = new JSONResponse();
    	json.useGlobalLocale(true);
    	
    	if(form == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
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
    		CFWField<?> summernoteField = form.getField(summernoteID);
    		if(summernoteField != null) {
    			String editorContent = (String)form.getField(summernoteID).getValue();
    			if(!Strings.isNullOrEmpty(editorContent)) {
    				payload.addProperty("html", form.getField(summernoteID).getValue().toString());
    			}else {
    				payload.addProperty("html", "");
    			}
    		}else {
    			payload.addProperty("html", "");
    		}
        	
        	json.getContent().append(payload.toString());
    	}
    	
    }
	
	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
    	String formID = request.getParameter(CFWForm.FORM_ID);
    	CFWForm form = CFW.Context.Session.getForm(formID);
    	
    	JSONResponse json = new JSONResponse();
    	json.useGlobalLocale(true);
    	
    	if(form == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.severe("The form with ID '"+formID+"' could not be found. Please refresh the page and try again.");
    		return;
    	}
    	
    	if(form instanceof CFWMultiForm) {
    		CFWMultiForm multiForm = (CFWMultiForm)form;
    		multiForm.getMultiFormHandler().handleForm(request, response, multiForm, multiForm.getOrigins());
    	}else {
    		form.getFormHandler().handleForm(request, response, form, form.getOrigin());
    	}
    	
	}
}
package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * This servlet is used to handle forms that have a BTFormHandler defined.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletAutocomplete extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = CFWLog.getLogger(ServletAutocomplete.class.getName());
	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		String formid = request.getParameter("cfw-formID");
		String fieldname = request.getParameter("cfwAutocompleteFieldname");
		String searchstring = request.getParameter("cfwAutocompleteSearchstring");
		JSONResponse json = new JSONResponse();
		
		//--------------------------------------------
		// Get Form
		//--------------------------------------------
		CFWForm form = CFW.Context.Session.getForm(formid);

    	if(form == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.severe("The form with ID '"+formid+"' could not be found. Try to refresh the page");
    		return;
    	}
    	
		//--------------------------------------------
		// Get Field
		//--------------------------------------------
		CFWField field = form.getField(fieldname);

    	if(field == null) {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.severe("The field with name '"+fieldname+"' could not be found. Try to refresh the page.");
    		return;
    	}
		
		//--------------------------------------------
		// Execute Autocomplete Handler
		//--------------------------------------------
    	if(field.getAutocompleteHandler() != null) {
    		AutocompleteResult suggestions = field.getAutocompleteHandler().getAutocompleteData(request, searchstring);
    		if(suggestions != null) {
    			json.getContent().append(suggestions.toJson());
    		}else {
    			json.getContent().append("null");
    		}
    	}else {
    		json.setSuccess(false);
    		new CFWLog(logger)
	    		.severe("The field with name '"+fieldname+"' doesn't have an autocomplete handler.");
    		return;
    	}
    	
    }
	
	
//    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
//	{
//    	String formID = request.getParameter(CFWForm.FORM_ID);
//    	CFWForm form = CFW.Context.Session.getForm(formID);
//    	
//    	JSONResponse json = new JSONResponse();
//    	if(form == null) {
//    		json.setSuccess(false);
//    		new CFWLog(logger)
//	    		.method("doGet")
//	    		.severe("The form with ID '"+formID+"' could not be found.");
//    		return;
//    	}
//    	    	
//    	form.getFormHandler().handleForm(request, response, form, form.getOrigin());
//	}
}
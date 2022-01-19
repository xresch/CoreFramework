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
import com.xresch.cfw.utils.CFWModifiableHTTPRequest;

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
    	AutocompleteResult suggestions = null;
    	if(form.getCustomAutocompleteHandler() == null) {
    		
    		if(field.getAutocompleteHandler() != null) {
        		suggestions = field.getAutocompleteHandler().getAutocompleteData(request, searchstring);
        	}else {
        		json.setSuccess(false);
        		new CFWLog(logger)
    	    		.severe("The field with name '"+fieldname+"' doesn't have an autocomplete handler.");
        		return;
        	}	 
    		
    	}else {
    		suggestions = form.getCustomAutocompleteHandler().getAutocompleteData(request, response, form, field, searchstring);
    	}
    	
		//--------------------------------------------
		// Add suggestions to response
		//--------------------------------------------
    	if(suggestions != null) {
			json.getContent().append(suggestions.toJson());
		}else {
			json.getContent().append("null");
		}
    	
    }
	
}
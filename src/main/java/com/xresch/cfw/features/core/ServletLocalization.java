package com.xresch.cfw.features.core;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletLocalization extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
	
		//-----------------------
		// Response Settings
		response.addHeader("Cache-Control", "max-age="
								+CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_TIMEOUTS, FeatureCore.CONFIG_BROWSER_RESOURCE_MAXAGE)
							);
		
		//-----------------------
		// Fetch LanguagePack
		JSONResponse json = new JSONResponse();
		String localeIdentifier = request.getParameter("id");
		Properties languagePack = CFW.Localization.getLanguagePackByIdentifier(localeIdentifier);

		if(languagePack == null) {
			json.setSuccess(false);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		int fileEtag = languagePack.hashCode();

		if(languagePack.size() > 0) {
			//-----------------------
			// Check ETag
			String requestEtag = request.getHeader("If-None-Match");
			// gzip handler will append "--gzip", therefore check on starts with
			if(requestEtag != null && requestEtag.startsWith(""+fileEtag)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			//-----------------------
			// Return Content
			json.getContent().append(CFW.JSON.toJSON(languagePack));
			response.addHeader("ETag", ""+fileEtag);
			
	        response.setStatus(HttpServletResponse.SC_OK);
	        json.setSuccess(true);
	    }else {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    	CFW.Messages.addWarningMessage("Language could not be loaded. Try to refresh the page.");
	    	json.setSuccess(false);
	    }
		
		//Done by RequestHandler
		//CFW.writeLocalized(request, response);
    }
}
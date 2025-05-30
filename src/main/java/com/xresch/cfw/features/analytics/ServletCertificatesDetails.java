package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.acme.CFWACMEClient;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletCertificatesDetails extends HttpServlet
{
	private static final Logger logger = CFWLog.getLogger(ServletCertificatesDetails.class.getName());
	private static final long serialVersionUID = 1L;

	/********************************************************************************
	 *
	 ********************************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
			HTMLResponse html = new HTMLResponse("Certificate Details");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_certdetails.js");
			
			html.addJavascriptCode("cfw_certdetails_draw();");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleActionRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
		}
        
    }
	
	/********************************************************************************
	 *
	 ********************************************************************************/
	private void handleActionRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {

			case "importscript":	
				new CFWLog(logger).audit(CFWAuditLogAction.IMPORT, "DatabaseScript", "Import database script.");
				String scriptFilePath = request.getParameter("filepath");
				boolean isImportSuccess = CFW.DB.importScript(scriptFilePath);
				if(isImportSuccess) {
					CFW.Messages.addSuccessMessage("Import successful!");
				}else {
					CFW.Messages.addErrorMessage("Error occured while importing script.");
				}
				break;
									
				
				
			case "forcerenewal":	
					new CFWLog(logger).audit(CFWAuditLogAction.RESET, "ACMECertificate", "Forced Renewal of certificate from Certificate Authority(CA).");
					try {
						CFWACMEClient.fetchCACertificate(true);
						CFW.Messages.addSuccessMessage("Certificate Renewed!");
						jsonResponse.setSuccess(true);
					} catch (Exception e) {
						CFW.Messages.addErrorMessage("Error while Renewing Certificate: " + e.getMessage());
						jsonResponse.setSuccess(false);
					}
				break;
											
											
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "certdetails": 		JsonObject object = new JsonObject();
												object.add("ACME Certificate", CFWACMEClient.getCertificateDetails());
												jsonResponse.setPayload(object);
	  											break;	
												
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/********************************************************************************
	 *
	 ********************************************************************************/
	private void reindexFulltextSearch() {

		ArrayList<CFWObject> objects = CFW.Registry.Objects.getCFWObjectInstances();
		for(CFWObject object : objects) {
			if(object.hasFulltextSearch()) {
				new CFWSQL(object).fulltextsearchReindex();
			}
		}
		// does not properly work
		// not really neccessary, as errors will be thrown by fulltextsearchReindex()
		//return success;
	}
		
	
}
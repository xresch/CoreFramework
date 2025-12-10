package com.xresch.cfw.features.filemanager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletStreamFileDownload extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletStreamFileDownload.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			
			//-------------------------
			// Get File ID
			String id = request.getParameter("id");
			String mode = request.getParameter("mode");
			
			if(Strings.isNullOrEmpty(mode)) {
				mode = "normal";
			}
			
			mode = mode.trim().toLowerCase();
			
			//-------------------------
			// Check has Access
			if(!CFW.DB.StoredFile.hasUserAccessToStoredFile(id)) {
				CFWMessages.accessDenied();
				writeErrorJSONResponse(request, response);
				return;
			}
			
			//-------------------------
			// Get File
			CFWStoredFile downloadThis = CFW.DB.StoredFile.selectByID(id);
			OutputStream out = response.getOutputStream();
			switch(mode) {
			
				case "normal":	
						response.setHeader("Content-disposition", "inline;filename="+downloadThis.name());
						response.setHeader("Content-Type", downloadThis.mimetype() );
						break;
						
				case "extraplain":	
						out.write("---".getBytes());
						
				case "plain":
						response.setHeader("Content-disposition", "inline;filename=data.txt");
						response.setHeader("Content-Type", "text/plain" );
						break;
			}
			
			
			boolean success = CFW.DB.StoredFile.retrieveData(downloadThis, out);
			
			if(!success) {
				// error messages should have been created by retrieveData()-method
				writeErrorJSONResponse(request, response);
			}
			
		}else {
			CFWMessages.accessDenied();
			writeErrorJSONResponse(request, response);
		}
			
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void writeErrorJSONResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONResponse jsonResponse = new JSONResponse();
		jsonResponse.setSuccess(false);
		CFW.Localization.writeLocalized(request, response);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
   protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
   {
		doGet(request, response);       
   }
	

	

	
}
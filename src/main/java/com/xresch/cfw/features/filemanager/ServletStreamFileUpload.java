package com.xresch.cfw.features.filemanager;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletStreamFileUpload extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletStreamFileUpload.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		JSONResponse jsonResponse = new JSONResponse();
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			
			//-------------------------
			// originalData
			Part originalDataPart = request.getPart("originalData");
			String originalData = CFW.Files.readContentsFromInputStream(originalDataPart.getInputStream());
			
			//-------------------------
			// Name
			Part namePart = request.getPart("name");
			String name = CFW.Files.readContentsFromInputStream(namePart.getInputStream());

			//-------------------------
			// Size
			Part sizePart = request.getPart("size");
			String size = CFW.Files.readContentsFromInputStream(sizePart.getInputStream());
			
			//-------------------------
			// Type
			Part typePart = request.getPart("type");
			String type = CFW.Files.readContentsFromInputStream(typePart.getInputStream());

			//-------------------------
			// lastModified
			Part lastModifiedPart = request.getPart("lastModified");
			String lastModified = CFW.Files.readContentsFromInputStream(lastModifiedPart.getInputStream());
			
			//-------------------------
			// File
			Part filePart = request.getPart("file");
			byte[] file = CFW.Files.readBytesFromInputStream(filePart.getInputStream());
			
			System.out.println("================================");
			System.out.println("originalData: "+originalData);
			System.out.println("name: "+name);
			System.out.println("type: "+type);
			System.out.println("size: "+size);
			System.out.println("lastModified: "+lastModified);
			
			jsonResponse.setSuccess(true);
			
			
		}else {
			CFWMessages.accessDenied();
		}
        
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
package com.xresch.cfw.features.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.datahandling.CFWStoredFileReferences;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.logging.SysoutInterceptor;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletStreamFileStoreJsonData extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletStreamFileStoreJsonData.class.getName());
	
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
			// Name
			Part namePart = request.getPart("name");
			String name = CFW.Files.readContentsFromInputStream(namePart.getInputStream());
			
			String type = "application/json";
			String extension = "json";
			String lastModified = ""+ System.currentTimeMillis();
			
			if( ! name.endsWith(".json") ) {
				name += ".json";
			}
			
			//-------------------------
			// File
			Part filePart = request.getPart("data");
			String size = ""+filePart.getSize();
			
			InputStream dataInputStream = filePart.getInputStream();
			
				//-----------------------------------------
				// Create New File
				CFWStoredFile newFile = new CFWStoredFile();
				newFile.foreignKeyOwner(CFW.Context.Request.getUserID());
				ServletStreamFileUpload.setStoredFileData(newFile, name, extension, size, type, lastModified);
				
				boolean success = CFW.DB.StoredFile.createAndStoreData(newFile, dataInputStream);
				
				CFWStoredFileReferences reference = new CFWStoredFileReferences(newFile);
				
				jsonResponse.setPayload( reference.getAsJsonArray() );
				jsonResponse.setSuccess(success);

		}else {
			CFWMessages.accessDenied();
		}
		
		//--------------------------
		// Write Response
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
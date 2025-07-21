package com.xresch.cfw.features.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.datahandling.CFWStoredFileReference;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025 
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
			
			String extension = "";
			
			if(name!= null && name.contains(".")) {
				extension = name.substring(name.lastIndexOf(".")+1);
			}
			
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
			
			InputStream dataInputStream = filePart.getInputStream();
			
			System.out.println("================================");
			System.out.println("originalData: "+originalData);
			System.out.println("name: "+name);
			System.out.println("extension: "+extension);
			System.out.println("type: "+type);
			System.out.println("size: "+size);
			System.out.println("lastModified: "+lastModified);
			
			CFWStoredFile newFile = new CFWStoredFile();
			newFile.foreignKeyOwner(CFW.Context.Request.getUserID());
			newFile.name(name);
			newFile.mimetype(type);
			newFile.extension(extension);
			
			if( ! Strings.isNullOrEmpty(size) ){
				newFile.size(Long.parseLong(size.trim()));
			}
			
			if( ! Strings.isNullOrEmpty(lastModified) ){
				newFile.lastModified(Long.parseLong(lastModified.trim()));
			}
			
			boolean success = CFW.DB.StoredFile.createAndStoreData(newFile, dataInputStream);
			
			CFWStoredFileReference reference = new CFWStoredFileReference(newFile);
			
			jsonResponse.setPayload( reference.getAsJsonObject() );
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
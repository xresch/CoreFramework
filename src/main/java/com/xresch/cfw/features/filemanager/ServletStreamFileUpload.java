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

			String originalData = null;
			if(originalDataPart != null ) {
				originalData = CFW.Files.readContentsFromInputStream(originalDataPart.getInputStream());
			}
			
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
			// replaceExisting
			Part replaceExistingPart = request.getPart("replaceExisting");
			String replaceExisting = CFW.Files.readContentsFromInputStream(replaceExistingPart.getInputStream());
			
			//-------------------------
			// File
			Part filePart = request.getPart("file");
			
			InputStream dataInputStream = filePart.getInputStream();
			
//			System.out.println("================================");
//			System.out.println("originalData: "+originalData);
//			System.out.println("replaceExisting: "+replaceExisting);
//			System.out.println("name: "+name);
//			System.out.println("extension: "+extension);
//			System.out.println("type: "+type);
//			System.out.println("size: "+size);
//			System.out.println("lastModified: "+lastModified);
			

			if(replaceExisting.equals("true")
			&& !Strings.isNullOrEmpty(originalData)
			&& !originalData.equals("null")
			&& !originalData.equals("[]")
			){
				
				//-----------------------------------------
				// Replace Existing Data
				CFWStoredFileReferences reference = new CFWStoredFileReferences(originalData);
				boolean success = false;
				
				if(reference.size() == 1) {
					Integer id = reference.getID(0);
					if(id != null) {
						
						CFWStoredFile existingFile = CFW.DB.StoredFile.selectByID(id);
						setStoredFileData(existingFile, name, extension, size, type, lastModified);

						//--------------------------
						// Store New Data
						success = CFW.DB.StoredFile.storeData(existingFile, dataInputStream);
						
						//--------------------------
						// Update File Details
						if(success) {
							CFW.DB.StoredFile.update(existingFile);
						}
						
						//--------------------------
						// Return Reference
						if(success) {
							CFWStoredFileReferences newReference = new CFWStoredFileReferences(existingFile);
							jsonResponse.setPayload( newReference.getAsJsonArray() );
						}
						
					}
				}
				
				jsonResponse.setSuccess(success);
				
			}else {
			
				//-----------------------------------------
				// Create New File
				CFWStoredFile newFile = new CFWStoredFile();
				newFile.foreignKeyOwner(CFW.Context.Request.getUserID());
				setStoredFileData(newFile, name, extension, size, type, lastModified);
				
				boolean success = CFW.DB.StoredFile.createAndStoreData(newFile, dataInputStream);
				
				CFWStoredFileReferences reference = new CFWStoredFileReferences(newFile);
				
				jsonResponse.setPayload( reference.getAsJsonArray() );
				jsonResponse.setSuccess(success);
			}
			
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
	public static void setStoredFileData(CFWStoredFile file, String name, String extension, String size, String type,
			String lastModified) {
		file.name(name);
		file.mimetype(type);
		file.extension(extension);
		
		if( ! Strings.isNullOrEmpty(size) ){
			file.size(Long.parseLong(size.trim()));
		}
		
		if( ! Strings.isNullOrEmpty(lastModified) ){
			file.lastModified(Long.parseLong(lastModified.trim()));
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
package com.xresch.cfw.features.filemanager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class ServletFilemanager extends HttpServlet
{

	private static final String MESSAGE_SHARED_GLOBAL = "All Stored File users will see this Stored File. The Stored File was saved as shared and no specific shared users or roles. ";

	private static final String MESSAGE_NOT_SHARED = "Users won't be able to access your Stored File until you set shared to true. The Stored File was saved as not shared and with at least one shared users or roles. ";

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletFilemanager.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("StoredFile List");
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {

				//html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureSpaces.RESOURCE_PACKAGE, "cfw_storedfile.css");
				
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureFilemanager.PACKAGE_RESOURCES, "cfw_storedfile_list.js");
				
				//content.append(CFW.Files.readPackageResource(FeatureSpaces.RESOURCE_PACKAGE, "cfw_storedfile.html"));
				
				html.addJavascriptCode("cfw_storedfilelist_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
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
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();
		
		//--------------------------------------
		// Check Permissions
		if(action.toLowerCase().equals("delete")
		|| action.toLowerCase().equals("copy")
		|| action.toLowerCase().equals("getform")) {
			if(!CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
			   && !CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)
			   && !CFW.DB.StoredFile.checkCanEdit(ID)
			   ) {
				CFWMessages.noPermission();
				return;
			}
		}
		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "mystoredfile": 		jsonResponse.getContent().append(CFW.DB.StoredFile.getUserStoredFileListAsJSON());
	  											break;
	  											
					case "myarchived":	 		jsonResponse.getContent().append(CFW.DB.StoredFile.getUserArchivedListAsJSON());
												break;
	  											
					case "sharedstoredfile": 	jsonResponse.getContent().append(CFW.DB.StoredFile.getSharedStoredFileListAsJSON());
												break;	
												
					case "adminstoredfile": 	jsonResponse.getContent().append(CFW.DB.StoredFile.getAdminStoredFileListAsJSON());
												break;	
												
					case "adminarchived": 		jsonResponse.getContent().append(CFW.DB.StoredFile.getAdminArchivedListAsJSON());
												break;	
												
					case "storedfiletats": 	String timeframeString = request.getParameter("timeframe");
												CFWTimeframe time = new CFWTimeframe(timeframeString);
												jsonResponse.setPayload(CFW.DB.StoredFile.getEAVStats(ID, time.getEarliest(), time.getLatest()));
												break;									
																										
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "update": 			
				switch(item.toLowerCase()) {

					case "isarchived":	String isArchived = request.getParameter("isarchived");
										jsonResponse.setSuccess(archiveStoredFile(ID,isArchived));
					break;
					
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "delete": 			
				switch(item.toLowerCase()) {
				
				case "storedfile": 	deleteStoredFile(jsonResponse, ID);
				break;  
				
				default: 			CFW.Messages.itemNotSupported(item);
				break;
				}
				break;	
				
//			case "duplicate": 			
//				switch(item.toLowerCase()) {
//
//					case "storedfile": 	duplicateStoredFile(jsonResponse, ID, false);
//											break;  
//					
//					default: 				CFW.Messages.itemNotSupported(item);
//											break;
//				}
//				break;	
				
				
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editstoredfile": 	createEditStoredFileForm(jsonResponse, ID);
											break;
					case "changeowner": 	createChangeStoredFileOwnerForm(jsonResponse, ID);
											break;
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private boolean archiveStoredFile(String ID, String isArchived) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)
		|| CFW.DB.StoredFile.isStoredFileOfCurrentUser(ID)) {
			return CFW.DB.StoredFile.updateIsArchived(ID, Boolean.parseBoolean(isArchived) );
		}
		
		return false;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteStoredFile(JSONResponse jsonResponse, String ID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			jsonResponse.setSuccess(CFW.DB.StoredFile.deleteByID(ID));
		}else {
			jsonResponse.setSuccess(CFW.DB.StoredFile.deleteByIDForCurrentUser(ID));
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
//	private void duplicateStoredFile(JSONResponse jsonResponse, String storedfileID, boolean newVersion) {
//		// TODO Auto-generated method stub
//		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)
//		|| (
//			   CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
//			&& CFW.DB.StoredFile.checkCanEdit(storedfileID) 
//			) 
//		) {
//			
//			Integer newID = CFW.DB.StoredFile.createDuplicate(storedfileID, newVersion);
//			
//			if(newID != null) {
//				jsonResponse.setSuccess(true);
//			}else {
//				jsonResponse.setSuccess(false);
//			}
//			
//		}else {
//			jsonResponse.setSuccess(false);
//			CFW.Messages.addErrorMessage("Insufficient permissions to duplicate the Stored File.");
//		}
//	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
				
		//--------------------------------------
		// Create StoredFile Form
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			
			CFWStoredFile newBoard = new CFWStoredFile();
			newBoard.updateSelectorFields();
			
			CFWForm createStoredFileForm = newBoard.toForm("cfwCreateStoredFileForm", "{!cfw_core_create!}");
			
			createStoredFileForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {
						
						origin.mapRequestParameters(request);
						CFWStoredFile storedfile = (CFWStoredFile)origin;
						storedfile.foreignKeyOwner(CFW.Context.Request.getUser().id());
						
						Integer newID = CFW.DB.StoredFile.createGetPrimaryKey(storedfile);
						
						if( newID != null ) {
							storedfile.id(newID);
							CFW.Messages.addSuccessMessage("StoredFile created successfully!");
							generateSharedMessages(storedfile);
							
							storedfile.saveSelectorFields();
						}
					}
					
				}
			});
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditStoredFileForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)
		|| CFW.DB.StoredFile.checkCanEdit(ID)) {
			CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(Integer.parseInt(ID));
			
			if(storedfile != null) {
				
				storedfile.updateSelectorFields();
				
				CFWForm editStoredFileForm = storedfile.toForm("cfwEditStoredFileForm"+ID, "Update StoredFile");

				editStoredFileForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						CFWStoredFile storedfile = (CFWStoredFile)origin;
						if(origin.mapRequestParameters(request) 
						&& CFW.DB.StoredFile.update(storedfile)) {
							
							
							CFW.Messages.addSuccessMessage("Updated!");
							
							generateSharedMessages(storedfile);
							
							storedfile.saveSelectorFields();
							
						}
						
					}

				});
				
				editStoredFileForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Messages.addErrorMessage("Insufficient permissions to execute action.");
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createChangeStoredFileOwnerForm(JSONResponse json, String ID) {
		
		if( CFW.DB.StoredFile.isStoredFileOfCurrentUser(ID)
		||	CFW.Context.Request.hasPermission(FeatureFilemanager.PERMISSION_STOREDFILE_ADMIN)) {
			CFWStoredFile storedfile = CFW.DB.StoredFile.selectByID(Integer.parseInt(ID));
			
			final String NEW_OWNER = "JSON_NEW_OWNER";
			if(storedfile != null) {
				
				CFWForm changeOwnerForm = new CFWForm("cfwChangeStoredFileOwnerForm"+ID, "Update StoredFile");
				
				changeOwnerForm.addField(
					CFWField.newTagsSelector(NEW_OWNER)
						.setLabel("New Owner")
						.addAttribute("maxTags", "1")
						.setDescription("Select the new owner of the StoredFile.")
						.addValidator(new NotNullOrEmptyValidator())
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
							}
						})
				);
				
				changeOwnerForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						String newOwnerJson = request.getParameter(NEW_OWNER);
						if(form.mapRequestParameters(request)) {
							LinkedHashMap<String,String> mappedValue = CFW.JSON.fromJsonLinkedHashMap(newOwnerJson);
							String newOwner = mappedValue.keySet().iterator().next();
	
							if(!Strings.isNullOrEmpty(newOwner)) {
								
								new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredFile.class, "Change owner ID of Stored File from "+storedfile.foreignKeyOwner()+" to "+newOwner);
								
								Integer oldOwner = storedfile.foreignKeyOwner();
								storedfile.foreignKeyOwner(Integer.parseInt(newOwner));
								
								if(storedfile.update(CFWStoredFileFields.FK_ID_OWNER)) {
									CFW.Messages.addSuccessMessage("Updated!");
									
									User currentUser = CFW.Context.Request.getUser();
									//----------------------------------
									// Send Notification to New Owner
									Notification newOwnerNotification = 
											new Notification()
													.foreignKeyUser(Integer.parseInt(newOwner))
													.messageType(MessageType.INFO)
													.title("Stored File assigned to you: '"+storedfile.name()+"'")
													.message("The user '"+currentUser.createUserLabel()+"' has assigned the Stored File to you. You are now the new owner of the Stored File.");

									CFW.DB.Notifications.create(newOwnerNotification);
									
									//----------------------------------
									// Send Notification to Old Owner
									User user = CFW.DB.Users.selectByID(newOwner);
									Notification oldOwnerNotification = 
											new Notification()
													.foreignKeyUser(oldOwner)
													.messageType(MessageType.INFO)
													.title("Owner of Stored File '"+storedfile.name()+"' is now "+user.createUserLabel())
													.message("The user '"+currentUser.createUserLabel()+"' has changed the owner of your former Stored File to the user '"+user.createUserLabel()+"'. ");

									CFW.DB.Notifications.create(oldOwnerNotification);
								}
							}
						}
					}
				});
				
				changeOwnerForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFWMessages.noPermission();
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void generateSharedMessages(CFWStoredFile storedfile) {
		if(!storedfile.isShared()
		&& (storedfile.sharedWithUsers().size() > 0
		   || storedfile.sharedWithGroups().size() > 0
		   || storedfile.editors().size() > 0
		   || storedfile.editorGroups().size() > 0
			)
		) {
			
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_NOT_SHARED
				);
		}
		
		if(storedfile.isShared()
		&& storedfile.sharedWithUsers().size() == 0
		&& storedfile.sharedWithGroups().size() == 0
		&& storedfile.editors().size() == 0
		&& storedfile.editorGroups().size() == 0) {
					
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_SHARED_GLOBAL
				);
		}
	}
}
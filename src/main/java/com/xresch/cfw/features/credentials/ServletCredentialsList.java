package com.xresch.cfw.features.credentials;

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
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.credentials.CFWCredentials.CFWCredentialsFields;
import com.xresch.cfw.features.credentials.FeatureCredentials;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletCredentialsList extends HttpServlet
{

	private static final String MESSAGE_SHARED_GLOBAL = "All credentials users will see this credentials. The credentials was saved as shared and no specific shared users or roles. ";

	private static final String MESSAGE_NOT_SHARED = "Users won't be able to access your credentials until you set shared to true. The credentials was saved as not shared and with at least one shared users or roles. ";

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletCredentialsList.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Credentials List");
		
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {

				//html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureSpaces.RESOURCE_PACKAGE, "cfw_credentials.css");
				
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureCredentials.PACKAGE_RESOURCES, "cfw_credentials_list.js");
				
				//content.append(CFW.Files.readPackageResource(FeatureSpaces.RESOURCE_PACKAGE, "cfw_credentials.html"));
				
				html.addJavascriptCode("cfw_credentialslist_initialDraw();");
				
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
			if(!CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_CREATOR)
			   && !CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)
			   && !CFW.DB.Credentials.checkCanEdit(ID)
			   ) {
				CFWMessages.noPermission();
				return;
			}
		}
		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "mycredentials": 		jsonResponse.getContent().append(CFW.DB.Credentials.getUserCredentialsListAsJSON());
	  											break;
	  											
					case "myarchived":	 		jsonResponse.getContent().append(CFW.DB.Credentials.getUserArchivedListAsJSON());
												break;
	  											
					case "sharedcredentials": 	jsonResponse.getContent().append(CFW.DB.Credentials.getSharedCredentialsListAsJSON());
												break;	
												
					case "admincredentials": 	jsonResponse.getContent().append(CFW.DB.Credentials.getAdminCredentialsListAsJSON());
												break;	
												
					case "adminarchived": 		jsonResponse.getContent().append(CFW.DB.Credentials.getAdminArchivedListAsJSON());
												break;	
												
					case "credentialstats": 	String timeframeString = request.getParameter("timeframe");
												CFWTimeframe time = new CFWTimeframe(timeframeString);
												jsonResponse.setPayload(CFW.DB.Credentials.getEAVStats(ID, time.getEarliest(), time.getLatest()));
												break;									
																										
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "update": 			
				switch(item.toLowerCase()) {

					case "isarchived":	String isArchived = request.getParameter("isarchived");
										jsonResponse.setSuccess(archiveCredentials(ID,isArchived));
					break;
					
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "delete": 			
				switch(item.toLowerCase()) {
				
				case "credentials": 	deleteCredentials(jsonResponse, ID);
				break;  
				
				default: 			CFW.Messages.itemNotSupported(item);
				break;
				}
				break;	
				
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "credentials": 	duplicateCredentials(jsonResponse, ID, false);
											break;  
					
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
				
				
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editcredentials": 	createEditCredentialsForm(jsonResponse, ID);
											break;
					case "changeowner": 	createChangeCredentialsOwnerForm(jsonResponse, ID);
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
	private boolean archiveCredentials(String ID, String isArchived) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)
		|| CFW.DB.Credentials.isCredentialsOfCurrentUser(ID)) {
			return CFW.DB.Credentials.updateIsArchived(ID, Boolean.parseBoolean(isArchived) );
		}
		
		return false;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteCredentials(JSONResponse jsonResponse, String ID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			jsonResponse.setSuccess(CFW.DB.Credentials.deleteByID(ID));
		}else {
			jsonResponse.setSuccess(CFW.DB.Credentials.deleteByIDForCurrentUser(ID));
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateCredentials(JSONResponse jsonResponse, String credentialsID, boolean newVersion) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)
		|| (
			   CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_CREATOR)
			&& CFW.DB.Credentials.checkCanEdit(credentialsID) 
			) 
		) {
			
			Integer newID = CFW.DB.Credentials.createDuplicate(credentialsID, newVersion);
			
			if(newID != null) {
				jsonResponse.setSuccess(true);
			}else {
				jsonResponse.setSuccess(false);
			}
			
		}else {
			jsonResponse.setSuccess(false);
			CFW.Messages.addErrorMessage("Insufficient permissions to duplicate the credentials.");
		}
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
				
		//--------------------------------------
		// Create Credentials Form
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			
			CFWCredentials newBoard = new CFWCredentials();
			newBoard.updateSelectorFields();
			
			CFWForm createCredentialsForm = newBoard.toForm("cfwCreateCredentialsForm", "{!cfw_core_create!}");
			
			createCredentialsForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {
						
						origin.mapRequestParameters(request);
						CFWCredentials credentials = (CFWCredentials)origin;
						credentials.foreignKeyOwner(CFW.Context.Request.getUser().id());
						
						Integer newID = CFW.DB.Credentials.createGetPrimaryKey(credentials);
						
						if( newID != null ) {
							credentials.id(newID);
							CFW.Messages.addSuccessMessage("Credentials created successfully!");
							generateSharedMessages(credentials);
							
							credentials.saveSelectorFields();
						}
					}
					
				}
			});
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditCredentialsForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)
		|| CFW.DB.Credentials.checkCanEdit(ID)) {
			CFWCredentials credentials = CFW.DB.Credentials.selectByID(Integer.parseInt(ID));
			
			if(credentials != null) {
				
				credentials.updateSelectorFields();
				
				CFWForm editCredentialsForm = credentials.toForm("cfwEditCredentialsForm"+ID, "Update Credentials");

				editCredentialsForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						CFWCredentials credentials = (CFWCredentials)origin;
						if(origin.mapRequestParameters(request) 
						&& CFW.DB.Credentials.update(credentials)) {
							
							
							CFW.Messages.addSuccessMessage("Updated!");
							
							generateSharedMessages(credentials);
							
							credentials.saveSelectorFields();
							
						}
						
					}

				});
				
				editCredentialsForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Messages.addErrorMessage("Insufficient permissions to execute action.");
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createChangeCredentialsOwnerForm(JSONResponse json, String ID) {
		
		if( CFW.DB.Credentials.isCredentialsOfCurrentUser(ID)
		||	CFW.Context.Request.hasPermission(FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN)) {
			CFWCredentials credentials = CFW.DB.Credentials.selectByID(Integer.parseInt(ID));
			
			final String NEW_OWNER = "JSON_NEW_OWNER";
			if(credentials != null) {
				
				CFWForm changeOwnerForm = new CFWForm("cfwChangeCredentialsOwnerForm"+ID, "Update Credentials");
				
				changeOwnerForm.addField(
					CFWField.newTagsSelector(NEW_OWNER)
						.setLabel("New Owner")
						.addAttribute("maxTags", "1")
						.setDescription("Select the new owner of the Credentials.")
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
								
								new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWCredentials.class, "Change owner ID of credentials from "+credentials.foreignKeyOwner()+" to "+newOwner);
								
								Integer oldOwner = credentials.foreignKeyOwner();
								credentials.foreignKeyOwner(Integer.parseInt(newOwner));
								
								if(credentials.update(CFWCredentialsFields.FK_ID_OWNER)) {
									CFW.Messages.addSuccessMessage("Updated!");
									
									User currentUser = CFW.Context.Request.getUser();
									//----------------------------------
									// Send Notification to New Owner
									Notification newOwnerNotification = 
											new Notification()
													.foreignKeyUser(Integer.parseInt(newOwner))
													.messageType(MessageType.INFO)
													.title("Credentials assigned to you: '"+credentials.name()+"'")
													.message("The user '"+currentUser.createUserLabel()+"' has assigned the credentials to you. You are now the new owner of the credentials.");

									CFW.DB.Notifications.create(newOwnerNotification);
									
									//----------------------------------
									// Send Notification to Old Owner
									User user = CFW.DB.Users.selectByID(newOwner);
									Notification oldOwnerNotification = 
											new Notification()
													.foreignKeyUser(oldOwner)
													.messageType(MessageType.INFO)
													.title("Owner of credentials '"+credentials.name()+"' is now "+user.createUserLabel())
													.message("The user '"+currentUser.createUserLabel()+"' has changed the owner of your former credentials to the user '"+user.createUserLabel()+"'. ");

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
	private void generateSharedMessages(CFWCredentials credentials) {
		if(!credentials.isShared()
		&& (credentials.sharedWithUsers().size() > 0
		   || credentials.sharedWithGroups().size() > 0
		   || credentials.editors().size() > 0
		   || credentials.editorGroups().size() > 0
			)
		) {
			
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_NOT_SHARED
				);
		}
		
		if(credentials.isShared()
		&& credentials.sharedWithUsers().size() == 0
		&& credentials.sharedWithGroups().size() == 0
		&& credentials.editors().size() == 0
		&& credentials.editorGroups().size() == 0) {
					
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_SHARED_GLOBAL
				);
		}
	}
}
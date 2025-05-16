package com.xresch.cfw.features.query.store;

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
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.features.query.store.FeatureStoredQuery;
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
public class ServletStoredQueryList extends HttpServlet
{

	private static final String MESSAGE_SHARED_GLOBAL = "All Stored Query users will see this stored query. The stored query was saved as shared and without specific shared users or roles. ";

	private static final String MESSAGE_NOT_SHARED = "Users won't be able to access your Stored Query until you set shared to true. The Stored Query was saved as not shared and with at least one shared users or roles. ";

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletStoredQueryList.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {
				JSONResponse error = new JSONResponse();
				error.setSuccess(false);
				CFW.Messages.addErrorMessage("Action was not defined");
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
			if(!CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_CREATOR)
			   && !CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)
			   && !CFW.DB.StoredQuery.checkCanEdit(ID)
			   ) {
				CFWMessages.noPermission();
				return;
			}
		}
		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "mystoredquery": 		jsonResponse.getContent().append(CFW.DB.StoredQuery.getUserStoredQueryListAsJSON());
	  											break;
	  											
					case "myarchived":	 		jsonResponse.getContent().append(CFW.DB.StoredQuery.getUserArchivedListAsJSON());
												break;
	  											
					case "sharedstoredquery": 	jsonResponse.getContent().append(CFW.DB.StoredQuery.getSharedStoredQueryListAsJSON());
												break;	
												
					case "adminstoredquery": 	jsonResponse.getContent().append(CFW.DB.StoredQuery.getAdminStoredQueryListAsJSON());
												break;	
												
					case "adminarchived": 		jsonResponse.getContent().append(CFW.DB.StoredQuery.getAdminArchivedListAsJSON());
												break;	
												
					case "storedquerytats": 	String timeframeString = request.getParameter("timeframe");
												CFWTimeframe time = new CFWTimeframe(timeframeString);
												jsonResponse.setPayload(CFW.DB.StoredQuery.getEAVStats(ID, time.getEarliest(), time.getLatest()));
												break;									
																										
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "update": 			
				switch(item.toLowerCase()) {

					case "isarchived":	String isArchived = request.getParameter("isarchived");
										jsonResponse.setSuccess(archiveStoredQuery(ID,isArchived));
					break;
					
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "delete": 			
				switch(item.toLowerCase()) {
				
				case "storedquery": 	deleteStoredQuery(jsonResponse, ID);
				break;  
				
				default: 			CFW.Messages.itemNotSupported(item);
				break;
				}
				break;	
				
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "storedquery": 	duplicateStoredQuery(jsonResponse, ID, false);
											break;  
					
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
				
				
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editstoredquery": 	createEditStoredQueryForm(jsonResponse, ID);
											break;
					case "changeowner": 	createChangeStoredQueryOwnerForm(jsonResponse, ID);
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
	private boolean archiveStoredQuery(String ID, String isArchived) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)
		|| CFW.DB.StoredQuery.isStoredQueryOfCurrentUser(ID)) {
			return CFW.DB.StoredQuery.updateIsArchived(ID, Boolean.parseBoolean(isArchived) );
		}
		
		return false;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteStoredQuery(JSONResponse jsonResponse, String ID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			jsonResponse.setSuccess(CFW.DB.StoredQuery.deleteByID(ID));
		}else {
			jsonResponse.setSuccess(CFW.DB.StoredQuery.deleteByIDForCurrentUser(ID));
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateStoredQuery(JSONResponse jsonResponse, String storedQueryID, boolean newVersion) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)
		|| (
			   CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_CREATOR)
			&& CFW.DB.StoredQuery.checkCanEdit(storedQueryID) 
			) 
		) {
			
			Integer newID = CFW.DB.StoredQuery.createDuplicate(storedQueryID, newVersion);
			
			if(newID != null) {
				jsonResponse.setSuccess(true);
			}else {
				jsonResponse.setSuccess(false);
			}
			
		}else {
			jsonResponse.setSuccess(false);
			CFW.Messages.addErrorMessage("Insufficient permissions to duplicate the storedQuery.");
		}
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
				
		//--------------------------------------
		// Create StoredQuery Form
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			
			CFWStoredQuery newBoard = new CFWStoredQuery();
			newBoard.updateSelectorFields();
			
			CFWForm createStoredQueryForm = newBoard.toForm("cfwCreateStoredQueryForm", "{!cfw_core_create!}");
			
			createStoredQueryForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {
						
						origin.mapRequestParameters(request);
						CFWStoredQuery storedQuery = (CFWStoredQuery)origin;
						storedQuery.foreignKeyOwner(CFW.Context.Request.getUser().id());
						
						Integer newID = CFW.DB.StoredQuery.createGetPrimaryKey(storedQuery);
						
						if( newID != null ) {
							storedQuery.id(newID);
							CFW.Messages.addSuccessMessage("StoredQuery created successfully!");
							generateSharedMessages(storedQuery);
							
							storedQuery.saveSelectorFields();
						}
					}
					
				}
			});
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditStoredQueryForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)
		|| CFW.DB.StoredQuery.checkCanEdit(ID)) {
			CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(Integer.parseInt(ID));
			
			if(storedQuery != null) {
				
				storedQuery.updateSelectorFields();
				
				CFWForm editStoredQueryForm = storedQuery.toForm("cfwEditStoredQueryForm"+ID, "Update StoredQuery");

				editStoredQueryForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						CFWStoredQuery storedQuery = (CFWStoredQuery)origin;
						if(origin.mapRequestParameters(request) 
						&& CFW.DB.StoredQuery.update(storedQuery)) {
							
							
							CFW.Messages.addSuccessMessage("Updated!");
							
							generateSharedMessages(storedQuery);
							
							storedQuery.saveSelectorFields();
							
						}
						
					}

				});
				
				editStoredQueryForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Messages.addErrorMessage("Insufficient permissions to execute action.");
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createChangeStoredQueryOwnerForm(JSONResponse json, String ID) {
		
		if( CFW.DB.StoredQuery.isStoredQueryOfCurrentUser(ID)
		||	CFW.Context.Request.hasPermission(FeatureStoredQuery.PERMISSION_STOREDQUERY_ADMIN)) {
			CFWStoredQuery storedQuery = CFW.DB.StoredQuery.selectByID(Integer.parseInt(ID));
			
			final String NEW_OWNER = "JSON_NEW_OWNER";
			if(storedQuery != null) {
				
				CFWForm changeOwnerForm = new CFWForm("cfwChangeStoredQueryOwnerForm"+ID, "Update StoredQuery");
				
				changeOwnerForm.addField(
					CFWField.newTagsSelector(NEW_OWNER)
						.setLabel("New Owner")
						.addAttribute("maxTags", "1")
						.setDescription("Select the new owner of the StoredQuery.")
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
								
								new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, CFWStoredQuery.class, "Change owner ID of storedQuery from "+storedQuery.foreignKeyOwner()+" to "+newOwner);
								
								Integer oldOwner = storedQuery.foreignKeyOwner();
								storedQuery.foreignKeyOwner(Integer.parseInt(newOwner));
								
								if(storedQuery.update(CFWStoredQueryFields.FK_ID_OWNER)) {
									CFW.Messages.addSuccessMessage("Updated!");
									
									User currentUser = CFW.Context.Request.getUser();
									//----------------------------------
									// Send Notification to New Owner
									Notification newOwnerNotification = 
											new Notification()
													.foreignKeyUser(Integer.parseInt(newOwner))
													.messageType(MessageType.INFO)
													.title("StoredQuery assigned to you: '"+storedQuery.name()+"'")
													.message("The user '"+currentUser.createUserLabel()+"' has assigned the storedQuery to you. You are now the new owner of the storedQuery.");

									CFW.DB.Notifications.create(newOwnerNotification);
									
									//----------------------------------
									// Send Notification to Old Owner
									User user = CFW.DB.Users.selectByID(newOwner);
									Notification oldOwnerNotification = 
											new Notification()
													.foreignKeyUser(oldOwner)
													.messageType(MessageType.INFO)
													.title("Owner of storedQuery '"+storedQuery.name()+"' is now "+user.createUserLabel())
													.message("The user '"+currentUser.createUserLabel()+"' has changed the owner of your former storedQuery to the user '"+user.createUserLabel()+"'. ");

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
	private void generateSharedMessages(CFWStoredQuery storedQuery) {
		if(!storedQuery.isShared()
		&& (storedQuery.sharedWithUsers().size() > 0
		   || storedQuery.sharedWithGroups().size() > 0
		   || storedQuery.editors().size() > 0
		   || storedQuery.editorGroups().size() > 0
			)
		) {
			
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_NOT_SHARED
				);
		}
		
		if(storedQuery.isShared()
		&& storedQuery.sharedWithUsers().size() == 0
		&& storedQuery.sharedWithGroups().size() == 0
		&& storedQuery.editors().size() == 0
		&& storedQuery.editorGroups().size() == 0) {
					
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_SHARED_GLOBAL
				);
		}
	}
}
package com.xresch.cfw.features.usermgmt;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.role.Role;
import com.xresch.cfw.features.role.FeatureRole;
import com.xresch.cfw.features.role.Role.RoleFields;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.PasswordValidator;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletUserManagementAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogManager().getLogger(ServletUserManagementAPI.class.getName());
       
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//-------------------------------------------
		// Initialize
		//-------------------------------------------
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		String IDs = request.getParameter("ids");
		
		String userID, roleID, permissionID;
		//-------------------------------------------
		// Fetch Data
		//-------------------------------------------
		JSONResponse jsonResponse = new JSONResponse();
		StringBuilder content = jsonResponse.getContent();

		if(CFW.Context.Request.hasPermission(FeatureUserManagement.PERMISSION_USER_MANAGEMENT)) {
			
			if (action == null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Parameter 'data' was not specified.");
				//content.append("{\"error\": \"Type was not specified.\"}");
			}else {
	
				switch(action.toLowerCase()) {
					
					case "fetch": 			
						switch(item.toLowerCase()) {
							case "users": 			content.append(CFW.DB.Users.getUserListAsJSON());
										  			break;
										  		
							case "user": 			content.append(CFW.DB.Users.getUserAsJSON(ID));
					  								break;			
							case "usersforrole": 	content.append(CFW.DB.Roles.getUsersForRoleAsJSON(ID));
													break;						
					  													
							case "roles": 			content.append(CFW.DB.Roles.getUserRoleListAsJSON());
							  			   			break;

							case "role": 			content.append(CFW.DB.Roles.getUserRolesAsJSON(ID));
													break;	
																				
							case "groups": 			content.append(CFW.DB.Roles.getGroupListAsJSON());
													break;	
													
							case "permissions":		content.append(CFW.DB.Permissions.getUserPermissionListAsJSON());
		  			   								break;  
		  			   		
							case "useraudit":		content.append(CFWRegistryAudit.auditUser(ID));
 													break;  
 													
							case "fullaudit":		content.append(CFWRegistryAudit.auditAllUsers());
													break;  	
													
							default: 				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
													break;
						}
						break;
					
					case "fetchpartial": 
						String pagesize = request.getParameter("pagesize");
						String pagenumber = request.getParameter("pagenumber");
						String filterquery = request.getParameter("filterquery");
						String sortby = request.getParameter("sortby");
						String isAscendingString = request.getParameter("isascending");
						String userOrRoleID = request.getParameter("id");
						
						boolean isAscending = (isAscendingString == null || isAscendingString.equals("true")) ? true : false;
						
						switch(item.toLowerCase()) {
							case "userrolemap": 	content.append(CFW.DB.UserRoleMap.getUserRoleMapForUserAsJSON(userOrRoleID, pagesize, pagenumber, filterquery, sortby, isAscending));
							break;	
							
							case "usergroupmap": 	content.append(CFW.DB.UserRoleMap.getUserGroupMapForUserAsJSON(userOrRoleID, pagesize, pagenumber, filterquery, sortby, isAscending));
							break;	
							
							case "rolepermissionmap": 	content.append(CFW.DB.RolePermissionMap.getPermissionMapForRoleAsJSON(userOrRoleID, pagesize, pagenumber, filterquery, sortby, isAscending));
							break;	
						}
						break;
					case "delete": 			
						switch(item.toLowerCase()) {
							case "users": 		jsonResponse.setSuccess(CFW.DB.Users.deleteMultipleByID(IDs));
										  		break;
										  
							case "roles": 		jsonResponse.setSuccess(CFW.DB.Roles.deleteMultipleByID(IDs));
												break;  
												
							case "groups": 		jsonResponse.setSuccess(CFW.DB.Roles.deleteMultipleByID(IDs));
												break;  
												
							case "permissions": jsonResponse.setSuccess(CFW.DB.Permissions.deleteMultipleByID(IDs));
		  			   							break;  
		  			   							
							default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
						}
						break;
					
					case "update": 			
						switch(item.toLowerCase()) {
							case "userrolemap": 
							case "usergroupmap": 
														userID = request.getParameter("itemid");
														roleID = request.getParameter("listitemid");
														jsonResponse.setSuccess(CFW.DB.UserRoleMap.toogleUserInRole(userID, roleID));
														SessionTracker.updateUserRights(Integer.parseInt(userID));
														break;
														
							case "rolepermissionmap": 	roleID = request.getParameter("itemid");
														permissionID = request.getParameter("listitemid");
														jsonResponse.setSuccess(CFW.DB.RolePermissionMap.tooglePermissionInRole(permissionID, roleID));
														break;
			
							default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
						}
						break;
					
					case "getform": 			
						switch(item.toLowerCase()) {
							case "edituser": 	createEditUserForm(jsonResponse, ID);
												break;
							
							case "editrole": 	createEditRoleForm(jsonResponse, ID);
												break;
							
							case "editgroup": 	createEditGroupForm(jsonResponse, ID);
												break;
												
							case "changeowner": createChangeGroupOwnerForm(jsonResponse, ID);
												break;
							
							case "resetpw": 	createResetPasswordForm(jsonResponse, ID);
							break;
							
							default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
						}
						break;
						
					default: 				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of action '"+action+"' is not supported.");
											break;
											
				}
							
			}
		
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditUserForm(JSONResponse json, String ID) {
		
		User user = CFW.DB.Users.selectByID(Integer.parseInt(ID));
		
		if(user != null) {
			
			CFWForm editUserForm = user.toForm("cfwEditUserForm"+ID, "Update User");
			
			editUserForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request) 
					&& CFW.DB.Users.update((User)origin)) {
						
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
							
					}
					
				}
			});
			
			editUserForm.appendToPayload(json);
			json.setSuccess(true);
			
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditRoleForm(JSONResponse json, String ID) {
		
		Role role = CFW.DB.Roles.selectByID(Integer.parseInt(ID));
		
		if(role != null) {
			
			role.updateSelectorFields();
			role.removeField(RoleFields.JSON_EDITORS); // no editors for roles
			
			CFWForm editRoleForm = role.toForm("cfwEditRoleForm"+ID, "Update Role");
			
			editRoleForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)
					&& CFW.DB.Roles.update((Role)origin)) {
						role.saveSelectorFields();
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");	
					}
					
				}
			});
			
			editRoleForm.appendToPayload(json);
			json.setSuccess(true);
			
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditGroupForm(JSONResponse json, String ID) {
		
		Role role = CFW.DB.Roles.selectByID(Integer.parseInt(ID));
		role.updateSelectorFields();
		
		if(role != null) {
			
			CFWForm editRoleForm = role.toForm("cfwEditGroupForm"+ID, "Update Group");
			
			editRoleForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)
					&& CFW.DB.Roles.update((Role)origin)) {
						role.saveSelectorFields();
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");	
					}
					
				}
			});
			
			editRoleForm.appendToPayload(json);
			json.setSuccess(true);
			
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createChangeGroupOwnerForm(JSONResponse json, String ID) {
		
		if( CFW.DB.Roles.isGroupOfCurrentUser(ID)
		||	CFW.Context.Request.hasPermission(FeatureUserManagement.PERMISSION_USER_MANAGEMENT)) {
			
			Role role = CFW.DB.Roles.selectByID(Integer.parseInt(ID));
			
			final String NEW_OWNER = "JSON_NEW_OWNER";
			if(role != null) {
				
				CFWForm changeOwnerForm = new CFWForm("cfwChangeGroupOwnerForm"+ID, "Update Role");
				
				changeOwnerForm.addField(
					CFWField.newTagsSelector(NEW_OWNER)
						.setLabel("New Owner")
						.addAttribute("maxTags", "1")
						.setDescription("Select the new owner of the group.")
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
								
								new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Role.class, "Change owner ID of role from "+role.foreignKeyGroupOwner()+" to "+newOwner);
								
								Integer oldOwner = role.foreignKeyGroupOwner();
								role.foreignKeyGroupOwner(Integer.parseInt(newOwner));
								
								if(role.update(RoleFields.FK_ID_GROUPOWNER)) {
									CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
									
									User currentUser = CFW.Context.Request.getUser();
									
									//----------------------------------
									// Send Notification to New Owner
									Notification newOwnerNotification = 
											new Notification()
													.foreignKeyUser(Integer.parseInt(newOwner))
													.messageType(MessageType.INFO)
													.title("You are now oner of the group: '"+role.name()+"'")
													.message("The user '"+currentUser.createUserLabel()+"' has made you the owner of the group.");

									CFW.DB.Notifications.create(newOwnerNotification);
									
									//----------------------------------
									// Send Notification to Old Owner
									User user = CFW.DB.Users.selectByID(newOwner);
									Notification oldOwnerNotification = 
											new Notification()
													.foreignKeyUser(oldOwner)
													.messageType(MessageType.INFO)
													.title("Owner of group '"+role.name()+"' is now "+user.createUserLabel())
													.message("The user '"+currentUser.createUserLabel()+"' has changed the owner of the group from you to the user '"+user.createUserLabel()+"'. ");

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
	private void createResetPasswordForm(JSONResponse json, String ID) {
		
		User user = CFW.DB.Users.selectByID(Integer.parseInt(ID));
		
		if(user != null) {
			
			CFWForm resetPasswordForm = new ResetPasswordForm("cfwResetPasswordForm"+ID, "Reset User Password", user);
			
			resetPasswordForm.appendToPayload(json);
			json.setSuccess(true);
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Unknown user ID:"+ID);
		}
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	class ResetPasswordForm extends CFWForm{
		
		protected CFWField<Integer> userid = CFWField.newInteger(FormFieldType.HIDDEN, "UserID")
				.addValidator(new LengthValidator(1, 255));
		
		protected CFWField<String> username = CFWField.newString(FormFieldType.TEXT, "Username")
				.addValidator(new LengthValidator(1, 255));
		
		protected CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, "Password")
				.disableSanitization()
				.addValidator(new LengthValidator(1, 255))
				.addValidator(new PasswordValidator());
		
		protected CFWField<String> repeatedPassword = CFWField.newString(FormFieldType.PASSWORD, "Repeat Password")
				.disableSanitization()
				.addValidator(new NotNullOrEmptyValidator());
		
		public ResetPasswordForm(String formID, String submitLabel, User affectedUser) {
			super(formID, submitLabel);
			this.addField(userid);
			this.addField(username);
			this.addField(password);
			this.addField(repeatedPassword);
			
			userid.setValue(affectedUser.id());
			username.setValue(affectedUser.username());
			username.isDisabled(true);
			
			this.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(form.mapRequestParameters(request)) {
						ResetPasswordForm casted = (ResetPasswordForm)form;
						User user = new User(casted.getUsername())
								.id(casted.getUserID())
								.setNewPassword(casted.getPassword(), casted.getRepeatedPassword());
						
						if(user != null) {
							boolean success = user.update(UserFields.PASSWORD_HASH.toString(), 
										UserFields.PASSWORD_SALT.toString());
							if(success) {
								CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Password Updated!");
								return;
							}
						}
					}
					
				}
			});

		}
		public int getUserID() { return userid.getValue(); }
		public String getUsername() { return username.getValue(); }
		public String getPassword() { return password.getValue(); }
		public String getRepeatedPassword() { return repeatedPassword.getValue(); }
	}
}

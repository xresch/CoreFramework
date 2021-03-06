package com.xresch.cfw.features.usermgmt;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
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
public class SevletUserManagementAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogManager().getLogger(SevletUserManagementAPI.class.getName());
       
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

		if(CFW.Context.Request.hasPermission(Permission.CFW_USER_MANAGEMENT)) {
			
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
					  								
							case "userrolemap": 	content.append(CFW.DB.UserRoleMap.getUserRoleMapForUserAsJSON(ID));
					  								break;	
					  								
							case "usergroupmap": 	content.append(CFW.DB.UserRoleMap.getUserGroupMapForUserAsJSON(ID));
													break;	
													
							case "roles": 			content.append(CFW.DB.Roles.getUserRoleListAsJSON());
							  			   			break;

							case "role": 			content.append(CFW.DB.Roles.getUserRolesAsJSON(ID));
													break;	
													
							case "rolepermissionmap": 	content.append(CFW.DB.RolePermissionMap.getPermissionMapForRoleAsJSON(ID));
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
	
	private void createEditRoleForm(JSONResponse json, String ID) {
		
		Role role = CFW.DB.Roles.selectByID(Integer.parseInt(ID));
		
		if(role != null) {
			
			CFWForm editRoleForm = role.toForm("cfwEditRoleForm"+ID, "Update Role");
			
			editRoleForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)
					&& CFW.DB.Roles.update((Role)origin)) {
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");	
					}
					
				}
			});
			
			editRoleForm.appendToPayload(json);
			json.setSuccess(true);
			
		}
		
	}
	
	private void createEditGroupForm(JSONResponse json, String ID) {
		
		Role role = CFW.DB.Roles.selectByID(Integer.parseInt(ID));
		
		if(role != null) {
			
			CFWForm editRoleForm = role.toForm("cfwEditGroupForm"+ID, "Update Group");
			
			editRoleForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)
					&& CFW.DB.Roles.update((Role)origin)) {
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");	
					}
					
				}
			});
			
			editRoleForm.appendToPayload(json);
			json.setSuccess(true);
			
		}
		
	}
	
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
	
	class ResetPasswordForm extends CFWForm{
		
		protected CFWField<Integer> userid = CFWField.newInteger(FormFieldType.HIDDEN, "UserID")
				.addValidator(new LengthValidator(1, 255));
		
		protected CFWField<String> username = CFWField.newString(FormFieldType.TEXT, "Username")
				.addValidator(new LengthValidator(1, 255));
		
		protected CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, "Password")
				.addValidator(new LengthValidator(-1, 255))
				.addValidator(new PasswordValidator());
		
		protected CFWField<String> repeatedPassword = CFWField.newString(FormFieldType.PASSWORD, "Repeat Password")
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

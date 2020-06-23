package com.pengtoolbox.cfw.features.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.usermgmt.User.UserFields;
import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.datahandling.CFWFormHandler;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.validation.LengthValidator;
import com.pengtoolbox.cfw.validation.NotNullOrEmptyValidator;
import com.pengtoolbox.cfw.validation.PasswordValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletUserManagement extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletUserManagement() {
	
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		HTMLResponse html = new HTMLResponse("User Management");
		
		StringBuffer content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(Permission.CFW_USER_MANAGEMENT)) {
			
			createForms();
			
			//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".js", "cfw_usermgmt.js"));
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureUserManagement.RESOURCE_PACKAGE, "cfw_usermgmt.js");
			
			content.append(CFW.Files.readPackageResource(FeatureUserManagement.RESOURCE_PACKAGE, "cfw_usermgmt.html"));
			
			html.addJavascriptCode("cfw_usermgmt_initialDraw({tab: 'users'});");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	private void createForms() {
		
		//--------------------------------------
		// Create User Form
		CreateUserForm createUserForm = new CreateUserForm("cfwCreateUserForm", "Create User");
		
		createUserForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
				
				if(form.mapRequestParameters(request)) {
					CreateUserForm casted = (CreateUserForm)form;
					User newUser = new User(casted.getUsername())
							.status(casted.getStatus())
							.isForeign(casted.getIsForeign())
							.setNewPassword(casted.getPassword(), casted.getRepeatedPassword());
					
					if(newUser != null) {
						if(CFW.DB.Users.create(newUser)) {
							
							User userFromDB = CFW.DB.Users.selectByUsernameOrMail(newUser.username());
							if (CFW.DB.UserRoleMap.addUserToRole(userFromDB, CFW.DB.Roles.CFW_ROLE_USER, true)) {
								CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "User created successfully!");
								return;
							}
						}
					}
				}

			}
		});
		
		//--------------------------------------
		// Create Role Form
		CFWForm createRoleForm = new Role().toForm("cfwCreateRoleForm", "Create Role");
		
		createRoleForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
								
				if(origin != null) {
					
					origin.mapRequestParameters(request);
					Role role = (Role)origin;
					role.category("user");
					
					if( CFW.DB.Roles.create(role) ) {
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Role created successfully!");
					}
				}
				
			}
		});
	}
	
	class CreateUserForm extends CFWForm{
				
		protected CFWField<String> username = CFWField.newString(FormFieldType.TEXT, "Username")
				.addValidator(new LengthValidator(1, 255));
		
		protected CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, "Password")
				.addValidator(new LengthValidator(-1, 255))
				.addValidator(new PasswordValidator());
		
		protected CFWField<String> repeatedPassword = CFWField.newString(FormFieldType.PASSWORD, "Repeat Password")
				.addValidator(new NotNullOrEmptyValidator());
		
		private CFWField<String> status = CFWField.newString(FormFieldType.SELECT, "Status")
				.setOptions(new String[] {"Active", "Inactive"})
				.setDescription("Active users can login, inactive users are prohibited to login.")
				.addValidator(new LengthValidator(-1, 15));
		
		private CFWField<Boolean> isForeign = CFWField.newBoolean(FormFieldType.BOOLEAN, UserFields.IS_FOREIGN.toString())
											 .setValue(false);
		
		public CreateUserForm(String formID, String submitLabel) {
			super(formID, submitLabel);
			this.addField(username);
			this.addField(password);
			this.addField(repeatedPassword);
			this.addField(status);
			this.addField(isForeign);
		}
		
		public String getUsername() { return username.getValue(); }
		public String getPassword() { return password.getValue(); }
		public String getRepeatedPassword() { return repeatedPassword.getValue(); }
		public String getStatus() { return status.getValue(); }
		public boolean getIsForeign() { return isForeign.getValue(); }

	}
}
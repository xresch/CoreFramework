package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureAPI extends CFWAppFeature {

	private static Logger logger = CFWLog.getLogger(FeatureAPI.class.getName());
	
	public static final String PERMISSION_CFW_API = "API";
	public static final String PERMISSION_CFW_APITOKEN_MGMT = "API Token Managment";
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.api.resources";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
    	
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(APIToken.class);
		CFW.Registry.Objects.addCFWObject(APITokenPermission.class);
		CFW.Registry.Objects.addCFWObject(APITokenPermissionMap.class);
		
    	//----------------------------------
    	// Register Menu Entry
		CFW.Registry.Components.addToolsMenuItem(
				(MenuItem)new MenuItem("API")
					.faicon("fas fa-code")
					.addPermission(PERMISSION_CFW_API)
					.href("/app/api")	
				, null);
	}

	@Override
	public void initializeDB() {
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
		//Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		
		CFW.DB.Permissions.oneTimeCreate(
			new Permission(PERMISSION_CFW_API, FeatureUserManagement.CATEGORY_USER)
				.description("User can access the API."),
				true,
				false
		);
			
		CFW.DB.Permissions.oneTimeCreate(
			new Permission(PERMISSION_CFW_APITOKEN_MGMT, FeatureUserManagement.CATEGORY_USER)
				.description("User can manage API Tokens."),
				true,
				false
		);

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletAPI.class,  "/api");
    	app.addAppServlet(ServletAPITokenManagement.class,  "/api/tokenmanagement");
    	app.addUnsecureServlet(ServletAPILogin.class,  "/cfw/apilogin");
	}

	@Override
	public void startTasks() {
		// nothing to start
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}

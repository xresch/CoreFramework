package com.xresch.cfw.features.credentials;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.spaces.CFWSpace;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class UserAuditExecutorCredentialsAdmin implements UserAuditExecutor {

	@Override
	public String name() {
		return "Credentials: Admin";
	}
	
	@Override
	public String description() {
		return """
				<p>Checks in which spaces the User has admin rights for Credentials.</p>
			""";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//-----------------------------------
		// Check All Spaces
		String permissionToCheck = FeatureCredentials.PERMISSION_CREDENTIALS_ADMIN;
		ArrayList<CFWSpace> userSpaces = CFW.DB.Spaces.getSpaceListForUser(user.id());
		HashMap<String, Permission> permissions = CFW.DB.Permissions.selectPermissionsForUser(user);
		
		//-----------------------------------
		// Check All Spaces
		JsonArray result = new JsonArray();
		for(CFWSpace space : userSpaces ) {
			
			//-----------------------------------
			// Check Credentials
			String message = null;
			String permissionIDSpaced = CFW.DB.RolePermissionMap.createPermissionIDSpaced(space.id(), permissionToCheck);
			if( permissions.containsKey(permissionToCheck) ) {
				message = "The user is global Admin(by Role) and can access any credential in this space.";
			}else if( permissions.containsKey(permissionIDSpaced) ) {
				message = "The user is local Admin(by Group) and can access any credential in this space.";
			}
			
			if(message != null) {
				JsonObject adminObject = new JsonObject();
				adminObject.addProperty("Space_ID", space.id());
				adminObject.addProperty("Space", space.abbreviation());
				adminObject.addProperty("Message", message);

				result.add(adminObject);
			}
		}
		//---------------------------------
		// Fetch Data
		return result;
	
	}
}

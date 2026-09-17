package com.xresch.cfw.features.credentials;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/

public class UserAuditExecutorCredentialsUserDirect implements UserAuditExecutor {

	@Override
	public String name() {
		return "Credentials: Direct";
	}
	
	@Override
	public String description() {
		return """
				<p>Checks on which credentials the users has direct access(not by being part of a group). If you miss access to credentials:</p>
				<ul>
					<li><b>Space:</b>Check if you have access to the space, one of its sub spaces or the space is global .</li>
					<li><b>Is Shared:</b>Check with the owner of the credentials if the credential is set to be shared.</li>
				</ul>
			""";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.Credentials.permissionAuditByUser(user);
	
	}
}

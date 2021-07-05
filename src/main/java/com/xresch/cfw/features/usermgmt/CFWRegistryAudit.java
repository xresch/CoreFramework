package com.xresch.cfw.features.usermgmt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryAudit {
	private static Logger logger = CFWLog.getLogger(CFWRegistryAudit.class.getName());
	
	/***********************************************************************
	 * Menus
	 ***********************************************************************/ 
	private static LinkedHashMap<String, UserAuditExecutor> executorMap = new LinkedHashMap<>();
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void addUserAudit(UserAuditExecutor executor)  {
		if(!executorMap.containsKey(executor.name())) {
			executorMap.put(executor.name(), executor);
		}else {
			new CFWLog(logger)
				.severe("A UserAuditExecutor with the name '"+executor.name()+"' was already registered. Please choose another name"
						, new Throwable());
		}
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	protected static JsonArray auditUser(String userID) {
		
		User user = CFW.DB.Users.selectByID(Integer.parseInt(userID));
		return auditUser(user);
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	protected static JsonArray auditUser(User user) {
		ArrayList<User> userList = new ArrayList<>();
		userList.add(user);
		return auditUsers( userList);
	}
	/***********************************************************************
	 * 
	 ***********************************************************************/
	protected static JsonArray auditUsers(ArrayList<User> userList) {
		
		JsonArray resultArray = new JsonArray();
		
		for(User currentUser : userList) {
			
			JsonObject userItem = new JsonObject();
			userItem.addProperty("cfw-Type", "User");
			userItem.addProperty("username", currentUser.createUserLabel());
			JsonArray auditArray = new JsonArray();
			userItem.add("children", auditArray);
			
			for(UserAuditExecutor executor : executorMap.values()) {
				JsonObject auditItem = new JsonObject();
				auditItem.addProperty("cfw-Type", "Audit");
				auditItem.addProperty("name", executor.name());
				auditItem.addProperty("description", executor.description());

				auditItem.add("auditResult", executor.executeAudit(currentUser));
				
				auditArray.add(auditItem);
				
			}
			
			resultArray.add(userItem);
		}
		
		return resultArray;
	}
	


}

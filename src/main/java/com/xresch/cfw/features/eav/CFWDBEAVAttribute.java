package com.xresch.cfw.features.eav;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.eav.EAVAttribute.EAVAttributeFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVAttribute {

	private static final String TABLE_NAME = new EAVAttribute().getTableName();
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVAttribute.class.getName());
	
	
	/****************************************************************
	 * Check if the entity is in the given user.
	 * 
	 * @param entity to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsEAVEntityFavedByUser(EAVEntity entity, User user) {
		
		if(entity != null && user != null) {
			return checkIsEAVEntityInUserFavs(entity.id(), user.id());
		}else {
			new CFWLog(logger)
				.severe("The entity and user cannot be null. User: '"+entity+"', User: '"+user+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the entity exists by name.
	 * 
	 * @param entity to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsEAVEntityInUserFavs(int entityid, int userid) {
		
		return 0 != new EAVAttribute()
			.queryCache(CFWDBEAVAttribute.class, "checkIsEAVEntityInUser")
			.selectCount()
			.where(EAVAttributeFields.FK_ID_ENTITY.toString(), entityid)
			.and(EAVAttributeFields.NAME.toString(), userid)
			.executeCount();

	}

	

	

	
		
}

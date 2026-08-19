package com.xresch.cfw.features.usermgmt;

/**************************************************************************************************************
 * Gets triggered on permission changes of users and roles:
 * <ul>
 * 	<li>Assigning/Removing User to a Role or Group</li>
 * 	<li>Assigning/Removing Permission to a Role or Group</li>
 * 	<li>Deleting a Role or Group</li>
 * </ul>
 * 
 * This is useful to reset caches and such. Register this listener with FeatureUserManagement.registerChangeListener();
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 **************************************************************************************************************/

public interface CFWPermissionChangeListener {
	
	/********************************************************************
	 * Implement this method to do things when permissions change.
	 ********************************************************************/
	public void onChange();
	
}

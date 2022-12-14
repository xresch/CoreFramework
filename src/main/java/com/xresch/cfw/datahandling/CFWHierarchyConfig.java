package com.xresch.cfw.datahandling;

import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

//###########################################################################################
// 
//###########################################################################################
public abstract class CFWHierarchyConfig {
	
	private static final Logger logger = CFWLog.getLogger(CFWHierarchyConfig.class.getName());
	
	private String configIdentifier = "";
	private Object[] fieldsToRetrieve = null;
	private Object[] fieldsForAuditLog = null;
	private Class<? extends CFWObject> clazz;
	
	/***********************************************************************
	 * Create a hierarchy config.
	 * @param clazz of the CFWObject that will be hierarchical.
	 * @param fieldsToRetrieve used in case of using hierarchy sorter
	 * @param fieldsForAuditLog used in audit logs
	 ***********************************************************************/
	public CFWHierarchyConfig(Class<? extends CFWObject> clazz, Object[] fieldsToRetrieve, Object[] fieldsForAuditLog) {
		this.configIdentifier = clazz.getSimpleName().toLowerCase();
		this.clazz = clazz;
		this.fieldsToRetrieve = fieldsToRetrieve;
		this.fieldsForAuditLog = fieldsForAuditLog;
		
	}
	
	public String setConfigIdentifier() {
		return this.configIdentifier;
	}
	
	public String getConfigIdentifier() {
		return this.configIdentifier;
	}
	
	public Object[] getFieldsToRetrieve() {
		return this.fieldsToRetrieve;
	}
	
	public Object[] fieldsForAuditLog() {
		return this.fieldsForAuditLog;
	}
		
	/***********************************************************************
	 * Get a list of CFWObject instances.
	 * @param objectClass
	 ***********************************************************************/
	public CFWObject getCFWObjectInstance()  {

		try {
			CFWObject instance = clazz.newInstance();
			return instance;

		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return null;
	}
	
	/***************************************************************************
	 * Return true if the user is allowed to access the hierarchy starting
	 * from the given root element.
	 * @param rootElementID the id of the root element. Can be null for the full hierarchy.
	 * @return true if has access, false otherwise
	 ***************************************************************************/
	public abstract boolean canAccessHierarchy(String rootElementID);
	
	/***************************************************************************
	 * Return true if:
	 *  - the user is allowed to sort the sorted element into the target element.
	 *  - The sorted element can be part of the target element.
	 *  
	 * This method is responsible for creating error messages.
	 * 
	 * @param targetParent target element or null if it should be moved to root
	 * @param movedElement the element that is moved in the hierarchy
	 ***************************************************************************/
	public abstract boolean canBeReordered(CFWObject targetParent, CFWObject movedElement);
	
}
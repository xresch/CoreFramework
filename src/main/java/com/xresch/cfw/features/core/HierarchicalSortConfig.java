package com.xresch.cfw.features.core;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.ServletDashboardList;
import com.xresch.cfw.logging.CFWLog;

//###########################################################################################
// 
//###########################################################################################
public abstract class HierarchicalSortConfig {
	
	private static final Logger logger = CFWLog.getLogger(HierarchicalSortConfig.class.getName());
	
	private String type = "";
	private Object[] fieldnames = null;
	private Class<? extends CFWObject> clazz;
	
	public HierarchicalSortConfig(String type, Class<? extends CFWObject> clazz, Object... fieldnames) {
		this.type = type;
		this.clazz = clazz;
		this.fieldnames = fieldnames;
	}
	
	public String getType() {
		return this.type;
	}
	
	public Object[] getFieldnames() {
		return this.fieldnames;
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
	 * Return true if the user is allowed to access the type hierarchy starting
	 * from the given root element.
	 * @param rootElementID the id of the root element. Can be null for the full hierarchy.
	 * @return true if has access, false otherwise
	 ***************************************************************************/
	public abstract boolean canAccess(String rootElementID);
	
	/***************************************************************************
	 * Return true if the user is allowed to sort the sorted element into the
	 * target element.
	 ***************************************************************************/
	public abstract boolean canSort(String sortedElementID, String targetParentID);
	
}
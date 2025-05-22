package com.xresch.cfw.features.parameter;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterFields;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterMode;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterScope;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBParameter {
	
	private static Class<CFWParameter> cfwObjectClass = CFWParameter.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBParameter.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			//--------------------------
			// Check null
			CFWParameter parameter = (CFWParameter)object;
			
			if(parameter == null ) {
				new CFWLog(logger)
					.warn("The parameter cannot be null.", new Throwable());
				return false;
			}
			
			
			//--------------------------
			// Check Name used
			if(!checkIsParameterNameUsedOnCreate(parameter)) {
				return true;
			}else {
				new CFWLog(logger).severe("The parameter name is already in use: "+parameter.name());
				return false;
			}
		}
	};
	
	
	private static PrecheckHandler prechecksDeleteUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			//--------------------------
			// Check null
			CFWParameter parameter = (CFWParameter)object;
			
			if(parameter == null ) {
				new CFWLog(logger)
					.warn("The parameter cannot be null.", new Throwable());
				return false;
			}
			
			
			//--------------------------
			// Check Name used
			if(!checkIsParameterNameUsedOnUpdate(parameter)) {
				return true;
			}else {
				new CFWLog(logger).severe("The parameter name is already in use: "+parameter.name());
				return false;
			}
			
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(CFWParameter... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate, items); }
	public static boolean 	create(CFWParameter item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, item);}
	public static Integer	createGetPrimaryKey(CFWParameter item) 	{ return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWParameter... items) 	{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, items); }
	public static boolean 	update(CFWParameter item) 		{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(String id) 				{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, CFWParameterFields.PK_ID.toString(), Integer.parseInt(id)); }
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, CFWParameterFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDeleteUpdate, cfwObjectClass, itemIDs); }
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWParameter selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWParameterFields.PK_ID.toString(), id);
	}
		
	/***************************************************************
	 * Return a list of all user parameters
	 * 
	 * @return Returns a resultSet with all parameters or null.
	 ****************************************************************/
	public static String getParametersForDashboardAsJSON(String dashboardID) {
		
		ArrayList<CFWParameter> objectList = getParametersForDashboard( dashboardID );
		
		JsonArray array = new JsonArray();
		for(CFWParameter param : objectList) {
			array.add(param.toJson());
		}
		return CFW.JSON.toJSON(array);
		
	}
	
	/***************************************************************
	 * Return a list of all dashboard parameters
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWParameter> getParametersForDashboard(String dashboardID) {
		
		return new CFWSQL(new CFWParameter())
				.queryCache()
				.select()
				.where(CFWParameterFields.FK_ID_DASHBOARD, dashboardID)
				.getAsObjectListConvert(CFWParameter.class);
		
	}
	
	/***************************************************************
	 * Return a list of all query parameters
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWParameter> getParametersForQuery(int queryID) {
		return getParametersForQuery(""+queryID);
	}
	
	/***************************************************************
	 * Return a list of all query parameters
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWParameter> getParametersForQuery(String queryID) {
		
		return new CFWSQL(new CFWParameter())
				.queryCache()
				.select()
				.where(CFWParameterFields.FK_ID_QUERY, queryID)
				.getAsObjectListConvert(CFWParameter.class);
		
	}
	
	
	/***************************************************************
	 * Return the parameter as JSON string.
	 * 
	 ****************************************************************/
	public static String getParameterAsJSON(String parameterID) {
		
		return new CFWSQL(new CFWParameter())
				.queryCache()
				.select()
				.where(CFWParameterFields.PK_ID, parameterID)
				.getAsJSON();
		
	}
	
	
	/***************************************************************
	 * @param scope TODO
	 * @return Returns true if the parameter name is already in use
	 * for the dashboard/query, ignores the the given parameter in the check.
	 ****************************************************************/
	public static boolean checkIsParameterNameUsedOnUpdate(CFWParameter parameter) {
		
		String idField = CFWParameterFields.FK_ID_DASHBOARD.toString();
		Integer ID = parameter.foreignKeyDashboard();
		if(CFWParameterScope.query.equals(parameter.scope())) {
			idField = CFWParameterFields.FK_ID_QUERY.toString();
			ID = parameter.foreignKeyQuery();
		}
		
		return  0 < new CFWSQL(new CFWParameter())
				//.queryCache() cannot cache this query
				.selectCount()
				.where(idField, ID)
				.and(CFWParameterFields.NAME, parameter.name())
				.and().not().is(CFWParameterFields.PK_ID, parameter.id())
				.executeCount();
		
	}
	/***************************************************************
	 * @param scope TODO
	 * @return Returns true if the parameter name is already in use
	 * for the dashboard/query.
	 ****************************************************************/
	public static boolean checkIsParameterNameUsedOnCreate(CFWParameter parameter) {
		
		
		String fieldname = CFWParameterFields.FK_ID_DASHBOARD.toString();
		Integer ID = parameter.foreignKeyDashboard();
		if(parameter.scope() == CFWParameterScope.query) {
			fieldname = CFWParameterFields.FK_ID_QUERY.toString();
			ID = parameter.foreignKeyQuery();
		}
		
		return  0 < new CFWSQL(new CFWParameter())
				//.queryCache() cannot cache this query
				.selectCount()
				.where(fieldname, ID)
				.and(CFWParameterFields.NAME, parameter.name())
				.executeCount();
		
	}
	
	/***************************************************************
	 * @return Returns true if the parameter is of the specified dashboard
	 ****************************************************************/
//	public static boolean checkIsParameterOfDashboard(String dashboardID, String parameterID) {
//		
//		return  1 == new CFWSQL(new CFWParameter())
//				.queryCache()
//				.selectCount()
//				.where(CFWParameterFields.FK_ID_DASHBOARD, dashboardID)
//				.and(CFWParameterFields.PK_ID, parameterID)
//				.executeCount();
//		
//	}
	
	
	/***************************************************************
	 * Return a list of available parameters for the given field.
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWObject> getAvailableParamsForDashboard(String dashboardID, String widgetType, String label, boolean allowGenericParams) {
		
		CFWSQL sql = new CFWParameter()
				.queryCache(CFWDBParameter.class, "autocompleteParametersForDashboard"+allowGenericParams)
				.select()
				.where(CFWParameterFields.FK_ID_DASHBOARD, dashboardID)
				.and(CFWParameterFields.MODE, CFWParameterMode.MODE_SUBSTITUTE.toString())
				.and().custom("(")
						.is(CFWParameterFields.WIDGET_TYPE, widgetType);
						if(allowGenericParams) sql.or().isNull(CFWParameterFields.WIDGET_TYPE);
						
			sql.custom(")")
				.and().custom("(")
					.is(CFWParameterFields.LABEL, label);
					if(allowGenericParams) sql.or().isNull(CFWParameterFields.WIDGET_TYPE);
					
		return sql.custom(")")
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return a list of available parameters for the given field.
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWObject> getAvailableParamsForQuery(String queryID, String label) {
		
		return new CFWParameter()
				.queryCache(CFWDBParameter.class, "autocompleteParametersForQuery")
				.select()
				.where(CFWParameterFields.FK_ID_QUERY, queryID)
				.and().is(CFWParameterFields.LABEL, label)
				.getAsObjectList()
				;
				
	}
	

	
	/***************************************************************
	 * Return a JSON string for export.
	 * @param scope TODO
	 * 
	 * @return Returns a JsonArray or null on error.
	 ****************************************************************/
	public static JsonArray getJsonArrayForExport(CFWParameterScope scope, String itemID) {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)
		|| CFW.DB.Dashboards.checkCanEdit(itemID)) {
			
			CFWParameterFields idField;
			switch (scope) {
				case dashboard: idField = CFWParameterFields.FK_ID_DASHBOARD; break;
				case query:		idField = CFWParameterFields.FK_ID_QUERY; break;
				default:		return null;
			}
			
			CFWSQL selectForExport = new CFWSQL(new CFWParameter())
				//.queryCache() cannot cache this query
				.select();
			
			if(!Strings.isNullOrEmpty(itemID)) {
				selectForExport.where(idField, itemID);
				return  selectForExport.getObjectsAsJSONArray();
			}
							
			return null;
		 
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
			return null;
		}
	}
	

}

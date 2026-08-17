package com.xresch.cfw.features.spaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWDBSpaces {
	
	private static Class<CFWSpace> cfwObjectClass = CFWSpace.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBSpaces.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWSpace HierarchicalSpace = (CFWSpace)object;
			
			if(HierarchicalSpace == null || HierarchicalSpace.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a firstname for the space.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
		
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(CFWSpace... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(CFWSpace item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(CFWSpace item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	public static CFWSpace createGetObject(CFWSpace item) { 
		return CFWDBSpaces.selectByID(
				CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item)
			);
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWSpace... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(CFWSpace item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	

	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		CFWSpace person = selectByID(id);
		if(person != null) {
			person.id(null);
			return create(person);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWSpace selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWSpaceFields.PK_ID.toString(), id);
	}
	
	public static CFWSpace selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWSpaceFields.PK_ID.toString(), id);
	}
	
	public static CFWSpace selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWSpaceFields.NAME.toString(), name);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static JsonArray getHierarchyForSpaceAsJson(String spaceID) {
		
		CFWHierarchy<CFWSpace> hierarchy = 
				new CFWHierarchy<CFWSpace>(new CFWSpace())
					.setFilter(
							new CFWSQL(null)
								.and().custom("(")
									.arrayContains(CFWHierarchy.H_LINEAGE, spaceID)
									.or(CFWSpaceFields.PK_ID, spaceID)
								.custom(")")
							)
					.fetchAndCreateHierarchy();
		
		return hierarchy.toJSONArray();
		
	}
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String getFullHierarchyAsJson() {
		
		CFWHierarchy<CFWSpace> hierarchy = 
				new CFWHierarchy<CFWSpace>(new CFWSpace())
					.fetchAndCreateHierarchy();
		
		return CFW.JSON.toJSON(hierarchy.toJSONArray());
		
	}
	
	/*****************************************************************************
	 *  Returns a list of all Spaces.
	 *****************************************************************************/
	public static String getSpaceListAsJSON() {
		
		return new CFWSQL(new CFWSpace())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	/*****************************************************************************
	 *  Returns a list of spaces with type "ORG".
	 *****************************************************************************/
	public static JsonArray getSpaceListForUser() {

		
		if(CFW.Context.Request.hasPermission(FeatureSpace.PERMISSION_SPACES_ADMIN)) {
			//--------------------------------
			// Return All Spaces for Admins
			return new CFWSQL(new CFWSpace())
					.queryCache() 
					.select(CFWSpaceFields.PK_ID, CFWSpaceFields.ABBREVIATION, CFWSpaceFields.NAME)
					.where(CFWSpaceFields.TYPE, CFWSpaceType.ORG)
					.getAsJSONArray();
		}else {
			//--------------------------------
			// Return Specific Spaces for User
			return new CFWSQL(new CFWSpace())
					.queryCache()   
					.loadSQLResource(FeatureSpace.PACKAGE_RESOURCE
							, "sql_getSpaceListForUser.sql"
							, CFW.Context.Request.getUserID()
							, CFW.Context.Request.getUserID()
							)
					.getAsJSONArray();
		}
		
	}
	
	/*****************************************************************************
	 *  Returns a list of spaces with type "ORG".
	 *****************************************************************************/
	public static JsonArray getSpaceListForUserOrganizeView() {
		
		if( CFW.Context.Request.hasPermission(FeatureSpace.PERMISSION_SPACES_ADMIN) ) {
			return new CFWSQL(new CFWSpace())
					.queryCache() 
					.select(CFWSpaceFields.PK_ID, CFWSpaceFields.ABBREVIATION, CFWSpaceFields.NAME)
					.where(CFWSpaceFields.TYPE, CFWSpaceType.ORG)
					.getAsJSONArray();
		}else {
			return getSpaceListForUser();
		}
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String getPartialSpaceListAsJSON(String pageSize, String pageNumber, String filterquery, String sortby, boolean sortAscending) {
		return getPartialSpaceListAsJSON(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, sortAscending);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String getPartialSpaceListAsJSON(int pageSize, int pageNumber, String filterquery, String sortby, boolean sortAscending) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the Person Object

		//Do not cache this statement
		return new CFWSQL(new CFWSpace())
				.fulltextSearchLucene(filterquery, sortby, sortAscending, pageSize, pageNumber)
				.getAsJSON();
		
		
		//===========================================
		// Manual Alternative
		//===========================================
		
//		if(Strings.isNullOrEmpty(filterquery)) {
//			//-------------------------------------
//			// Unfiltered
//			return new CFWSQL(new Person())
//				.queryCache()
//				.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
//				.select()
//				.limit(pageSize)
//				.offset(pageSize*(pageNumber-1))
//				.getAsJSON();
//		}else {
//			//-------------------------------------
//			// Filter with fulltext search
//			// Enabled by CFWObject.enableFulltextSearch()
//			// on the Person Object
//			return new CFWSQL(new Person())
//					.queryCache()
//					.select()
//					.fulltextSearch()
//						.custom(filterquery)
//						.build(pageSize, pageNumber)
//					.getAsJSON();
//		}
		
	}	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new CFWSpace())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static AutocompleteResult autocompletePostForRootSpace(String searchString, int spaceid, int limit) {
		return autocompleteSpaceForRootSpace(searchString, CFWSpaceType.POST, spaceid, limit);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static AutocompleteResult autocompleteSpaceForRootSpace(String searchString, int spaceid, int limit) {
		return autocompleteSpaceForRootSpace(searchString, null, spaceid, limit);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	private static AutocompleteResult autocompleteSpaceForRootSpace(String searchString, CFWSpaceType typeFilter, int spaceid, int limit) {
		
		//---------------------------------------------
		// Fetche se Data
		String likeString = "%"+searchString+"%";
		
		CFWSQL sql = new CFWSQL(new CFWSpace())
				//.queryCache() cannot cache
				.select()
				.where()
					.custom("(")
						.arrayContains(CFWHierarchy.H_LINEAGE, spaceid)
						.or(CFWSpaceFields.PK_ID, spaceid)
					.custom(")")
					.and().custom("(")
						.like(CFWSpaceFields.NAME, likeString)
						.or().like(CFWSpaceFields.ABBREVIATION, likeString)
					.custom(")");
					if(typeFilter != null) {
						sql.and(CFWSpaceFields.TYPE, typeFilter.toString());
					}
			
		ArrayList<CFWSpace> spaceList = 
				sql.limit(limit)
					.getAsObjectListConvert(CFWSpace.class)
					;
		
		//---------------------------------------------
		// Creatione di Listo
		AutocompleteList list = new AutocompleteList();

		for(int i = 0; i < spaceList.size(); i++ ) {
			CFWSpace current = spaceList.get(i);
			list.addItem(current.id(), current.createSpaceLabel(), current.createBreadcrumbsString());
		}
		return new AutocompleteResult(list);
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static AutocompleteResult autocompletePost(String searchString, int limit) {
		return autocompleteSpace(searchString, CFWSpaceType.POST, limit);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static AutocompleteResult autocompleteSpace(String searchString, int limit) {
		return autocompleteSpace(searchString, null, limit);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	private static AutocompleteResult autocompleteSpace(String searchString, CFWSpaceType typeFilter, int limit) {
		
		//---------------------------------------------
		// Fetche se Data
		String likeString = "%"+searchString+"%";
		
		CFWSQL sql = new CFWSQL(new CFWSpace())
				//.queryCache() cannot cache
				.select()
				.where()
					.custom("(")
						.like(CFWSpaceFields.NAME, likeString)
						.or().like(CFWSpaceFields.ABBREVIATION, likeString)
					.custom(")");
					if(typeFilter != null) {
						sql.and(CFWSpaceFields.TYPE, typeFilter.toString());
					}
			
		ArrayList<CFWSpace> spaceList = 
				sql.limit(limit)
					.getAsObjectListConvert(CFWSpace.class)
					;
		
		//---------------------------------------------
		// Creatione di Listo
		AutocompleteList list = new AutocompleteList();

		for(int i = 0; i < spaceList.size(); i++ ) {
			CFWSpace current = spaceList.get(i);
			list.addItem(current.id(), current.createSpaceLabel(), current.createBreadcrumbsString());
		}
		return new AutocompleteResult(list);
	
	}
}

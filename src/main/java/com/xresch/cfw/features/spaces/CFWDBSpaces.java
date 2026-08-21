package com.xresch.cfw.features.spaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWDBSpaces {
	
	private static final String BREADCRUMBS = "BREADCRUMBS";

	private static Class<CFWSpace> cfwObjectClass = CFWSpace.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBSpaces.class.getName());
	
	// userID and SpaceList
	private static LinkedHashMap<Integer, CFWSpace> allSpacesCache = null;
	
	// userID and SpaceList
	private static Cache<Integer, ArrayList<CFWSpace>> userSpacelistCache = CFW.Caching.addCache("CFW SpaceList for User", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(10000)
				.expireAfterAccess(65, TimeUnit.MINUTES)
			);
	
	// userID and SpaceList with 
	private static Cache<Integer, JsonArray> userBreadcrumbedSpacelistCache = CFW.Caching.addCache("CFW SpaceList(Breadcrumbed) for User", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(10000)
				.expireAfterAccess(65, TimeUnit.MINUTES)
			);
	
	//####################################################################################################
	// Cache Management
	//####################################################################################################
	public static void resetCaches(){
		
		userSpacelistCache.invalidateAll();
		userBreadcrumbedSpacelistCache.invalidateAll();
		
		allSpacesCache = getSpaceListAsArrayList();
		
	}
	
	/**************************************************************
	 * Returns a space from cache.
	 * @param id
	 * @return space or null if not found
	 **************************************************************/
	public static CFWSpace getFromCache(Integer id){
		
		if(allSpacesCache != null) {
			return allSpacesCache.get(id);
		}
		
		return null;
	}
		
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
	public static boolean	create(CFWSpace... items) 	{
			boolean success = CFWDBDefaultOperations.create(prechecksCreateUpdate, items);
			resetCaches();
			return success;
	}
	public static boolean 	create(CFWSpace item) { 
		boolean success = CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		resetCaches();
		return success;
	}
	public static Integer createGetPrimaryKey(CFWSpace item) { 
		Integer primaryKey = CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);
		resetCaches();
		return primaryKey;
	}
	public static CFWSpace createGetObject(CFWSpace item) { 
		CFWSpace newObject =CFWDBSpaces.selectByID(
				CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item)
			);
		resetCaches();
		return newObject;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWSpace... items) { 
		boolean success =  CFWDBDefaultOperations.update(prechecksCreateUpdate, items); 
		resetCaches();
		return success;
	}
	public static boolean 	update(CFWSpace item) { 
		boolean success =  CFWDBDefaultOperations.update(prechecksCreateUpdate, item); 
		resetCaches();
		return success;
	}
	

	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		CFWSpace space = selectByID(id);
		if(space != null) {
			space.id(null);
			return create(space);
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
	public static LinkedHashMap<Integer, CFWSpace> getSpaceListAsArrayList() {
		
		return new CFWSQL(new CFWSpace())
				.queryCache()
				.select()
				.getAsKeyObjectMap(CFWSpace.class);
		
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
	public static LinkedHashMap<Integer,String> getSpaceListForUserOptions() {
		
		LinkedHashMap<Integer,String> result = new LinkedHashMap<>();
		
		Integer userID = CFW.Context.Request.getUserID();
		
		if(userID != null) {
			JsonArray spaceList = getSpaceListForUserAsJsonWithBreadcrumbs();
			for(JsonElement element : spaceList) {
				JsonObject space = (JsonObject)element;
				Integer id = space.get(CFWSpaceFields.PK_ID.toString()).getAsInt();
				String  breadcrumbs = space.get(BREADCRUMBS).getAsString();
				result.put(id, breadcrumbs );
			}
		}
		
		return result;		
	}
	
	/*****************************************************************************
	 *  Returns a list of spaces with type "ORG".
	 *****************************************************************************/
	public static JsonArray getSpaceListForUserAsJsonWithBreadcrumbs() {
		
		JsonArray spaceList = null;
		int userID = CFW.Context.Request.getUserID();
		try {
			// cache to avoid overloading backend systems.
			spaceList = userBreadcrumbedSpacelistCache.get(userID, new Callable<JsonArray>() {
				@Override
				public JsonArray call() throws Exception {
					ArrayList<CFWSpace> spaces = getSpaceListForUser();
					
					//-----------------------------
					// Add Breadcrumbs and Sort
					TreeMap<String, JsonObject> sortedByBreadcrumbs = new TreeMap<>();
					for(CFWSpace space : spaces) {
						JsonObject object = new JsonObject();
						object.addProperty(CFWSpaceFields.PK_ID.toString(), space.id());
						object.addProperty(CFWSpaceFields.ABBREVIATION.toString(), space.abbreviation());
						object.addProperty(CFWSpaceFields.NAME.toString(), space.name());
						object.addProperty(CFWHierarchy.H_DEPTH, (Integer)space.getField(CFWHierarchy.H_DEPTH).getValue());
						object.addProperty(CFWHierarchy.H_PARENT, (Integer)space.getField(CFWHierarchy.H_PARENT).getValue());
						
						String breadcrumbs = space.createBreadcrumbsString();
						object.addProperty(BREADCRUMBS, breadcrumbs);
						
						sortedByBreadcrumbs.put(breadcrumbs, object);
						
					}
					
					//-----------------------------
					// Create Array
					JsonArray breadcrumbedArray = new JsonArray();
					for(JsonObject object : sortedByBreadcrumbs.values()) {
						breadcrumbedArray.add(object);
					}
							
					return breadcrumbedArray;
				}
			});

			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while loading widget from DB or Cache: "+e.getMessage(), e);
		}
		
		return spaceList;
		
		
		
	}
		
	/*****************************************************************************
	 *  Returns a list of spaces with type "ORG".
	 *****************************************************************************/
	public static ArrayList<CFWSpace> getSpaceListForUser() {
		ArrayList<CFWSpace> spaceList = null;
		int userID = CFW.Context.Request.getUserID();
		try {
			// cache to avoid overloading backend systems.
			spaceList = userSpacelistCache.get(userID, new Callable<ArrayList<CFWSpace>>() {
				@Override
				public ArrayList<CFWSpace> call() throws Exception {
					return getSpaceListForUserSQL(userID)
								.getAsObjectListConvert(CFWSpace.class);
				}
			});

			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while loading widget from DB or Cache: "+e.getMessage(), e);
		}
		
		return spaceList;
		

	}
	
	/*****************************************************************************
	 *  Returns a list of spaces for the current user.
	 *****************************************************************************/
	public static CFWSQL getSpaceListForUserSQL() {
		return getSpaceListForUserSQL(CFW.Context.Request.getUserID());
	}
	
	/*****************************************************************************
	 *  Returns a list of spaces with type "ORG".
	 *****************************************************************************/
	public static CFWSQL getSpaceListForUserSQL(int userID) {

		if(CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN)) {
			//--------------------------------
			// Return All Spaces for Admins
			return new CFWSQL(new CFWSpace())
					.queryCache() 
					.select(CFWSpaceFields.PK_ID
							, CFWSpaceFields.ABBREVIATION
							, CFWSpaceFields.NAME
							, CFWHierarchy.H_DEPTH
							, CFWHierarchy.H_PARENT
							, CFWHierarchy.H_LINEAGE
						)
					.where(CFWSpaceFields.IS_ENABLED, true)
					;
		}else {
			//--------------------------------
			// Return Specific Spaces for User
			
			return new CFWSQL(new CFWSpace())
					.queryCache()   
					.loadSQLResource(FeatureSpaces.PACKAGE_RESOURCE
							, "sql_getSpaceListForUser.sql"
							, userID
							, userID
							, userID
							, userID
							)
					;
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
	public static boolean checkCurrentUserHasAccessToSpace(int spaceID) {
				
		for(CFWSpace space : getSpaceListForUser()) {
			if(space.id() == spaceID) {
				return true;
			}
		}
		return false;
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static AutocompleteResult autocompletePostForRootSpace(String searchString, int spaceid, int limit) {
		return autocompleteSpaceForRootSpace(searchString, CFWSpaceType.SPACE, spaceid, limit);
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
		return autocompleteSpace(searchString, CFWSpaceType.SPACE, limit);
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

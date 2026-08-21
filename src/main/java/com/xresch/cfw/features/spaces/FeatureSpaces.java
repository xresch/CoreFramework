package com.xresch.cfw.features.spaces;

import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.features.usermgmt.CFWPermissionChangeListener;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class FeatureSpaces extends CFWAppFeature {
	
	
	
	public static final String FK_ID_SPACE = "FK_ID_SPACE";
	
	public static final String FEATURE_NAME = "Spaces";
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.features.spaces.resources";
	
	public static final String PERMISSION_SPACES_VIEWER = "Space: Viewer";
	public static final String PERMISSION_SPACES_ADMIN = "Space: Admin All";
	public static final String PERMISSION_SPACES_CREATE = "Space: Create Spaces";
	
	// Default spaces created when activating the feature
	public enum FeatureSpacesDefaults{
		  ALL(0, "All", "This is a special space that can display everything in every space. Useful for admin purposes.")
		, DEFAULT(1, "Default", "The default space. Contains all things that existed before the space feature has been activated." )
		, GLOBAL(2, "Global", "A default global root space. Everything in this space can be selected by everyone.")
		;
		
		private int id;
		private String label;
		private String description;
		
		private FeatureSpacesDefaults(int id, String label, String description){
			this.id = id;
			this.label = label;
			this.description = description;
		}
		
		public int id() { return id; }
		public String label() { return label; }
		public String description() { return description; }
	}
	/************************************************************************************
	 * Return the unique name of this feature for the feature cfw_spacesment.
	 * If this method returns null(default), the feature will not be visible in the 
	 * Feature Management.
	 * 
	 ************************************************************************************/
	public String getNameForFeatureManagement() {
		return FEATURE_NAME;
	};
	
	/************************************************************************************
	 * Register a description for the feature cfw_spacesment.
	 ************************************************************************************/
	public String getDescriptionForFeatureManagement() {
		return "Enables space cfw_spacesment for multi-client capabilities. ";
	};
	
	/************************************************************************************
	 * Return if the cfw_spacesd feature is active by default or if an admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return false;
	};
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
				
		//----------------------------------
    	// Register Menu			
		CFW.Registry.Components.addToolsMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Spaces")
				.faicon("fas fa-sitemap")
				.addPermission(PERMISSION_SPACES_VIEWER)
				.addPermission(PERMISSION_SPACES_ADMIN)
				.addPermission(PERMISSION_SPACES_CREATE)
				.href("/app/spaces")
				, null);
		
		//-------------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWSpace.class);		
		CFW.Registry.Objects.addCFWObject(CFWSpaceUserMap.class);		
		CFW.Registry.Objects.addCFWObject(CFWSpaceUserGroupsMap.class);		
		CFW.Registry.Objects.addCFWObject(CFWSpaceAdminMap.class);		
		CFW.Registry.Objects.addCFWObject(CFWSpaceAdminGroupsMap.class);		
		
		//-------------------------------------
    	// Register Change Listener
		FeatureUserManagement.registerChangeListener(new CFWPermissionChangeListener() {
			@Override
			public void onChange() {
				CFW.DB.Spaces.resetCaches();
			}
		});
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void initializeDB() {
			
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_SPACES_VIEWER, FeatureUserManagement.CATEGORY_USER)
					.description("The user can view the full hierarchy of the space in which he is part of."),
				true,
				true);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_SPACES_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("The user can create, view and cfw_spaces all root spaces."),
				true,
				false);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_SPACES_CREATE, FeatureUserManagement.CATEGORY_USER)
					.description("Grants permission to create new root spaces."),
				true,
				false);
						
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void addFeature(CFWApplicationExecutor executor) {
		
		executor.addAppServlet(ServletSpaces.class, "/spaces");
		
		createDefaultSpaces();
		
		if(CFWDBSpaces.getCount() == 3) {
			//createTestdataLarge();
			new CFWSpacesTestdataGenerator(true).generateHierarchy();
		}
		
		CFW.DB.Spaces.resetCaches();

	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void createDefaultSpaces() {
		//-------------------------------------
		// Create Default Spaces
		if(CFWDBSpaces.getCount() == 0) {
			
			//-------------------------------------
			// Admin
			Role superuserRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_SUPERUSER);
			LinkedHashMap<String, String> superuserGroup = new LinkedHashMap<>();
			superuserGroup.put(superuserRole.id()+"", superuserRole.name());
			
			Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
			LinkedHashMap<String, String> userGroup = new LinkedHashMap<>();
			userGroup.put(userRole.id()+"", userRole.name());
			
			//-------------------------------------
			// ALL
			CFWSpace spaceAll = new CFWSpace()
					.type(CFWSpaceType.ROOT_SPACE)
					.abbreviation("ALL")
					.id(FeatureSpacesDefaults.ALL.id())
					.name(FeatureSpacesDefaults.ALL.label())
					.description(FeatureSpacesDefaults.ALL.description())
					.adminGroups(superuserGroup)
					;
			
			Integer allID = CFWHierarchy.create(null, spaceAll);
			
			spaceAll.saveSelectorFields();
			
			//-------------------------------------
			// DEFAULT
			CFWSpace spaceDefault = new CFWSpace()
					.type(CFWSpaceType.ROOT_SPACE)
					.abbreviation("DEF")
					.id(FeatureSpacesDefaults.DEFAULT.id())
					.name(FeatureSpacesDefaults.DEFAULT.label())
					.description(FeatureSpacesDefaults.DEFAULT.description())
					.assignedGroups(userGroup)
					;
			
			Integer defaultID = CFWHierarchy.create(null, spaceDefault);
			
			spaceDefault.saveSelectorFields();
			
			//-------------------------------------
			// DEFAULT
			CFWSpace spaceGlobal = new CFWSpace()
					.type(CFWSpaceType.ROOT_SPACE)
					.abbreviation("GLB")
					.id(FeatureSpacesDefaults.GLOBAL.id())
					.name(FeatureSpacesDefaults.GLOBAL.label())
					.description(FeatureSpacesDefaults.GLOBAL.description())
					.adminGroups(superuserGroup)
					.isGlobal(true)
					;
			
			Integer globalID = CFWHierarchy.create(null, spaceGlobal);
			
			spaceGlobal.saveSelectorFields();
			
		}
		
	}
	

	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void startTasks() {
		// TODO Auto-generated method stub

	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
	}
	
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void addSpacesCommonJS(HTMLResponse html) {
		html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSpaces.PACKAGE_RESOURCE, "om_spaces_common.js");
	}
	
	/**********************************************************************************
	 * If the spaces feature is active, returns a selector field that is a foreign key
	 * of the CFWSpace object. If it is inactive, this method returns nothing.
	 * @param parent object this field should be assigned too
	 * @param isHidden if the field is hidden.
	 * @return field with the name FeatureSpaces.FK_ID_SPACE
	 **********************************************************************************/
	public static CFWField<Integer> createSpaceSelectorField(CFWObject parent, boolean isHidden) {
		
		if( ! FeatureCore.isFeatureActive(FeatureSpaces.FEATURE_NAME) ) {
			return null;
		}
		
		CFWField<Integer> field;
		
		if(isHidden) {
			field = CFWField.newInteger(FormFieldType.HIDDEN, FK_ID_SPACE);
		}else {
			field = CFWField.newInteger(FormFieldType.SELECT, FK_ID_SPACE);
		}
		
		field.setColumnDefinition("INT DEFAULT "+ FeatureSpacesDefaults.DEFAULT.id() )
			.setForeignKeyCascade(parent, CFWSpace.class, CFWSpaceFields.PK_ID)
			.setLabel("Space")
			.setDescription("The space this entity belongs to.")
			.setOptions(CFW.DB.Spaces.getSpaceListForUserOptions())
			.apiFieldType(FormFieldType.SELECT);
		
		Integer selectedSpace = CFW.Context.Request.getSelectedSpaceID();
		if(selectedSpace != null) {
			field.setValue(selectedSpace);
		}
		return field;
	}
	
	/***********************************************************************
	 * Adds information for the space to the given objects in the array.
	 * 
	 * @param array containing JsonObjects with field FK_ID_SPACE
	 * @returns the array for chaining
	 * 
	 ***********************************************************************/
	public static JsonArray addSpacesInfoToJSON(JsonArray array) {
		for(JsonElement element : array) {
			JsonObject object = element.getAsJsonObject();
			
			int spaceID = object.get(FK_ID_SPACE).getAsInt();
			CFWSpace space = CFW.DB.Spaces.getFromCache(spaceID);
			if(space != null) {
				object.addProperty("SPACE_ABBREV", space.abbreviation());
			}
		}
		
		return array;
	}
	
	/**********************************************************************************
	 * Returns a partial query which will filter by the column "FK_ID_SPACE" and returns
	 * entities:
	 * <ul>
	 *   <li>Directly in the selected space.</li>
	 *   <li>In parent spaces of selected space.</li>
	 *   <li>In global spaces of Type ROOT_SPACE.</li>
	 *   <li>In global spaces of Type SPACE with the same ROOT_SPACE.</li>
	 * </ul>  
	 **********************************************************************************/
	public static CFWSQL getSQLFilter() {
		boolean filterInclusive = CFW.Context.Request.getFilterSpaceInclusive();
		int spaceID = CFW.Context.Request.getSelectedSpaceID();
		
		//--------------------------
		// Filter Inclusive
		if(filterInclusive) {
			return getSQLFilterInclusive(spaceID);
		}else {
			return getSQLFilterExclusive(spaceID);
		}
	}
	
	/**********************************************************************************
	 * Returns a partial query which will filter by the column "FK_ID_SPACE" and returns
	 * entities:
	 * <ul>
	 *   <li>Directly in the selected space.</li>
	 *   <li>In parent spaces of selected space.</li>
	 *   <li>In global spaces of Type ROOT_SPACE.</li>
	 *   <li>In global spaces of Type SPACE within the same parent chain.</li>
	 * </ul>  
	 * 
	 * @return  CFWSQL partial SQL 
	 **********************************************************************************/
	public static CFWSQL getSQLFilterInclusive() {
		return getSQLFilterInclusive( CFW.Context.Request.getSelectedSpaceID() );
	}
	
	/**********************************************************************************
	 * Returns a partial query which will filter by the column "FK_ID_SPACE" and returns
	 * entities:
	 * <ul>
	 *   <li>Directly in the selected space.</li>
	 *   <li>In parent spaces of selected space.</li>
	 *   <li>In global spaces of Type ROOT_SPACE.</li>
	 *   <li>In global spaces of Type SPACE within the same parent chain.</li>
	 * </ul>  
	 * 
	 * @param spaceID to filter by
	 * 
	 * @return  CFWSQL partial SQL 
	 **********************************************************************************/
	public static CFWSQL getSQLFilterInclusive(int spaceID) {
		
			return new CFWSQL(null)
					.queryCache()   
					.loadSQLResource(FeatureSpaces.PACKAGE_RESOURCE
							, "sql_getSQLFilterInclusive.sql"
							, spaceID
							, spaceID
							, spaceID
							, spaceID
							);
	}
	
	/**********************************************************************************
	 * Returns a partial query which will filter by the column "FK_ID_SPACE" and returns
	 * entities belonging to the given space id. 
	 * 
	 * @return  CFWSQL partial SQL 
	 **********************************************************************************/
	public static CFWSQL getSQLFilterExclusive() {
		return getSQLFilterExclusive( CFW.Context.Request.getSelectedSpaceID() );
	}
	
	/**********************************************************************************
	 * Returns a partial query which will filter by the column "FK_ID_SPACE" and returns
	 * entities belonging to the given space id. 
	 * 
	 * @param spaceID to filter by
	 * 
	 * @return  CFWSQL partial SQL 
	 **********************************************************************************/
	public static CFWSQL getSQLFilterExclusive(int spaceID) {
		
		return new CFWSQL(null)
				.custom(" ( FK_ID_SPACE = ?) ", spaceID);
	}
		

}

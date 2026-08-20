package com.xresch.cfw.features.spaces;

import java.util.LinkedHashMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.features.usermgmt.CFWPermissionChangeListener;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWRandom;

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
			createTestdataHierarchy(0);
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
	public void createTestdataHierarchy(int index) {
		
		String[] characters = new String[] {"A", "B", "C", "D"};
		
		//----------------------------------
		// Is Done
		if(index > characters.length - 2) {
			return;
		}
		
		//----------------------------------
		// Create root space
		String char1 = characters[index];
		String char2 = characters[index+1];
		String rootName = char1 + char2;
		
		CFWSpace rootSpace = new CFWSpace()
			.type(CFWSpaceType.ROOT_SPACE)
			.name("Root " + rootName)
			.abbreviation("R" + rootName);
		
		Integer rootID = CFWHierarchy.create(null, rootSpace);
		
		if(rootID == null) {
			return; // error
		}
		
		//-----------------------------
		// Create Credentials
		CFWCredentials credentialsRoot = new CFWCredentials()
				.fkidSpace(rootID)
				.foreignKeyOwner(1)
				.name("CREDS Root "+rootName)
				.isShared(true)
				.tags("tag_R"+rootName)
				;
		
		Integer credsRootID = CFW.DB.Credentials.createGetPrimaryKey(credentialsRoot);
				
		//-----------------------------
		// Create Subordinate Trees
		createSubordinatesHierarchy(rootID, rootID, char1, 0, 2);
		createSubordinatesHierarchy(rootID, rootID, char2, 0, 2);
		
		//----------------------------------
		// Create next Root
		createTestdataHierarchy(index+2);
					
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void createSubordinatesHierarchy(int rootID, int parentID, String character, int currentDepth, int maxDepth) {
		
		//#########################################################
		// Regular Space 
		//#########################################################
		
		//-----------------------------
		// Make Space
		String label = character + "L" + currentDepth;
		CFWSpace space = new CFWSpace()
			.type(CFWSpaceType.SPACE)
			.name("Space "+label)
			.abbreviation(label);
		
		Integer newSpaceID = CFWHierarchy.create(parentID, space);
		
		if(newSpaceID == null) {
			return; // error
		}
		
		//-----------------------------
		// Create Credentials
		CFWCredentials credentials = new CFWCredentials()
				.fkidSpace(newSpaceID)
				.foreignKeyOwner(1)
				.name("CREDS "+label)
				.isShared(true)
				.tags("tag_"+label)
				;
		
		Integer credsID = CFW.DB.Credentials.createGetPrimaryKey(credentials);
		
		//#########################################################
		// Global Space 
		//#########################################################
		
		//-----------------------------
		// Make Space
		String labelGlobal = label+"G";
		CFWSpace globalSpace = new CFWSpace()
			.type(CFWSpaceType.SPACE)
			.name("Global " + labelGlobal)
			.abbreviation(labelGlobal)
			.isGlobal(true);
		
		Integer newGlobalSpaceID = CFWHierarchy.create(parentID, globalSpace);
		
		if(newGlobalSpaceID == null) {
			return; // error
		}
		
		//-----------------------------
		// Create Credentials
		CFWCredentials credentialsGlobals = new CFWCredentials()
				.fkidSpace(newGlobalSpaceID)
				.foreignKeyOwner(1)
				.name("CREDS Global "+labelGlobal)
				.isShared(true)
				.tags("tag_"+labelGlobal)
				;
		
		Integer credsGlobalID = CFW.DB.Credentials.createGetPrimaryKey(credentialsGlobals);
				
		//#########################################################
		// Subordinates
		//#########################################################
		if(currentDepth < maxDepth) {
			createSubordinatesHierarchy(rootID, newSpaceID, character, currentDepth+1, maxDepth);
		}
	
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void createTestdataLarge() {
		//-------------------------------------
		// Create Testdata
		if(CFWDBSpaces.getCount() == 0) {
		
			//----------------------------------
			// Create hierarchy root elements
			for(int i = 0; i < 1; i++) {
				String name = CFWRandom.colorName() + " "+ CFWRandom.fruitName() + " Space";
				String abbrevation = CFWRandom.stringAlphaNum(3).toUpperCase();
				String description = CFWRandom.issueResolvedMessage();
				String location = CFWRandom.mythicalLocation();
				String email = name.toLowerCase().replace(" ", ".") + "@"+location.replace(" ", "-").toLowerCase() + ".com";
				
				CFWSpace rootSpace = new CFWSpace()
					.type(CFWSpaceType.ROOT_SPACE)
					.name(name)
					.abbreviation(abbrevation)
					.email(email)
					.description(description)
					.isEnabled(CFWRandom.bool());
				
				Integer rootID = CFWHierarchy.create(null, rootSpace);
				
				if(rootID == null) {
					return; // error
				}
				
				//-----------------------------
				// Create Subordinates
				createSubordinatesLarge(rootID, rootID, 3, 3, 0, 2);
			}
					
		}
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void createSubordinatesLarge(int rootID, int parentID, int minSubordinates, int maxSubordinates, int currentDepth, int maxDepth) {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		int max = CFWRandom.integer(minSubordinates, maxSubordinates);
	
		for(int i = 0; i < max; i++) {
	
			String name = CFWRandom.jobTitle();
			String abbrevation = CFWRandom.stringAlphaNum(3).toUpperCase();
			String description = CFWRandom.issueResolvedMessage();
			String location = CFWRandom.mythicalLocation();
			String email = name.toLowerCase().replace(" ", ".") + "@"+location.replace(" ", "-").toLowerCase() + ".com";
			
			CFWSpace person = new CFWSpace()
				.type(CFWSpaceType.SPACE)
				.name(name)
				.abbreviation(abbrevation)
				.email(email)
				.description(description)
				.isEnabled(CFWRandom.bool());
			
			Integer newPostID = CFWHierarchy.create(parentID, person);
			
			if(newPostID == null) {
				return; // error
			}
			
			//-----------------------------
			// Create User
			/*String firstname = CFW.Random.firstnameOfGod();
			String lastname = CFW.Random.lastnameSweden();
			String username = firstname+"_"+CFW.Random.stringAlphaNum(4);
			String userLocation = CFW.Random.mythicalLocation().replace(" ", "-").toLowerCase();
			String userEmail = username.toLowerCase() + "@" + userLocation + ".com";
			
			User user = new User(username)
					.firstname(firstname)
					.lastname(lastname)
					.email(userEmail)
					.setNewPassword(username, username);
			
			Integer userID = CFW.DB.Users.createGetPrimaryKey(user);
			CFW.DB.SpaceUserMap.assignUserToSpace(userID, newPostID);
			*/
					
			//-----------------------------
			// Create Subordinates
			if(currentDepth < maxDepth) {
				int newMin = (minSubordinates-2 > 0) ? minSubordinates-2 : 0;
				createSubordinatesLarge(rootID, newPostID, newMin, maxSubordinates-1, currentDepth+1, maxDepth);
			}
	
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
		Integer spaceID = CFW.Context.Request.getSelectedSpaceID();
		
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

}

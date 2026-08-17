package com.xresch.cfw.features.spaces;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class FeatureSpace extends CFWAppFeature {
	
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.features.spaces.resources";
	
	public static final String PERMISSION_SPACES_VIEWER = "Space: Viewer";
	public static final String PERMISSION_SPACES_ADMIN = "Space: Admin All";
	public static final String PERMISSION_SPACES_CREATE = "Space: Create Spaces";
	
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
		CFW.Registry.Objects.addCFWObject(CFWSpaceAdminsMap.class);		
		CFW.Registry.Objects.addCFWObject(CFWSpaceUserMap.class);		
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
					.description("The user can create, view and manage all root spaces."),
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
		
		createTestdata();

	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void createTestdata() {
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
					.type(CFWSpaceType.ORG)
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
				createSubordinates(rootID, rootID, 3, 3, 0, 2);
			}
					
		}
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void createSubordinates(int rootID, int parentID, int minSubordinates, int maxSubordinates, int currentDepth, int maxDepth) {
		
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
				.type(CFWSpaceType.POST)
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
			String firstname = CFW.Random.firstnameOfGod();
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
			
					
			//-----------------------------
			// Create Subordinates
			if(currentDepth < maxDepth) {
				int newMin = (minSubordinates-2 > 0) ? minSubordinates-2 : 0;
				createSubordinates(rootID, newPostID, newMin, maxSubordinates-1, currentDepth+1, maxDepth);
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

}

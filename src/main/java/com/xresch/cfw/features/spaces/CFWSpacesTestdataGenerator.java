package com.xresch.cfw.features.spaces;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.filemanager.CFWStoredFile;
import com.xresch.cfw.features.query.store.CFWStoredQuery;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWSpacesTestdataGenerator {

	boolean doCreateCredentials = false;
	boolean doCreateDashboards = false;
	boolean doCreateStoredFiles = false;
	boolean doCreateStoredQueries = false;
	boolean doCreateGroups = false;
	
	int ownerID = 1; // 1 is admin
	
	/******************************************************************
	 * 
	 * @param defaultForEntities sets all entities (credentials, groups, etc...)
	 ******************************************************************/
	public CFWSpacesTestdataGenerator(boolean defaultForEntities) {
		
		doCreateCredentials = defaultForEntities;
		doCreateDashboards = defaultForEntities;
		doCreateStoredFiles = defaultForEntities;
		doCreateStoredQueries = defaultForEntities;
		doCreateGroups 		= defaultForEntities;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private Integer createCredentials(int spaceID, String name, boolean isShared, String... tags) {
		
		if(doCreateCredentials) {
			CFWCredentials object = new CFWCredentials()
					.fkidSpace(spaceID)
					.foreignKeyOwner(ownerID)
					.name(name)
					.tags(tags)
					.isShared(isShared)
					;
			
			return CFW.DB.Credentials.createGetPrimaryKey(object);
		}
		
		return null;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private Integer createDashboard(int spaceID, String name, boolean isShared, String... tags) {
		
		if(doCreateDashboards) {
			Dashboard object = new Dashboard()
					.fkidSpace(spaceID)
					.foreignKeyOwner(ownerID)
					.name(name)
					.tags(tags)
					.isShared(isShared)
					;
			
			return CFW.DB.Dashboards.createGetPrimaryKey(object);
		}
		
		return null;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private Integer createStoredFile(int spaceID, String name, boolean isShared, String... tags) {
		
		if(doCreateStoredFiles) {
			CFWStoredFile file = new CFWStoredFile()
					.fkidSpace(spaceID)
					.foreignKeyOwner(ownerID)
					.name(name+".txt")
					.tags(tags)
					.isShared(isShared)
					.lastModified(System.currentTimeMillis())
					.size(888L)
					;

			InputStream data = new StringBufferInputStream("test test test");
			
			CFW.DB.StoredFile.createAndStoreData(file, data);

		}
		
		return null;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private Integer createStoredQuery(int spaceID, String name, boolean isShared, String... tags) {
		
		if(doCreateStoredFiles) {
			CFWStoredQuery object = new CFWStoredQuery()
					.fkidSpace(spaceID)
					.foreignKeyOwner(ownerID)
					.name(name+".txt")
					.query("| source random")
					.tags(tags)
					.isShared(isShared);
			
			return CFW.DB.StoredQuery.createGetPrimaryKey(object);
			
		}
		
		return null;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private Integer createGroup(int spaceID, String name) {
		
		if(doCreateGroups) {
			Role entity = new Role()
					.fkidSpace(spaceID)
					.foreignKeyGroupOwner(ownerID)
					.name(name)
					.isGroup(true)
					;
			
			return CFW.DB.Roles.createGetPrimaryKey(entity);
		}
		
		return null;
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void generateHierarchy() {
		createTestdataHierarchy(0);
	}
		
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void createTestdataHierarchy(int index) {
		
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
		// Create Entities
		Integer rootIDCreds = createCredentials(rootID, "CREDS Root "+rootName, true, "tag_R"+rootName);
		Integer rootIDDash = createDashboard(rootID, "DASH Root "+rootName, true, "tag_R"+rootName);
		Integer rootIDFile = createStoredFile(rootID, "FILE Root "+rootName, true, "tag_R"+rootName);
		Integer rootIDQuery = createStoredQuery(rootID, "QUERY Root "+rootName, true, "tag_R"+rootName);
		Integer rootIDGroup = createGroup(rootID, "GROUP Root "+rootName);

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
		// Create Entities
		Integer credsID = createCredentials(newSpaceID, "CREDS "+label, true, "tag_"+label);
 		Integer dashID = createDashboard(newSpaceID, "DASH "+label, true, "tag_"+label);
 		Integer fileID = createStoredFile(newSpaceID, "FILE "+label, true, "tag_"+label);
 		Integer queryID = createStoredQuery(newSpaceID, "QUERY "+label, true, "tag_"+label);
		Integer groupID = createGroup(newSpaceID, "GROUP "+label);

		
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
		// Create Entities
		Integer globalCredsID = createCredentials( newGlobalSpaceID, "CREDS "+labelGlobal, true, "tag_"+labelGlobal);
		Integer globalDashID  = createDashboard( newGlobalSpaceID, "DASH "+labelGlobal, true, "tag_"+labelGlobal);
		Integer globalFileID  = createStoredFile( newGlobalSpaceID, "FILE "+labelGlobal, true, "tag_"+labelGlobal);
		Integer globalQueryID = createStoredQuery( newGlobalSpaceID, "QUERY "+labelGlobal, true, "tag_"+labelGlobal);
		Integer globalGroupID = createGroup(newGlobalSpaceID, "GROUP "+labelGlobal);
				
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
	public static void createTestdataLarge() {
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
	private static void createSubordinatesLarge(int rootID, int parentID, int minSubordinates, int maxSubordinates, int currentDepth, int maxDepth) {
		
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
	
}

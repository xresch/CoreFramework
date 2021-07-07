package com.xresch.cfw.tests.features.contextsettings;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.ContextSettings;
import com.xresch.cfw.features.contextsettings.FeatureContextSettings;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestContextSettings extends DBTestMaster {
	
	protected static Role roleContextSettings;
	protected static Role roleAllowedGroup;
	
	protected static User userWithContextSettingsRole;
	protected static User userAllowedByGroup;
	protected static User userAllowedAsUser;
	protected static User userJustARegularGuy;
	
	protected static Permission permissionContextSettings;
	
	protected static ContextSettings settingsAllHaveAccess;
	protected static ContextSettings settingsRestricted;
	
	protected static Dashboard boardAllSpecificAccess;
	protected static Dashboard boardSharedWithAll;
	protected static Dashboard boardMinimalAccess;
	protected static Dashboard boardOtherCreator;
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@BeforeAll
	public static void fillWithTestData() {
				
		//------------------------------
		// Fetch Permissions
		permissionContextSettings = CFW.DB.Permissions.selectByName(FeatureContextSettings.PERMISSION_CONTEXT_SETTINGS);
		
		//------------------------------
		// Setup Roles
		CFW.DB.Roles.create(new Role("TestContextSettingsRole", FeatureUserManagement.CATEGORY_USER));
		roleContextSettings = CFW.DB.Roles.selectFirstByName("TestContextSettingsRole");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionContextSettings.id(), roleContextSettings.id(), true);

		CFW.DB.Roles.create(new Role("TestAllowedGroup", FeatureUserManagement.CATEGORY_USER));
		roleAllowedGroup = CFW.DB.Roles.selectFirstByName("TestAllowedGroup");
		
		//------------------------------
		// Setup Users
		CFW.DB.Users.create(new User("TestUserWithContextSettingsRole").setNewPassword("TestUserWithContextSettingsRole", "TestUserWithContextSettingsRole"));
		userWithContextSettingsRole = CFW.DB.Users.selectByUsernameOrMail("TestUserWithContextSettingsRole");
		CFW.DB.UserRoleMap.addRoleToUser(userWithContextSettingsRole, roleContextSettings, true);

		CFW.DB.Users.create(new User("TestUserAllowedByGroup").setNewPassword("TestUserAllowedByGroup", "TestUserAllowedByGroup"));
		userAllowedByGroup = CFW.DB.Users.selectByUsernameOrMail("TestUserAllowedByGroup");
		CFW.DB.UserRoleMap.addRoleToUser(userAllowedByGroup, roleAllowedGroup, true);
		
		CFW.DB.Users.create(new User("TestUserAllowedAsUser").setNewPassword("TestUserAllowedAsUser", "TestUserAllowedAsUser"));
		userAllowedAsUser = CFW.DB.Users.selectByUsernameOrMail("TestUserAllowedAsUser");
		
		CFW.DB.Users.create(new User("TestUserJustARegularGuy").setNewPassword("TestUserJustARegularGuy", "TestUserJustARegularGuy"));
		userJustARegularGuy = CFW.DB.Users.selectByUsernameOrMail("TestUserJustARegularGuy");
		
		//----------------------------------------
		// Create Relations
		LinkedHashMap<String, String> restrictedToUser = new LinkedHashMap<>();
		restrictedToUser.put(userAllowedAsUser.id()+"", userAllowedAsUser.username());
				
		LinkedHashMap<String, String> restrictedToRoles = new LinkedHashMap<>();
		restrictedToRoles.put(roleAllowedGroup.id()+"", roleAllowedGroup.name());
				
		//----------------------------------------
		// Create ContextSettings
		CFW.Registry.ContextSettings.register(TestMockupContextSettings.SETTINGS_TYPE, TestMockupContextSettings.class);
		
		CFW.DB.ContextSettings.oneTimeCreate(
			new ContextSettings()
				.type(TestMockupContextSettings.SETTINGS_TYPE)
				.name("Settings All Have Access")
				.settings(null)
			);
	
		settingsAllHaveAccess = CFW.DB.ContextSettings.selectFirstByName("Settings All Have Access");
		
		//----------------------------------------
		// Create ContextSettings
		CFW.Registry.ContextSettings.register(TestMockupContextSettings.SETTINGS_TYPE, TestMockupContextSettings.class);
				
		CFW.DB.ContextSettings.oneTimeCreate(
			new ContextSettings()
				.type(TestMockupContextSettings.SETTINGS_TYPE)
				.name("SettingsRestricted")
				.restrictedToUsers(restrictedToUser)
				.restrictedToGroups(restrictedToRoles)
				.settings("{}")
			);
			
		settingsRestricted = CFW.DB.ContextSettings.selectFirstByName("SettingsRestricted");

	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testCreateContextSettings() {
				
		Assertions.assertEquals("SettingsRestricted", settingsRestricted.name());
		Assertions.assertEquals(TestMockupContextSettings.SETTINGS_TYPE, settingsRestricted.type());

		Assertions.assertTrue(settingsRestricted.restrictedToUsers().containsKey(userAllowedAsUser.id()+""));
		Assertions.assertTrue(settingsRestricted.restrictedToGroups().containsKey(roleAllowedGroup.id()+""));
		Assertions.assertEquals("{}",settingsRestricted.settings());
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessUserWithContextsettingsRole() {
		
		//----------------------------------------
		// Create Pseudo Session
		CFWSessionData data = new CFWSessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userWithContextSettingsRole);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureContextSettings.RESOURCE_PACKAGE);

		//----------------------------------------
		// Assert Access
		LinkedHashMap<Object, Object> contextSettingsForUser = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(TestMockupContextSettings.SETTINGS_TYPE);
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsAllHaveAccess.id()));
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsRestricted.id()));
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessUserAllowedAsUser() {
		
		//----------------------------------------
		// Create Pseudo Session
		CFWSessionData data = new CFWSessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userAllowedAsUser);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureContextSettings.RESOURCE_PACKAGE);

		//----------------------------------------
		// Assert Access
		LinkedHashMap<Object, Object> contextSettingsForUser = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(TestMockupContextSettings.SETTINGS_TYPE);
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsAllHaveAccess.id()));
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsRestricted.id()));
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessUserAllowedByGroup() {
		
		//----------------------------------------
		// Create Pseudo Session
		CFWSessionData data = new CFWSessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userAllowedByGroup);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureContextSettings.RESOURCE_PACKAGE);

		//----------------------------------------
		// Assert Access
		LinkedHashMap<Object, Object> contextSettingsForUser = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(TestMockupContextSettings.SETTINGS_TYPE);
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsAllHaveAccess.id()));
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsRestricted.id()));
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessUserJustARegularGuy() {
		
		//----------------------------------------
		// Create Pseudo Session
		CFWSessionData data = new CFWSessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userJustARegularGuy);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureContextSettings.RESOURCE_PACKAGE);

		//----------------------------------------
		// Assert Access
		LinkedHashMap<Object, Object> contextSettingsForUser = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(TestMockupContextSettings.SETTINGS_TYPE);
		Assertions.assertTrue(contextSettingsForUser.containsKey(settingsAllHaveAccess.id()));
		Assertions.assertFalse(contextSettingsForUser.containsKey(settingsRestricted.id()));
	}
		
}

package com.xresch.cfw.tests.features.dashboard;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.SessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestDashboard extends DBTestMaster {
	
	protected static Role roleViewer;
	protected static Role roleCreator;
	protected static Role roleAdmin;
	protected static Role roleGroupEditors;
	protected static Role roleGroupShared;
	
	protected static User userViewer;
	protected static User userCreator;
	protected static User userCreatorTwo;
	protected static User userAdmin;
	protected static User userDirectEditor;
	protected static User userEditorByRole;
	protected static User userViewerByRole;
	
	protected static Permission permissionViewer;
	protected static Permission permissionCreator;
	protected static Permission permissionAdmin;
		
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
		permissionViewer = CFW.DB.Permissions.selectByName(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER);
		permissionCreator = CFW.DB.Permissions.selectByName(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR);
		permissionAdmin = CFW.DB.Permissions.selectByName(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN);
		
		//------------------------------
		// Setup Roles
		CFW.DB.Roles.create(new Role("TestDashboardViewer", "user"));
		roleViewer = CFW.DB.Roles.selectFirstByName("TestDashboardViewer");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionViewer.id(), roleViewer.id(), true);
		
		CFW.DB.Roles.create(new Role("TestDashboardCreator", "user"));
		roleCreator = CFW.DB.Roles.selectFirstByName("TestDashboardCreator");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionCreator.id(), roleCreator.id(), true);
		
		CFW.DB.Roles.create(new Role("TestDashboardAdmin", "user"));
		roleAdmin = CFW.DB.Roles.selectFirstByName("TestDashboardAdmin");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAdmin.id(), roleAdmin.id(), true);
		
		CFW.DB.Roles.create(new Role("TestGroupEditors", "user"));
		roleGroupEditors = CFW.DB.Roles.selectFirstByName("TestGroupEditors");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionViewer.id(), roleGroupEditors.id(), true);
		
		CFW.DB.Roles.create(new Role("TestGroupShared", "user"));
		roleGroupShared = CFW.DB.Roles.selectFirstByName("TestGroupShared");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionViewer.id(), roleGroupShared.id(), true);
		
		
		//------------------------------
		// Setup Users
		CFW.DB.Users.create(new User("TestViewer").setNewPassword("TestViewer", "TestViewer"));
		userViewer = CFW.DB.Users.selectByUsernameOrMail("TestViewer");
		CFW.DB.UserRoleMap.addUserToRole(userViewer, roleViewer, true);

		CFW.DB.Users.create(new User("TestCreator").setNewPassword("TestCreator", "TestCreator"));
		userCreator = CFW.DB.Users.selectByUsernameOrMail("TestCreator");
		CFW.DB.UserRoleMap.addUserToRole(userCreator, roleCreator, true);
		
		CFW.DB.Users.create(new User("TestCreatorTwo").setNewPassword("TestCreatorTwo", "TestCreatorTwo"));
		userCreatorTwo = CFW.DB.Users.selectByUsernameOrMail("TestCreatorTwo");
		CFW.DB.UserRoleMap.addUserToRole(userCreatorTwo, roleCreator, true);
		
		CFW.DB.Users.create(new User("TestAdmin").setNewPassword("TestAdmin", "TestAdmin"));
		userAdmin = CFW.DB.Users.selectByUsernameOrMail("TestAdmin");
		CFW.DB.UserRoleMap.addUserToRole(userAdmin, roleAdmin, true);
		
		CFW.DB.Users.create(new User("TestDirectEditor").setNewPassword("TestDirectEditor", "TestDirectEditor"));
		userDirectEditor = CFW.DB.Users.selectByUsernameOrMail("TestDirectEditor");
		CFW.DB.UserRoleMap.addUserToRole(userDirectEditor, roleViewer, true);
		
		CFW.DB.Users.create(new User("TestEditorByGroup").setNewPassword("TestEditorByGroup", "TestEditorByGroup"));
		userEditorByRole = CFW.DB.Users.selectByUsernameOrMail("TestEditorByGroup");
		CFW.DB.UserRoleMap.addUserToRole(userEditorByRole, roleGroupEditors, true);

		CFW.DB.Users.create(new User("TestViewerByRole").setNewPassword("TestViewerByRole", "TestViewerByRole"));
		userViewerByRole = CFW.DB.Users.selectByUsernameOrMail("TestViewerByRole");
		CFW.DB.UserRoleMap.addUserToRole(userViewerByRole, roleGroupShared, true);

		//----------------------------------------
		// Create Relations
		LinkedHashMap<String, String> sharedWithUser = new LinkedHashMap<>();
		sharedWithUser.put(userViewer.id()+"", userViewer.username());
		
		LinkedHashMap<String, String> editors = new LinkedHashMap<>();
		editors.put(userDirectEditor.id()+"", userDirectEditor.username());
		
		LinkedHashMap<String, String> sharedWithRoles = new LinkedHashMap<>();
		sharedWithRoles.put(roleGroupShared.id()+"", roleGroupShared.name());
		
		LinkedHashMap<String, String> editorRoles = new LinkedHashMap<>();
		editorRoles.put(roleGroupEditors.id()+"", roleGroupEditors.name());
		
		
		//----------------------------------------
		// Create Dashboard
		String boardName = "testDashboardAllHaveAccess";
		CFW.DB.Dashboards.create(
			new Dashboard()
				.name(boardName)
				.foreignKeyOwner(userCreator.getPrimaryKey())
				.description("Test Dashboard")
				.isShared(true)
				.sharedWithUsers(sharedWithUser)
				.sharedWithRoles(sharedWithRoles)
				.editors(editors)
				.editorRoles(editorRoles)
				.isDeletable(true)
				.isRenamable(true)
		);
		
		boardAllSpecificAccess = CFW.DB.Dashboards.selectFirstByName(boardName);
		
		//----------------------------------------
		// Create Dashboard
		boardName = "testDashboardSharedWithAll";
		CFW.DB.Dashboards.create(
			new Dashboard()
				.name(boardName)
				.foreignKeyOwner(userCreator.getPrimaryKey())
				.description("Test Shared with All")
				.isShared(true)
		);
		
		boardSharedWithAll = CFW.DB.Dashboards.selectFirstByName(boardName);
		
		//----------------------------------------
		// Create Dashboard
		boardName = "testDashboardMinimalAccess";
		CFW.DB.Dashboards.create(
			new Dashboard()
				.name(boardName)
				.foreignKeyOwner(userCreator.getPrimaryKey())
				.description("Test Minimal Access")
				.isShared(false)
		);
		
		boardMinimalAccess = CFW.DB.Dashboards.selectFirstByName(boardName);
		
		//----------------------------------------
		// Create  Dashboard
		boardName = "testDashboardOtherCreator";
		CFW.DB.Dashboards.create(
			new Dashboard()
				.name(boardName)
				.foreignKeyOwner(userCreatorTwo.getPrimaryKey())
				.description("Test Other Creator not Shared")
				.isShared(false)
		);
		
		boardOtherCreator = CFW.DB.Dashboards.selectFirstByName(boardName);
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testCreateDashboard() {
				
		Assertions.assertEquals("testDashboardAllHaveAccess", boardAllSpecificAccess.name());
		Assertions.assertEquals("Test Dashboard", boardAllSpecificAccess.description());
		Assertions.assertEquals(userCreator.getPrimaryKey(), boardAllSpecificAccess.foreignKeyOwner());
		Assertions.assertTrue(boardAllSpecificAccess.isShared());
		Assertions.assertTrue(boardAllSpecificAccess.sharedWithUsers().containsKey(userViewer.id()+""));
		Assertions.assertTrue(boardAllSpecificAccess.sharedWithRoles().containsKey(roleGroupShared.id()+""));
		Assertions.assertTrue(boardAllSpecificAccess.editors().containsKey(userDirectEditor.id()+""));
		Assertions.assertTrue(boardAllSpecificAccess.editorRoles().containsKey(roleGroupEditors.id()+""));
		Assertions.assertTrue(boardAllSpecificAccess.isDeletable());
		Assertions.assertTrue(boardAllSpecificAccess.isRenamable());
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessViewer() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userViewer);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);

		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonUserDashboards);
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(2, sharedBoards.getAsJsonArray().size(), "Contains two shared dashboards.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonAdminDashboards);
		
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Viewer can view but cannot edit." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Viewer can view but cannot edit." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Viewer cannot view and cannot edit." );		
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Viewer cannot view and cannot edit." );		

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessCreator() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userCreator);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);
				
		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		JsonElement userBoards = CFW.JSON.fromJson(jsonUserDashboards);
		Assertions.assertTrue(userBoards.isJsonArray());
		Assertions.assertEquals(3, userBoards.getAsJsonArray().size(), "Contains his three dashboards.");
		Assertions.assertTrue(jsonUserDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonUserDashboards.contains(boardSharedWithAll.name()) );
		Assertions.assertTrue(jsonUserDashboards.contains(boardMinimalAccess.name()) );
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(2, sharedBoards.getAsJsonArray().size(), "Contains two shared dashboard.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonAdminDashboards);
		
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Creator can edit his own dashboard." );
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Creator can edit his own dashboard." );
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Creator can edit his own dashboard." );		
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Creator cannot edit private dashboard of other creator." );		

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessAdmin() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userAdmin);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);
		
		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		JsonElement userBoards = CFW.JSON.fromJson(jsonUserDashboards);
		Assertions.assertTrue(userBoards.isJsonArray());
		Assertions.assertEquals(0, userBoards.getAsJsonArray().size(), "Has not created any dashboards.");
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(1, sharedBoards.getAsJsonArray().size(), "Contains one shared dashboard.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		JsonElement adminBoards = CFW.JSON.fromJson(jsonAdminDashboards);
		Assertions.assertTrue(adminBoards.isJsonArray());
		Assertions.assertEquals(4, adminBoards.getAsJsonArray().size(), "Has access to every dashboard.");
		Assertions.assertTrue(jsonAdminDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonAdminDashboards.contains(boardSharedWithAll.name()) );
		Assertions.assertTrue(jsonAdminDashboards.contains(boardMinimalAccess.name()) );
		Assertions.assertTrue(jsonAdminDashboards.contains(boardOtherCreator.name()) );
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Admin can edit every dashboard." );
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Admin can edit every dashboard." );
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Admin can edit every dashboard." );		
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Admin can edit every dashboard." );		

	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessDirectEditor() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userDirectEditor);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);

		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonUserDashboards);
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(2, sharedBoards.getAsJsonArray().size(), "Contains two shared dashboards.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonAdminDashboards);
		
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Direct Editor can edit the dashboard were he was given permissions." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Direct Editor cannot edit a shared dashboard without editor permissions." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Direct Editor cannot edit a private dashboard without editor permissions." );		
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Direct Editor cannot edit a private dashboard without editor permissions." );		

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessEditorByRole() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userEditorByRole);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);

		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonUserDashboards);
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(2, sharedBoards.getAsJsonArray().size(), "Contains two shared dashboards.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonAdminDashboards);
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertTrue(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Editor by Role can edit the dashboard were he was given permissions." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Editor by Role cannot edit a shared dashboard without editor permissions." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Editor by Role cannot edit a private dashboard without editor permissions." );		
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Editor by Role cannot edit a private dashboard without editor permissions." );		

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Test
	public void testAccessViewerByRole() {
		
		//----------------------------------------
		// Assert Own Dashboards
		SessionData data = new SessionData("ricks-sessionids-roll");
		CFW.Context.Request.setSessionData(data);
		data.setUser(userViewerByRole);
		data.triggerLogin();
		
		CFW.Files.addAllowedPackage(FeatureDashboard.PACKAGE_RESOURCES);

		//----------------------------------------
		// Assert Own Dashboards
		String jsonUserDashboards = CFW.DB.Dashboards.getUserDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonUserDashboards);
		
		//----------------------------------------
		// Assert Shared Dashboards Access
		String jsonSharedDashboards = CFW.DB.Dashboards.getSharedDashboardListAsJSON();
		JsonElement sharedBoards = CFW.JSON.fromJson(jsonSharedDashboards);
		Assertions.assertTrue(sharedBoards.isJsonArray());
		Assertions.assertEquals(2, sharedBoards.getAsJsonArray().size(), "Contains two shared dashboards.");
		Assertions.assertTrue(jsonSharedDashboards.contains(boardAllSpecificAccess.name()) );
		Assertions.assertTrue(jsonSharedDashboards.contains(boardSharedWithAll.name()) );
		
		//----------------------------------------
		// Assert Admin Dashboard Access
		String jsonAdminDashboards = CFW.DB.Dashboards.getAdminDashboardListAsJSON();
		Assertions.assertEquals("[]", jsonAdminDashboards);
		
		//----------------------------------------
		// Assert Can Edit
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardAllSpecificAccess.id()+""), "Viewer by Role can view but not edit." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardSharedWithAll.id()+""), "Viewer by Role can view but not edit." );
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardMinimalAccess.id()+""), "Viewer by Role can view but not edit." );		
		Assertions.assertFalse(CFW.DB.Dashboards.checkCanEdit(boardOtherCreator.id()+""), "Viewer by Role can view but not edit." );		

	}
		
}

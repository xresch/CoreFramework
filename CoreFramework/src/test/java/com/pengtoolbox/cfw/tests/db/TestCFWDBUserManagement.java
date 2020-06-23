package com.pengtoolbox.cfw.tests.db;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.tests._master.DBTestMaster;
import com.pengtoolbox.cfw.utils.CFWSecurity;

public class TestCFWDBUserManagement extends DBTestMaster {

	protected static Role testroleA;
	protected static Role testroleB;
	protected static Role testroleC;
	
	protected static User testuserA;
	protected static User testuserB;
	protected static User testuserC;
	
	protected static Permission permissionA;
	protected static Permission permissionAA;
	protected static Permission permissionAAA;
	
	protected static Permission permissionB;
	protected static Permission permissionBB;
	
	protected static Permission permissionC;
	
	@BeforeClass
	public static void fillWithTestData() {
		
		//------------------------------
		// Roles
		CFW.DB.Roles.create(new Role("TestroleA", "user"));
		testroleA = CFW.DB.Roles.selectFirstByName("TestroleA");
		
		CFW.DB.Roles.create(new Role("TestroleB", "user"));
		testroleB = CFW.DB.Roles.selectFirstByName("TestroleB");
		
		CFW.DB.Roles.create(new Role("TestroleC", "user"));
		testroleC = CFW.DB.Roles.selectFirstByName("TestroleC");
		
		//------------------------------
		// Users
		CFW.DB.Users.create(new User("TestuserA").setNewPassword("TestuserA", "TestuserA"));
		testuserA = CFW.DB.Users.selectByUsernameOrMail("TestuserA");
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleA, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleB, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleC, true);
		
		CFW.DB.Users.create(new User("TestuserB").setNewPassword("TestuserB", "TestuserB"));
		testuserB = CFW.DB.Users.selectByUsernameOrMail("TestuserB");
		CFW.DB.UserRoleMap.addUserToRole(testuserB, testroleA, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserB, testroleB, true);
		
		CFW.DB.Users.create(new User("TestuserC").setNewPassword("TestuserC", "TestuserC"));	
		testuserC = CFW.DB.Users.selectByUsernameOrMail("TestuserC");
		CFW.DB.UserRoleMap.addUserToRole(testuserC, testroleC, true);
		
		//------------------------------
		// Permissions
		CFW.DB.Permissions.create(new Permission("PermissionA", "user"));
		permissionA = CFW.DB.Permissions.selectByName("PermissionA");
		System.out.println("=============== PermissionA ===================\n"+permissionA.dumpFieldsAsKeyValueString());
		System.out.println("=============== RoleA ===================\n"+testroleA.dumpFieldsAsKeyValueString());
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionA.id(), testroleA.id(), true);
		
		CFW.DB.Permissions.create(new Permission("PermissionAA", "user"));
		permissionAA = CFW.DB.Permissions.selectByName("PermissionAA");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAA.id(), testroleA.id(), true);
		
		CFW.DB.Permissions.create(new Permission("PermissionAAA", "user"));
		permissionAAA = CFW.DB.Permissions.selectByName("PermissionAAA");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAAA.id(), testroleA.id(), true);
		
		CFW.DB.Permissions.create(new Permission("PermissionB", "user"));
		permissionB = CFW.DB.Permissions.selectByName("PermissionB");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionB.id(), testroleB.id(), true);
		
		CFW.DB.Permissions.create(new Permission("PermissionBB", "user"));
		permissionBB = CFW.DB.Permissions.selectByName("PermissionBB");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionBB.id(), testroleB.id(), true);
		
		CFW.DB.Permissions.create(new Permission("PermissionC", "user"));
		permissionC = CFW.DB.Permissions.selectByName("PermissionC");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionC.id(), testroleC.id(), true);
	}
	
	
	@Test
	public void testPasswordHandling() {
		
		//--------------------------------------
		// Test Password encryption methods
		String salt = CFW.Security.createPasswordSalt(31);
		String hashtext = CFWSecurity.createPasswordHash("admin", salt);
		
        Assertions.assertTrue(salt.length() == 31);
        Assertions.assertTrue(hashtext.length() <= 127);
        
		//System.out.println("Salt: "+salt);
        //System.out.println("Hashtext: "+hashtext);
        
		//--------------------------------------
		// Check Initial Password
        User testUser = new User("PasswordTestUser").setNewPassword("correctPassword", "correctPassword");
        
        CFW.DB.Users.create(testUser);
        testUser = CFW.DB.Users.selectByUsernameOrMail("PasswordTestUser");

		Assertions.assertTrue(testUser.passwordValidation("correctPassword"), "The password is correct.");
		Assertions.assertFalse(testUser.passwordValidation("wrongPassword"), "The password is wrong.");
		Assertions.assertFalse(testUser.passwordValidation(" correctPassword "), "The password is wrong.");
		
		testUser.setNewPassword("wrongPW", "test");
		Assertions.assertTrue(testUser.passwordValidation("correctPassword"), "The password was not changed.");
		
		//--------------------------------------
		// Check Initial Password with new 
		testUser.changePassword("wrongPW", "newPassword", "newPassword");
		Assertions.assertTrue(testUser.passwordValidation("correctPassword"), "The password was not changed because old password is wrong.");
		
		testUser.changePassword("correctPassword", "newPassword", "newPasswordxxx");
		Assertions.assertTrue(testUser.passwordValidation("correctPassword"), "The password was not changed because new password is not the same.");
		
		testUser.changePassword("correctPassword", "newPassword", "newPassword");
		Assertions.assertTrue(testUser.passwordValidation("newPassword"), "The password was successfully changed.");
		
	}
	
	@Test
	public void testCRUDUser() {
		String username = "T.Testonia";
		String usernameUpdated = "T.Testonia2";
		
		//--------------------------------------
		// Cleanup
		User userToDelete = CFW.DB.Users.selectByUsernameOrMail(username);
		if(userToDelete != null) {
			CFW.DB.Users.deleteByID(userToDelete.id());
		}
		
		userToDelete = CFW.DB.Users.selectByUsernameOrMail(usernameUpdated);
		if(userToDelete != null) {
			CFW.DB.Users.deleteByID(userToDelete.id());
		}
		
		Assertions.assertFalse(CFW.DB.Users.checkUsernameExists(username), "User doesn't exist, checkUsernameExists(string) works.");
		Assertions.assertFalse(CFW.DB.Users.checkUsernameExists(userToDelete), "User doesn't exist, checkUsernameExists(user) works.");
		
		//--------------------------------------
		// CREATE
		CFW.DB.Users.create(new User(username)
				.email("t.testonia@cfw.com")
				.firstname("Testika")
				.lastname("Testonia")
				.passwordHash("hash")
				.passwordSalt("salt")
				.status("BLOCKED")
				.isDeletable(false)
				.isRenamable(false)
				.isForeign(true)
				);
		
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(username), "User created successfully, checkUsernameExists(string) works.");
		
		//--------------------------------------
		// USERNAME IS CASE INSENSITIVE
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(username.toUpperCase()), "User is found uppercase letters.");
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(username.toLowerCase()), "User is found lowercase letters.");
		
		//--------------------------------------
		// SELECT BY USERNAME
		User user = CFW.DB.Users.selectByUsernameOrMail(username);
		
		//System.out.println("===== USER =====");
		//System.out.println(user.getKeyValueString());

		Assertions.assertTrue(user != null);
		Assertions.assertTrue(user.username().equals(username));
		Assertions.assertTrue(user.email().equals("t.testonia@cfw.com"));
		Assertions.assertTrue(user.firstname().equals("Testika"));
		Assertions.assertTrue(user.lastname().equals("Testonia"));
		Assertions.assertTrue(user.passwordHash().equals("hash"));
		Assertions.assertTrue(user.passwordSalt().equals("salt"));
		Assertions.assertTrue(user.status().equals("BLOCKED"));
		Assertions.assertTrue(user.isDeletable() == false);
		Assertions.assertTrue(user.isRenamable() == false);
		Assertions.assertTrue(user.isForeign() == true);
		
		//--------------------------------------
		// SELECT BY USERNAME CASE INSENSITIVE
		Assertions.assertNotNull(CFW.DB.Users.selectByUsernameOrMail(username.toLowerCase()),"User is selected with lowercase letters.");
		Assertions.assertNotNull(CFW.DB.Users.selectByUsernameOrMail(user.email().toUpperCase()),"User is selected with uppercase letters.");
		
		//--------------------------------------
		// CHECK NOT DELETABLE
		Assertions.assertFalse(CFW.DB.Users.deleteByID(user.id()), "The user is not deleted, returns false.");
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(username), "The user still exists.");
		
		//--------------------------------------
		// CHECK NOT RENAMABME
		user.username("notRenamable");
		Assertions.assertFalse(user.username().equals("notRenamable"), "The user is not renamed, returns false.");
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(username), "The user still exists.");
		
		//--------------------------------------
		// UPDATE
		user.isRenamable(true);
		user.isDeletable(true);
		user.username(usernameUpdated)
			.email("t.testonia2@cfw.com")
			.firstname("Testika2")
			.lastname("Testonia2")
			.passwordHash("hash2")
			.passwordSalt("salt2")
			.status("Inactive")
			.isForeign(false)
			;
		
		Assertions.assertTrue(CFW.DB.Users.update(user),"The update with rename is successful.");
		
		//--------------------------------------
		// SELECT UPDATED USER
		User updatedUser = CFW.DB.Users.selectByUsernameOrMail(usernameUpdated);
		
		System.out.println("===== USER =====");
		System.out.println(user.dumpFieldsAsKeyValueString());
		
		System.out.println("===== UPDATED USER =====");
		System.out.println(updatedUser.dumpFieldsAsKeyValueString());
		
		Assertions.assertTrue(CFW.DB.Users.checkUsernameExists(updatedUser), "User exists, checkUsernameExists(user) works.");
		Assertions.assertTrue(updatedUser != null);
		Assertions.assertTrue(updatedUser.username().equals(usernameUpdated));
		Assertions.assertTrue(updatedUser.email().equals("t.testonia2@cfw.com"));
		Assertions.assertTrue(updatedUser.firstname().equals("Testika2"));
		Assertions.assertTrue(updatedUser.lastname().equals("Testonia2"));
		Assertions.assertTrue(updatedUser.passwordHash().equals("hash2"));
		Assertions.assertTrue(updatedUser.passwordSalt().equals("salt2"));
		Assertions.assertTrue(updatedUser.status().equals("Inactive"));
		Assertions.assertTrue(updatedUser.isDeletable() == true);
		Assertions.assertTrue(updatedUser.isRenamable() == true);
		Assertions.assertTrue(updatedUser.isForeign() == false);

		
		//--------------------------------------
		// CHECH EMAIL METHODS
		Assertions.assertTrue(CFW.DB.Users.checkEmailExists(updatedUser), "Email exists, checkEmailExists(User) works.");
		Assertions.assertTrue(CFW.DB.Users.checkEmailExists("t.testonia2@cfw.com"), "Email exists, checkEmailExists(String) works.");
		Assertions.assertTrue(CFW.DB.Users.checkEmailExists("t.testonia2@cfw.com".toUpperCase()), "Email case insensitive works. ");
		
		User userbyMail = CFW.DB.Users.selectByUsernameOrMail("t.testonia2@cfw.com");
		
		Assertions.assertTrue( (userbyMail != null), "Select User by Mail works.");
		
		//--------------------------------------
		// SELECT BY ID

		User userbyID = CFW.DB.Users.selectByID(userbyMail.id());
		
		Assertions.assertTrue( (userbyID != null), "Select User by ID works.");
				
		//--------------------------------------
		// DELETE
		CFW.DB.Users.deleteByID(userbyMail.id());
		
		Assertions.assertFalse(CFW.DB.Users.checkUsernameExists(username));

	}
	
	@Test
	public void testCRUDRole() {
		
		String rolename = "Test Role";
		String rolenameUpdated = "Test RoleUPDATED";
		
		//--------------------------------------
		// Cleanup
		Role roleToDelete = CFW.DB.Roles.selectFirstByName(rolename);
		if(roleToDelete != null) {
			CFW.DB.Roles.deleteByID(roleToDelete.id());
		}

		roleToDelete = CFW.DB.Roles.selectFirstByName(rolenameUpdated);
		if(roleToDelete != null) {
			CFW.DB.Roles.deleteByID(roleToDelete.id());
		}
		Assertions.assertFalse(CFW.DB.Roles.checkExistsByName(rolename), "Role doesn't exists, checkRoleExists(String) works.");
		Assertions.assertFalse(CFW.DB.Roles.checkExistsByName(roleToDelete), "Role doesn't exist, checkRoleExists(Role) works.");
		
		
		//--------------------------------------
		// CREATE
		CFW.DB.Roles.create(new Role(rolename, "user")
				.description("Testdescription")
				.isDeletable(false)
				);
		
		Assertions.assertTrue(CFW.DB.Roles.checkExistsByName(rolename), "Role created successfully, checkRoleExists(String) works.");

		//--------------------------------------
		// SELECT BY NAME
		Role role = CFW.DB.Roles.selectFirstByName(rolename);
		
		//System.out.println("===== USER =====");
		//System.out.println(role.getKeyValueString());

		Assertions.assertTrue(CFW.DB.Roles.checkExistsByName(role), "Role created successfully, checkRoleExists(Role) works.");
		Assertions.assertTrue(role != null);
		Assertions.assertTrue(role.name().equals(rolename));
		Assertions.assertTrue(role.description().equals("Testdescription"));
		Assertions.assertTrue(role.isDeletable() == false);
		
		//--------------------------------------
		// CHECK NOT DELETABLE
		Assertions.assertFalse(CFW.DB.Roles.deleteByID(role.id()), "The role is not deleted, returns false.");
		Assertions.assertTrue(CFW.DB.Roles.checkExistsByName(role), "The role still exists.");
		
		//--------------------------------------
		// UPDATE
		role.name(rolenameUpdated)
			.description("Testdescription2")
			.isDeletable(true);
		
		CFW.DB.Roles.update(role);
		
		//--------------------------------------
		// SELECT UPDATED GROUP
		Role updatedRole = CFW.DB.Roles.selectFirstByName(rolenameUpdated);
		
		//System.out.println("===== UPDATED GROUP =====");
		//System.out.println(updatedRole.getKeyValueString());
		
		Assertions.assertTrue(role != null);
		Assertions.assertTrue(role.name().equals(rolenameUpdated));
		Assertions.assertTrue(role.description().equals("Testdescription2"));
		Assertions.assertTrue(role.isDeletable() == true);
		
		//--------------------------------------
		// SELECT BY ID
		Role roleByID = CFW.DB.Roles.selectByID(updatedRole.id());
		
		Assertions.assertTrue(roleByID != null, "Role is selected by ID.");
		//--------------------------------------
		// DELETE
		CFW.DB.Roles.deleteByID(updatedRole.id());
		
		Assertions.assertFalse(CFW.DB.Roles.checkExistsByName(rolename));
		
	}
	
	@Test
	public void testCRUDUserRoleMap() {
		
		//--------------------------------------
		// Preparation
		User newUser = new User("newUser");
		CFW.DB.Users.create(newUser);
		CFW.DB.UserRoleMap.removeUserFromRole(newUser, testroleA);
		
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(newUser, testroleA), "User is not in the role to the role.");
		
		//--------------------------------------
		// Test checkIsUserInRole()
		System.out.println("================= checkIsUserInRole() =================");
		Assertions.assertTrue(CFW.DB.UserRoleMap.checkIsUserInRole(testuserA, testroleA), "checkIsUserInRole() finds the testuser.");
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(99, testroleA.id()), "checkIsUserInRole() cannot find not existing user.");
	
		//--------------------------------------
		// Test  addUserToRole()
		System.out.println("================= Test addUserToRole() =================");
		User newUserFromDB = CFW.DB.Users.selectByUsernameOrMail("newUser");
		CFW.DB.UserRoleMap.addUserToRole(newUserFromDB, testroleA, true);
		
		Assertions.assertTrue(CFW.DB.UserRoleMap.checkIsUserInRole(newUserFromDB, testroleA), "User was added to the role.");
		
		//--------------------------------------
		// Test removeUserFromRole()
		System.out.println("================= Test removeUserFromRole() =================");
		CFW.DB.UserRoleMap.removeUserFromRole(newUserFromDB, testroleA);
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(newUserFromDB, testroleA), "User was removed from the role.");
		
		//--------------------------------------
		// Test remove UserRoleMapping when user is deleted
		System.out.println("================= Test remove UserRoleMapping when user is deleted =================");
		CFW.DB.UserRoleMap.addUserToRole(newUserFromDB, testroleA, true);
		Assertions.assertTrue(CFW.DB.UserRoleMap.checkIsUserInRole(newUserFromDB, testroleA), "User was added to the role.");
		
		CFW.DB.Users.deleteByID(newUserFromDB.id());
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(newUserFromDB, testroleA), "User was removed from the role when it was deleted.");
		
		//--------------------------------------
		// Test selectRolesForUser()
		System.out.println("================= Test selectRolesForUser() =================");
		HashMap<String, Role> roles = CFW.DB.Users.selectRolesForUser(testuserB);
		
		Assertions.assertEquals(2, roles.size(), "Testuser2 is part of 2 roles.");
		Assertions.assertTrue(roles.containsKey(testroleA.name()), "User is part of testroleA.");
		Assertions.assertTrue(roles.containsKey(testroleB.name()), "User is part of testroleB.");
		Assertions.assertFalse(roles.containsKey(testroleC.name()), "User is NOT part of testroleC.");
		
		//--------------------------------------
		// Test remove UserRoleMapping when role is deleted
		System.out.println("================= Test remove UserRoleMapping when role is deleted =================");
		//Cleanup
		CFW.DB.Roles.deleteByName("TestRoleToDelete");
		
		Role roleToDelete = new Role("TestRoleToDelete", "user");
		
		CFW.DB.Roles.create(roleToDelete);
		roleToDelete = CFW.DB.Roles.selectFirstByName("TestRoleToDelete");
		
		CFW.DB.UserRoleMap.addUserToRole(testuserB, roleToDelete, true);
		Assertions.assertTrue(CFW.DB.UserRoleMap.checkIsUserInRole(testuserB, roleToDelete), "User was added to the role.");
		
		CFW.DB.UserRoleMap.addUserToRole(testuserC, roleToDelete, true);
		Assertions.assertTrue(CFW.DB.UserRoleMap.checkIsUserInRole(testuserC, roleToDelete), "User was added to the role.");
		
		CFW.DB.Roles.deleteByID(roleToDelete.id());
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(testuserB, roleToDelete), "UserRoleMapping was removed when role was deleted.");
		Assertions.assertFalse(CFW.DB.UserRoleMap.checkIsUserInRole(testuserC, roleToDelete), "UserRoleMapping was removed when role was deleted.");
		
	}
	
	@Test
	public void testCRUDPermission() {
		
		String permissionname = "Test Permission";
		String permissionnameUpdated = "Test PermissionUPDATED";
		
		//--------------------------------------
		// Cleanup
		Permission permissionToDelete = CFW.DB.Permissions.selectByName(permissionname);
		if(permissionToDelete != null) {
			CFW.DB.Permissions.deleteByID(permissionToDelete.id());
		}
		
		permissionToDelete = CFW.DB.Permissions.selectByName(permissionnameUpdated);
		if(permissionToDelete != null) {
			CFW.DB.Permissions.deleteByID(permissionToDelete.id());
		}
		
		Assertions.assertFalse(CFW.DB.Permissions.checkExistsByName(permissionname), "Permission doesn't exists, checkPermissionExists(String) works.");
		Assertions.assertFalse(CFW.DB.Permissions.checkExistsByName(permissionToDelete), "Permission doesn't exist, checkPermissionExists(Permission) works.");
		
		
		//--------------------------------------
		// CREATE
		CFW.DB.Permissions.create(new Permission(permissionname, "user")
				.description("Testdescription")
				);
		
		Assertions.assertTrue(CFW.DB.Permissions.checkExistsByName(permissionname), "Permission created successfully, checkPermissionExists(String) works.");

		//--------------------------------------
		// SELECT BY NAME
		Permission permission = CFW.DB.Permissions.selectByName(permissionname);
		
		//System.out.println("===== USER =====");
		//System.out.println(permission.getKeyValueString());

		Assertions.assertTrue(CFW.DB.Permissions.checkExistsByName(permission), "Permission created successfully, checkPermissionExists(Permission) works.");
		Assertions.assertTrue(permission != null);
		Assertions.assertTrue(permission.name().equals(permissionname));
		Assertions.assertTrue(permission.description().equals("Testdescription"));
		
		//--------------------------------------
		// CHECK NOT DELETABLE
		Assertions.assertFalse(CFW.DB.Permissions.deleteByID(permission.id()), "The permission is not deleted, returns false.");
		Assertions.assertTrue(CFW.DB.Permissions.checkExistsByName(permission), "The permission still exists.");
		
		//--------------------------------------
		// UPDATE
		permission.name(permissionnameUpdated)
			.description("Testdescription2");
		
		CFW.DB.Permissions.update(permission);
		
		//--------------------------------------
		// SELECT UPDATED PERMISSION
		Permission updatedPermission = CFW.DB.Permissions.selectByName(permissionnameUpdated);
		
		//System.out.println("===== UPDATED PERMISSION =====");
		//System.out.println(updatedPermission.getKeyValueString());
		
		Assertions.assertTrue(permission != null);
		Assertions.assertTrue(permission.name().equals(permissionnameUpdated));
		Assertions.assertTrue(permission.description().equals("Testdescription2"));
		
		//--------------------------------------
		// SELECT BY ID
		Permission permissionByID = CFW.DB.Permissions.selectByID(updatedPermission.id());
		
		Assertions.assertTrue(permissionByID != null, "Permission is selected by ID.");
		//--------------------------------------
		// DELETE
		CFW.DB.Permissions.deleteByID(updatedPermission.id());
		
		Assertions.assertFalse(CFW.DB.Permissions.checkExistsByName(permissionname));
		
	}
	
	@Test
	public void testCRUDRolePermissionMap() {
		
		//--------------------------------------
		// Preparation
		Permission newPermission = new Permission("newPermission", "user");
		CFW.DB.Permissions.create(newPermission);
		CFW.DB.RolePermissionMap.removePermissionFromRole(newPermission.id(), testroleA.id());
		
		Assertions.assertFalse(CFW.DB.RolePermissionMap.checkIsPermissionInRole(newPermission, testroleA), "Permission is not in the role to the role.");
		
		//--------------------------------------
		// Test checkIsPermissionInRole()
		System.out.println("================= checkIsPermissionInRole() =================");
		Assertions.assertTrue(CFW.DB.RolePermissionMap.checkIsPermissionInRole(permissionA, testroleA), "checkIsPermissionInRole() finds the permissionA.");
		Assertions.assertFalse(CFW.DB.RolePermissionMap.checkIsPermissionInRole(999, testroleA.id()), "checkIsPermissionInRole() cannot find not existing permission.");
	
		//--------------------------------------
		// Test  addPermissionToRole()
		System.out.println("================= Test addPermissionToRole() =================");
		Permission newPermissionFromDB = CFW.DB.Permissions.selectByName("newPermission");
		CFW.DB.RolePermissionMap.addPermissionToRole(newPermissionFromDB, testroleA, true);
		
		Assertions.assertTrue(CFW.DB.RolePermissionMap.checkIsPermissionInRole(newPermissionFromDB, testroleA), "Permission was added to the role.");
		
		//--------------------------------------
		// Test removePermissionFromRole()
		System.out.println("================= Test removePermissionFromRole() =================");
		CFW.DB.RolePermissionMap.removePermissionFromRole(newPermissionFromDB, testroleA);
		Assertions.assertFalse(CFW.DB.RolePermissionMap.checkIsPermissionInRole(newPermissionFromDB, testroleA), "Permission was removed from the role.");
		
		//--------------------------------------
		// Test remove RolePermissionMapping when permission is deleted
		System.out.println("================= Test remove RolePermissionMapping when permission is deleted =================");
		CFW.DB.RolePermissionMap.addPermissionToRole(newPermissionFromDB, testroleA, true);
		Assertions.assertTrue(CFW.DB.RolePermissionMap.checkIsPermissionInRole(newPermissionFromDB, testroleA), "Permission was added to the role.");
		
		//--------------------------------------
		// Test selectPermissionsForRole()
		System.out.println("================= Test selectPermissionsForRole() =================");
		HashMap<String, Permission> rolePermissions = CFW.DB.Roles.selectPermissionsForRole(testroleA);
		
		Assertions.assertEquals(4, rolePermissions.size(), "TestroleA has 4 permissions.");
		Assertions.assertTrue(rolePermissions.containsKey(permissionA.name()), "PermissionA is part of testroleA.");
		Assertions.assertTrue(rolePermissions.containsKey(permissionAA.name()), "PermissionAA is part of testroleA.");
		Assertions.assertTrue(rolePermissions.containsKey(permissionAAA.name()), "PermissionAAA is part of testroleA.");
		Assertions.assertFalse(rolePermissions.containsKey(permissionB.name()), "PermissionB is NOT part of testroleA.");
		
		//--------------------------------------
		// Test selectPermissionsForUser()
		System.out.println("================= Test selectPermissionsForUser() =================");
		HashMap<String, Permission> userPermissions = CFW.DB.Users.selectPermissionsForUser(testuserB);
		
		Assertions.assertEquals(6, userPermissions.size(), "TestuserB has 6 permissions.");
		Assertions.assertTrue(userPermissions.containsKey(permissionA.name()), "TestuserB has PermissionA.");
		Assertions.assertTrue(userPermissions.containsKey(permissionAA.name()), "TestuserB has PermissionAA.");
		Assertions.assertTrue(userPermissions.containsKey(permissionAAA.name()), "TestuserB has PermissionAAA.");
		Assertions.assertTrue(userPermissions.containsKey(permissionB.name()), "TestuserB has PermissionB.");
		Assertions.assertTrue(userPermissions.containsKey(permissionBB.name()), "TestuserB has PermissionBB.");
		Assertions.assertFalse(userPermissions.containsKey(permissionC.name()), "TestuserB HASN'Ts PermissionC.");
		
		//--------------------------------------
		// Test remove RolePermissionMapping when role is deleted
		System.out.println("================= Test remove RolePermissionMapping when role is deleted =================");
		//Cleanup
		CFW.DB.Roles.deleteByName("TestRoleToDelete");
		
		Role roleToDelete = new Role("TestRoleToDelete", "user");
		
		CFW.DB.Roles.create(roleToDelete);
		roleToDelete = CFW.DB.Roles.selectFirstByName("TestRoleToDelete");
		
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAA, roleToDelete, true);
		Assertions.assertTrue(CFW.DB.RolePermissionMap.checkIsPermissionInRole(permissionAA, roleToDelete), "Permission was added to the role.");
		
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAAA, roleToDelete, true);
		Assertions.assertTrue(CFW.DB.RolePermissionMap.checkIsPermissionInRole(permissionAAA, roleToDelete), "Permission was added to the role.");
		
		CFW.DB.Roles.deleteByID(roleToDelete.id());
		Assertions.assertFalse(CFW.DB.RolePermissionMap.checkIsPermissionInRole(permissionAA, roleToDelete), "RolePermissionMapping was removed when role was deleted.");
		Assertions.assertFalse(CFW.DB.RolePermissionMap.checkIsPermissionInRole(permissionAAA, roleToDelete), "RolePermissionMapping was removed when role was deleted.");
		
	}
	
}

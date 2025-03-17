package com.xresch.cfw.tests.features.api;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.api.APIToken;
import com.xresch.cfw.features.api.APITokenDBMethods;
import com.xresch.cfw.features.api.APITokenPermission;
import com.xresch.cfw.features.api.APITokenPermissionDBMethods;
import com.xresch.cfw.features.api.APITokenPermissionMapDBMethods;
import com.xresch.cfw.tests._master.WebTestMaster;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpResponse;

public class TestAPITokenManagement extends WebTestMaster {

	protected static APIToken testtokenA;
	protected static APIToken testtokenB;
	protected static APIToken testtokenNoPermissions;
	
	protected static APITokenPermission permissionA;
	protected static APITokenPermission permissionB;
	
	@BeforeAll
	public static void fillWithTestData() {
		
		
		//------------------------------
		// APITokens
		
		APITokenDBMethods.create(new APIToken().token("testtokenA-123"));
		testtokenA = APITokenDBMethods.selectFirstByToken("testtokenA-123");
		
		APITokenDBMethods.create(new APIToken().token("testtokenB-abc"));
		testtokenB = APITokenDBMethods.selectFirstByToken("testtokenB-abc");
		
		APITokenDBMethods.create(new APIToken().token("testtokenNoPermissions-%?£!"));
		testtokenNoPermissions = APITokenDBMethods.selectFirstByToken("testtokenNoPermissions-%?£!");
		
		//------------------------------
		// APITokenPermissions
		APITokenPermissionDBMethods.oneTimeCreate(new APITokenPermission().apiName("User").actionName("fetchData"));
		permissionA =  APITokenPermissionDBMethods.selectFirst("User", "fetchData");
		
		APITokenPermissionDBMethods.oneTimeCreate(new APITokenPermission().apiName("Role").actionName("fetchData"));
		permissionB =  APITokenPermissionDBMethods.selectFirst("Role", "fetchData");
		
		//------------------------------
		// Add Permissions to Tokens
		APITokenPermissionMapDBMethods.addPermissionToAPIToken(permissionA, testtokenA);
		APITokenPermissionMapDBMethods.addPermissionToAPIToken(permissionB, testtokenB);
		
		CFW.DB.transactionCommit();
		CFW.DB.transactionStart();
	}
	
	
	@Test
	public void testAPIToken() {
		
		String baseURL = "http://localhost:"+CFW.Properties.HTTP_PORT+"/app/api?";
		
		//--------------------------------------
		// Prechecks
		Assertions.assertNotNull(testtokenA, "Testtoken A is in the database.");
		Assertions.assertNotNull(testtokenB, "Testtoken B is in the database.");
		Assertions.assertNotNull(permissionA, "Permission A is in the database.");
		Assertions.assertNotNull(permissionB, "Permission B is in the database.");	
		
		//--------------------------------------
		// Test token only return API List 
		String requestURL = baseURL + CFW.HTTP.encode("apitoken", testtokenA.token());
		CFWHttpResponse responseWithAPIList = CFW.HTTP.sendGETRequest(requestURL);
		
		String jsonAPIList = responseWithAPIList.getResponseBody();
		System.out.println("================ Response: API List ================");
		System.out.println(jsonAPIList);
		Assertions.assertTrue(jsonAPIList.contains("List of permitted APIs"), "The list of permitted Apis is returned.");
		Assertions.assertTrue(jsonAPIList.contains("\"name\":\"User\",\"action\":\"fetchData\""), "Contains the expected permission.");
		
		Assertions.assertFalse(jsonAPIList.contains("\"name\":\"Role\""), "Does not contain the permission which is not granted.");
		
		//--------------------------------------
		// Test Roles.fetchData with permissions
		HashMap<String, String> paramsB = new HashMap<>();
		paramsB.put("apitoken", testtokenB.token());
		paramsB.put("apiName", permissionB.apiName());
		paramsB.put("actionName", permissionB.actionName());
		
		CFWHttpResponse responseWithRoles = CFW.HTTP.sendGETRequest(baseURL, paramsB);
		String jsonRoles = responseWithRoles.getResponseBody();
		
		System.out.println("================ Response: Roles.fetchData with permissions ================");
		System.out.println(jsonRoles);	
		Assertions.assertTrue(jsonRoles.contains("\"NAME\":\"Superuser\""), "List of roles is returned.");
		
		//--------------------------------------
		// Test Roles.fetchData Without Permissions
		HashMap<String, String> paramsA = new HashMap<>();
		paramsA.put("apitoken", testtokenA.token());
		paramsA.put("apiName", permissionB.apiName());
		paramsA.put("actionName", permissionB.actionName());
		
		CFWHttpResponse responseWithoutRoles = CFW.HTTP.sendGETRequest(baseURL, paramsA);
		String jsonNoRoles = responseWithoutRoles.getResponseBody();
		
		System.out.println("================ Response: Roles.fetchData without permissions ================");
		System.out.println(jsonNoRoles);	
		
		Assertions.assertTrue(jsonNoRoles.contains("The token does not have access to the API Role.fetchData."), "Access is denied.");
		Assertions.assertFalse(jsonNoRoles.contains("Superuser"), "List of roles is not returned.");
		
		//--------------------------------------
		// Test Zero Permissions defined for Token
		HashMap<String, String> paramsNoPermissions = new HashMap<>();
		paramsNoPermissions.put("apitoken", testtokenNoPermissions.token());
		
		CFWHttpResponse responseNoPermissions = CFW.HTTP.sendGETRequest(baseURL, paramsNoPermissions);
		String jsonNoPermissions = responseNoPermissions.getResponseBody();
		
		System.out.println("================ Zero Permissions defined for Token ================");
		System.out.println(jsonNoPermissions);	
		
		Assertions.assertTrue(jsonNoPermissions.contains("\"payload\": []"), "List of APIs is empty.");
		
		//--------------------------------------
		// Test Header Token
		HashMap<String, String> paramsHeaderToken = new HashMap<>();
		paramsHeaderToken.put("apiName", permissionB.apiName());
		paramsHeaderToken.put("actionName", permissionB.actionName());
		
		HashMap<String, String> headers = new HashMap<>();
		headers.put("API-Token", testtokenB.token());
		
		CFWHttpResponse responseHeaderToken = CFW.HTTP.sendGETRequest(baseURL, paramsHeaderToken, headers);
		String jsonRoles2 = responseHeaderToken.getResponseBody();
		
		System.out.println("================ Response: Test Header Token ================");
		System.out.println(jsonRoles2);	
		Assertions.assertTrue(jsonRoles2.contains("\"NAME\":\"Superuser\""), "List of roles is returned.");
	}
	
}

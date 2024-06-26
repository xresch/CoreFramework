package com.xresch.cfw.tests.datahandling;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWObject extends DBTestMaster{
	protected static Role testgroupA;
	
	@BeforeAll
	public static void createTestData() {
		
		//------------------------------
		// Groups
		CFW.DB.Roles.create(new Role("TestgroupA", FeatureUserManagement.CATEGORY_USER));
		testgroupA = CFW.DB.Roles.selectFirstByName("TestgroupA");
		testgroupA.description("TestgroupADescription");
		
		CFW.DB.Roles.update(testgroupA);
	}
	
	@Test
	public void testMapResultSet() throws SQLException {
		

		String selectByName = 
				"SELECT "
				  + Role.RoleFields.PK_ID +", "
				  + Role.RoleFields.NAME +", "
				  + Role.RoleFields.DESCRIPTION +", "
				  + Role.RoleFields.IS_DELETABLE +" "
				+" FROM "+Role.TABLE_NAME
				+" WHERE "
				+ Role.RoleFields.NAME + " = ?";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectByName, "TestgroupA");
		
		Assertions.assertNotNull(result, "Result was found.");
		
		Role group = new Role(result);

	}
	
	
	@Test
	public void testSerializeToJson() {
		
		//---------------------------------
		// Test Simple CFWObject
		//---------------------------------
		CFWObject object = new CFWObject();
		object.addField(CFWField.newInteger(FormFieldType.NUMBER, "testInteger"));
		object.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "testBoolean"));
		object.addField(CFWField.newString(FormFieldType.TEXT, "testString"));

		ArrayList<String> arrayValue = new ArrayList<>();
		arrayValue.add("foo");
		arrayValue.add("bar");
		object.addField(CFWField.newArray(FormFieldType.TAGS, "testArray").setValue(arrayValue));
		
		String json = CFW.JSON.toJSON(object);
		System.out.println(json);
		Assertions.assertEquals("{\"testInteger\":null,\"testBoolean\":null,\"testString\":null,\"testArray\":[\"foo\",\"bar\"]}",
				json,			
				"Object is serialized.");
		//---------------------------------
		// Test Subclass CFWObject and
		// Hierarchy
		//---------------------------------
		Role role = new Role()
				.id(1)
				.category(FeatureUserManagement.CATEGORY_USER)
				.name("Top Role")
				.description("Test top role.");
				
		Role subrole = new Role()
			.id(2)
			.category(FeatureUserManagement.CATEGORY_USER)
			.name("Sub Role")
			.description("Test sub role.");
		
		Role subroleB = new Role()
				.id(3)
				.category(FeatureUserManagement.CATEGORY_USER)
				.name("Sub Role B")
				.description("Test sub role B.");
		
		Role subroleBChild = new Role()
				.id(4)
				.category(FeatureUserManagement.CATEGORY_USER)
				.name("Sub Role B Child")
				.description("Test sub role B child.");
		
		role.getChildObjects().put(2, subrole);
		role.getChildObjects().put(3, subroleB);
		subroleB.getChildObjects().put(4, subroleBChild);
		
		String jsonHierarchy = CFW.JSON.toJSON(role);
		System.out.println(jsonHierarchy);
		
		// approximate assertion, if asserting on full string will fail whenever there are changes to Role.java
		Assertions.assertEquals(true, jsonHierarchy.startsWith("{"));
		Assertions.assertEquals(true, jsonHierarchy.endsWith("}"));
		Assertions.assertEquals(true, jsonHierarchy.contains("\""+RoleFields.PK_ID+"\""));
		Assertions.assertEquals(true, jsonHierarchy.contains("\""+RoleFields.CATEGORY+"\""));
		Assertions.assertEquals(true, jsonHierarchy.contains("\""+RoleFields.DESCRIPTION+"\""));
		Assertions.assertEquals(true, jsonHierarchy.contains("\""+RoleFields.JSON_EDITORS+"\""));


	}
	
	
	
}

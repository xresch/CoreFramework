package com.pengtoolbox.cfw.tests.datahandling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.tests._master.DBTestMaster;

public class TestCFWObject extends DBTestMaster{
	protected static Role testgroupA;
	
	@BeforeClass
	public static void createTestData() {
		
		//------------------------------
		// Groups
		CFW.DB.Roles.create(new Role("TestgroupA", "user"));
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
		object.addField(CFWField.newArray(FormFieldType.TAGS, "testArray").setValue(new String[] {"foo", "bar"}));
		object.addField(CFWField.newDate(FormFieldType.DATEPICKER, "testDate").setValue(new Date(2020, 01, 03)));
		object.addField(CFWField.newTimestamp(FormFieldType.DATEPICKER, "testTimestamp").setValue(new Timestamp(2020, 01, 03, 4, 5, 500, 0)));
		
		String json = CFW.JSON.toJSON(object);
		System.out.println(json);
		Assertions.assertEquals("{\"testInteger\":null,\"testBoolean\":null,\"testString\":null,\"testArray\":[\"foo\",\"bar\"],\"testDate\":61538828400000,\"testTimestamp\":61538843600000,\"children\":[]}",
				json,
				"Object is serialized.");
		//---------------------------------
		// Test Subclass CFWObject and
		// Hierarchy
		//---------------------------------
		Role role = new Role()
				.id(1)
				.category("user")
				.name("Top Role")
				.description("Test top role.");
				
		Role subrole = new Role()
			.id(2)
			.category("user")
			.name("Sub Role")
			.description("Test sub role.");
		
		Role subroleB = new Role()
				.id(3)
				.category("user")
				.name("Sub Role B")
				.description("Test sub role B.");
		
		Role subroleBChild = new Role()
				.id(4)
				.category("user")
				.name("Sub Role B Child")
				.description("Test sub role B child.");
		
		role.getChildObjects().put(2, subrole);
		role.getChildObjects().put(3, subroleB);
		subroleB.getChildObjects().put(4, subroleBChild);
		
		String jsonHierarchy = CFW.JSON.toJSON(role);
		System.out.println(jsonHierarchy);
		Assertions.assertEquals("{\"PK_ID\":1,\"CATEGORY\":\"user\",\"NAME\":\"Top Role\",\"DESCRIPTION\":\"Test top role.\",\"IS_DELETABLE\":true,\"IS_RENAMABLE\":true,\"children\":[{\"PK_ID\":2,\"CATEGORY\":\"user\",\"NAME\":\"Sub Role\",\"DESCRIPTION\":\"Test sub role.\",\"IS_DELETABLE\":true,\"IS_RENAMABLE\":true,\"children\":[]},{\"PK_ID\":3,\"CATEGORY\":\"user\",\"NAME\":\"Sub Role B\",\"DESCRIPTION\":\"Test sub role B.\",\"IS_DELETABLE\":true,\"IS_RENAMABLE\":true,\"children\":[{\"PK_ID\":4,\"CATEGORY\":\"user\",\"NAME\":\"Sub Role B Child\",\"DESCRIPTION\":\"Test sub role B child.\",\"IS_DELETABLE\":true,\"IS_RENAMABLE\":true,\"children\":[]}]}]}",
				jsonHierarchy,
				"Object is serialized.");
	}
	
	
	
}

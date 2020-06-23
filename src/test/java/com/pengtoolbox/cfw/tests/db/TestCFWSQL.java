package com.pengtoolbox.cfw.tests.db;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.tests._master.DBTestMaster;

public class TestCFWSQL extends DBTestMaster {

	
	@BeforeClass
	public static void fillWithTestData() {
		
	}

	@Test
	public void testCreateSQL() {
		
		String appleSQL = new CFWSQL(null)
				.queryCache(this.getClass(), "SelectApplesSQL")
				.custom("SELECT ID, CATEGORY, TYPE, NAME, COUNT FROM FRUITS ")
				.where("CATEGORY", "Fruit")
				.and("TYPE", "Apple")
				.orderbyDesc("NAME")
				.getStatementCached();
		
		System.out.println(appleSQL);
		
		Assertions.assertEquals("SELECT ID, CATEGORY, TYPE, NAME, COUNT FROM FRUITS  WHERE CATEGORY = ? AND TYPE = ? ORDER BY NAME DESC", 
				appleSQL,
				"The SQL is created.");
				
	}
	
}

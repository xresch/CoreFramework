package com.xresch.cfw.tests.db;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWSQL extends DBTestMaster {

	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@AfterAll
	public static void afterAll() {
		CFWDB.transactionRollback();
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@BeforeAll
	public static void beforeAll() {
		CFWDB.transactionStart();
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
		
		Assertions.assertEquals(" SELECT ID, CATEGORY, TYPE, NAME, COUNT FROM FRUITS   WHERE  T.\"CATEGORY\" = ? AND  T.\"TYPE\" = ? ORDER BY T.NAME DESC", 
				appleSQL,
				"The SQL is created.");
				
	}
	
}

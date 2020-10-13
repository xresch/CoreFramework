package com.xresch.cfw.tests.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWSQL extends DBTestMaster {

	
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
		
		Assertions.assertEquals(" SELECT ID, CATEGORY, TYPE, NAME, COUNT FROM FRUITS   WHERE CATEGORY = ? AND TYPE = ? ORDER BY T.NAME DESC", 
				appleSQL,
				"The SQL is created.");
				
	}
	
}

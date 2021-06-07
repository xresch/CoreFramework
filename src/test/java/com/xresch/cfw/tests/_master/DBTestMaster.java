package com.xresch.cfw.tests._master;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;

public class DBTestMaster extends WebTestMaster {
	
	@BeforeAll
	public static void startTransaction() throws Exception {
		// Make sure it is started, should be done by superclass.
		CFW.DB.initializeDB();
		CFWDB.beginTransaction();
	}
	
	@AfterAll
	public static void stopDefaultApplication() throws Exception {
		CFWDB.rollbackTransaction();
		System.out.println("========== ALERTS =========");
		System.out.println(CFW.Context.Request.getAlertsAsJSONArray());
	}
}

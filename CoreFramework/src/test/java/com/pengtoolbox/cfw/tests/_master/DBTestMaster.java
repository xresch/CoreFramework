package com.pengtoolbox.cfw.tests._master;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main._MainForTesting;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.logging.CFWLog;

public class DBTestMaster extends WebTestMaster {
	
	private static Logger logger = CFWLog.getLogger(DBTestMaster.class.getName());
	
	@BeforeClass
	public static void a_startDefaultApplication() throws Exception {
		
//		CFW.initializeApp(new _MainForTesting(), new String [] {});
//		//CFW.initialize("./config/cfw.properties");
//		CFWDB.initialize();
		
		CFWDB.beginTransaction();
	}
	
	@AfterClass
	public static void stopDefaultApplication() throws Exception {
		CFWDB.rollbackTransaction();
		System.out.println("========== ALERTS =========");
		System.out.println(CFW.Context.Request.getAlertsAsJSONArray());
	}
}

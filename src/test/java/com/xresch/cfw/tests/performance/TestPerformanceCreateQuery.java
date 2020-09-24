package com.xresch.cfw.tests.performance;

import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

public class TestPerformanceCreateQuery {

	// Run without JIT: -Djava.compiler=NONE
	
	public static void main(String[] args) {
		testSmallQuery();
		testSmallQueryWithAutoCache();
		testBiggerQuery();
		testBiggerQueryWithAutoCache();
		testBiggerQueryCached();
		testBiggerQueryWithAutoCacheCached();
		testBiggerQueryWithAutoCacheDeepStackCached();
	}
	
	
	public static void testSmallQuery() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache(TestPerformanceCreateQuery.class, "testSmallQuery")
				.select(UserFields.USERNAME)
				.where(UserFields.PK_ID.toString(), 2)
				.getStatementString();
		}
		System.out.println("============== testSmallQuery() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
	
	public static void testSmallQueryWithAutoCache() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache()
				.select(UserFields.USERNAME)
				.where(UserFields.PK_ID.toString(), 2)
				.getStatementString();
		}
		System.out.println("============== testSmallQueryWithAutoCache() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
	
	public static void testBiggerQuery() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache(TestPerformanceCreateQuery.class, "testBiggerQuery")
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			    .select()
				.where(UserFields.PK_ID, 2)
				.and("somefield", "somevalue")
				.limit(1000)
				.offset(22)
				.orderby(UserFields.USERNAME)
				.getStatementString();

		}
		System.out.println("============== testBiggerQuery() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
		
	public static void testBiggerQueryWithAutoCache() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache()
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			    .select()
				.where(UserFields.PK_ID, 2)
				.and("somefield", "somevalue")
				.limit(1000)
				.offset(22)
				.orderby(UserFields.USERNAME)
				.getStatementString();

		}
		System.out.println("============== testBiggerQueryWithAutoCache() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
	
	public static void testBiggerQueryCached() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache(TestPerformanceCreateQuery.class, "testBiggerQuery")
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			    .select()
				.where(UserFields.PK_ID, 2)
				.and("somefield", "somevalue")
				.limit(1000)
				.offset(22)
				.orderby(UserFields.USERNAME)
				.getStatementCached();

		}
		System.out.println("============== testBiggerQueryCached() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
	
	public static void testBiggerQueryWithAutoCacheCached() {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache()
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			    .select()
				.where(UserFields.PK_ID, 2)
				.and("somefield", "somevalue")
				.limit(1000)
				.offset(22)
				.orderby(UserFields.USERNAME)
				.getStatementCached();

		}
		System.out.println("============== testBiggerQueryWithAutoCacheCached() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}
	
	
	
	public static void testBiggerQueryWithAutoCacheDeepStackCached() {	
		VeryDeepAndJustCrazyHumongousStacktraceSimulator.simulateDeepStacktrace(20, 0, TestPerformanceCreateQuery.class, "testBiggerQueryWithAutoCacheDeepStackCached_execute");
	}
	
	public static void testBiggerQueryWithAutoCacheDeepStackCached_execute() {		
		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();

		CFWSQL sql = new CFWSQL(new User());
		for(int i = 0; i < 10000; i++) {
			sql.queryCache()
				.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
			    .select()
				.where(UserFields.PK_ID, 2)
				.and("somefield", "somevalue")
				.limit(1000)
				.offset(22)
				.orderby(UserFields.USERNAME)
				.getStatementCached();

		}
		System.out.println("============== testBiggerQueryWithAutoCacheDeepStackCached_execute() =============== ");
		System.out.println("time[ms]: "+(System.nanoTime() - start)/1000000);

	}

}

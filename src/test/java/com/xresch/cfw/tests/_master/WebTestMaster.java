package com.xresch.cfw.tests._master;

import javax.servlet.Servlet;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppInterface;
import com.xresch.cfw._main.CFWApplicationExecutor;

public class WebTestMaster {

	protected static CFWApplicationExecutor APP;
	protected static String TEST_URL;
	public static String RESOURCE_PACKAGE = "com.xresch.cfw.tests.assets.resources";
		
	public static void addServlet(Class<? extends Servlet> clazz, String contextPath) {
		
		boolean alreadyExists = APP.isServletPathUsed("/test"+contextPath);
		if(!alreadyExists) {
			System.out.println("ADD SERVLET:"+contextPath);
			APP.addUnsecureServlet(clazz, "/test"+contextPath);
		}
	}
	
	@BeforeClass
	public static void startDefaultApplication() throws Exception {
		
		CFW.initializeApp(new CFWAppInterface() {
			
			@Override
			public void settings() {
				
			}
			@Override
			public void stopApp() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void startApp(CFWApplicationExecutor app) {
				APP  = app;

				TEST_URL = "http://localhost:"+CFW.Properties.HTTP_PORT+"/test";
				
				//Seperate thread to not make the test thread block
				Runnable r = new Runnable() {

					@Override
					public void run() {
						try {
							APP.start();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				Thread t = new Thread(r);
				
				t.start();

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			@Override
			public void register() {
				// TODO Auto-generated method stub
				CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
			}
			
			@Override
			public void initializeDB() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void startTasks() {
				// TODO Auto-generated method stub
				
			}
		}, new String[] {});
		
		
		
	}
	
	@AfterClass
	public static void stopDefaultApplication() throws Exception {
		System.out.println("========== ALERTS =========");
		System.out.println(CFW.Context.Request.getAlertsAsJSONArray());
		APP.stop();
	}
}

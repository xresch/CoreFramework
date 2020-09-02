package com.xresch.cfw._main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWContextApp {
	
	private static CFWApplicationExecutor app;

	public static CFWApplicationExecutor getApp() {
		return app;
	}

	public static void setApp(CFWApplicationExecutor app) {
		CFWContextApp.app = app;
	}
	
	

}

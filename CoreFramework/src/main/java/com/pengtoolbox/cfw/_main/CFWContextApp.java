package com.pengtoolbox.cfw._main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
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

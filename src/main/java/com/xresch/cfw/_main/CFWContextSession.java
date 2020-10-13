package com.xresch.cfw._main;

import java.util.Collection;

import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.features.usermgmt.SessionData;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWContextSession {
	
	public static SessionData getSessionData(){
		return CFW.Context.Request.getSessionData();
	}
	
	public static void addForm(CFWForm form){
		CFW.Context.Request.getSessionData().addForm(form);
	}
	
	public static CFWForm getForm(String formID) {
		return CFW.Context.Request.getSessionData().getForm(formID);
	}
	
	public static Collection<CFWForm> getForms() {
		return CFW.Context.Request.getSessionData().getForms();
	}
	
	public static String getSessionID() {
		SessionData data =  CFW.Context.Request.getSessionData();
		return (data == null) ? null : data.getSessionID();
	}

}

package com.xresch.cfw._main;

import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWMessages {

	/****************************************************************
	 * Adds a custom success message to the Request Context. 
	 ****************************************************************/
	public static void addSuccessMessage(String message){
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, message);		
	}
	/****************************************************************
	 * Adds a custom success message to the Request Context. 
	 ****************************************************************/
	public static void addInfoMessage(String message){
		CFWContextRequest.addAlertMessage(MessageType.INFO, message);		
	}
	
	/****************************************************************
	 * Adds a custom warning message to the Request Context.
	 ****************************************************************/
	public static void addWarningMessage(String message){
		CFWContextRequest.addAlertMessage(MessageType.WARNING, message);		
	}
	
	/****************************************************************
	 * Adds a custom error message to the Request Context.
	 ****************************************************************/
	public static void addErrorMessage(String message){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, message);		
	}
		
	/****************************************************************
	 * Adds a localized "Access Denied" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void saved(){
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, CFW.L("cfw_core_success_saved", "Saved!"));		
	}
	
	/****************************************************************
	 * Adds a localized "Access Denied" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void deleted(){
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, CFW.L("cfw_core_success_deleted", "Deleted!"));		
	}
	
	/****************************************************************
	 * Adds a localized "Access Denied" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void accessDenied(){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));		
	}

	/****************************************************************
	 * Adds a localized "No Permission" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void noPermission(){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_nopermission", "You do not have the required permission to execute this action."));		
	}

	/****************************************************************
	 * Adds a localized "Item not Supported" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void itemNotSupported(String itemValue){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_itemnotsupported", "The value '"+itemValue+"' is not supported for the parameter item.", itemValue));		
	}
	
	/****************************************************************
	 * Adds a localized "Action not Supported" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void actionNotSupported(String action){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_actionnotsupported", "The action '"+action+"' is not supported by this servlet.", action));		
	}
	
}

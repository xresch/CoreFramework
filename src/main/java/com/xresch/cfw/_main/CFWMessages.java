package com.xresch.cfw._main;

public class CFWMessages {

	public enum MessageType {
		INFO, 
		SUCCESS, 
		WARNING, 
		ERROR;
		
		public static boolean hasMessageType(String value) {
			if(value == null) { return false; }
			if(value.equals("INFO")
			|| value.equals("SUCCESS")
			|| value.equals("WARNING")
			|| value.equals("ERROR")
			){
				return true;
			}
			return false;
		}
	}

	/****************************************************************
	 * Adds a custom message to the Request Context. 
	 ****************************************************************/
	public static void addMessage(MessageType type, String message){
		CFWContextRequest.addAlertMessage(type, message);		
	}
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
	 * Adds a localized "Saved!" success message to the Request Context.
	 *   
	 ****************************************************************/
	public static void saved(){
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, CFW.L("cfw_core_success_saved", "Saved!"));		
	}
	
	/****************************************************************
	 * Adds a localized "Done!" success message to the Request Context.
	 *   
	 ****************************************************************/
	public static void done(){
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, CFW.L("cfw_core_success_done", "Done!"));		
	}
	
	/****************************************************************
	 * Adds a localized "Deleted!" success message to the Request Context.
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
	 * Adds a localized "No Permission To Edit" error message to the
	 * Request Context.
	 *   
	 ****************************************************************/
	public static void noPermissionToEdit(){
		CFWContextRequest.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_nopermissiontoedit", "You do not have the required permission to edit this item."));		
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

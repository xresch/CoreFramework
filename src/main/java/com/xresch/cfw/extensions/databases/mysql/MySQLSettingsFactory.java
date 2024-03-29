package com.xresch.cfw.extensions.databases.mysql;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class MySQLSettingsFactory {
	
	public static final String FIELDNAME_ENVIRONMENT = "environment";

	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		// use string to allow parameter substitution
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ENVIRONMENT)
				.setLabel("{!cfw_dbextensions_environment!}")
				.setDescription("{!cfw_dbextensions_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(MySQLEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
		

}

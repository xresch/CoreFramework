package com.xresch.cfw.extensions.databases.mssql;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class MSSQLSettingsFactory {
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
				.setLabel("{!cfw_dbextensions_environment!}")
				.setDescription("{!cfw_dbextensions_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(MSSQLEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
		

}

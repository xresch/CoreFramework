package com.xresch.cfw.features.eav.api;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class APIEAVPushStatsCSVFields extends CFWObject {
	
	public APIEAVPushStatsCSVFields() {
		
		this.addField(
				CFWField.newString(FormFieldType.NONE, APIEAVPushStatsCSV.FIELDNAME_SEPARATOR)
					.setDescription("The separator for the CSV data. (Default: ,)")
					.setValue(",")
			);
		
		this.addField(
				CFWField.newString(FormFieldType.TEXTAREA, APIEAVPushStatsCSV.FIELDNAME_VALUES)
					.setDescription("The query that should be executed.")
					//.addAttribute("rows", "15")
					.setValue("""
						category,entity,attributes,count,min,avg,max,sum,p50,p95
						TestResults,010_Open_Homepage,"{status: \\"ok\\" }",42,150,240,5000,15000,444,1234 
						TestResults,010_Open_Homepage,"{status: \\"nok\\" }",33,133,330,3000,33000,330,3210 
						TestResults,010_Open_Homepage,"{status: \\"all\\" }",75,150,222,6655,48000,1234,4544
						TestResults,020_Login,"{status: \\"ok\\" }",77,177,777,7777,17070,707,7007 
						TestResults,020_Login,"{status: \\"nok\\" }",11,111,1111,5111,15111,501,5001 
						TestResults,020_Login,"{status: \\"all\\" }",88,288,822,8822,88000,2828,8008
						""")
			);
		
		this.addField(
				CFWField.newString(FormFieldType.NONE, APIEAVPushStatsCSV.FIELDNAME_JSON_OBJECT)
					.setDescription("Returns a Json object with status info.")
			);
	}
	
}
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
public class APIEAVPushStatsFields extends CFWObject {
	
	public APIEAVPushStatsFields() {
		
		this.addField(
				CFWField.newString(FormFieldType.TEXTAREA, APIEAVPushStats.FIELDNAME_VALUES)
					.setDescription("The query that should be executed.")
					.addAttribute("rows", "15")
					.setValue("""
						[
							{ 	
								  "type": "counter"
								, "category": "customCategory"
								, "entity": "UsecaseStepName"
								, "value": "1"
								, "attributes": {metric: "count", "user": "jane.doe", "load-agent": "loadserver123win" }
							}
							, { 
								  "type": "value"
								, "category": "customCategory"
								, "entity": "UsecaseStepName"
								, "value": "1230"
								, "attributes": {metric: "responsetime", "user": "jane.doe", "load-agent": "loadserver123win"  }
							}
						]""")
			);
		
		this.addField(
				CFWField.newString(FormFieldType.NONE, APIEAVPushStats.FIELDNAME_JSON_OBJECT)
					.setDescription("The returned values depend on the executed queries.")
			);
	}
	
}
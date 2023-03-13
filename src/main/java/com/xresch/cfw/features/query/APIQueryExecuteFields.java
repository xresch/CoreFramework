package com.xresch.cfw.features.query;

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
public class APIQueryExecuteFields extends CFWObject {
	
	public APIQueryExecuteFields() {
		this.addField(
				CFWField.newTimeframe(APIQueryExecute.FIELDNAME_TIME)
				.setLabel("Time")
				.setDescription("The timeframe the query should be executed for, specified in JSON object notation. You can either specify an offset or earliest/latest. If offset is specified, earliest/latest is ignored. Parameter clientTimezoneOffset(offset in minutes) might be needed to adjust for timezone differences.")
				.addValidator(new NotNullOrEmptyValidator())
				.setValue(new CFWTimeframe())
				);
		
		this.addField(
				CFWField.newString(FormFieldType.TEXTAREA, APIQueryExecute.FIELDNAME_QUERY)
					.setDescription("The query that should be executed.")
					.setValue("| source random limit=10")
			);
		
		this.addField(
				CFWField.newString(FormFieldType.NONE, APIQueryExecute.FIELDNAME_JSON_OBJECT)
					.setDescription("The returned values depend on the executed queries.")
			);
	}
	
	public CFWTimeframe getTimeframe() {
		return (CFWTimeframe)this.getField(APIQueryExecute.FIELDNAME_TIME).getValue();
	}
}
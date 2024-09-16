package com.xresch.cfw.features.query.sources;

import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceCSV extends CFWQuerySource {

	private static final String SOURCE_NAME = "csv";

	private static final String FIELDNAME_DATA = "data";
	private static final String FIELDNAME_SEPARATOR = "separator";
	private static final String FIELDNAME_COUNT = "count";
	private static final String FIELDNAME_EMPTY = "empty";

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceCSV(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "csv";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes a csv input and reads it as a table.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Not applicable.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_"+SOURCE_NAME+".html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return "None";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// do nothing
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_DATA)
						.setDescription("The text that should be splitted into records.")
						.addValidator(new NotNullOrEmptyValidator())
						.disableSanitization()
				)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_SEPARATOR)
							.setDescription("The separator used for splitting the text, uses regular expressions. (Default: ',')")
							.setValue(",")
							.disableSanitization()
					)
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//do nothing
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		String data = (String)parameters.getField(FIELDNAME_DATA).getValue();
		String separator = (String)parameters.getField(FIELDNAME_SEPARATOR).getValue();

		//-------------------------------
		// Handle Text Empty
		if(Strings.isNullOrEmpty(data)) {
			return;
		}
		
		//-------------------------------
		// Handle Separator Empty
		if(Strings.isNullOrEmpty(separator)) {
			separator=",";
		}
		
		//-------------------------------
		// Handle Text & Separator
		
		JsonArray csvArray = CFW.CSV.toJsonArray(data, separator, false, false);
		
		for(JsonElement element : csvArray) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			outQueue.add(object);
		}
		
	}

}

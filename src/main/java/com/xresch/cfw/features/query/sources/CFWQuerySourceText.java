package com.xresch.cfw.features.query.sources;

import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
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
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceText extends CFWQuerySource {

	private static final String FIELDNAME_TEXT = "text";
	private static final String FIELDNAME_SEPARATOR = "separator";
	private static final String FIELDNAME_COUNT = "count";

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceText(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "text";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes a plain txt input and splits it up in records.";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_text.html");
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
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TEXT)
						.setDescription("The text that should be splitted into records.")
						.addValidator(new NotNullOrEmptyValidator())
				)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_SEPARATOR)
							.setDescription("The separator used for splitting the text, supports regular expressions. (Default: newline)")
							.setValue("\n")
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_COUNT)
							.setDescription("A string that should be counted.")	
							.setValue(null)
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
		
		String text = (String)parameters.getField(FIELDNAME_TEXT).getValue();
		String separator = (String)parameters.getField(FIELDNAME_SEPARATOR).getValue();
		String countThis = (String)parameters.getField(FIELDNAME_COUNT).getValue();
		
		//-------------------------------
		// Handle Text Empty
		if(Strings.isNullOrEmpty(text)) {
			return;
		}
		
		//-------------------------------
		// Handle Separator Empty
		if(Strings.isNullOrEmpty(separator)) {
			EnhancedJsonObject object = new EnhancedJsonObject();
			object.addProperty("text", text);
			outQueue.add(object);
			return;
		}
		
		//-------------------------------
		// Handle Text & Separator
		String[] splittedString = text.split(separator);
		boolean hasCount = (countThis != null);
		for(String part : splittedString) {
			EnhancedJsonObject object = new EnhancedJsonObject();
			object.addProperty("part", part);
			if(hasCount) {
				object.addProperty("count", StringUtils.countMatches(part, countThis));
			}
			outQueue.add(object);
		}
		
	}

}

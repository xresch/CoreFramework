package com.xresch.cfw.features.query.sources;

import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
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
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceText extends CFWQuerySource {

	private static final String FIELDNAME_TEXT = "text";
	private static final String FIELDNAME_SEPARATOR = "separator";
	private static final String FIELDNAME_COUNT = "count";
	private static final String FIELDNAME_EMPTY = "empty";

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
						.disableSanitization()
				)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_SEPARATOR)
							.setDescription("The separator used for splitting the text, uses regular expressions. (Default: newline)")
							.setValue("\n")
							.disableSanitization()
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_COUNT)
							.setDescription("(Optional) A regular expressions who's number of matches should be counted in each record.")	
							.setValue(null)
					)
				.addField(
						CFWField.newBoolean(FormFieldType.TEXTAREA, FIELDNAME_EMPTY)
						.setDescription("Include empty records in the results.(Default: false)")	
						.setValue(false)
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
		Boolean includeEmpty = (Boolean)parameters.getField(FIELDNAME_EMPTY).getValue();

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
		Pattern regex = (hasCount) ? Pattern.compile(countThis) : null;
		
		for(String part : splittedString) {
			if(!includeEmpty && Strings.isNullOrEmpty(part.trim())) {
				continue;
			}
			EnhancedJsonObject object = new EnhancedJsonObject();
			object.addProperty("part", part);
			if(hasCount) {
				Matcher matcher = regex.matcher(part);
				int count = 0;
				while(matcher.find()) { count++; }
				object.addProperty("count", count);
			}
			outQueue.add(object);
		}
		
	}

}

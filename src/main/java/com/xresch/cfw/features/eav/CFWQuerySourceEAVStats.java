package com.xresch.cfw.features.eav;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceEAVStats extends CFWQuerySource {

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceEAVStats(CFWQuery parent) {
		super(parent);
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "eavstats";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches statistical data from Entry-Attribute-Value(EAV) tables.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "The timeframe selected with the timeframe picker is used to filter the values.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() { 
		return CFW.Files.readPackageResource(FeatureEAV.PACKAGE_MANUAL, "source_eavstats.html");
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return ""+FeatureEAV.PERMISSION_EAV_USER;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureEAV.PERMISSION_EAV_USER);
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
					CFWField.newString(FormFieldType.TEXT, "category")
						.setDescription("The name of the category.")
						.addValidator(new NotNullOrEmptyValidator())
				)
			.addField(
					CFWField.newString(FormFieldType.TEXT, "entity")
					.setDescription("The name of the entity.")
					.addValidator(new NotNullOrEmptyValidator())
					)
			.addField(
					CFWField.newString(FormFieldType.TEXT, "attributes")
					.setDescription("The attributes to filter by.")
					.addValidator(new NotNullOrEmptyValidator())
					)
			.addField(
					CFWField.newBoolean(FormFieldType.BOOLEAN, "detailed")
					.setDescription("If set to true, fetch all attributes and not only the ones used in the filtering. (Default: false)")
					.addValidator(new NotNullOrEmptyValidator())
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
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		String category = (String)parameters.getField("category").getValue();
		String entity = (String)parameters.getField("entity").getValue();
		String attributesString = (String)parameters.getField("attributes").getValue();
		Boolean detailed = (Boolean)parameters.getField("detailed").getValue();

		LinkedHashMap<String, String> attributes = CFW.JSON.fromJsonLinkedHashMap(attributesString);
		
		if(attributes == null) {
			attributes = new LinkedHashMap<>();
		}
		
		JsonArray array = CFWDBEAVStats.fetchStatsAsJsonArray(
									category
									, entity
									, attributes
									, earliestMillis
									, latestMillis
									, detailed
									);
		
		for(int i = 0; i < array.size(); i++) {
			
			EnhancedJsonObject object = new EnhancedJsonObject( array.get(i).getAsJsonObject());
			outQueue.add(object);
			
			if( isLimitReached(limit, i)) { break; }
		}
		
		

	}

}

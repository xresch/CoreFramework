package com.xresch.cfw.features.eav;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class EAVEntity extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_EAV_ENTITY";
	
	public enum EAVEntityFields{
		PK_ID,
		CATEGORY,
		NAME,
		DESCRIPTION,
		FORMFIELD_TYPE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, EAVEntityFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.apiFieldType(FormFieldType.NUMBER)
									.setDescription("The id of the entity.")
									.setValue(null);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, EAVEntityFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(128)")
									.setDescription("The category of the entity.")
									.addValidator(new LengthValidator(1, 128))
									;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, EAVEntityFields.NAME.toString())
									.setColumnDefinition("VARCHAR(4096)")
									.setDescription("The name of the entity.")
									.addValidator(new LengthValidator(1, 4096))
									;
	
	private CFWField<String> formfieldType = CFWField.newString(FormFieldType.TEXT, EAVEntityFields.FORMFIELD_TYPE.toString())
			.setColumnDefinition("VARCHAR(128)")
			.setDescription("The form field type of the entity.")
			.addValidator(new LengthValidator(-1, 128));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, EAVEntityFields.DESCRIPTION.toString())
											.setColumnDefinition("VARCHAR(4096)")
											.setDescription("The description of the entity.")
											.addValidator(new LengthValidator(-1, 4096));
	
	public EAVEntity() {
		initialize();
	}
	
	public EAVEntity(String category, String name) {
		initialize();
		this.category.setValue(category);
		this.name.setValue(name);
	}
	
	public EAVEntity(String category, String name, String description) {
		initialize();
		this.category.setValue(category);
		this.name.setValue(name);
		this.description.setValue(description);
	}
	
	private void initialize() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, category, name, formfieldType, description);
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
						
		String[] inputFields = 
				new String[] {
						EAVEntityFields.PK_ID.toString(), 
						EAVEntityFields.CATEGORY.toString(),
						EAVEntityFields.NAME.toString(),
						EAVEntityFields.FORMFIELD_TYPE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						EAVEntityFields.PK_ID.toString(), 
						EAVEntityFields.CATEGORY.toString(),
						EAVEntityFields.NAME.toString(),
						EAVEntityFields.DESCRIPTION.toString(),
						EAVEntityFields.FORMFIELD_TYPE.toString(),
				};

		//----------------------------------
		// fetchData
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		return apis;
	}
	
	public int id() {
		return id.getValue();
	}
	
	public EAVEntity id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public EAVEntity category(String value) {
		this.category.setValue(value);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public EAVEntity name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public EAVEntity description(String value) {
		this.description.setValue(value);
		return this;
	}

	public String formfieldType() {
		return formfieldType.getValue();
	}

	public EAVEntity formfieldType(FormFieldType value) {
		this.formfieldType.setValue(value.name());
		return this;
	}

	
	



	
	
}

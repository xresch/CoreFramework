package com.xresch.cfw.features.eav;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class EAVAttribute extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_EAV_ATTRIBUTE";
	
	enum EAVAttributeFields{
		PK_ID, 
		FK_ID_ENTITY,
		NAME,
		DESCRIPTION,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, EAVAttributeFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the attribute.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeyEntity = CFWField.newInteger(FormFieldType.HIDDEN, EAVAttributeFields.FK_ID_ENTITY)
			.setForeignKeyCascade(this, EAVEntity.class, EAVEntityFields.PK_ID)
			.setDescription("The id of the attribute.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, EAVAttributeFields.NAME)
			.setColumnDefinition("VARCHAR(4096)")
			.setDescription("The name of the attribute.")
			.addValidator(new LengthValidator(1, 4096))
			;

	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXT, EAVAttributeFields.DESCRIPTION)
			.setColumnDefinition("VARCHAR(4096)")
			.setDescription("The description of the attribute.")
			.addValidator(new LengthValidator(1, 4096))
			;
	
	public EAVAttribute() {
		initializeFields();
	}
	
	public EAVAttribute(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyEntity, name, description);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						EAVAttributeFields.PK_ID.toString(), 
						EAVAttributeFields.FK_ID_ENTITY.toString(),
						EAVAttributeFields.NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						EAVAttributeFields.PK_ID.toString(), 
						EAVAttributeFields.NAME.toString(),
						EAVAttributeFields.FK_ID_ENTITY.toString(),
						EAVAttributeFields.DESCRIPTION.toString(),
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
	
	
	public Integer id() {
		return id.getValue();
	}

	public Integer foreignKeyUser() {
		return foreignKeyEntity.getValue();
	}
	
	public EAVAttribute foreignKeyUser(Integer value) {
		this.foreignKeyEntity.setValue(value);
		return this;
	}	
	
	public String name() {
		return name.getValue();
	}
	
	public EAVAttribute name(String value) {
		this.name.setValue(value);
		return this;
	}	
	
	public String description() {
		return description.getValue();
	}
	
	public EAVAttribute description(String value) {
		this.description.setValue(value);
		return this;
	}	
	

}

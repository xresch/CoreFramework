package com.xresch.cfw.features.eav;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class EAVValue extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_EAV_VALUE";
	
	enum EAVValueFields{
		PK_ID, 
		FK_ID_ENTITY,
		FK_ID_ATTR,
		VALUE,
		CUSTOM,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, EAVValueFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the value.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeyEntity = CFWField.newInteger(FormFieldType.HIDDEN, EAVValueFields.FK_ID_ENTITY)
			.setForeignKeyCascade(this, EAVEntity.class, EAVEntityFields.PK_ID)
			.setDescription("The id of the entity.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyAttribute = CFWField.newInteger(FormFieldType.HIDDEN, EAVValueFields.FK_ID_ATTR)
			.setForeignKeyCascade(this, EAVAttribute.class, EAVEntityFields.PK_ID)
			.setDescription("The id of the attribute.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, EAVValueFields.VALUE)
			//.setColumnDefinition("VARCHAR(4096)")
			.setDescription("The actual value of this EAV Value.")
			.addValidator(new LengthValidator(1, 4096))
			;

	private CFWField<String> custom = CFWField.newString(FormFieldType.TEXT, EAVValueFields.CUSTOM)
			.setDescription("Custom data you want to associate with this value.")
			.addValidator(new LengthValidator(1, 4096))
			;
	
	public EAVValue() {
		initializeFields();
	}
	
	public EAVValue(int entityID, int attributeID, String value) {
		initializeFields();
		this.foreignKeyEntity(entityID);
		this.foreignKeyAttribute(attributeID);
		this.value(value);
	}
	
	public EAVValue(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyEntity, foreignKeyAttribute, value, custom);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void updateTable() {
		
		// add constraint to make sure no duplicate values are in database.
		new CFWSQL(null)
			.custom("ALTER TABLE "+TABLE_NAME
					+" ADD CONSTRAINT IF NOT EXISTS UNIQUE_VALUES UNIQUE("
					+ EAVValueFields.FK_ID_ENTITY
					+", "+EAVValueFields.FK_ID_ATTR
					+", \""+EAVValueFields.VALUE+"\");")
			.execute();
			;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						EAVValueFields.PK_ID.toString(), 
						EAVValueFields.FK_ID_ENTITY.toString(),
						EAVValueFields.FK_ID_ATTR.toString(),
						EAVValueFields.VALUE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						EAVValueFields.PK_ID.toString(), 
						EAVValueFields.FK_ID_ENTITY.toString(),
						EAVValueFields.FK_ID_ATTR.toString(),
						EAVValueFields.VALUE.toString(),
						EAVValueFields.CUSTOM.toString(),
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

	public Integer foreignKeyEntity() {
		return foreignKeyEntity.getValue();
	}
	
	public EAVValue foreignKeyEntity(String value) {
		return foreignKeyEntity(value);
	}	
	public EAVValue foreignKeyEntity(Integer value) {
		this.foreignKeyEntity.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyAttribute() {
		return foreignKeyAttribute.getValue();
	}
	
	public EAVValue foreignKeyAttribute(String value) {
		return foreignKeyAttribute(value);
	}	
	public EAVValue foreignKeyAttribute(Integer value) {
		this.foreignKeyAttribute.setValue(value);
		return this;
	}	
	
	public String value() {
		return value.getValue();
	}
	
	public EAVValue value(String value) {
		this.value.setValue(value);
		return this;
	}	
	
	public String custom() {
		return custom.getValue();
	}
	
	public EAVValue custom(String value) {
		this.custom.setValue(value);
		return this;
	}	
	

}

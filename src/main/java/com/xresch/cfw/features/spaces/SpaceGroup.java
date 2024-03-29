package com.xresch.cfw.features.spaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SpaceGroup extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_SPACE_GROUP";
	
	public static final String CFW_SPACEGROUP_TESTSPACE = "	TestSpace";

	public enum SpaceGroupFields{
		PK_ID,
		NAME,
		DESCRIPTION,
	}

	private static Logger logger = CFWLog.getLogger(SpaceGroup.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, SpaceGroupFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the space group.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, SpaceGroupFields.NAME.toString())
			.setColumnDefinition("VARCHAR(255) UNIQUE")
			.setDescription("The name of the space group.")
			.addValidator(new LengthValidator(1, 255))
			.setChangeHandler(new CFWFieldChangeHandler<String>() {
				public boolean handle(String oldValue, String newValue) {
					if(name.isDisabled()) { 
						new CFWLog(logger)
						.severe("The name cannot be changed as the field is disabled.");
						return false; 
					}
					return true;
				}
			});
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, SpaceGroupFields.DESCRIPTION.toString())
			.setColumnDefinition("CLOB")
			.setDescription("The description of the space group.")
			.addValidator(new LengthValidator(-1, 2000000));
	

	public SpaceGroup() {
		initializeFields();
	}
	
	public SpaceGroup(String name) {
		initializeFields();
		this.name.setValue(name);
	}
	
	public SpaceGroup(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, name, description);
	}
			
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void initDB() {
			
		//-----------------------------------------
		// Create TestSpace
		//-----------------------------------------
		if(!CFW.DB.SpaceGroups.checkSpaceGroupExists(SpaceGroup.CFW_SPACEGROUP_TESTSPACE)) {
			CFW.DB.SpaceGroups.create(new SpaceGroup(SpaceGroup.CFW_SPACEGROUP_TESTSPACE)
				.description("A space group for development.")
			);
		}
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						SpaceGroupFields.PK_ID.toString(), 
						SpaceGroupFields.NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						SpaceGroupFields.PK_ID.toString(), 
						SpaceGroupFields.NAME.toString(),
						SpaceGroupFields.DESCRIPTION.toString(),
				};

		//----------------------------------
		// fetchJSON
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
	
	public SpaceGroup id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public SpaceGroup name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public SpaceGroup description(String description) {
		this.description.setValue(description);
		return this;
	}
	
}

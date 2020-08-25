package com.xresch.cfw.features.analytics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * Class to represent method signatures.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CPUSampleSignature extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_STATS_CPUSAMPLE_SIGNATURE";
	
	public enum CPUSampleSignatureFields{
		PK_ID,
		SIGNATURE
	}

	private static Logger logger = CFWLog.getLogger(CPUSampleSignature.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CPUSampleSignatureFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the method signature.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
		
	private CFWField<String> signature = CFWField.newString(FormFieldType.TEXT, CPUSampleSignatureFields.SIGNATURE.toString())
			.setColumnDefinition("VARCHAR UNIQUE")
			.setDescription("The signature of the method.")
			.addValidator(new LengthValidator(0, 255));
	
	public CPUSampleSignature() {
		initializeFields();
	}
	
	public CPUSampleSignature(String name) {
		initializeFields();
		this.signature.setValue(name);
	}
	
	public CPUSampleSignature(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, signature);
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						CPUSampleSignatureFields.PK_ID.toString(), 
						CPUSampleSignatureFields.SIGNATURE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CPUSampleSignatureFields.PK_ID.toString(), 
						CPUSampleSignatureFields.SIGNATURE.toString()
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

	public int id() {
		return id.getValue();
	}
	
	public CPUSampleSignature id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String signature() {
		return signature.getValue();
	}
	
	public CPUSampleSignature signature(String signature) {
		this.signature.setValue(signature);
		return this;
	}
	
}

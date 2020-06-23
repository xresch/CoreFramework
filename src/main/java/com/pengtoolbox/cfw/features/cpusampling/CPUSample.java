package com.pengtoolbox.cfw.features.cpusampling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CPUSample extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_STATS_CPUSAMPLE";
	
	public enum StatsCPUSampleFields{
		PK_ID,
		TIME,
		FK_ID_SIGNATURE,
		FK_ID_PARENT,
		COUNT,
		INTERVAL,
		GRANULARITY, 
		MIN,
		AVG,
		MAX,
	}

	private static Logger logger = CFWLog.getLogger(CPUSample.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the statistic.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Timestamp> time = CFWField.newTimestamp(FormFieldType.NONE, StatsCPUSampleFields.TIME)
			.setDescription("The date and time of when the statistic was written to the database.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Integer> foreignKeySignature = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.FK_ID_SIGNATURE)
			.setForeignKeyCascade(this, CPUSampleSignature.class, StatsCPUSampleFields.PK_ID)
			.setDescription("The id of the method signature.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyParent = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.FK_ID_PARENT)
			.setForeignKeyCascade(this, CPUSampleSignature.class, StatsCPUSampleFields.PK_ID)
			.setDescription("The id of the parent method signature.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	private CFWField<Integer> count = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.COUNT)
			.setDescription("The total number of occurences of this method(estimated to occurences per minute).")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> min = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.MIN)
			.setDescription("The minimun number of occurences.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> avg = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.AVG)
			.setDescription("The average of the occurences.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> max = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.MAX)
			.setDescription("The maximum number of occurences.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> granularity = CFWField.newInteger(FormFieldType.NONE, StatsCPUSampleFields.GRANULARITY)
			.setDescription("The aggregation period in minutes represented by this statistics.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	
	public CPUSample() {
		initializeFields();
	}
		
	public CPUSample(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, time, foreignKeySignature, foreignKeyParent, count, min, avg, max, granularity);
	}
	
	/**************************************************************************************
	 * Migrate Table
	 **************************************************************************************/
	public void migrateTable() {
				
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void updateTable() {
						
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void initDB() {

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						StatsCPUSampleFields.PK_ID.toString(),
						StatsCPUSampleFields.TIME.toString(), 
						StatsCPUSampleFields.FK_ID_SIGNATURE.toString(),
						StatsCPUSampleFields.FK_ID_PARENT.toString(),
						StatsCPUSampleFields.GRANULARITY.toString()
				};
		
		String[] outputFields = 
				new String[] {
						StatsCPUSampleFields.PK_ID.toString(),
						StatsCPUSampleFields.TIME.toString(), 
						StatsCPUSampleFields.FK_ID_SIGNATURE.toString(),
						StatsCPUSampleFields.FK_ID_PARENT.toString(),
						StatsCPUSampleFields.COUNT.toString(),
						StatsCPUSampleFields.MIN.toString(),
						StatsCPUSampleFields.AVG.toString(),
						StatsCPUSampleFields.MAX.toString(),
						StatsCPUSampleFields.GRANULARITY.toString()	
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
	
	public CPUSample id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public Timestamp time() {
		return time.getValue();
	}
	
	public CPUSample time(Timestamp time) {
		this.time.setValue(time);
		return this;
	}
	
	public int foreignKeySignature() {
		return foreignKeySignature.getValue();
	}
	
	public CPUSample foreignKeySignature(Integer foreignKeySignature) {
		this.foreignKeySignature.setValue(foreignKeySignature);
		return this;
	}
	
	public int foreignKeyParent() {
		return foreignKeyParent.getValue();
	}
	
	public CPUSample foreignKeyParent(Integer foreignKeyParent) {
		this.foreignKeyParent.setValue(foreignKeyParent);
		return this;
	}

	public int count() {
		return count.getValue();
	}
	
	public CPUSample count(int count) {
		this.count.setValue(count);
		return this;
	}
	
	public CPUSample increaseCount() {
		this.count.setValue(count.getValue()+1);
		return this;
	}
	
	//
	public CPUSample prepareStatistics(int collectionIntervalSeconds) {
		int computedCount = count.getValue() * collectionIntervalSeconds;
		this.count.setValue(computedCount);
		this.min.setValue(computedCount);
		this.max.setValue(computedCount);
		this.avg.setValue(computedCount);
		return this;
	}
	public int granularity() {
		return granularity.getValue();
	}
	
	public CPUSample granularity(int granularity) {
		this.granularity.setValue(granularity);
		return this;
	}
	
}

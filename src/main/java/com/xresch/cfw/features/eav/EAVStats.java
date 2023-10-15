package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class EAVStats extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_EAV_STATS";
	
	private static final ArrayList<BigDecimal> valuesArray = new ArrayList<>();
	
	enum EAVStatsFields{
		PK_ID, 
		FK_ID_ENTITY,
		/* Values holds an array of the values */
		FK_ID_VALUES,
		FK_ID_DATE,
		TIME,
		COUNT,
		MIN,
		AVG,
		MAX,
		SUM,
		GRANULARITY,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, EAVStatsFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the statistic.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeyEntity = CFWField.newInteger(FormFieldType.HIDDEN, EAVStatsFields.FK_ID_ENTITY)
			.setForeignKeyCascade(this, EAVEntity.class, EAVEntityFields.PK_ID)
			.setDescription("The id of the entity.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<ArrayList<Number>> foreignKeyValues = CFWField.newArrayNumber(FormFieldType.HIDDEN, EAVStatsFields.FK_ID_VALUES)
			.setDescription("Array of the ids of values that are related to the statistic.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Timestamp> time = CFWField.newTimestamp(FormFieldType.NONE, EAVStatsFields.TIME)
			.setDescription("The date and time of when the statistic was written to the database.")
			.setValue(new Timestamp(new Date().getTime()));
	

	private CFWField<Integer> count = CFWField.newInteger(FormFieldType.NONE, EAVStatsFields.COUNT)
			.setDescription("The count of values.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(0);
	
	private CFWField<BigDecimal> min = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.MIN)
			.setDescription("The minimun value.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<BigDecimal> avg = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.AVG)
			.setDescription("The average value.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<BigDecimal> max = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.MAX)
			.setDescription("The maximum value.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<BigDecimal> sum = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.SUM)
			.setDescription("The sum of the values.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<Integer> granularity = CFWField.newInteger(FormFieldType.NONE, EAVStatsFields.GRANULARITY)
			.setDescription("The granularity in minutes represented by this statistics.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(0);
	
	public EAVStats() {
		initializeFields();
	}
	
	public EAVStats(int entityID, TreeSet<Integer> valueIDs) {
		initializeFields();
		this.foreignKeyEntity(entityID);
		this.foreignKeyValues(valueIDs);
	}
	
	public EAVStats(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyEntity, foreignKeyValues, time, count, min, avg, max, sum, granularity);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						EAVStatsFields.PK_ID.toString(), 
						EAVStatsFields.FK_ID_ENTITY.toString(),
						EAVStatsFields.FK_ID_VALUES.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						EAVStatsFields.PK_ID.toString(), 
						EAVStatsFields.FK_ID_ENTITY.toString(),
						EAVStatsFields.FK_ID_VALUES.toString(),
						EAVStatsFields.TIME.toString(),
						EAVStatsFields.COUNT.toString(),
						EAVStatsFields.MIN.toString(),
						EAVStatsFields.AVG.toString(),
						EAVStatsFields.MAX.toString(),
						EAVStatsFields.GRANULARITY.toString(),
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
	
	public EAVStats foreignKeyEntity(String value) {
		return foreignKeyEntity(value);
	}	
	public EAVStats foreignKeyEntity(Integer value) {
		this.foreignKeyEntity.setValue(value);
		return this;
	}	
	
	public ArrayList<Number> foreignKeyValues() {
		return foreignKeyValues.getValue();
	}
	
	public EAVStats foreignKeyAttribute(String value) {
		return foreignKeyAttribute(value);
	}
	
	public EAVStats foreignKeyValues(TreeSet<Integer> value) {
		
		this.foreignKeyValues.setValue(new ArrayList<>(value));
		return this;
	}	
			
	
	/****************************************************************
	 * Adds a value to this statistic.
	 ****************************************************************/
	public EAVStats addValue(BigDecimal number) {
		valuesArray.add(number);
		return this;
	}
	
	/****************************************************************
	 * Calculates the statistical values for putting it in the database.
	 ****************************************************************/
	public EAVStats calculateStatistics(int collectionIntervalMillis) {
		
		if(valuesArray.isEmpty()) {
			this.count.setValue(0);
			this.min.setValue(BigDecimal.ZERO);
			this.avg.setValue(BigDecimal.ZERO);
			this.max.setValue(BigDecimal.ZERO);
			this.sum.setValue(BigDecimal.ZERO);
			return this;
		}
		
		
		int count = valuesArray.size();
		BigDecimal min = valuesArray.get(0);
		BigDecimal max = valuesArray.get(0);
		BigDecimal sum = BigDecimal.ZERO;
		
		for(BigDecimal current : valuesArray) {
			sum = sum.add(current);
			if(current.compareTo(min) < 0) { min = current; }
			if(current.compareTo(max) > 0) { max = current; }
		}
		
		// Normalize count to 1 second, round up to one if zero
		this.count.setValue(count);
		this.min.setValue(min);
		this.max.setValue(max);
		this.avg.setValue(sum.divide(new BigDecimal(count)));
		this.sum.setValue(sum);
		
		return this;
	}
	
	public int granularity() {
		return granularity.getValue();
	}
	
	public EAVStats granularity(int granularity) {
		this.granularity.setValue(granularity);
		return this;
	}
	

}
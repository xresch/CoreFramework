package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.datetime.CFWDate;
import com.xresch.cfw.features.datetime.CFWDate.CFWDateFields;
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;
import com.xresch.cfw.features.eav.api.APIEAVPushStats;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class EAVStats extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_EAV_STATS";
	
	private final ArrayList<BigDecimal> valuesArray = new ArrayList<>();
	
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
		VAL,
		P50,
		P95,
		GRANULARITY,
	}
	
	enum EAVStatsType{
		COUNTER, VALUES, CUSTOM
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
	
	private CFWField<Integer> foreignKeyDate = CFWField.newInteger(FormFieldType.HIDDEN, EAVStatsFields.FK_ID_DATE)
			.setForeignKeyCascade(this, CFWDate.class, CFWDateFields.PK_ID)
			.setDescription("The id of the date.")
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

	private CFWField<BigDecimal> val = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.VAL)
			.setDescription("A normalized value giving a value per minute. Calculated using SUM divided by GRANULARITY.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<BigDecimal> p50 = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.P50)
			.setColumnDefinition("NUMERIC(64, 6) DEFAULT 0")
			.setDescription("The 50th percentile of the values.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<BigDecimal> p95 = CFWField.newBigDecimal(FormFieldType.NONE, EAVStatsFields.P95)
			.setColumnDefinition("NUMERIC(64, 6) DEFAULT 0")
			.setDescription("The 95th percentile of the values.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(BigDecimal.ZERO);
	
	private CFWField<Integer> granularity = CFWField.newInteger(FormFieldType.NONE, EAVStatsFields.GRANULARITY)
			.setDescription("The granularity in minutes represented by this statistics.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(0);
	
	private EAVStatsType type = EAVStatsType.VALUES;
	private int counter = 0;
	
	public EAVStats() {
		initializeFields();
	}
	
	public EAVStats(int entityID, TreeSet<Integer> valueIDs, EAVStatsType type) {
		initializeFields();
		this.foreignKeyEntity(entityID);
		this.foreignKeyValues(valueIDs);
		this.type = type;
	}
	
	public EAVStats(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyEntity, foreignKeyValues, foreignKeyDate, time, count, min, avg, max, sum, val, p50, p95, granularity);
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
						EAVStatsFields.GRANULARITY.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						EAVStatsFields.PK_ID.toString(), 
						EAVStatsFields.FK_ID_ENTITY.toString(),
						EAVStatsFields.FK_ID_VALUES.toString(),
						EAVStatsFields.FK_ID_DATE.toString(),
						EAVStatsFields.TIME.toString(),
						EAVStatsFields.COUNT.toString(),
						EAVStatsFields.VAL.toString(),
						EAVStatsFields.MIN.toString(),
						EAVStatsFields.AVG.toString(),
						EAVStatsFields.MAX.toString(),
						EAVStatsFields.SUM.toString(),
						EAVStatsFields.P50.toString(),
						EAVStatsFields.P95.toString(),
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
		apis.add(new APIEAVPushStats(this.getClass().getSimpleName(), "pushStats"));
		
		return apis;
	}
	
	
	
	public Integer id() {
		return id.getValue();
	}
	
	public Timestamp time() {
		return time.getValue();
	}
	
	
	public EAVStats time(long millis) {
		time.setValue(new Timestamp(millis));
		foreignKeyDate.setValue(CFWDate.newDate(millis).id());
		return this;
	}

	public EAVStatsType type() {
		return type;
	}
	
	public EAVStats type(EAVStatsType value) {
		type = value;
		return this;
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
		
	public EAVStats foreignKeyValues(TreeSet<Integer> value) {
		
		this.foreignKeyValues.setValue(new ArrayList<>(value));
		return this;
	}	
	
	public Integer foreignKeyDate() {
		return foreignKeyDate.getValue();
	}
	
	public int granularity() {
		return granularity.getValue();
	}

	public EAVStats granularity(int granularity) {
		this.granularity.setValue(granularity);
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
	 * Adds a value to this statistic.
	 ****************************************************************/
	public EAVStats increaseCounter(int increaseBy) {
		counter += increaseBy;
		return this;
	}
	
	/****************************************************************
	 * Sanitizes values and replaces nulls with zeros for putting it in the database.
	 ****************************************************************/
	public EAVStats sanitizeValues() {

		if(this.count.getValue() == null) { this.count.setValue(0); }
		if(this.min.getValue() == null) { this.min.setValue(BigDecimal.ZERO); }
		if(this.avg.getValue() == null) { this.avg.setValue(BigDecimal.ZERO); }
		if(this.max.getValue() == null) { this.max.setValue(BigDecimal.ZERO); }
		if(this.sum.getValue() == null) { this.sum.setValue(BigDecimal.ZERO); }
		if(this.val.getValue() == null) { this.val.setValue(BigDecimal.ZERO); }
		if(this.p50.getValue() == null) { this.p50.setValue(BigDecimal.ZERO); }
		if(this.p95.getValue() == null) { this.p95.setValue(BigDecimal.ZERO); }
		
		return this;
	}
	
	/****************************************************************
	 * Calculates the statistical values for putting it in the database.
	 ****************************************************************/
	public EAVStats setStatistics(
			  int count
			, BigDecimal min
			, BigDecimal avg
			, BigDecimal max
			, BigDecimal sum
			, BigDecimal p50
			, BigDecimal p95
		) {
		
		this.count.setValue(count);
		this.min.setValue(min);
		this.avg.setValue(avg);
		this.max.setValue(max);
		this.sum.setValue(sum);
		this.p50.setValue(p50);
		this.p95.setValue(p95);
		
		return this;
		
	}
	
	/****************************************************************
	 * Calculates the statistical values for putting it in the database.
	 ****************************************************************/
	public EAVStats addStatistics(
			  int count
			, BigDecimal min
			, BigDecimal avg
			, BigDecimal max
			, BigDecimal sum
			, BigDecimal p50
			, BigDecimal p95
			) {
		
		//-----------------------------------
		// Count
		Integer currentCount = this.count.getValue();
		int sureNumber = ( currentCount != null) ? currentCount : 0;
		this.count.setValue(sureNumber + count);
		
		//-----------------------------------
		// Min
		if(min != null) { 
			BigDecimal currentMin = this.min.getValue();
			if(currentMin != null) {
				if(min.compareTo(currentMin) < 0) { this.min.setValue(min); }
			}else {
				this.min.setValue(min);
			}
		}
		
		//-----------------------------------
		// Max
		if(max != null) { 
			BigDecimal currentMax = this.max.getValue();
			if(currentMax != null) {
				if(max.compareTo(currentMax) > 0) { this.max.setValue(max); }
			}else {
				this.max.setValue(max);
			}
		}
		
		//-----------------------------------
		// Sum
		boolean sumIsNotNull = (this.sum.getValue() != null);
		if(sum != null) { 
			if(sumIsNotNull) {
				BigDecimal currentSum = this.sum.getValue();
				this.sum.setValue(currentSum.add(sum));
			}else {
				this.sum.setValue(sum);
				sumIsNotNull = true;
			}
		}
		
		//-----------------------------------
		// Avg
		if(sumIsNotNull 
		&& this.count.getValue() != null
		&& this.count.getValue() > 0
		) {
			BigDecimal elCount = new BigDecimal(this.count.getValue());
			this.avg.setValue(sum.divide(elCount, RoundingMode.HALF_UP));
		}else {
			
			if(avg != null) { 
				BigDecimal currentAvg = this.avg.getValue();
				if(currentAvg != null) {
					BigDecimal diff = currentAvg.subtract(avg);
					BigDecimal halfDiff = diff.divide(CFW.Math.BIGDEC_TWO, RoundingMode.HALF_UP);
					this.avg.setValue( currentAvg.subtract(halfDiff) );
				}else {
					this.avg.setValue(avg);
				}
			}
		}

		//-----------------------------------
		// P50
		if(p50 != null) { 
			BigDecimal currentP50 = this.p50.getValue();
			if(currentP50 != null) {
				BigDecimal newP50 = CFW.Math.bigPercentile(50, p50, currentP50);
				this.p50.setValue(newP50);
			}else {
				this.p50.setValue(p50);
			}
		}
		
		
		//-----------------------------------
		// P95
		if(p95 != null) { 
			BigDecimal currentP95 = this.p95.getValue();
			if(currentP95 != null) {
				BigDecimal newP95 = CFW.Math.bigPercentile(95, p95, currentP95);
				this.p95.setValue(newP95);
			}else {
				this.p95.setValue(p95);
			}
		}
		
		return this;
		
	}

	/****************************************************************
	 * Calculates the statistical values for putting it in the database.
	 ****************************************************************/
	public EAVStats calculateStatistics() {
		
		BigDecimal bigGranularity = new BigDecimal(this.granularity());
		//--------------------------------
		// Handle Type COUNTER
		if(this.type == EAVStatsType.COUNTER) {
			
			this.count.setValue(counter);
			BigDecimal bigCount = new BigDecimal(counter);
			this.min.setValue(bigCount);
			this.max.setValue(bigCount);
			this.avg.setValue(bigCount);
			this.sum.setValue(bigCount);
			this.val.setValue(bigCount.divide(bigGranularity, RoundingMode.HALF_UP));
			this.p50.setValue(bigCount);
			this.p95.setValue(bigCount);
			return this;
		}
		
		//--------------------------------
		// Handle Type CUSTOM
		if(this.type == EAVStatsType.CUSTOM) {
			BigDecimal sum = this.sum.getValue();
			this.val.setValue(sum.divide(bigGranularity, RoundingMode.HALF_UP));

			return this;
		}
		

		//--------------------------------
		//Handle Empty Array
		if(valuesArray.isEmpty()) {
			this.count.setValue(0);
			this.min.setValue(BigDecimal.ZERO);
			this.avg.setValue(BigDecimal.ZERO);
			this.max.setValue(BigDecimal.ZERO);
			this.sum.setValue(BigDecimal.ZERO);
			this.val.setValue(BigDecimal.ZERO);
			this.p50.setValue(BigDecimal.ZERO);
			this.p95.setValue(BigDecimal.ZERO);
			return this;
		}
		
		//--------------------------------
		// Handle Type VALUES
		int count = valuesArray.size();
		
		BigDecimal min = valuesArray.get(0);
		BigDecimal max = valuesArray.get(0);
		BigDecimal sum = BigDecimal.ZERO;
		
		for(BigDecimal current : valuesArray) {
			sum = sum.add(current);
			if(current.compareTo(min) < 0) { min = current; }
			if(current.compareTo(max) > 0) { max = current; }
		}
		
		this.count.setValue(count);
		this.min.setValue(min);
		this.max.setValue(max);
		this.avg.setValue(sum.divide(new BigDecimal(count), RoundingMode.HALF_UP));
		this.sum.setValue(sum);
		this.val.setValue(sum.divide(bigGranularity, RoundingMode.HALF_UP));
		this.p50.setValue(CFW.Math.bigPercentile(50, valuesArray));
		this.p95.setValue(CFW.Math.bigPercentile(95, valuesArray));
		
		return this;
	}
	

}

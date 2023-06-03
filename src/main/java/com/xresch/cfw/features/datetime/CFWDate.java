package com.xresch.cfw.features.datetime;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.CFWCacheManagement;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDate extends CFWObject {
	
	private static final Logger logger = CFWLog.getLogger(CFWDate.class.getName());
	
	public static String TABLE_NAME = "CFW_DATE";
	public static String TABLE_NAME_OLD = "OM_DATAPOINTS_DATE";
	
	public enum OMDatapointDateFields{
		  PK_ID
		, DATE
		, DATE_ISO
		, EPOCH_MILLIS
		, YEAR
		, MONTH
		, DAY
		, DAY_OF_WEEK
		, DAY_NAME
		, DAY_NAME_SHORT
		, DAY_OF_YEAR
		, WEEK_OF_YEAR
		, WEEK_OF_MONTH
		, QUARTER_OF_YEAR
		, IS_WEEKEND
		, IS_MONTHS_LAST_DAY
		, IS_MONTHS_LAST_WEEK
	}
		
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, OMDatapointDateFields.PK_ID)
								   .setPrimaryKey(this)
								   .setDescription("The id of the date. Matches YYYYMMDD to keep consistent over several systems.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Date> date = CFWField.newDate(FormFieldType.DATEPICKER, OMDatapointDateFields.DATE)
			.setDescription("The actual date for this record.")
			.setValue(null);
	
	private CFWField<String> dateISO = CFWField.newString(FormFieldType.NUMBER, OMDatapointDateFields.DATE_ISO)
			.setDescription("The date as a string in ISO 8601 format YYYY-MM-DD.")
			.setValue(null);
	
	private CFWField<Long> epochMillis = CFWField.newLong(FormFieldType.NUMBER, OMDatapointDateFields.EPOCH_MILLIS)
			.setDescription("The epoch time, milliseconds since 1970-01-01 00:00:00.")
			.setValue(null);
	
	private CFWField<Integer> year = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.YEAR)
			.setDescription("The year of the date.")
			.setValue(null);
	
	private CFWField<Integer> month = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.MONTH)
			.setDescription("The month of the date.")
			.setValue(null);
	
	private CFWField<Integer> day = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.DAY)
			.setDescription("The day of the date.")
			.setValue(null);
	
	private CFWField<Integer> dayOfWeek = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.DAY_OF_WEEK)
			.setDescription("The day of the week as number.(e.g. Monday is 1, Tuesday is 2...)")
			.setValue(null);
	
	private CFWField<String> dayName = CFWField.newString(FormFieldType.TEXT, OMDatapointDateFields.DAY_NAME)
			.setDescription("The name of the day(e.g Monday, Tuesday ...)")
			.setValue(null);
	
	private CFWField<String> dayNameShort = CFWField.newString(FormFieldType.TEXT, OMDatapointDateFields.DAY_NAME_SHORT)
			.setDescription("The short name of the day(e.g Mon, Tue ...)")
			.setValue(null);
	
	private CFWField<Integer> dayOfYear = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.DAY_OF_YEAR)
			.setDescription("The day of the year. ")
			.setValue(null);
	
	private CFWField<Integer> weekOfYear = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.WEEK_OF_YEAR)
			.setDescription("The week of the year. ")
			.setValue(null);
	
	private CFWField<Integer> weekOfMonth = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.WEEK_OF_MONTH)
			.setDescription("The week of the month. (e.g. January is 1, February is 2...)")
			.setValue(null);
	
	private CFWField<Integer> quarterOfYear = CFWField.newInteger(FormFieldType.NUMBER, OMDatapointDateFields.QUARTER_OF_YEAR)
			.setDescription("The quarter of the year. (e.g. Jan-Mar is 1, Apr-Jun is 2 ...)")
			.setValue(null);
	
	private CFWField<Boolean> isWeekend = CFWField.newBoolean(FormFieldType.BOOLEAN, OMDatapointDateFields.IS_WEEKEND)
			.setDescription("True if the day is a Saturday or Sunday, false otherwise.")
			.setValue(null);
	
	private CFWField<Boolean> isMonthsLastDay = CFWField.newBoolean(FormFieldType.BOOLEAN, OMDatapointDateFields.IS_MONTHS_LAST_DAY)
			.setDescription("True if the day is the last day of the month, false otherwise.")
			.setValue(null);
	
	private CFWField<Boolean> isMonthsLastWeek = CFWField.newBoolean(FormFieldType.BOOLEAN, OMDatapointDateFields.IS_MONTHS_LAST_WEEK)
			.setDescription("True if the week is the last week of the month, false otherwise.")
			.setValue(null);
		
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		
		this.addFields( 
				  id
				, date
				, dateISO
				, epochMillis
				, year
				, month
				, day
				, dayName
				, dayNameShort
				, dayOfWeek
				, dayOfYear
				, weekOfYear
				, weekOfMonth
				, quarterOfYear
				, isWeekend
				, isMonthsLastDay
				, isMonthsLastWeek
			);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public CFWDate() {
		initializeFields();
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public CFWDate(ZonedDateTime date) {
		initializeFields();
		this.initializeData(date);
	}

	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void migrateTable() {
		new CFWLog(logger).off("Migration: renaming table "+TABLE_NAME_OLD+" to "+TABLE_NAME);
		new CFWSQL(this).renameTable(TABLE_NAME_OLD, TABLE_NAME);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private void initializeData(ZonedDateTime input) {
		
		//calendar.setFirstDayOfWeek(Calendar.MONDAY);
		//----------------------------------------------
		// Set Year, Month, Day 
		int year = input.getYear();
		int month = input.getMonthValue();
		int day = input.getDayOfMonth();
		
		this.dateISO.setValue( CFW.Time.formatDateAsISO(input) );
		this.year.setValue(input.getYear());
		this.month.setValue(input.getMonthValue());
		this.day.setValue(input.getDayOfMonth());
		
		//----------------------------------------------
		// Create and Set ID
		// Create id that corresponds to YYYYMMDD
		int id = (year * 10000) + (month * 100) + day;
			
		this.id.setValue(id);
		
		//----------------------------------------------
		// Date and Millis

		this.date.setValue(	new java.sql.Date(input.toEpochSecond()*1000) );
		this.epochMillis.setValue(	input.toEpochSecond()*1000);

		//----------------------------------------------
		// Week Day Related Fields
		DayOfWeek mondayIsOne = input.getDayOfWeek();
		
		switch(mondayIsOne) {
			case MONDAY: 
				this.dayName.setValue("Monday");
				this.dayNameShort.setValue("Mon");
				this.isWeekend.setValue(false);	
				this.dayOfWeek.setValue(1);
			break;
			
			case TUESDAY: 
				this.dayName.setValue("Tuesday");
				this.dayNameShort.setValue("Tue");
				this.isWeekend.setValue(false);	
				this.dayOfWeek.setValue(2);
			break;
			
			case WEDNESDAY: 
				this.dayName.setValue("Wednesday");
				this.dayNameShort.setValue("Wed");
				this.isWeekend.setValue(false);	
				this.dayOfWeek.setValue(3);
			break;
			
			case THURSDAY: 
				this.dayName.setValue("Thursday");
				this.dayNameShort.setValue("Thu");
				this.isWeekend.setValue(false);	
				this.dayOfWeek.setValue(4);
			break;
			
			case FRIDAY: 
				this.dayName.setValue("Friday");
				this.dayNameShort.setValue("Fri");
				this.isWeekend.setValue(false);	
				this.dayOfWeek.setValue(5);
			break;
			
			case SATURDAY: 
				this.dayName.setValue("Saturday");
				this.dayNameShort.setValue("Sat");
				this.isWeekend.setValue(true);	
				this.dayOfWeek.setValue(6);
			break;
			
			case SUNDAY: 
				this.dayName.setValue("Sunday");
				this.dayNameShort.setValue("Sun");
				this.isWeekend.setValue(true);	
				this.dayOfWeek.setValue(7);
			break;
		}
		
		//----------------------------------------------
		// OtherFields
		this.dayOfYear.setValue(	input.getDayOfYear());
		this.weekOfYear.setValue(	input.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) );
		
		int weekOfMonth = input.get(ChronoField.ALIGNED_WEEK_OF_MONTH) ;
		this.weekOfMonth.setValue(weekOfMonth);
		
		this.quarterOfYear.setValue(input.get(IsoFields.QUARTER_OF_YEAR));
		
		int maxDaysInMonth = input.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
		this.isMonthsLastDay.setValue(day == maxDaysInMonth);
		
		int maxWeeksInMonth = input.with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.ALIGNED_WEEK_OF_MONTH);
		this.isMonthsLastWeek.setValue(weekOfMonth == maxWeeksInMonth);
	
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
						OMDatapointDateFields.PK_ID.toString(), 
						OMDatapointDateFields.DATE.toString(), 
						OMDatapointDateFields.YEAR.toString(), 
						OMDatapointDateFields.EPOCH_MILLIS.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						OMDatapointDateFields.PK_ID.toString(), 
						OMDatapointDateFields.DATE.toString(), 
						OMDatapointDateFields.YEAR.toString(), 
						OMDatapointDateFields.EPOCH_MILLIS.toString(),
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
		
	public Date date() {
		return date.getValue();
	}
	
	public String dateISO() {
		return dateISO.getValue();
	}
	
	public Long epochMillis() {
		return epochMillis.getValue();
	}	
	
	public Integer year() {
		return year.getValue();
	}
	
	public Integer month() {
		return month.getValue();
	}
	
	public Integer day() {
		return day.getValue();
	}
	
	public Integer dayOfWeek() {
		return dayOfWeek.getValue();
	}
	
	public String dayName() {
		return dayName.getValue();
	}
	
	public String dayNameShort() {
		return dayNameShort.getValue();
	}
	
	public Integer dayOfYear() {
		return dayOfYear.getValue();
	}
	
	public Integer weekOfYear() {
		return weekOfYear.getValue();
	}
	
	public Integer weekOfMonth() {
		return weekOfMonth.getValue();
	}
	
	public boolean isWeekend() {
		return isWeekend.getValue();
	}
	
	public boolean isMonthsLastDay() {
		return isMonthsLastDay.getValue();
	}
	
	public boolean isMonthsLastWeek() {
		return isMonthsLastWeek.getValue();
	}
	

		
}

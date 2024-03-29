package com.xresch.cfw.features.datetime;

import java.sql.Date;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * Class for handling dates in memory and in the database for statistical purposes.
 * This class does not handle hours and any lower time units.
 * Use the methods "newDate()" to get a new Date. 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWDate extends CFWObject {
	
	private static final Logger logger = CFWLog.getLogger(CFWDate.class.getName());
	
	public static String TABLE_NAME = "CFW_DATE";
	public static String TABLE_NAME_OLD = "OM_DATAPOINTS_DATE";
	
	// dateID and Instance
	public static LinkedHashMap<Integer,CFWDate> dateCache = new LinkedHashMap<>();
	
	public enum CFWDateFields{
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
		
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWDateFields.PK_ID)
								   .setPrimaryKey(this)
								   .setDescription("The id of the date. Matches YYYYMMDD to keep consistent over several systems.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Date> date = CFWField.newDate(FormFieldType.DATEPICKER, CFWDateFields.DATE)
			.setDescription("The actual date for this record.")
			.setValue(null);
	
	private CFWField<String> dateISO = CFWField.newString(FormFieldType.NUMBER, CFWDateFields.DATE_ISO)
			.setDescription("The date as a string in ISO 8601 format YYYY-MM-DD.")
			.setValue(null);
	
	private CFWField<Long> epochMillis = CFWField.newLong(FormFieldType.NUMBER, CFWDateFields.EPOCH_MILLIS)
			.setDescription("The epoch time, milliseconds since 1970-01-01 00:00:00.")
			.setValue(null);
	
	private CFWField<Integer> year = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.YEAR)
			.setDescription("The year of the date.")
			.setValue(null);
	
	private CFWField<Integer> month = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.MONTH)
			.setDescription("The month of the date.")
			.setValue(null);
	
	private CFWField<Integer> day = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.DAY)
			.setDescription("The day of the date.")
			.setValue(null);
	
	private CFWField<Integer> dayOfWeek = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.DAY_OF_WEEK)
			.setDescription("The day of the week as number.(e.g. Monday is 1, Tuesday is 2...)")
			.setValue(null);
	
	private CFWField<String> dayName = CFWField.newString(FormFieldType.TEXT, CFWDateFields.DAY_NAME)
			.setDescription("The name of the day(e.g Monday, Tuesday ...)")
			.setValue(null);
	
	private CFWField<String> dayNameShort = CFWField.newString(FormFieldType.TEXT, CFWDateFields.DAY_NAME_SHORT)
			.setDescription("The short name of the day(e.g Mon, Tue ...)")
			.setValue(null);
	
	private CFWField<Integer> dayOfYear = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.DAY_OF_YEAR)
			.setDescription("The day of the year. ")
			.setValue(null);
	
	private CFWField<Integer> weekOfYear = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.WEEK_OF_YEAR)
			.setDescription("The week of the year. ")
			.setValue(null);
	
	private CFWField<Integer> weekOfMonth = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.WEEK_OF_MONTH)
			.setDescription("The week of the month. (e.g. January is 1, February is 2...)")
			.setValue(null);
	
	private CFWField<Integer> quarterOfYear = CFWField.newInteger(FormFieldType.NUMBER, CFWDateFields.QUARTER_OF_YEAR)
			.setDescription("The quarter of the year. (e.g. Jan-Mar is 1, Apr-Jun is 2 ...)")
			.setValue(null);
	
	private CFWField<Boolean> isWeekend = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWDateFields.IS_WEEKEND)
			.setDescription("True if the day is a Saturday or Sunday, false otherwise.")
			.setValue(null);
	
	private CFWField<Boolean> isMonthsLastDay = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWDateFields.IS_MONTHS_LAST_DAY)
			.setDescription("True if the day is the last day of the month, false otherwise.")
			.setValue(null);
	
	private CFWField<Boolean> isMonthsLastWeek = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWDateFields.IS_MONTHS_LAST_WEEK)
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
	 * Please use the methods newDate() to create you date instance.
	 * This constructor is for internal purposes only, like creating the database table.
	 **************************************************************************************/
	public CFWDate() {
		initializeFields();
	}
	
	/**************************************************************************************
	 * @param millis epoch milliseconds
	 **************************************************************************************/
	public static CFWDate newDate(long millis) {
		return newDate(CFW.Time.zonedTimeFromEpoch(millis));
	}
	
	/**************************************************************************************
	 * Returns an instance of CFWDate, either new instance or from cache.
	 * Calling this method ensures that the date with the specified ID is in the database.
	 * 
	 **************************************************************************************/
	public static CFWDate newDate(ZonedDateTime date) {
		return CFWDate.getInstance(date);
	}
	

	/**************************************************************************************
	 * @param dateID integer as "YYYYMMDD"
	 **************************************************************************************/
	public static CFWDate newDate(int dateID) {
		if(dateCache.containsKey(dateID)) {
			return dateCache.get(dateID);
		}
		return newDate(dateID+"");
	}
	
	/**************************************************************************************
	 * @param dateID string as "YYYYMMDD"
	 **************************************************************************************/
	public static CFWDate newDate(String dateID) {

		try {
			//----------------------------------
			// Check exists in Cache
			int id = Integer.parseInt(dateID);
			if(dateCache.containsKey(id)) {
				return dateCache.get(id);
			}
			
			//----------------------------------
			// Create Date
			long millis =  CFW.Time.parseTime("yyyyMMdd", dateID);
			ZonedDateTime date = CFW.Time.zonedTimeFromEpoch(millis);
			
			return CFWDate.getInstance(date);
			
		} catch (ParseException e) {
			new CFWLog(logger).severe(e);
		}
		
		return null;
	}

	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void migrateTable() {
		new CFWSQL(this).renameTable(TABLE_NAME_OLD, TABLE_NAME);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private static CFWDate getInstance(ZonedDateTime input) {
		
		CFWDate cfwDate = new CFWDate();
		
		//calendar.setFirstDayOfWeek(Calendar.MONDAY);

		//----------------------------------------------
		// Create DateID
		int year = input.getYear();
		int month = input.getMonthValue();
		int day = input.getDayOfMonth();
		int dateID = (year * 10000) + (month * 100) + day;
		
		//----------------------------------------------
		// Check Cache
		if(dateCache.containsKey(dateID)) {
			return dateCache.get(dateID);
		}
		
		//----------------------------------------------
		// Set Year, Month, Day 
		cfwDate.dateISO.setValue( CFW.Time.formatDateAsISO(input) );
		cfwDate.year.setValue(input.getYear());
		cfwDate.month.setValue(input.getMonthValue());
		cfwDate.day.setValue(input.getDayOfMonth());
		
		//----------------------------------------------
		// Create and Set ID
		// Create id that corresponds to YYYYMMDD
		cfwDate.id.setValue(dateID);
		
		//----------------------------------------------
		// Date and Millis

		cfwDate.date.setValue(	new java.sql.Date(input.toEpochSecond()*1000) );
		cfwDate.epochMillis.setValue(	input.toEpochSecond()*1000);

		//----------------------------------------------
		// Week Day Related Fields
		DayOfWeek mondayIsOne = input.getDayOfWeek();
		
		switch(mondayIsOne) {
			case MONDAY: 
				cfwDate.dayName.setValue("Monday");
				cfwDate.dayNameShort.setValue("Mon");
				cfwDate.isWeekend.setValue(false);	
				cfwDate.dayOfWeek.setValue(1);
			break;
			
			case TUESDAY: 
				cfwDate.dayName.setValue("Tuesday");
				cfwDate.dayNameShort.setValue("Tue");
				cfwDate.isWeekend.setValue(false);	
				cfwDate.dayOfWeek.setValue(2);
			break;
			
			case WEDNESDAY: 
				cfwDate.dayName.setValue("Wednesday");
				cfwDate.dayNameShort.setValue("Wed");
				cfwDate.isWeekend.setValue(false);	
				cfwDate.dayOfWeek.setValue(3);
			break;
			
			case THURSDAY: 
				cfwDate.dayName.setValue("Thursday");
				cfwDate.dayNameShort.setValue("Thu");
				cfwDate.isWeekend.setValue(false);	
				cfwDate.dayOfWeek.setValue(4);
			break;
			
			case FRIDAY: 
				cfwDate.dayName.setValue("Friday");
				cfwDate.dayNameShort.setValue("Fri");
				cfwDate.isWeekend.setValue(false);	
				cfwDate.dayOfWeek.setValue(5);
			break;
			
			case SATURDAY: 
				cfwDate.dayName.setValue("Saturday");
				cfwDate.dayNameShort.setValue("Sat");
				cfwDate.isWeekend.setValue(true);	
				cfwDate.dayOfWeek.setValue(6);
			break;
			
			case SUNDAY: 
				cfwDate.dayName.setValue("Sunday");
				cfwDate.dayNameShort.setValue("Sun");
				cfwDate.isWeekend.setValue(true);	
				cfwDate.dayOfWeek.setValue(7);
			break;
		}
		
		//----------------------------------------------
		// OtherFields
		cfwDate.dayOfYear.setValue(	input.getDayOfYear());
		cfwDate.weekOfYear.setValue(	input.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) );
		
		int weekOfMonth = input.get(ChronoField.ALIGNED_WEEK_OF_MONTH) ;
		cfwDate.weekOfMonth.setValue(weekOfMonth);
		
		cfwDate.quarterOfYear.setValue(input.get(IsoFields.QUARTER_OF_YEAR));
		
		int maxDaysInMonth = input.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
		cfwDate.isMonthsLastDay.setValue(day == maxDaysInMonth);
		
		int maxWeeksInMonth = input.with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.ALIGNED_WEEK_OF_MONTH);
		cfwDate.isMonthsLastWeek.setValue(weekOfMonth == maxWeeksInMonth);
	
		//----------------------------------------------
		// Create DB if not Exists
		
		if(!CFW.DB.Date.checkExistsByID(dateID)) {
			CFW.DB.Date.oneTimeCreate(cfwDate);
		}
		
		//----------------------------------------------
		// Add to cache
		dateCache.put(cfwDate.id(), cfwDate);
		return cfwDate;
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
						CFWDateFields.PK_ID.toString(), 
						CFWDateFields.DATE.toString(), 
						CFWDateFields.YEAR.toString(), 
						CFWDateFields.EPOCH_MILLIS.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWDateFields.PK_ID.toString(), 
						CFWDateFields.DATE.toString(), 
						CFWDateFields.YEAR.toString(), 
						CFWDateFields.EPOCH_MILLIS.toString(),
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

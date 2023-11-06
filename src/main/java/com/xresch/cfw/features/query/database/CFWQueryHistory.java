package com.xresch.cfw.features.query.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.EmailValidator;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWQueryHistory extends CFWObject {
	
	public static String TABLE_NAME = "CFW_QUERY_HISTORY";
	
	public enum CFWQueryHistoryFields{
		PK_ID, 
		FK_ID_USER,
		TIME, 
		EARLIEST, 
		LATEST, 
		QUERY, 
		JSON_PARAMS,
	}
	
	private static ArrayList<String> fullTextSearchColumns = new ArrayList<>();
	static {
		fullTextSearchColumns.add(CFWQueryHistoryFields.QUERY.toString());
		fullTextSearchColumns.add(CFWQueryHistoryFields.JSON_PARAMS.toString());
	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWQueryHistoryFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The id of the history entry.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWQueryHistoryFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Timestamp> time = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, CFWQueryHistoryFields.TIME)
				.setDescription("The execution time of the query.")
				.setValue(new Timestamp(System.currentTimeMillis()))
				;
	
	private CFWField<Timestamp> earliest = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, CFWQueryHistoryFields.EARLIEST)
			.setDescription("The earliest time of the selected time range.")
			.setValue(null)
			;
	
	private CFWField<Timestamp> latest = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, CFWQueryHistoryFields.LATEST)
			.setDescription("The latest time of the selected time range.")
			.setValue(null)
			;
	
	private CFWField<String> query = CFWField.newString(FormFieldType.TEXT, CFWQueryHistoryFields.QUERY)
			.setDescription("The executed query.")
			;
		
	private CFWField<String> params = CFWField.newString(FormFieldType.TEXT, CFWQueryHistoryFields.JSON_PARAMS)
			.setLabel("Parameters")
			.setDescription("The parameters used for the execution.")
			;
		
					
	public CFWQueryHistory() {
		initializeFields();
	}
		
	public CFWQueryHistory(int userid, CFWQueryContext context) {

		this.initializeFields();
		
		this.foreignKeyUser(userid);
		this.earliest(context.getEarliestMillis());
		this.latest(context.getLatestMillis());
		this.query(context.getFullQueryString());
		this.params( CFW.JSON.toJSON(context.getParameters()) );
	}

	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.enableFulltextSearch(fullTextSearchColumns);
		
		this.addFields(
				id 
				, foreignKeyUser 
				, time 
				, earliest
				, latest
				, query
				, params
				);
	}
	
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						CFWQueryHistoryFields.PK_ID.toString(), 
						CFWQueryHistoryFields.FK_ID_USER.toString(), 
						CFWQueryHistoryFields.QUERY.toString(),
						CFWQueryHistoryFields.JSON_PARAMS.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWQueryHistoryFields.PK_ID.toString(), 
						CFWQueryHistoryFields.FK_ID_USER.toString(), 
						CFWQueryHistoryFields.TIME.toString(),
						CFWQueryHistoryFields.EARLIEST.toString(),
						CFWQueryHistoryFields.LATEST.toString(),
						CFWQueryHistoryFields.QUERY.toString(),
						CFWQueryHistoryFields.JSON_PARAMS.toString(),
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
	
	public CFWQueryHistory id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWQueryHistory foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}
	
	public Timestamp time() {
		return time.getValue();
	}
	
	public CFWQueryHistory time(Timestamp value) {
		this.time.setValue(value);
		return this;
	}
	
	public Timestamp earliest() {
		return earliest.getValue();
	}
	
	public CFWQueryHistory earliest(long value) {
		return this.earliest( new Timestamp(value) );
	}
	
	public CFWQueryHistory earliest(Timestamp value) {
		this.earliest.setValue(value);
		return this;
	}
	
	public Timestamp latest() {
		return latest.getValue();
	}
	
	public CFWQueryHistory latest(long value) {
		return this.latest( new Timestamp(value) );
	}
	
	public CFWQueryHistory latest(Timestamp value) {
		this.latest.setValue(value);
		return this;
	}
	

	
	public String query() {
		return query.getValue();
	}
	
	public CFWQueryHistory query(String value) {
		this.query.setValue(value);
		return this;
	}
	
	public String params() {
		return params.getValue();
	}
	
	public CFWQueryHistory params(String value) {
		this.params.setValue(value);
		return this;
	}
	
	
	
}

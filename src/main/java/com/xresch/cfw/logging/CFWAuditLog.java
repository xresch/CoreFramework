package com.xresch.cfw.logging;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * This object is registered by the class FeatureCore.
 * 
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWAuditLog extends CFWObject {
	
	public static String TABLE_NAME = "CFW_AUDITLOG";
	
	public enum CFWAuditLogFields{
		PK_ID, 
		TIMESTAMP, 
		FK_ID_USER,
		USERNAME,
		ACTION, 
		ITEM, 
		MESSAGE,		
	}
	
	public enum CFWAuditLogAction{
		CREATE, 
		READ, 
		UPDATE,
		DELETE, 
		ADD, 
		ASSIGN,
		REMOVE,
		CHANGE,
		RENAME,
		RESET,
		TRANSFER,
		TRANSFORM,
		MIGRATE,
		MOVE,
		RESIZE,
		INCREASE,
		DECREASE, 
		
	}
		
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWAuditLogFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The id of the log.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
				
	private CFWField<Timestamp> timestamp = 
			CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, CFWAuditLogFields.TIMESTAMP)
			.setValue(new Timestamp(System.currentTimeMillis()));
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWAuditLogFields.FK_ID_USER)
			.setDescription("The id of the user that executed the action.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null)
			// Will not work when table is created before CFW_UserTable exists.
			// Also not needed, prevents audit logs from deletion when user is deleted.
			//.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			;
	
	private CFWField<String> username = CFWField.newString(FormFieldType.TEXT, CFWAuditLogFields.USERNAME)
			.setDescription("The name of the user that executed the action.");
	
	private CFWField<String> action = CFWField.newString(FormFieldType.TEXT, CFWAuditLogFields.ACTION)
			.setDescription("The type of action executed by the user.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> item = CFWField.newString(FormFieldType.TEXT, CFWAuditLogFields.ITEM)
			.setDescription("The item that was changed.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> message = CFWField.newString(FormFieldType.TEXT, CFWAuditLogFields.MESSAGE)
			.setDescription("Message containing additional details about the change.")
			.addValidator(new LengthValidator(-1, 255));
					
	public CFWAuditLog() {
		initializeFields();
	}
	
	public CFWAuditLog(String username) {
		initializeFields();
	}
	
	public CFWAuditLog(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);

	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, 
				timestamp,
				foreignKeyUser,
				username,
				action, 
				item, 
				message
				);
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
						CFWAuditLogFields.PK_ID.toString(), 
						CFWAuditLogFields.TIMESTAMP.toString(),
						CFWAuditLogFields.FK_ID_USER.toString(),
						CFWAuditLogFields.USERNAME.toString(),
						CFWAuditLogFields.ACTION.toString(),
						CFWAuditLogFields.ITEM.toString(),
						CFWAuditLogFields.MESSAGE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWAuditLogFields.PK_ID.toString(), 
						CFWAuditLogFields.TIMESTAMP.toString(),
						CFWAuditLogFields.FK_ID_USER.toString(),
						CFWAuditLogFields.USERNAME.toString(),
						CFWAuditLogFields.ACTION.toString(),
						CFWAuditLogFields.ITEM.toString(),
						CFWAuditLogFields.MESSAGE.toString(),
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
	
	public CFWAuditLog id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWAuditLog foreignKeyUser(Integer foreignKeyUser) {
		this.foreignKeyUser.setValue(foreignKeyUser);
		return this;
	}
	
	public Timestamp timestamp() {
		return timestamp.getValue();
	}
	
	public CFWAuditLog timestamp(Timestamp timestamp) {
		this.timestamp.setValue(timestamp);
		return this;
	}
	
	public String action() {
		return action.getValue();
	}
		
	public CFWAuditLog action(CFWAuditLogAction action) {
		this.action.setValue(action.toString());
		return this;
	}
	
	public String item() {
		return item.getValue();
	}
	
	public CFWAuditLog item(String item) {
		this.item.setValue(item);
		return this;
	}
	
	public String message() {
		return message.getValue();
	}
	
	public CFWAuditLog message(String message) {
		this.message.setValue(message);
		return this;
	}
	
	public String username() {
		return username.getValue();
	}
	
	public CFWAuditLog username(String username) {
		this.username.setValue(username);
		return this;
	}
	
}

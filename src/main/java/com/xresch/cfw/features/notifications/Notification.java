package com.xresch.cfw.features.notifications;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class Notification extends CFWObject {
	
	public static String TABLE_NAME = "CFW_NOTIFICATION";
	
	public enum NotificationFields{
		PK_ID, 
		FK_ID_USER,
		TIMESTAMP,
		CATEGORY, 
		TITLE, 
		MESSAGE,
		MESSAGE_TYPE,
		IS_READ
	}
		
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, NotificationFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The id of the notification.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, NotificationFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the user of the notification.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Timestamp> timestamp = 
			CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, NotificationFields.TIMESTAMP)
			.setDescription("The time the notification was created.")
			.setValue(new Timestamp(System.currentTimeMillis()));
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, NotificationFields.CATEGORY)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("A custom category which can be used to filter notifications.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> title = CFWField.newString(FormFieldType.TEXT, NotificationFields.TITLE)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The title of the notification.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> message = CFWField.newString(FormFieldType.TEXTAREA, NotificationFields.MESSAGE)
			.allowHTML(true)
			.setDescription("The message of the notification, can contain HTML.")
			.addValidator(new LengthValidator(-1, 100000));
	
	private CFWField<String> messageType = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, NotificationFields.MESSAGE_TYPE)
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The type of the message.")
			.addValidator(new LengthValidator(3, 1024));
	
	private CFWField<Boolean> isRead = CFWField.newBoolean(FormFieldType.BOOLEAN, NotificationFields.IS_READ)
			.setDescription("Is read by the user or not.")
			.setValue(false);
	
	public Notification() {
		initializeFields();
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);

		this.addFields(id, 
				foreignKeyUser,
				timestamp,
				category,
				title, 
				message,
				messageType,
				isRead
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
						NotificationFields.PK_ID.toString(), 
						NotificationFields.FK_ID_USER.toString(),
						NotificationFields.TITLE.toString(),
						NotificationFields.MESSAGE_TYPE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						NotificationFields.PK_ID.toString(), 
						NotificationFields.FK_ID_USER.toString(),
						NotificationFields.TIMESTAMP.toString(),
						NotificationFields.CATEGORY.toString(),
						NotificationFields.TITLE.toString(),
						NotificationFields.MESSAGE.toString(),
						NotificationFields.MESSAGE_TYPE.toString(),
						NotificationFields.IS_READ.toString(),
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
	
	public Notification id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public Notification foreignKeyUser(Integer foreignKeyUser) {
		this.foreignKeyUser.setValue(foreignKeyUser);
		return this;
	}
	
	public Timestamp timestamp() {
		return timestamp.getValue();
	}
	
	public Notification timestamp(Timestamp value) {
		this.timestamp.setValue(value);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Notification category(String value) {
		this.category.setValue(value);
		return this;
	}
	
	public String title() {
		return title.getValue();
	}
	
	public Notification title(String value) {
		this.title.setValue(value);
		return this;
	}
		
	public String message() {
		return message.getValue();
	}

	public Notification message(String messageHTML) {
		this.message.setValue(messageHTML);
		return this;
	}
	
	public MessageType messageType() {
		if(messageType.getValue() != null) {
			return MessageType.valueOf(messageType.getValue());
		} else {
			return null;
		}
	}
	
	public Notification messageType(MessageType value) {
		this.messageType.setValue(value.toString());
		return this;
	}
			
	public boolean isRead() {
		return isRead.getValue();
	}
	
	public Notification isRead(boolean value) {
		this.isRead.setValue(value);
		return this;
	}
		
}

package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DashboardParameter extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_PARAMETER";
	

	public enum DashboardParameterMode{
		MODE_SUBSTITUTE,
		MODE_GLOBAL_OVERRIDE,
		
	}
	private static LinkedHashMap<String, String> modeOptions = new LinkedHashMap<String, String>();
	static {
		modeOptions.put(DashboardParameterMode.MODE_SUBSTITUTE.toString(), "Substitute");
		modeOptions.put(DashboardParameterMode.MODE_GLOBAL_OVERRIDE.toString(), "Global Override");
	}
		
	public enum DashboardParameterFields{
		PK_ID,
		FK_ID_DASHBOARD,
		WIDGET_TYPE,
		WIDGET_SETTING,
		PARAM_TYPE,
		NAME,
		VALUE,
		MODE,
		IS_MODE_CHANGE_ALLOWED,
	}

	private static Logger logger = CFWLog.getLogger(DashboardParameter.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardParameterFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the parameter.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyDashboard = CFWField.newInteger(FormFieldType.HIDDEN, DashboardParameterFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard this parameter is related to.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> paramType = CFWField.newString(FormFieldType.NONE, DashboardParameterFields.PARAM_TYPE)
			.setDescription("The type of the parameter.")
			.setOptions(FormFieldType.values());
	
	private CFWField<String> widgetType = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, DashboardParameterFields.WIDGET_TYPE)
			.setDescription("The type of the widget.");
	
	private CFWField<String> widgetSetting = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, DashboardParameterFields.WIDGET_SETTING)
			.setDescription("The setting of the widget.");
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.NAME)
			.setDescription("The name of the parameter. This name will be used as a placeholder like '$name$' in the widget settings.")
			.addValidator(new NotNullOrEmptyValidator());
	
	// As the type of the value will be defined by the setting it is associated with, this is stored as JSON.
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.VALUE)
			.setDescription("The value of the parameter.");
	
	private CFWField<String> mode = CFWField.newString(FormFieldType.SELECT, DashboardParameterFields.MODE)
			.setDescription("The mode of the widget.")
			.setOptions(modeOptions);
	
	private CFWField<Boolean> isModeChangeAllowed = CFWField.newBoolean(FormFieldType.NONE, DashboardParameterFields.IS_MODE_CHANGE_ALLOWED)
			.setDescription("Define if the mode can be changed or not.")
			.setValue(true)
			.setChangeHandler(new CFWFieldChangeHandler<Boolean>() {
				
				@Override
				public boolean handle(Boolean oldValue, Boolean newValue) {
					if(newValue) {
						mode.isDisabled(false);
					}else {
						mode.isDisabled(true);
					}
					
					return true;
				}
			});
	
	public DashboardParameter() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyDashboard, widgetType, widgetSetting, paramType, name, value, mode, isModeChangeAllowed);
	}

	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
					DashboardParameterFields.PK_ID.toString(), 
					DashboardParameterFields.FK_ID_DASHBOARD.toString(), 
				};
		
		String[] outputFields = 
				new String[] {
					DashboardParameterFields.PK_ID.toString(), 
					DashboardParameterFields.FK_ID_DASHBOARD.toString(), 
					DashboardParameterFields.WIDGET_TYPE.toString(),
					DashboardParameterFields.WIDGET_SETTING.toString(),
					DashboardParameterFields.PARAM_TYPE.toString(),
					DashboardParameterFields.NAME.toString(),
					DashboardParameterFields.VALUE.toString(),
					DashboardParameterFields.MODE.toString(),
					DashboardParameterFields.IS_MODE_CHANGE_ALLOWED.toString(),
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
	
	public DashboardParameter id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyDashboard() {
		return foreignKeyDashboard.getValue();
	}
	
	public DashboardParameter foreignKeyDashboard(Integer foreignKeyDashboard) {
		this.foreignKeyDashboard.setValue(foreignKeyDashboard);
		return this;
	}
	
	public String widgetType() {
		return widgetType.getValue();
	}
	
	public DashboardParameter widgetType(String value) {
		this.widgetType.setValue(value);
		return this;
	}
	
	public String paramType() {
		return paramType.getValue();
	}
	
	public DashboardParameter paramType(FormFieldType value) {
		this.paramType.setValue(value.toString());
		return this;
	}
	
	
	public String widgetSetting() {
		return widgetSetting.getValue();
	}
	
	public DashboardParameter widgetSetting(String value) {
		this.widgetSetting.setValue(value);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public DashboardParameter name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String value() {
		return value.getValue();
	}
	
	public DashboardParameter value(String value) {
		this.value.setValue(value);
		return this;
	}
	
	public String mode() {
		return mode.getValue();
	}
	
	public DashboardParameter mode(DashboardParameterMode value) {
		this.mode.setValue(value.toString());
		return this;
	}
	
	public Boolean isModeChangeAllowed() {
		return isModeChangeAllowed.getValue();
	}
	
	public DashboardParameter isModeChangeAllowed(Boolean value) {
		this.isModeChangeAllowed.setValue(value);
		return this;
	}
	
}

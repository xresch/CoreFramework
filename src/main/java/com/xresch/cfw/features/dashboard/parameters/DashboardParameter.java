package com.xresch.cfw.features.dashboard.parameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetParameter;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.CustomValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * CFWObject representing the dashboard parameters.
 * The implementation of the Dashboard Parameter functionality is rather complex. If you want to enhance or change 
 * it keep the following in mind:
 * <ul>
 *     <li> {@link DashboardParameter#addParameterHandlingToField()}: Adds defined parameters when editing widget settings</li>
 *     <li> {@link DashboardParameter#prepareParamObjectsForForm()}: For the defined parameter, get the original widget settings field. New general parameters have to be added here. </li>
 *     <li> {@link DashboardParameter#prepareParamObjectsForForm()}: For the defined parameter, get the original widget setting field. </li>
 *     
 * </ul>
 * @author Reto Scheiwiller, (c) Copyright 2021
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
		modeOptions.put(DashboardParameterMode.MODE_GLOBAL_OVERRIDE.toString(), "Global");
	}
		
	public enum DashboardParameterFields{
		PK_ID,
		FK_ID_DASHBOARD,
		WIDGET_TYPE,
		LABEL,
		PARAM_TYPE,
		NAME,
		VALUE,
		MODE,
		IS_MODE_CHANGE_ALLOWED,
	}

	private static Logger logger = CFWLog.getLogger(DashboardParameter.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.NONE, DashboardParameterFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the parameter.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyDashboard = CFWField.newInteger(FormFieldType.NONE, DashboardParameterFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard this parameter is related to.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> paramType = CFWField.newString(FormFieldType.NONE, DashboardParameterFields.PARAM_TYPE)
			.setDescription("The type of the parameter.")
			.setOptions(FormFieldType.values());
	
	private CFWField<String> widgetType = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, DashboardParameterFields.WIDGET_TYPE)
			.setDescription("The type of the widget.");
	
	private CFWField<String> paramLabel = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, DashboardParameterFields.LABEL)
			.setDescription("The label of the parameter. Either custom or the name of the widget setting.");
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.NAME)
			.setDescription("The name of the parameter. This name will be used as a placeholder like '$name$' in the widget settings.")
			.addValidator(new NotNullOrEmptyValidator())
			.addValidator(new CustomValidator() {
				
				@Override
				public boolean validate(Object value) {
					String stringValue = value.toString().trim();
					if(stringValue.matches("[\\w-_]*")) {
						if(stringValue.equals("id")
						|| stringValue.equals("title")
						|| stringValue.equals("timeframepreset")
						|| stringValue.equals("earliest")
						|| stringValue.equals("latest")) {
							CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Parameter name(yours:'"+stringValue+"') cannot be the following: id, title, timeframepreset, earliest, latest");
							return false;
						}
						return true;
					}else {
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Parameter name can only contain 0-9, a-z, A-Z, dashes and underscores(pay attention to blanks): "+value);
						return false;
					}
				}
			});
	
	// As the type of the value will be defined by the setting it is associated with, this is stored as JSON.
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.VALUE)
			.setDescription("The value of the parameter.")
			.disableSecurity();
	
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
		this.addFields(id, foreignKeyDashboard, widgetType, paramLabel, paramType, name, value, mode, isModeChangeAllowed);
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
					DashboardParameterFields.LABEL.toString(),
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
	
	public FormFieldType paramType() {
		return FormFieldType.valueOf(paramType.getValue());
	}
	
	public DashboardParameter paramType(FormFieldType value) {
		this.paramType.setValue(value.toString());
		return this;
	}
	
	
	public String paramSettingsLabel() {
		return paramLabel.getValue();
	}
	
	public DashboardParameter paramSettingsLabel(String value) {
		this.paramLabel.setValue(value);
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

	/*****************************************************************
	 * Add the defined parameters to autocomplete results and selects.
	 * 
	 *****************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addParameterHandlingToField(CFWObject settings, String dashboardID, String widgetType) {
		
		if(!widgetType.equals(WidgetParameter.WIDGET_TYPE)) {
			for(CFWField field : settings.getFields().values()) {
				
				//------------------------------------
				// Autocomplete handler
				CFWAutocompleteHandler handler = field.getAutocompleteHandler();
				if(handler != null) {
					// Wraps handler and adds itself to the field as the new handler
					new DashboardParameterAutocompleteWrapper(field, dashboardID, widgetType);
				}
				
				//------------------------------------
				// SELECT Fields
				String fieldname = field.getName();
				if(field.fieldType() == FormFieldType.SELECT) {
					ArrayList<CFWObject> availableParams = CFW.DB.DashboardParameters.getAvailableParamsForDashboard(dashboardID, widgetType, fieldname, true);
					LinkedHashMap options = field.getValueLabelOptions();
					for(CFWObject object : availableParams) {
						String param = "$"+((DashboardParameter)object).name()+"$";
						options.put(param, param);
					}
					
				}
			}
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void prepareParamObjectsForForm(HttpServletRequest request, ArrayList<CFWObject> parameterList, boolean doForWidget) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		//===========================================
		// Replace Value Field
		//===========================================
		for(CFWObject object : parameterList) {
			DashboardParameter param = (DashboardParameter)object;
			CFWField<String> currentValueField = (CFWField<String>)param.getField(DashboardParameterFields.VALUE.toString());
			CFWField newValueField;
			
			if(param.widgetType() != null) {
				//---------------------------------------
				// Replace Value field with field from WidgetSettings
				WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(param.widgetType());
				CFWObject settings = definition.getSettings();

				// param.paramSettingsLabel() returns the actual name of the setting
				newValueField = settings.getField(param.paramSettingsLabel());
				
				//adjust labels
				param.paramSettingsLabel(newValueField.getName()); //change value displayed in column "Widget Setting"
				newValueField.setLabel("Value"); //Change name of column to "Value"
				
				newValueField.setName(DashboardParameterFields.VALUE.toString());
				newValueField.setDescription("The value of the parameter.");
				
				//currentValue field is always a String field
				newValueField.setValueConvert(currentValueField.getValue());

			}else {
				//----------------------------
				// Add From ParamDefinition 
				ParameterDefinition def = CFW.Registry.Parameters.getDefinition(param.paramSettingsLabel());
				if(def != null) {
					if(doForWidget) {
						newValueField = def.getFieldForWidget(request, dashboardID, currentValueField.getValue());
					}else {
						newValueField = def.getFieldForSettings(request, dashboardID, currentValueField.getValue());
					}
					newValueField.setName(DashboardParameterFields.VALUE.toString());
					newValueField.setLabel("Value");
				}else {
					new CFWLog(logger).severe("Parameter definition could not be found:"+param.paramSettingsLabel(), new IllegalArgumentException());
					continue;
				}


			}
			
			param.getFields().remove(DashboardParameterFields.VALUE.toString());
			param.addField(newValueField);

		}
	}
	
}

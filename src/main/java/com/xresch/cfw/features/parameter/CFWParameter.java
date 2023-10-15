package com.xresch.cfw.features.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.JSON;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.CustomValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * CFWObject representing the dashboard parameters.
 * The implementation of the Dashboard Parameter functionality is rather complex. If you want to enhance or change 
 * it keep the following in mind:
 * <ul>
 *     <li> {@link CFWParameter#addParameterHandlingToField()}: Adds defined parameters when editing widget settings</li>
 *     <li> {@link CFWParameter#prepareParamObjectsForForm()}: For the defined parameter, get the original widget settings field. New general parameters have to be added here. </li>
 *     <li> {@link CFWParameter#prepareParamObjectsForForm()}: For the defined parameter, get the original widget setting field. </li>
 *     
 * </ul>
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWParameter extends CFWObject {
	
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
		IS_DYNAMIC,
	}

	private static Logger logger = CFWLog.getLogger(CFWParameter.class.getName());
	
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
			.setDescription("The value of the parameter as entered in the parameter editor. This might not be the final value(e.g. could also be a query for dynamic loading).")
			.disableSanitization();
	
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
	
	private CFWField<Boolean> isDynamic = CFWField.newBoolean(FormFieldType.NONE, DashboardParameterFields.IS_DYNAMIC)
			.setDescription("Defines if the parameter loads values dynamically or is static.")
			.setColumnDefinition("BOOLEAN DEFAULT FALSE");
	
	
	
	public CFWParameter() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyDashboard, widgetType, paramLabel, paramType, name, value, mode, isModeChangeAllowed, isDynamic);
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
					DashboardParameterFields.IS_DYNAMIC.toString(),
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
	
	public CFWParameter id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyDashboard() {
		return foreignKeyDashboard.getValue();
	}
	
	public CFWParameter foreignKeyDashboard(Integer foreignKeyDashboard) {
		this.foreignKeyDashboard.setValue(foreignKeyDashboard);
		return this;
	}
	
	public String widgetType() {
		return widgetType.getValue();
	}
	
	public CFWParameter widgetType(String value) {
		this.widgetType.setValue(value);
		return this;
	}
	
	public FormFieldType paramType() {
		return FormFieldType.valueOf(paramType.getValue());
	}
	
	public CFWParameter paramType(FormFieldType value) {
		this.paramType.setValue(value.toString());
		return this;
	}
	
	
	public String paramSettingsLabel() {
		return paramLabel.getValue();
	}
	
	public CFWParameter paramSettingsLabel(String value) {
		this.paramLabel.setValue(value);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public CFWParameter name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String value() {
		return value.getValue();
	}
	
	public CFWParameter value(String value) {
		this.value.setValue(value);
		return this;
	}
	
	public String mode() {
		return mode.getValue();
	}
	
	public CFWParameter mode(DashboardParameterMode value) {
		this.mode.setValue(value.toString());
		return this;
	}
	
	public Boolean isModeChangeAllowed() {
		return isModeChangeAllowed.getValue();
	}
	
	public CFWParameter isModeChangeAllowed(Boolean value) {
		this.isModeChangeAllowed.setValue(value);
		return this;
	}
	
	public Boolean isDynamic() {
		return isDynamic.getValue();
	}
	
	public CFWParameter isDynamic(Boolean value) {
		this.isDynamic.setValue(value);
		return this;
	}

	/*****************************************************************
	 * Returns the settings with applied parameters
	 *****************************************************************/
	public static JsonElement replaceParamsInSettings(String jsonSettings, String jsonParams, String widgetType) {
		
		//###############################################################################
		//############################ IMPORTANT ########################################
		//###############################################################################
		// When changing this method you have to apply the same changes in the javascript 
		// method:
		// cfw_dashboard.js >> cfw_parameter_applyToFields()
		//
		//###############################################################################
	
		
		// Parameter Sample
		//{"PK_ID":1092,"FK_ID_DASHBOARD":2081,"WIDGET_TYPE":null,"LABEL":"Boolean","PARAM_TYPE":false,"NAME":"boolean","VALUE":"FALSE","MODE":"MODE_SUBSTITUTE","IS_MODE_CHANGE_ALLOWED":false},
		JsonElement dashboardParams = CFW.JSON.fromJson(jsonParams);
		
		//=============================================
		// Handle SUBSTITUTE PARAMS
		//=============================================
		
		// paramSettingsLabel and paramObject
		HashMap<String, JsonObject> globalOverrideParams = new HashMap<>();
		
		if(dashboardParams != null 
		&& !dashboardParams.isJsonNull()
		&& dashboardParams.isJsonArray()
		) {
			JsonArray paramsArray = dashboardParams.getAsJsonArray();
			
			for(JsonElement current : paramsArray) {
				String paramName = current.getAsJsonObject().get("NAME").getAsString();
				
				//--------------------------------------
				// Skip Timeframe Params as they are already
				// done.
				if(paramName.equals("earliest") || paramName.equals("latest") ) {
					continue;
				}
				
				//--------------------------------------
				// Check is Global Override Parameter
				CFWParameter paramObject = new CFWParameter();
				paramObject.mapJsonFields(current, true, true);
				
				if(paramObject.mode().equals(DashboardParameterMode.MODE_GLOBAL_OVERRIDE.toString())
				&& ( paramObject.widgetType() == null || paramObject.widgetType().equals(widgetType)) ) {
					globalOverrideParams.put(paramObject.paramSettingsLabel(), current.getAsJsonObject());
					continue;
				}
					
				//--------------------------------------
				// Do Substitute
				// Escape because Java regex is a bitch.
				
				//String escaped = Matcher.quoteReplacement(paramObject.value());
				String escaped = CFW.JSON.escapeString(
								CFW.JSON.escapeString(paramObject.value())
							);
				
				if(escaped == null) {
					escaped = "";
				}
				
				jsonSettings = jsonSettings.replaceAll("\\$"+paramObject.name()+"\\$", escaped);
				
				
			}
		}
		
		//=============================================
		// Handle GLOBAL OVERRIDE PARAMS
		//=============================================
		JsonElement settingsElement = CFW.JSON.fromJson(jsonSettings);
		
		if(settingsElement != null 
		&& !settingsElement.isJsonNull()
		&& settingsElement.isJsonObject()
		) {
			JsonObject settingsObject = settingsElement.getAsJsonObject();
			
			for(String paramName : globalOverrideParams.keySet()) {
				
				if (settingsObject.has(paramName)) {
					JsonElement value = globalOverrideParams.get(paramName).get("VALUE");
					settingsObject.add(paramName, value);
				}
			}
				
		}
		
		return settingsElement;
		
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
					new ParameterAutocompleteWrapper(field, dashboardID, widgetType);
				}
				
				//------------------------------------
				// SELECT Fields
				String fieldname = field.getName();
				if(field.fieldType() == FormFieldType.SELECT) {
					ArrayList<CFWObject> availableParams = CFW.DB.Parameters.getAvailableParamsForDashboard(dashboardID, widgetType, fieldname, true);
					HashMap options = field.getOptions();
					for(CFWObject object : availableParams) {
						String param = "$"+((CFWParameter)object).name()+"$";
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
	public static void prepareParamObjectsForForm(HttpServletRequest request, ArrayList<CFWObject> parameterList, CFWTimeframe timeframe, boolean doForWidget) {
		
		String dashboardID = request.getParameter("id");
		
		//===========================================
		// Replace Value Field
		//===========================================
		for(CFWObject object : parameterList) {
			CFWParameter param = (CFWParameter)object;
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
				newValueField.setValueConvert(currentValueField.getValue(), true);

			}else {
				//----------------------------
				// Add From ParamDefinition 
				ParameterDefinition def = CFW.Registry.Parameters.getDefinition(param.paramSettingsLabel());
				if(def != null) {
					if(doForWidget) {
						newValueField = def.getFieldForWidget(request, dashboardID, currentValueField.getValue(), timeframe);
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

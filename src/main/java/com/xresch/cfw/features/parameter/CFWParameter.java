package com.xresch.cfw.features.parameter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
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
import com.xresch.cfw.features.query.store.CFWStoredQuery;
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.logging.CFWLog;
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
	
	public enum CFWParameterScope{
		  dashboard
		, query
		;
		
		private static HashSet<String> names = new HashSet<>();
		static {
			for(CFWParameterScope type : CFWParameterScope.values()) { 
				names.add(type.name()); 
			}
		}

		public static boolean has(String value) { return names.contains(value); }
		
	}
	public enum CFWParameterMode{
		MODE_SUBSTITUTE,
		MODE_GLOBAL_OVERRIDE,
		
	}
	

	private static LinkedHashMap<String, String> modeOptions = new LinkedHashMap<String, String>();
	static {
		modeOptions.put(CFWParameterMode.MODE_SUBSTITUTE.toString(), "Substitute");
		modeOptions.put(CFWParameterMode.MODE_GLOBAL_OVERRIDE.toString(), "Global");
	}
		
	public enum CFWParameterFields{
		PK_ID,
		FK_ID_DASHBOARD,
		FK_ID_QUERY,
		WIDGET_TYPE,
		LABEL,
		PARAM_TYPE,
		NAME,
		DESCRIPTION,
		VALUE,
//		JSON_VALUE,  // to support form field types that need to start with 'JSON_'
		MODE,
		IS_MODE_CHANGE_ALLOWED,
		IS_DYNAMIC,
	}

	private static Logger logger = CFWLog.getLogger(CFWParameter.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.NONE, CFWParameterFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the parameter.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyDashboard = CFWField.newInteger(FormFieldType.NONE, CFWParameterFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard this parameter is related to.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyQuery = CFWField.newInteger(FormFieldType.NONE, CFWParameterFields.FK_ID_QUERY)
			.setForeignKeyCascade(this, CFWStoredQuery.class, CFWStoredQueryFields.PK_ID)
			.setDescription("The id of the query this parameter is related to.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> paramType = CFWField.newString(FormFieldType.NONE, CFWParameterFields.PARAM_TYPE)
			.setDescription("The type of the parameter.")
			.setOptions(FormFieldType.values());
	
	private CFWField<String> widgetType = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWParameterFields.WIDGET_TYPE)
			.setDescription("The type of the widget.");
	
	private CFWField<String> paramLabel = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWParameterFields.LABEL)
			.setDescription("The label of the parameter. Either custom or the name of the widget setting.");
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWParameterFields.NAME)
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
							CFW.Messages.addErrorMessage("Parameter name(yours:'"+stringValue+"') cannot be the following: id, title, timeframepreset, earliest, latest");
							return false;
						}
						return true;
					}else {
						CFW.Messages.addErrorMessage("Parameter name can only contain 0-9, a-z, A-Z, dashes and underscores(pay attention to blanks): "+value);
						return false;
					}
				}
			});
	

	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWParameterFields.DESCRIPTION)
			.setDescription("(Optional) A description for the parameter")
			.addAttribute("rows", "1");
	
	// As the type of the value will be defined by the setting it is associated with, this is stored as JSON.
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, CFWParameterFields.VALUE)
			.setDescription("The value of the parameter as entered in the parameter editor. This might not be the final value(e.g. could also be a query for dynamic loading).")
			.disableSanitization();
	
	// As the type of the value will be defined by the setting it is associated with, this is stored as JSON.
//	private CFWField<String> jsonValue = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.JSON_VALUE)
//			.setDescription("The value of the parameter as entered in the parameter editor. This might not be the final value(e.g. could also be a query for dynamic loading).")
//			.disableSanitization();
	
	private CFWField<String> mode = CFWField.newString(FormFieldType.SELECT, CFWParameterFields.MODE)
			.setDescription("The mode of the widget.")
			.setOptions(modeOptions);
		
	private CFWField<Boolean> isModeChangeAllowed = CFWField.newBoolean(FormFieldType.NONE, CFWParameterFields.IS_MODE_CHANGE_ALLOWED)
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
	
	private CFWField<Boolean> isDynamic = CFWField.newBoolean(FormFieldType.NONE, CFWParameterFields.IS_DYNAMIC)
			.setDescription("Defines if the parameter loads values dynamically or is static.")
			.setColumnDefinition("BOOLEAN DEFAULT FALSE");
	
	
	
	public CFWParameter() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(
					id
					, foreignKeyDashboard
					, foreignKeyQuery
					, widgetType
					, paramLabel
					, paramType
					, name
					, description
					, value
					, mode
					, isModeChangeAllowed
					, isDynamic
				);
	}

	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
					CFWParameterFields.PK_ID.toString(), 
					CFWParameterFields.FK_ID_DASHBOARD.toString(), 
				};
		
		String[] outputFields = 
				new String[] {
					CFWParameterFields.PK_ID.toString(), 
					CFWParameterFields.FK_ID_DASHBOARD.toString(), 
					CFWParameterFields.FK_ID_QUERY.toString(), 
					CFWParameterFields.WIDGET_TYPE.toString(),
					CFWParameterFields.LABEL.toString(),
					CFWParameterFields.PARAM_TYPE.toString(),
					CFWParameterFields.NAME.toString(),
					CFWParameterFields.DESCRIPTION.toString(),
					CFWParameterFields.VALUE.toString(),
					//DashboardParameterFields.JSON_VALUE.toString(),
					CFWParameterFields.IS_DYNAMIC.toString(),
					CFWParameterFields.MODE.toString(),
					CFWParameterFields.IS_MODE_CHANGE_ALLOWED.toString(),
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
	
	public CFWParameter foreignKeyDashboard(Integer value) {
		this.foreignKeyDashboard.setValue(value);
		return this;
	}
	
	public Integer foreignKeyQuery() {
		return foreignKeyQuery.getValue();
	}
	
	public CFWParameter foreignKeyQuery(Integer value) {
		this.foreignKeyQuery.setValue(value);
		return this;
	}
	
	public String widgetType() {
		return widgetType.getValue();
	}
	
	public CFWParameter widgetType(String value) {
		this.widgetType.setValue(value);
		return this;
	}
	
	public CFWParameterScope scope() {
		if(foreignKeyDashboard() != null) {
			return CFWParameterScope.dashboard;
		}else {
			return CFWParameterScope.query;
		}
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
	
	public String description() {
		return description.getValue();
	}
	
	public CFWParameter description(String value) {
		this.description.setValue(value);
		return this;
	}
	
	public String value() {
		return value.getValue();
	}
	
	public CFWParameter value(String value) {
		this.value.setValue(value);
		return this;
	}
	
//	public String jsonValue() {
//		return jsonValue.getValue();
//	}
//	
//	public CFWParameter jsonValue(String value) {
//		this.jsonValue.setValue(value);
//		return this;
//	}
	
	public String mode() {
		return mode.getValue();
	}
	
	public CFWParameter mode(CFWParameterMode value) {
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
	 * Returns paramsArray as a JsonObject with key value pairs.
	 * Key will be the param name.
	 * Value will be the param value.
	 * 
	 * @param 
	 *****************************************************************/
	public static JsonObject paramsArrayToParamsObject(JsonElement parameters) {
		
		JsonObject object = new JsonObject();
		if(parameters != null 
		&& !parameters.isJsonNull()
		&& parameters.isJsonArray()
		) {
			JsonArray paramsArray = parameters.getAsJsonArray();
			
			for(JsonElement current : paramsArray) {
				String paramName = current.getAsJsonObject().get("NAME").getAsString();
				JsonElement value = current.getAsJsonObject().get("VALUE");
				object.add(paramName, value);
			}
				
		}
		
		return object;
			
	}
	/*****************************************************************
	 * Returns the settings with applied parameters.
	 * 
	 * @param javascriptStyleParams in the format of the javascript parameters
	 *****************************************************************/
	public static JsonElement replaceParamsInSettings(String jsonSettings, String javascriptStyleParams, String widgetType) {
		return replaceParamsInSettings(jsonSettings, CFW.JSON.fromJson(javascriptStyleParams), widgetType);
	}
	
	/*****************************************************************
	 * Returns the settings with applied parameters.
	 * 
	 * @param javascriptStyleParams in the format of the javascript parameters
	 *****************************************************************/
	public static JsonElement replaceParamsInSettings(String jsonSettings, JsonElement parameters, String widgetType) {
		
		//###############################################################################
		//############################ IMPORTANT ########################################
		//###############################################################################
		// When changing this method you have to apply the same changes in the javascript 
		// method:
		// cfw_parameter.js >> cfw_parameter_applyToFields()
		//
		//###############################################################################
	
		//=============================================
		// Handle SUBSTITUTE PARAMS
		//=============================================
		
		// paramSettingsLabel and paramObject
		HashMap<String, JsonObject> globalOverrideParams = new HashMap<>();
		
		if(parameters != null 
		&& !parameters.isJsonNull()
		&& parameters.isJsonArray()
		) {
			JsonArray paramsArray = parameters.getAsJsonArray();
			
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
				
				if(paramObject.mode().equals(CFWParameterMode.MODE_GLOBAL_OVERRIDE.toString())
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
	
	/*******************************************************************************
	 * Applies the parameters to the specified string.
	 * When params type is SUBSTITUTE, dollar placeholders "${paramName}$" are replaces with the
	 * parameter value.
	 * GLOBAL_OVERRIDE parameters are ignored.
	 *
	 * @param zeString the string to apply the parameters too
	 * @param jsonParams the parameters to be applied, like { paramName1: value1, etc...}
	 * @returns string with replaced parameters
	 ******************************************************************************/
	public static String substituteInString(String zeString, String jsonParams) {
		if( Strings.isNullOrEmpty(jsonParams) ) {
			return zeString;
		}
		return substituteInString(zeString, CFW.JSON.fromJson(jsonParams).getAsJsonObject(), false);
	}
	
	/*******************************************************************************
	 * Applies the parameters to the specified string.
	 * When params type is SUBSTITUTE, dollar placeholders "${paramName}$" are replaces with the
	 * parameter value.
	 * GLOBAL_OVERRIDE parameters are ignored.
	 *
	 * @param zeString the string to apply the parameters too
	 * @param jsonParams the parameters to be applied, like { paramName1: value1, etc...}
	 * @param doEscape toggle if some characters like tabs and newline should be escaped
	 * @returns string with replaced parameters
	 ******************************************************************************/
	public static String substituteInString(String zeString, JsonObject parameters, boolean doEscape) {
		
		//###############################################################################
		//############################ IMPORTANT ########################################
		//###############################################################################
		// When changing this method you have to apply the same changes in the javascript 
		// method:
		// cfw_parameter.js >> cfw_parameter_applyToFields()
		//
		//###############################################################################
	
		//=============================================
		// Prechecks
		//=============================================
		if(parameters == null 
		|| parameters.isJsonNull()
		|| ! parameters.isJsonObject()
		|| ! zeString.contains("$")
		) {
			return zeString;
		}
		
		//=============================================
		// Handle SUBSTITUTE PARAMS
		//=============================================
		JsonObject paramsObject = parameters.getAsJsonObject();
		
		for(Entry<String, JsonElement> current : paramsObject.entrySet()) {
			String paramName = current.getKey();
			JsonElement valueElement = current.getValue();
			
			String valueString = "";
			if(valueElement == null || valueElement.isJsonNull()) {
				valueString="null";
			}else if(valueElement.isJsonPrimitive() ){
				JsonPrimitive primitive = valueElement.getAsJsonPrimitive();
				if(primitive.isString()) {
					valueString = primitive.getAsString();
				}else if (primitive.isNumber()) {
					valueString = ""+primitive.getAsNumber();
				}else {
					valueString = (primitive.getAsBoolean()+"").toLowerCase();
				}
				
			}else {
				valueString = CFW.JSON.toJSON(valueElement);
			}
			

			//--------------------------------------
			// Do Substitute
			String escaped = valueString;
			
			if(doEscape) {
				escaped = CFW.JSON.escapeString(valueString);
			}
			
			if(escaped == null) {
				escaped = "";
			}

			zeString = zeString.replaceAll("\\$"+paramName+"\\$", escaped);
			
		}
		
		//=============================================
		// Return Parameters
		//=============================================
		return zeString;
		
	}
	
	

	/*****************************************************************
	 * Add the defined parameters to autocomplete results and selects.
	 * 
	 *****************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addParameterHandlingToField(CFWParameterScope scope, CFWObject settings, String itemID, String widgetType) {
		
		if(!widgetType.equals(WidgetParameter.WIDGET_TYPE)) {
			for(CFWField field : settings.getFields().values()) {
				
				//------------------------------------
				// Autocomplete handler
				CFWAutocompleteHandler handler = field.getAutocompleteHandler();
				if(handler != null) {
					// Wraps handler and adds itself to the field as the new handler
					new ParameterAutocompleteWrapper(field, scope, itemID, widgetType);
				}
				
				//------------------------------------
				// SELECT Fields
				String fieldname = field.getName();
				if(field.fieldType() == FormFieldType.SELECT) {
					
					ArrayList<CFWObject>availableParams = null;
					switch(scope) {
						case dashboard:	availableParams = CFW.DB.Parameters.getAvailableParamsForDashboard(itemID, widgetType, fieldname, true); break;
						case query:		availableParams = CFW.DB.Parameters.getAvailableParamsForQuery(itemID, fieldname);		break;
						default:		CFW.Messages.addErrorMessage("Unsupported scope: "+scope.toString());
										return;
					}
						
					for(CFWObject object : availableParams) {
						String param = "$"+((CFWParameter)object).name()+"$";
						field.addOption(param, param);
					}
					
				}
			}
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void prepareParamObjectsForForm(HttpServletRequest request, ArrayList<CFWParameter> parameterList, CFWTimeframe timeframe, boolean doForWidget) {
		
		//---------------------------------
		// Get Dashboard ID if not null
		String dashboardID = null;
		
		if(request != null) {
			request.getParameter("id");
		}
		
		//---------------------------------
		// Sanitize Timeframe
		if(timeframe == null) {
			// default to last 30 minutes
			timeframe = new CFWTimeframe();
		}
		
		
		//---------------------------------
		// Get Parameters Selected by User
		String userSelectedParamsJson = null;
		if(request != null) {
			userSelectedParamsJson = request.getParameter(FeatureParameter.CFW_PARAMS);
		}
				
		
		JsonObject userSelectedParamsObject = null;
		if( !Strings.isNullOrEmpty(userSelectedParamsJson) ) {
			JsonElement paramsElementArray = CFW.JSON.fromJson(userSelectedParamsJson);
			userSelectedParamsObject = CFWParameter.paramsArrayToParamsObject(paramsElementArray);
		}
		
		//===========================================
		// Replace Value Field
		//===========================================
		for(CFWParameter param : parameterList) {

			CFWField<String> currentValueField = (CFWField<String>)param.getField(CFWParameterFields.VALUE.toString());
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
				
				newValueField.setName(CFWParameterFields.VALUE.toString());
				newValueField.setDescription("The value of the parameter.");
				
				//currentValue field is always a String field
				newValueField.setValueConvert(currentValueField.getValue(), true);

			}else {
				//----------------------------
				// Add From ParamDefinition 
				ParameterDefinition def = CFW.Registry.Parameters.getDefinition(param.paramSettingsLabel());
				if(def != null) {
					if(doForWidget) {
						newValueField = def.getFieldForWidget(request, dashboardID, currentValueField.getValue(), timeframe, userSelectedParamsObject);
					}else {
						newValueField = def.getFieldForSettings(request, dashboardID, currentValueField.getValue());
					}
					newValueField.setName(CFWParameterFields.VALUE.toString());
					newValueField.setLabel("Value");
				}else {
					new CFWLog(logger).severe("Parameter definition could not be found:"+param.paramSettingsLabel(), new IllegalArgumentException());
					continue;
				}


			}
			
			param.getFields().remove(CFWParameterFields.VALUE.toString());
			param.addField(newValueField);

		}
	}
	
	/*****************************************************************
	 * Create Json Conversion
	 *****************************************************************/
	public JsonObject toJson() {
		

		JsonObject object = new JsonObject();
		
		object.addProperty(name.getName(), name.getValue());
		

		
		//------------------------------------------------
		// Convert Value to Correct Type
		if(value.getValue() == null) {
			object.add(id.getName(), JsonNull.INSTANCE);
		}else if(paramType.getValue().contentEquals("BOOLEAN")) {
			object.addProperty(value.getName(), Boolean.parseBoolean(value.getValue()) );
		}else if(paramType.getValue().contentEquals("NUMBER")) {
			
			object.addProperty(value.getName(), new BigDecimal(value.getValue()) );
		}else {
			object.addProperty(value.getName(), value.getValue() );
		}
		
		object.addProperty(id.getName(), id.getValue());
		object.addProperty(foreignKeyDashboard.getName(), foreignKeyDashboard.getValue());
		object.addProperty(widgetType.getName(), widgetType.getValue());
		object.addProperty(paramLabel.getName(), paramLabel.getValue());
		object.addProperty(paramType.getName(), paramType.getValue());		
		object.addProperty(mode.getName(), mode.getValue());
		object.addProperty(isModeChangeAllowed.getName(), isModeChangeAllowed.getValue());
		object.addProperty(isDynamic.getName(), isDynamic.getValue());
		
		return object;
	}
}

package com.pengtoolbox.cfw.datahandling;

import java.security.Key;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.core.FeatureCore;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.pengtoolbox.cfw.utils.TextUtils;
import com.pengtoolbox.cfw.validation.BooleanValidator;
import com.pengtoolbox.cfw.validation.EpochOrTimeValidator;
import com.pengtoolbox.cfw.validation.FloatValidator;
import com.pengtoolbox.cfw.validation.IValidatable;
import com.pengtoolbox.cfw.validation.IValidator;
import com.pengtoolbox.cfw.validation.IntegerValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWField<T> extends HierarchicalHTMLItem implements IValidatable<T> {
	
	private static Logger logger = CFWLog.getLogger(CFWField.class.getName());
	
	//--------------------------------------------------
	// Password Handling Constants
	private static String PASSWORD_STUB_PREFIX = "cfwStubPW-";
	private static Cache<String, String> pwCache = 
			CacheBuilder.newBuilder()
						.maximumSize(1000)
						.expireAfterWrite(10, TimeUnit.HOURS)
						.build();
	
	//--------------------------------------------------
	// Encryption Constants
	// IMPORTANT!!! Do not change these values, you will
	// break any application already using this mechanism.
	private static String ENCRYPT_PREFIX = "cfwenc:";
	private static String ENCRYPT_ALGORITHM = "AES";
	

	//--------------------------------
	// General
	private String name = "";
	private Object value;
	private String description = null;
	private Class<T> valueClass;
	private ArrayList<String> invalidMessages;
	private byte[] encryptionSalt = null;
	private boolean allowHTML = false;
	private boolean sanitizeStrings = true;
	private boolean preventFormSubmitOnEnter = true;
	
	private ArrayList<IValidator> validatorArray;

	@SuppressWarnings("rawtypes")
	private CFWFieldChangeHandler changeHandler = null;
	
	private CFWAutocompleteHandler autocompleteHandler = null;
	//--------------------------------
	// Form
	private FormFieldType type;
	private String formLabel = "&nbsp;";
	@SuppressWarnings("rawtypes")
	private LinkedHashMap valueLabelOptions = null;
	private boolean isDisabled = false;
	
	public enum FormFieldType{
		TEXT, TEXTAREA, PASSWORD, NUMBER, EMAIL, HIDDEN, BOOLEAN, SELECT, LIST, WYSIWYG, DATEPICKER, DATETIMEPICKER, TAGS, TAGS_SELECTOR, NONE
	}
	
	//--------------------------------
	// API
	private FormFieldType apiFieldType;
	
	//--------------------------------
	// Database
	private String columnDefinition = null;
	
	//###################################################################################
	// CONSTRUCTORS
	//###################################################################################
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected CFWField(Class clazz, FormFieldType type, String fieldName) {
		this.valueClass = clazz;
		this.type = type;
		this.name = fieldName;
		this.formLabel = TextUtils.fieldNameToLabel(fieldName);
	}
			
	//###################################################################################
	// Initializer
	//###################################################################################
	
	//===========================================
	// String
	//===========================================
	public static CFWField<String> newString(FormFieldType type, Enum<?> fieldName){
		return newString(type, fieldName.toString());
	}
	public static CFWField<String> newString(FormFieldType type, String fieldName){
		return new CFWField<String>(String.class, type, fieldName)
				.setColumnDefinition("VARCHAR");
	}
	
	
	//===========================================
	// Integer
	//===========================================
	public static CFWField<Integer> newInteger(FormFieldType type, Enum<?> fieldName){
		return newInteger(type, fieldName.toString());
	}
	public static CFWField<Integer> newInteger(FormFieldType type, String fieldName){
		return new CFWField<Integer>(Integer.class, type, fieldName)
				.setColumnDefinition("INT")
				.addValidator(new IntegerValidator());
	}
	
	//===========================================
	// Float
	//===========================================
	public static CFWField<Integer> newFloat(FormFieldType type, Enum<?> fieldName){
		return newInteger(type, fieldName.toString());
	}
	public static CFWField<Float> newFloat(FormFieldType type, String fieldName){
		return new CFWField<Float>(Float.class, type, fieldName)
				.setColumnDefinition("FLOAT")
				.addValidator(new FloatValidator());
	}
	//===========================================
	// Boolean
	//===========================================
	public static CFWField<Boolean> newBoolean(FormFieldType type, Enum<?> fieldName){
		return newBoolean(type, fieldName.toString());
	}
	public static CFWField<Boolean> newBoolean(FormFieldType type, String fieldName){
		return new CFWField<Boolean>(Boolean.class, type, fieldName)
				.setColumnDefinition("BOOLEAN")
				.addValidator(new BooleanValidator());
	}
	
	//===========================================
	// Timestamp
	//===========================================
	public static CFWField<Timestamp> newTimestamp(FormFieldType type, Enum<?> fieldName){
		return newTimestamp(type, fieldName.toString());
	}
	public static CFWField<Timestamp> newTimestamp(FormFieldType type, String fieldName){
		return new CFWField<Timestamp>(Timestamp.class, type, fieldName)
				.setColumnDefinition("TIMESTAMP")
				.addValidator(new EpochOrTimeValidator());
	}
	
	//===========================================
	// Date
	//===========================================
	public static CFWField<Date> newDate(FormFieldType type, Enum<?> fieldName){
		return newDate(type, fieldName.toString());
	}
	
	public static CFWField<Date> newDate(FormFieldType type, String fieldName){
		return new CFWField<Date>(Date.class, type, fieldName)
				.setColumnDefinition("DATE")
				.addValidator(new EpochOrTimeValidator());
	}
	
	//===========================================
	// Array
	//===========================================
	public static CFWField<Object[]> newArray(FormFieldType type, Enum<?> fieldName){
		return newArray(type, fieldName.toString());
	}
	
	public static CFWField<Object[]> newArray(FormFieldType type, String fieldName){
		return new CFWField<Object[]>(Object[].class, type, fieldName)
				.setColumnDefinition("ARRAY");
	}
		
	//===========================================
	// TAGS SELECTOR
	//===========================================
	public static CFWField<LinkedHashMap<String,String>> newTagsSelector(Enum<?> fieldName){
		return newTagsSelector(fieldName.toString());
	}
	public static CFWField<LinkedHashMap<String,String>> newTagsSelector(String fieldName){
		if(!fieldName.startsWith("JSON_")) {
			new CFWLog(logger)
				.method("newTagsSelector")
				.severe("Fieldname of TAG_SELECTOR fields have to start with 'JSON_'.", new InstantiationException());
			return null;
		}
		return new CFWField<LinkedHashMap<String,String>> (LinkedHashMap.class, FormFieldType.TAGS_SELECTOR, fieldName)
				.setColumnDefinition("VARCHAR");
	}
	//###########################################################################################################
	//###########################################################################################################
	// HTML and Form Methods
	//###########################################################################################################
	//###########################################################################################################
		
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @param StringBuilder to append the resulting html
	 * @return String html for this item. 
	 ***********************************************************************************/
	@SuppressWarnings("unchecked")
	protected void createHTML(StringBuilder html) {

		//---------------------------------------------
		// Check Type
		//---------------------------------------------
		FormFieldType formFieldType = this.type;
		if(this.parent instanceof CFWForm) {
			
			// Use normal fieldType if apiFieldType is not defined
			if(((CFWForm)this.parent).isAPIForm() && this.apiFieldType != null) {
				formFieldType = this.apiFieldType;
			}
			
			if ( ((CFWForm)this.parent).isEmptyForm() && !this.name.contentEquals("cfw-formID") ){
				// Set Value to num
				value = null;
				
				if(this.valueLabelOptions != null) {
					valueLabelOptions.put("", "");
				}
			}
		}
		//---------------------------------------------
		// Create Form Group
		//---------------------------------------------
		if(formFieldType != FormFieldType.HIDDEN && formFieldType != FormFieldType.NONE) {
			
			//---------------------------------------
			// Create Group and Label
			html.append("<div class=\"form-group row ml-1\">");
			html.append("  <label class=\"col-sm-3 col-form-label\" for=\""+name+"\" >");
			html.append(formLabel+":</label> ");
			
			//---------------------------------------
			// Check if Autocomplete
			if(autocompleteHandler == null) {
				html.append("  <div class=\"col-sm-9\">");
			}else {
				html.append("  <div class=\"col-sm-9 cfw-autocomplete\" >");
				this.addAttribute("id", name);
			}
			
			//---------------------------------------
			// Check if Description available
			if(description != null && !description.isEmpty()) {
				html.append("<span class=\"badge badge-info cfw-decorator\" data-toggle=\"tooltip\" data-placement=\"top\" data-delay=\"500\" title=\""+description+"\"><i class=\"fa fa-sm fa-info\"></i></span>");
			}
			
		}
		
		//---------------------------------------------
		// Set Attributes
		//---------------------------------------------
		this.addAttribute("placeholder", formLabel);
		this.addAttribute("name", name);
		
		if(isDisabled) {	this.addAttribute("disabled", "disabled");};
		
		if(preventFormSubmitOnEnter 
		&& this.type != FormFieldType.TEXTAREA
		&& this.type != FormFieldType.TAGS
		&& this.type != FormFieldType.TAGS_SELECTOR) {	
			this.addAttribute("onkeydown", "return event.key != 'Enter';");
		};
		
		if(value != null) {	
			
			if( !(value instanceof Object[]) ) {
				this.addAttribute("value", value.toString().replace("\"", "&quot;")); 
			}else {
				StringBuilder builder = new StringBuilder();
				Object[] array = (Object[])value;
				for(Object object : array) {
					builder.append(object.toString()).append(",");
				}
				if(array.length > 0) {
					builder.deleteCharAt(builder.length()-1);
				}
				this.addAttribute("value", builder.toString().replace("\"", "&quot;")); 
			}
			
		};
		
		
		//---------------------------------------------
		// Create Field
		//---------------------------------------------
		switch(formFieldType) {
			case TEXT:  			html.append("<input type=\"text\" class=\"form-control\" "+this.getAttributesString()+"/>");
									break;
			
			case NUMBER:  			html.append("<input type=\"number\" class=\"form-control\" "+this.getAttributesString()+"/>");
									break;
			
			case TEXTAREA: 			createTextArea(html);
									break;
			
			case WYSIWYG: 			createWYSIWYG(html);
									break;						
									
			case HIDDEN:  			html.append("<input type=\"hidden\" "+this.getAttributesString()+"/>");
									break;
			
			case BOOLEAN:  			createBooleanRadiobuttons(html);
									break;		
									
			case SELECT:  			createSelect(html);
									break;	
									
			case LIST:  			createList(html);
									break;	
			case EMAIL:  			html.append("<input type=\"email\" class=\"form-control\" "+this.getAttributesString()+"/>");
									break;
								
			case DATEPICKER:  		createDatePicker(html);
									break;
			
			case DATETIMEPICKER:  	createDateTimePicker(html);
									break;
			
			case TAGS:			  	createTagsField(html, FormFieldType.TAGS);
									break;
									
			case TAGS_SELECTOR:		createTagsField(html, FormFieldType.TAGS_SELECTOR);
									break;						
									
			case PASSWORD:  		createPasswordField(html);
									break;
			
			case NONE:				//do nothing
									break;
			
		}
		
		//---------------------------------------------
		// Add Autocomplete Initialization
		//---------------------------------------------
		if(autocompleteHandler != null) {
			if(this.parent instanceof CFWForm) {
				String formID = ((CFWForm)this.parent).getFormID();
				int maxResults = this.getAutocompleteHandler().getMaxResults();
				((CFWForm)this.parent).javascript.append("cfw_initializeAutocomplete('"+formID+"','"+name+"',"+maxResults+");\r\n");
			}
		}
		//---------------------------------------------
		// Close Field
		//---------------------------------------------
		if(formFieldType != FormFieldType.HIDDEN && formFieldType != FormFieldType.NONE) {
			html.append("</div>");
			html.append("</div>");
		}
	}

	/***********************************************************************************
	 * Create Boolean Radio Buttons
	 ***********************************************************************************/
	private void createBooleanRadiobuttons(StringBuilder html) {
		
		String falseChecked = "";
		String trueChecked = "";
		
		if(value != null && value.toString().trim().toLowerCase().equals("true")) {
			trueChecked = "checked";
		}else {
			falseChecked = "checked";
		}
		
		this.removeAttribute("value");
		
		String disabled = "";
		if(isDisabled) {	disabled = "disabled=\"disabled\""; };
		
		html.append("<div class=\"form-check form-check-inline col-form-labelmt-5\">" + 
			"  <input class=\"form-check-input\" type=\"radio\" value=\"true\" name=\""+name+"\" "+this.getAttributesString()+" "+disabled+" "+trueChecked+" />" + 
			"  <label class=\"form-check-label\" for=\"inlineRadio1\">true</label>" + 
			"</div>");
		
		html.append("<div class=\"form-check form-check-inline col-form-label\">" + 
				"  <input class=\"form-check-input\" type=\"radio\" value=\"false\" name=\""+name+"\" "+this.getAttributesString()+" "+disabled+" "+falseChecked+"/>" + 
				"  <label class=\"form-check-label\" for=\"inlineRadio1\">false</label>" + 
				"</div>");
	}
	
	/***********************************************************************************
	 * Create Select
	 ***********************************************************************************/
	private void createSelect(StringBuilder html) {
		
		this.removeAttribute("value");
		
		String stringVal = (value == null) ? "" : value.toString();
		
		html.append("<select class=\"form-control\" "+this.getAttributesString()+" >");
		
		//-----------------------------------
		// handle options
		if(valueLabelOptions != null) {
			for(Object optionValue : valueLabelOptions.keySet()) {
				String currentLabel = valueLabelOptions.get(optionValue).toString();
				if(optionValue.toString().equals(stringVal)) {
					html.append("<option value=\""+optionValue+"\" selected>")
						.append(currentLabel)
					.append("</option>");
				}else {
					html.append("<option value=\""+optionValue+"\">")
						.append(currentLabel)
					.append("</option>");
				}
			}
		}
		
		html.append("</select>");
	}
	
	/***********************************************************************************
	 * Create List
	 ***********************************************************************************/
	private void createList(StringBuilder html) {
		
		html.append("<input list=\"list-"+name+"\" class=\"form-control\" "+this.getAttributesString()+"/>");

		//-----------------------------------
		// handle options
		if(valueLabelOptions != null) {
			html.append("<datalist id=\"list-"+name+"\">");
			for(Object optionValue : valueLabelOptions.keySet()) {
				String currentLabel = valueLabelOptions.get(optionValue).toString();
				html.append("<option value=\""+optionValue+"\">")
					.append(currentLabel)
					.append("</option>");
			}
			html.append("</datalist>");
		}
		
		html.append("</select>");
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createWYSIWYG(StringBuilder html) {
		
		//---------------------------------
		// Set initial value
		this.removeAttribute("value");
		this.addAttribute("id", name);
		//---------------------------------
		// Create Field
		html.append("<textarea class=\"form-control\" "+this.getAttributesString()+"></textarea>");

		
		if(this.parent instanceof CFWForm) {
			CFWForm form = ((CFWForm)this.parent);
			
			form.javascript.append("cfw_initializeSummernote('"+form.getFormID()+"', '"+name+"');\r\n");
		}

		
	}
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createDatePicker(StringBuilder html) {
		
		//---------------------------------
		// Set initial value
		String epochTime = null;
		if(this.value != null) {
			
			if(value instanceof Date) {
				epochTime = ""+((Date)value).getTime();
				this.addAttribute("value", ""+epochTime);
			}else if(value instanceof Timestamp) {
				epochTime = ""+((Timestamp)value).getTime();
				this.addAttribute("value", ""+epochTime);
			}else {
				epochTime = value.toString();
				this.addAttribute("value", ""+value.toString());
			}

		}
		
		//---------------------------------
		// Create Field
		
		html.append("<input id=\""+name+"-datepicker\" type=\"date\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"form-control\" placeholder=\"Date\" >\r\n" + 
				"	<input id=\""+name+"\" type=\"hidden\" class=\"form-control\" "+this.getAttributesString()+">\r\n");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeTimefield('"+name+"', "+epochTime+");\r\n");
		}

		
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createDateTimePicker(StringBuilder html) {
		
		//---------------------------------
		// Set initial value
		String epochTime = null;
		if(this.value != null) {
			
			if(value instanceof Date) {
				epochTime = ""+((Date)value).getTime();
				this.addAttribute("value", ""+epochTime);
			}else if(value instanceof Timestamp) {
				epochTime = ""+((Timestamp)value).getTime();
				this.addAttribute("value", ""+epochTime);
			}else {
				epochTime = value.toString();
				this.addAttribute("value", ""+value.toString());
			}

		}
		
		//---------------------------------
		// Create Field
		html.append("  <div class=\"custom-control-inline w-100 mr-0\">\r\n"
					+ "    <input id=\""+name+"-datepicker\" type=\"date\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"col-md-9 form-control\" >\r\n"
					+ "    <input id=\""+name+"-timepicker\" type=\"time\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"col-md-3 form-control\">"
					+ "	   <input id=\""+name+"\" type=\"hidden\" class=\"form-control\" "+this.getAttributesString()+">\r\n" 
					+ "</div>\r\n");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeTimefield('"+name+"', "+epochTime+");\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createTagsField(StringBuilder html, FormFieldType type) {
		
		int maxTags = 128;
		
		if(attributes.containsKey("maxTags")) {
			maxTags = Integer.parseInt(attributes.get("maxTags"));
		}
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"text\" data-role=\"tagsinput\" class=\"form-control\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			if(type.equals(FormFieldType.TAGS_SELECTOR)) {
				((CFWForm)this.parent).javascript.append("cfw_initializeTagsSelectorField('"+name+"', "+maxTags+", "+CFW.JSON.toJSON(value)+");\r\n");
			}else {
				((CFWForm)this.parent).javascript.append("cfw_initializeTagsField('"+name+"', "+maxTags+");\r\n");
			}
		}
				
	}
	
	/***********************************************************************************
	 * Create a text area
	 ***********************************************************************************/
	private void createTextArea(StringBuilder html) {
		
		if(!this.attributes.containsKey("rows")) {
			this.addAttribute("rows", "5");
		}
		this.removeAttribute("value");
		String value = "";
		if(this.value != null) {
			value = this.value.toString();
		}
		html.append("<textarea class=\"form-control\" "+this.getAttributesString()+">"+value+"</textarea>");
	}
	
	/***********************************************************************************
	 * Create a password field.
	 ***********************************************************************************/
	private void createPasswordField(StringBuilder html) {
		
		if(this.value != null && !value.toString().isEmpty()) {
			String placeholderName = PASSWORD_STUB_PREFIX + CFW.Security.createRandomStringAtoZ(7);
			pwCache.put(placeholderName, this.value.toString());
			this.addAttribute("value", placeholderName);
		}
		html.append("<input type=\"password\" class=\"form-control\" "+this.getAttributesString()+"/>");

	}
	
	/***********************************************************************************
	 * Add an attribute to the html tag.
	 * Adding a value for the same attribute multiple times will overwrite preceding values.
	 * @param name the name of the attribute.
	 * @param key the key of the attribute.
	 * @return instance for chaining
	 ***********************************************************************************/
	@SuppressWarnings("unchecked")
	public CFWField<T> addAttribute(String name, String value) {
		return (CFWField<T>)super.addAttribute(name, value);
	}
	
	/***********************************************************************************
	 * Remove an attribute from the html tag.
	 * Adding a value for the same attribute multiple times will overwrite preceeding values.
	 * 
	 * @param name the name of the attribute.
	 * @return instance for chaining
	 ***********************************************************************************/
	@SuppressWarnings("unchecked")
	public CFWField<T> removeAttribute(String name) {
		return (CFWField<T>)super.removeAttribute(name);
	}
	
	//###########################################################################################################
	//###########################################################################################################
	// IValidatable Implementation 
	//###########################################################################################################
	//###########################################################################################################
		
	/*************************************************************************
	 * Executes all validators added to this instance and validates the current
	 * value.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validate(){
		
		boolean isValid = true;
		
		if(validatorArray != null) {
			
			invalidMessages = new ArrayList<String>();
			for(IValidator validator : validatorArray){
				
				if(!validator.validate(value)){
					invalidMessages.add(validator.getInvalidMessage());
					isValid=false;
				}
			}
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Executes all validators added to the instance of this class.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validateValue(Object value){
		
		boolean isValid = true;
		if(validatorArray != null) {
			invalidMessages = new ArrayList<String>();
			for(IValidator validator : validatorArray){
				
				if(!validator.validate(value)){
					invalidMessages.add(validator.getInvalidMessage());
					
					isValid=false;
				}
			}
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/ 
	protected void addInvalidMessage(String message) {
		if(invalidMessages == null) {
			invalidMessages = new ArrayList<String>();
		}
		invalidMessages.add(message);
	}
		
	/*************************************************************************
	 * Add a validator to the field.
	 * Will be executed when using setValueValidated() 
	 * or mapAndValidateParamsToFields();
	 * 
	 * @param validator to add
	 * @return instance for chaining
	 *************************************************************************/ 
	public CFWField<T> addValidator(IValidator validator) {
		if(validatorArray == null) {
			validatorArray = new ArrayList<IValidator>();
		}
		if(!validatorArray.contains(validator)) {
			validatorArray.add(validator);
			validator.setValidateable(this);
		}
		
		return this;
	}
	
	/******************************************************************************************************
	 * Remove the validator from this field
	 * 
	 * @return true if the specified validator was in the list
	 ******************************************************************************************************/
	public boolean removeValidator(IValidator o) {
		if(validatorArray != null) {
			return validatorArray.remove(o);
		}
		return false;
	}
	
	//###########################################################################################################
	//###########################################################################################################
	// Getters and Setters
	//###########################################################################################################
	//###########################################################################################################
	
	/******************************************************************************************************
	 * Set the name of this field.
	 * Will be used as the name attribute of form elements and the name of the DB column.
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setName(String propertyName) {
		this.name = propertyName;
		return this;
	}
	
	public String getName() {
		return name;
	}
		
	public String getLabel() {
		return formLabel;
	}

	/******************************************************************************************************
	 * Set the label of this field.
	 * Will be used as the field label in html forms.
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setLabel(String label) {
		fireChange();
		this.formLabel = label;
		return this;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public String getDescription() {
		return description;
	}

	/******************************************************************************************************
	 * Set the desciption of this field.
	 * Will be used as a decorator in html forms.
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setDescription(String description) {
		fireChange();
		this.description = description;
		return this;
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	public String getColumnDefinition() {
		return columnDefinition;
	}
	
	/******************************************************************************************************
	 * Set the DB column definition of this field. 
	 * e.g "VARCHAR(255)", "INT UNIQUE", "CLOB", "BOOLEAN", "ARRAY"
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
		return this;
	}
	
	/******************************************************************************************************
	 * Enable Encryption of values for storing them into the database. 32 bytes of the string
	 * will be used, additional bytes will be ignored, bytes will be added if the string is shorter.
	 ******************************************************************************************************/
	public CFWField<T> enableEncryption(String encryptionSalt) {
		if( String.class.isAssignableFrom(this.getValueClass()) ){
			this.encryptionSalt = Arrays.copyOf(encryptionSalt.getBytes(), 32);
		}
		return this;
	}
	
	/******************************************************************************************************
	 * Allow HTML content and do not sanitize the input values. 
	 ******************************************************************************************************/
	public CFWField<T> disableSecurity() {
		this.allowHTML = true;
		this.sanitizeStrings = false;
		return this;
	}

	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public CFWField<T> allowHTML(boolean allowHTML) {
		this.allowHTML = allowHTML;
		return this;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public CFWField<T> sanitizeStrings(boolean sanitizeStrings) {
		this.sanitizeStrings = sanitizeStrings;
		return this;
	}
	
	/******************************************************************************************************
	 * Set the DB column definition of this field as a primary key.
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	public CFWField<T> setPrimaryKeyAutoIncrement(CFWObject parentObject) {
		
		this.columnDefinition = "INT PRIMARY KEY AUTO_INCREMENT";
		if(this.valueClass == Integer.class) {
			parentObject.setPrimaryField((CFWField<Integer>)this);
		}
		return this;
	}
	
	public CFWField<T> setForeignKeyCascade(CFWObject parent, Class<? extends CFWObject> foreignObject, Enum<?> foreignField) {
		return setForeignKeyCascade(parent, foreignObject, foreignField.toString());
	}
	
	public CFWField<T> setForeignKeyCascade(CFWObject parent, Class<? extends CFWObject> foreignObject, String foreignField) {
		parent.addForeignKey(this.getName(), foreignObject, foreignField, "CASCADE");
		return this;
	}
		
	public LinkedHashMap<?, ?> getValueLabelOptions() {
		return valueLabelOptions;
	}
	
	/******************************************************************************************************
	 * Returns the type of this fields.
	 ******************************************************************************************************/
	public FormFieldType fieldType() {
		return this.type;
	}
	
	/******************************************************************************************************
	 * Set this field as a parameter for the API.
	 * 
	 * @param type of the form field
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> apiFieldType(FormFieldType apiFieldType) {
		this.apiFieldType = apiFieldType;
		return this;
	}
	
	/******************************************************************************************************
	 *
	 ******************************************************************************************************/
	public FormFieldType getAPIFormFieldType() {
		return apiFieldType;
	}
	

	/******************************************************************************************************
	 * Set values for selection fields. The string representations of the provided elements will be used. 
	 * The values will be used as labels for the options.
	 * This will reset any options set with setValueLabelOptions().
	 * 
	 * @param array with values
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setOptions(Object[] options) {
		LinkedHashMap<Object,Object> optionsMap = new LinkedHashMap<Object,Object>();
		if(options != null) {
			for(Object option : options) {
				optionsMap.put(option, option);
			}
			this.valueLabelOptions = optionsMap;
		}
		return this;
	}
	
	/******************************************************************************************************
	 * Set values for selection fields. First element in the map will be the value of the field, the second
	 * will be used as the label for the option.
	 * This will reset any options set with setOptions().
	 * 
	 * @param map with value/label pairs
	 * @return instance for chaining
	 ******************************************************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWField<T> setOptions(LinkedHashMap valueLabelPairs) {
		this.valueLabelOptions = valueLabelPairs;
		return this;
	}
	
	/******************************************************************************************************
	 * Check if this field is disabled.
	 * 
	 ******************************************************************************************************/
	public boolean isDisabled() {return isDisabled;}

	/******************************************************************************************************
	 * Change if this field should be enabled or disabled when represented as a form field.
	 * 
	 ******************************************************************************************************/
	public CFWField<T> isDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
		return this;
	}
	
	/******************************************************************************************************
	 * Check if this field prevents submit on enter.
	 * 
	 ******************************************************************************************************/
	public boolean preventFormSubmitOnEnter() {return preventFormSubmitOnEnter;}

	/******************************************************************************************************
	 * Change if this field should prevent submit on enter.
	 * 
	 ******************************************************************************************************/
	public CFWField<T> preventFormSubmitOnEnter(boolean preventFormSubmitOnEnter) {
		this.preventFormSubmitOnEnter = preventFormSubmitOnEnter;
		return this;
	}
	

	/******************************************************************************************************
	 * Change the value and trigger the change handler if specified.
	 * Only changes the value when assigned change handler returns true.
	 * Triggers HierarchicalHTMLItem.fireChange() to propagate changes.
	 * 
	 * @param value to apply.
	 * return true on success, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	private boolean changeValue(Object value) {
		
		if(this.isDisabled()) { 
			new CFWLog(logger)
			.method("changeValue")
			.severe("The field '"+this.name+"' cannot be changed as the field is disabled.");
			return false; 
		}
		if(changeHandler != null) {
			if(changeHandler.handle(this.value, value)) {
				this.value = value;
			}else {
				return false;
			}
		}else {
			this.value = value;
		}
		
		this.fireChange();
		return true;
	}
	
	/******************************************************************************************************
	 * Sanitize String values based on the fields settings.
	 * 
	 ******************************************************************************************************/
	private String sanitizeString(String value) {
		String saneValue = value;
		
		if(!this.allowHTML && !CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_ALLOW_HTML)) {
			saneValue = CFW.Security.escapeHTMLEntities(saneValue);
		}else if(sanitizeStrings && !CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_ALLOW_JAVASCRIPT) ) {
			saneValue = CFW.Security.sanitizeHTML((String)saneValue);
		}
		
		return saneValue;
	}
	
	
	/******************************************************************************************************
	 * Returns true if this value should be stored encrypted when writing it to the database.
	 ******************************************************************************************************/
	public boolean persistEncrypted() {
		return encryptionSalt != null;
	}
	
	/******************************************************************************************************
	 * Returns the value encrypted.
	 * Will be ignored 
	 ******************************************************************************************************/
	public  T getValueEncrypted() {
		if(encryptionSalt == null 
		|| value == null ) {
			return (T)value;
		}else {
			String encryptedValue = null;
			try { 
				//---------------------------
				// Prepare Cipher
				Key key = new SecretKeySpec(encryptionSalt, ENCRYPT_ALGORITHM);
		        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
		        cipher.init(Cipher.ENCRYPT_MODE, key);
		      
		        //---------------------------
		      	// Encode Value
		        byte[] encodedBytes = cipher.doFinal(value.toString().getBytes());
		        encryptedValue = Base64.getEncoder().encodeToString(encodedBytes);
		        encryptedValue = ENCRYPT_PREFIX + encryptedValue;
		       
			}catch (Exception e) {
				new CFWLog(logger)
					.method("getValueDecrypted")
					.severe("Could not encrypt the value.", e);
			}
			
			return (T)encryptedValue;
		}
    }
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public T decryptValue(T value) {
		if(encryptionSalt == null 
		|| value == null
		|| !value.toString().startsWith(ENCRYPT_PREFIX)) {
			return (T)value;
		}else {
			String decryptedValue = null;
			try {
				Key key = new SecretKeySpec(encryptionSalt, ENCRYPT_ALGORITHM);
		        Cipher cipher;
	
					cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
	
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        String encryptedValue = value.toString().replaceFirst(ENCRYPT_PREFIX, "");
		        byte[] decryptedBytes = Base64.getDecoder().decode(encryptedValue);
		        byte[] decodedValue = cipher.doFinal(decryptedBytes);
		        decryptedValue = new String(decodedValue);
		        
			} catch (Exception e) {
				new CFWLog(logger)
					.method("getValueDecrypted")
					.severe("Could not decrypt value.", e);
			}
			
			return (T)decryptedValue;
		}
    }
    
	/******************************************************************************************************
	 * Change the value by first converting it to the correct type.
	 * 
	 ******************************************************************************************************/
	private boolean setValueConvert(T value) {
		boolean success = true;
				
		//-------------------------------------------------
		// prevent Strings from being empty. Might lead to 
		// unique constraint violation on DB when not using 
		// null values.
		if(value == null 
		|| ( valueClass == String.class && (((String)value).trim().equals(""))) ) {
			return this.changeValue(null);
		}
		
		
		//-------------------------------------------------
		// If value is a subclass of the valueClass change 
		// the value without conversion
		if(this.valueClass.isAssignableFrom(value.getClass())) {
			
			//---------------------------------
			// Sanitize strings if needed 
			if(valueClass == String.class ) {
				return this.changeValue(sanitizeString((String)value));
			}else {
				return this.changeValue(value);
			}
		}
		
		//-------------------------------------------------
		// Convert string values to the appropriate type
		if(value.getClass() == String.class) {
			
			if( ((String)value).trim().equals("")) { 
				if(valueClass == Integer.class) 	    { return this.changeValue(null); }
				else if(valueClass == Float.class) 		{ return this.changeValue(null); }
				else if(valueClass == Boolean.class) 	{ return this.changeValue(false); }
				else if(valueClass == Timestamp.class)  { return this.changeValue(null);  }
				else if(valueClass == Date.class)  		{ return this.changeValue(null); }
				else if(valueClass == Object[].class)	{ return this.changeValue(null); }
				else {	
					new CFWLog(logger)
					.method("setValueConvert")
					.severe("The choosen type is not supported: "+valueClass.getName());
					return false;
				}
			}
			else if(valueClass == Integer.class) 	{ return this.changeValue(Integer.parseInt((String)value)); }
			else if(valueClass == Float.class) 		{ return this.changeValue(Float.parseFloat((String)value)); }
			else if(valueClass == Boolean.class) 	{ return this.changeValue(Boolean.parseBoolean( ((String)value).trim()) ); }
			else if(valueClass == Timestamp.class)  { return this.changeValue(new Timestamp(Long.parseLong( ((String)value).trim()) )); }
			else if(valueClass == Date.class)  		{ return this.changeValue(new Date(Long.parseLong( ((String)value).trim()) )); }
			else if(valueClass == Object[].class)	{ return this.changeValue( sanitizeString((String)value).split(",") ); }
			else if(valueClass == LinkedHashMap.class){ 
				LinkedHashMap<String,String> map = CFW.JSON.fromJsonLinkedHashMap((String)value);
				for(Entry<String,String> entry : map.entrySet()) {
					entry.setValue(sanitizeString((String)entry.getValue()));
				}
				return this.changeValue(map); 
			}
			
			else {
				new CFWLog(logger)
					.method("setValueConvert")
					.severe("The choosen type is not supported: "+valueClass.getName());
				return false;
			}
		}
		
		return success;
	}
	
	/******************************************************************************************************
	 * Validate the value using the assigned validators.
	 * Set the value by converting it to the correct type.
	 * 
	 * @param value the new value
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	public boolean setValueValidated(T value) {
		
		boolean result = true;
		
		//--------------------------------
		// Decryption
		if(this.encryptionSalt != null
		&& value != null
		&& value.toString().startsWith(ENCRYPT_PREFIX)) {
			value = decryptValue(value);
		}
		
		//--------------------------------
		// Resolve Password Field
		if(type == FormFieldType.PASSWORD
		&& value != null
		&& value.toString().startsWith(PASSWORD_STUB_PREFIX)) {
			String retrievedPW;
			try {
				retrievedPW = pwCache.get(value.toString(), new Callable<String>() {

					@Override
					public String call() throws Exception {
						throw new Exception();
					}
				});
			} catch (Throwable e) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error handling password field. Refresh the page and try again.");
				return false;
			}

			value = (T)retrievedPW;
		}
		
		//--------------------------------
		// Do Validated
		if(this.validateValue(value)) {
			result = this.setValueConvert(value);
		}else {
			result = false;
			if(invalidMessages != null) {
				for(String message : invalidMessages) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, message);
				}
			}
		}
		return result;
	}
	
	/******************************************************************************************************
	 * Set the values without validation except for the assigned CFWFieldChangeHandler.
	 * Does not convert the value.
	 * @param value the new value
	 * @return instance of chaining
	 * 
	 ******************************************************************************************************/
	public CFWField<T> setValue(T value) {
		this.changeValue(value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return (T)value;
	}
	
	/******************************************************************************************************
	 * Returns the class of the value.
	 * 
	 * @return class
	 ******************************************************************************************************/
	public Class<T> getValueClass() {
		return valueClass;
	}

	/******************************************************************************************************
	 * Returns the changeHandler.
	 * 
	 * @return CFWFieldChangeHandler<?> 
	 ******************************************************************************************************/
	public CFWFieldChangeHandler<?> getChangeHandler() {
		return changeHandler;
	}
	
	/******************************************************************************************************
	 * Add a change handler. Will be executed when the value is changed.
	 * The change handler can prevent the change of the value by returning false.
	 * 
	 * @param changeHandler
	 * @return instance of chaining
	 ******************************************************************************************************/
	public CFWField<T> setChangeHandler(CFWFieldChangeHandler<?> changeHandler) {
		this.changeHandler = changeHandler;
		return this;
	}
	
	/******************************************************************************************************
	 * Returns the changeHandler.
	 * 
	 * @return CFWFieldChangeHandler<?> 
	 ******************************************************************************************************/
	public CFWAutocompleteHandler getAutocompleteHandler() {
		return autocompleteHandler;
	}
	
	/******************************************************************************************************
	 * Add a change handler. Will be executed when the value is changed.
	 * The change handler can prevent the change of the value by returning false.
	 * 
	 * @param autocompleteHandler
	 * @return instance of chaining
	 ******************************************************************************************************/
	public CFWField<T> setAutocompleteHandler(CFWAutocompleteHandler autocompleteHandler) {
		autocompleteHandler.setParent(this);
		this.autocompleteHandler = autocompleteHandler;
		return this;
	}

	/******************************************************************************************************
	 * Map the values of request parameters to CFWFields.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean mapAndValidateParamsToFields(HttpServletRequest request, HashMap<String,CFWField<?>> fields) {
		
		Enumeration<String> parameters = request.getParameterNames();
		boolean result = true;
		
		while(parameters.hasMoreElements()) {
			String key = parameters.nextElement();
			
			if(!key.equals(CFWForm.FORM_ID)) {
				if (fields.containsKey(key)) {
					CFWField field = fields.get(key);
					
					if(!field.setValueValidated(request.getParameter(key)) ){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.method("mapAndValidateParamsToFields")
						.silent(true)
						.finest("The field with name '"+key+"' is unknown for this type.");
				}
			}
		}
		
		return result;
	}
	
	/******************************************************************************************************
	 * Map the values of the JsonObject to CFWFields.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean mapAndValidateJsonToFields(JsonObject json, HashMap<String,CFWField<?>> fields) {
		
		Set<String> members = json.keySet();
		boolean result = true;
		
		for(String key : members) {

			if(!key.equals(CFWForm.FORM_ID)) {
				if (fields.containsKey(key)) {
					CFWField field = fields.get(key);
					
					JsonElement element = json.get(key);
					
					if(element.isJsonNull()) {
						if(!field.setValueValidated(null) ){
							result = false;
						}
					}else if( element.isJsonObject() || element.isJsonArray() ){
						if(!field.setValueValidated(CFW.JSON.gsonInstance.toJson(element)) ){
							result = false;
						}
					}
					else if(!field.setValueValidated(element.getAsString()) ){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.method("mapAndValidateJsonToFields")
						.silent(true)
						.finest("The field with name '"+key+"' is unknown for this type.");
				}
			}
		}
		
		return result;
	}
	
	/******************************************************************************************************
	 * Map the values of request parameters to CFWFields.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean mapResultSetColumnsToFields(ResultSet result, HashMap<String,CFWField<?>> fields) {
		
		ResultSetMetaData metadata;
		boolean success = true;
		try {
			
			if(result == null) {
				return false;
			}
			//--------------------------------------
			// Check has results
			if(result.isBeforeFirst()) {
				result.next();
			}
			metadata = result.getMetaData();

			
			for(int i=1; i <= metadata.getColumnCount(); i++) {
				String colName = metadata.getColumnName(i);
				
				if(fields.containsKey(colName)) {
					CFWField current = fields.get(colName);
					
					if     ( String.class.isAssignableFrom(current.getValueClass()) )  { current.setValueValidated(result.getString(colName)); }
					else if( Integer.class.isAssignableFrom(current.getValueClass()))  { current.setValueValidated(result.getObject(colName)); }
					else if( Float.class.isAssignableFrom(current.getValueClass()))    { current.setValueValidated(result.getObject(colName)); }
					else if( Boolean.class.isAssignableFrom(current.getValueClass()))  { current.setValueValidated(result.getBoolean(colName)); }
					else if( Timestamp.class.isAssignableFrom(current.getValueClass()))  { current.setValueValidated(result.getTimestamp(colName)); }
					else if( Date.class.isAssignableFrom(current.getValueClass()))  { current.setValueValidated(result.getDate(colName)); }
					else if( Object[].class.isAssignableFrom(current.getValueClass()))  { 
						Array array = result.getArray(colName);
						if(array != null) {
							current.setValueValidated(result.getArray(colName).getArray()); 
						}else {
							current.setValueValidated(null);
						}
						
					}else if( LinkedHashMap.class.isAssignableFrom(current.getValueClass()))  { 
						String json = result.getString(colName);
						if(json != null) {
							current.setValueValidated(CFW.JSON.fromJsonLinkedHashMap(json)); 
						}else {
							current.setValueValidated(null);
						}
					}
					
				}else {
					success = false;
					new CFWLog(logger)
						.method("mapResultSetColumnsToFields")
						.silent(true)
						.finest("The object doesn't contain a field with name '"+colName+"'.");
				}
			}
		
		} catch (SQLException e) {
			success = false;
			new CFWLog(logger)
				.method("mapResultSetColumnsToFields")
				.severe("SQL Exception occured while trying to map ResultSet to fields. Check Cursor position.", e);
		}
		
		return success;
	}

		
}

package com.xresch.cfw.datahandling;

import java.math.BigDecimal;
import java.security.Key;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.db.CFWSQL.CFWSQLReferentialAction;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHandler;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.CFWHTMLItem;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.BooleanValidator;
import com.xresch.cfw.validation.DoubleValidator;
import com.xresch.cfw.validation.EpochOrTimeValidator;
import com.xresch.cfw.validation.FloatValidator;
import com.xresch.cfw.validation.IValidatable;
import com.xresch.cfw.validation.IValidator;
import com.xresch.cfw.validation.IntegerValidator;
import com.xresch.cfw.validation.LongValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWField<T> extends CFWHTMLItem implements IValidatable<T> {
	
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
	private CFWObject relatedCFWObject;
	private boolean lastValidationResult = false;
	private String name = "";
	private Object value;
	private String description = null;

	//--------------------------------
	// Validation and Encryption
	private ArrayList<String> invalidMessages;
	private byte[] encryptionSalt = null;
	private boolean allowHTML = false;
	private boolean sanitizeStrings = true;
	private boolean preventFormSubmitOnEnter = true;
	private ArrayList<IValidator> validatorArray;

	//--------------------------------
	// Handlers
	@SuppressWarnings("rawtypes")
	private CFWFieldChangeHandler changeHandler = null;
	private CFWAutocompleteHandler autocompleteHandler = null;
	
	//--------------------------------
	// Form and Display
	private Class<T> valueClass;
	private Class valueSubtypeClass; // used when valueClass is a generic type, like ArrayList<Foo>, valueSubtypeClass would be "Foo"
	private FormFieldType type;
	private FormFieldType apiFieldType;
	private boolean isDecoratorDisplayed = true;
	private String formLabel = "&nbsp;";
	@SuppressWarnings("rawtypes")
	private HashMap valueLabelOptions = null;
	private boolean isDisabled = false;
	
	//--------------------------------------------------
	// IMPORTANT!!! Do not change the names of these
	// enums, they are put into the database sometimes
	public enum FormFieldType{
		TEXT, 
		TEXTAREA, 
		PASSWORD, 
		NUMBER, 
		EMAIL, 
		HIDDEN, 
		BOOLEAN, 
		SELECT, 
		CHECKBOXES,
		LIST, 
		CUSTOM_LIST,
		WYSIWYG, 
		COLORPICKER,
		DATEPICKER, 
		DATETIMEPICKER, 
		TIMEFRAMEPICKER,
		TIMEZONEPICKER,
		FILEPICKER,
		TAGS, 
		// Input Order of elements messed up by client side when containing numbers in keys (numbers will be sorted and listed first)
		TAGS_SELECTOR,
		CHART_SETTINGS,
		QUERY_EDITOR,
		SCHEDULE, 
		LANGUAGE,
		UNMODIFIABLE_TEXT, 
		VALUE_LABEL,
		NONE
	}
	
	//--------------------------------
	// Flags
	
	public enum CFWFieldFlag{
		/*Used for example by Dashboard Widgets to determine if a setting should be sent to client side in json responses. */
		SERVER_SIDE_ONLY,
		/* Can be used for custom filtering */
		REMOVE,
		/* Can be used for custom filtering */
		KEEP,
		/*Used for general custom stuff */
		A, B, C, D, E, F, G
	}
	
	private EnumSet<CFWFieldFlag> flags;
		

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
		this.formLabel = CFW.Utils.Text.fieldNameToLabel(fieldName);
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
				.setColumnDefinition("VARCHAR_IGNORECASE");
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
	// Long
	//===========================================
	public static CFWField<Long> newLong(FormFieldType type, Enum<?> fieldName){
		return newLong(type, fieldName.toString());
	}
	public static CFWField<Long> newLong(FormFieldType type, String fieldName){
		return new CFWField<Long>(Long.class, type, fieldName)
				.setColumnDefinition("BIGINT")
				.addValidator(new LongValidator());
	}
	
	//===========================================
	// Float
	//===========================================
	public static CFWField<Float> newFloat(FormFieldType type, Enum<?> fieldName){
		return newFloat(type, fieldName.toString());
	}
	public static CFWField<Float> newFloat(FormFieldType type, String fieldName){
		return new CFWField<Float>(Float.class, type, fieldName)
				.setColumnDefinition("FLOAT")
				.addValidator(new FloatValidator());
	}
	
	//===========================================
	// BigDecimal
	//===========================================
	public static CFWField<BigDecimal> newBigDecimal(FormFieldType type, Enum<?> fieldName){
		return newBigDecimal(type, fieldName.toString());
	}
	public static CFWField<BigDecimal> newBigDecimal(FormFieldType type, String fieldName){
		return new CFWField<BigDecimal>(BigDecimal.class, type, fieldName)
				.setColumnDefinition("NUMERIC(64, 6)")
				.addValidator(new DoubleValidator());
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
	// BLOB
	//===========================================
	public static CFWField<String> newBlob(FormFieldType type, Enum<?> fieldName){
		return newBlob(type, fieldName.toString());
	}
	
	public static CFWField<String> newBlob(FormFieldType type, String fieldName){
		return new CFWField<String>(Boolean.class, type, fieldName)
				.setColumnDefinition("BLOB")
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
	public static CFWField<ArrayList<String>> newArray(FormFieldType type, Enum<?> fieldName){
		return newArray(type, fieldName.toString());
	}
	
	public static CFWField<ArrayList<String>> newArray(FormFieldType type, String fieldName){
		return new CFWField<ArrayList<String>>(ArrayList.class, type, fieldName)
				.setColumnDefinition("VARCHAR ARRAY")
				.setValueSubtype(String.class);
	}
	
	//===========================================
	// ArrayNumber
	//===========================================
	public static CFWField<ArrayList<Number>> newArrayNumber(FormFieldType type, Enum<?> fieldName){
		return newArrayNumber(type, fieldName.toString());
	}
	
	public static CFWField<ArrayList<Number>> newArrayNumber(FormFieldType type, String fieldName){
		return new CFWField<ArrayList<Number>>(ArrayList.class, type, fieldName)
				.setColumnDefinition("NUMERIC ARRAY")
				.setValueSubtype(Number.class);
	}
	
	/******************************************************************************************************
	 * Set the subtype of the value.
	 * This is needed in case of Generic classes like ArrayList<T>.
	 ******************************************************************************************************/
	private CFWField<T> setValueSubtype(Class subtype) {
		this.valueSubtypeClass = subtype;
		return this;
	}
	
	
	//===========================================
	// JSON_ PREFIX CHECK 
	//===========================================
	private static boolean fieldnameStartsWithJSON(String fieldname) {
		if(!fieldname.startsWith("JSON_")) {
			new CFWLog(logger)
				.severe("Development Error: Fieldname of this field type have to start with 'JSON_'.", new InstantiationException());
			return false;
		}
		
		return true;
	}
	
	
	//===========================================
	// TAGS SELECTOR
	//===========================================
	public static CFWField<LinkedHashMap<String,String>> newTagsSelector(Enum<?> fieldName){
		return newTagsSelector(fieldName.toString());
	}
	
	public static CFWField<LinkedHashMap<String,String>> newTagsSelector(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<LinkedHashMap<String,String>> (LinkedHashMap.class, FormFieldType.TAGS_SELECTOR, fieldName)
				.setColumnDefinition("VARCHAR");
		}
		return null;
	}
	
	//===========================================
	// CustomList
	//===========================================
	public static CFWField<ArrayList<String>> newCustomList(Enum<?> fieldName){
		return newCustomList(fieldName.toString());
	}
	
	public static CFWField<ArrayList<String>> newCustomList(String fieldName){
		return new CFWField<ArrayList<String>>(ArrayList.class, FormFieldType.CUSTOM_LIST, fieldName)
				.setColumnDefinition("ARRAY")
				.setValueSubtype(String.class);
	}
	
	//===========================================
	// VALUE LABEL
	//===========================================
	public static CFWField<LinkedHashMap<String,String>> newValueLabel(Enum<?> fieldName){
		return newValueLabel(fieldName.toString());
	}
	
	public static CFWField<LinkedHashMap<String,String>> newValueLabel(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<LinkedHashMap<String,String>> (LinkedHashMap.class, FormFieldType.VALUE_LABEL, fieldName)
				.setColumnDefinition("VARCHAR");
		}
		return null;
	}
	
	//===========================================
	// VALUE LABEL
	//===========================================
	public static CFWField<LinkedHashMap<String,String>> newCheckboxes(Enum<?> fieldName){
		return newCheckboxes(fieldName.toString());
	}
	
	public static CFWField<LinkedHashMap<String,String>> newCheckboxes(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<LinkedHashMap<String,String>> (LinkedHashMap.class, FormFieldType.CHECKBOXES, fieldName)
				.setColumnDefinition("VARCHAR");
		}
		return null;
	}
	
	//===========================================
	// FILEPICKER
	//===========================================
	public static CFWField<CFWStoredFileReferences> newFilepicker(Enum<?> fieldName){
		return newFilepicker(fieldName.toString());
	}
	
	public static CFWField<CFWStoredFileReferences> newFilepicker(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<CFWStoredFileReferences>(CFWStoredFileReferences.class, FormFieldType.FILEPICKER, fieldName)
					.setColumnDefinition("VARCHAR");
		}
		return null;
		
	}
	
	//===========================================
	// CHART SETTINGS
	//===========================================
	public static CFWField<CFWChartSettings> newChartSettings(Enum<?> fieldName){
		return newChartSettings(fieldName.toString());
	}
	public static CFWField<CFWChartSettings> newChartSettings(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<CFWChartSettings> (CFWChartSettings.class, FormFieldType.CHART_SETTINGS, fieldName)
					.setColumnDefinition("VARCHAR");
		}
		return null;
		
	}
	
	//===========================================
	// SCHEDULE
	//===========================================
	public static CFWField<CFWSchedule> newSchedule(Enum<?> fieldName){
		return newSchedule(fieldName.toString());
	}
	public static CFWField<CFWSchedule> newSchedule(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<CFWSchedule> (CFWSchedule.class, FormFieldType.SCHEDULE, fieldName)
					.setColumnDefinition("VARCHAR");
		}
		return null;
		
	}
	
	//===========================================
	// TIMEFRAME
	//===========================================
	public static CFWField<CFWTimeframe> newTimeframe(Enum<?> fieldName){
		return newTimeframe(fieldName.toString());
	}
	public static CFWField<CFWTimeframe> newTimeframe(String fieldName){
		if( fieldnameStartsWithJSON(fieldName) ) {
			return new CFWField<CFWTimeframe> (CFWTimeframe.class, FormFieldType.TIMEFRAMEPICKER, fieldName)
					.setColumnDefinition("VARCHAR");
		}
		return null;
		
	}
		
	
	//###########################################################################################################
	//###########################################################################################################
	// HTML and Form Methods
	//###########################################################################################################
	//###########################################################################################################
		
	/***********************************************************************************
	 * Prepares the final form field and returns the type of the form field.
	 * This checks if the field is used in an empty form(e.g. for API) and hence removed the 
	 * default values. 
	 * Also it checks if there is a different form field type defined for API forms.
	 * 
	 * @return FormFieldType
	 ***********************************************************************************/
	@SuppressWarnings("unchecked")
	private FormFieldType prepareFinalFormField() {
		//---------------------------------------------
		// Check Type
		//---------------------------------------------
		FormFieldType formFieldType = this.type;
		if(this.parent instanceof CFWForm) {
			
			if(this.valueLabelOptions != null
			&& (
					((CFWForm)this.parent).isAPIForm() 
				||	((CFWForm)this.parent).isEmptyForm() 
				)
			) {
				valueLabelOptions.put("", "");
			}
			
			// Use normal fieldType if apiFieldType is not defined
			if(((CFWForm)this.parent).isAPIForm() && this.apiFieldType != null) {
				formFieldType = this.apiFieldType;
			}
			
			if ( ((CFWForm)this.parent).isEmptyForm() && !this.name.contentEquals("cfw-formID") ){
				// Set Value to null
				value = null;
			}
		}
		return formFieldType;
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @param StringBuilder to append the resulting html
	 * @return String html for this item. 
	 ***********************************************************************************/
	@SuppressWarnings("unchecked")
	protected void createHTML_LabeledFormField(CFWForm form, StringBuilder html) {
		//---------------------------------------------
		// Check Type
		//---------------------------------------------
		FormFieldType formFieldType = prepareFinalFormField();
		
		//---------------------------------------------
		// Create Form Group
		//---------------------------------------------
		if(formFieldType != FormFieldType.HIDDEN && formFieldType != FormFieldType.NONE) {
			
			if(!form.isInlineForm()) {
				//---------------------------------------
				// Create Group and Label
				html.append("<div class=\"form-group row ml-1\">");
				html.append("  <label class=\"col-sm-3 col-form-label\" for=\""+name+"\" >");
				html.append(formLabel+":</label> ");
				
				html.append("  <div class=\"col-sm-9 d-flex\">");
			}else {
				//---------------------------------------
				// Create Inline Group and Label
				html.append("<div class=\"d-flex flex-column align-items-start ml-2 mb-1 flex-grow-1\">");
				html.append("  <label class=\"\" for=\""+name+"\" >");
				html.append(formLabel+":</label> ");
				
				if(this.fieldType() != FormFieldType.BOOLEAN) {
					this.addCssClass("w-100");
				}
				//html.append("  <div class=\"col-sm-9 d-flex\">");
			}
						
		}
		
		//---------------------------------------------
		// Create Form Field
		//---------------------------------------------
		createHTML(html);
		
		//---------------------------------------------
		// Close form-group and col-sm-9
		//---------------------------------------------
		if(formFieldType != FormFieldType.HIDDEN && formFieldType != FormFieldType.NONE) {
			html.append("</div>");
			if(!form.isInlineForm()) {
				html.append("</div>");
			}
		}
	}
	
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
		FormFieldType finalFieldType = prepareFinalFormField();
				
		//---------------------------------------------
		// Set Attributes
		//---------------------------------------------
		this.addAttribute("placeholder", formLabel);
		this.addAttribute("name", name);
		this.addAttribute("cfwtype", this.type.toString());
		
		if(isDisabled) {	this.addAttribute("disabled", "disabled");};
		
		if(preventFormSubmitOnEnter 
		&& finalFieldType != FormFieldType.TEXTAREA
		&& finalFieldType != FormFieldType.TAGS
		&& finalFieldType != FormFieldType.TAGS_SELECTOR) {	
			this.addAttribute("onkeydown", "return event.key != 'Enter';");
		};
		
		if(value != null) {	
			
			if(value instanceof BigDecimal) {
				this.addAttribute("value", ((BigDecimal) value).stripTrailingZeros().toPlainString() ); 
			}else if( !(value instanceof ArrayList) ) {
				this.addAttribute("value", value.toString().replace("\"", "&quot;")); 
			}else {
				StringBuilder builder = new StringBuilder();
				ArrayList<Object> array = (ArrayList<Object>)value;
				for(Object current : array) {
					builder.append(current.toString()).append(",");
				}
				if(array.size() > 0) {
					builder.deleteCharAt(builder.length()-1);
				}
				this.addAttribute("value", builder.toString().replace("\"", "&quot;")); 
			}
			
		};
		

		//---------------------------------------------
		// Decorators and Autcomplete
		//---------------------------------------------
		boolean hasAutocomplete = false;
		if(finalFieldType != FormFieldType.HIDDEN 
		&& finalFieldType != FormFieldType.NONE) {
			
			hasAutocomplete = ( 
								   autocompleteHandler != null 
								&& this.parent instanceof CFWForm 
							  )
						   || ( 
								   finalFieldType.toString().startsWith("TAGS") 
								&& valueLabelOptions != null
							  );
			
			//---------------------------------------
			// Create Field Wrapper
			String autocompleteClass = (autocompleteHandler == null)  ? "" : "cfw-autocomplete";
			html.append("<div class=\"cfw-field-wrapper "+autocompleteClass+"\">");
			
			//---------------------------------------
			// Check if Description available
			createDecoratorArea(html, finalFieldType);

			//---------------------------------------
			// Autocomplete Wrapper
			if (hasAutocomplete) {
				html.append("  <div class=\"cfw-autocomplete\">");
			}
		}


		//---------------------------------------------
		// Create Field
		//---------------------------------------------
		String cssClasses = this.getAttributeValue("class");
		this.removeAttribute("class");
		this.addAttribute("id", name);
		switch(finalFieldType) {
			case TEXT:  			html.append("<input type=\"text\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
									break;
			
			case NUMBER:  			html.append("<input type=\"number\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
									break;
			
			case TEXTAREA: 			createTextArea(html, cssClasses);
									break;
			
			case UNMODIFIABLE_TEXT: String label = this.getAttributeValue("value");
									html.append("<span class=\"d-flex align-items-center "+cssClasses+"\" "+this.getAttributesString()+">"+label+"</span>");
									html.append("<input type=\"hidden\" "+this.getAttributesString()+"/>");
									break;
			
			case SELECT:  			createSelect(html, cssClasses);
									break;	
			
			case CHECKBOXES:		createCheckboxField(html, cssClasses);
									break;	
			
			case HIDDEN:  			html.append("<input type=\"hidden\" "+this.getAttributesString()+"/>");
									break;
			
			case BOOLEAN:  			createBooleanSwitch(html, cssClasses);
									break;	
									
			case CUSTOM_LIST:		createCustomListField(html, cssClasses);
									break;	
									
			case VALUE_LABEL:		createValueLabelField(html, cssClasses);
									break;				
									
			case WYSIWYG: 			createWYSIWYG(html, cssClasses);
									break;						
			
			case LIST:  			createList(html, cssClasses);
									break;
									
			case EMAIL:  			html.append("<input type=\"email\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
									break;
								
			case COLORPICKER:  		createColorPicker(html, cssClasses);
									break;
			
			case DATEPICKER:  		createDatePicker(html, cssClasses);
									break;
			
			case DATETIMEPICKER:  	createDateTimePicker(html, cssClasses);
									break;
									
			case TIMEFRAMEPICKER:	createTimeframePicker(html, cssClasses);
									break;
									
			case TIMEZONEPICKER: 	createTimezoneSelect(html, cssClasses);
									break;	
									
			case FILEPICKER:		createFilePicker(html, cssClasses);
									break;
			
			case SCHEDULE:		  	createSchedule(html, cssClasses);
									break;
			
			case CHART_SETTINGS:	createChartSettings(html, cssClasses);
									break;
			
			case QUERY_EDITOR:		createQueryEditor(html, cssClasses);
									break;
			
			case LANGUAGE:  		createLanguageSelect(html, cssClasses);
									break;	
									
			case TAGS:			  	createTagsField(html, cssClasses+" cfw-tags", FormFieldType.TAGS);
									break;
									
			case TAGS_SELECTOR:		createTagsField(html, cssClasses+" cfw-tags-selector", FormFieldType.TAGS_SELECTOR);
									break;						
									
			case PASSWORD:  		createPasswordField(html, cssClasses);
									break;
			
			case NONE:				//do nothing
									break;
			
		}
		
		//---------------------------------------------
		// Add Autocomplete Initialization
		//---------------------------------------------
		if( hasAutocomplete
		&& finalFieldType != FormFieldType.QUERY_EDITOR) { // query editor initializes itself
			
			html.append("</div>");
			CFWForm form = (CFWForm)this.parent;
			String formID = form.getFormID();
			
			if(this.autocompleteHandler != null) {
				int maxResults = this.getAutocompleteHandler().getMaxResults();
				int minChars = this.getAutocompleteHandler().getMinChars();
				form.javascript.append("cfw_autocompleteInitialize('"+formID+"','"+name+"',"+minChars+","+maxResults+");\r\n");
			}else {
				
				// cfw_autocompleteInitialize(formID, fieldName, minChars, maxResults, array, triggerWithCtrlSpace, target);
				JsonArray array = CFW.JSON.fromHashMapToJsonArray(valueLabelOptions);
				form.javascript.append("cfw_autocompleteInitialize('"+formID+"', '"+name+"', 1, 30, "+array+");\r\n");

			}
			
		}
		//---------------------------------------------
		// Close Field Wrapper
		//---------------------------------------------
		if(finalFieldType != FormFieldType.HIDDEN && finalFieldType != FormFieldType.NONE) {
			html.append("</div>");
		}
	}

	/***********************************************************************************
	 * Creates the decorator area for the field.
	 * This method will do nothing in case decorator was disabled using {@link #isDecoratorDisplayed()}
	 * @param finalFieldType as returned by {@link #prepareFinalFormField()}
	 ***********************************************************************************/
	public void createDecoratorArea(StringBuilder html, FormFieldType finalFieldType) {
		
		//---------------------------------------
		// Check if Description available
		if(isDecoratorDisplayed) {
			html.append("<div class=\"cfw-decorator-area\">");
				if(description != null 
				&& !description.isEmpty() 
				&& finalFieldType != FormFieldType.HIDDEN 
				&& finalFieldType != FormFieldType.NONE
				&& finalFieldType != FormFieldType.UNMODIFIABLE_TEXT
				) {
					html.append("<span class=\"badge badge-info cfw-decorator\" data-toggle=\"tooltip\" data-placement=\"top\" data-html=\"true\" data-delay=\"500\""
									+ " title=\""
									+ CFW.Security.sanitizeHTML(description)
										.replaceAll("\"", "&nbsp;")
										.replaceAll("\n", "<br>")
									+ "\"><i class=\"fa fa-sm fa-info\"></i></span>");
				}
			html.append("</div>");
		}
	}
	
	/***********************************************************************************
	 * Create Boolean Radio Buttons
	 ***********************************************************************************/
	private void createBooleanSwitch(StringBuilder html, String cssClasses) {
				
		String disabled = "";
		if(isDisabled) {	disabled = "disabled=\"disabled\""; };
		
		String originalID = this.getAttributeValue("id");
		this.removeAttribute("id");
		//if(originalID != null) { this.addAttribute("id", originalID+"-TRUE");}
	
		html.append(
			"<input type=\"text\" name=\""+name+"\" id=\""+originalID+"\"  "+this.getAttributesString()+" "+disabled+">"
		);
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_internal_initializeBooleanSwitch('"+originalID+"');\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create Select
	 ***********************************************************************************/
	private void createSelect(StringBuilder html, String cssClasses) {
		
		String id = this.getAttributeValue("id");
		
		//-----------------------------------
		// Set Selected Value
		String selectedValue = "";
		if(value != null) {
			selectedValue = value.toString();
		}else if (valueLabelOptions != null && !valueLabelOptions.isEmpty()) {
			selectedValue = valueLabelOptions.keySet().toArray()[0].toString();
		}	
		
		this.addAttribute("value", selectedValue);
		
		//-----------------------------------
		// Create Options Array
		JsonArray optionsArray = new JsonArray();
		if(valueLabelOptions != null) {
			
			for(Object optionValue : valueLabelOptions.keySet()) {
				String currentLabel = valueLabelOptions.get(optionValue).toString();
				String stringValue = (optionValue == null) ? "" : optionValue.toString();
				
				JsonObject object = new JsonObject();
				object.addProperty("value", stringValue);
				object.addProperty("label", currentLabel);
				optionsArray.add(object);
			}
		}
		
		//-----------------------------------
		// Create Select 
		html.append("<input class=\""+cssClasses+"\" "+this.getAttributesString()+" >");
		if(this.parent instanceof CFWForm) {
			String arrayString = CFW.JSON.toJSON(optionsArray);

			((CFWForm)this.parent).javascript.append("cfw_initializeSelect('"+id+"', "+arrayString+", true);\r\n");
		}
		
	}
	
//	/***********************************************************************************
//	 * Create Select
//	 ***********************************************************************************/
//	private void createSelect(StringBuilder html, String cssClasses) {
//		
//		this.removeAttribute("value");
//		
//		//-----------------------------------
//		// Set Selected Value
//		String selectedValue = "";
//		if(value != null) {
//			selectedValue = value.toString();
//		}else if (valueLabelOptions != null && !valueLabelOptions.isEmpty()) {
//			selectedValue = valueLabelOptions.keySet().toArray()[0].toString();
//		}						
//		
//		//-----------------------------------
//		// Create Select 
//		html.append("<select class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+" >");
//		
//		if(valueLabelOptions != null) {
//			
//			for(Object optionValue : valueLabelOptions.keySet()) {
//				
//				String currentLabel = valueLabelOptions.get(optionValue).toString();
//				String stringValue = (optionValue == null) ? "" : optionValue.toString();
//				String stringValueNoQuotes = stringValue.replaceAll("\"", "&quot;");
//				
//				if(stringValue.equals(selectedValue)) {
//					
//					html.append("<option value=\""+stringValueNoQuotes +"\" selected>")
//					.append(currentLabel)
//					.append("</option>");
//				}else {
//					html.append("<option value=\""+stringValueNoQuotes+"\">")
//					.append(currentLabel)
//					.append("</option>");
//				}
//			}
//		}
//		
//		html.append("</select>");
//	}
	
	/***********************************************************************************
	 * Create Select
	 ***********************************************************************************/
	private void createLanguageSelect(StringBuilder html, String cssClasses) {
		
		this.removeAttribute("value");
		
		String stringVal = (value == null) ? "" : value.toString();
		
		html.append("<select class=\""+cssClasses+"\" "+this.getAttributesString()+" >");
		
		//-----------------------------------
		// handle options
		String languages[] = Locale.getISOLanguages();
		Locale userLocale = CFW.Localization.getUsersPreferredLocale();
		
		TreeMap<String, String> sortedByLocale = new TreeMap<>();
		for(String optionValue : languages) {
			
			String currentLabel = new Locale(optionValue).getDisplayLanguage(userLocale);
			
			sortedByLocale.put(currentLabel, optionValue.toUpperCase());
			
		}
		

		//Add empty option
		html.append("<option value=\"\">&nbsp;</option>");
		
		for(Entry<String, String> entry : sortedByLocale.entrySet()) {
			
			String currentLabel = entry.getKey();
			String optionValue = entry.getValue();
			
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
		
		
		html.append("</select>");
	}
	
	/***********************************************************************************
	 * Create List
	 ***********************************************************************************/
	private void createList(StringBuilder html, String cssClasses) {
		
		html.append("<input list=\"list-"+name+"\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");

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
	 * Create WYSIWYG
	 ***********************************************************************************/
	private void createWYSIWYG(StringBuilder html, String cssClasses) {
		
		//---------------------------------
		// Set initial value
		this.removeAttribute("value");
		this.addAttribute("id", name);
		//---------------------------------
		// Create Field
		html.append("<textarea class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"></textarea>");

		
		if(this.parent instanceof CFWForm) {
			CFWForm form = ((CFWForm)this.parent);
			
			form.javascript.append("cfw_initializeSummernote('"+form.getFormID()+"', '"+name+"');\r\n");
		}

	}
	
	/***********************************************************************************
	 * Create Color Picker
	 ***********************************************************************************/
	private void createColorPicker(StringBuilder html, String cssClasses) {
				
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"hidden\" data-role=\"colorpicker\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeColorPickerField('"+name+"', '"+value+"');\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createDatePicker(StringBuilder html, String cssClasses) {
		
		//---------------------------------
		// Set initial value
		String epochTime = this.toEpochTimeString(this.value);
		this.addAttribute("value", epochTime);
		
		//---------------------------------
		// Create Field
		
		html.append("<input id=\""+name+"-datepicker\" type=\"date\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"form-control "+cssClasses+"\" placeholder=\"Date\" >\r\n" + 
				"<input id=\""+name+"\" type=\"hidden\" class=\"form-control\" "+this.getAttributesString()+">\r\n");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeTimefield('"+name+"', "+epochTime+");\r\n");
		}

	}
	

	
	/***********************************************************************************
	 * Create DateTimePicker
	 ***********************************************************************************/
	private void createDateTimePicker(StringBuilder html, String cssClasses) {
		
		//---------------------------------
		// Set initial value
		String epochTime = this.toEpochTimeString(this.value);
		this.addAttribute("value", epochTime);
		
		//---------------------------------
		// Create Field
		html.append("  <div class=\"custom-control-inline w-100 mr-0 "+cssClasses+"\">\r\n"
						+ "<input id=\""+name+"-datepicker\" type=\"date\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"col-md-9 form-control\" >\r\n"
						+ "<input id=\""+name+"-timepicker\" type=\"time\" onchange=\"cfw_updateTimeField('"+name+"')\" class=\"col-md-3 form-control\">"
						+ "<input id=\""+name+"\" type=\"hidden\" class=\"form-control\" "+this.getAttributesString()+">\r\n" 
					+ "</div>\r\n");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeTimefield('"+name+"', "+epochTime+");\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create TimeframePicker
	 ***********************************************************************************/
	private void createTimeframePicker(StringBuilder html, String cssClasses) {
				
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"hidden\" data-role=\"timeframepicker\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeTimeframePicker('"+name+"', "+CFW.JSON.toJSON(value)+", null);\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create FilePicker
	 ***********************************************************************************/
	private void createFilePicker(StringBuilder html, String cssClasses) {
				
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"hidden\" data-role=\"filepicker\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			boolean isMultiple = false; 
			((CFWForm)this.parent).javascript.append("cfw_initializeFilePicker('"+name+"', "+isMultiple+",   "+CFW.JSON.toJSON(value)+", null);\r\n");
		}
				
	}
	

	/***********************************************************************************
	 * Create Timezone Select
	 ***********************************************************************************/
	private void createTimezoneSelect(StringBuilder html, String cssClasses) {
		
		this.removeAttribute("value");
		
		String stringVal = (value == null) ? "" : value.toString();
		
		html.append("<select class=\""+cssClasses+"\" "+this.getAttributesString()+" >");
		
		//-----------------------------------
		// handle options
		String timezones[] = TimeZone.getAvailableIDs();
		Locale userLocale = CFW.Localization.getUsersPreferredLocale();
		
		TreeMap<String, String> sortedByDisplayName = new TreeMap<>();
		for(String optionValue : timezones) {
			
			String currentLabel = TimeZone.getTimeZone(optionValue).getDisplayName(userLocale);
			
			sortedByDisplayName.put(currentLabel, optionValue);
			
		}
		
		//Add empty option
		html.append("<option value=\"\">&nbsp;</option>");
		
		for(Entry<String, String> entry : sortedByDisplayName.entrySet()) {
			
			String currentLabel = entry.getKey();
			String optionValue = entry.getValue();
			
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
		
		html.append("</select>");
	}
	
	/***********************************************************************************
	 * Create Schedule
	 ***********************************************************************************/
	private void createChartSettings(StringBuilder html, String cssClasses) {
				
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"hidden\" data-role=\"chartsettings\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeChartSettingsField('"+name+"', "+CFW.JSON.toJSON(value)+");\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create Query Editor
	 ***********************************************************************************/
	private void createQueryEditor(StringBuilder html, String cssClasses) {
		
		this.removeAttribute("value");
		String inputValue = "";
		if(this.value != null) {
			inputValue = this.value.toString();
		}
		html.append("<textarea class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+">"+inputValue+"</textarea>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeQueryEditor('"+name+"');\r\n");
		}
		
		this.setAutocompleteHandler(new CFWQueryAutocompleteHandler());
	}
	
	/***********************************************************************************
	 * Create Schedule
	 ***********************************************************************************/
	private void createSchedule(StringBuilder html, String cssClasses) {
				
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"hidden\" data-role=\"schedule\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeScheduleField('"+name+"', "+CFW.JSON.toJSON(value)+");\r\n");
		}
				
	}
	
	
	/********************************************************************************************
	 * Used to get the epoch time as a string to create a time field.
	 * @return string
	 ********************************************************************************************/
	private String toEpochTimeString(Object value) {
		String epochTime = null;
		if(value != null) {
			
			if(value instanceof Date) {
				epochTime = ""+((Date)value).getTime();
				
			}else if(value instanceof Timestamp) {
				epochTime = ""+((Timestamp)value).getTime();
				this.addAttribute("value", ""+epochTime);
			}else {
				epochTime = value.toString();
				this.addAttribute("value", ""+value.toString());
			}
		}
		
		return epochTime;
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createTagsField(StringBuilder html, String cssClasses, FormFieldType type) {
		
		int maxTags = 128;
		
		if(attributes.containsKey("maxTags")) {
			maxTags = Integer.parseInt(attributes.get("maxTags"));
		}
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"text\" data-role=\"tagsinput\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			
			CFWForm form = (CFWForm)this.parent;
			//------------------------------------
			// Initialize Selector
			if(type.equals(FormFieldType.TAGS_SELECTOR)) {
				form.javascript.append("cfw_initializeTagsSelectorField('"+name+"', "+maxTags+", "+CFW.JSON.toJSON(value)+");\r\n");
			}else {
				form.javascript.append("cfw_initializeTagsField('"+name+"', "+maxTags+");\r\n");
			}
									
		}
		
	
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createValueLabelField(StringBuilder html, String cssClasses) {
		
//		int maxTags = 128;
//		
//		if(attributes.containsKey("maxTags")) {
//			maxTags = Integer.parseInt(attributes.get("maxTags"));
//		}
		
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"text\" data-role=\"valuelabel\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeValueLabelField('"+name+"', "+CFW.JSON.toJSON(value)+");\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createCustomListField(StringBuilder html, String cssClasses) {
		
//		int maxTags = 128;
//		
//		if(attributes.containsKey("maxTags")) {
//			maxTags = Integer.parseInt(attributes.get("maxTags"));
//		}
		
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"text\" data-role=\"customList\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		String stringValue = "[]";
		if(value instanceof ArrayList) {
			stringValue = CFW.JSON.toJSON(value);
		}else if(value != null) {
			stringValue = value.toString();
		}
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeCustomListField('"+name+"', "+stringValue+");\r\n");
		}
					
	}
	
	/***********************************************************************************
	 * Create DatePicker
	 ***********************************************************************************/
	private void createCheckboxField(StringBuilder html, String cssClasses) {
		
//		int maxTags = 128;
//		
//		if(attributes.containsKey("maxTags")) {
//			maxTags = Integer.parseInt(attributes.get("maxTags"));
//		}
		
		//---------------------------------
		// Create Field
		html.append("<input id=\""+name+"\" type=\"text\" data-role=\"checkboxes\" class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+"/>");
		
		if(this.parent instanceof CFWForm) {
			String optionsJSON = CFW.JSON.toJSON(this.getOptions());
			String valuesJSON = CFW.JSON.toJSON(value);
			((CFWForm)this.parent).javascript.append("cfw_initializeCheckboxesField('"+name+"',"+optionsJSON+", "+valuesJSON+");\r\n");
		}
				
	}
	
	/***********************************************************************************
	 * Create a text area
	 ***********************************************************************************/
	private void createTextArea(StringBuilder html, String cssClasses) {
		
		if(!this.attributes.containsKey("rows")) {
			this.addAttribute("rows", "5");
		}
		this.removeAttribute("value");
		String inputValue = "";
		if(this.value != null) {
			inputValue = this.value.toString();
		}
		html.append("<textarea class=\"form-control "+cssClasses+"\" "+this.getAttributesString()+">"+inputValue+"</textarea>");
		
		if(this.parent instanceof CFWForm) {
			((CFWForm)this.parent).javascript.append("cfw_initializeExpandableTextareaField('"+name+"');\r\n");
		}
	}
	
	/***********************************************************************************
	 * Create a password field.
	 ***********************************************************************************/
	private void createPasswordField(StringBuilder html, String cssClasses) {
		
		if(this.value != null && !value.toString().isEmpty()) {
			String placeholderName = PASSWORD_STUB_PREFIX + CFWRandom.stringAlphaNumSpecial(7);
			pwCache.put(placeholderName, this.value.toString());
			this.addAttribute("value", placeholderName);
		}
		html.append("<input type=\"password\" class=\"form-control "+cssClasses+"\" autocomplete=\"new-password\" "+this.getAttributesString()+"/>");

	}
	
	/***********************************************************************************
	 * Add an attribute to the html tag.
	 * Adding a value for the same attribute multiple times will overwrite preceding values.
	 * @param name the name of the attribute.
	 * @param key the key of the attribute.
	 * @return instance for chaining
	 ***********************************************************************************/
	@Override
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
	@Override
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
		return this.validateValue(value);
	}
	
	/*************************************************************************
	 * Executes all validators added to the instance of this class.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean validateValue(Object value){
		
		lastValidationResult=true;
		if(validatorArray != null) {
			invalidMessages = new ArrayList<String>();
			for(IValidator validator : validatorArray){
				
				if(!validator.validate(value)){
					invalidMessages.add(validator.getInvalidMessage());
					
					lastValidationResult=false;
				}
			}
		}
		
		return lastValidationResult;
	}
	
	/*************************************************************************
	 * Returns the result of the last validation.
	 * Returns false if the field was not validated.
	 * 
	 * @return true if all validators returned true, false otherwise
	 *************************************************************************/ 
	public boolean lastValidationResult() {
		return lastValidationResult;
	}
	
	/*************************************************************************
	 * Returns the messages for invalid fields of the last validation.
	 * 
	 * @return list of messages
	 *************************************************************************/ 
	public ArrayList<String> getInvalidationMessages() {
		if(invalidMessages == null) { return new ArrayList<>(); }
		return invalidMessages;
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
	
	
	protected CFWField setRelatedCFWObject(CFWObject object) {
		this.relatedCFWObject = object;
		return this;
	}
	/******************************************************************************************************
	 * Set the name of this field.
	 * Will be used as the name attribute of form elements and the name of the DB column.
	 * 
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setName(String name) {
		String oldname = this.name;
		this.name = name;
		if(relatedCFWObject != null) {
			relatedCFWObject.getFields().remove(oldname);
			relatedCFWObject.getFields().put(name, this);
		}
		return this;
	}
	
	/******************************************************************************************************
	 * 
	 * @return String the name of the field
	 ******************************************************************************************************/
	public String getName() {
		return name;
	}
	
	/******************************************************************************************************
	 * 
	 * @return String the label of the field
	 ******************************************************************************************************/	
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
	 * When values have already been stored in the database with encryption enabled, changing the salt 
	 * will cause all the stored values to be wrongly decrypted. Never change a salt once set.
	 * 
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
	public CFWField<T> disableSanitization() {
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
	public CFWField<T> setPrimaryKey(CFWObject parentObject) {
		
		this.columnDefinition = "INT PRIMARY KEY";
		if(this.valueClass == Integer.class) {
			parentObject.setPrimaryField((CFWField<Integer>)this);
		}
		return this;
	}
	/******************************************************************************************************
	 * Set the DB column definition of this field as a primary key with auto increment.
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
		this.setForeignKey(parent, foreignObject, foreignField, CFWSQLReferentialAction.CASCADE);
		return this;
	}
	
	public CFWField<T> setForeignKey(CFWObject parent, Class<? extends CFWObject> foreignObject, Object foreignFieldname, CFWSQLReferentialAction actionOnDelete) {
		parent.addForeignKey(this.getName(), foreignObject, foreignFieldname, actionOnDelete);
		return this;
	}
		
	@SuppressWarnings("rawtypes")
	public HashMap getOptions() {
		return valueLabelOptions;
	}
	
	/******************************************************************************************************
	 * Changes the type of this field.
	 * Useful to change to FormFieldType.NONE.
	 ******************************************************************************************************/
	public CFWField<T> fieldType(FormFieldType type) {
		this.type = type;
		return this;
	}
	
	/******************************************************************************************************
	 * Returns the form type of this field.
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
	 * Add an option for selection fields. The string representations of the provided parameters will be used. 
	 * The values will be used as labels for the options.
	 * 
	 * @param array with values
	 * @return instance for chaining
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	public CFWField<T> addOption(Object valueAndLabel) {		
		return this.addOption(valueAndLabel, valueAndLabel);
	}
	
	/******************************************************************************************************
	 * Add an option for selection fields. The string representations of the provided parameters will be used. 
	 * The values will be used as labels for the options.
	 * 
	 * @param array with values
	 * @return instance for chaining
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	public CFWField<T> addOption(Object value, Object label) {
		if(this.valueLabelOptions == null) {
			this.valueLabelOptions = new LinkedHashMap<Object,Object>();
		}
		
		this.valueLabelOptions.put(value, label);
		return this;
	}
	
	/******************************************************************************************************
	 * Adds options for selection fields. The string representations of the provided parameters will be used. 
	 * The values will be used as labels for the options.
	 * 
	 * @param map with value/label pairs
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> addOptions(HashMap valueLabelPairs) {
		if(this.valueLabelOptions == null) {
			this.valueLabelOptions = new LinkedHashMap<Object,Object>();
		}
		
		this.valueLabelOptions.putAll(valueLabelPairs);
		return this;
	}
	
	/******************************************************************************************************
	 * Adds options for selection fields. The string representations of the provided parameters will be used 
	 * for both the value and the label.
	 * 
	 * @param String array with options
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> addOptions(String ...options) {

		for(String option : options) {
			this.addOption(option, option);
		}
		return this;
	}

	/******************************************************************************************************
	 * Set values for selection fields. The string representations of the provided elements will be used. 
	 * The values will be used as labels for the options.
	 * This will reset any options set with setValueLabelOptions() or addOption()..
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
	 * Set values for selection fields. The string representations of the provided elements will be used. 
	 * The values will be used as labels for the options.
	 * This will reset any options set with setValueLabelOptions() or addOption()..
	 * 
	 * @param array with values
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setOptions(ArrayList<String> options) {
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
	 * Set values for selection fields. The string representations of the provided elements will be used. 
	 * The values will be used as labels for the options.
	 * This will reset any options set with setValueLabelOptions() or addOption()..
	 * 
	 * @param array with values
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> setOptions(Set<String> options) {
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
	public CFWField<T> setOptions(HashMap valueLabelPairs) {
		this.valueLabelOptions = valueLabelPairs;
		return this;
	}
	
	/******************************************************************************************************
	 * Adds the specified flags for this field.
	 * 
	 * @param flags the flags to set
	 * @return instance for chaining
	 ******************************************************************************************************/
	public CFWField<T> addFlags(EnumSet<CFWFieldFlag> flags) {
		if(this.flags == null) { 
			this.flags = flags;
		}else {
			this.flags.addAll(flags);
		}
		return this;
	}
	
	/******************************************************************************************************
	 * Checks if this field if flagged the specified flag.
	 * 
	 * @param set of flags
	 * @return true if all the given flags are specified for this field
	 ******************************************************************************************************/
	public CFWField<T> addFlag(CFWFieldFlag flag) {
		
		if(this.flags == null) { 
			this.flags = EnumSet.of(flag);
		}else {
			this.flags.add(flag);
		}
		return this;
	}
	
	/******************************************************************************************************
	 * Checks if this field if flagged the specified flag.
	 * 
	 * @param set of flags
	 * @return true if all the given flags are specified for this field
	 ******************************************************************************************************/
	public boolean hasFlag(CFWFieldFlag flag) {
		if(this.flags == null || flag == null) { return false;}
		return this.flags.contains(flag);
	}
	
	/******************************************************************************************************
	 * Checks if this field has one of the specified flags.
	 * 
	 * @param set of flags
	 * @return true if one or more of the given flags are specified for this field
	 ******************************************************************************************************/
	public boolean hasFlag(EnumSet<CFWFieldFlag> flags) {
		if(this.flags == null || flags == null) { return false; }
		for(CFWFieldFlag flag : flags) {
			
			if(this.flags.contains(flag)) {
				return true;
			}
		}
		
		return false;
	}
	
	/******************************************************************************************************
	 * Checks if this field has all the specified flags.
	 * 
	 * @param set of flags
	 * @return true if all the given flags are specified for this field
	 ******************************************************************************************************/
	public boolean hasFlags(EnumSet<CFWFieldFlag> flags) {
		if(this.flags == null) { return false;}
		return this.flags.containsAll(flags);
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
	 * Check if the decorator of the field is displayed.
	 * 
	 ******************************************************************************************************/
	public boolean isDecoratorDisplayed() {return isDecoratorDisplayed;}

	/******************************************************************************************************
	 * Toggle if the decorator of the field is displayed.
	 * 
	 ******************************************************************************************************/
	public CFWField<T> isDecoratorDisplayed(boolean isDecoratorDisplayed) {
		this.isDecoratorDisplayed = isDecoratorDisplayed;
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
	@SuppressWarnings("unchecked")
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
					.severe("Could not decrypt value.", e);
			}
			
			return (T)decryptedValue;
		}
    }
    
	/******************************************************************************************************
	 * Change the value by first converting it to the correct type.
	 * Value will not be validated.
	 * @param doSanitize TODO
	 * 
	 ******************************************************************************************************/
	public boolean setValueConvert(T value, boolean doSanitize) {
		boolean success = true;
				
		//--------------------------------
		// Decryption
		if(this.encryptionSalt != null
		&& value != null
		&& value.toString().startsWith(ENCRYPT_PREFIX)) {
			value = decryptValue(value);
		}
		
		//-------------------------------------------------
		// prevent Strings from being empty. Might lead to 
		// unique constraint violation on DB when not using 
		// null values. Do not trim to allow blank strings.
		if(value == null 
		|| ( valueClass == String.class && (value.toString().equals(""))) ) {
			return this.changeValue(null);
		}
		
		//-------------------------------------------------
		// If value is a subclass of the valueClass change 
		// the value without conversion
		if(this.valueClass.isAssignableFrom(value.getClass())) {
			//---------------------------------
			// Sanitize strings if needed 
			if(doSanitize && valueClass == String.class ) {
				return this.changeValue(sanitizeString((String)value));
			}else {
				return this.changeValue(value);
			}
		}
				
		//-------------------------------------------------
		// Convert string values to the appropriate type
		if(value.getClass() == String.class) {
			
			String stringValue = (String)value;
			if(stringValue.trim().equals("")) { 
				if(valueClass == Boolean.class) { return this.changeValue(false); }
				else {  return this.changeValue(null); }

			}
			else if(valueClass == Integer.class) 	{ return this.changeValue(Integer.parseInt(stringValue)); }
			else if(valueClass == BigDecimal.class) 		{ return this.changeValue(new BigDecimal(stringValue)); }
			else if(valueClass == Long.class) 		{ return this.changeValue( Long.valueOf(Long.parseLong(stringValue)) ); }
			else if(valueClass == Float.class) 		{ return this.changeValue( Float.valueOf(Float.parseFloat(stringValue)) ); }
			else if(valueClass == Boolean.class) 	{ return this.changeValue(Boolean.parseBoolean( (stringValue).trim()) ); }
			else if(valueClass == Timestamp.class)  { return this.changeValue(new Timestamp(Long.parseLong( (stringValue).trim()) )); }
			else if(valueClass == Date.class)  		{ return this.changeValue(new Date(Long.parseLong( (stringValue).trim()) )); }
			else if(valueClass == ArrayList.class)	{ 
				
				if(stringValue.trim().startsWith("[")) {
					if(this.valueSubtypeClass == String.class) {
						ArrayList<String> stringArray = new ArrayList<>();
						JsonElement element = CFW.JSON.fromJson(stringValue);
						if(element.isJsonArray()) {
							
							for(JsonElement current : element.getAsJsonArray()) {
								stringArray.add(sanitizeString(current.getAsString()));
							}
							
						}
						return  this.changeValue(stringArray);
						
					}else if(this.valueSubtypeClass == Number.class) {
						ArrayList<Number> stringArray = new ArrayList<>();
						JsonElement element = CFW.JSON.fromJson(stringValue);
						
						if(element.isJsonArray()) {
							for(JsonElement current : element.getAsJsonArray()) {
								stringArray.add(current.getAsNumber());
							}
						}
						
						return  this.changeValue(stringArray);
					}else {
						new CFWLog(logger).severe("Unsupported subtype for array: "+this.valueSubtypeClass, new Throwable());	
					}
				}else {
					ArrayList<String> stringArray = new ArrayList<>();
					for(String current : stringValue.trim().split(",")) {
						stringArray.add(sanitizeString(current));
					}
					return  this.changeValue(stringArray);
				}
				
				new CFWLog(logger).severe("Unable to convert ArrayList value: "+value, new Throwable());				
				return false;
			}
			
			else if(valueClass == LinkedHashMap.class){ 
				LinkedHashMap<String,String> map = CFW.JSON.fromJsonLinkedHashMap(stringValue);
				if(map == null) {
					return this.changeValue(map); 
				}
				for(Entry<String,String> entry : map.entrySet()) {
					entry.setValue(sanitizeString((String)entry.getValue()));
				}
				return this.changeValue(map); 
			}
			
			else if(valueClass == CFWSchedule.class){ 
				CFWSchedule schedule = new CFWSchedule(stringValue);
				return this.changeValue(schedule); 
			}
			
			else if(valueClass == CFWChartSettings.class){ 
				CFWChartSettings chartSettings = new CFWChartSettings(stringValue);
				return this.changeValue(chartSettings); 
			}

			else if(valueClass == CFWTimeframe.class){ 
				CFWTimeframe timeframe = new CFWTimeframe(stringValue);
				return this.changeValue(timeframe); 
			}
			
			else {
				new CFWLog(logger)
					.severe("The choosen type is not supported: "+valueClass.getName());
				return false;
			}
		}
		
		
		//-------------------------------------------------
		// Convert Arrays to String ArrayList
		if(value.getClass() == Object[].class) {

			if(this.valueSubtypeClass == String.class) {
				ArrayList<String> stringArray = new ArrayList<>();
				for(Object object : (Object[])value ){
					stringArray.add(object.toString());
				}
				return  this.changeValue(stringArray);
				
			}else if(this.valueSubtypeClass == Number.class) {
				ArrayList<Number> numberArray = new ArrayList<>();
				for(Object object : (Object[])value ){
					
					if(object instanceof Number) {
						numberArray.add((Number)object);
					}else if(object instanceof String) {
						numberArray.add(Double.parseDouble((String)object));
					}else {
						new CFWLog(logger).severe("Unexpected type for array values: "+object.getClass());	
						return false;
					}
				}
				return  this.changeValue(numberArray);
			}else {
				new CFWLog(logger).severe("Unsupported subtype for array: "+this.valueSubtypeClass, new Throwable());	
				return false;
			}
			
		}
		
		//-------------------------------------------------
		// Convert String
		if(valueClass == String.class) 	{ 
			return this.changeValue(value.toString() );  
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
			} catch (Exception e) {
				CFW.Messages.addErrorMessage("Error handling password field. Refresh the page and try again.");
				return false;
			}

			value = (T)retrievedPW;
		}
		
		//--------------------------------
		// Do Validated
		if(this.validateValue(value)) {
			result = this.setValueConvert(value, true);
		}else {
			result = false;
			if(invalidMessages != null) {
				for(String message : invalidMessages) {
					CFW.Messages.addErrorMessage(message);
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
	@SuppressWarnings("rawtypes")
	public CFWFieldChangeHandler getChangeHandler() {
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
	 * Maps and validates the values of request parameters to CFWFields.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean mapAndValidateParamsToFields(HttpServletRequest request, HashMap<String,CFWField> fields) {
		
		Enumeration<String> parameters = request.getParameterNames();
		boolean result = true;
		
		while(parameters.hasMoreElements()) {
			String key = parameters.nextElement();
			
			if(!key.equalsIgnoreCase(CFWForm.FORM_ID)) {
				if (fields.containsKey(key)) {
					CFWField field = fields.get(key);
					
					if(!field.setValueValidated(request.getParameter(key)) ){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.silent(true)
						.finer("The field with name '"+key+"' is unknown for this type.");
				}
			}
		}
		
		return result;
	}
	
	/******************************************************************************************************
	 * Map and validates the values of job execution parameters to CFWFields.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean mapJobExecutionContextToFields(JobExecutionContext context, HashMap<String,CFWField> fields) {
		
		JobDataMap data = context.getMergedJobDataMap();
		boolean result = true;
		
		for(CFWField field : fields.values()) {
			String fieldname = field.getName();
			
			if(!fieldname.equals(CFWForm.FORM_ID)) {
				if (data.containsKey(fieldname)) {
					String value = data.getString(fieldname);
					
					if(!field.setValueValidated(value) ){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.silent(true)
						.finer("The field with name '"+fieldname+"' is unknown for this type.");
				}
			}
		}
		
		return result;
	}
	
	/******************************************************************************************************
	 * Map the values of the JsonObject to CFWFields.
	 * @param doSanitize TODO
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean mapJsonToFields(JsonObject json, HashMap<String,CFWField> fields, boolean doSanitize) {
		
		Set<String> members = json.keySet();
		boolean result = true;
		
		for(String key : members) {

			if(!key.equals(CFWForm.FORM_ID)) {
				if (fields.containsKey(key)) {
					CFWField field = fields.get(key);
					
					JsonElement element = json.get(key);
					
					if(element.isJsonNull()) {
						if(!field.setValueConvert(null, doSanitize) ){
							result = false;
						}
					}else if(element.isJsonArray() ) {
						if(!field.setValueConvert(CFW.JSON.jsonToObjectArray(element.getAsJsonArray()), doSanitize) ){
							result = false;
						}
					}else if( element.isJsonObject()){
						if(!field.setValueConvert(CFW.JSON.toJSON(element), doSanitize) ){
							result = false;
						}
					}
					else if(!field.setValueConvert(element.getAsString(), doSanitize)){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.silent(true)
						.finer("The field with name '"+key+"' is unknown for this type.");
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
	public static boolean mapAndValidateJsonToFields(JsonObject json, HashMap<String,CFWField> fields) {
		
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
					}else if(element.isJsonArray() ) {
						if(!field.setValueValidated(CFW.JSON.jsonToObjectArray(element.getAsJsonArray())) ){
							result = false;
						}
					}else if( element.isJsonObject()){
						if(!field.setValueValidated(CFW.JSON.toJSON(element)) ){
							result = false;
						}
					}
					else if(!field.setValueValidated(element.getAsString()) ){
						result = false;
					}
				}else {
					new CFWLog(logger)
						.silent(true)
						.finer("The field with name '"+key+"' is unknown for this type.");
				}
			}
		}
		
		return result;
	}
	
	/******************************************************************************************************
	 * Map the values of a result set to CFWFields.
	 * The values will not be validated
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean mapResultSetColumnsToFields(ResultSet result, HashMap<String,CFWField> fields) {
		
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
					
					if     ( String.class.isAssignableFrom(current.getValueClass()) )  { current.setValueConvert(result.getString(colName), true); }
					else if( Integer.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getObject(colName), true); }
					else if( BigDecimal.class.isAssignableFrom(current.getValueClass()))    { current.setValueConvert(result.getBigDecimal(colName), true); }
					else if( Float.class.isAssignableFrom(current.getValueClass()))    { current.setValueConvert(result.getFloat(colName), true); }
					else if( Long.class.isAssignableFrom(current.getValueClass()))    { current.setValueConvert(result.getLong(colName), true); }
					else if( Boolean.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getBoolean(colName), true); }
					else if( Timestamp.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getTimestamp(colName), true); }
					else if( Date.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getDate(colName), true); }
					else if( CFWChartSettings.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getString(colName), true); }
					else if( CFWSchedule.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getString(colName), true); }
					else if( CFWTimeframe.class.isAssignableFrom(current.getValueClass()))  { current.setValueConvert(result.getString(colName), true); }
					else if( ArrayList.class.isAssignableFrom(current.getValueClass()))  { 
						Array array = result.getArray(colName);
						
						if(array != null) {
							current.setValueConvert(result.getArray(colName).getArray(), true); 
						}else {
							current.setValueConvert(null, true);
						}
						
					}else if( LinkedHashMap.class.isAssignableFrom(current.getValueClass()))  { 
						String json = result.getString(colName);
						if(json != null) {
							current.setValueConvert(CFW.JSON.fromJsonLinkedHashMap(json), true); 
						}else {
							current.setValueConvert(null, true);
						}
					}
					
				}else {
					success = false;
					new CFWLog(logger)
						.silent(true)
						.finer("The object doesn't contain a field with name '"+colName+"'.");
				}
			}
		
		} catch (SQLException e) {
			success = false;
			new CFWLog(logger)
				.severe("SQL Exception occured while trying to map ResultSet to fields. Check Cursor position.", e);
		}
		
		return success;
	}
		
	/******************************************************************************************************
	 * Returns the value that can be inserted into an SQL statement.
	 * @param url used for the request.
	 * @return true if successful, false otherwise
	 ******************************************************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getValueForSQL() {

		Class clazz = this.getValueClass();
		
		if(this.getValue() == null) { return "NULL"; }
		
		if( String.class.isAssignableFrom(clazz) )  	{ return "'"+this.getValue().toString().replace("'", "''")+"'"; }
		if( Integer.class.isAssignableFrom(clazz))  	{ return ""+this.getValue(); }
		if( Boolean.class.isAssignableFrom(clazz))  	{ return ""+this.getValue(); }
		if( BigDecimal.class.isAssignableFrom(clazz)) 	{ return ""+((BigDecimal)this.getValue()).toPlainString(); }
		if( Float.class.isAssignableFrom(clazz))    	{ return ""+this.getValue(); }
		if( Long.class.isAssignableFrom(clazz))    		{ return ""+this.getValue(); }
		
		if( CFWChartSettings.class.isAssignableFrom(clazz)
		  || CFWSchedule.class.isAssignableFrom(clazz)
		  || CFWTimeframe.class.isAssignableFrom(clazz) 
		  || LinkedHashMap.class.isAssignableFrom(clazz) 
		  ){ return "'"+this.getValue().toString().replace("'", "''")+"'"; }
		
		if( Timestamp.class.isAssignableFrom(clazz))  {
			
			long millis = ((Timestamp)this.getValue()).getTime();
			String timestamp = CFW.Time.formatMillisAsTimestamp(millis);
			timestamp = timestamp.replace("T", " ");
			return "'"+timestamp+"'";  // e.g. '2025-01-31 19:53:54.355'
			
		}
		
		if( Date.class.isAssignableFrom(clazz))  { 
			
			long millis = ((Date)this.getValue()).getTime();
			String date = CFW.Time.formatMillisAsISODate(millis);
			return "'"+date+"'";  // e.g. '2025-01-31'
			
		}
		
		if( ArrayList.class.isAssignableFrom(clazz))  { 
			String arrayString = ((ArrayList)this.getValue()).toString();
			
			arrayString = arrayString
							.replace("'", "''")
							.replace("\"", "'")
							;
			
			return "'"+arrayString+"'";  
			
		}
					
			
		return "'CFWField.getValueForSQL(): unknown value type or class'";
	}

		
}

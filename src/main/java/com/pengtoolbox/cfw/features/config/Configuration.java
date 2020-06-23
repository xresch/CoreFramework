package com.pengtoolbox.cfw.features.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Configuration extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CONFIG";
	public static final String FILE_CACHING = "Cache Files";
	public static final String THEME = "Theme";
	public static final String LANGUAGE = "Default Language";
	public static final String CODE_THEME = "Code Theme";
	public static final String MENU_TITLE = "Menu Title";
	public static final String CPU_SAMPLING_SECONDS = "CPU Sampling Seconds";
	public static final String CPU_SAMPLING_AGGREGATION = "CPU Sampling Aggregation";

	public static final String LOGO_PATH = "Logo Path";
	
	public enum ConfigFields{
		PK_ID,
		CATEGORY,
		NAME,
		DESCRIPTION,
		TYPE,
		VALUE,
		OPTIONS
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ConfigFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.apiFieldType(FormFieldType.NUMBER)
									.setDescription("The id of the configuration.")
									.setValue(-999);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, ConfigFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(255)")
									.setDescription("The category of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, ConfigFields.NAME.toString())
									.setColumnDefinition("VARCHAR(255) UNIQUE")
									.setDescription("The name of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, ConfigFields.DESCRIPTION.toString())
											.setColumnDefinition("VARCHAR(4096)")
											.setDescription("A description of the configuration.")
											.addValidator(new LengthValidator(-1, 4096));
	
	private CFWField<String> type = CFWField.newString(FormFieldType.SELECT, ConfigFields.TYPE.toString())
			.setColumnDefinition("VARCHAR(32)")
			.setDescription("The form field type of the configuration.")
			.setOptions(FormFieldType.values())
			.addValidator(new LengthValidator(1, 32));
	
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, ConfigFields.VALUE.toString())
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The current value of the field. Can be null.")
			.addValidator(new LengthValidator(-1, 1024));
	
	private CFWField<Object[]> options = CFWField.newArray(FormFieldType.NONE, ConfigFields.OPTIONS.toString())
			.setColumnDefinition("ARRAY")
			.setDescription("The options available for the configuration(optional field).");
	
	
	public Configuration() {
		initialize();
	}
	
	public Configuration(String category, String name) {
		initialize();
		this.category.setValue(category);
		this.name.setValue(name);
	}
	
	public Configuration(ResultSet result) throws SQLException {
		initialize();
		this.mapResultSet(result);	
	}
	
	private void initialize() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, name, description, type, value, options, category);
	}
	
	public void initDBSecond() {
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", Configuration.FILE_CACHING)
				.description("Enables the caching of files read from the disk.")
				.type(FormFieldType.BOOLEAN)
				.value("true")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", Configuration.LANGUAGE)
				.description("Set the default language of the application.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"EN", "DE"})
				.value("EN")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", Configuration.THEME)
				.description("Set the application look and feel. 'Slate' is the default and recommended theme, all others are not 100% tested. For custom the file has to be placed under ./resources/css/bootstrap-theme-custom.css.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"custom", "darkblue", "flatly", "lumen", "materia", "minty", "pulse", "sandstone", "simplex", "slate", "slate-edged", "spacelab", "superhero", "united", "warm-soft", "warm-edged"})
				.value("slate-edged")
		);
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", Configuration.CODE_THEME)
				.description("Set the style for the code highlighting.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"androidstudio", "arduino-light", "magula", "pojoaque", "sunburst", "zenburn"})
				.value("zenburn")
		);
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", Configuration.MENU_TITLE )
				.description("Set the title displayed in the menu bar. Applies to all new sessions, login/logout required to see the change.")
				.type(FormFieldType.TEXT)
				.value("")
		);
		
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", Configuration.LOGO_PATH )
				.description("The path of the logo displayed in the menu bar. Relativ to the installation directory or a valid URL.")
				.type(FormFieldType.TEXT)
				.value("/resources/images/applogo.png")
		);
		
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", Configuration.CPU_SAMPLING_SECONDS )
				.description("The interval in seconds between two CPU samplings. Changes to this value needs a restart to take effect.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"1", "5", "10", "30", "60"})
				.value("10")
		);
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", Configuration.CPU_SAMPLING_AGGREGATION )
				.description("The period in minutes used for the aggregation of the statistics and writing them to the database.")
				.type(FormFieldType.SELECT)
				.options(new Integer[]{3, 15, 60, 240, 720, 1440})
				.value("3")
		);
		
							
		CFW.DB.Config.updateCache();
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
						
		String[] inputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.DESCRIPTION.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
						ConfigFields.OPTIONS.toString(),
				};

		//----------------------------------
		// fetchData
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
	
	public int id() {
		return id.getValue();
	}
	
	public Configuration id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Configuration category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public Configuration name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Configuration description(String description) {
		this.description.setValue(description);
		return this;
	}

	public String type() {
		return type.getValue();
	}

	public Configuration type(FormFieldType type) {
		this.type.setValue(type.toString());
		return this;
	}

	public String value() {
		return value.getValue();
	}

	public Configuration value(String value) {
		this.value.setValue(value);
		return this;
	}

	public Object[] options() {
		return options.getValue();
	}

	public Configuration options(Object[] options) {
		this.options.setValue(options);
		return this;
	}
	
	



	
	
}

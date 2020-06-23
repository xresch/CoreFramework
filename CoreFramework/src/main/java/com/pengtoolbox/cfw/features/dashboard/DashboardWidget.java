package com.pengtoolbox.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.features.dashboard.Dashboard.DashboardFields;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.validation.NumberRangeValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class DashboardWidget extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_WIDGET";
	
	public enum DashboardWidgetFields{
		PK_ID,
		FK_ID_DASHBOARD,
		TYPE,
		X,
		Y,
		WIDTH,
		HEIGHT,
		TITLE,
		TITLE_FONTSIZE,
		CONTENT_FONTSIZE,
		FOOTER,
		BGCOLOR,
		FGCOLOR,
		JSON_SETTINGS
	}

	private static Logger logger = CFWLog.getLogger(DashboardWidget.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardWidgetFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the widget.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyDashboard = CFWField.newInteger(FormFieldType.HIDDEN, DashboardWidgetFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard containing the widget.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> type = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.TYPE.toString())
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The type of the widget.");
	
	private CFWField<Integer> x = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.X.toString())
			.setDescription("The x coordinate of the widget.")
			.setValue(0);
	
	private CFWField<Integer> y = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.Y.toString())
			.setDescription("The y coordinate of the widget.")
			.setValue(0);
	
	private CFWField<Integer> width = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.WIDTH.toString())
			.setDescription("The width of the widget.")
			.setValue(5);
	
	private CFWField<Integer> height = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.HEIGHT.toString())
			.setDescription("The height of the widget.")
			.setValue(4);
	
	private CFWField<String> title = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.TITLE.toString())
			.setColumnDefinition("VARCHAR(32767)")
			.setDescription("The title of the widget.");
	
	private CFWField<Integer> titleFontsize = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.TITLE_FONTSIZE.toString())
			.setDescription("The font size of the title.")
			.addValidator(new NumberRangeValidator(6, 32).setNullAllowed(true));
	
	private CFWField<Integer> contentFontsize = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.CONTENT_FONTSIZE.toString())
			.setDescription("The font size of the content.")
			.addValidator(new NumberRangeValidator(6, 32).setNullAllowed(true));
	
	private CFWField<String> footer = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.FOOTER.toString())
			.setColumnDefinition("VARCHAR(32767)")
			.setDescription("The footer of the widget.");
	
	private CFWField<String> bgcolor = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.BGCOLOR.toString())
			.setColumnDefinition("VARCHAR(64)")
			.setDescription("The background color of the widget.");
	
	private CFWField<String> fgcolor = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.FGCOLOR.toString())
			.setColumnDefinition("VARCHAR(64)")
			.setDescription("The forground color of the widget, used for text and borders.");
	
	private CFWField<String> settings = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.JSON_SETTINGS.toString())
			.setDescription("The custom settings of the widget as JSON.")
			.disableSecurity();
	
	public DashboardWidget() {
		initializeFields();
	}
	
//	public Dashboard(String name, String category) {
//		initializeFields();
//		this.name.setValue(name);
//		this.category.setValue(category);
//	}
	
	public DashboardWidget(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyDashboard, type, x, y, width, height, title, titleFontsize, contentFontsize, footer, bgcolor, fgcolor, settings);
	}
		
	/**************************************************************************************
	 * Migrate Table
	 **************************************************************************************/
	public void migrateTable() {
		
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void updateTable() {
						
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						DashboardWidgetFields.PK_ID.toString(), 
						DashboardWidgetFields.FK_ID_DASHBOARD.toString(), 
				};
		
		String[] outputFields = 
				new String[] {
						DashboardWidgetFields.PK_ID.toString(), 
						DashboardWidgetFields.FK_ID_DASHBOARD.toString(), 
						DashboardWidgetFields.TYPE.toString(),
						DashboardWidgetFields.X.toString(),
						DashboardWidgetFields.Y.toString(),
						DashboardWidgetFields.WIDTH.toString(),
						DashboardWidgetFields.HEIGHT.toString(),
						DashboardWidgetFields.TITLE.toString(),
						DashboardWidgetFields.TITLE_FONTSIZE.toString(),
						DashboardWidgetFields.CONTENT_FONTSIZE.toString(),
						DashboardWidgetFields.FOOTER.toString(),
						DashboardWidgetFields.BGCOLOR.toString(),
						DashboardWidgetFields.FGCOLOR.toString(),
						DashboardWidgetFields.JSON_SETTINGS.toString(),		
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
	
	public DashboardWidget id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyDashboard() {
		return foreignKeyDashboard.getValue();
	}
	
	public DashboardWidget foreignKeyDashboard(Integer foreignKeyDashboard) {
		this.foreignKeyDashboard.setValue(foreignKeyDashboard);
		return this;
	}
	
	public String type() {
		return type.getValue();
	}
	
	public DashboardWidget type(String type) {
		this.type.setValue(type);
		return this;
	}
	
	public Integer x() {
		return x.getValue();
	}
	
	public DashboardWidget x(Integer x) {
		this.x.setValue(x);
		return this;
	}
	
	public Integer y() {
		return y.getValue();
	}
	
	public DashboardWidget y(Integer y) {
		this.y.setValue(y);
		return this;
	}

	public Integer width() {
		return width.getValue();
	}
	
	public DashboardWidget width(Integer width) {
		this.width.setValue(width);
		return this;
	}
	
	public Integer height() {
		return height.getValue();
	}
	
	public DashboardWidget height(Integer height) {
		this.height.setValue(height);
		return this;
	}
	
	public String title() {
		return title.getValue();
	}
	
	public DashboardWidget title(String title) {
		this.title.setValue(title);
		return this;
	}
	
	public Integer titleFontsize() {
		return titleFontsize.getValue();
	}
	
	public DashboardWidget titleFontsize(Integer titleFontsize) {
		this.titleFontsize.setValue(titleFontsize);
		return this;
	}
	
	public Integer contentFontsize() {
		return contentFontsize.getValue();
	}
	
	public DashboardWidget contentFontsize(Integer contentFontsize) {
		this.contentFontsize.setValue(contentFontsize);
		return this;
	}
	
	public String footer() {
		return footer.getValue();
	}
	
	public DashboardWidget footer(String footer) {
		this.footer.setValue(footer);
		return this;
	}
	
	public String bgcolor() {
		return bgcolor.getValue();
	}
	
	public DashboardWidget bgcolor(String bgcolor) {
		this.bgcolor.setValue(bgcolor);
		return this;
	}
	
	
	public String fgcolor() {
		return fgcolor.getValue();
	}
	
	public DashboardWidget fgcolor(String fgcolor) {
		this.fgcolor.setValue(fgcolor);
		return this;
	}
	
	public String settings() {
		return settings.getValue();
	}
	
	public DashboardWidget settings(String settings) {
		this.settings.setValue(settings);
		return this;
	}
}

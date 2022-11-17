package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWChartSettings.AxisType;
import com.xresch.cfw.datahandling.CFWChartSettings.ChartType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.NumberRangeValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
		TITLE_LINK,
		TITLE_POSITION,
		TITLE_FONTSIZE,
		CONTENT_FONTSIZE,
		FOOTER,
		BGCOLOR,
		FGCOLOR,
		JSON_SETTINGS,
		JSON_TASK_PARAMETERS,
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
			.setValue(8);
	
	private CFWField<Integer> height = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.HEIGHT.toString())
			.setDescription("The height of the widget.")
			.setValue(12);
	
	private CFWField<String> title = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.TITLE)
			.setColumnDefinition("VARCHAR(32767)")
			.setDescription("The title of the widget.");
	
	private CFWField<String> titlelink = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.TITLE_LINK)
			.setColumnDefinition("VARCHAR(32767)")
			.setDescription("An optional link for the title.");
	
	private CFWField<Integer> titleFontsize = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.TITLE_FONTSIZE)
			.setDescription("The font size of the title.")
			.addValidator(new NumberRangeValidator(6, 32).setNullAllowed(true));
	
	private CFWField<String> titleposition = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.TITLE_POSITION)
			.setColumnDefinition("VARCHAR(32)")
			.setDescription("The position of the title.");
	
	private CFWField<Integer> contentFontsize = CFWField.newInteger(FormFieldType.NUMBER, DashboardWidgetFields.CONTENT_FONTSIZE)
			.setDescription("The font size of the content.")
			.addValidator(new NumberRangeValidator(6, 32).setNullAllowed(true));
	
	private CFWField<String> footer = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.FOOTER)
			.setColumnDefinition("VARCHAR(32767)")
			.setDescription("The footer of the widget.");
	
	private CFWField<String> bgcolor = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.BGCOLOR)
			.setColumnDefinition("VARCHAR(64)")
			.setDescription("The background color of the widget.");
	
	private CFWField<String> fgcolor = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.FGCOLOR)
			.setColumnDefinition("VARCHAR(64)")
			.setDescription("The forground color of the widget, used for text and borders.");
	
	/** Settings are coming from the fields defined by {@link WidgetDefinition#getSettings()}. Security can be disabled here. */
	private CFWField<String> settings = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.JSON_SETTINGS)
			.setDescription("The custom settings of the widget as JSON.")
			.disableSanitization();
	
	/** TaskParameters are coming from the fields defined by {@link WidgetDefinition#getTasksParameters()}. Security can be disabled here. */
	private CFWField<String> taskParameters = CFWField.newString(FormFieldType.TEXT, DashboardWidgetFields.JSON_TASK_PARAMETERS)
			.setDescription("The task parameters for the widgets task execution.")
			.disableSanitization();
	
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
		this.addFields(id, foreignKeyDashboard, type, x, y, width, height, title, titlelink, titleFontsize, titleposition, contentFontsize, footer, bgcolor, fgcolor, settings, taskParameters);
	}

	/**************************************************************************************
	 * Creates a short HTML string with a message stating the dashboard and widget a
	 * message (e.g. eMail) has originated from. Contains a link to the Dashboard.
	 * Useful for creating alert messages.
	 * 
	 **************************************************************************************/
	public String createWidgetOriginMessage() {
		Integer dashboardID = this.foreignKeyDashboard();
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		String dashboardLink = FeatureDashboard.createURLForDashboard(dashboardID);

		String linkHTML = "<p>This message was created by "
					+ ( Strings.isNullOrEmpty(this.title()) ? "a widget on the dashboard " : " the widget <b>"+this.title()+"</b> on the dashboard ")
					+ (dashboard != null ? "<b>"+dashboard.name()+"</b>.</p>" : " with the ID <b>"+dashboardID+"'</b>. </p>")
					+ (dashboardLink != null ? "<p>Click <a target=\"_blank\" href=\""+dashboardLink+"\">here</a> to open the dashboard.</p>" : "");
		
		return linkHTML;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
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
						DashboardWidgetFields.TITLE_LINK.toString(),
						DashboardWidgetFields.TITLE_POSITION.toString(),
						DashboardWidgetFields.TITLE_FONTSIZE.toString(),
						DashboardWidgetFields.CONTENT_FONTSIZE.toString(),
						DashboardWidgetFields.FOOTER.toString(),
						DashboardWidgetFields.BGCOLOR.toString(),
						DashboardWidgetFields.FGCOLOR.toString(),
						DashboardWidgetFields.JSON_SETTINGS.toString(),	 	
						DashboardWidgetFields.JSON_TASK_PARAMETERS.toString(),	 	
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
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void updateTable() {
		
		//###########################################################
		// Migration of Chart Settings
		//###########################################################
		
		//------------------------------------
		// Fetch Widgets with chart settings
		ArrayList<DashboardWidget> chartWidgetArray = 
				new CFWSQL(new DashboardWidget())
					.select(  DashboardWidgetFields.PK_ID
							, DashboardWidgetFields.TITLE
							, DashboardWidgetFields.TYPE
							, DashboardWidgetFields.JSON_SETTINGS
						)
					.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%chart_type%")
					.and().like(DashboardWidgetFields.JSON_SETTINGS, "%stacked%")
					.getAsObjectListConvert(DashboardWidget.class);
		
		//------------------------------------
		// Iterate widgets 
		for(DashboardWidget widget : chartWidgetArray) {
			
			JsonObject settingsObject = widget.settingsAsJson();
			
			//------------------------------------
			// Grab Existing Settings
			ChartType charttype = ChartType.valueOf(settingsObject.get("chart_type").getAsString()); 	
			
			
			Boolean stacked = settingsObject.get("stacked").getAsBoolean(); 		
			Boolean showlegend = settingsObject.get("show_legend").getAsBoolean(); 	
			Float pointradius = settingsObject.get("pointradius").getAsFloat(); 		
			
			Float ymin = 
					! settingsObject.get("ymin").isJsonNull()
					? settingsObject.get("ymin").getAsFloat()
					: null; 
			
			Float ymax = 
					! settingsObject.get("ymax").isJsonNull()
					? settingsObject.get("ymax").getAsFloat()
					: null; 
			
			Boolean showaxes = 
					settingsObject.get("show_axes") != null
					? settingsObject.get("show_axes").getAsBoolean()
					: true;
			
			
			AxisType xaxisType = 
					settingsObject.get("x_axis_type") != null 
						? AxisType.valueOf(settingsObject.get("x_axis_type").getAsString()) 
						: AxisType.time; 
			
			
			AxisType yaxisType = 
					settingsObject.get("y_axis_type") != null 
						? AxisType.valueOf(settingsObject.get("y_axis_type").getAsString()) 
						: AxisType.linear; 
			
			
			//------------------------------------
			// Remove Existing Settings
			settingsObject.remove("chart_type");
			settingsObject.remove("stacked");
			settingsObject.remove("show_legend");
			settingsObject.remove("pointradius");
			settingsObject.remove("ymin");
			settingsObject.remove("ymax");
			settingsObject.remove("show_axes");
			settingsObject.remove("x_axis_type");
			settingsObject.remove("y_axis_type");
			
			//------------------------------------
			// Create new Settings Structure
			CFWChartSettings chartSettings = 
				new CFWChartSettings()
					.chartType(charttype)
					.showLegend(showlegend)
					.showAxes(showaxes)
					.stacked(stacked)
					.pointRadius(pointradius)
					.xaxisType(xaxisType)
					.yaxisType(yaxisType)
					.yaxisMin(ymin)
					.yaxisMax(ymax);
			
			settingsObject.add(WidgetSettingsFactory.FIELDNAME_CHARTSETTINGS, chartSettings.getAsJsonObject());

			//------------------------------------
			// Store in DB
			widget.settings( CFW.JSON.toJSON(settingsObject) );
			
			if(widget.update(DashboardWidgetFields.JSON_SETTINGS)) {
				new CFWLog(logger).audit(CFWAuditLogAction.MIGRATE
						, DashboardWidget.class
						, "Migrated Chart settings for widget: TITLE:'"+widget.title()+"',  TYPE:"+widget.type()+", ID:"+widget.id()
					);
			}else {
				new CFWLog(logger).severe(
						"Error while migrating chart settings for widget: TITLE:'"+widget.title()+"',  TYPE:"+widget.type()+", ID:"+widget.id()
						, new Exception()
					);
			}
		}
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
	
	public JsonObject settingsAsJson() {
		return CFW.JSON.stringToJsonObject(settings.getValue());
	}
	
	public DashboardWidget settings(String settings) {
		this.settings.setValue(settings);
		return this;
	}
	
	public String taskParameters() {
		return taskParameters.getValue();
	}
	
	public DashboardWidget taskParameters(String value) {
		this.taskParameters.setValue(value);
		return this;
	}
}

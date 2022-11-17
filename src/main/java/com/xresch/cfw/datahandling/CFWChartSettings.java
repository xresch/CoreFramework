package com.xresch.cfw.datahandling;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.AbstractValidatable;
import com.xresch.cfw.validation.ScheduleValidator;

public class CFWChartSettings {

	private static Logger logger = CFWLog.getLogger(CFWChartSettings.class.getName());
	
	private JsonObject chartSettingsData;
		
	private static final String CHARTTYPE 		= "charttype";
	private static final String XAXIS_TYPE 		= "xtype";
	private static final String YAXIS_TYPE 		= "ytype";
	private static final String YAXIS_MIN 		= "ymin";
	private static final String YAXIS_MAX 		= "ymax";
	
	private static final String STACKED 		= "stacked";
	private static final String SHOWLEGEND 		= "showlegend";
	private static final String SHOWAXES 		= "showaxes";
	private static final String POINTRADIUS 	= "pointradius";
	
//	chartSettings.charttype 	= $(selector+'-CHARTTYPE').val();
//	chartSettings.xtype 		= $(selector+'-XAXIS_TYPE').val();
//	chartSettings.ytype 		= $(selector+'-YAXIS_TYPE').val();
//	chartSettings.ymin 			= $(selector+'-YAXIS_MIN').val();
//	chartSettings.ymax			= $(selector+'-YAXIS_MAX').val();
//	chartSettings.stacked  		= $(selector+"-STACKED:checked").val();  
//	chartSettings.showlegend	= $(selector+"-SHOWLEGEND:checked").val();  
//	chartSettings.showaxes		= $(selector+"-SHOWAXES:checked").val();  
//	chartSettings.pointradius	= $(selector+"-POINTRADIUS").val();  
//	
	public enum ChartType{
		  area
		, line
		, bar
		, scatter
		, steppedarea
		, steppedline
	}
	
	public enum AxisType{
		  linear
		, logarithmic
		, time
		//, category
	}
		
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings() {
		setToDefaults();
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings(String jsonString) {
		
		if(Strings.isNullOrEmpty(jsonString)) {
			setToDefaults();
			return;
		}
		
		JsonElement element = CFW.JSON.stringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			chartSettingsData = element.getAsJsonObject();
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {

		chartSettingsData = new JsonObject();
		
		chartSettingsData.addProperty(CHARTTYPE, ChartType.area.toString());
		chartSettingsData.addProperty(XAXIS_TYPE, AxisType.time.toString());
		chartSettingsData.addProperty(YAXIS_TYPE, AxisType.linear.toString());
		chartSettingsData.addProperty(YAXIS_MIN, 0);
		chartSettingsData.add(YAXIS_MAX, null);
		
		chartSettingsData.addProperty(STACKED, false);
		
		chartSettingsData.addProperty(SHOWLEGEND, false);
		chartSettingsData.addProperty(SHOWAXES, true);
		chartSettingsData.addProperty(POINTRADIUS, 2);
								
	}
	
	
	
	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public ChartType chartType() {
		if(chartSettingsData == null || chartSettingsData.get(CHARTTYPE).isJsonNull()) return null;
		
		return ChartType.valueOf(chartSettingsData.get(CHARTTYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings chartType(ChartType type) {
		chartSettingsData.addProperty(CHARTTYPE, type.toString());
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public AxisType xaxisType() {
		if(chartSettingsData == null || chartSettingsData.get(XAXIS_TYPE).isJsonNull()) return null;
		
		return AxisType.valueOf(chartSettingsData.get(XAXIS_TYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings xaxisType(AxisType type) {
		chartSettingsData.addProperty(XAXIS_TYPE, type.toString());
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public AxisType yaxisType() {
		if(chartSettingsData == null || chartSettingsData.get(YAXIS_TYPE).isJsonNull()) return null;
		
		return AxisType.valueOf(chartSettingsData.get(YAXIS_TYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings yaxisType(AxisType type) {
		chartSettingsData.addProperty(YAXIS_TYPE, type.toString());
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Integer yaxisMin() {
		if(chartSettingsData == null || chartSettingsData.get(YAXIS_MIN).isJsonNull()) return null;
		
		return chartSettingsData.get(YAXIS_MIN).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings yaxisMin(int value) {
		chartSettingsData.addProperty(YAXIS_MIN, value);
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Integer yaxisMax() {
		if(chartSettingsData == null || chartSettingsData.get(YAXIS_MAX).isJsonNull()) return null;
		
		return chartSettingsData.get(YAXIS_MAX).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings yaxisMax(int value) {
		chartSettingsData.addProperty(YAXIS_MAX, value);
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Integer pointRadius() {
		if(chartSettingsData == null || chartSettingsData.get(POINTRADIUS).isJsonNull()) return null;
		
		return chartSettingsData.get(POINTRADIUS).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings pointRadius(int value) {
		chartSettingsData.addProperty(POINTRADIUS, value);
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public Boolean stacked() {
		if(chartSettingsData == null || chartSettingsData.get(STACKED).isJsonNull()) return null;

		return chartSettingsData.get(STACKED).getAsBoolean();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings stacked(boolean value) {
		chartSettingsData.addProperty(STACKED, value);
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public Boolean showLegend() {
		if(chartSettingsData == null || chartSettingsData.get(SHOWLEGEND).isJsonNull()) return null;

		return chartSettingsData.get(SHOWLEGEND).getAsBoolean();
	}
	
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings showLegend(boolean value) {
		chartSettingsData.addProperty(SHOWLEGEND, value);
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public Boolean showAxes() {
		if(chartSettingsData == null || chartSettingsData.get(SHOWAXES).isJsonNull()) return null;

		return chartSettingsData.get(SHOWAXES).getAsBoolean();
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings showAxes(boolean value) {
		chartSettingsData.addProperty(SHOWAXES, value);
		return this;
	}
	
	
	/***************************************************************************************
	 * Convert to JSON String
	 ***************************************************************************************/
	@Override
	public String toString() {
		return CFW.JSON.toJSON(chartSettingsData);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public JsonObject getAsJsonObject() {
		return chartSettingsData.deepCopy();
	}
	
	/***************************************************************************************
	 * Returns true if schedule is valid, false otherview
	 ***************************************************************************************/
//	public boolean validate() {
//		
//		ScheduleValidator validator = new ScheduleValidator(
//				new AbstractValidatable<String>() {}.setLabel("schedule")
//			);
//		
//		if( !validator.validate(this.toString()) ){
//			new CFWLog(logger).severe(validator.getInvalidMessage(), new Exception());
//			return false;
//		}
//		
//		return true;
//	}

}

package com.xresch.cfw.datahandling;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

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
	private static final String SPANGAPS 		= "spangaps";
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
		chartSettingsData = createDefaults();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings(String jsonString) {
		
		chartSettingsData = createDefaults();

		System.out.println("before: "+CFW.JSON.toJSON(chartSettingsData));
		JsonElement element = CFW.JSON.stringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			
			CFW.JSON.mergeData(chartSettingsData, object, false);
			System.out.println("merged");
		}
		System.out.println("after: "+CFW.JSON.toJSON(chartSettingsData));
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private JsonObject createDefaults() {

		JsonObject defaults = new JsonObject();
		
		defaults.addProperty(CHARTTYPE, ChartType.area.toString());
		defaults.addProperty(XAXIS_TYPE, AxisType.time.toString());
		defaults.addProperty(YAXIS_TYPE, AxisType.linear.toString());
		defaults.addProperty(YAXIS_MIN, 0);
		defaults.add(YAXIS_MAX, null);
		
		defaults.addProperty(STACKED, false);
		defaults.addProperty(SHOWLEGEND, false);
		defaults.addProperty(SHOWAXES, true);
		defaults.addProperty(SPANGAPS, false);
		defaults.addProperty(POINTRADIUS, 2);
		
		return defaults;
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
	public Float yaxisMin() {
		if(chartSettingsData == null || chartSettingsData.get(YAXIS_MIN).isJsonNull()) return null;
		
		return chartSettingsData.get(YAXIS_MIN).getAsFloat();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings yaxisMin(Float value) {
		chartSettingsData.addProperty(YAXIS_MIN, value);
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Float yaxisMax() {
		if(chartSettingsData == null || chartSettingsData.get(YAXIS_MAX).isJsonNull()) return null;
		
		return chartSettingsData.get(YAXIS_MAX).getAsFloat();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings yaxisMax(Float value) {
		chartSettingsData.addProperty(YAXIS_MAX, value);
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Float pointRadius() {
		if(chartSettingsData == null || chartSettingsData.get(POINTRADIUS).isJsonNull()) return null;
		
		return chartSettingsData.get(POINTRADIUS).getAsFloat();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings pointRadius(float value) {
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
	 * 
	 ***************************************************************************************/
	public Boolean spanGaps() {
		if(chartSettingsData == null || chartSettingsData.get(SPANGAPS).isJsonNull()) return null;

		return chartSettingsData.get(SPANGAPS).getAsBoolean();
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWChartSettings spanGaps(boolean value) {
		chartSettingsData.addProperty(SPANGAPS, value);
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

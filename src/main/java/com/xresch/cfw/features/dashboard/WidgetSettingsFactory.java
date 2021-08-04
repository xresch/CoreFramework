package com.xresch.cfw.features.dashboard;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetSettingsFactory {

//new String[]{"Area", "Line", "Bar", "Scatter"}
	private static LinkedHashMap<String,String> linearChartTypes = new LinkedHashMap<String,String>();
	
	static {
		linearChartTypes.put("area", "Area");
		linearChartTypes.put("line", "Line");
		linearChartTypes.put("bar", "Bar");
		linearChartTypes.put("scatter", "Scatter");
		linearChartTypes.put("steppedline", "Stepped Line");
		linearChartTypes.put("steppedarea", "Stepped Area");
	}
		
	/************************************************************************************
	 *  
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDisplayAsSelect(String[] rendererNames, String defaultValue ){
		return 	CFWField.newString(FormFieldType.SELECT, "renderer")
					.setLabel("{!cfw_widget_displayas!}")
					.setDescription("{!cfw_widget_displayas_desc!}")
					.setOptions(rendererNames)
					.setValue(defaultValue);
		
	}
	
	/************************************************************************************
	 *  
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDisableBoolean(){
		return 	CFWField.newBoolean(FormFieldType.BOOLEAN, "disable")
					.setLabel("{!cfw_widget_disable!}")
					.setDescription("{!cfw_widget_disable_desc!}")
					.setValue(false);
	}
	
	/************************************************************************************
	 * Returns the default example data boolean field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSampleDataField(){
		return CFWField.newBoolean(FormFieldType.BOOLEAN, "sampledata")
		.setLabel("{!cfw_widget_sampledata!}")
		.setDescription("{!cfw_widget_sampledata_desc!}")
		.setValue(false);
	}
	
	
	/************************************************************************************
	 * Create Display As field to Choose the Renderer.(fieldname=renderer)
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDefaultDisplayAsField(){
		
		LinkedHashMap<String, String> rendererOptions = new LinkedHashMap<>();
		rendererOptions.put("tiles", "Tiles");
		rendererOptions.put("table", "Table");
		rendererOptions.put("panels", "Panels");
		rendererOptions.put("cards", "Cards");
		rendererOptions.put("csv", "CSV");
		rendererOptions.put("json", "JSON");
		
		return CFWField.newString(FormFieldType.SELECT, "renderer")
			.setLabel("{!cfw_widget_displayas!}")
			.setDescription("{!cfw_widget_displayas_desc!}")
			.setOptions(rendererOptions)
			.setValue("Tiles");
	}
	
	/************************************************************************************
	 * Create settings used for tiles: sizefactor, borderstyle, showlabels.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createTilesSettingsFields(){
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		fieldsMap.put("sizefactor", CFWField.newString(FormFieldType.SELECT, "sizefactor")
				.setLabel("{!cfw_widget_tilessizefactor!}")
				.setDescription("{!cfw_widget_tilessizefactor_desc!}")
				.setOptions(new String[]{"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1", "1.2", "1.4", "1.6", "1.8","2.0", "2.5", "3.0", "4.0", "5.0"})
				.setValue("1"));
		
		fieldsMap.put("borderstyle", CFWField.newString(FormFieldType.SELECT, "borderstyle")
				.setLabel("{!cfw_widget_tilesborderstyle!}")
				.setDescription("{!cfw_widget_tilesborderstyle_desc!}")
				.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
				.setValue("None")
				);		
		
		fieldsMap.put("showlabels", CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
				.setLabel("{!cfw_widget_tilesshowlabels!}")
				.setDescription("{!cfw_widget_tilesshowlabels_desc!}")
				.setValue(true)
				);	
			
		return fieldsMap;
	}
	
	/************************************************************************************
	 * Returns default threshold fields as a LinkedHashMap.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createThresholdFields(){
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		fieldsMap.put("threshold_excellent", CFWField.newFloat(FormFieldType.NUMBER, "threshold_excellent")
					.setLabel("{!cfw_widget_thresholdexcellent!}")
					.setDescription("{!cfw_widget_thresholdexcellent_desc!}")
					.setValue(null)
				);
		
		fieldsMap.put("threshold_good", CFWField.newFloat(FormFieldType.NUMBER, "threshold_good")
					.setLabel("{!cfw_widget_thresholdgood!}")
					.setDescription("{!cfw_widget_thresholdgood_desc!}")
					.setValue(null)
				);			
		fieldsMap.put("threshold_warning", CFWField.newFloat(FormFieldType.NUMBER, "threshold_warning")
					.setLabel("{!cfw_widget_thresholdwarning!}")
					.setDescription("{!cfw_widget_thresholdwarning_desc!}")
					.setValue(null)
				);			
		fieldsMap.put("threshold_emergency", CFWField.newFloat(FormFieldType.NUMBER, "threshold_emergency")
					.setLabel("{!cfw_widget_thresholdemergency!}")
					.setDescription("{!cfw_widget_thresholdemergency_desc!}")
					.setValue(null)
				);			
		fieldsMap.put("threshold_danger", CFWField.newFloat(FormFieldType.NUMBER, "threshold_danger")
					.setLabel("{!cfw_widget_thresholddanger!}")
					.setDescription("{!cfw_widget_thresholddanger_desc!}")
					.setValue(null)
				);	
			
		return fieldsMap;
	}
	
	/************************************************************************************
	 * Returns default chart fields as a LinkedHashMap.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createDefaultChartFields(){
		
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		//Needed to clone because dashboard parameters might mess up the hashmap.
		LinkedHashMap<String, String> chartOptions = new LinkedHashMap<>();
		chartOptions.putAll(linearChartTypes);
		
		fieldsMap.put("chart_type", CFWField.newString(FormFieldType.SELECT, "chart_type")
				.setLabel("{!cfw_widget_charttype!}")
				.setDescription("{!cfw_widget_charttype_desc!}")
				.setOptions(chartOptions)
				.setValue("Area")
		);
		
		fieldsMap.put("stacked", CFWField.newBoolean(FormFieldType.BOOLEAN, "stacked")
				.setLabel("{!cfw_widget_chartstacked!}")
				.setDescription("{!cfw_widget_chartstacked_desc!}")
				.setValue(false)
		);
		
		fieldsMap.put("show_legend", CFWField.newBoolean(FormFieldType.BOOLEAN, "show_legend")
				.setLabel("{!cfw_widget_chartshowlegend!}")
				.setDescription("{!cfw_widget_chartshowlegend_desc!}")
				.setValue(false)
		);
		
		fieldsMap.put("show_axes", CFWField.newBoolean(FormFieldType.BOOLEAN, "show_axes")
				.setLabel("{!cfw_widget_chartshowaxes!}")
				.setDescription("{!cfw_widget_chartshowaxes_desc!}")
				.setValue(true)
		);
		fieldsMap.put("pointradius", CFWField.newFloat(FormFieldType.NUMBER, "pointradius")
				.setLabel("{!cfw_widget_chartpointradius!}")
				.setDescription("{!cfw_widget_chartpointradius_desc!}")
				.setValue(0.0f)
				.addValidator(new NumberRangeValidator(0, 10))
		);
		fieldsMap.put("ymin", CFWField.newInteger(FormFieldType.NUMBER, "ymin")
				.setLabel("{!cfw_widget_chart_ymin!}")
				.setDescription("{!cfw_widget_chart_ymin_desc!}")
				.setValue(0)
		);
		
		fieldsMap.put("ymax", CFWField.newInteger(FormFieldType.NUMBER, "ymax")
				.setLabel("{!cfw_widget_chart_ymax!}")
				.setDescription("{!cfw_widget_chart_ymax_desc!}")
		);
		return fieldsMap;
	}
}

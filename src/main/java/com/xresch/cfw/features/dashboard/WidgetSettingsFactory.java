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
	 * Returns default threshold fields as a LinkedHashMap.
	 * 
	 * @return
	 ************************************************************************************/
	public static LinkedHashMap<String,CFWField<?>> createThresholdFields(){
		LinkedHashMap<String,CFWField<?>> fieldsMap = new LinkedHashMap<String,CFWField<?>>();
		
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
	public static LinkedHashMap<String,CFWField<?>> createDefaultChartFields(){
		
		LinkedHashMap<String,CFWField<?>> fieldsMap = new LinkedHashMap<String,CFWField<?>>();
		
		fieldsMap.put("chart_type", CFWField.newString(FormFieldType.SELECT, "chart_type")
				.setLabel("{!cfw_widget_charttype!}")
				.setDescription("{!cfw_widget_charttype_desc!}")
				.setOptions(linearChartTypes)
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

package com.xresch.cfw.features.dashboard;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class WidgetSettingsFactory {

	
	public static CFWField<?>[] createThresholdFields(){
		
		return new CFWField<?>[] {
			CFWField.newFloat(FormFieldType.NUMBER, "threshold_excellent")
					.setLabel("{!cfw_widget_thresholdexcellent!}")
					.setDescription("{!cfw_widget_thresholdexcellent_desc!}")
					.setValue(null)
					
			,CFWField.newFloat(FormFieldType.NUMBER, "threshold_good")
					.setLabel("{!cfw_widget_thresholdgood!}")
					.setDescription("{!cfw_widget_thresholdgood_desc!}")
					.setValue(null)
					
			,CFWField.newFloat(FormFieldType.NUMBER, "threshold_warning")
					.setLabel("{!cfw_widget_thresholdwarning!}")
					.setDescription("{!cfw_widget_thresholdwarning_desc!}")
					.setValue(null)
					
			,CFWField.newFloat(FormFieldType.NUMBER, "threshold_emergency")
					.setLabel("{!cfw_widget_thresholdemergency!}")
					.setDescription("{!cfw_widget_thresholdemergency_desc!}")
					.setValue(null)
					
			,CFWField.newFloat(FormFieldType.NUMBER, "threshold_danger")
					.setLabel("{!cfw_widget_thresholddanger!}")
					.setDescription("{!cfw_widget_thresholddanger_desc!}")
					.setValue(null)
			
			
		};
	}
	
	public static LinkedHashMap<String,CFWField<?>> createDefaultChartFields(){
		
		LinkedHashMap<String,CFWField<?>> fieldsMap = new LinkedHashMap<String,CFWField<?>>();
		
		fieldsMap.put("chart_type", CFWField.newString(FormFieldType.SELECT, "chart_type")
				.setLabel("{!cfw_widget_charttype!}")
				.setDescription("{!cfw_widget_charttype_desc!}")
				.setOptions(new String[]{"Area", "Line", "Bar", "Scatter"})
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

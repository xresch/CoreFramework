package com.xresch.cfw.features.dashboard;

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
}

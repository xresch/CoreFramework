(function (){
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_customthresholdlegend",
		{
			category:  CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('cfw_widget_thresholdlegend', "Custom Threshold Legend"),
			description: CFWL('cfw_widget_thresholdlegend_desc', "Displays a legend for the threshold colors with custom labels."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				
				var settings = widgetObject.JSON_SETTINGS;
				var html = '<div class="w-100 d-flex flex-wrap word-break-all">';
				if(!CFW.utils.isNullOrEmpty(settings.labelexcellent)){ 	html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-green">&nbsp;</div> '+settings.labelexcellent+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelgood)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-limegreen">&nbsp;</div> '+settings.labelgood+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelwarning)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-yellow">&nbsp;</div> '+settings.labelwarning+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelemergency)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-orange">&nbsp;</div> '+settings.labelemergency+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labeldanger)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-red">&nbsp;</div> '+settings.labeldanger+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelgray)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> '+settings.labelgray+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labeldarkgray)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-darkgray">&nbsp;</div> '+settings.labeldarkgray+' </div>'; }

				html += '</div>';
					
				callback(widgetObject, html);
			},
			
		}
	);
})();
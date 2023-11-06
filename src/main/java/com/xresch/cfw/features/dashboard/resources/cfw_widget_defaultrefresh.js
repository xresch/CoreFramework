(function (){

	CFW.dashboard.registerWidget("cfw_defaultrefresh",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-sync",
			menulabel: CFWL('cfw_widget_defaultrefresh', 'Default Refresh'),
			description: CFWL('cfw_widget_defaultrefresh_desc', 'Set the refresh to a specified values in case it is set to off.'),
			defaultsettings: {
				WIDTH: 8,
				HEIGHT: 6,
				INVISIBLE: true
			},
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				
				var settings = widgetObject.JSON_SETTINGS;
				if(settings.defaultinterval != null){
					
					var refreshSelector = $('#refreshSelector');
					
					if(settings.defaultinterval == 'stop' 
					|| refreshSelector.val() == 'stop'){
					
						refreshSelector.val(settings.defaultinterval)			
						cfw_dashboard_setReloadInterval(refreshSelector);
						
					}
					
					callback(widgetObject, "<span>Selected default interval: "+settings.defaultinterval);
				}
				
					
				


			},
						
		}
	);
})();
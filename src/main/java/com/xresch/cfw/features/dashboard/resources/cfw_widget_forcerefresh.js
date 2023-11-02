(function (){

	CFW.dashboard.registerWidget("cfw_forcerefresh",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-sync",
			menulabel: CFWL('cfw_widget_forcerefresh', 'Force Refresh'),
			description: CFWL('cfw_widget_forcerefresh_desc', 'Forces the widget data to refresh and does not take it from the widget cache.'),
			defaulttitle: "",
			defaultwidth: 4,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				var settings = widgetObject.JSON_SETTINGS;
				var buttonLabel = '<i class="fas fa-sync">&nbsp;</i>';
				
				if( !CFW.utils.isNullOrEmpty(settings.LABEL) ) {
					buttonLabel += settings.LABEL;
				}
				
				button = '<button class="btn btn-sm btn-primary w-100 h-100" onclick="cfw_dashboard_draw(false, true)">'+buttonLabel+"</button>";
				
				callback(widgetObject, button);


			},
						
		}
	);
})();
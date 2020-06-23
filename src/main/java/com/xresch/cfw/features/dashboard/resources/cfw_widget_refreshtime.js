(function (){

	CFW.dashboard.registerWidget("cfw_refreshtime",
		{
			category: "Static Widgets",
			menuicon: "fas fa-history",
			menulabel: CFWL('cfw_widget_cfwrefreshtime', 'Refresh Time'),
			description: CFWL('cfw_widget_cfwrefreshtime_desc', 'Displays the time the dashboard was refreshed.'),
			defaulttitle: "",
			defaultwidth: 4,
			defaultheight: 1,
			createWidgetInstance: function (widgetObject, callback) {	
				
				var settings = widgetObject.JSON_SETTINGS;
				console.log("settings.format: "+settings.format);
				if(settings.format == null || settings.format.trim() == ""){
					settings.format = "HH:mm";
				}
				var rotationClass = '';
				
				if(widgetObject.JSON_SETTINGS.direction == "Top to Bottom"){
					rotationClass = 'rotate-90';
				}else if(widgetObject.JSON_SETTINGS.direction == "Bottom to Top"){
					rotationClass = 'rotate-270';
				}else if(widgetObject.JSON_SETTINGS.direction == "Upside Down"){
					rotationClass = 'rotate-180';
				}
				
				var labelHTML = '<div class="label-box"><span class="text-center '+rotationClass+'" style="white-space: nowrap; font-size: '+24*settings.sizefactor+'px;"">'
								+ new CFWDate().getDateFormatted(settings.format)
								+'</span></div>'; 
				
				callback(widgetObject, labelHTML);
				

			},			
		}
	);
		
})();
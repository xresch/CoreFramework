(function (){

	CFW.dashboard.registerWidget("cfw_javascript",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fab fa-js-square",
			menulabel: CFWL('cfw_widget_cfwjavascript', 'Javascript'),
			description: CFWL('cfw_widget_cfwjavascript_desc', 'Can be used to add custom javascript to the dashboard.'),
			
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				if(widgetObject.JSON_SETTINGS.script != null){
					var jsid = 'js-widget-'+widgetObject.PK_ID;
					
					//---------------------------
					// remove if exists
					var scriptElement = $('#'+jsid);
					if(jsid.length > 0){
						scriptElement.remove();
					}
					
					//---------------------------
					// Add script
					var script = $('<script id="'+jsid+'">'+widgetObject.JSON_SETTINGS.script+"</script>")
					$('#javascripts').append(script);
				}
				
				callback(widgetObject, '');

			},
						
		}
	);
	
})();
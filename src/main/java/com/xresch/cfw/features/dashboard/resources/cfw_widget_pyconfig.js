(function (){

	CFW.dashboard.registerWidget("cfw_pyconfig",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fab fa-python",
			menulabel: CFWL('cfw_widget_pyconfig', 'Py Config'),
			description: CFWL('cfw_widget_pyconfig_desc', 'Widget to add a py-config tag to a page.'),
			
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				var settings = widgetObject.JSON_SETTINGS;
				if(settings.pyconfig != null){
					var elementID = 'py-config-'+widgetObject.PK_ID;
					
					//---------------------------
					// Replace if exists
					var scriptElement = $('#'+elementID);
					if(scriptElement.length > 0){
						scriptElement.remove();
					}
					
					//---------------------------
					// Add script
					var script = $('<py-config id="'+elementID+'" class="cfw-py-config">'+settings.pyconfig+"</py-config>")
					$('#javascripts').append(script);
				}
				
				callback(widgetObject, settings.pyconfig);

			},
						
		}
	);
	
})();
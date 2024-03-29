(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_website",
		{
			category: CFW.dashboard.global.categoryDefault,
			menuicon: "fas fa-globe",
			menulabel: CFWL('cfw_widget_cfwwebsite', "Website"),
			description: CFWL('cfw_widget_cfwwebsite_desc', "Displays a website(only works when the website that is embeded allows it)."),
			
			createWidgetInstance: function (widgetObject, params, callback) {
				
				callback(widgetObject, '<iframe class="w-100 h-100" frameborder="0" src="'+widgetObject.JSON_SETTINGS.url+'">');
				
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
		}
	);
})();
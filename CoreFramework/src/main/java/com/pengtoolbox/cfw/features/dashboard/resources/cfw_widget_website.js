(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_website",
		{
			category: "Static Widgets",
			menuicon: "fas fa-globe",
			menulabel: CFWL('cfw_widget_cfwwebsite', "Website"),
			description: CFWL('cfw_widget_cfwwebsite_desc', "Displays a website(only works when the website that is embeded allows it)."),
			
			createWidgetInstance: function (widgetObject, callback) {
				callback(widgetObject, '<iframe class="w-100 h-100" sandbox="allow-scripts allow-forms" src="'+widgetObject.JSON_SETTINGS.url+'">');
				
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
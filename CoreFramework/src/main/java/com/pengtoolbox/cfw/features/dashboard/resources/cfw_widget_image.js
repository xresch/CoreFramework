(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_image",
		{
			category: "Static Widgets",
			menuicon: "far fa-image",
			menulabel: CFWL('cfw_widget_cfwimage', "Image"),
			description: CFWL('cfw_widget_cfwimage_desc', "Displays an image."),
			createWidgetInstance: function (widgetObject, callback) {							
				callback(widgetObject, '<div class="dashboard-image w-100 h-100" style="background-image: url(\''+widgetObject.JSON_SETTINGS.url+'\');">');
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				var settingsForm = $(form);
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();
(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_image",
		{
			category: "Standard Widgets",
			menuicon: "far fa-image",
			menulabel: CFWL('cfw_widget_cfwimage', "Image"),
			description: CFWL('cfw_widget_cfwimage_desc', "Displays an image."),
			createWidgetInstance: function (widgetObject, callback) {							
				callback(widgetObject, '<div class="dashboard-image w-100 h-100" style="background-image: url(\''+widgetObject.JSON_SETTINGS.url+'\');">');
			},

		}
	);
})();
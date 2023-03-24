(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_image",
		{
			category: CFW.dashboard.global.categoryDefault,
			menuicon: "far fa-image",
			menulabel: CFWL('cfw_widget_cfwimage', "Image"),
			description: CFWL('cfw_widget_cfwimage_desc', "Displays an image."),
			createWidgetInstance: function (widgetObject, params, callback) {							
				callback(widgetObject, '<div class="dashboard-image w-100 h-100" style="background-image: url(\''+widgetObject.JSON_SETTINGS.url+'\');">');
			},

		}
	);
})();
(function (){
	CFW.dashboard.registerWidget("cfw_tags",
		{
			category: "Standard Widgets",
			menuicon: "fas fa-tags",
			menulabel: CFWL('cfw_widget_tags', "Tags"),
			description: CFWL('cfw_widget_tags_desc', "Displays a list of tags."),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				var tags = widgetObject.JSON_SETTINGS.tags;
				
				callback(widgetObject, CFW.format.arrayToBadges( tags.split(',') ) );
			},
			
		}
	);	
	
})();
(function (){

	CFW.dashboard.registerWidget("cfw_parameter",
		{
			category: "Standard Widgets",
			menuicon: "fas fa-sliders-h",
			menulabel: CFWL('cfw_widget_parameter', 'Parameter'),
			description: CFWL('cfw_widget_parameter_desc', 'Displays a widget with parameters that the viewer of the dashboard can adjust to customize the dashboard. User choices will be saved in the browser.'),
			
			createWidgetInstance: function (widgetObject, callback) {			
				
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var formHTML = data.payload.html;
					
					
					var parentDiv = $('<div class= "d-flex flex-column">');

					var noflexDiv = $('<div class="d-block w-100">');
					noflexDiv.append(settings.description);
					parentDiv.append(noflexDiv);
					
					var flex 
					parentDiv.append(formHTML);
					
					callback(widgetObject, parentDiv);
				});
			},
		}
	);
	
	function cfw_widget_paramater_fireParamUpdate(paramElement){
		console.log('param updated');
	}
	
})();
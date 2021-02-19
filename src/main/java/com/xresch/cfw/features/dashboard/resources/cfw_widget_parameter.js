

(function (){

	CFW.dashboard.registerWidget("cfw_parameter",
		{
			category: "Standard Widgets",
			menuicon: "fas fa-sliders-h",
			menulabel: CFWL('cfw_widget_parameter', 'Parameter'),
			description: CFWL('cfw_widget_parameter_desc', 'Displays a widget with parameters that the viewer of the dashboard can adjust to customize the dashboard. User choices will be saved in the browser.'),
			
			createWidgetInstance: function (widgetObject, callback) {			
				
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					if(data.payload == null){
						callback(widgetObject, '');
						return;
					}
					var settings = widgetObject.JSON_SETTINGS;
					var formHTML = data.payload.html;
					
					var parentDiv = $('<div class= "d-flex flex-column">');

					var noflexDiv = $('<div class="d-block w-100">');
					noflexDiv.append(settings.description);
					parentDiv.append(noflexDiv);
					
					parentDiv.append(formHTML);
					
					//----------------------------------
					// Apply Custum Viewer Settings from
					// Browser Store
					var storedViewerParams = cfw_dashboard_parameters_getStoredViewerParams();
					parentDiv.find('form input, form textarea, form select').each(function (){
						var inputField = $(this);
						var name = inputField.attr('name');
						
						var viewerCustomValue = storedViewerParams[name];
						if(!CFW.utils.isNullOrEmpty(viewerCustomValue)){
							inputField.val(viewerCustomValue);
						}
					});
					
					//----------------------------------
					// Callback
					callback(widgetObject, parentDiv);
				});
			},
		}
	);
	
})();
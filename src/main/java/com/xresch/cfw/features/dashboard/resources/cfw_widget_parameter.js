

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
					
					var parentDiv = $('<div class= "d-flex flex-column cfw-parameter-widget-marker">');

					var noflexDiv = $('<div class="d-block w-100">');
					noflexDiv.append(settings.description);
					parentDiv.append(noflexDiv);
					
					parentDiv.append(formHTML);
					
					//----------------------------------
					// Add to workspace and initialize
					// form fields
					CFW.ui.getWorkspace().append(parentDiv);
					CFW.http.evaluateFormScript(parentDiv);
					
					//----------------------------------
					// Apply Custom Viewer Settings from
					// Browser Store
					var storedViewerParams = cfw_dashboard_parameters_getStoredViewerParams();
					parentDiv.find('form input, form textarea, form select').each(function (){
						var inputField = $(this);
						var name = inputField.attr('name');
						var type = inputField.attr('type');
						
						var viewerCustomValue = storedViewerParams[name];
						if(!CFW.utils.isNullOrEmpty(viewerCustomValue)){
							if(type == 'radio'){
								//$('input[name="'+name+'"]').prop("checked", false);
								parentDiv.find('input[name="'+name+'"]').each(function(){
									var current = $(this);
									
									if(current.val() == ""+viewerCustomValue){
										current.prop("checked", 'checked');
									}else{
										current.prop("checked", false);
									}
								});
							}else if(inputField.hasClass('cfw-tags-selector')){
								var tagsInputValues = JSON.parse(viewerCustomValue);
								//must be initialized to add values
								for(var key in tagsInputValues){
									console.log( "value: "+ key+ ", label:"+ tagsInputValues[key])
									inputField.tagsinput('add', { "value": key , "label": tagsInputValues[key] });
								}
							}else{
								inputField.val(viewerCustomValue);
							}
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

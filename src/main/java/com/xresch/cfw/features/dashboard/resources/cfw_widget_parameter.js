
(function (){

	CFW.dashboard.registerWidget("cfw_parameter",
		{
			category: "Advanced",
			menuicon: "fas fa-sliders-h",
			menulabel: CFWL('cfw_widget_parameter', 'Parameter'),
			description: CFWL('cfw_widget_parameter_desc', 'Displays a widget with parameters that the viewer of the dashboard can adjust to customize the dashboard. User choices will be saved in the browser.'),
			defaultsettings: {
				TITLE: ""
			},
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					if(data.payload == null){
						callback(widgetObject, '');
						return;
					}
					var settings = widgetObject.JSON_SETTINGS;
					var formHTML = data.payload.html;
					
					
					var parentDiv = $('<div class="d-flex flex-column cfw-parameter-widget-parent w-100" data-widget-id="'+widgetObject.PK_ID+'">');

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
					if(settings.load_previous_values){
						var storedViewerParams = cfw_parameter_getStoredUserParams();
						
						parentDiv.find('input, textarea, select').each(function (){
							var inputField = $(this);
							var name = inputField.attr('name');
							var type = inputField.attr('type');
							
							//--------------------
							// Skip Hidden Fields
							if(type == "hidden"
							&& inputField.data("role") != "chartsettings"){ 
								return; 
							}
							
							//--------------------
							// Do others
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
								}
								
								//----------------------------------------------
								// Select
								else if (inputField.attr('cfwtype') == "SELECT"){
									cfw_setSelectValue(
										inputField.attr('id')
										, viewerCustomValue);
								}
								
								//----------------------------------------------
								// Boolean Switches
								else if (inputField.attr('cfwtype') == "BOOLEAN"){
									cfw_internal_setBooleanSwitchValue(
										inputField.closest('.cfw-switch')
										, viewerCustomValue);
								}
								
								//----------------------------------------------
								// Tag Selectors
								else if (inputField.hasClass('cfw-tags-selector')){
									var tagsInputValues = JSON.parse(viewerCustomValue);
									//must be initialized to add values
									inputField.tagsinput('removeAll');
									for(var key in tagsInputValues){
										inputField.tagsinput('add', { "value": key , "label": tagsInputValues[key] });
									}
									
								//----------------------------------------------
								// Tags
								}else if(inputField.hasClass('cfw-tags')){
									var tagsInputValues = viewerCustomValue.split(',');
									//must be initialized to add values
									inputField.tagsinput('removeAll');
									for(var index in tagsInputValues){
										inputField.tagsinput('add', tagsInputValues[index]);
									}
									
								//----------------------------------------------
								// Chart Settings
								}else if(inputField.data("role") == "chartsettings"){
									var chartsettingsValues = JSON.parse(viewerCustomValue);
									var wrapper = inputField.closest('.cfw-chartsettings-field-wrapper');
									 cfw_internal_applyChartSettings(inputField.attr('id'), wrapper, chartsettingsValues);
								}else{
									// stringify value, else it won't work properly with booleans
									inputField.val(""+viewerCustomValue);
								}
							}
						});
					}
					
					//----------------------------------
					// Callback
					$('#globalParams').html('');
					if( settings.addtotop != true ){
						callback(widgetObject, parentDiv);
					}else{
						parentDiv.find('form')
								 .css('width', '100%')
								 .css('flex-flow', 'wrap')
								 ;
						$('#cfw-dashboard-control-panel > .btn-group').addClass('mt-2');
						$('#globalParams')
								.html('')
								.removeClass('mt-2')
								.append(parentDiv)
								;
						callback(widgetObject, "<span>Added next to title</span>");
					}
					
					
					//----------------------------------
					// Hack overflow on widget for autocomplete lists

					window.setTimeout(function(){
						parentDiv.closest('.cfw-dashboard-widget-body')
								 .css('padding', '0.2rem')
								 .css('padding-right', '0.5rem')
								 .css('overflow', 'visible')
								 ;
					}, 500)
				});
			},
		}
	);
	
})();


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
					
					//---------------------------------
					// Clear Global Params Here
					// Reason: Else initialization of fields will
					// happen on existing fields, which would be deleted and new ones will not be initialized
					$('#globalParams').html('');
					
					//---------------------------------
					// Check Has Payload
					if(data.payload == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Initialize Variables
					let settings = widgetObject.JSON_SETTINGS;
					let formHTML = data.payload.html;
					
					
					let parentDiv = $('<div class="d-flex flex-column cfw-parameter-widget-parent w-100" data-widget-id="'+widgetObject.PK_ID+'">');

					let noflexDiv = $('<div class="d-block w-100">');
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
						let storedViewerParams = cfw_parameter_getStoredUserParams();
						
						parentDiv.find('input, textarea, select').each(function (){
							let inputField = $(this);
							let name = inputField.attr('name');
							let type = inputField.attr('type');
							
							//--------------------
							// Skip Hidden Fields
							if(type == "hidden"
							&& inputField.data("role") != "chartsettings"){ 
								return; 
							}
							
							//--------------------
							// Do others
							let viewerCustomValue = storedViewerParams[name];

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
									let tagsInputValues = JSON.parse(viewerCustomValue);
									//must be initialized to add values
									inputField.tagsinput('removeAll');
									for(let key in tagsInputValues){
										inputField.tagsinput('add', { "value": key , "label": tagsInputValues[key] });
									}
									
								//----------------------------------------------
								// Tags
								}else if(inputField.hasClass('cfw-tags')){
									let tagsInputValues = viewerCustomValue.split(',');
									//must be initialized to add values
									inputField.tagsinput('removeAll');
									for(let index in tagsInputValues){
										inputField.tagsinput('add', tagsInputValues[index]);
									}
									
								//----------------------------------------------
								// Chart Settings
								}else if(inputField.data("role") == "chartsettings"){
									let chartsettingsValues = JSON.parse(viewerCustomValue);
									let wrapper = inputField.closest('.cfw-chartsettings-field-wrapper');
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
						callback(widgetObject, "<span>Parameters added next to title</span>");
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

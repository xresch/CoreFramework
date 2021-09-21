(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-flask", "Server Side Category");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_replica",
		{
			category: "Standard Widgets",
			menuicon: "fas fa-copy",
			menulabel:  CFWL('cfw_widget_replica', 'Replica'),
			description: CFWL('cfw_widget_replica_desc', 'Replicates the contents of a widget from another dashboard.'),
			createWidgetInstance: function (widgetObject, params, callback) {		
				
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					if(data.payload != null && data.payload.length > 0){
						var replicatedWidgetObject = data.payload[0];
						
						if(replicatedWidgetObject.TYPE == 'cfw_replica'){
							CFW.ui.addToastWarning('Replication of Replica widgets is not supported.');
							return callback(widgetObject, '');
							
						}
						
						//prevent saving of widget
						replicatedWidgetObject.FK_ID_DASHBOARD	= null;
						
						// do not override, ID needed for fetching data for replicated widget
						//replicatedWidgetObject.PK_ID	= widgetObject.PK_ID;
						
						// override position, sizing and title
						replicatedWidgetObject.X 		= widgetObject.X;
						replicatedWidgetObject.Y 		= widgetObject.Y;
						replicatedWidgetObject.HEIGHT 	= widgetObject.HEIGHT;
						replicatedWidgetObject.WIDTH 	= widgetObject.WIDTH;
						replicatedWidgetObject.TITLE 	= widgetObject.TITLE;
						
						var replicatedDefinition = CFW.dashboard.getWidgetDefinition(replicatedWidgetObject.TYPE);
						
						var finalParams = cfw_dashboard_parameters_getFinalParams();
						let parameterizedSettings = cfw_dashboard_parameters_applyToFields(replicatedWidgetObject.JSON_SETTINGS, replicatedWidgetObject.TYPE, finalParams);
						let widgetCloneParameterized = _.cloneDeep(replicatedWidgetObject);
						widgetCloneParameterized.JSON_SETTINGS = parameterizedSettings;
						

						try{
							replicatedDefinition.createWidgetInstance(widgetCloneParameterized, params,
								function(subReplicatedWidgetObject, widgetContent, subWidgetObject = widgetObject){
									
									//---------------------------------------
									// Remove Placeholder
									var placeholderWidget = $('#'+subWidgetObject.guid);
									cfw_dashboard_widget_removeFromGrid(placeholderWidget);
									
									//---------------------------------------
									// Add Widget
									subWidgetObject.content = widgetContent;
									var widgetInstance = cfw_dashboard_widget_createHTMLElement(subWidgetObject);

									var grid = $('.grid-stack').data('gridstack');

								    grid.addWidget($(widgetInstance),
								    		subWidgetObject.X, 
								    		subWidgetObject.Y, 
								    		subWidgetObject.WIDTH, 
								    		subWidgetObject.HEIGHT, 
								    		false);
								   
								    //----------------------------
								    // Reload Widget from Instance
								    var subWidgetObject = $(widgetInstance).data('widgetObject');
								    
								    //----------------------------
								    // Check Edit Mode
								    if(!CFW_DASHBOARD_EDIT_MODE){
								    	grid.movable('#'+subWidgetObject.guid, false);
								    	grid.resizable('#'+subWidgetObject.guid, false);
								    }
								    //----------------------------
								    // Update Data
								    subWidgetObject.WIDTH	= widgetInstance.attr("data-gs-width");
								    subWidgetObject.HEIGHT	= widgetInstance.attr("data-gs-height");
								    subWidgetObject.X		= widgetInstance.attr("data-gs-x");
								    subWidgetObject.Y		= widgetInstance.attr("data-gs-y");

								}
							);
							}catch(err){
								CFW.ui.addToastDanger('An error occured while creating a widget instance: '+err.message);
								console.log(err);
							}
						    
							cfw_dashboard_widget_save_state(widgetObject);
						return;
					}
					
					return callback(widgetObject, '');
					
				});
				
			},
		}
	);
})();
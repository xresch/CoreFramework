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
			category: "Static Widgets",
			menuicon: "fas fa-copy",
			menulabel:  CFWL('cfw_widget_replica', 'Replica'),
			description: CFWL('cfw_widget_replica_desc', 'Replicates the contents of a widget from another dashboard.'),
			createWidgetInstance: function (widgetObject, callback) {		
				
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					if(data.payload != null && data.payload.length > 0){
						var replicatedWidgetObject = data.payload[0];
						
						if(replicatedWidgetObject.TYPE == 'cfw_replica'){
							CFW.ui.addToastWarning('Replication of Replica widgets is not supported.');
							return callback(widgetObject, '');
							
						}
						
						//prevent saving of widget
						replicatedWidgetObject.FK_ID_DASHBOARD	= null;
						replicatedWidgetObject.PK_ID	= widgetObject.PK_ID;
						replicatedWidgetObject.X 		= widgetObject.X;
						replicatedWidgetObject.Y 		= widgetObject.Y;
						replicatedWidgetObject.HEIGHT 	= widgetObject.HEIGHT;
						replicatedWidgetObject.WIDTH 	= widgetObject.WIDTH;
						replicatedWidgetObject.TITLE 	= widgetObject.TITLE;
						
						var replicatedDefinition = CFW.dashboard.getWidgetDefinition(replicatedWidgetObject.TYPE);
						
						try{
							var widgetInstance = replicatedDefinition.createWidgetInstance(replicatedWidgetObject, 
								function(replicatedWidgetObject, widgetContent, subWidgetObject = widgetObject){
									
									subWidgetObject.content = widgetContent;
									var widgetInstance = CFW.dashboard.createWidget(subWidgetObject);

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
						    
							cfw_dashboard_saveWidgetState(widgetObject);
						return;
					}
					
					return callback(widgetObject, '');
					
				});
				
			},
		}
	);
})();
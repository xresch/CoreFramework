
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-terminal", "Query");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_widget_queryresults",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-terminal",
			menulabel: CFWL('cfw_widget_queryresults', "Display Query Results"),
			description: CFWL('cfw_widget_queryresults_desc', "Executes a CFWQL query and display its results."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					//var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Render Data
					var targetDiv = $('<div class="flex-column align-items-stretch h-100 w-100">');
					cfw_query_renderAllQueryResults(targetDiv, data.payload);
				
					callback(widgetObject, targetDiv);
				});
			},
			
		}
	);	
	
})();

(function (){
	

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_triggerjobs",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-magic",
			menulabel: "Trigger Jobs",
			description: "Allows a user to start and stop jobs.", 
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
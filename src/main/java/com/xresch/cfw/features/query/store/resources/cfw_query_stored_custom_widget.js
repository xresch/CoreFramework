/******************************************************************
 * 
 ******************************************************************/
CFW.dashboard.registerCategory("fas fa-star", "Custom");
	
/******************************************************************
 * 
 ******************************************************************/
function createStoredQueryCustomWidget(queryName, widgetCategory, description){
		
		console.log("category: "+widgetCategory)
		console.log("queryName: "+queryName)
	//-------------------------
	// Get Description
	let finalDescription = "This is a custom widget based on a stored query.";
	if( ! CFW.utils.isNullOrEmpty(description) ){
		finalDescription += description;
	}
	
	return {
		category: widgetCategory,
		menuicon: "fas fa-star",
		menulabel: queryName,
		description: finalDescription, 
				
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
		
		getEditForm: function (widgetObject) { 
			return CFW.dashboard.getSettingsForm(widgetObject);
		},
				
		onSave: function (form, widgetObject) {
			widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
			return true;	
		}
	}
}	

(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-flask", "Server Side Category");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_helloworld",
		{
			category: "Easter Eggs",
			menuicon: "fas fa-font",
			menulabel: 'Hello World',
			description: CFWL('cfw_widget_helloworld_desc', 'Takes a name and greats a person.'),
			createWidgetInstance: function (widgetObject, params, callback) {		
				
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var helloString = 
						CFWL('cfw_widget_helloworld_hello', 'Hello')+' '
						+settings.name+'! '
						+ data.payload;
					
					var hobbies = settings.JSON_HOBBIES_SELECTOR;
					if(hobbies != null){
						helloString += '<div><h2>Hobbies</h2>';
						for(var key in hobbies){
							helloString += '<p>'+hobbies[key]+'</p>';
							
						}
						helloString += '</div>';
						
					}
										
					callback(widgetObject, helloString);
				});
				
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				var settingsForm = $(form);
				
				var doSave = ( settingsForm.find('input[name="dosave"]:checked').val() == "true" )
								
				if(doSave){
					widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
					return true;		
				}else{
					CFW.ui.addToastDanger('Wrong settings, cannot save the data.');
					return false;
				}
			}
		}
	);
})();
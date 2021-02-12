(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_lightswitch",
		{
			category: "Easter Eggs",
			menuicon: "fas fa-lightbulb text-cfw-yellow",
			menulabel: CFWL('cfw_widget_lightswitch', "Lightswitch"),
			description: CFWL('cfw_widget_lightswitch_desc', "Toggles the light on and off. Can be used to create screenshots. Will be messed up when dashboard gets reloaded."),
			defaulttitle: "",
			defaultwidth: 4,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, callback) {		
				var html = 
					 '<button class="btn btn-sm text-cfw-yellow bg-cfw-black fas fa-lightbulb" onclick="cfw_widget_lightswitch_toggleLight()" style="height: 100%; width:100%;"></button>'
					;
				callback(widgetObject, html);
				
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();

var CFW_LIGHTSWITCH_STATE = 'none'; 
function cfw_widget_lightswitch_toggleLight() {
	
	var selectorItemsToChange = 'body, .grid-stack-item .card, #top-ruler';
	switch(CFW_LIGHTSWITCH_STATE){
		case 'none': 
			$(selectorItemsToChange).addClass('cfw-easteregg-lightswitch-on');
			CFW_LIGHTSWITCH_STATE = 'on';
			break;
		case 'on': 
			$(selectorItemsToChange).removeClass('cfw-easteregg-lightswitch-on');
			$(selectorItemsToChange).addClass('cfw-easteregg-lightswitch-off');
			CFW_LIGHTSWITCH_STATE = 'off';
			break;
			
		case 'off': 
			$(selectorItemsToChange).removeClass('cfw-easteregg-lightswitch-off');
			CFW_LIGHTSWITCH_STATE = 'none';
			break;
		
	
	}
	
}
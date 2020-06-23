(function (){

	CFW.dashboard.registerWidget("cfw_list",
		{
			category: "Static Widgets",
			menuicon: "fas fa-list-ol",
			menulabel: CFWL('cfw_widget_cfwlist', 'List'),
			description: CFWL('cfw_widget_cfwlist_desc', 'Displays a list. Every line in the text will be shown as a separate item.'),
			
			createWidgetInstance: function (widgetObject, callback) {			
				
				//-------------------------
				//Create List HTML
				var lines = widgetObject.JSON_SETTINGS.content.trim().split(/\r\n|\r|\n/);
				
				var listHTML = '<ul>';
				if(widgetObject.JSON_SETTINGS.isordered){ listHTML = '<ol>';}
				
			 	for(var i = 0; i < lines.length; i++){
			 		listHTML += '<li>'+lines[i].trim()+"</li>";
			 	 	
			 	}
			 	(widgetObject.JSON_SETTINGS.isordered) ? listHTML += '<ol>' : listHTML += '</ul>';
			 		
				//-------------------------
				// Render
				var textRenderer = CFW.render.getRenderer('html');
				var content = textRenderer.render({data: listHTML});
				
				callback(widgetObject, content);
				
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
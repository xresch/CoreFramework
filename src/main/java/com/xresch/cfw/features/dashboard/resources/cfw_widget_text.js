(function (){

	CFW.dashboard.registerWidget("cfw_text",
		{
			category: "Static Widgets",
			menuicon: "fas fa-font",
			menulabel: CFWL('cfw_widget_cfwtext', 'Text'),
			description: CFWL('cfw_widget_cfwtext_desc', 'Display static text. Can be used to create labels and descriptions.'),
			
			createWidgetInstance: function (widgetObject, callback) {			
				
				var textRenderer = CFW.render.getRenderer('html');
				var adjustedText = widgetObject.JSON_SETTINGS.content;
				if(widgetObject.JSON_SETTINGS.content != null){
					adjustedText = adjustedText.replace(/\r\n|\r|\n/,'<br>');
					callback(widgetObject, adjustedText);
				}else{
					callback(widgetObject, '');
				}

				
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
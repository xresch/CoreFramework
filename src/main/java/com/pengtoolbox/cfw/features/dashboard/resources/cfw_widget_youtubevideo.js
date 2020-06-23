(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_youtubevideo",
		{
			category: "Static Widgets",
			menuicon: "fab fa-youtube",
			menulabel: CFWL('cfw_widget_cfwyoutubevideo', "Youtube Video"),
			description: CFWL('cfw_widget_cfwyoutubevideo_desc', "Adds a Youtube video to the dashboard."),
			
			createWidgetInstance: function (widgetObject, callback) {
				
				if(widgetObject.JSON_SETTINGS.url == null){
					callback(widgetObject, '');
					return;
				}
				
				var finalURL = '';
				if(widgetObject.JSON_SETTINGS.url.includes('/embed')){
					finalURL = widgetObject.JSON_SETTINGS.url;
				}else if(widgetObject.JSON_SETTINGS.url.includes('/watch?v=')){
					var regex = /watch\?v=([^&]+)/g;
					var groups = regex.exec(widgetObject.JSON_SETTINGS.url);
					  
					finalURL = 'https://www.youtube.com/embed/'+groups[1];
				}else if(widgetObject.JSON_SETTINGS.url.includes('youtu.be/')){
					var regex = /youtu.be\/(.+)/g;
					var groups = regex.exec(widgetObject.JSON_SETTINGS.url);
					  
					finalURL = 'https://www.youtube.com/embed/'+groups[1];
				}
					
				
				callback(widgetObject, '<iframe class="w-100 h-100" src="'+finalURL
						+'" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>');
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
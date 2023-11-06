(function (){

	CFW.dashboard.registerWidget("cfw_label",
		{
			category: CFW.dashboard.global.categoryDefault,
			menuicon: "fas fa-tag",
			menulabel: CFWL('cfw_widget_cfwlabel', 'Label'),
			description: CFWL('cfw_widget_cfwlabel_desc', 'Displays a vertical label.'),
			defaultsettings: {
				TITLE: "",
				WIDTH: 8,
				HEIGHT: 3,
			},
			createWidgetInstance: function (widgetObject, params, callback) {	
				var settings = widgetObject.JSON_SETTINGS;
				if(settings.label != null){
					var rotationClass = '';
					
					if(settings.direction == "Top to Bottom"){
						rotationClass = 'rotate-90';
					}else if(settings.direction == "Bottom to Top"){
						rotationClass = 'rotate-270';
					}else if(settings.direction == "Upside Down"){
						rotationClass = 'rotate-180';
					}
					
					var labelHTML = '<div class="label-box"><span class="text-center '+rotationClass+'" style="zoom: '+100*settings.sizefactor+'%;">';
					
					var target = 'target="_blank"';
					if(settings.newwindow != null && !settings.newwindow){
						target = "" ;
					}
					if(widgetObject.JSON_SETTINGS.link != null && widgetObject.JSON_SETTINGS.link != ''){
						labelHTML += '<a '+target+' class="text-'+widgetObject.FGCOLOR+'" href="'+widgetObject.JSON_SETTINGS.link+'">'+widgetObject.JSON_SETTINGS.label+'</a>'
						+'</span></div>'; 
					}else{
						labelHTML += widgetObject.JSON_SETTINGS.label
						+'</span></div>'; 
					}
					
					callback(widgetObject, labelHTML);
				}else{
					callback(widgetObject, '');
				}

			},
						
		}
	);
		
})();
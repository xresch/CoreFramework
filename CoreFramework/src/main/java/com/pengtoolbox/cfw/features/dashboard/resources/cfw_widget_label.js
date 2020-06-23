(function (){

	CFW.dashboard.registerWidget("cfw_label",
		{
			category: "Static Widgets",
			menuicon: "fas fa-tag",
			menulabel: CFWL('cfw_widget_cfwlabel', 'Label'),
			description: CFWL('cfw_widget_cfwlabel_desc', 'Displays a vertical label.'),
			defaulttitle: "",
			defaultwidth: 4,
			defaultheight: 1,
			createWidgetInstance: function (widgetObject, callback) {	
				var settings = widgetObject.JSON_SETTINGS;
				if(settings.label != null){
					var rotationClass = '';
					
					if(widgetObject.JSON_SETTINGS.direction == "Top to Bottom"){
						rotationClass = 'rotate-90';
					}else if(widgetObject.JSON_SETTINGS.direction == "Bottom to Top"){
						rotationClass = 'rotate-270';
					}else if(widgetObject.JSON_SETTINGS.direction == "Upside Down"){
						rotationClass = 'rotate-180';
					}
					
					var labelHTML = '<div class="label-box"><span class="text-center '+rotationClass+'" style="white-space: nowrap; font-size: '+24*settings.sizefactor+'px;"">';
					
					if(widgetObject.JSON_SETTINGS.link != null && widgetObject.JSON_SETTINGS.link != ''){
						labelHTML += '<a target="_blank" class="text-'+widgetObject.FGCOLOR+'" href="'+widgetObject.JSON_SETTINGS.link+'">'+widgetObject.JSON_SETTINGS.label+'</a>'
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
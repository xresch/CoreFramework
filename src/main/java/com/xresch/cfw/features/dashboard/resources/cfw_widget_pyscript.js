(function (){

	CFW.dashboard.registerWidget("cfw_pyscript",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fab fa-python",
			menulabel: CFWL('cfw_widget_pyscript', 'Py Script'),
			description: CFWL('cfw_widget_pyscript_desc', 'Widget to add a py-script tag to a page.'),
			
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				var settings = widgetObject.JSON_SETTINGS;
				if(settings.pyscript != null){
					
					//---------------------------
					// Add PyScript JS and CSS
					var mainScriptID = 'py-scriptjs';
					var scriptElement = $('#'+mainScriptID);
					if(scriptElement.length == 0){
						$('#javascripts').append('<script defer id="'+mainScriptID+'" src="https://pyscript.net/latest/pyscript.js"></script>');
					}
					
					// messes up 
					/*var mainCssID = 'py-scriptcss';
					var cssElement = $('#'+mainCssID);
					if(cssElement.length == 0){
						$('head').append('<link id="'+mainCssID+'" rel="stylesheet" href="https://pyscript.net/latest/pyscript.css">');
					}*/
					
					
					
					//---------------------------
					// Replace if exists
					var elementID = 'py-script-'+widgetObject.PK_ID;
					var targetID = 'py-script-target-'+widgetObject.PK_ID;
					
					var scriptElement = $('#'+elementID);
					if(scriptElement.length > 0){
						scriptElement.remove();
					}
					
					//---------------------------
					// Add script
					var finalScript = 'from js import document \r\n'
									+ 'targetID = "'+targetID+'";\r\n'
									+ 'widget = document.querySelector("#"+targetID);  \r\n' 
								 	+ settings.pyscript;
					var script = $('<py-script id="'+elementID+'" class="cfw-py-script">'+finalScript+"</py-script>")
					$('#javascripts').append(script);
					
					//---------------------------
					// Add widget target div
					
					
					var target = $('<div id="'+targetID+'" class="w-100 h-100">'+CFW.ui.createLoaderHTML()+'</div>');
					callback(widgetObject, target);
				}
				
				

			},
						
		}
	);
	
})();
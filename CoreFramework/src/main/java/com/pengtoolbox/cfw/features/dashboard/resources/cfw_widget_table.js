(function (){
	CFW.dashboard.registerWidget("cfw_table",
		{
			category: "Static Widgets",
			menuicon: "fas fa-table",
			menulabel: CFWL('cfw_widget_csvtable', "CSV Table"),
			description: CFWL('cfw_widget_csvtable_desc', "Takes values in CSV format and displays them as a table."),
			createWidgetInstance: function (widgetObject, callback) {
					
				var separator = widgetObject.JSON_SETTINGS.separator;
				var tableData = widgetObject.JSON_SETTINGS.tableData;
				
				var dataToRender = tableData;
				
				if(typeof tableData == 'string'){
					var objectArray = CFW.format.csvToObjectArray(tableData, separator);
					
					dataToRender = {
						data: objectArray,
						rendererSettings:{
							table: {
								narrow: 		widgetObject.JSON_SETTINGS.narrow,
								filterable: 	widgetObject.JSON_SETTINGS.filterable,
								striped: 		widgetObject.JSON_SETTINGS.striped,
							}
					}};
				}
				
				//--------------------------
				// Get Values
				var tableRenderer = CFW.render.getRenderer('table');
				var cfwTable = tableRenderer.render(dataToRender);

				callback(widgetObject, cfwTable);
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				var settingsForm = $(form);
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
		}
	);	
	
})();
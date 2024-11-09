(
	function() {

		// Register your category, will be ignored if a category with this name already exists
		CFW.dashboard.registerCategory("fas fa-magic", "Command Line");

		// Register the widget with an unique name - must be the same name specified as in the java widget class
		CFW.dashboard.registerWidget("cfw_cliextensions_results",
			{
				category: 'Command Line',
				menuicon: 'fas fa-magic',
				menulabel: 'CLI Results',
				description: 'Executes commands on the command line and shows the output.',
				createWidgetInstance: function (widgetObject, params, callback) {

					CFW.dashboard.fetchWidgetData(widgetObject, params, function(data) {

						var settings = widgetObject.JSON_SETTINGS;

						//--------------------
						// Handle Empty Response
						if (data.payload == null) {
							callback(widgetObject, "No Data");
							return;
						}


						//-------------------------------
						// Create Respones Body Code Block
						var output = data.payload.output;
						var pre = $("<pre>");
						var code = $("<code>");
						code.text(output)
						pre.append(code);
															
						callback(widgetObject, pre);
						//callback(widgetObject, "Debug Info");
						return;

						
					})

				}
			})

	}
)();
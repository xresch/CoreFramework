/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT
 **************************************************************************************************************/
const CFW_LOGCONFIG_URL='./logconfiguration';


/******************************************************************
 * 
 ******************************************************************/
function cfw_logconfiguration_createLevelSelector(loggerName, currentLevel){

	var $selector = $(
			'<select>'
				+'<option value="ALL">ALL</option>'
				+'<option value="FINEST">FINEST</option>'
				+'<option value="FINER">FINER</option>'
				+'<option value="FINE">FINE</option>'
				+'<option value="CONFIG">CONFIG</option>'
				+'<option value="INFO">INFO</option>'
				+'<option value="WARNING">WARNING</option>'
				+'<option value="SEVERE">SEVERE</option>'
				+'<option value="OFF">OFF</option>'
			+ '</select>');
	
	$selector.val(currentLevel);
	
	$selector.on('change', function(){

		var newLevel = $(this).val();
		
		CFW.ui.confirmExecute('Change the level of "'+loggerName+'" from '+currentLevel+' to '+newLevel+' ?', "Let's Go!", function(){
			CFW.http.getJSON(CFW_LOGCONFIG_URL, {action: "update", item: "loglevel", loggerName: loggerName, level: newLevel}, function(data){
				
				//-----------------------------------
				// Update Table
				cfw_logconfiguration_fetchLoggersAndDisplay();
				
				//-----------------------------------
				// Check Success
				if(!data.success){
					CFW.ui.addToastDanger('Error updating Log level.');
				}
			});
		})
	})
	
	return $selector;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_logconfiguration_reloadConfigFile(){

	
	CFW.ui.confirmExecute('Do you want to reload the log configuration from the properties file?', "Oh Yeah!", function(){
		CFW.http.getJSON(CFW_LOGCONFIG_URL, {action: "update", item: "configfile"}, function(data){
			
			//-----------------------------------
			// Update Table
			cfw_logconfiguration_fetchLoggersAndDisplay();
			
		});
	});


}

/******************************************************************
 * 
 ******************************************************************/
function cfw_logconfiguration_fetchLoggersAndDisplay(){

	CFW.http.getJSON(CFW_LOGCONFIG_URL, {action: "fetch", item: "loggers"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['name'],
				 	titledelimiter: ' ',
				 	labels: {},
				 	customizers: {
				 		level: function(record, value){
				 			return cfw_logconfiguration_createLevelSelector(record.name, record.level);
				 		}
				 	},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#loggers").html('').append(renderResult);
		}
	});
	
}



/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_logconfiguration_draw(){
	
	CFW.ui.toogleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Fetch and Print Loggers
		parent.append('<h1>Log Configuration</h1>'
				+'<p>List of all the registered loggers. You can change the log levels during runtime, these settings will be reverted after a application restart or with below reload button.</p>'
				+'<p><strong style="color: red;">IMPORTANT: </strong>Changing log levels can cause severe performance degradations. Make sure you know what you are doing.</p>'
				+ '<p><a href="#" class="btn btn-sm btn-primary" onclick="cfw_logconfiguration_reloadConfigFile();"><i class="fas fa-upload mr-2"></i>Reload from File</a></p>'
				+'<p id="loggers"></p>');

		cfw_logconfiguration_fetchLoggersAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 50);
}
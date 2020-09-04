/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='./loggers';


/******************************************************************
 * 
 ******************************************************************/
function cfw_loggers_fetchLoggersAndDisplay(){

	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "loggers"}, function(data){
		
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
				 	},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#loggers").append(renderResult);
		}
	});
	
}



/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_loggers_draw(){
	
	CFW.ui.toogleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Fetch and Print Loggers
		parent.append('<h2>Loggers</h2>'
				+'<p>List of all the registered loggers.</p>'
				+'<p id="loggers"></p>');

		cfw_loggers_fetchLoggersAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 50);
}
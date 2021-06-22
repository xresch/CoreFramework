/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='./sessionoverview';
	
/******************************************************************
 * 
 ******************************************************************/
function cfw_sessionoverview_fetchsessionoverviewAndDisplay(){

	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "sessionoverview"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data			
			var timestampFormatter = function(record, value){ return CFW.format.epochToTimestamp(value); };
			var durationFormatter = function(record, value){ return CFW.format.millisToDuration(value); };
			
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['USERNAME', 'SESSION_ID'],
				 	visiblefields: ['USERNAME', 'FIRSTNAME', 'LASTNAME', 'SESSION_ID', 'CREATION_TIME',  
				 					'LAST_ACCESS_TIME','ALIVE_TIME','SESSION_TIMOUT', 'EXPIRATION_TIME', 'CLIENT_IP'],
				 	titleformat: '{0}',
				 	labels: {
				 		CLIENT_IP: 'Client IP',
				 	},
				 	customizers: {
				 		CREATION_TIME: timestampFormatter,
				 		LAST_ACCESS_TIME: function(record, value){ 
				 			var now = moment.utc().valueOf();
				 			return CFW.format.millisToDuration(now-value); 
				 		},
				 		EXPIRATION_TIME: timestampFormatter,
				 		SESSION_TIMOUT: durationFormatter,
				 		ALIVE_TIME: durationFormatter,
				 	},
					data: data.payload,
					rendererSettings: {
						dataviewer: {
							storeid: 'sessionoverview',
							renderers: [
								{	label: 'Table',
									name: 'table',
									renderdef: {
										rendererSettings: {
											table: {filterable: false},
										},
									}
								},
								{	label: 'Smaller Table',
									name: 'table',
									renderdef: {
										actions: [],
										rendererSettings: {
											table: {filterable: false, narrow: true},
										},
									}
								},
								{	label: 'Panels',
									name: 'panels',
									renderdef: {}
								},
								{	label: 'Cards',
									name: 'cards',
									renderdef: {}
								},
								{	label: 'Tiles',
									name: 'tiles',
									renderdef: {
										visiblefields: ['LAST_ACCESS_TIME', 'EXPIRATION_TIME', 'CLIENT_IP',],
										rendererSettings: {
											tiles: {
												popover: false,
												border: '2px solid black'
											},
										},
										
									}
								},
								{	label: 'CSV',
									name: 'csv',
									renderdef: {}
								},
								{	label: 'JSON',
									name: 'json',
									renderdef: {}
								}
							]
						},
						table: {narrow: true, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
			
			$("#targetContainer").append(renderResult);
		}
	});
	
}



/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_sessionoverview_draw(){
	
	CFW.ui.toogleLoader(true);
	
	
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.addClass('maxvw-90');
		parent.html('');
		
		//-------------------------------------
		// Fetch and Print
				
		parent.append('<h2>Session Overview</h2>'
				+'<p>'
				+'An Overview of all open sessions.'
				+'</p>'
				+'<p id="targetContainer"></p>');

		cfw_sessionoverview_fetchsessionoverviewAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 50);
}
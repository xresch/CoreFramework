/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='./statusmonitor';

/******************************************************************
 * 
 ******************************************************************/
function cfw_statusmonitor_fetchStatusMonitorsAndDisplay(){

	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "statusmonitor"}, function(data){
		
		if(data.payload != null){
			
			$("#statusmonitor").html('');
			payload = data.payload
			//-----------------------------------
			// Render Data
			var rendererSettings = {
					data: null,
				 	idfield: null,
				 	bgstylefield: "bgstyle",
				 	textstylefield: null,
				 	titlefields: ['name'],
				 	visiblefields: ['status', 'name'],
				 	titleformat: '{0} {1}',
				 	labels: {},
				 	customizers: {},
					rendererSettings: {
						table: {narrow: false, filterable: true}
					}
				};
			
			for(let category in payload.categories)	{	
				let categoryArray = payload.categories[category];
				rendererSettings.data = categoryArray;
				
				for(let index in categoryArray){
					let current = categoryArray[index];
					current.bgstyle = CFW.colors.getCFWStateStyle(current.status);
				}
				
				let renderResult = CFW.render.getRenderer('statuslist').render(rendererSettings);
				
				$("#statusmonitor").append('<h3>'+category+'</h3>');
				$("#statusmonitor").append(renderResult);
			}	
			
			
		}
	});
	
}


/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_statusmonitor_draw(){
	
	CFW.ui.toggleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Fetch and Print Cache Statistics
				
		parent.append('<h2>Status Monitor</h2>'
				+`This page shows the statuses of the internal monitors.`
				+'</p>'
				+'<p id="statusmonitor"></p>');

		cfw_statusmonitor_fetchStatusMonitorsAndDisplay();
			
		CFW.ui.toggleLoader(false);
	}, 50);
}
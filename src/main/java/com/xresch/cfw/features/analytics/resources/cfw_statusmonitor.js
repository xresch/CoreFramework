/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT
 **************************************************************************************************************/

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
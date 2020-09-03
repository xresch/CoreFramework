/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='./cachestatistics';


/******************************************************************
 * 
 ******************************************************************/
function cfw_cachestatistics_fetchcachestatisticsAndDisplay(){

	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "cachestatistics"}, function(data){
		
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
				 		name: function(record, value){ return '<div class="mvw-30 word-wrap-break">'+value+'</div>'; },
				 		hit_rate: function(record, value){ return (value*100).toFixed(1)+"%"; },
				 		miss_rate: function(record, value){ return (value*100).toFixed(1)+"%"; },
				 		load_time_avg: function(record, value){ return value.toFixed(2)+"ms"; },
				 		load_time_sum: function(record, value){ return value.toFixed(2)+"ms"; },
				 		saved_time: function(record, value){ return (value/1000).toFixed(2)+"s"; },
				 	},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#cachestatistics").append(renderResult);
		}
	});
	
}



/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_cachestatistics_draw(){
	
	CFW.ui.toogleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Fetch and Print Cache Statistics
				
		parent.append('<h2>Cache Statistics</h2>'
				+'<p>Statistics for all registered caches.</p>'
				+'<p id="cachestatistics"></p>');

		cfw_cachestatistics_fetchcachestatisticsAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 50);
}
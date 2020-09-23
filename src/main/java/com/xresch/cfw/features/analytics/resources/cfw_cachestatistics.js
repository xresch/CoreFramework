/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='./cachestatistics';


/******************************************************************
 * 
 ******************************************************************/
function cfw_cachestatistics_showCacheDetails(element){
	var cacheName = $(element).html();
	
	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "cachedetails", name: cacheName}, function(data){
		
		if(data.payload != null){
			
			//-----------------------------------
			// Create Entry Table
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['key'],
				 	titledelimiter: ' ',
				 	labels: {},
				 	customizers: {
				 		key: function(record, value){ return '<p class="maxvw-15 word-wrap-break">'+value+'</p>'; },
				 		value: function(record, value){ 
				 			let adjustedValue = value.replace(/\n/g, "<br />");
				 			return $('<p class="word-break-all word-wrap-break">').text(adjustedValue); 
				 		},
				 	},
					data: data.payload.entries,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			//-----------------------------------
			// Create Details
			var $details = $('<div>');
			$details.append(
					'<ul>'
						+'<li><strong>Cache Name: </strong> '+data.payload.name+'</li>'
						+'<li><strong>Total Entries(estimated): </strong> '+data.payload.entry_count+'</li>'
						+'<li><strong>Entry Type: </strong> '+data.payload.clazz +'</li>'
					+'</ul>'
			);
			$details.append(renderResult);
			CFW.ui.showModal("Cache Details", $details, null);
		}
	});
	
}

	
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
				 		name: function(record, value){ return '<a href="#" class="maxvw-30 word-wrap-break" onclick="cfw_cachestatistics_showCacheDetails(this)">'+value+'</a>'; },
				 		hit_rate: function(record, value){ return (value*100).toFixed(1)+"%"; },
				 		miss_rate: function(record, value){ return (value*100).toFixed(1)+"%"; },
				 		load_time_avg: function(record, value){ return value.toFixed(2)+"ms"; },
				 		load_time_sum: function(record, value){ return value.toFixed(2)+"ms"; },
				 		load_time_saved: function(record, value){ return (value/1000).toFixed(2)+"s"; },
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
				+'<p>'
				+'Statistics for all registered caches. The Load Time statistics are inaccurate in some cases and are much lower than the actual time needed for loading. '
				+'<br/> Click on a cache name to get a list of the first 100 entries in the cache.'
				+'The value of the entries will either be the value itself if values are Strings, or a JSON representation of the cached object.'
				+'Only the first 500 characters are shown for an entry, longer entries will be truncated.'
				+'</p>'
				+'<p id="cachestatistics"></p>');

		cfw_cachestatistics_fetchcachestatisticsAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 50);
}
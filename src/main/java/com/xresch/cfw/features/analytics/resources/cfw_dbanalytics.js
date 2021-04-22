
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

CFW_DBANALYTICS_URL = "./dbanalytics";

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_createDatabaseSnapshot(){
	CFW.http.getJSON(CFW_DBANALYTICS_URL, {action: "dbsnapshot"});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_fetchConnectionPoolStatsAndDisplay(){

	CFW.http.getJSON(CFW_DBANALYTICS_URL, {action: "fetch", item: "connectionpoolstats"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var durationFormatter = function(record, value){ return CFW.format.millisToDuration(value); };
			
			var rendererSettings = {
				 	idfield: null,
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['NAME'],
				 	titleformat: '{0}',
				 	//visiblefields: [],
				 	labels: {},
				 	customizers: {
				 		MAX_CONNECTION_LIFETIME: durationFormatter,
				 		EVICTION_INTERVAL: durationFormatter,
				 	},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#table-connectionpool-stats").append(renderResult);
		}
	});
	
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_fetchRowCountAndDisplay(){
	
	CFW.http.getJSON(CFW_DBANALYTICS_URL, {action: "fetch", item: "tablerowcount"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var rendererSettings = {
					idfield: 'PK_ID',
					bgstylefield: null,
					textstylefield: null,
					titlefields: ['TABLE_NAME'],
					titleformat: '{0}',
					visiblefields: ['TABLE_NAME', 'ROW_COUNT'],
					labels: {},
					customizers: {},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
			};
			
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#table-row-count").append(renderResult);
		}
	});
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_fetchQueryStatisticsAndDisplay(){

	CFW.http.getJSON(CFW_DBANALYTICS_URL, {action: "fetch", item: "querystatistics"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['STATEMENT'],
				 	titleformat: '{0}',
				 	labels: {},
				 	customizers: {
				 		STATEMENT: function(record, value){ return '<div class="maxvw-30 word-wrap-break">'+value+'</div>';}
				 	},
					data: data.payload,
					rendererSettings: {
						table: {narrow: false, filterable: true}
					},
				};
					
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			$("#querystatistics").append(renderResult);
		}
	});
	
}



/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_dbanalytics_draw(){
	
	CFW.ui.toogleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Create Actions
		
		parent.append('<h1>Database Analytics</h1>');
		parent.append('<h2>Actions</h2>');
		parent.append(
			'<p>'
				+ '<a href="#" class="btn btn-sm btn-primary" onclick="cfw_dbanalytics_createDatabaseSnapshot();"><i class="fas fa-camera-retro mr-2"></i>Create Database Snapshot</a>'
			+'</p>'
		);
		
		parent.append('<h2>DB Connection Pool Statistics</h2>'
				+'<p>Statistics of managed connection pools.</p>'
				+'<p id="table-connectionpool-stats"></p>');
		
		parent.append('<h2>Table Row Count</h2>'
				+'<p>Number of rows for each table in the database.</p>'
				+'<p id="table-row-count"></p>');
		
		parent.append('<h2>SQL Statement Statistics</h2>'
				+'<p>Statistic for the top SQL statements. All times in milliseconds.</p>'
				+'<p id="querystatistics"></p>');
		
		
		cfw_dbanalytics_fetchConnectionPoolStatsAndDisplay();
		cfw_dbanalytics_fetchRowCountAndDisplay();
		cfw_dbanalytics_fetchQueryStatisticsAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 100);
}
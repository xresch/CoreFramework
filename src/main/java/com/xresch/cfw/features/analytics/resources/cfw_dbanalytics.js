
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/



/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_createDatabaseSnapshot(){

	CFW.http.getJSON("./dbanalytics", {action: "dbsnapshot"});
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_fetchRowCountAndDisplay(){

	CFW.http.getJSON("./dbanalytics", {action: "fetch", item: "tablerowcount"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['TABLE_NAME'],
				 	titledelimiter: ' ',
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

	CFW.http.getJSON("./dbanalytics", {action: "fetch", item: "querystatistics"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var rendererSettings = {
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['STATEMENT'],
				 	titledelimiter: ' ',
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
		
		parent.append('<h2>Table Row Count</h2>'
				+'<p>Number of rows for each table in the database.</p>'
				+'<p id="table-row-count"></p>');
		
		parent.append('<h2>SQL Statement Statistics</h2>'
				+'<p>Statistic for the top SQL statements. All times in milliseconds.</p>'
				+'<p id="querystatistics"></p>');
		
		
		cfw_dbanalytics_fetchRowCountAndDisplay();
		cfw_dbanalytics_fetchQueryStatisticsAndDisplay();
			
		CFW.ui.toogleLoader(false);
	}, 100);
}

var CFW_QUERY_URLPARAMS = CFW.http.getURLParamsDecoded();
var CFW_QUERY_URL="/app/query";

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_execute(){
	
	var targetDiv = $('#cfw-query-results');
	var timeframe = JSON.parse($('#timeframePicker').val());
	var query =  $('#query').val();
	
	params = {action: "execute"
			, item: "query"
			, query: query
			, offset: timeframe.offset
			, earliest: timeframe.earliest
			, latest: timeframe.latest
			};
			
	CFW.http.getJSON(CFW_QUERY_URL, params, 
		function(data) {
			
			targetDiv.html("");
			
			if(data.success){
				
				//-----------------------------------
				// Add Params to URL
				CFW.http.removeURLParam('query');
				
				if(timeframe.offset != null){
					CFW.http.removeURLParam('earliest');
					CFW.http.removeURLParam('latest');
					CFW.http.setURLParam('offset', timeframe.offset);
				}else{
					CFW.http.setURLParam('earliest', timeframe.earliest);
					CFW.http.setURLParam('latest', timeframe.latest);
					CFW.http.removeURLParam('offset');
				}
				CFW.http.setURLParam('query', query);
						
				//-----------------------------------
				// Iterate all Query results
				for(var i = 0; i < data.payload.length; i++){
					var currentResults = data.payload[i];
					targetDiv.append('<h2>Query '+(i+1)+'</h2>')
					var rendererSettings = {
							data: currentResults.results,
						 	//idfield: 'PK_ID',
						 	bgstylefield: null,
						 	textstylefield: null,
						 	titlefields: null,
						 	titleformat: '{0}',
						 	visiblefields: null,
						 	labels: {
						 		PK_ID: "ID",
						 	},
						 	customizers: {},
		
							rendererSettings: {
								dataviewer: {
									storeid: 'cfw-query',
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
												visiblefields: ['PK_ID', 'LOCATION', "EMAIL", "LIKES_TIRAMISU"],
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
										{	label: 'XML',
											name: 'xml',
											renderdef: {}
										},
										{	label: 'JSON',
											name: 'json',
											renderdef: {}
										}
									],
								},
							},
						};
				
					var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
					
					targetDiv.append(renderResult);
				}
			}
	});
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_resizeTextareaToFitQuery(){
		
		var queryString = $('#query').val();
		
		if( !CFW.utils.isNullOrEmpty(queryString) ){
			var oldLineCount = $('#query').attr('rows');
			var currentLineCount = queryString.split(/\r\n|\n/).length + 1;
			
			if(currentLineCount <= 23 && oldLineCount < currentLineCount) {
				$('#query').attr('rows', currentLineCount);
			}
		}
}
		
/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_initialDraw(){
	
	//-----------------------------------
	// Prepare HTML Base
	var parent = $('#cfw-container');
	var formID = JSDATA.formID;
	parent.css('max-width', '100%');
	
	parent.append(`
		<div id="cfw-query-content-wrapper">
			<div class="row mb-2">
				<div class="col-12 d-flex justify-content-end">
					<input id="timeframePicker" name="timeframePicker" type="text" class="form-control">
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-save"></i></button>
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-star"></i></button>
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-history"></i></button>
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_execute();"><b>Execute</b></button>
				</div>
				
			</div>
			<div class="row">
				<div class="col-12">
					<form id="${formID}">
						<input id="cfw-formID" name="cfw-formID" type="hidden" value="${formID}">
						<textarea id="query" name="query" class="form-control" rows="3" placeholder="Write your query, use Ctrl+Space for content assist..."></textarea>
					</form>
				</div>
			</div>
			
		</div>
		
		<div id="cfw-query-results">
		</div>
	`);
	
	//-------------------------------------------------
	// Initialize Autocomplete, trigger with Ctrl+Space
	cfw_autocompleteInitialize(formID,'query',0,10, null, true);
	
	//-----------------------------------
	// Load Timeframe from URL or set default
	if(!CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.offset)){
		cfw_initializeTimeframePicker('timeframePicker', {offset: CFW_QUERY_URLPARAMS.offset}, null);
	}else{
		if(!CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.earliest)
		&& !CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.latest) ){
			cfw_initializeTimeframePicker('timeframePicker', {earliest: CFW_QUERY_URLPARAMS.earliest, latest: CFW_QUERY_URLPARAMS.latest}, null);
		}else{
			cfw_initializeTimeframePicker('timeframePicker', {offset: '1-h'}, null);
		}
	}
	
	//-----------------------------------
	// Load Query from URL
	if( !CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.query) ){

		$('#query').val(CFW_QUERY_URLPARAMS.query);
		cfw_query_resizeTextareaToFitQuery();
		cfw_query_execute();
	}
	
	//-----------------------------------
	// Query Field Event Handler
	$('#query').on("keydown", function(e){
		
		//---------------------------
		// Enter
		if (e.keyCode == 13) {
			cfw_query_resizeTextareaToFitQuery();
		}
	});
	
	
		
}

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
	
	cfw_query_highlightExecuteButton(false);
	cfw_ui_toogleLoader(true);	
	
	CFW.http.getJSON(CFW_QUERY_URL, params, 
		function(data) {
						
			if(data.success){
				targetDiv.html("");
				
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
						
					//-----------------------------------
					// Create Title				
					var execSeconds = '';
					if(currentResults.execTimeMillis != -1){
						execSeconds = " ("+(currentResults.execTimeMillis / 1000).toFixed(3)+"s)";
					}
					
					var title = $('<h2>');
					if(currentResults.metadata.name == null){
						title.text('Query '+ (i+1) + execSeconds);
					}else{
						title.text(currentResults.metadata.name + execSeconds);
					}
					targetDiv.append(title);
					
					//-----------------------------------
					// Detected Fields 
					if(currentResults.detectedFields != null){
						targetDiv.append('<p>'+JSON.stringify(currentResults.detectedFields)+'</p>');
					}
					
					//-----------------------------------
					// Handle Description
					if(currentResults.metadata.description != null){
						targetDiv.append('<span>'+currentResults.metadata.description+'</span>');
					}
					
					
					//-----------------------------------
					// Check is result empty
					if(currentResults.results.length == 0 ){
						targetDiv.append('<p>The result is empty.</p>')
						continue;
					}
					
					//-----------------------------------
					// Render Results
					var rendererSettings = {
							data: currentResults.results,
						 	//idfield: 'PK_ID',
						 	bgstylefield: null,
						 	textstylefield: null,
						 	titlefields: null,
						 	titleformat: '{0}',
						 	visiblefields: currentResults.detectedFields,
						 	labels: {
						 		// todo, set labels manually
						 	},
						 	customizers: {},
		
							rendererSettings: {
								dataviewer: {
									storeid: 'cfw-query'+i,
									sortable: false,
									renderers: [
										{	label: 'Table',
											name: 'table',
											renderdef: {
												rendererSettings: {
													table: {filterable: false, narrow: true},
												},
											}
										},
										{	label: 'Bigger Table',
											name: 'table',
											renderdef: {
												rendererSettings: {
													table: {filterable: false},
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
			
		cfw_ui_toogleLoader(false);	
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
 * 
 ******************************************************************************/
function cfw_query_highlightExecuteButton(enableHighlighting){
	
	if(enableHighlighting){
		$("#executeButton").addClass('btn-warning')
		$("#executeButton").removeClass('btn-btn-primary')
	}else{
		$("#executeButton").removeClass('btn-warning')
		$("#executeButton").addClass('btn-btn-primary')
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
					<button id="executeButton" type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_execute();"><b>Execute</b></button>
				</div>
				
			</div>
			<div class="row">
				<div class="col-12">
					<form id="${formID}">
						<input id="cfw-formID" name="cfw-formID" type="hidden" value="${formID}">
						<textarea id="query" name="query" class="form-control" rows="3" placeholder="Write your query. \r\n Ctrl+Space for content assist. \r\n Ctrl+Enter to execute."></textarea>
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
		cfw_initializeTimeframePicker('timeframePicker'
						, {offset: CFW_QUERY_URLPARAMS.offset}
						, function(){ cfw_query_highlightExecuteButton(true); } );
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
		
		cfw_query_highlightExecuteButton(true);
		
		//---------------------------
		// Ctrl + Enter
		if (e.ctrlKey && e.keyCode == 13) {
			cfw_query_execute();
		}
		
		//---------------------------
		// Enter
		if (e.keyCode == 13) {
			cfw_query_resizeTextareaToFitQuery();
		}
		
	});
	
	
		
}
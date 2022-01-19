
CFW_QUERY_URL="/app/query";

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
				// Render Data
				for(index in data.payload){
					var rendererSettings = {
							data: data.payload[index].results,
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
						<textarea id="query" name="query" class="form-control" placeholder="Start typing your query..."></textarea>
					</form>
				</div>
			</div>
			
		</div>
		
		<div id="cfw-query-results">
		</div>
	`);
	
	//-----------------------------------
	// Initialize Fields
	cfw_autocompleteInitialize('cfwQueryAutocompleteForm','query',1,10);
	cfw_initializeTimeframePicker('timeframePicker', {"offset":"1-h"}, null);
	
}
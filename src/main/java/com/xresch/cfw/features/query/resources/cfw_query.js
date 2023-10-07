
var CFW_QUERY_URLPARAMS = CFW.http.getURLParamsDecoded();
var CFW_QUERY_URL="/app/query";


/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_initialDraw(){
	
	$('#cfw-content').css('padding', "10px 20px 20px 20px");
	//-----------------------------------
	// Prepare HTML Base
	var parent = $('#cfw-container');

	parent.css('max-width', '100%');
	
	parent.append(`
		<div>
			
			<div class="row">
				<div class="col-12">
					<textarea id="query" name="query" class="query-original query-text-format" spellcheck="false" placeholder="Write your query. \r\n Ctrl+Space for content assist. \r\n Ctrl+Enter to execute."></textarea>
				</div>
			</div>
			
		</div>
		
		<div id="cfw-query-results" class="monospace">
		</div>
	`);
	
	var $QUERYAREA = $('#query');
	
	var queryEditor = new CFWQueryEditor($QUERYAREA, {
			// the div where the results should be sent to
			 resultDiv: $("#cfw-query-results")
			// the id of the timeframe picker, if null new one will be created (Default: null)
			, timeframePickerID: null
			// toggle is the query data should use URL params
			, useURLParams: true
			
		});
	
	// -----------------------------
	// Handle back button
	window.onpopstate = function() {
		queryEditor.loadQueryFromURLAndExecute();
	}
		
}

var CFW_QUERY_URLPARAMS = CFW.http.getURLParamsDecoded();
var CFW_QUERY_URL="/app/query";

var CFW_QUERY_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_query_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		list.append('<li class="nav-item"><a class="nav-link" id="tab-editor" data-toggle="pill" href="#" role="tab" onclick="cfw_query_draw({tab: \'editor\'})"><i class="fas fa-pen mr-2"></i>Editor</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-history" data-toggle="pill" href="#" role="tab" onclick="cfw_query_draw({tab: \'history\'})"><i class="fas fa-history mr-2"></i>History</a></li>');

		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_query_deleteHistoryItem(id){
	
	params = {action: "delete", item: "historyitem", id: id};
	CFW.http.getJSON(CFW_QUERY_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfw_query_printHistoryView(JSEXAMPLES_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected history item could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Print the editor tab.
 ******************************************************************/
function cfw_query_printEditor(){

	var parent = $('#tab-content');

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

/******************************************************************
 * Full example using the dataviewer renderer.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_query_printHistoryView(){
	
	var parent = $("#tab-content");
	parent.html('');
	
	//======================================
	// Prepare actions
	var actionButtons = [];

	//-------------------------
	// Delete Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete <strong>\\\''+record.PK_ID+'\\\'</strong>?\', \'Delete\', \'cfw_query_deleteHistoryItem('+id+');\')">'
					+ '<i class="fa fa-trash"></i>'
					+ '</button>';

		});
	
	//-----------------------------------
	// Render Data
	var rendererSettings = {
			data: null,
		 	idfield: 'PK_ID',
		 	bgstylefield: null,
		 	textstylefield: null,
		 	titlefields: [],
		 	visiblefields: ['PK_ID', 'TIME', 'EARLIEST', 'LATEST', "QUERY"],
		 	labels: {
		 		PK_ID: "ID",
		 		TIME: "Exection Time",
		 	},
		 	customizers: {
		 		TIME: function(record, value) { return CFW.format.epochToTimestamp(value); }
		 		, EARLIEST: function(record, value) { return CFW.format.epochToTimestamp(value); }
		 		, LATEST: function(record, value) { return CFW.format.epochToTimestamp(value); }
				, QUERY: function(record, value) { 
					if(value == null){ return "&nbsp;"; }
					
					var pre = $('<pre class="maxvh-20">');
					var code = $('<code class="language-cfwquery">');
					code.text(value);
					pre.append(code);
					return pre; 
					}
		 		},
			actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
			
			rendererSettings: {
				dataviewer: {
					storeid: 'queryhistorylist',
					datainterface: {
						url: CFW_QUERY_URL,
						item: 'queryhistorylist',
						callback: function() {
								parent.find('pre code').each(function(index, element){
								console.log('hit');
								hljs.highlightElement(element);
							});
						}
					},
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
						{	label: 'Smaller Table',
							name: 'table',
							renderdef: {
								visiblefields: ['FIRSTNAME', 'LASTNAME', 'EMAIL', 'LIKES_TIRAMISU'],
								actions: [],
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
	
	parent.append(renderResult);
	
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_initialDraw(){
	
	//-----------------------------------
	// make wide
	$('#cfw-container').css('max-width', '100%');
	
	$('#cfw-content').css('padding', "10px 20px 20px 20px");
	
	//-----------------------------------
	// handle Tabs
	cfw_query_createTabs();
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("cfw-query-lasttab", "editor");
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	cfw_query_draw({tab: tabToDisplay});
	
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mydashboards|shareddashboards|admindashboards', 
 *  }
 * @return 
 ******************************************************************/
function cfw_query_draw(options){
	CFW_QUERY_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("cfw-query-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "editor":		cfw_query_printEditor();
								break;	
								
			case "history":		cfw_query_printHistoryView();
								break;	
								
			default:			CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}
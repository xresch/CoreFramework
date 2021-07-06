
/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var CFWJOBS_URL = "./jobs";
var CFWJOBS_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfwjobs_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		list.append('<li class="nav-item"><a class="nav-link" id="tab-myjobs" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'myjobs\'})"><i class="fas fa-share-alt mr-2"></i>Data Handling</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-pagination-static" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'pagination-static\'})"><i class="fas fa-copy mr-2"></i>Pagination(Static)</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-pagination-dynamic" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'pagination-dynamic\'})"><i class="fas fa-dna mr-2"></i>Pagination(Dynamic)</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-full-dataviewer" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'full-dataviewer\'})"><i class="fas fa-eye mr-2"></i>Full Dataviewer</a></li>');
		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfwjobs_add(){
	
	var html = $('<div>');	

	CFW.http.getForm('cfwCreateJobForm', html);
	
	CFW.ui.showModalMedium(
			"Create Job", 
			html, 
			"CFW.cache.clearCache(); cfwjobs_draw(CFWJOBS_LAST_OPTIONS)"
		);
	
}
/******************************************************************
 * Edit Role
 ******************************************************************/
function cfwjobs_edit(id){
	
	//-----------------------------------
	// Details
	//-----------------------------------
	var detailsDiv = $('<div id="jsexamples-details">');
	
	CFW.ui.showModalMedium(
		"Edit Job", 
		detailsDiv, 
		"CFW.cache.clearCache(); cfwjobs_draw(CFWJOBS_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFWJOBS_URL, {action: "getform", item: "editperson", id: id}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfwjobs_delete(id){
	
	params = {action: "delete", item: "person", id: id};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.clearCache();
				cfwjobs_draw(CFWJOBS_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected person could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfwjobs_duplicate(id){
	
	params = {action: "duplicate", item: "person", id: id};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfwjobs_draw(CFWJOBS_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * Full example using the dataviewer renderer.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfwjobs_printMyJobs(){
	
	var parent = $("#tab-content");

	//--------------------------------
	// Button
	var addButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfwjobs_add()">'
						+ '<i class="mr-1 fas fa-plus-circle"></i>Add Job</button>');

	parent.append(addButton);
	
	//======================================
	// Prepare actions
	var actionButtons = [];
	//-------------------------
	// Edit Button
	actionButtons.push(
		function (record, id){ 
			return '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
					+'onclick="cfwjobs_edit('+id+');">'
					+ '<i class="fa fa-pen"></i>'
					+ '</button>';

		});

	
	//-------------------------
	// Duplicate Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-warning btn-sm" alt="Duplicate" title="Duplicate" '
					+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.FIRSTNAME.replace(/\"/g,'&quot;')+'\\\'</strong>.\', \'Do it!\', \'cfwjobs_duplicate('+id+');\')">'
					+ '<i class="fas fa-clone"></i>'
					+ '</button>';
	});
	
	//-------------------------
	// Delete Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete <strong>\\\''+record.FIRSTNAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfwjobs_delete('+id+');\')">'
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
		 	titlefields: ['FIRSTNAME', 'LASTNAME'],
		 	titleformat: '{0} {1}',
		 	visiblefields: ['PK_ID', 'FIRSTNAME', 'LASTNAME', 'LOCATION', "EMAIL", "LIKES_TIRAMISU", "CHARACTER"],
		 	labels: {
		 		PK_ID: "ID",
		 	},
		 	customizers: {
		 		LIKES_TIRAMISU: function(record, value) { 
		 			var likesTiramisu = value;
		 			if(likesTiramisu){
							return '<span class="badge badge-success m-1">true</span>';
					}else{
						return '<span class="badge badge-danger m-1">false</span>';
					}
		 		},
		 		CHARACTER: function(record, value) { 
			 		return CFW.format.arrayToBadges(value.split(','));			 			 
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
					storeid: 'fulldataviewerexample',
					datainterface: {
						url: CFWJOBS_URL,
						item: 'personlist'
					},
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

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mydashboards|shareddashboards|admindashboards', 
 *  }
 * @return 
 ******************************************************************/

function cfwjobs_initialDraw(){
	
	cfwjobs_createTabs();
	
	//-----------------------------------
	// Restore last tab
	var tabToDisplay = CFW.cache.retrieveValueForPage("jsexamples-lasttab", "myjobs");
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	cfwjobs_draw({tab: tabToDisplay});
}

function cfwjobs_draw(options){
	CFWJOBS_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("jsexamples-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "myjobs":		CFW.http.fetchAndCacheData(CFWJOBS_URL, {action: "fetch", item: "personlist"}, "personlist", cfwjobs_printDataHandling);
										break;	
			case "pagination-static":	CFW.http.fetchAndCacheData(CFWJOBS_URL, {action: "fetch", item: "personlist"}, "personlist", cfwjobs_printPaginationStatic);
										break;	
			case "pagination-dynamic":	cfwjobs_printPaginationDynamic();
										break;	
			case "full-dataviewer":		cfwjobs_printFullDataviewer();
			break;	
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 50);
}
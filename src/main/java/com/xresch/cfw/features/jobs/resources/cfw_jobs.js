
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
		
		list.append('<li class="nav-item"><a class="nav-link" id="tab-myjoblist" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'myjoblist\'})"><i class="fas fa-share-alt mr-2"></i>My Jobs</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-adminjoblist" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'adminjoblist\'})"><i class="fas fa-copy mr-2"></i>Admin</a></li>');

		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfwjobs_add_selectTask(){
	
	var html = $('<div>');	

	CFW.http.getForm('cfwSelectJobTaskForm', html);
	
	CFW.ui.showModalMedium(
			"Select Task", 
			html, 
			"CFW.cache.clearCache(); cfwjobs_draw(CFWJOBS_LAST_OPTIONS)"
		);
	
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfwjobs_add_createJob(submitButton){
	
	var form = $(submitButton).closest('form');
	var taskname = form.find('#TASK').val();
	
	var createJobDiv = $('<div id="cfw-create-job">');	

	CFW.ui.showModalMedium(
			"Create Job", 
			createJobDiv, 
			"CFW.cache.clearCache(); cfwjobs_draw(CFWJOBS_LAST_OPTIONS)"
		);
		
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFWJOBS_URL, {action: "getform", item: "createjob", taskname: taskname}, createJobDiv);
	
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
	CFW.http.createForm(CFWJOBS_URL, {action: "getform", item: "editjob", id: id}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfwjobs_delete(id){
	
	params = {action: "delete", item: "job", id: id};
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
	
	params = {action: "duplicate", item: "job", id: id};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfwjobs_draw(CFWJOBS_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfwjobs_printMyJobs(){
	cfwjobs_printJobs('myjoblist');
}

/******************************************************************
 * 
 ******************************************************************/
function cfwjobs_printAdminJobs(){
	cfwjobs_printJobs('adminjoblist');
}
	
/******************************************************************
 * Full example using the dataviewer renderer.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfwjobs_printJobs(itemType){
	
	var parent = $("#tab-content");
	parent.html("");
	//--------------------------------
	// Button
	var addButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfwjobs_add_selectTask()">'
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
					+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong>.\', \'Do it!\', \'cfwjobs_duplicate('+id+');\')">'
					+ '<i class="fas fa-clone"></i>'
					+ '</button>';
	});
	
	//-------------------------
	// Delete Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete <strong>\\\''+record.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfwjobs_delete('+id+');\')">'
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
		 	titlefields: ['JOB_NAME'],
		 	titleformat: '{0}',
		 	visiblefields: ['PK_ID', 'JOB_NAME', 'TASK_NAME', 'DESCRIPTION', 'IS_ENABLED', 'JSON_SCHEDULE', 'JSON_PROPERTIES'],
		 	labels: {
		 		PK_ID: "ID",
		 		JSON_SCHEDULE: "Schedule",
		 		JSON_PROPERTIES: "Properties",
		 	},
		 	customizers: {
		 		IS_ENABLED: function(record, value) { 
		 			return '<span class="badge badge-'+((value == true)? 'success' : 'danger') +'">'+value+'</span>'; 
		 			},
		 		JSON_SCHEDULE: function(record, value) { 
		 				return CFW.format.cfwSchedule(value); 
		 			},
		 		JSON_PROPERTIES: function(record, value) { 
		 				return CFW.format.objectToHTMLList(value); 
		 			},
		 		
		 	},
			actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
			
			rendererSettings: {
				dataviewer: {
					storeid: itemType,
					datainterface: {
						url: CFWJOBS_URL,
						item: itemType
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
//						{	label: 'Smaller Table',
//							name: 'table',
//							renderdef: {
//								visiblefields: ['FIRSTNAME', 'LASTNAME', 'EMAIL', 'LIKES_TIRAMISU'],
//								actions: [],
//								rendererSettings: {
//									table: {filterable: false, narrow: true},
//								},
//							}
//						},
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
								visiblefields: ['PK_ID', 'TASK_NAME'],
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
 * 		tab: 'myjoblist|adminjoblist', 
 *  }
 * @return 
 ******************************************************************/

function cfwjobs_initialDraw(){
	
	cfwjobs_createTabs();
	
	//-----------------------------------
	// Restore last tab
	var tabToDisplay = CFW.cache.retrieveValueForPage("cfwjobs-lasttab", "myjoblist");
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	cfwjobs_draw({tab: tabToDisplay});
}

function cfwjobs_draw(options){
	CFWJOBS_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("cfwjobs-lasttab", options.tab);
	
	$("#tab-content").html("");
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "myjoblist":			cfwjobs_printMyJobs();
										break;	
										
			case "adminjoblist":		cfwjobs_printAdminJobs();
										break;	
	
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 50);
}
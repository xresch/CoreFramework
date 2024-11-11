
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
		
		if(CFW.hasPermission("Jobs: Admin")){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-adminjoblist" data-toggle="pill" href="#" role="tab" onclick="cfwjobs_draw({tab: \'adminjoblist\'})"><i class="fas fa-copy mr-2"></i>Admin</a></li>');
		}
		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfwjobs_add_selectTask(){
	
	params = {action: "fetch", item: "tasks"};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				
				//-------------------------
				// Sort by Name
				var sortedTasks = _.sortBy(data.payload, ['NAME']);
				
				//-------------------------
				// Select Button
				var actionButtons = [];
				actionButtons.push(
					function (record, id){
						return '<button class="btn btn-success btn-sm" alt="Add" title="Add" '
								+'onclick="cfwjobs_add_createJob(\''+record.NAME+'\');">'
								+ '<i class="fas fa-plus-circle"></i>'
								+ '</button>';

					});
				

				//-----------------------------------
				// Render Data
				var rendererSettings = {
						data: sortedTasks,
					 	idfield: 'NAME',
					 	visiblefields: ['NAME', 'DESCRIPTION'],

						actions: actionButtons,

						rendererSettings: {
							table: {narrow: true, filterable: true}
						},
					};
						
				var result = CFW.render.getRenderer('table').render(rendererSettings);	
				
				
				CFW.ui.showModalMedium(
						"Select Task", 
						result, 
						"CFW.cache.clearCache(); cfwjobs_draw(CFWJOBS_LAST_OPTIONS)"
					);
			}
	});
	

	
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfwjobs_add_createJob(taskname){
	
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
 * Execute
 ******************************************************************/
function cfwjobs_execute(id){
	
	params = {action: "execute", item: "job", id: id};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				//do nothing
			}
	});
}

/******************************************************************
 * Execute
 ******************************************************************/
function cfwjobs_stop(id){
	
	params = {action: "stop", item: "job", id: id};
	CFW.http.getJSON(CFWJOBS_URL, params, 
		function(data) {
			if(data.success){
				//do nothing
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
 * 
 ******************************************************************/
function cfwjobs_formatMessages(record, value){
	
	//-------------------------
	// Handle Null
	if(CFW.utils.isNullOrEmpty(value)
	|| value == "[]"
	|| value.length == 0 
	|| value.length == undefined){
		return '&nbsp;';
	}
	
	//-------------------------
	// Find highest severity INFO, SUCCESS, WARNING, ERROR 
	// Create Popup message
	var highestSeverity = 'INFO';
	var popupMessage = '<span>Message(s) of last execution:<ul>';
	
	for(let index in value){
		
		current = value[index];
		popupMessage += '<li><b>'+current.type+':&nbsp;</b>'+current.message+'</li>'
		
		if(highestSeverity != 'ERROR'){
			if(current.type == 'ERROR'){ highestSeverity = 'ERROR'; continue; }
			if(highestSeverity != 'WARNING'){
				if(current.type == 'WARNING'){ highestSeverity = 'WARNING'; continue; }
				if(highestSeverity != 'SUCCESS'){
					if(current.type == 'SUCCESS'){ highestSeverity = 'SUCCESS'; continue; }
				}
			}
		}
	}
	
	popupMessage += '</ul>';
	
	//-------------------------
	// Create Icon
	var icon;
	switch(highestSeverity){
		case 'INFO':  icon = $('<span class="badge badge-info"><i class="fas fa-info-circle"></span>'); break;
		case 'SUCCESS':  icon = $('<span class="badge badge-success"><i class="fas fa-check"></span>'); break;
		case 'WARNING':  icon = $('<span class="badge badge-warning text-white"><i class="fas fa-exclamation-circle"></span>'); break;
		case 'ERROR':  icon = $('<span class="badge badge-danger"><i class="fas fa-exclamation-triangle"></span>'); break;
		
	}
	
	//-------------------------
	// add Popover
	icon.popover({
		trigger: 'hover',
		html: true,
		placement: 'auto',
		boundary: 'window',
		// title: 'Details',
		sanitize: false,
		content: popupMessage
	})
	
	return icon;
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
	// Stop Button
	actionButtons.push(
		function (record, id){ 
			
			if(CFW.utils.isNullOrEmpty(record.EXECUTION_START)){ return "&nbsp;"; }
					
			return   '<button class="btn btn-sm btn-danger" alt="Execute" title="Stop" '
					+'onclick="CFW.ui.confirmExecute(\'Do you really want to stop the job <strong>\\\''+record.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong> now?\', \'Let\\\'s Go!\', \'cfwjobs_stop('+record.PK_ID+');\')">'
					+ '<i class="fa fa-ban"></i>'
					+ '</button>'
				;

		});
	
	//-------------------------
	// Execute Button
	actionButtons.push(
		function (record, id){ 
			return '<button class="btn btn-sm btn-success" alt="Execute" title="Execute" '
					+'onclick="CFW.ui.confirmExecute(\'Do you really want to execute the job <strong>\\\''+record.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong> now?\', \'Let\\\'s Go!\', \'cfwjobs_execute('+id+');\')">'
					+ '<i class="fa fa-play"></i>'
					+ '</button>';

		});
	
	//-------------------------
	// Edit Button
	actionButtons.push(
		function (record, id){ 
			return '<button class="btn btn-sm btn-primary" alt="Edit" title="Edit" '
					+'onclick="cfwjobs_edit('+id+');">'
					+ '<i class="fa fa-pen"></i>'
					+ '</button>';
		});

	
	//-------------------------
	// Duplicate Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-sm btn-warning text-white" alt="Duplicate" title="Duplicate" '
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
	
	//-------------------------
	// Visible Fields
	var fieldsWithOwner =  	 ['JSON_LASTRUN_MESSAGES', 'PK_ID', 'OWNER', 'JOB_NAME', 'TASK_NAME', 'IS_ENABLED', 'SCHEDULE_START', 'SCHEDULE_END', 'SCHEDULE_INTERVAL', 'LAST_RUN_TIME', 'EXECUTION_START']
	var fieldsWithoutOwner = ['JSON_LASTRUN_MESSAGES', 'PK_ID', 'JOB_NAME', 'TASK_NAME', 'IS_ENABLED', 'SCHEDULE_START', 'SCHEDULE_END', 'SCHEDULE_INTERVAL', 'LAST_RUN_TIME', 'EXECUTION_START']
	
	var visiblefields;
	if(itemType == 'adminjoblist'){
		visiblefields = fieldsWithOwner;
	}else{
		visiblefields = fieldsWithoutOwner;
	}

	//-----------------------------------
	// Render Data
	var rendererSettings = {
			data: null,
		 	idfield: 'PK_ID',
		 	bgstylefield: null,
		 	textstylefield: null,
		 	titlefields: ['JOB_NAME'],
		 	titleformat: '{0}',
		 	visiblefields: visiblefields,
		 	labels: {
		 		JSON_LASTRUN_MESSAGES: "Messages",
		 		PK_ID: "ID",
		 		IS_ENABLED: "Enabled",
		 		JSON_SCHEDULE: "Schedule",
		 		JSON_PROPERTIES: "Properties",
		 		EXECUTION_START: "Exec Duration",
		 	},
		 	customizers: {
		 		JSON_LASTRUN_MESSAGES: cfwjobs_formatMessages,
		 		
		 		DESCRIPTION: function(record, value) { 
		 			return '<div class="word-break-word" style="max-width: 250px">'+CFW.utils.nullTo(value, "&nbsp;")+'</div>'; 
	 			},
		 		
		 		IS_ENABLED: function(record, value) { 
		 			return '<span class="badge badge-'+((value == true)? 'success' : 'danger') +'">'+value+'</span>'; 
		 			},
		 		//JSON_SCHEDULE: function(record, value) { return CFW.format.cfwSchedule(value); },
		 		SCHEDULE_START: function(record, value) { return CFW.format.epochToTimestamp(value); },
		 		
		 		SCHEDULE_END: function(record, value) { 
		 			if(value == null) return "&nbsp;";
		 			if(isNaN(value)){
		 				return value; 
		 			}else{
		 				return CFW.format.epochToTimestamp(parseInt(value)); 
		 			}
		 		},
		 		
		 		EXECUTION_START: function(record, value) { 
					if(CFW.utils.isNullOrEmpty(value)){ return "&nbsp;"; }
					
					let millis = Date.now() - value;
					
					return '<div class="text-right w-100 pr-1" >' 
							+ CFW.format.timeToDuration(millis)
						+'<div>';
					
		 		},
		 		
		 		LAST_RUN_TIME: function(record, value) { return CFW.format.epochToTimestamp(value); },
		 		
		 		JSON_PROPERTIES: function(record, value) { 
		 			if(value.children != null
		 			&& value.children.length == 0){
		 				delete value.children;
		 			}
		 			
		 			let div = $('<div class="word-break-word maxvh-25 overflow-auto">');
		 			div.append(CFW.format.objectToHTMLList(value));
		 			return div; 
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
					sortfields: fieldsWithoutOwner,
					datainterface: {
						url: CFWJOBS_URL,
						item: itemType,
						//preprocess: function(data){ CFW.format.splitCFWSchedule(data, 'JSON_SCHEDULE') }
					},
					renderers: [
						{	label: 'Table',
							name: 'table',
							renderdef: {
							 	labels: {
							 		JSON_LASTRUN_MESSAGES: "&nbsp;",
							 		PK_ID: "ID",
							 		IS_ENABLED: "Enabled",
							 		JSON_SCHEDULE: "Schedule",
							 		JSON_PROPERTIES: "Properties",
							 	},
								rendererSettings: {
									table: { filterable: false, narrow: true},
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
							renderdef: {
								labels: { JSON_LASTRUN_MESSAGES: "Messages Last Run" }
							}
						},
						{	label: 'Cards',
							name: 'cards',
							renderdef: {
								labels: { JSON_LASTRUN_MESSAGES: "Messages Last Run" }
							}
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
							renderdef: {
								labels: { JSON_LASTRUN_MESSAGES: "Messages Last Run" }
							}
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
	
	$('#cfw-container').css('max-width', '100%');
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	cfwjobs_draw({tab: tabToDisplay});
}

function cfwjobs_draw(options){
	CFWJOBS_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("cfwjobs-lasttab", options.tab);
	
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "myjoblist":			cfwjobs_printMyJobs();
										break;	
										
			case "adminjoblist":		cfwjobs_printAdminJobs();
										break;	
	
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}
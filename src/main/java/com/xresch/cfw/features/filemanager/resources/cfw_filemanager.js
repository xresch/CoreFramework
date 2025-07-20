
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/

var CFW_STOREDFILELIST_URL = "/app/filemanager";
var CFW_STOREDFILELIST_LAST_OPTIONS = null;


/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_storedfilelist_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		//--------------------------------
		// My StoredFile Tab
		if(CFW.hasPermission('StoredFile: Creator') 
		|| CFW.hasPermission('StoredFile: Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mystoredfile" data-toggle="pill" href="#" role="tab" onclick="cfw_storedfilelist_draw({tab: \'mystoredfile\'})"><i class="fas fa-file mr-2"></i>My Files</a></li>'
			);
		}
		
		//--------------------------------
		// Shared StoredFile Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-sharedstoredfile" data-toggle="pill" href="#" role="tab" onclick="cfw_storedfilelist_draw({tab: \'sharedstoredfile\'})"><i class="fas fa-share-alt mr-2"></i>Shared</a></li>');
		
		
		//--------------------------------
		// Archived StoredFile Tab	
		if( CFW.hasPermission('StoredFile: Creator') ){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-myarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_storedfilelist_draw({tab: \'myarchived\'})"><i class="fas fa-folder-open mr-2"></i>Archive</a></li>');
		}
		
		//--------------------------------
		// Admin StoredFile Tab	
		if(CFW.hasPermission('StoredFile: Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-adminarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_storedfilelist_draw({tab: \'adminarchived\'})"><i class="fas fa-folder-open mr-2"></i>Admin Archive</a></li>'
				+'<li class="nav-item"><a class="nav-link" id="tab-adminstoredfile" data-toggle="pill" href="#" role="tab" onclick="cfw_storedfilelist_draw({tab: \'adminstoredfile\'})"><i class="fas fa-tools mr-2"></i>Admin</a></li>'
				);
		}
		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Edit
 ******************************************************************/
function cfw_storedfilelist_editStoredFile(id){
	
	cfw_storedfile_editStoredFile(id, "cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS)");
	 
}

/******************************************************************
 * Create
 ******************************************************************/
function cfw_storedfilelist_createStoredFile(){
	
	let fieldID ='cfw-upload-file';
	let html = $(`<div id="cfw-storedfile-createStoredFile">
		<input id="${fieldID}" type="hidden" data-role="filepicker" >
	</div>
	`);	

	CFW.ui.showModalMedium(CFWL('cfw_storedfilelist_createStoredFile', 
			CFWL("cfw_storedfilelist_createStoredFile", "Upload Files")), 
			html, "cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS)");
	
	cfw_initializeFilePicker(fieldID, true, null);
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_importStoredFile(){
	
	var uploadHTML = 
		'<p>Select a previously exported storedfile file. Share settings will be imported as well. If you exported the storedfile from another application or application instance, the widgets might not be able to load correctly.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Select File to Import:</label>'
			+'<input type="file" class="form-control" name="importFile" id="importFile" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_storedfilelist_importStoredFileExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import StoredFile", 
			uploadHTML,
			"cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS)");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_importStoredFileExecute(){
	
	var file = document.getElementById('importFile').files[0];
	var reader = new FileReader();

	  // Read file into memory as UTF-8
	  reader.readAsText(file, "UTF-8");
	  
	  reader.onload = function loaded(evt) {
		  // Obtain the read file data
		  var fileString = evt.target.result;
		  
			var params = {action: "import", item: "storedfile", jsonString: fileString};
			CFW.http.postJSON(CFW_STOREDFILELIST_URL, params, 
				function(data) {
					//do nothing
				}
			);

		}
}

/******************************************************************
 * Edit StoredFile
 ******************************************************************/
function cfw_storedfilelist_changeStoredFileOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-storedfile-details">');

	
	CFW.ui.showModalMedium(
			CFWL("cfw_storedfile_editStoredFile","Edit StoredFile"), 
			formDiv, 
			"cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_STOREDFILELIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_storedfile_editStoredFile(id, callbackJSOrFunc){
	

	// ##################################################
	// Create and show Modal
	// ##################################################
	var compositeDiv = $('<div id="editStoredFileSettingsComposite">');
	
	// ----------------------------------
	// Create Pill Navigation
	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');

	list.append(
		'<li class="nav-item"><a class="nav-link active" id="storedfileSettingsTab" data-toggle="pill" href="#storedfileSettingsContent" role="tab" ><i class="fas fa-tools mr-2"></i>Settings</a></li>'
	 // + '<li class="nav-item"><a class="nav-link" id="storedfileStatisticsTab" data-toggle="pill" href="#storedfileStatisticsContent" role="tab" onclick="cfw_storedfilecommon_showStatistics('+id+')" ><i class="fas fa-chart-bar"></i>&nbsp;Statistics</a></li>'
	);
	
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="storedfileSettingsContent" role="tabpanel" aria-labelledby="storedfileSettingsTab"></div>'
			 // +'<div class="tab-pane fade" id="storedfileStatisticsContent" role="tabpanel" aria-labelledby="storedfileStatisticsTab"></div>'
		+'</div>' );	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	
	CFW.ui.showModalMedium(
			CFWL('cfw_core_settings', 'Settings'), 
			compositeDiv, 
			callbackJSOrFunc,
			true
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	var elTargeto = compositeDiv.find('#storedfileSettingsContent');
	CFW.http.createForm(CFW_STOREDFILELIST_URL, {action: "getform", item: "editstoredfile", id: id}, elTargeto );

	$('#editStoredFileSettingsComposite [data-toggle="tooltip"]').tooltip();		
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedfilelist_delete(id){
	
	var params = {action: "delete", item: "storedfile", id: id};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The file could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_deleteMultiple(elements, records, values){
	
	params = {action: "delete", item: "multiple", ids: values.join()};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected file(s) could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedfilelist_archive(id, isarchived){
	
	var params = {action: "update", item: "isarchived", id: id, isarchived: isarchived};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected storedfile could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_archiveMultiple(elements, records, values){
	
	params = {action: "update", item: "archivemultiple", ids: values.join()};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected file(s) could <b style="color: red">NOT</strong> be archived.</span>');
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_restoreMultiple(elements, records, values){
	
	params = {action: "update", item: "restoremultiple", ids: values.join()};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected file(s) could <b style="color: red">NOT</strong> be restored.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedfilelist_duplicate(id){
	
	var params = {action: "duplicate", item: "storedfile", id: id};
	CFW.http.getJSON(CFW_STOREDFILELIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedfilelist_draw(CFW_STOREDFILELIST_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printMyStoredFile(data){
	cfw_storedfilelist_printStoredFile(data, 'mystoredfile');
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printFavedStoredFile(data){
	cfw_storedfilelist_printStoredFile(data, 'favedstoredfile');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printSharedStoredFile(data){
	cfw_storedfilelist_printStoredFile(data, 'sharedstoredfile');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printMyArchived(data){
	cfw_storedfilelist_printStoredFile(data, 'myarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printAdminArchived(data){
	cfw_storedfilelist_printStoredFile(data, 'adminarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedfilelist_printAdminStoredFile(data){
	cfw_storedfilelist_printStoredFile(data, 'adminstoredfile');
}


/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_storedfilelist_printStoredFile(data, type){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Tab Desciption

	switch(type){
		case "mystoredfile":		parent.append('<p>This tab shows all storedfile where you are the owner.</p>')
									break;	
									
		case "myarchived":			parent.append('<p>This tab shows all archived storedfile where you are the owner.</p>')
									break;	
									
		case "sharedstoredfile":	parent.append('<p>This list contains all the storedfile that are shared by others and by you.</p>')
									break;
									
		case "adminarchived":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin archive. The list contains all archived storedfile of all users.</b></p>')
									break;	
									
		case "adminstoredfile":	parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin area. The list contains all storedfile of all users.</b></p>')
									break;	
														
		default:					break;
	}
	
	//--------------------------------
	//  Create Button
	if(type == 'mystoredfile'){
		var createButton = $('<button id="button-add-storedfile" class="btn btn-sm btn-success m-1" onclick="cfw_storedfilelist_createStoredFile()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_core_add')
					   + '</button>');
	
		parent.append(createButton);
				
	}
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any storedfile in the list.");
		}
		
		//-----------------------------------
		// Prepare Columns
		var showFields = [];
		if(type == 'mystoredfile' 
		|| type == 'myarchived'){
			showFields = ['SIZE', 'NAME', 'DESCRIPTION', 'TAGS', 'IS_SHARED', 'TIME_CREATED'];
		}else if ( type == 'sharedstoredfile'
				|| type == 'favedstoredfile'){
			showFields = ['OWNER', 'SIZE', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'adminstoredfile'
				||type == 'adminarchived' ){
			showFields = ['PK_ID', 'OWNER', 'SIZE', 'NAME', 'DESCRIPTION', 'TAGS','IS_SHARED', 'TIME_CREATED'];
		}
		
		//======================================
		// Prepare actions
		
		var actionButtons = [ ];		
		var bulkActions = {};		
		
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_OWNER 
				|| type == 'adminstoredfile'
				|| (record.IS_EDITOR) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_storedfilelist_editStoredFile('+id+')");">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// Change Owner Button & Archive Selected Button
		if(type == 'mystoredfile'
		|| type == 'adminstoredfile'){

			actionButtons.push(
				function (record, id){
					var htmlString = '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
							+'onclick="cfw_storedfilelist_changeStoredFileOwner('+id+');">'
							+ '<i class="fas fa-user-edit"></i>'
							+ '</button>';
					
					return htmlString;
				});
			
			bulkActions["Archive Selected"] = 
				function(elements, records, values){
						cfw_ui_confirmExecute("Are you sure you want to archive the selected file(s)?", "Do it!", function(){
							cfw_storedfilelist_archiveMultiple(elements, records, values);
						});
					};
		}
		
		//-------------------------
		// Duplicate Button
/*		if( (type != 'myarchived' && type != 'adminarchived' )
			&& (
			   CFW.hasPermission('StoredFile: Creator')
			|| CFW.hasPermission('StoredFile: Admin')
			)
		){
			actionButtons.push(
				function (record, id){
					
					// IMPORTANT: Do only allow duplicate if the user can edit the storedfile,
					// else this would create a security issue.
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_OWNER 
					|| CFW.hasPermission('StoredFile: Admin')
					|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
						htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your storedfile.\', \'Do it!\', \'cfw_storedfilelist_duplicate('+id+');\')">'
							+ '<i class="fas fa-clone"></i>'
							+ '</button>';
					}else{
						htmlString += '&nbsp;';
					}
					
					return htmlString;
				});
		}*/
		
		//-------------------------
		// Archive / Restore Button
		actionButtons.push(
			function (record, id){
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_OWNER 
				|| type == 'adminarchived'
				|| type == 'adminstoredfile'
				){
					let isArchived = record.IS_ARCHIVED;
					let confirmMessage = "Do you want to archive the storedfile";
					let title = "Archive";
					let icon = "fa-folder-open";
					let color =  "btn-danger";
					
					if(isArchived){
						confirmMessage = "Do you want to restore the storedfile";
						title = "Restore";
						icon = "fa-trash-restore" ;
						color = "btn-success";
					}
					
					htmlString += '<button class="btn '+color+' btn-sm" alt="'+title+'" title="'+title+'" '
						+'onclick="CFW.ui.confirmExecute(\''+confirmMessage+' <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Do it!\', \'cfw_storedfilelist_archive('+id+', '+!isArchived+');\')">'
						+ '<i class="fa '+icon+'"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
	

		//-------------------------
		// Delete Buttons
		if(type == 'myarchived'
		|| type == 'adminarchived'){
			actionButtons.push(
				function (record, id){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the storedfile <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_storedfilelist_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';

				});
			
			bulkActions["Restore Selected"] = 
				function(elements, records, values){
						cfw_ui_confirmExecute('Are you sure you want to <b style="color: green">restore</b> the selected file(s)?', "Oh Yeah!", function(){
							cfw_storedfilelist_restoreMultiple(elements, records, values);
						});
					};
						
			bulkActions["Delete Selected"] = 
				function(elements, records, values){
						cfw_ui_confirmExecute('Are you sure you want to <b style="color: red">delete</b> the selected file(s)?', "Eradicate them for Eternity!", function(){
							cfw_storedfilelist_deleteMultiple(elements, records, values);
						});
					};
					
			
		}

		
		//-------------------------
		// Sharing Details View
		sharingDetailsView = null;
		
		if(type == 'mystoredfile'
		|| type == 'adminstoredfile'){
			sharingDetailsView = 
				{ 
					label: 'Sharing Details',
					name: 'table',
					renderdef: {
						visiblefields: [ "NAME", "IS_SHARED", "JSON_SHARE_WITH_USERS", "JSON_SHARE_WITH_GROUPS", "JSON_EDITORS", "JSON_EDITOR_GROUPS"],
						labels: {
					 		PK_ID: "ID",
					 		IS_SHARED: 'Shared',
					 		TIME_CREATED: 'Time Uploaded',
					 		JSON_SHARE_WITH_USERS: 'Shared User', 
						 	JSON_SHARE_WITH_GROUPS: 'Shared Groups', 
						 	JSON_EDITORS: 'Editors', 
						 	JSON_EDITOR_GROUPS: 'Editor Groups'
					 	},
						rendererSettings: {
							table: {filterable: false},
						},
					}
				};
			
			if(type == 'adminstoredfile'){
				sharingDetailsView.renderdef.visiblefields.unshift("OWNER");
			}
		}

		//-----------------------------------
		// Render Data
		
		var badgeCustomizerFunction = function(record, value) { 
 			var badgesHTML = '<div class="maxvw-25">';
 			
 			for(id in value){
 				badgesHTML += '<span class="badge badge-primary m-1">'+value[id]+'</span>';
 			}
 			badgesHTML += '</div>';
 			
 			return badgesHTML;
 			 
 		};
 		
		var storeID = 'storedfile-'+type;
		
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: null,
			 	visiblefields: showFields,
			 	labels: {
			 		PK_ID: "ID",
			 		IS_SHARED: 'Shared'
			 	},
			 	customizers: {
						
					IS_SHARED: function(record, value) { 
			 			var isShared = value;
			 			if(isShared){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
						} 
			 		},
					SIZE: function(record, value) { 
			 			
			 			if(!CFW.utils.isNullOrEmpty(value)){
							return 	'<div class="text-right monospace pr-3">'
										+ CFW.format.numbersInThousands(value, 1, true, true)
									+ '</div>'
								;
						}else{
							return 'unknown size';
						} 
			 		},

			 		TAGS: badgeCustomizerFunction,
			 		TIME_CREATED: function(record, value) { 
			 			if(value == null) return "&nbsp;";
			 			if(isNaN(value)){
			 				return value; 
			 			}else{
			 				return CFW.format.epochToTimestamp(parseInt(value)); 
			 			}
			 			
			 		},
			 		JSON_SHARE_WITH_USERS: badgeCustomizerFunction, 
			 		JSON_SHARE_WITH_GROUPS: badgeCustomizerFunction, 
			 		JSON_EDITORS: badgeCustomizerFunction, 
			 		JSON_EDITOR_GROUPS: badgeCustomizerFunction
			 	},
				actions: actionButtons,
				
				bulkActions: bulkActions,
				
				bulkActionsPos: "top",
				
				data: data.payload,
				rendererSettings: {
					dataviewer:{
						storeid: 'storedfile-'+type,
						renderers: [
							{	label: 'Table',
								name: 'table',
								renderdef: {
									labels: {
										PK_ID: "ID",
			 							IS_SHARED: 'Shared'
									},
									rendererSettings: {
										table: {filterable: false, narrow: true},
										
									},
								}
							},
							{	label: 'Bigger Table',
								name: 'table',
								renderdef: {
									labels: {
										PK_ID: "ID",
			 							IS_SHARED: 'Shared'
									},
									rendererSettings: {
										table: {filterable: false},
									},
								}
							},
							sharingDetailsView,
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
									visiblefields: showFields,
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
									visiblefields: null
								}
							},
							{	label: 'XML',
								name: 'xml',
								renderdef: {
									visiblefields: null
								}
							},
							{	label: 'JSON',
								name: 'json',
								renderdef: {}
							}
						],
					},
					table: {filterable: false}
				},
			};
				
		
		var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no storedfile can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mystoredfile|sharedstoredfile|adminstoredfile', 
 *  }
 * @return 
 ******************************************************************/

function cfw_storedfilelist_initialDraw(){
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '100%');
	
	//-------------------------------------------
	// Create Tabs
	cfw_storedfilelist_createTabs();
	
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("storedfilelist-lasttab", "mystoredfile");
	
	if(CFW.hasPermission('StoredFile: Viewer') 
	&& !CFW.hasPermission('StoredFile: Creator') 
	&& !CFW.hasPermission('StoredFile: Admin')){
		tabToDisplay = "sharedstoredfile";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	//-------------------------------------------
	// Draw Tab
	cfw_storedfilelist_draw({tab: tabToDisplay});
	
}

function cfw_storedfilelist_draw(options){
	CFW_STOREDFILELIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("storedfilelist-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mystoredfile":		CFW.http.getJSON(CFW_STOREDFILELIST_URL, {action: "fetch", item: "mystoredfile"}, cfw_storedfilelist_printMyStoredFile);
										break;	
			case "sharedstoredfile":	CFW.http.getJSON(CFW_STOREDFILELIST_URL, {action: "fetch", item: "sharedstoredfile"}, cfw_storedfilelist_printSharedStoredFile);
										break;
			case "myarchived":			CFW.http.getJSON(CFW_STOREDFILELIST_URL, {action: "fetch", item: "myarchived"}, cfw_storedfilelist_printMyArchived);
										break;	
			case "adminarchived":		CFW.http.getJSON(CFW_STOREDFILELIST_URL, {action: "fetch", item: "adminarchived"}, cfw_storedfilelist_printAdminArchived);
										break;	
			case "adminstoredfile":		CFW.http.getJSON(CFW_STOREDFILELIST_URL, {action: "fetch", item: "adminstoredfile"}, cfw_storedfilelist_printAdminStoredFile);
										break;						
			default:					CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}

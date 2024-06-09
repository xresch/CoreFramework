
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/

var CFW_CREDENTIALSLIST_URL = "/app/credentials";
var URI_DASHBOARD_VIEW_PUBLIC = "/public/credentials/view";
var CFW_DASHBOARDLIST_LAST_OPTIONS = null;


/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_credentialslist_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		//--------------------------------
		// My Credentials Tab
		if(CFW.hasPermission('Credentials Creator') 
		|| CFW.hasPermission('Credentials Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mycredentials" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'mycredentials\'})"><i class="fas fa-user-circle mr-2"></i>My Credentials</a></li>'
			);
		}
		
		//--------------------------------
		// Shared Credentials Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-sharedcredentials" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'sharedcredentials\'})"><i class="fas fa-share-alt mr-2"></i>Shared</a></li>');
		
		
		//--------------------------------
		// Faved Credentials Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-favedcredentials" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'favedcredentials\'})"><i class="fas fa-star mr-2"></i>Favorites</a></li>');
		
		//--------------------------------
		// Archived Credentials Tab	
		if( CFW.hasPermission('Credentials Creator') ){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-myarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'myarchived\'})"><i class="fas fa-folder-open mr-2"></i>Archive</a></li>');
		}
		
		//--------------------------------
		// Admin Credentials Tab	
		if(CFW.hasPermission('Credentials Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-adminarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'adminarchived\'})"><i class="fas fa-folder-open mr-2"></i>Admin Archive</a></li>'
				+'<li class="nav-item"><a class="nav-link" id="tab-admincredentials" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'admincredentials\'})"><i class="fas fa-tools mr-2"></i>Admin</a></li>'
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
function cfw_credentialslist_editCredentials(id){
	
	cfw_credentialscommon_editCredentials(id, "cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	 
}

/******************************************************************
 * Create
 ******************************************************************/
function cfw_credentialslist_createCredentials(){
	
	var html = $('<div id="cfw-credentials-createCredentials">');	

	CFW.http.getForm('cfwCreateCredentialsForm', html);
	
	CFW.ui.showModalMedium(CFWL('cfw_credentialslist_createCredentials', 
			CFWL("cfw_credentialslist_createCredentials", "Create Credentials")), 
			html, "cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_importCredentials(){
	
	var uploadHTML = 
		'<p>Select a previously exported credentials file. Share settings will be imported as well. If you exported the credentials from another application or application instance, the widgets might not be able to load correctly.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Select File to Import:</label>'
			+'<input type="file" class="form-control" name="importFile" id="importFile" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_credentialslist_importCredentialsExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import Credentials", 
			uploadHTML,
			"cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_importCredentialsExecute(){
	
	var file = document.getElementById('importFile').files[0];
	var reader = new FileReader();

	  // Read file into memory as UTF-8
	  reader.readAsText(file, "UTF-8");
	  
	  reader.onload = function loaded(evt) {
		  // Obtain the read file data
		  var fileString = evt.target.result;
		  
			var params = {action: "import", item: "credentials", jsonString: fileString};
			CFW.http.postJSON(CFW_CREDENTIALSLIST_URL, params, 
				function(data) {
					//do nothing
				}
			);

		}
}

/******************************************************************
 * Edit Credentials
 ******************************************************************/
function cfw_credentialslist_changeCredentialsOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-credentials-details">');

	
	CFW.ui.showModalMedium(
			CFWL("cfw_credentialscommon_editCredentials","Edit Credentials"), 
			formDiv, 
			"cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_CREDENTIALSLIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_delete(id){
	
	var params = {action: "delete", item: "credentials", id: id};
	CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected credentials could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_archive(id, isarchived){
	
	var params = {action: "update", item: "isarchived", id: id, isarchived: isarchived};
	CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected credentials could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_duplicate(id){
	
	var params = {action: "duplicate", item: "credentials", id: id};
	CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printMyCredentials(data){
	cfw_credentialslist_printCredentials(data, 'mycredentials');
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printFavedCredentials(data){
	cfw_credentialslist_printCredentials(data, 'favedcredentials');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printSharedCredentials(data){
	cfw_credentialslist_printCredentials(data, 'sharedcredentials');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printMyArchived(data){
	cfw_credentialslist_printCredentials(data, 'myarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printAdminArchived(data){
	cfw_credentialslist_printCredentials(data, 'adminarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printAdminCredentials(data){
	cfw_credentialslist_printCredentials(data, 'admincredentials');
}


/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_credentialslist_printCredentials(data, type){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Tab Desciption

	switch(type){
		case "mycredentials":		parent.append('<p>This tab shows all credentials where you are the owner.</p>')
									break;	
									
		case "myarchived":			parent.append('<p>This tab shows all archived credentials where you are the owner.</p>')
									break;	
									
		case "sharedcredentials":	parent.append('<p>This list contains all the credentials that are shared by others and by you.</p>')
									break;
									
		case "favedcredentials":		parent.append('<p>Here you can find all the credentials you have faved. If you unfave a credentials here it will vanish from the list the next time the tab or page gets refreshed.</p>')
									break;	
									
		case "adminarchived":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin archive. The list contains all archived credentials of all users.</b></p>')
									break;	
		case "admincredentials":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin area. The list contains all credentials of all users.</b></p>')
									break;	
														
		default:					break;
	}
	
	//--------------------------------
	//  Create Button
	if(type == 'mycredentials'){
		var createButton = $('<button id="button-add-credentials" class="btn btn-sm btn-success m-1" onclick="cfw_credentialslist_createCredentials()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_credentialslist_createCredentials')
					   + '</button>');
	
		parent.append(createButton);
		
		var importButton = $('<button id="button-import" class="btn btn-sm btn-success m-1" onclick="cfw_credentialslist_importCredentials()">'
				+ '<i class="fas fa-upload"></i> '+ CFWL('cfw_core_import', 'Import')
		   + '</button>');

		parent.append(importButton);
		
	}
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any credentials in the list.");
		}
		
		//-----------------------------------
		// Prepare Columns
		var showFields = [];
		if(type == 'mycredentials' 
		|| type == 'myarchived'){
			showFields = ['NAME', 'DESCRIPTION', 'TAGS', 'IS_SHARED', 'TIME_CREATED'];
		}else if ( type == 'sharedcredentials'
				|| type == 'favedcredentials'){
			showFields = ['OWNER', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'admincredentials'
				||type == 'adminarchived' ){
			showFields = ['PK_ID', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS','IS_SHARED', 'TIME_CREATED'];
		}
		
		//======================================
		// Prepare actions
		
		var actionButtons = [ ];		
		
		//-------------------------
		// View Button
		actionButtons.push(
			function (record, id){ 
				return '<a class="btn btn-success btn-sm" role="button" href="/app/credentials/view?id='+id+'&title='+encodeURIComponent(record.NAME)+'" alt="View" title="View" >'
				+ '<i class="fa fa-eye"></i>'
				+ '</a>';
			}
		);

		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_USER 
				|| type == 'admincredentials'
				|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_credentialslist_editCredentials('+id+')");">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// Change Owner Button
		if(type == 'mycredentials'
		|| type == 'admincredentials'){

					actionButtons.push(
						function (record, id){
							var htmlString = '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
									+'onclick="cfw_credentialslist_changeCredentialsOwner('+id+');">'
									+ '<i class="fas fa-user-edit"></i>'
									+ '</button>';
							
							return htmlString;
						});
				}
		
		//-------------------------
		// Duplicate Button
		if( (type != 'myarchived' && type != 'adminarchived' )
			&& (
			   CFW.hasPermission('Credentials Creator')
			|| CFW.hasPermission('Credentials Admin')
			)
		){
			actionButtons.push(
				function (record, id){
					
					// IMPORTANT: Do only allow duplicate if the user can edit the credentials,
					// else this would create a security issue.
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_USER 
					|| CFW.hasPermission('Credentials Admin')
					|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
						htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your credentials.\', \'Do it!\', \'cfw_credentialslist_duplicate('+id+');\')">'
							+ '<i class="fas fa-clone"></i>'
							+ '</button>';
					}else{
						htmlString += '&nbsp;';
					}
					
					return htmlString;
				});
		}
		
		//-------------------------
		// Export Button
		if(type == 'mycredentials'
		|| type == 'admincredentials'
		|| type == 'favedcredentials'){

			actionButtons.push(
				function (record, id){
					if(JSDATA.userid == record.FK_ID_USER 
					|| type == 'admincredentials'){
						return '<a class="btn btn-warning btn-sm text-white" target="_blank" alt="Export" title="Export" '
							+' href="'+CFW_CREDENTIALSLIST_URL+'?action=fetch&item=export&id='+id+'" download="'+record.NAME.replaceAll(' ', '_')+'_export.json">'
							+'<i class="fa fa-download"></i>'
							+ '</a>';
					}else{
						return '&nbsp;';
					}
					
				});
		}

		//-------------------------
		// Archive / Restore Button
		actionButtons.push(
			function (record, id){
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_USER 
				|| type == 'adminarchived'
				|| type == 'admincredentials'
				){
					let isArchived = record.IS_ARCHIVED;
					let confirmMessage = "Do you want to archive the credentials";
					let icon = "fa-folder-open";
					let color =  "btn-danger";
					
					if(isArchived){
						confirmMessage = "Do you want to restore the credentials";
						icon = "fa-trash-restore" ;
						color = "btn-success";
					}
					
					htmlString += '<button class="btn '+color+' btn-sm" alt="Archive" title="Archive" '
						+'onclick="CFW.ui.confirmExecute(\''+confirmMessage+' <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Do it!\', \'cfw_credentialslist_archive('+id+', '+!isArchived+');\')">'
						+ '<i class="fa '+icon+'"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
	

		//-------------------------
		// Delete Button
		if(type == 'myarchived'
		|| type == 'adminarchived'){
			actionButtons.push(
				function (record, id){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the credentials <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_credentialslist_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';

				});
		}

		
		//-------------------------
		// Sharing Details View
		sharingDetailsView = null;
		
		if(type == 'mycredentials'
		|| type == 'admincredentials'){
			sharingDetailsView = 
				{ 
					label: 'Sharing Details',
					name: 'table',
					renderdef: {
						visiblefields: [ "NAME", "IS_SHARED", "JSON_SHARE_WITH_USERS", "JSON_SHARE_WITH_GROUPS", "JSON_EDITORS", "JSON_EDITOR_GROUPS"],
						labels: {
					 		PK_ID: "ID",
					 		IS_SHARED: 'Shared',
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
			
			if(type == 'admincredentials'){
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
 		
		var storeID = 'credentials-'+type;
		
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
						
			 		NAME: function(record, value, rendererName) { 
			 			
			 			if(rendererName == 'table'){
								return '<a href="/app/credentials/view?id='+record.PK_ID+'" style="color: inherit;">'+record.NAME+'</a>';
						}else{
							return value;
						} 
			 		},
					IS_SHARED: function(record, value) { 
			 			var isShared = value;
			 			if(isShared){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
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
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				data: data.payload,
				rendererSettings: {
					dataviewer:{
						storeid: 'credentials-'+type,
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
		CFW.ui.addAlert('error', 'Something went wrong and no credentials can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mycredentials|sharedcredentials|admincredentials', 
 *  }
 * @return 
 ******************************************************************/

function cfw_credentialslist_initialDraw(){
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '100%');
	
	//-------------------------------------------
	// Create Tabs
	cfw_credentialslist_createTabs();
	
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("credentialslist-lasttab", "mycredentials");
	
	if(CFW.hasPermission('Credentials Viewer') 
	&& !CFW.hasPermission('Credentials Creator') 
	&& !CFW.hasPermission('Credentials Admin')){
		tabToDisplay = "sharedcredentials";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	//-------------------------------------------
	// Draw Tab
	cfw_credentialslist_draw({tab: tabToDisplay});
	
}

function cfw_credentialslist_draw(options){
	CFW_DASHBOARDLIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("credentialslist-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mycredentials":		CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, {action: "fetch", item: "mycredentials"}, cfw_credentialslist_printMyCredentials);
										break;	
			case "sharedcredentials":	CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, {action: "fetch", item: "sharedcredentials"}, cfw_credentialslist_printSharedCredentials);
										break;
			case "myarchived":			CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, {action: "fetch", item: "myarchived"}, cfw_credentialslist_printMyArchived);
										break;	
			case "adminarchived":		CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, {action: "fetch", item: "adminarchived"}, cfw_credentialslist_printAdminArchived);
										break;	
			case "admincredentials":		CFW.http.getJSON(CFW_CREDENTIALSLIST_URL, {action: "fetch", item: "admincredentials"}, cfw_credentialslist_printAdminCredentials);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}

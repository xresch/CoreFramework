
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/

var CFW_STOREDQUERYLIST_URL = "/app/storedquery";
var CFW_STOREDQUERYLIST_LAST_OPTIONS = null;


/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_storedQuerylist_createTabs(){
	var pillsTab = $("#pills-tab-stored");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab-stored" role="tablist">');
		
		//--------------------------------
		// My StoredQuery Tab
		if(CFW.hasPermission('Query Store: Creator') 
		|| CFW.hasPermission('Query Store: Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mystoredQuery" data-toggle="pill" href="#" role="tab" onclick="cfw_storedQuerylist_draw({tab: \'mystoredQuery\'})"><i class="fas fa-user-circle mr-2"></i>My Store</a></li>'
			);
		}
		
		//--------------------------------
		// Shared StoredQuery Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-sharedstoredQuery" data-toggle="pill" href="#" role="tab" onclick="cfw_storedQuerylist_draw({tab: \'sharedstoredQuery\'})"><i class="fas fa-share-alt mr-2"></i>Shared</a></li>');
		
		
		//--------------------------------
		// Archived StoredQuery Tab	
		if( CFW.hasPermission('Query Store: Creator') ){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-myarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_storedQuerylist_draw({tab: \'myarchived\'})"><i class="fas fa-folder-open mr-2"></i>Archive</a></li>');
		}
		
		//--------------------------------
		// Admin StoredQuery Tab	
		if(CFW.hasPermission('Query Store: Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-adminarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_storedQuerylist_draw({tab: \'adminarchived\'})"><i class="fas fa-folder-open mr-2"></i>Admin Archive</a></li>'
				+'<li class="nav-item"><a class="nav-link" id="tab-adminstoredQuery" data-toggle="pill" href="#" role="tab" onclick="cfw_storedQuerylist_draw({tab: \'adminstoredQuery\'})"><i class="fas fa-tools mr-2"></i>Admin</a></li>'
				);
		}
		
		var parent = $("#tab-content");
		parent.append(list);
		parent.append('<div id="tab-content-stored"></div>');
	}

}

/******************************************************************
 * Edit
 ******************************************************************/
function cfw_storedQuerylist_editStoredQuery(id){
	
	cfw_storedQuery_editStoredQuery(id, "cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS)");
	 
}

/******************************************************************
 * Create
 ******************************************************************/
function cfw_storedQuerylist_createStoredQuery(){
	
	var html = $('<div id="cfw-storedQuery-createStoredQuery">');	

	CFW.http.getForm('cfwCreateStoredQueryForm', html);
	
	CFW.ui.showModalMedium(CFWL('cfw_storedQuerylist_createStoredQuery', 
			CFWL("cfw_storedQuerylist_createStoredQuery", "Create StoredQuery")), 
			html, "cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS)");
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_importStoredQuery(){
	
	var uploadHTML = 
		'<p>Select a previously exported stored query file. Share settings will be imported as well. If you exported the storedQuery from another application or application instance, the widgets might not be able to load correctly.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Select File to Import:</label>'
			+'<input type="file" class="form-control" name="importFile" id="importFile" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_storedQuerylist_importStoredQueryExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import StoredQuery", 
			uploadHTML,
			"cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS)");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_importStoredQueryExecute(){
	
	var file = document.getElementById('importFile').files[0];
	var reader = new FileReader();

	  // Read file into memory as UTF-8
	  reader.readAsText(file, "UTF-8");
	  
	  reader.onload = function loaded(evt) {
		  // Obtain the read file data
		  var fileString = evt.target.result;
		  
			var params = {action: "import", item: "storedquery", jsonString: fileString};
			CFW.http.postJSON(CFW_STOREDQUERYLIST_URL, params, 
				function(data) {
					//do nothing
				}
			);

		}
}

/******************************************************************
 * Edit StoredQuery
 ******************************************************************/
function cfw_storedQuerylist_changeStoredQueryOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-storedQuery-details">');

	
	CFW.ui.showModalMedium(
			CFWL("cfw_storedQuery_editStoredQuery","Edit StoredQuery"), 
			formDiv, 
			"cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_STOREDQUERYLIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_storedQuery_editStoredQuery(id, callbackJSOrFunc){
	
	cfw_parameter_setScope("query", id);
	
	// ##################################################
	// Create and show Modal
	// ##################################################
	var compositeDiv = $('<div id="editStoredQuerySettingsComposite">');
	
	// ----------------------------------
	// Create Pill Navigation
	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab-stored-settings" role="tablist">');
	let paramsTabID = 'storedQueryParamsContent';
	list.append(
		'<li class="nav-item"><a class="nav-link active" id="storedQuerySettingsTab" data-toggle="pill" href="#storedQuerySettingsContent" role="tab" ><i class="fas fa-tools mr-2"></i>Settings</a></li>'
	  + '<li class="nav-item"><a class="nav-link" id="storedQueryParamsTab" data-toggle="pill" href="#'+paramsTabID+'" role="tab" onclick="cfw_parameter_edit(\'#'+paramsTabID+'\')" ><i class="fas fa-sliders"></i>&nbsp;Parameters</a></li>'
	);
	
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="storedQuerySettingsContent" role="tabpanel" aria-labelledby="storedQuerySettingsTab"></div>'
			  +'<div class="tab-pane fade" id="'+paramsTabID+'" role="tabpanel" aria-labelledby="storedQueryParamsTab"></div>'
		+'</div>' );	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	
	CFW.ui.showModalLarge(
			CFWL('cfw_core_settings', 'Settings'), 
			compositeDiv, 
			callbackJSOrFunc,
			true
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	var elTargeto = compositeDiv.find('#storedQuerySettingsContent');
	CFW.http.createForm(CFW_STOREDQUERYLIST_URL, {action: "getform", item: "editstoredQuery", id: id}, elTargeto );

	$('#editStoredQuerySettingsComposite [data-toggle="tooltip"]').tooltip();		
}

/******************************************************************
 * Edit Params
 ******************************************************************/
function cfw_storedQuerylist_editParams(id){
	cfw_parameter_setScope("query", id);
	cfw_parameter_edit();
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedQuerylist_delete(id){
	
	var params = {action: "delete", item: "storedQuery", id: id};
	CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected storedQuery could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}
	

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedQuerylist_archive(id, isarchived){
	
	var params = {action: "update", item: "isarchived", id: id, isarchived: isarchived};
	CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected storedQuery could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_storedQuerylist_duplicate(id){
	
	var params = {action: "duplicate", item: "storedQuery", id: id};
	CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_storedQuerylist_draw(CFW_STOREDQUERYLIST_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printMyStoredQuery(data){
	cfw_storedQuerylist_printStoredQuery(data, 'mystoredQuery');
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printFavedStoredQuery(data){
	cfw_storedQuerylist_printStoredQuery(data, 'favedstoredQuery');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printSharedStoredQuery(data){
	cfw_storedQuerylist_printStoredQuery(data, 'sharedstoredQuery');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printMyArchived(data){
	cfw_storedQuerylist_printStoredQuery(data, 'myarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printAdminArchived(data){
	cfw_storedQuerylist_printStoredQuery(data, 'adminarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_storedQuerylist_printAdminStoredQuery(data){
	cfw_storedQuerylist_printStoredQuery(data, 'adminstoredQuery');
}


/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_storedQuerylist_printStoredQuery(data, type){
	
	var parent = $("#tab-content-stored");
	
	//--------------------------------
	// Tab Desciption

	switch(type){
		case "mystoredQuery":		parent.append('<p>This tab shows all stored queries where you are the owner.</p>')
									break;	
									
		case "myarchived":			parent.append('<p>This tab shows all archived stored queries where you are the owner.</p>')
									break;	
									
		case "sharedstoredQuery":	parent.append('<p>This list contains all the stored queries that are shared by others and by you.</p>')
									break;
									
		case "adminarchived":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin archive. The list contains all archived stored queries of all users.</b></p>')
									break;	
									
		case "adminstoredQuery":	parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin area. The list contains all stored queries of all users.</b></p>')
									break;	
														
		default:					break;
	}
	
	//--------------------------------
	//  Create Button
	if(type == 'mystoredQuery'){
		var createButton = $('<button id="button-add-storedQuery" class="btn btn-sm btn-success m-1" onclick="cfw_storedQuerylist_createStoredQuery()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_core_add')
					   + '</button>');
	
		parent.append(createButton);
		
		var importButton = $('<button id="button-import" class="btn btn-sm btn-success m-1" onclick="cfw_storedQuerylist_importStoredQuery()">'
				+ '<i class="fas fa-upload"></i> '+ CFWL('cfw_core_import', 'Import')
		   + '</button>');

		parent.append(importButton);
				
	}
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any stored queries in the list.");
		}
		
		//-----------------------------------
		// Prepare Columns
		var showFields = [];
		if(type == 'mystoredQuery' 
		|| type == 'myarchived'){
			showFields = ['NAME', 'DESCRIPTION', 'TAGS', 'MAKE_WIDGET', 'IS_SHARED', 'CHECK_PERMISSIONS', 'TIME_CREATED'];
		}else if ( type == 'sharedstoredQuery'
				|| type == 'favedstoredQuery'){
			showFields = ['OWNER', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'adminstoredQuery'
				||type == 'adminarchived' ){
			showFields = ['PK_ID', 'OWNER', 'NAME',  'DESCRIPTION', 'TAGS', 'MAKE_WIDGET', 'IS_SHARED', 'CHECK_PERMISSIONS', 'TIME_CREATED'];
		}
		
		//======================================
		// Prepare actions
		
		var actionButtons = [ ];		
		
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_OWNER 
				|| type == 'adminstoredQuery'
				|| (record.IS_EDITOR) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_storedQuerylist_editStoredQuery('+id+')");">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
			
		//-------------------------
		// Edit Params Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_OWNER 
				|| type == 'adminstoredQuery'
				|| (record.IS_EDITOR) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit Params" title="Edit Params" '
						+'onclick="cfw_storedQuerylist_editParams('+id+')");">'
						+ '<i class="fa fa-sliders-h"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// Change Owner Button
		if(type == 'mystoredQuery'
		|| type == 'adminstoredQuery'){

					actionButtons.push(
						function (record, id){
							var htmlString = '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
									+'onclick="cfw_storedQuerylist_changeStoredQueryOwner('+id+');">'
									+ '<i class="fas fa-user-edit"></i>'
									+ '</button>';
							
							return htmlString;
						});
				}
		
		//-------------------------
		// Duplicate Button
		if( (type != 'myarchived' && type != 'adminarchived' )
			&& (
			   CFW.hasPermission('Query Store: Creator')
			|| CFW.hasPermission('Query Store: Admin')
			)
		){
			actionButtons.push(
				function (record, id){
					
					// IMPORTANT: Do only allow duplicate if the user can edit the storedQuery,
					// else this would create a security issue.
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_OWNER 
					|| CFW.hasPermission('Query Store: Admin')
					|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
						htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your storedQuery.\', \'Do it!\', \'cfw_storedQuerylist_duplicate('+id+');\')">'
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
		if(type == 'mystoredQuery'
		|| type == 'adminstoredQuery'
		|| type == 'favedstoredQuery'){

			actionButtons.push(
				function (record, id){
					if(JSDATA.userid == record.FK_ID_OWNER 
					|| type == 'adminstoredQuery'){
						return '<a class="btn btn-warning btn-sm text-white" target="_blank" alt="Export" title="Export" '
							+' href="'+CFW_STOREDQUERYLIST_URL+'?action=fetch&item=export&id='+id+'" download="'+record.NAME.replaceAll(' ', '_')+'.json">'
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
				if(JSDATA.userid == record.FK_ID_OWNER 
				|| type == 'adminarchived'
				|| type == 'adminstoredQuery'
				){
					let isArchived = record.IS_ARCHIVED;
					let confirmMessage = "Do you want to archive the storedQuery";
					let title = "Archive";
					let icon = "fa-folder-open";
					let color =  "btn-danger";
					
					if(isArchived){
						confirmMessage = "Do you want to restore the storedQuery";
						title = "Restore";
						icon = "fa-trash-restore" ;
						color = "btn-success";
					}
					
					htmlString += '<button class="btn '+color+' btn-sm" alt="'+title+'" title="'+title+'" '
						+'onclick="CFW.ui.confirmExecute(\''+confirmMessage+' <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Do it!\', \'cfw_storedQuerylist_archive('+id+', '+!isArchived+');\')">'
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
							+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the storedQuery <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_storedQuerylist_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';

				});
		}

		
		//-------------------------
		// Sharing Details View
		sharingDetailsView = null;
		
		if(type == 'mystoredQuery'
		|| type == 'adminstoredQuery'){
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
			
			if(type == 'adminstoredQuery'){
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
 		
		var storeID = 'storedQuery-'+type;
		
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: null,
			 	visiblefields: showFields,
			 	labels: {
			 		  PK_ID: "ID"
			 		, IS_SHARED: 'Shared'
			 		, MAKE_WIDGET: 'Widget'
			 	},
			 	customizers: {
						
					MAKE_WIDGET: cfw_customizer_booleanFormat,
					IS_SHARED: cfw_customizer_booleanFormat,
					CHECK_PERMISSIONS: cfw_customizer_booleanFormat,
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
						storeid: 'storedQuery-'+type,
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
										table: {narrow: false, filterable: false},
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
		CFW.ui.addAlert('error', 'Something went wrong and no stored queries can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mystoredQuery|sharedstoredQuery|adminstoredQuery', 
 *  }
 * @return 
 ******************************************************************/
function cfw_storedQuerylist_initialDraw(){
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '100%');
	
	//-------------------------------------------
	// Create Tabs
	$("#tab-content").html('');
	cfw_storedQuerylist_createTabs();
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("storedQuerylist-lasttab", "mystoredQuery");
	
	if(CFW.hasPermission('Query Store: Viewer') 
	&& !CFW.hasPermission('Query Store: Creator') 
	&& !CFW.hasPermission('Query Store: Admin')){
		tabToDisplay = "sharedstoredQuery";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	//-------------------------------------------
	// Draw Tab
	cfw_storedQuerylist_draw({tab: tabToDisplay});
	
}

function cfw_storedQuerylist_draw(options){
	CFW_STOREDQUERYLIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("storedQuerylist-lasttab", options.tab);
	$("#tab-content-stored").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mystoredQuery":		CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, {action: "fetch", item: "mystoredQuery"}, cfw_storedQuerylist_printMyStoredQuery);
										break;	
			case "sharedstoredQuery":	CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, {action: "fetch", item: "sharedstoredQuery"}, cfw_storedQuerylist_printSharedStoredQuery);
										break;
			case "myarchived":			CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, {action: "fetch", item: "myarchived"}, cfw_storedQuerylist_printMyArchived);
										break;	
			case "adminarchived":		CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, {action: "fetch", item: "adminarchived"}, cfw_storedQuerylist_printAdminArchived);
										break;	
			case "adminstoredQuery":		CFW.http.getJSON(CFW_STOREDQUERYLIST_URL, {action: "fetch", item: "adminstoredQuery"}, cfw_storedQuerylist_printAdminStoredQuery);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}


/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var CFW_APITOKENMGMT_URL = "./tokenmanagement";


/******************************************************************
 * 
 ******************************************************************/
function cfw_apitokenmgmt_createToggleTable(parent, mapName, tokenID){

	CFW.http.getJSON(CFW_APITOKENMGMT_URL, {action: "fetch", item: 'permissionmap', tokenid: tokenID}, 
		function(data) {
			if(data.payload != null){
				
				var htmlString = "";
				var cfwTable = new CFWTable({narrow: true});
				
				cfwTable.addHeaders(['&nbsp;', 'API', 'Action']);
				
				var resultCount = data.payload.length;
				if(resultCount == 0){
					CFW.ui.addAlert("info", "Hmm... seems there aren't any permissions in the list.");
				}

				for(var i = 0; i < resultCount; i++){
					var current = data.payload[i];
					var row = $('<tr>');
					
					//Toggle Button
					var params = {action: "update", item: mapName, tokenid: tokenID, permissionid: current.PK_ID};
					var cfwToggleButton = CFW.ui.createToggleButton(CFW_APITOKENMGMT_URL, params, (current.TOKEN_ID == tokenID));
					
					var buttonCell = $("<td>");
					cfwToggleButton.appendTo(buttonCell);
					row.append(buttonCell);
					
					row.append('<td>'+current.API_NAME+'</td>'
							  +'<td>'+current.ACTION_NAME+'</td>');
					
					cfwTable.addRow(row);
				}
				
				
				cfwTable.appendTo(parent);
				
			}else{
				CFW.ui.addAlert('error', '<span>The '+mapName+' data for the id '+tokenID+' could not be loaded.</span>');
			}	
		}
	);
}
/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_apitokenmgmt_addToken(){
	
	var html = $('<div>');	

	CFW.http.getForm('cfwCreateTokenForm', html);
	
	CFW.ui.showModalMedium(
			"Create Token", 
			html, 
			"CFW.cache.clearCache(); cfw_apitokenmgmt_draw()"
		);
	
}
/******************************************************************
 * Edit Role
 ******************************************************************/
function cfw_apitokenmgmt_edit(id){
	
	var allDiv = $('<div id="cfw-usermgmt">');	
	//-----------------------------------
	// Details
	//-----------------------------------
	var detailsDiv = $('<div id="apitokenmgmt-details">');
	detailsDiv.append('<h2>Token Details</h2>');
	allDiv.append(detailsDiv);
	
	//-----------------------------------
	// Permissions
	//-----------------------------------
	var permissionDiv = $('<div id="apitokenmgmt-permissions">');
	permissionDiv.append('<h2>Token Permissions</h2>');
	allDiv.append(permissionDiv);
	
	cfw_apitokenmgmt_createToggleTable(permissionDiv, "permissionmap", id);
	
	CFW.ui.showModalMedium(
		"Edit Token", 
		allDiv, 
		"CFW.cache.clearCache(); cfw_apitokenmgmt_draw()"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_APITOKENMGMT_URL, {action: "getform", item: "edittoken", id: id}, detailsDiv);
	
}

/******************************************************************
 * Edit user
 ******************************************************************/
function cfw_usermgmt_editUser(userID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// User Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-usermgmt-details">');
	detailsDiv.append('<h2>'+CFWL('cfw_usermgmt_user', 'User')+'Details</h2>');
	allDiv.append(detailsDiv);
	
	//-----------------------------------
	// Roles
	//-----------------------------------
	var roleDiv = $('<div id="cfw-usermgmt-roles">');
	roleDiv.append('<h2>'+CFWL('cfw_usermgmt_roles', "Roles")+'</h2>');
	allDiv.append(roleDiv);
	
	cfw_usermgmt_createToggleTable(roleDiv, "userrolemap", userID)
	
	CFW.ui.showModalMedium("Edit User", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'users'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "edituser", id: userID}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_apitokenmgmt_delete(id){
	
	params = {action: "delete", item: "token", id: id};
	CFW.http.getJSON(CFW_APITOKENMGMT_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.clearCache();
				cfw_apitokenmgmt_draw();
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected token could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_apitokenmgmt_duplicate(id){
	
	params = {action: "duplicate", item: "token", id: id};
	CFW.http.getJSON(CFW_APITOKENMGMT_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfw_apitokenmgmt_draw();
			}
	});
}

/******************************************************************
 * Example of pagination of static data using the dataviewer render.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_apitokenmgmt_printTokenList(data){
	
	var parent = $("#cfw-container");
	parent.html('');
	parent.append('<h1>API Token Management</h1>');
	//--------------------------------
	// Button
	var addTokenButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_apitokenmgmt_addToken()">'
						+ '<i class="mr-1 fas fa-plus-circle"></i>Add Token</button>');

	parent.append(addTokenButton);
	
	//--------------------------------
	// Table
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any tokens in the list.");
			return;
		}
				
		
		//======================================
		// Prepare actions
		var actionButtons = [];
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				return '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_apitokenmgmt_edit('+id+');">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';

			});

		//-------------------------
		// Duplicate Button
		actionButtons.push(
			function (record, id){
				return '<button class="btn btn-warning btn-sm" alt="Duplicate" title="Duplicate" '
						+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.TOKEN.replace(/\"/g,'&quot;')+'\\\'</strong>.\', \'Do it!\', \'cfw_apitokenmgmt_duplicate('+id+');\')">'
						+ '<i class="fas fa-clone"></i>'
						+ '</button>';
		});
		
		//-------------------------
		// Delete Button
		actionButtons.push(
			function (record, id){
				return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
						+'onclick="CFW.ui.confirmExecute(\'Do you want to delete <strong>\\\''+record.TOKEN.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_apitokenmgmt_delete('+id+');\')">'
						+ '<i class="fa fa-trash"></i>'
						+ '</button>';

			});
		
		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['TOKEN'],
			 	titleformat: '{0}',
			 	visiblefields: ['PK_ID', 'TOKEN', 'DESCRIPTION', 'IS_ACTIVE', 'JSON_RESPONSIBLE_USERS', 'CREATED_BY'],
			 	labels: {
			 		PK_ID: "ID",
			 		JSON_RESPONSIBLE_USERS: "Responsible Users"
			 	},
			 	customizers: {
			 		JSON_RESPONSIBLE_USERS: function(record, value) { 
			 			let html = ''; 
			 			for(key in value){
			 				html += '<span class="badge badge-primary m-1">'+value[key]+'</span>'
			 			}
			 			return html;
			 			 
			 		},
			 		IS_ACTIVE: function(record, value) { return '<span class="badge badge-'+((value == true)? 'success' : 'danger') +'">'+value+'</span>'; },
			 	},
				actions: actionButtons,				
				rendererSettings: {
					dataviewer: {
						storeid: 'tokenlist',
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
									visiblefields: ['PK_ID', 'TOKEN', 'IS_ACTIVE', 'JSON_RESPONSIBLE_USERS', 'CREATED_BY'],
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
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no tokens can be displayed.');
	}
}


/******************************************************************
 * Main method.
 ******************************************************************/

function cfw_apitokenmgmt_draw(){
	$("#cfw-container").addClass('maxvw-90');
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		CFW.http.fetchAndCacheData(CFW_APITOKENMGMT_URL, {action: "fetch", item: "tokenlist"}, "tokenlist", cfw_apitokenmgmt_printTokenList);

		CFW.ui.toggleLoader(false);
	}, 50);
}
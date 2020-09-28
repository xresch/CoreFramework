
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

var CFW_USRMGMT_URL = "./usermanagement/data";

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_usermgmt_reset(){
	
	$("#tab-content").html("");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_createToggleTable(parent, mapName, itemID){

	CFW.http.getJSON(CFW_USRMGMT_URL, {action: "fetch", item: mapName, id: itemID}, 
		function(data) {
			if(data.payload != null){
				
				var htmlString = "";
				var cfwTable = new CFWTable();
				
				cfwTable.addHeaders(['&nbsp;',
					CFWL('cfw_usermgmt_name'),
					CFWL('cfw_usermgmt_description')]);
				var resultCount = data.payload.length;
				if(resultCount == 0){
					CFW.ui.addAlert("info", "Hmm... seems there aren't any roles in the list.");
				}

				for(var i = 0; i < resultCount; i++){
					var current = data.payload[i];
					var row = $('<tr>');
					
					//Toggle Button
					var params = {action: "update", item: mapName, itemid: itemID, listitemid: current.PK_ID};
					var cfwToggleButton = CFW.ui.createToggleButton(CFW_USRMGMT_URL, params, (current.ITEM_ID == itemID));
					
					if(current.IS_DELETABLE != null && !current.IS_DELETABLE){
						cfwToggleButton.setLocked();
					}
					var buttonCell = $("<td>");
					cfwToggleButton.appendTo(buttonCell);
					row.append(buttonCell);
					
					row.append('<td>'+current.NAME+'</td>'
							  +'<td>'+CFW.utils.nullTo(current.DESCRIPTION, '')+'</td>');
					
					cfwTable.addRow(row);
				}
				
				
				cfwTable.appendTo(parent);
				
			}else{
				CFW.ui.addAlert('error', '<span>The '+mapName+' data for the id '+itemID+' could not be loaded.</span>');
			}	
		}
	);
}

/******************************************************************
 * Create user
 ******************************************************************/
function cfw_usermgmt_createUser(){
	
	var html = $('<div id="cfw-usermgmt-createUser">');	

	CFW.http.getForm('cfwCreateUserForm', html);
	
	CFW.ui.showModal(CFWL('cfw_usermgmt_createUser', "Create User"), html, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'users'})");
	
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
	
	CFW.ui.showModal("Edit User", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'users'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "edituser", id: userID}, detailsDiv);
	
}

/******************************************************************
 * ResetPassword
 ******************************************************************/
function cfw_usermgmt_resetPassword(userID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// User Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-usermgmt-details">');
	allDiv.append(detailsDiv);
		
	
	CFW.ui.showModal("Reset Password", allDiv);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "resetpw", id: userID}, detailsDiv);
	
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_usermgmt_createRole(){
	
	var html = $('<div id="cfw-usermgmt-createRole">');	

	CFW.http.getForm('cfwCreateRoleForm', html);
	
	CFW.ui.showModal(CFWL('cfw_usermgmt_createRole', "Create Role"), html, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'roles'})");
	
}
/******************************************************************
 * Edit Role
 ******************************************************************/
function cfw_usermgmt_editRole(roleID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-usermgmt-details">');
	detailsDiv.append('<h2>'+CFWL('cfw_usermgmt_role', "Role")+' Details</h2>');
	allDiv.append(detailsDiv);
	
	//-----------------------------------
	// Permissions
	//-----------------------------------
	var permissionDiv = $('<div id="cfw-usermgmt-roles">');
	permissionDiv.append('<h2>'+CFWL('cfw_usermgmt_permissions', "Permissions")+'</h2>');
	allDiv.append(permissionDiv);
	
	cfw_usermgmt_createToggleTable(permissionDiv, "rolepermissionmap", roleID)
	
	//-----------------------------------
	// Users in Role
	//-----------------------------------
	var usersInRoleDiv = $('<div id="cfw-usermgmt-roles">');
	usersInRoleDiv.append('<h2>'+CFWL('cfw_usermgmt_users_in_role', "Users in Role")+'</h2>');
	allDiv.append(usersInRoleDiv);
	
	CFW.ui.showModal("Edit Role", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'roles'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "editrole", id: roleID}, detailsDiv);
	CFW.http.getJSON(CFW_USRMGMT_URL, {action: "fetch", item: "usersforrole", id: roleID}, function(data){
		if(data.payload != null){
			var renderSettings = {
					data: data.payload,
					visiblefields: ['USER_ID', 'USERNAME', 'FIRSTNAME','LASTNAME'],
					actions: [
						function(record, value){//Toggle Button
							var params = {action: "update", item: "userrolemap", itemid: record.USER_ID, listitemid: roleID};
							var cfwToggleButton = CFW.ui.createToggleButton(CFW_USRMGMT_URL, params, true);
							
							if(record.IS_REMOVABLE != null && !record.IS_REMOVABLE){
								cfwToggleButton.setLocked();
							}
							console.log(cfwToggleButton.button);
							return cfwToggleButton.button;
						}
					]
			}

			
			var tableRenderer = CFW.render.getRenderer('table');
			var table = tableRenderer.render(renderSettings);
			usersInRoleDiv.append(table);
		}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_usermgmt_delete(item, ids){
	
	var url = "./usermanagement/data";
	
	var params = {action: "delete", item: item, ids: ids};
	CFW.http.getJSON(url, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showSmallModal('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.data[item] = null;
				cfw_usermgmt_draw({tab: item});
			}else{
				CFW.ui.showSmallModal("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Example of pagination of static data using the dataviewer render.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_usermgmt_printUserList(data){
	
	var parent = $("#tab-content");
	
	var cfwTable = new CFWTable();
	
	//--------------------------------
	// Button
	var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_usermgmt_createUser()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_usermgmt_createUser')
					   + '</button>');
	
	parent.append(createButton);
	
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any users in the list.");
		}
				
		
		//======================================
		// Prepare actions
		var actionButtons = [];
		
		//-------------------------
		// Reset Password Button
		actionButtons.push(
			function (record, id){ 
				if(!record.IS_FOREIGN){
					return '<button class="btn btn-warning btn-sm" alt="Reset Password" title="Reset Password" '
						+'onclick="cfw_usermgmt_resetPassword('+id+');">'
						+ '<i class="fas fa-unlock-alt"></i>'
						+ '</button></td>';
				}else{
					return '&nbsp;';
				}

			});

		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				return 	'<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
					+'onclick="cfw_usermgmt_editUser('+id+');">'
					+ '<i class="fa fa-pen"></i>'
					+ '</button>&nbsp;';

			});

		
		//-------------------------
		// Delete Button
		actionButtons.push(
			function (record, id){
				if(record.IS_DELETABLE){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete"  '
						+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the user?\', \'Delete\', \'cfw_usermgmt_delete(\\\'users\\\','+id+');\')">'
						+ '<i class="fa fa-trash"></i>'
						+ '</button>';
				}else{
					return '&nbsp;';
				}

			});
		

		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titledelimiter: ' ',
			 	visiblefields: ['PK_ID', 'USERNAME', 'EMAIL', 'FIRSTNAME', 'LASTNAME', 'STATUS', "LAST_LOGIN"],
			 	labels: {
			 		PK_ID: "ID",
			 	},
			 	customizers: {
			 		EMAIL: function(record, value) { return CFW.utils.nullTo(value, '-'); },
			 		FIRSTNAME: function(record, value) { return CFW.utils.nullTo(value, '-'); },
			 		LASTNAME: function(record, value) { return CFW.utils.nullTo(value, '-'); },
			 		STATUS: function(record, value) { return '<span class="badge badge-'+((value.toLowerCase() == "active")? 'success' : 'danger') +'">'+value+'</span>'; },
			 		LAST_LOGIN: function(record, value) { return CFW.format.epochToTimestamp(value); },
			 	},
				actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				
				rendererSettings: {
					dataviewer: {},
					table: { filterable: false },
				},
			};
		
		var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no users can be displayed.');
	}
}

/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_printRoleList(data){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Button
	var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_usermgmt_createRole()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_usermgmt_createRole')
					   + '</button>');
	
	parent.append(createButton);
	
	//--------------------------------
	// Table
	
	var cfwTable = new CFWTable();
	cfwTable.addHeaders(['ID', "Name", "Description"]);
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any roles in the list.");
		}

		var htmlString = "";
		for(var i = 0; i < resultCount; i++){
			var current = data.payload[i];
			htmlString += '<tr>';
			htmlString += '<td>'+current.PK_ID+'</td>';
			htmlString += '<td>'+current.NAME+'</td>';
			htmlString += '<td>'+CFW.utils.nullTo(current.DESCRIPTION, '')+'</td>';
			
			//Edit Button
			htmlString += '<td><button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
				+'onclick="cfw_usermgmt_editRole('+current.PK_ID+');">'
				+ '<i class="fa fa-pen"></i>'
				+ '</button></td>';
			
			//Delete Button
			if(current.IS_DELETABLE){
				htmlString += '<td><button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the role?\', \'Delete\', \'cfw_usermgmt_delete(\\\'roles\\\','+current.PK_ID+');\')">'
					+ '<i class="fa fa-trash"></i>'
					+ '</button></td>';
			}else{
				htmlString += '<td>&nbsp;</td>';
			}
			
			htmlString += '</tr>';
		}
		
		cfwTable.addRows(htmlString);
		
		cfwTable.appendTo(parent);
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no users can be displayed.');
	}
}

/******************************************************************
 * Print the list of permissions;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_printPermissionList(data){
	
	var parent = $("#tab-content");
	
	var cfwTable = new CFWTable();
	cfwTable.addHeaders(['ID', "Name", "Description"]);
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any permissions in the list.");
		}

		var htmlString = "";
		for(var i = 0; i < resultCount; i++){
			var current = data.payload[i];
			htmlString += '<tr>';
			htmlString += '<td>'+current.PK_ID+'</td>';
			htmlString += '<td>'+current.NAME+'</td>';
			htmlString += '<td>'+CFW.utils.nullTo(current.DESCRIPTION, '')+'</td>';			
			htmlString += '</tr>';
		}
		
		cfwTable.addRows(htmlString);
		
		cfwTable.appendTo(parent);
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no users can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'users|roles|permissions', 
 *  }
 * @return 
 ******************************************************************/

function cfw_usermgmt_initialDraw(options){
	cfw_usermgmt_draw(options);
}

function cfw_usermgmt_draw(options){
	
	cfw_usermgmt_reset();
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){

		
		var url = "./usermanagement/data"
		switch(options.tab){
		
			case "users":			CFW.http.fetchAndCacheData(url, {action: "fetch", item: "users"}, "users", cfw_usermgmt_printUserList);
									break;
									
			case "roles":			CFW.http.fetchAndCacheData(url, {action: "fetch", item: "roles"}, "roles", cfw_usermgmt_printRoleList);
									break;
									
			case "permissions":		CFW.http.fetchAndCacheData(url, {action: "fetch", item: "permissions"}, "permissions", cfw_usermgmt_printPermissionList);
									break;	
									
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 100);
}
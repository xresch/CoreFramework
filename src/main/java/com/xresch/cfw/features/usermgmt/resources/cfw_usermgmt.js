
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
function cfw_usermgmt_formatAuditResults(parent, item){
	
	if(Array.isArray(item)){
		
		//------------------------------------
		// Handle Arrays
		for(key in item){
			cfw_usermgmt_formatAuditResults(parent, item[key]);
		} 
	}else{

		//------------------------------------
		// Handle Items
		if(item['cfw-Type'] == "User"){
			parent.append('<h1><b>User:</b> '+item.username+'</h1>');
			for(key in item.children){
				cfw_usermgmt_formatAuditResults(parent, item.children[key]);
			}
		}else if(item['cfw-Type'] == "Audit"){
			parent.append('<h3><b>Audit:</b> '+item.name+'</h3>');
			parent.append('<p>'+item.description+'</p>');
			
			//-----------------------------------
			// Handle Empty Results
			if(item.auditResult == null || item.auditResult.length == 0){
				parent.append('<span class="badge badge-info">No results found for this audit.</span>');
				return;
			}
			
			//-----------------------------------
			// Add customizer
			var booleanCustomizer = function(record, value) { 
				if(value == null){
					return "&nbsp;";
				}else if(value == true){
					return '<span class="badge badge-success">'+value+'</span>'; 
				}else if(value == false){
					return '<span class="badge badge-danger">'+value+'</span>'; 
				}else{
					return value;
				}
			}
				
			var customizers = {};
			for(key in item.auditResult[0]){
				customizers[key] = booleanCustomizer;
			}
			
			//-----------------------------------
			// Render Data
			var rendererSettings = {
					data: item.auditResult,
				 	idfield: 'PK_ID',
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: null,
				 	titleformat: '{0}',
				 	visiblefields: null,
				 	labels: { PK_ID: "ID" },
				 	customizers: customizers,
					actions: [],					
					rendererSettings: {
						table: {
							filterable: false,
							narrow: true,							
						},
						
					},
				};
			
			var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
			parent.append(renderResult);
			
		}
	}
}

/*********************************************************************************
* Creates a printView by opening a new window and returns a divElement where you 
* can put the content inside which you want to print.
* 
* @param "cards or "text"
* @return domElement a div you can write the content to print to.
*********************************************************************************/
function cfw_usermgmt_FullAuditPrintView(){

	printview = CFW.ui.createPrintView("Full User Audit Report", "List all users and audit results.");
	
	printview.append($('#toc'));
	printview.append($('#auditResults'));
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_createToggleTable(parent, mapName, itemID){

	CFW.http.getJSON(CFW_USRMGMT_URL, {action: "fetch", item: mapName, id: itemID}, 
		function(data) {
			if(data.payload != null){
				
				//-----------------------------------
				// Render Data
				var rendererSettings = {
						data: data.payload,
					 	idfield: 'PK_ID',
					 	bgstylefield: null,
					 	textstylefield: null,
					 	titlefields: ['NAME'],
					 	titleformat: '{0}',
					 	visiblefields: ['PK_ID', 'NAME', 'DESCRIPTION'],
					 	labels: {
					 		PK_ID: "&nbsp;",
					 	},
					 	customizers: {
					 		PK_ID: function(record, value) { 
								//Toggle Button
								var params = {action: "update", item: mapName, itemid: itemID, listitemid: record.PK_ID};
								var cfwToggleButton = CFW.ui.createToggleButton(CFW_USRMGMT_URL, params, (record.ITEM_ID == itemID));
								
								if(record.IS_DELETABLE != null && !record.IS_DELETABLE){
									cfwToggleButton.setLocked();
								}
								
								return cfwToggleButton.getButton();
					 		},

					 	},
						rendererSettings: {
							dataviewer: {
								defaultsize: 25,
								sortfields: ['NAME', 'DESCRIPTION'],
								storeid: 'usermgmtToggleTable'+mapName+itemID,
								renderers: [
									{	label: 'Table',
										name: 'table',
										renderdef: {
											rendererSettings: {
												table: {narrow: true },
											},
										}
									}
									]
							},
						},
					};
				
				var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
				parent.append(renderResult);
				
//				var htmlString = "";
//				var cfwTable = new CFWTable({narrow: true});
//				
//				cfwTable.addHeaders(['&nbsp;',
//					CFWL('cfw_usermgmt_name'),
//					CFWL('cfw_usermgmt_description')]);
//				var resultCount = data.payload.length;
//				if(resultCount == 0){
//					CFW.ui.addAlert("info", "Hmm... seems there aren't any roles in the list.");
//				}
//
//				for(var i = 0; i < resultCount; i++){
//					var current = data.payload[i];
//					var row = $('<tr>');
//					
//					//Toggle Button
//					var params = {action: "update", item: mapName, itemid: itemID, listitemid: current.PK_ID};
//					var cfwToggleButton = CFW.ui.createToggleButton(CFW_USRMGMT_URL, params, (current.ITEM_ID == itemID));
//					
//					if(current.IS_DELETABLE != null && !current.IS_DELETABLE){
//						cfwToggleButton.setLocked();
//					}
//					var buttonCell = $("<td>");
//					cfwToggleButton.appendTo(buttonCell);
//					row.append(buttonCell);
//					
//					row.append('<td>'+current.NAME+'</td>'
//							  +'<td>'+CFW.utils.nullTo(current.DESCRIPTION, '')+'</td>');
//					
//					cfwTable.addRow(row);
//				}
//				
//				
//				cfwTable.appendTo(parent);
				
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
	
	CFW.ui.showModalMedium(CFWL('cfw_usermgmt_createUser', "Create User"), html, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'users'})");
	
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
	
	cfw_usermgmt_createToggleTable(roleDiv, "userrolemap", userID);
	//-----------------------------------
	// groups
	//-----------------------------------
	var groupDiv = $('<div id="cfw-usermgmt-groups">');
	groupDiv.append('<h2>'+CFWL('cfw_usermgmt_groups', "Groups")+'</h2>');
	allDiv.append(groupDiv);
	
	cfw_usermgmt_createToggleTable(groupDiv, "usergroupmap", userID);
	
	
	CFW.ui.showModalMedium("Edit User", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'users'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "edituser", id: userID}, detailsDiv);
	
}

/******************************************************************
 * Edit user
 ******************************************************************/
function cfw_usermgmt_auditUser(userID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// User Details
	//-----------------------------------
	var auditDiv = $('<div id="cfw-usermgmt-audit">');		
	
	CFW.ui.showModalMedium("User Audit", auditDiv);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.getJSON(CFW_USRMGMT_URL, {action: "fetch", item: "useraudit", id: userID}, function(data){
		if(data.payload != null){
			cfw_usermgmt_formatAuditResults(auditDiv, data.payload);
		}
	});
	
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
		
	
	CFW.ui.showModalMedium("Reset Password", allDiv);
	
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
	
	CFW.ui.showModalMedium(CFWL('cfw_usermgmt_createRole', "Create Role"), html, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'roles'})");
	
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
	
	CFW.ui.showModalMedium("Edit Role", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'roles'})");
	
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
				//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.data[item] = null;
				cfw_usermgmt_draw({tab: item});
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_usermgmt_createGroup(){
	
	var html = $('<div id="cfw-usermgmt-createGroup">');	

	CFW.http.getForm('cfwCreateGroupForm', html);
	
	CFW.ui.showModalMedium(CFWL('cfw_usermgmt_createGroup', "Create Group"), html, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'groups'})");
	
}

/******************************************************************
 * Edit Group
 ******************************************************************/
function cfw_usermgmt_editGroup(roleID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-usermgmt-details">');
	detailsDiv.append('<h2>'+CFWL('cfw_usermgmt_group', "Group")+' Details</h2>');
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
	var usersInGroupDiv = $('<div id="cfw-usermgmt-groups">');
	usersInGroupDiv.append('<h2>'+CFWL('cfw_usermgmt_users_in_group', "Users in Group")+'</h2>');
	allDiv.append(usersInGroupDiv);
	
	CFW.ui.showModalMedium("Edit Group", allDiv, "CFW.cache.clearCache(); cfw_usermgmt_draw({tab: 'groups'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USRMGMT_URL, {action: "getform", item: "editgroup", id: roleID}, detailsDiv);
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
							return cfwToggleButton.button;
						}
				]
			}

			
			var tableRenderer = CFW.render.getRenderer('table');
			var table = tableRenderer.render(renderSettings);
			usersInGroupDiv.append(table);
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
	
	var STOREID_USERLIST = 'userlist';

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
					return '<button class="btn btn-sm btn-warning text-white" title="Reset Password" '
						+'onclick="cfw_usermgmt_resetPassword('+id+');">'
						+ '<i class="fas fa-unlock-alt"></i>'
						+ '</button></td>';
				}else{
					return '&nbsp;';
				}

			});
		
		//-------------------------
		// Audit Button
		actionButtons.push(
				function (record, id){ 
					return 	'<button class="btn btn-sm btn-warning text-white" title="Audit" '
					+'onclick="cfw_usermgmt_auditUser('+id+');">'
					+ '<i class="fa fa-stethoscope"></i>'
					+ '</button>';
					
				});
		
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				return 	'<button class="btn btn-sm btn-primary" title="Edit" '
					+'onclick="cfw_usermgmt_editUser('+id+');">'
					+ '<i class="fa fa-pen"></i>'
					+ '</button>';

			});

				
		//-------------------------
		// Delete Button
		actionButtons.push(
			function (record, id){
				
				if(record.IS_DELETABLE){
					return '<button class="btn btn-sm btn-danger" alt="Delete" title="Delete"  '
						+'onclick="CFW.ui.confirmExecute(\'Deleting a user <font color=red><b>will also delete data associated</b></font> with the deleted user. If the deleted user has shared something with other users they might also lose that data. To avoid loss of data it is <span class=text-cfw-green><b>recommended to set the user to inactive</b></span>(prevents login).<br/><br/> Do you want to delete the user <b>&quot;'+record.USERNAME+'&quot;</b>?\', \'Delete\', \'cfw_usermgmt_delete(\\\'users\\\','+id+');\')">'
						+ '<i class="fa fa-trash"></i>'
						+ '</button>';
				}else{
					return '&nbsp;';
				}

			});
		

		//-----------------------------------
		// Render Data
		
		var dataviewerDefaults = CFW.render.createDataviewerDefaults();
		
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['USERNAME', 'FIRSTNAME', 'LASTNAME'],
			 	titleformat: '{0} ({1} {2})',
			 	visiblefields: ['PK_ID', 'USERNAME', 'EMAIL', 'FIRSTNAME', 'LASTNAME', 'STATUS', "DATE_CREATED", "LAST_LOGIN"],
			 	labels: {
			 		PK_ID: "ID",
			 	},
			 	customizers: {
			 		EMAIL: function(record, value) { return CFW.utils.nullTo(value, ''); },
			 		FIRSTNAME: function(record, value) { return CFW.utils.nullTo(value, ''); },
			 		LASTNAME: function(record, value) { return CFW.utils.nullTo(value, ''); },
			 		STATUS: function(record, value) { return '<span class="badge badge-'+((value.toLowerCase() == "active")? 'success' : 'danger') +'">'+value+'</span>'; },
			 		LAST_LOGIN: function(record, value) { return CFW.format.epochToTimestamp(value); },
			 		DATE_CREATED: function(record, value) { return CFW.format.epochToTimestamp(value); },
			 	},
				actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				
				rendererSettings: {
					dataviewer: {
						storeid: STOREID_USERLIST,
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
							{	label: 'Panels',
								name: 'panels',
								renderdef: {}
							},
							{	label: 'Smaller Panels',
								name: 'panels',
								renderdef: {
									rendererSettings: {
										panels: {narrow: true},
									},
								}
							},
							{	label: 'Cards',
								name: 'cards',
								renderdef: {}
							},
							{	label: 'Tiles',
								name: 'tiles',
								renderdef: {
									visiblefields: ['PK_ID', 'EMAIL', 'STATUS', "LAST_LOGIN"],
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
						]
					},
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
	// Intro
	parent.append("<p>Create roles and assign permissions to them. All users assigned to the role will get those permissions.</p>");
	
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
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the role <b>&quot;'+current.NAME+'&quot;</b> ?\', \'Delete\', \'cfw_usermgmt_delete(\\\'roles\\\','+current.PK_ID+');\')">'
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
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_printGroupList(data){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Button
	parent.append("<p>Groups are basically the same as roles, only difference is that users can choose to share things like dashboards with groups. You can assign additional permissions through groups.</p>");
	
	//--------------------------------
	// Button
	var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_usermgmt_createGroup()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_usermgmt_createGroup')
					   + '</button>');
	
	parent.append(createButton);
	
	//--------------------------------
	// Table
	
	var cfwTable = new CFWTable();
	cfwTable.addHeaders(['ID', "Name", "Description"]);
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any groups in the list.");
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
				+'onclick="cfw_usermgmt_editGroup('+current.PK_ID+');">'
				+ '<i class="fa fa-pen"></i>'
				+ '</button></td>';
			
			//Delete Button
			if(current.IS_DELETABLE){
				htmlString += '<td><button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the group <b>&quot;'+current.NAME+'&quot;</b>?\', \'Delete\', \'cfw_usermgmt_delete(\\\'groups\\\','+current.PK_ID+');\')">'
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
 * Print the full audit;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_executeFullAudit(){
	
	CFW.ui.confirmExecute('Depending on number of users and audits, this might impact your application performance. Wanna do it anyway?', 'Full speed ahead!', 
		function(){
			CFW.http.getJSON(CFW_USRMGMT_URL, {action: "fetch", item: "fullaudit"}, function(data){
				if(data.payload != null){
					var parent = $("#tab-content");
					
					var printButton = $('#printButton');
					
					if(printButton.length == 0){
						var printButton = $('<button id="printButton" class="btn btn-sm btn-primary ml-2 mb-2" onclick="cfw_usermgmt_FullAuditPrintView()">'
								+ '<i class="fas fa-print"></i> Create Print View'
						   + '</button>');
						parent.append(printButton);
					}
					
					
					var toc = $('<div id="toc">');
					parent.append(toc);
					
					var auditResults = $('<div id="auditResults">');
					parent.append(auditResults);
	
					cfw_usermgmt_formatAuditResults(auditResults, data.payload);
					
					CFW.ui.toc(auditResults, toc);
					
				}
			})
		}
	);

}

/******************************************************************
 * Print the full audit;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_printFullAuditView(){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Button
	var createButton = $('<button class="btn btn-sm btn-danger mb-2" onclick="cfw_usermgmt_executeFullAudit()">'
							+ '<i class="fas fa-bolt"></i> '+ CFWL('cfw_core_execute')
					   + '</button>');
	
	parent.append(createButton);
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
	
	$('#cfw-container').css('max-width', '80%');
	
	cfw_usermgmt_draw(options);
}

function cfw_usermgmt_draw(options){
	
	cfw_usermgmt_reset();
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){

		
		var url = "./usermanagement/data"
		switch(options.tab){
		
			case "users":			CFW.http.fetchAndCacheData(url, {action: "fetch", item: "users"}, "users", cfw_usermgmt_printUserList);
									break;
									
			case "roles":			CFW.http.fetchAndCacheData(url, {action: "fetch", item: "roles"}, "roles", cfw_usermgmt_printRoleList);
									break;
									
			case "groups":			CFW.http.fetchAndCacheData(url, {action: "fetch", item: "groups"}, "groups", cfw_usermgmt_printGroupList);
			break;
									
			case "permissions":		CFW.http.fetchAndCacheData(url, {action: "fetch", item: "permissions"}, "permissions", cfw_usermgmt_printPermissionList);
									break;	
									
			case "fullaudit":		cfw_usermgmt_printFullAuditView();
			break;	
			
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 100);
}
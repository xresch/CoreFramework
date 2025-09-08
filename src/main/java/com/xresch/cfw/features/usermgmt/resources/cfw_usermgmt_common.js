
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/

var CFW_USERMGMT_URL = "./usermanagement/data";
var CFW_USERMGMT_SCOPE = ""; // either usermgmt or groups
var CFW_USERMGMT_SCOPE_GROUPS = "groups";
var CFW_USERMGMT_SCOPE_USERMGMT = "usermanagement";

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_setScopeGroups(){			CFW_USERMGMT_SCOPE = CFW_USERMGMT_SCOPE_GROUPS; }
function cfw_usermgmt_setScopeUserManagement(){	CFW_USERMGMT_SCOPE = CFW_USERMGMT_SCOPE_USERMGMT; }

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_createToggleTable(parent, mapName, itemID){

	//-----------------------------------
	// Render Data
	var rendererSettings = {
			data: null,
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
					var params = {action: "update"
								, item: mapName
								, itemid: itemID
								, listitemid: record.PK_ID
								};
					var cfwToggleButton = CFW.ui.createToggleButton(CFW_USERMGMT_URL, params, (record.ITEM_ID == itemID));
					
					if(record.IS_DELETABLE != null && !record.IS_DELETABLE){
						cfwToggleButton.setLocked();
					}
					
					return cfwToggleButton.getButton();
		 		},

		 	},
			rendererSettings: {
				dataviewer: {
					defaultsize: 25,
					sortoptions: ['NAME', 'DESCRIPTION'],
					storeid: 'usermgmtToggleTable'+mapName+itemID,
					datainterface: {
						url: CFW_USERMGMT_URL,
						item: mapName,
						customparams: {
							id: itemID
						}
					},
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
								
}

/******************************************************************
 * Delete User, Role or Group
 ******************************************************************/
function cfw_usermgmt_delete(item, ids){
	
	CFW.ui.confirmExecute("As this cannot be reverted: Are you <b>REALLY REALLY</b> sure you want to delete this group?"
		, "Yes, I know what I am doing", function(){
	
		var params = {action: "delete", item: item, ids: ids};
		CFW.http.getJSON(CFW_USERMGMT_URL, params, 
			function(data) {
				if(data.success){
					//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
					//clear cache and reload data
					CFW.cache.data[item] = null;
					cfw_usermgmt_common_redrawCallback({tab: item});
				}else{
					CFW.ui.showModalSmall("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</b> be deleted.</span>');
				}
		});
	}
	);
		
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_common_redrawCallback(options){
	
	CFW.cache.clearCache();
	
	if(CFW_USERMGMT_SCOPE == CFW_USERMGMT_SCOPE_GROUPS){
		 cfw_usermgmt_groups_draw(options);
	}else{
		cfw_usermgmt_draw(options);
	}
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_usermgmt_createGroup(){
	
	var createGroupForm = $('<div id="cfw-usermgmt-createGroup">');	

	var formID = "cfwCreateGroupForm";
	if(CFW_USERMGMT_SCOPE == CFW_USERMGMT_SCOPE_GROUPS){
		formID = "cfwCreateGroupWithOwnerForm";
	}
	
	CFW.http.getForm(formID, createGroupForm);
	CFW.ui.showModalMedium(CFWL('cfw_usermgmt_createGroup', "Create Group"), createGroupForm, "cfw_usermgmt_common_redrawCallback({tab: 'groups'})" );
	
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

	if(CFW_USERMGMT_SCOPE == CFW_USERMGMT_SCOPE_USERMGMT){
		var permissionDiv = $('<div id="cfw-usermgmt-roles">');
		permissionDiv.append('<h2>'+CFWL('cfw_usermgmt_permissions', "Permissions")+'</h2>');
		allDiv.append(permissionDiv);
		
		cfw_usermgmt_createToggleTable(permissionDiv, "rolepermissionmap", roleID)
	}
	
	//-----------------------------------
	// Users in Role
	//-----------------------------------
	var usersInGroupDiv = $('<div id="cfw-usermgmt-groups">');
	usersInGroupDiv.append('<h2>'+CFWL('cfw_usermgmt_users_in_group', "Users in Group")+'</h2>');
	allDiv.append(usersInGroupDiv);
	
	CFW.ui.showModalMedium("Edit Group", allDiv, "cfw_usermgmt_common_redrawCallback({tab: 'groups'})");
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USERMGMT_URL, {action: "getform", item: "editgroup", id: roleID}, detailsDiv);
	CFW.http.getJSON(CFW_USERMGMT_URL, {action: "fetch", item: "usersforrole", id: roleID}, function(data){
		if(data.payload != null){
			var renderSettings = {
					data: data.payload,
					titlefields: ['USERNAME'],
					visiblefields: ['USER_ID', 'USERNAME', 'FIRSTNAME','LASTNAME', 'EMAIL'],
					actions: [
						function(record, value){//Toggle Button
							var params = {action: "update", item: "userrolemap", itemid: record.USER_ID, listitemid: roleID};
							var cfwToggleButton = CFW.ui.createToggleButton(CFW_USERMGMT_URL, params, true);
							
							if(record.IS_REMOVABLE != null && !record.IS_REMOVABLE){
								cfwToggleButton.setLocked();
							}
							return cfwToggleButton.button;
						}
					],
					rendererSettings: {
						dataviewer: {
							//storeid: 'rolelist',
							renderers: CFW.render.createDataviewerDefaults()
						}
					}
				}

			
			var tableRenderer = CFW.render.getRenderer('dataviewer');
			var table = tableRenderer.render(renderSettings);
			usersInGroupDiv.append(table);
		}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_printGroupListCanEdit(data){
	cfw_usermgmt_printGroupList(data, true, "This list contains all the groups that you can edit. Either you are the owner of the group or an editor.");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_printGroupListOverview(data){
	cfw_usermgmt_printGroupList(data, false, "Here you can find a list of all groups and their owners and editors. This can help you finding someone that can add you to a group.");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_usermgmt_printGroupListAdmin(data){
	cfw_usermgmt_printGroupList(data, true, "Groups are basically the same as roles, only difference is that users can choose to share things like dashboards with groups. You can assign additional permissions through groups.");
}
	
/******************************************************************
 * Print the list of roles or groups;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_usermgmt_printGroupList(data, allowEdit, description){
	
	let parent = $("#tab-content");
	
	//--------------------------------
	// Description
	parent.append("<p>"+description+"</p>");
	
	//--------------------------------
	// Button
	if(allowEdit){
		var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_usermgmt_createGroup()">'
								+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_usermgmt_createGroup')
						   + '</button>');
		
		parent.append(createButton);
	}
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		if(data.payload.length == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any groups in the list.");
		}
		//======================================
		// Prepare actions
		var actionButtons = [];
		
		if(allowEdit){
			//-------------------------
			// Edit Button
			actionButtons.push(
				function (record, id){ 
					return '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_usermgmt_editGroup('+record.PK_ID+');">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				});
				
			//-------------------------
			// Change Owner Button
			actionButtons.push(
				function (record, id){
	
					if(JSDATA.userid == record.FK_ID_GROUPOWNER
					|| CFW_USERMGMT_SCOPE == CFW_USERMGMT_SCOPE_USERMGMT ){
						return '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
							+'onclick="cfw_usermgmt_changeGroupOwner('+id+');">'
							+ '<i class="fas fa-user-edit"></i>'
							+ '</button>';
					}
					
					return "&nbsp;";
				});
				
			//-------------------------
			// Delete Button
			actionButtons.push(
				function (record, id){
					
				if( record.IS_DELETABLE 
				&& (JSDATA.userid == record.FK_ID_GROUPOWNER
					|| CFW_USERMGMT_SCOPE == CFW_USERMGMT_SCOPE_USERMGMT )
				){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
						+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the group <b>&quot;'+record.NAME+'&quot;</b> ?\', \'Delete\', \'cfw_usermgmt_delete(\\\'groups\\\','+record.PK_ID+');\')">'
						+ '<i class="fa fa-trash"></i>'
						+ '</button>';
				}
				
				return '&nbsp;';
	
			});
		}
		
		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME',],
			 	titleformat: '{0}',
			 	visiblefields: ['PK_ID', 'OWNER', 'NAME', 'JSON_EDITORS', 'DESCRIPTION'],
			 	labels: {
			 		PK_ID: "ID",
			 		JSON_EDITORS: "Editors"
			 	},
			 	customizers: {
					JSON_EDITORS: cfw_customizer_badgesFromObjectValues 
				 },
				actions: actionButtons,
				
				rendererSettings: {
					dataviewer: {
						storeid: 'grouplist',
						renderers: CFW.render.createDataviewerDefaults()
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
 * Edit Dashboard
 ******************************************************************/
function cfw_usermgmt_changeGroupOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-group-details">');

	
	CFW.ui.showModalMedium(
			"Edit Owner", 
			formDiv, 
			"cfw_usermgmt_common_redrawCallback({tab: 'groups'})"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_USERMGMT_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}


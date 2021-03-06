
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

var CFW_DASHBOARDLIST_URL = "./list";
var CFW_DASHBOARDLIST_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_dashboardlist_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		if(CFW.hasPermission('Dashboard Creator') 
		|| CFW.hasPermission('Dashboard Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mydashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'mydashboards\'})"><i class="fas fa-user-circle mr-2"></i>My Dashboards</a></li>'
				+'<li class="nav-item"><a class="nav-link" id="tab-shareddashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'shareddashboards\'})"><i class="fas fa-share-alt mr-2"></i>Shared Dashboards</a></li>'
			);
		}else if(CFW.hasPermission('Dashboard Viewer')){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-shareddashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'shareddashboards\'})"><i class="fas fa-share-alt mr-2"></i>Shared Dashboards</a></li>');
		}
		
		if(CFW.hasPermission('Dashboard Admin')){
					list.append(
						'<li class="nav-item"><a class="nav-link" id="tab-admindashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'admindashboards\'})"><i class="fas fa-tools mr-2"></i>Manage Dashboards</a></li>');
		}
		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_dashboardlist_createDashboard(){
	
	var html = $('<div id="cfw-dashboard-createDashboard">');	

	CFW.http.getForm('cfwCreateDashboardForm', html);
	
	CFW.ui.showModalMedium(CFWL('cfw_dashboardlist_createDashboard', 
			CFWL("cfw_dashboardlist_createDashboard", "Create Dashboard")), 
			html, "CFW.cache.clearCache(); cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	
}

/******************************************************************
 * Edit Dashboard
 ******************************************************************/
function cfw_dashboardlist_importDashboard(){
	
	var uploadHTML = 
		'<p>Select a previously exported dashboard file. Share settings will be imported as well. If you exported the dashboard from another application or application instance, the widgets might not be able to load correctly.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Select File to Import:</label>'
			+'<input type="file" class="form-control" name="importFile" id="importFile" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_dashboardlist_importDashboardExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import Dashboard", 
			uploadHTML,
			"CFW.cache.clearCache(); cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
}

/******************************************************************
 * Edit Dashboard
 ******************************************************************/
function cfw_dashboardlist_importDashboardExecute(){
	
	var file = document.getElementById('importFile').files[0];
	var reader = new FileReader();

	  // Read file into memory as UTF-8
	  reader.readAsText(file, "UTF-8");
	  
	  reader.onload = function loaded(evt) {
		  // Obtain the read file data
		  var fileString = evt.target.result;
		  
			var params = {action: "import", item: "dashboards", jsonString: fileString};
			CFW.http.postJSON(CFW_DASHBOARDLIST_URL, params, 
				function(data) {
					//do nothing
				}
			);

		}
}


/******************************************************************
 * Edit Dashboard
 ******************************************************************/
function cfw_dashboardlist_editDashboard(id){
	
	var allDiv = $('<div id="cfw-dashboard">');	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-dashboard-details">');
	detailsDiv.append('<h2>'+CFWL('cfw_dashboardlist_dashboard', "Dashboard")+' Details</h2>');
	allDiv.append(detailsDiv);
	
	CFW.ui.showModalMedium(
			CFWL("cfw_dashboardlist_editDashboard","Edit Dashboard"), 
			allDiv, 
			"CFW.cache.clearCache(); cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "editdashboard", id: id}, detailsDiv);
	
}

/******************************************************************
 * Edit Dashboard
 ******************************************************************/
function cfw_dashboardlist_changeDashboardOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-dashboard-details">');

	
	CFW.ui.showModalMedium(
			CFWL("cfw_dashboardlist_editDashboard","Edit Dashboard"), 
			formDiv, 
			"CFW.cache.clearCache(); cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_dashboardlist_delete(ids){
	
	var params = {action: "delete", item: "dashboards", ids: ids};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.clearCache();
				cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_dashboardlist_duplicate(id){
	
	var params = {action: "duplicate", item: "dashboard", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboardlist_printMyDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'mydashboards');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboardlist_printSharedDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'shareddashboards');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboardlist_printAdminDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'admindashboards');
}


/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_dashboardlist_printDashboards(data, type){
	
	var parent = $("#tab-content");

	//--------------------------------
	// Button
	if(type == 'mydashboards'){
		var createButton = $('<button class="btn btn-sm btn-success m-1" onclick="cfw_dashboardlist_createDashboard()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_dashboardlist_createDashboard')
					   + '</button>');
	
		parent.append(createButton);
		
		var importButton = $('<button class="btn btn-sm btn-success m-1" onclick="cfw_dashboardlist_importDashboard()">'
				+ '<i class="fas fa-upload"></i> '+ CFWL('cfw_core_import', 'Import')
		   + '</button>');

		parent.append(importButton);
		
	}
	
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any dashboards in the list.");
		}
		
		//-----------------------------------
		// Prepare Columns
		var showFields = [];
		if(type == 'mydashboards'){
			showFields = ['NAME', 'DESCRIPTION', 'TAGS', 'IS_SHARED'];
		}else if (type == 'shareddashboards'){
			showFields = ['OWNER', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'admindashboards'){
			showFields = ['PK_ID', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS','IS_SHARED'];
		}
		
		
		//======================================
		// Prepare actions
		
		//-------------------------
		// View Button
		var actionButtons = [ 
			function (record, id){ 
				return '<a class="btn btn-success btn-sm" role="button" href="/app/dashboard/view?id='+id+'&title='+encodeURIComponent(record.NAME)+'" alt="View" title="View" >'
				+ '<i class="fa fa-eye"></i>'
				+ '</a>';
			}];

		//-------------------------
		// Edit Button
		if(type == 'mydashboards'
		|| type == 'admindashboards'){

			actionButtons.push(
				function (record, id){ 
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_USER || type == 'admindashboards'){
						htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
							+'onclick="cfw_dashboardlist_editDashboard('+id+');">'
							+ '<i class="fa fa-pen"></i>'
							+ '</button>';
					}else{
						htmlString += '&nbsp;';
					}
					return htmlString;
				});
		}
		
		//-------------------------
		// Change Owner Button
		if(CFW.hasPermission('Dashboard Creator') 
		&& type == 'admindashboards'){

					actionButtons.push(
						function (record, id){
							var htmlString = '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
									+'onclick="cfw_dashboardlist_changeDashboardOwner('+id+');">'
									+ '<i class="fas fa-user-edit"></i>'
									+ '</button>';
							
							return htmlString;
						});
				}
		
		//-------------------------
		// Duplicate Button
		if(CFW.hasPermission('Dashboard Creator') 
		|| CFW.hasPermission('Dashboard Admin')){

			actionButtons.push(
				function (record, id){
					var htmlString = '<button class="btn btn-warning btn-sm" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your dashboards.\', \'Do it!\', \'cfw_dashboardlist_duplicate('+id+');\')">'
							+ '<i class="fas fa-clone"></i>'
							+ '</button>';
					
					return htmlString;
				});
		}
		
		//-------------------------
		// Export Button
		if(type == 'mydashboards'
		|| type == 'admindashboards'){

			actionButtons.push(
				function (record, id){
					return '<a class="btn btn-warning btn-sm" target="_blank" alt="Export" title="Export" '
							+' href="'+CFW_DASHBOARDLIST_URL+'?action=fetch&item=export&id='+id+'" download="'+record.NAME.replaceAll(' ', '_')+'_export.json">'
							+'<i class="fa fa-download"></i>'
							+ '</a>';
					
				});
		}

		//-------------------------
		// Delete Button
		if(type == 'mydashboards'
		|| type == 'admindashboards'){
			actionButtons.push(
				function (record, id){
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_USER || type == 'admindashboards'){
						htmlString += '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the dashboard <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_dashboardlist_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';
					}else{
						htmlString += '&nbsp;';
					}
					return htmlString;
				});
		}

		
		//-------------------------
		// Sharing Details View
		sharingDetailsView = null;
		
		if(type == 'mydashboards'
		|| type == 'admindashboards'){
			sharingDetailsView = 
				{ 
					label: 'Sharing Details',
					name: 'table',
					renderdef: {
						visiblefields: ["NAME", "IS_SHARED", "JSON_SHARE_WITH_USERS", "JSON_SHARE_WITH_GROUPS", "JSON_EDITORS", "JSON_EDITOR_GROUPS"],
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
		}

		//-----------------------------------
		// Render Data
		
		var badgeCustomizerFunction = function(record, value) { 
 			var badgesHTML = '<div>';
 			
 			for(id in value){
 				badgesHTML += '<span class="badge badge-primary m-1">'+value[id]+'</span></br>';
 			}
 			badgesHTML += '</div>';
 			
 			return badgesHTML;
 			 
 		};
 		
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: '{0}',
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
			 		TAGS: badgeCustomizerFunction,
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
						storeid: 'dashboards-'+type,
						renderers: [
							{	label: 'Table',
								name: 'table',
								renderdef: {
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
		CFW.ui.addAlert('error', 'Something went wrong and no dashboards can be displayed.');
	}
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

function cfw_dashboardlist_initialDraw(){
	
	cfw_dashboardlist_createTabs();
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("dashboardlist-lasttab", "mydashboards");
	
	if(CFW.hasPermission('Dashboard Viewer') 
	&& !CFW.hasPermission('Dashboard Creator') 
	&& !CFW.hasPermission('Dashboard Admin')){
		tabToDisplay = "shareddashboards";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	cfw_dashboardlist_draw({tab: tabToDisplay});
}

function cfw_dashboardlist_draw(options){
	CFW_DASHBOARDLIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("dashboardlist-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mydashboards":		CFW.http.fetchAndCacheData(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "mydashboards"}, "mydashboards", cfw_dashboardlist_printMyDashboards);
										break;	
			case "shareddashboards":	CFW.http.fetchAndCacheData(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "shareddashboards"}, "shareddashboards", cfw_dashboardlist_printSharedDashboards);
										break;
			case "admindashboards":		CFW.http.fetchAndCacheData(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "admindashboards"}, "admindashboards", cfw_dashboardlist_printAdminDashboards);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 50);
}
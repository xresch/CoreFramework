
/**************************************************************************************************************
 * This file contains the javscript code for building up the view of the space page.
 * @author Reto Scheiwiller, (c) Copyright 2026
 **************************************************************************************************************/

var URL_HIERARCHY = "/app/hierarchy";

var CFW_SPACES_CONFIG_ID = "cfwspace";

var CFW_SPACES_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function om_spaces_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		

		list.append('<li class="nav-item"><a class="nav-link" id="tab-spaces-list" data-toggle="pill" href="#" role="tab" onclick="om_spaces_draw({tab: \'spaces-list\'})"><i class="fas fa-share-alt mr-2"></i>Space List</a></li>');
		
		
		if( CFW.hasPermission('Space: Admin All') ){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-spaces-hierarchy" data-toggle="pill" href="#" role="tab" onclick="om_spaces_draw({tab: \'spaces-hierarchy\'})"><i class="fas fa-sitemap mr-2"></i>Hierarchy</a></li>'
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
function om_spaces_addSpace(type){
	
	var allDiv = $('<div id="om-edit-div">');	

	//-----------------------------------
	// Details
	//-----------------------------------
	var detailsDiv = $('<div id="formDiv">');
	detailsDiv.append('<h2>Space Details</h2>');
	allDiv.append(detailsDiv);
	
	
	CFW.ui.showModalMedium(
			"Create Space", 
			allDiv, 
			"om_spaces_reloadSpaceSelectorAndDraw()"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	var selectedSpace = cfw_spaces_getSelectedSpace();
	CFW.http.createForm(URL_CFWSPACES, {action: "getform", item: "createspace", type: type, spaceid: selectedSpace}, detailsDiv);
	
}

/******************************************************************
 * Edit
 ******************************************************************/
function om_spaces_edit(id){
	
	var allDiv = $('<div id="om-edit-div">');	

	//-----------------------------------
	// Details
	//-----------------------------------
	var detailsDiv = $('<div id="formDiv">');
	detailsDiv.append('<h2>Edit Space</h2>');
	allDiv.append(detailsDiv);
	
	
	CFW.ui.showModalMedium(
			"Edit Space", 
			allDiv, 
			"om_spaces_draw(CFW_SPACES_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(URL_CFWSPACES, {action: "getform", item: "editspace", id: id}, detailsDiv);
	
}


/******************************************************************
 * Delete
 ******************************************************************/
/*function om_spaces_delete(id){
	
	var params = {action: "delete", item: "space", id: id};
	CFW.http.getJSON(URL_CFWSPACES, params, 
		function(data) {
			if(data.success){
				om_spaces_reloadSpaceSelectorAndDraw();
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected space could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}*/


/******************************************************************
 * Example of pagination of static data using the dataviewer render.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function om_spaces_printList(data){
	
	var parent = $("#tab-content");

	//--------------------------------
	// Button
	if(JSDATA.canCreateSpaces){
		var createSpaceButton = $('<button class="btn btn-sm btn-primary mb-2" onclick="om_spaces_addSpace(\'ROOT_SPACE\')">'
							+ '<i class="mr-1 fas fa-sitemap"></i>Create Root Space</button>');
	
		parent.append(createSpaceButton);
	}
	
	if(data.isAdminForSelectedSpace){
		var dropdownHTML = '<div class="dropdown d-inline pl-1">'
			+ '<button class="btn btn-sm btn-success mb-2 dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
			+ '  <i class="fas fa-plus-circle"></i> '+ CFWL('cfw_core_add', 'Add')
			+ '</button>'
			+ '  <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">';
		
		var typesArray = JSDATA.types.split(',');
		for(var i = 0; i < typesArray.length; i++){
			var type = typesArray[i];
			dropdownHTML += '	<a class="dropdown-item" onclick="om_spaces_addSpace(\''+type+'\')">'+CFW.format.fieldNameToLabel(type)+'</a>';
		}
		dropdownHTML += '</div> </div>';
	
		parent.append(dropdownHTML);
	}
	
	//--------------------------------
	// Table
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any spaces in the list.");
			return;
		}
				
		
		//======================================
		// Prepare actions
		var actionButtons = [];
		
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				if(data.isAdminForSelectedSpace){
					return '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
							+'onclick="om_spaces_edit('+id+');">'
							+ '<i class="fa fa-pen"></i>'
							+ '</button>';
				}

			});

		
		//-------------------------
		// Duplicate Button
		/*actionButtons.push(
			function (record, id){
				return '<button class="btn btn-warning btn-sm" alt="Duplicate" title="Duplicate" '
						+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>.\', \'Do it!\', \'om_spaces_duplicate('+id+');\')">'
						+ '<i class="fas fa-clone"></i>'
						+ '</button>';
		});*/
		
		//-------------------------
		// Delete Button
/*		actionButtons.push(
			function (record, id){
				if(record.TYPE == 'ORG' && !JSDATA.isSpacesAdmin){
					return '&nbsp;';
				}
				if(data.isAdminForSelectedSpace){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+'onclick="CFW.ui.confirmExecute(\'Are you sure you want to delete the space <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and all related subordinate spaces?\', \'Delete\', \'om_spaces_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';
					}
				return '&nbsp;';
				
			});*/
		
		//-------------------------
		// Formatter
		var trueFalseFormatter = 
				function(record, value) { 
		 			var likesTiramisu = value;
		 			if(likesTiramisu){
							return '<span class="badge badge-success m-1">true</span>';
					}else{
						return '<span class="badge badge-danger m-1">false</span>';
					} 
		 		};

		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['ABBREVIATION', 'NAME'],
			 	titleformat: '[{0}] - {1}',
			 	visiblefields: ["PK_ID", "TYPE", "ABBREVIATION", "NAME", "DESCRIPTION", "IS_ENABLED", "SHARED_EMAIL"],
			 	labels: {
			 		PK_ID: "ID",
					IS_ENABLED: "Enabled",
			 	},
			 	customizers: {
			 		IS_ENABLED: trueFalseFormatter,
					DESCRIPTION: function(record, value) { 
			 			if( CFW.utils.isNullOrEmpty(value) ){
								return '&nbsp;';
						}else{
							return '<div class="maxvw-25">'+value+'</div>';
						} 
			 		}
			 	},
				actions: actionButtons,
				hierarchy: true,
			 	hierarchyAsTree: true,

//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				
				rendererSettings: {
					dataviewer: {
						storeid: 'spaceslist',
						renderers: [
							{	label: 'Hierarchical Table',
								name: 'table',
								
								renderdef: {
									merge: false,
									titlefields: ['NAME'],
									rendererSettings: {
										table: {filterable: false, narrow: true},
										dataviewer: {sortable: false}
									},
								}
							},
							{	label: 'Hierachical Panels',
								name: 'panels',
								renderdef: {
									visiblefields: ["NONE"],
									rendererSettings: {
										panels: {narrow: true},
										dataviewer: {sortable: false}
									},
								}
							},
							{	label: 'Hierachical Panels Detailed',
								name: 'panels',
								renderdef: {
									rendererSettings: {
										panels: {narrow: true},
										dataviewer: {sortable: false}
									},
								}
							},
							{	label: 'Flat Table',
								name: 'table',
								renderdef: {
									hierarchyAsTree: false,
									hierarchyIconClasses: "",
									rendererSettings: {
										table: {filterable: false, narrow: true},
									},
								}
							},
							{	label: 'Flat Panels',
								name: 'panels',
								renderdef: {
									hierarchyAsTree: false,
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
				},
			};
		
		var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no users can be displayed.');
	}
}


/******************************************************************
 *
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function om_spaces_printSortableHierarchy(data){
	
	var parent = $("#tab-content");
	parent.html("");
	parent.append("<h1>Sortable Hierarchy</h1>");
	parent.append("<p>Drag and drop the items to change the hierachical structure. Use the up and down arrows to change their order of the elements that have the same parent.</p>");

	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['ABBREVIATION', 'NAME'],
			 	titleformat: '[{0}] {1}',
			 	visiblefields: [],
			 	labels: {
			 		PK_ID: "ID",
			 	},
			 	customizers: {
			 		LIKES_TIRAMISU: function(record, value) { 
			 			var likesTiramisu = value;
			 			if(likesTiramisu){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
						}
			 			 
			 		}
			 	},				
				rendererSettings: {
					hierarchy_sorter: {configid: CFW_SPACES_CONFIG_ID}
				},
			};
				
		var renderResult = CFW.render.getRenderer('hierarchy_sorter').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no items can be displayed.');
	}
}

/******************************************************************
 * 
 ******************************************************************/
function om_spaces_reloadSpaceSelectorAndDraw(){
	cfw_spaces_createSpaceSelector(function(spaceid){
		om_spaces_draw(null);
	}, true);
}

/******************************************************************
 * 
 ******************************************************************/
function om_spaces_initialDraw(){
	
	CFW.cache.clearCache();
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '90%');
	
	//-------------------------------------------
	// Create Tabs
	om_spaces_createTabs();
	
	var tabToDisplay = CFW.cache.retrieveValueForPage("om-spaces-lasttab", "spaces-list");
	
	if(CFW.hasPermission('Spaces: Viewer') 
	&& !CFW.hasPermission('Spaces: Admin')){
		tabToDisplay = "spaces-list";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	CFW_SPACES_LAST_OPTIONS = {tab: tabToDisplay};
	om_spaces_reloadSpaceSelectorAndDraw();
	
}

/******************************************************************
 * Main function
 ******************************************************************/
function om_spaces_draw(options){
	
	if(options == null){
		options = CFW_SPACES_LAST_OPTIONS;
	}
	
	CFW_SPACES_LAST_OPTIONS = options;
	
	var selectedSpace = cfw_spaces_getSelectedSpace();
	
	CFW.cache.storeValueForPage("om-spaces-lasttab", options.tab);
	
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "spaces-list":		CFW.http.getJSON(URL_CFWSPACES, {configid: CFW_SPACES_CONFIG_ID, action: "fetch", item: "spaceslist", spaceid: selectedSpace}, om_spaces_printList);
										break;	
										
			case "spaces-hierarchy":	CFW.http.getJSON(URL_HIERARCHY, {configid: CFW_SPACES_CONFIG_ID, action: "fetch", item: "hierarchy", rootid: selectedSpace}, om_spaces_printSortableHierarchy);
										break;
					
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}
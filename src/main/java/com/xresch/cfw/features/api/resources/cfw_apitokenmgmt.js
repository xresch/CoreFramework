
/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var CFW_APITOKENMGMT_URL = "./tokenmanagement";


/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_apitokenmgmt_addToken(){
	
	var html = $('<div>');	

	CFW.http.getForm('cfwCreateTokenForm', html);
	
	CFW.ui.showModal(
			"Create Token", 
			html, 
			"CFW.cache.clearCache(); cfw_apitokenmgmt_draw()"
		);
	
}
/******************************************************************
 * Edit Role
 ******************************************************************/
function cfw_apitokenmgmt_edit(id){
	
	//-----------------------------------
	// Details
	//-----------------------------------
	var detailsDiv = $('<div id="jsexamples-details">');
	
	CFW.ui.showModal(
		"Edit Token", 
		detailsDiv, 
		"CFW.cache.clearCache(); cfw_apitokenmgmt_draw()"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_APITOKENMGMT_URL, {action: "getform", item: "edittoken", id: id}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_apitokenmgmt_delete(id){
	
	params = {action: "delete", item: "token", id: id};
	CFW.http.getJSON(CFW_APITOKENMGMT_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showSmallModal('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.clearCache();
				cfw_apitokenmgmt_draw();
			}else{
				CFW.ui.showSmallModal("Error!", '<span>The selected token could <b style="color: red">NOT</strong> be deleted.</span>');
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
			CFW.ui.addToastInfo("Hmm... seems there aren't any people in the list.");
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
			 	visiblefields: ['PK_ID', 'TOKEN', 'DESCRIPTION', 'JSON_RESPONSIBLE_USERS'],
			 	labels: {
			 		PK_ID: "ID",
			 	},
			 	customizers: {
			 		JSON_RESPONSIBLE_USERS: function(record, value) { 
			 			let html = ''; 
			 			for(key in value){
			 				html += '<span class="badge badge-primary m-1">'+value[key]+'</span>'
			 			}
			 			return html;
			 			 
			 		}
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
									visiblefields: ['PK_ID', 'TOKEN', 'JSON_RESPONSIBLE_USERS'],
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
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		CFW.http.fetchAndCacheData(CFW_APITOKENMGMT_URL, {action: "fetch", item: "tokenlist"}, "tokenlist", cfw_apitokenmgmt_printTokenList);

		CFW.ui.toogleLoader(false);
	}, 50);
}

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

var CFW_CTXSETTINGS_URL = "./contextsettings";
var CFW_CTXSETTINGS_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_contextsettings_reset(){
	
	var parent = $("#cfw-container");
	parent.html("");
	
	var dropdownHTML = '<div class="dropdown">'
		+ '<button class="btn btn-sm btn-success mb-2 dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
		+ '  <i class="fas fa-plus-circle"></i> '+ CFWL('cfw_core_add', 'Add')
		+ '</button>'
		+ '  <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">';
	
	var typesArray = JSDATA.types.split(',');
	for(var i = 0; i < typesArray.length; i++){
		var type = typesArray[i];
		dropdownHTML += '	<a class="dropdown-item" onclick="cfw_contextsettings_create(\''+type+'\')">'+type+'</a>';
	}
	dropdownHTML += '</div> </div>';
	//--------------------------------
	// Button
//	var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_contextsettings_create()">'
//						+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_core_add', 'Add')
//				   + '</button>');

	parent.append(dropdownHTML);
}

/******************************************************************
 * Create
 ******************************************************************/
function cfw_contextsettings_create(type){
	
	var html = $('<div id="createContextSettings">');	

	//-----------------------------------
	// Create Modal
	//-----------------------------------
	var formDiv = $('<div id="formDiv">');
	formDiv.append('<h2>'+CFWL('cfw_contextsettings_dashboard', "Context Settings")+' Details</h2>');
	html.append(formDiv);
	CFW.ui.showModalMedium(
			CFWL("cfw_contextsettings_create","Create Context Settings"), 
			html, 
			"CFW.cache.clearCache(); cfw_contextsettings_draw(CFW_CTXSETTINGS_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_CTXSETTINGS_URL, {action: "getform", item: "createcontextsettings", type: type}, formDiv);
	
}
/******************************************************************
 * Edit
 ******************************************************************/
function cfw_contextsettings_edit(id){
	
	var allDiv = $('<div id="cfw-edit-div">');	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	var detailsDiv = $('<div id="formDiv">');
	detailsDiv.append('<h2>'+CFWL('cfw_contextsettings_dashboard', "Context Settings")+' Details</h2>');
	allDiv.append(detailsDiv);
	

	CFW.ui.showModalMedium(
			CFWL("cfw_contextsettings_edit","Edit Context Settings"), 
			allDiv, 
			"CFW.cache.clearCache(); cfw_contextsettings_draw(CFW_CTXSETTINGS_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_CTXSETTINGS_URL, {action: "getform", item: "editcontextsettings", id: id}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_contextsettings_delete(id){
	
	var params = {action: "delete", item: "contextsettings", id: id};
	CFW.http.getJSON(CFW_CTXSETTINGS_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showModalSmall('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.clearCache();
				cfw_contextsettings_draw(CFW_CTXSETTINGS_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Duplicate
 ******************************************************************/
function cfw_contextsettings_duplicate(id){
	
	var params = {action: "duplicate", item: "contextsettings", id: id};
	CFW.http.getJSON(CFW_CTXSETTINGS_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				cfw_contextsettings_draw(CFW_CTXSETTINGS_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * Print the list of context settings.
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_contextsettings_printContextSettings(data){
	
	var parent = $("#cfw-container");
		
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any context settings in the list.");
			return;
		}
		
		//======================================
		// Prepare actions
		var actionButtons = [];
		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
					+'onclick="cfw_contextsettings_edit('+id+');">'
					+ '<i class="fa fa-pen"></i>'
					+ '</button>';
				return htmlString;
			});
		
	

		//-------------------------
		// Duplicate Button
		actionButtons.push(
			function (record, id){
				var htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
						+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of the selected settings.\', \'Do it!\', \'cfw_contextsettings_duplicate('+id+');\')">'
						+ '<i class="fas fa-clone"></i>'
						+ '</button>';
				
				return htmlString;
			});
		
		
		//-------------------------
		// Delete Button
			actionButtons.push(
				function (record, id){
					var htmlString = '';

					htmlString += '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
						+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the settings?\', \'Delete\', \'cfw_contextsettings_delete('+id+');\')">'
						+ '<i class="fa fa-trash"></i>'
						+ '</button>';

					return htmlString;
			});
		}

		

		//-----------------------------------
		// Render Data
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['CFW_CTXSETTINGS_NAME'],
			 	titleformat: '{0}',
			 	visiblefields: ['PK_ID','CFW_CTXSETTINGS_TYPE', 'CFW_CTXSETTINGS_NAME', 'CFW_CTXSETTINGS_DESCRIPTION'],
			 	labels: {
			 		PK_ID: "ID",
			 		CFW_CTXSETTINGS_TYPE: CFWL('cfw_core_type', 'Type'),
			 		CFW_CTXSETTINGS_NAME: CFWL('cfw_core_name', 'Name'),
			 		CFW_CTXSETTINGS_DESCRIPTION: CFWL('cfw_core_description', 'Description')
			 	},
			 	customizers: {},
				actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				data: data.payload,
			rendererSettings: {
				dataviewer: {
					storeid: 'cfw_contextsettings',
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
						{	label: 'Tiles',
							name: 'tiles',
							renderdef: {
								visiblefields: ['PK_ID','CFW_CTXSETTINGS_TYPE'],
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
					],
				},
			},
		};
				
		var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
		
		parent.append(renderResult);
		
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'contextsettings', 
 *  }
 * @return 
 ******************************************************************/

function cfw_contextsettings_initialDraw(){
	cfw_contextsettings_draw({tab: "contextsettings"});
}

function cfw_contextsettings_draw(options){
	CFW_CTXSETTINGS_LAST_OPTIONS = options;
	
	cfw_contextsettings_reset();
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "contextsettings":		CFW.http.fetchAndCacheData(CFW_CTXSETTINGS_URL, {action: "fetch", item: "contextsettings"}, "contextsettings", cfw_contextsettings_printContextSettings);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 50);
}
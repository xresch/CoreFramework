
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

var CFW_DASHBOARDLIST_URL = "./list";
var URI_DASHBOARD_VIEW_PUBLIC = "/public/dashboard/view";
var CFW_DASHBOARDLIST_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_dashboardlist_tutorialsRegister(){
	cfw_dashboardlist_tutorialsMyDashboards();
	cfw_dashboardlist_tutorialsFavedDashboards();
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_dashboardlist_tutorialsMyDashboards(){
		
	//===================================================
	// Check has Tab
	if($("#tab-mydashboards").length == 0){
		return;
	}
	
	//===================================================
	// Tutorial: My Dashboards
	var DASHBOARD_NAME = 'A Nice Tutorial Dashboard';
	
	cfw_tutorial_bundleStart($("#tab-mydashboards"));
	
		//----------------------------------
		// Tab MyDashboard
		cfw_tutorial_addStep({
			  selector: "#tab-mydashboards"
			, clickable: false
			, text: "In this tab you can create, view and manage your own dashboards."
			, drawDelay: 500
			, beforeDraw: null
		});
		
		//----------------------------------
		// Create Button
		cfw_tutorial_addStep({
			  selector: "#button-add-dashboard"
			, clickable: false
			, text: "Using this button allows you to add a new dashboard."
			, drawDelay: 500
			, beforeDraw: function(){
				$("#tab-mydashboards").click();
			}
		});	

		//----------------------------------
		// Enter Name
		cfw_tutorial_addStep({
			  selector: 'input#NAME'
			, clickable: false
			, text: "We have to give the dashboard a name."
			, drawDelay: 1000
			, beforeDraw: function(){
				$("#button-add-dashboard").click();
				CFW.ui.waitForAppear('input#NAME').then(function(){
					
					$('#cfw-default-modal').addClass('cfw-tuts-highlight').css('z-index', "1055");
					$('input#NAME').val(DASHBOARD_NAME);
				})
			}
		});	
		
		//----------------------------------
		// Enter Description
		cfw_tutorial_addStep({
			  selector: "textarea#DESCRIPTION"
			, clickable: false
			, text: "We can enter a description for the dashboard."
			, drawDelay: 500
			, beforeDraw: function(){
				$('textarea#DESCRIPTION').val("Dashboard showing cute cats blocking someones keyboard.");
			}
		});	
		
		//----------------------------------
		// Enter TAGS
		cfw_tutorial_addStep({
			  selector: ".tutorial0-tagsparent"
			, clickable: false
			, text: "Lets add some tags too...."
			, drawDelay: 500
			, beforeDraw: function(){
				$('input#TAGS-tagsinput').parent().addClass('tutorial0-tagsparent');
				
				$('input#TAGS').tagsinput('add', 'cute');
				$('input#TAGS').tagsinput('add', 'cats');
				$('input#TAGS').tagsinput('add', 'keyboard');
				$('input#TAGS').tagsinput('add', 'siege');
				
			}
		});	
		
		//----------------------------------
		// Click Submit Button
		cfw_tutorial_addStep({
			  selector: "#cfwCreateDashboardForm-submitButton"
			, clickable: false
			, text: "With a click on create the dashboard will be added to your list."
			, drawDelay: 500
			, beforeDraw: null
		});	
		
		//----------------------------------
		// Close Button
		cfw_tutorial_addStep({
			  selector: "#cfw-default-modal-closebutton"
			, clickable: false
			, text: "After we created the dashboard we can close the modal."
			, drawDelay: 500
			, beforeDraw: function(){
				$("#cfwCreateDashboardForm-submitButton").click();
				
			}
		});		
		
		//----------------------------------
		// Set Display as
		cfw_tutorial_addStep({
			  selector: "select.dataviewer-displayas"
			, clickable: false
			, text: "Here we can choose how to display the records. Let me make sure it is set to table so the tutorial will work properly."
			, drawDelay: 1000
			, beforeDraw: function(){
				$("#cfw-default-modal-closebutton").click();
				$("select.dataviewer-displayas").val(0);
				
			}
		});	
		
		//----------------------------------
		// Set Filter
		cfw_tutorial_addStep({
			  selector: "input.dataviewer-filterquery"
			, clickable: false
			, text: "Now let this automated tutorial set a filter to find that dashboard we just created."
			, drawDelay: 1000
			, beforeDraw: function(){
				
				window.setTimeout(function(){
					$("input.dataviewer-filterquery").val(DASHBOARD_NAME);
				}, 1000);
			}
		});	
		
		//----------------------------------
		// First Row
		cfw_tutorial_addStep({
			  selector: ".cfwRecordContainer"
			, clickable: false
			, text: "Now we have the list filtered for our dashboard."
			, drawDelay: 500
			, beforeDraw: function(){
				$("input.dataviewer-filterquery").trigger('change');
				
			}
		});	
		
		//----------------------------------
		// Explain Favorite
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-0"
			, clickable: false
			, text: "Clicking the star will add the dashboard to your favorites."
			, drawDelay: 500
			, beforeDraw: function(){
				
				//add IDs to cells
				$(".cfwRecordContainer:first td").each(function(index){
					$(this).addClass('tutorial0-cell-'+index);
				});
				
			}
		});	
		
		//----------------------------------
		// Explain Name
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-1"
			, clickable: false
			, text: "Clicking the title will open your dashboard."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});		
		
		//----------------------------------
		// Explain Description
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-2"
			, clickable: false
			, text: "Here is the description, as precise as ever."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});		
		
		//----------------------------------
		// Explain Tags
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-3"
			, clickable: false
			, text: "Tags can be useful to filter and find the dashboard you search for faster."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});	
		
		//----------------------------------
		// Explain Shared
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-4"
			, clickable: false
			, text: "Here you can see if the dashboard is shared with others."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain View Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-6"
			, clickable: false
			, text: "Clicking this button opens the dashboard, same as clicking on the name of the dashboard."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain Edit Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-7"
			, clickable: false
			, text: "The edit button opens a modal to edit the settings of the dashboard."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
				
		//----------------------------------
		// Explain Duplicate Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-8"
			, clickable: false
			, text: "This will create a copy of the dashboard."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain Export Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-9"
			, clickable: false
			, text: "Here you can export your dashboard to store it to a file. Can be useful to create backups."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
				
		//----------------------------------
		// Explain Delete Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-10"
			, clickable: false
			, text: "To delete a dashboard use this button."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
		
		//----------------------------------
		// Explain Import Button
		cfw_tutorial_addStep({
			  selector: "#button-import"
			, clickable: false
			, text: "Oh... not to forget what to do with your exports. Here you can import them when needed."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
			
	cfw_tutorial_bundleEnd();
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_dashboardlist_tutorialsFavedDashboards(){
	
	//===================================================
	// Check has Tab
	if($("#tab-faveddashboards").length == 0){
		return;
	}
	
	//===================================================
	// Tutorial: Faved Dashboards
	cfw_tutorial_bundleStart($("#tab-faveddashboards"));
	
		//----------------------------------
		// Favorite Tab	
		cfw_tutorial_addStep({
				  selector: "#tab-faveddashboards"
				, clickable: false
				, text: "Here you can find all the dashboards you have marked as favorite."
				, drawDelay: 500
				, beforeDraw: function(){				
					}
			});
		
		//----------------------------------
		// Explain favorites
		
		cfw_tutorial_addStep({
			  selector: null
			, clickable: false
			, text: 'The favorites tab lists all the dashboards you have marked as favorite.'
					+'<br/>Else the functioniality is similar to the My Dashboards tab.'
					+''
			, drawDelay: 500
			, beforeDraw: function(){
				$("#tab-faveddashboards").click();
				
			}
		});
		
					
	cfw_tutorial_bundleEnd();
}
/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_dashboardlist_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		//--------------------------------
		// My Dashboard Tab
		if(CFW.hasPermission('Dashboard Creator') 
		|| CFW.hasPermission('Dashboard Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mydashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'mydashboards\'})"><i class="fas fa-user-circle mr-2"></i>My Dashboards</a></li>'
			);
		}
		
		//--------------------------------
		// Shared Dashboard Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-shareddashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'shareddashboards\'})"><i class="fas fa-share-alt mr-2"></i>Shared</a></li>');
		
		
		//--------------------------------
		// Faved Dashboard Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-faveddashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'faveddashboards\'})"><i class="fas fa-star mr-2"></i>Favorites</a></li>');
		
		//--------------------------------
		// Admin Dashboard Tab	
		if(CFW.hasPermission('Dashboard Admin')){
					list.append(
						'<li class="nav-item"><a class="nav-link" id="tab-admindashboards" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'admindashboards\'})"><i class="fas fa-tools mr-2"></i>Admin</a></li>');
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
			html, "cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	
}

/******************************************************************
 * 
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
			"cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
}

/******************************************************************
 * 
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
 * Show Public Link
 ******************************************************************/
function cfw_dashboardlist_showPublicLink(id){
	var link = CFW.http.getHostURL() + URI_DASHBOARD_VIEW_PUBLIC + "?id=" + id;
	var linkDiv = $('<div id="cfw-dashboard-publiclink">');
	
	linkDiv.append('<p>Public Link: <a target="blank" href="'+link+'">'+link+'</a></p>');
	
	CFW.ui.showModalMedium("Public Link", linkDiv);
}


/******************************************************************
 * Edit Dashboard
 ******************************************************************/
/*function cfw_dashboardlist_editDashboard(id){
	
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
			"cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "editdashboard", id: id}, detailsDiv);
	
}
*/
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardlist_showStatistics(id){
	var statisticsTab = $('#dashboardStatisticsContent');
	
	//-----------------------------
	// Create HTML Structure 
	var dashStatsChartDiv = $('#dashStatsChartDiv');
	var timepicker = $('#dashStatsTime');
	if(timepicker.length == 0){
		
		statisticsTab.append("<p>Following charts show you average values per minute.</p>");

		var timepicker = $('<input id="dashStatsTime" type="text">');
		statisticsTab.append(timepicker);
		cfw_initializeTimeframePicker("dashStatsTime", {offset: '1-d'}, function(){
			cfw_dashboardlist_showStatistics(id);
		});
		
				
		dashStatsChartDiv =$('<div id="dashStatsChartDiv">');
		statisticsTab.append(dashStatsChartDiv);
		

	}
	
	//-----------------------------
	// Clear chart
	dashStatsChartDiv.html('');
	
	
	//-----------------------------
	// Fetch Data
	;
	
	var requestParams = {
		action: 'fetch'
		, item: 'dashboardstats'
		, id: id
		, timeframe: timepicker.val()
	};
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDLIST_URL, requestParams, function(data){
				
				if(data.payload != null){
					
					let payload = data.payload;

					//---------------------------
					// Render Settings
					var dataToRender = {
						data: payload,
						titlefields: ["ENTITY"],
						rendererSettings:{
							chart: {
								charttype: "area",
								// How should the input data be handled groupbytitle|arrays 
								datamode: 'groupbytitle',
								xfield: "TIME",
								yfield: "VAL",
								type: "line",
								xtype: "time",
								ytype: "linear",
								stacked: false,
								legend: true,
								axes: true,
								ymin: 0,
								ymax: null,
								pointradius: 1,
								spangaps: true,
								padding: '2px',
								multichart: true
							}
						}
					};
													
					//--------------------------
					// Render Widget
					var renderer = CFW.render.getRenderer('chart');
					
					var renderResult = CFW.render.getRenderer('chart').render(dataToRender);	
					dashStatsChartDiv.append(renderResult);
								
				}
			}
		);
	$.ajaxSetup({async: true});
}
	
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardlist_editDashboard(id){
	

	// ##################################################
	// Create and show Modal
	// ##################################################
	var compositeDiv = $('<div id="editDashboardSettingsComposite">');
	
	// ----------------------------------
	// Create Pill Navigation
	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');

	list.append(
		'<li class="nav-item"><a class="nav-link active" id="dashboardSettingsTab" data-toggle="pill" href="#dashboardSettingsContent" role="tab" ><i class="fas fa-tools mr-2"></i>Settings</a></li>'
	  + '<li class="nav-item"><a class="nav-link" id="dashboardStatisticsTab" data-toggle="pill" href="#dashboardStatisticsContent" role="tab" onclick="cfw_dashboardlist_showStatistics('+id+')" ><i class="fas fa-chart-bar"></i>&nbsp;Statistics</a></li>'
	);
	
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="dashboardSettingsContent" role="tabpanel" aria-labelledby="dashboardSettingsTab"></div>'
			  +'<div class="tab-pane fade" id="dashboardStatisticsContent" role="tabpanel" aria-labelledby="dashboardStatisticsTab"></div>'
		+'</div>' );	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	
	CFW.ui.showModalMedium(
			CFWL('cfw_core_settings', 'Settings'), 
			compositeDiv, 
			"cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)",
			true
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	var elTargeto = compositeDiv.find('#dashboardSettingsContent');
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "editdashboard", id: id}, elTargeto );

	$('#editDashboardSettingsComposite [data-toggle="tooltip"]').tooltip();		
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
			"cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_dashboardlist_delete(id){
	
	var params = {action: "delete", item: "dashboard", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_dashboardlist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected dashboard could <b style="color: red">NOT</b> be deleted.</span>');
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
function cfw_dashboardlist_printFavedDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'faveddashboards');
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
	// Tab Desciption

	switch(type){
		case "mydashboards":		parent.append('<p>This tab shows all dashboards where you are the owner.</p>')
									break;	
									
		case "shareddashboards":	parent.append('<p>This list contains all the dashboard that are shared by others and by you.</p>')
									break;
									
		case "faveddashboards":		parent.append('<p>Here you can find all the dashboards you have faved. If you unfave a dashboard here it will vanish from the list the next time the tab or page gets refreshed.</p>')
									break;	
									
		case "admindashboards":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin area. The list contains all dashboards of all users.</b></p>')
									break;	
														
		default:					break;
	}
	
	//--------------------------------
	//  Create Button
	if(type == 'mydashboards'){
		var createButton = $('<button id="button-add-dashboard" class="btn btn-sm btn-success m-1" onclick="cfw_dashboardlist_createDashboard()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_dashboardlist_createDashboard')
					   + '</button>');
	
		parent.append(createButton);
		
		var importButton = $('<button id="button-import" class="btn btn-sm btn-success m-1" onclick="cfw_dashboardlist_importDashboard()">'
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
			showFields = ['IS_FAVED', 'NAME', 'DESCRIPTION', 'TAGS', 'IS_SHARED', 'TIME_CREATED'];
		}else if ( type == 'shareddashboards'
				|| type == 'faveddashboards'){
			showFields = ['IS_FAVED', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'admindashboards'){
			showFields = ['IS_FAVED', 'PK_ID', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS','IS_SHARED', 'TIME_CREATED'];
		}
		
		
		//======================================
		// Prepare actions
		
		var actionButtons = [ ];
		//-------------------------
		// Public Link Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(record.IS_PUBLIC){
					htmlString += '<button class="btn btn-primary btn-sm" title="Show Public Link"'
						+'onclick="cfw_dashboardlist_showPublicLink('+id+');">'
						+ '<i class="fa fa-link"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// View Button
		actionButtons.push(
			function (record, id){ 
				return '<a class="btn btn-success btn-sm" role="button" href="/app/dashboard/view?id='+id+'&title='+encodeURIComponent(record.NAME)+'" alt="View" title="View" >'
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
				|| type == 'admindashboards'
				|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_dashboardlist_editDashboard('+id+');">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
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
					var htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your dashboards.\', \'Do it!\', \'cfw_dashboardlist_duplicate('+id+');\')">'
							+ '<i class="fas fa-clone"></i>'
							+ '</button>';
					
					return htmlString;
				});
		}
		
		//-------------------------
		// Export Button
		if(type == 'mydashboards'
		|| type == 'admindashboards'
		|| type == 'faveddashboards'){

			actionButtons.push(
				function (record, id){
					if(JSDATA.userid == record.FK_ID_USER 
					|| type == 'admindashboards'){
						return '<a class="btn btn-warning btn-sm text-white" target="_blank" alt="Export" title="Export" '
							+' href="'+CFW_DASHBOARDLIST_URL+'?action=fetch&item=export&id='+id+'" download="'+record.NAME.replaceAll(' ', '_')+'_export.json">'
							+'<i class="fa fa-download"></i>'
							+ '</a>';
					}else{
						return '&nbsp;';
					}
					
				});
		}

		//-------------------------
		// Delete Button
		if(type == 'mydashboards'
		|| type == 'admindashboards'
		|| type == 'faveddashboards'){
			actionButtons.push(
				function (record, id){
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_USER 
					|| type == 'admindashboards'){
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
			
			if(type == 'admindashboards'){
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
 		
		var storeID = 'dashboards-'+type;
		
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: null,
			 	visiblefields: showFields,
			 	labels: {
			 		IS_FAVED: "Favorite",
			 		PK_ID: "ID",
			 		IS_SHARED: 'Shared'
			 	},
			 	customizers: {
					IS_FAVED: function(record, value) { 
						
						var isFaved = value;
						//Toggle Button
						var params = {action: "update", item: 'favorite', listitemid: record.PK_ID};
						var cfwToggleButton = CFW.ui.createToggleButton(CFW_DASHBOARDLIST_URL, params, isFaved, "fave");
						
						return cfwToggleButton.getButton();
			 		},
						
			 		NAME: function(record, value, rendererName) { 
			 			
			 			if(rendererName == 'table'){
								return '<a href="/app/dashboard/view?id='+record.PK_ID+'" style="color: inherit;">'+record.NAME+'</a>';
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
						storeid: 'dashboards-'+type,
						renderers: [
							{	label: 'Table',
								name: 'table',
								renderdef: {
									labels: {
		 								IS_FAVED: "&nbsp;",
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
		 								IS_FAVED: "&nbsp;",
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
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '100%');
	
	//-------------------------------------------
	// Create Tabs
	cfw_dashboardlist_createTabs();
	
	//-------------------------------------------
	// Register Tutorials
	cfw_dashboardlist_tutorialsRegister()
	

	
	var tabToDisplay = CFW.cache.retrieveValueForPage("dashboardlist-lasttab", "mydashboards");
	
	if(CFW.hasPermission('Dashboard Viewer') 
	&& !CFW.hasPermission('Dashboard Creator') 
	&& !CFW.hasPermission('Dashboard Admin')){
		tabToDisplay = "shareddashboards";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	//-------------------------------------------
	// Draw Tab
	cfw_dashboardlist_draw({tab: tabToDisplay});
	
}

function cfw_dashboardlist_draw(options){
	CFW_DASHBOARDLIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("dashboardlist-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mydashboards":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "mydashboards"}, cfw_dashboardlist_printMyDashboards);
										break;	
			case "faveddashboards":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "faveddashboards"}, cfw_dashboardlist_printFavedDashboards);
										break;	
			case "shareddashboards":	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "shareddashboards"}, cfw_dashboardlist_printSharedDashboards);
										break;
			case "admindashboards":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "admindashboards"}, cfw_dashboardlist_printAdminDashboards);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}


var CFW_DASHBOARDLIST_URL = "/app/dashboard/list"; //must be absolute to work with public dashboards too

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardcommon_showStatistics(id){
	var statisticsTab = $('#dashboardStatisticsContent');
	
	//-----------------------------
	// Create HTML Structure 
	var dashStatsChartDiv = $('#dashStatsChartDiv');
	var timepicker = $('#dashStatsTime');
	if(timepicker.length == 0){
		
		statisticsTab.append(
			  "<p>Following charts display summarized values for all users that have accessed the dashboard.</p>"
			+ "<ul>"
			+ "<li><b>Page Loads:&nbsp;</b> Total times the dashboard was loaded in the browser, including page reloads.</li>"
			+ "<li><b>Page Loads and Refreshes:&nbsp;</b> Above Page Loads, plus every time the dashboard was refreshed with the automatic refresh.</li>"
			+ "<li><b>Widget Loads Cached:&nbsp;</b> Number of times widget data has been loaded from the cache.</li>"
			+ "<li><b>Widget Loads Not Cached:&nbsp;</b> Number of times widget data has been loaded from the data source.</li>"
			+ "</ul>"
		);

		var timepicker = $('<input id="dashStatsTime" type="text">');
		statisticsTab.append(timepicker);
		cfw_initializeTimeframePicker("dashStatsTime", {offset: '1-d'}, function(){
			cfw_dashboardcommon_showStatistics(id);
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
								yfield: "SUM",
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
function cfw_dashboardcommon_createVersion(id){
	
		var params = {action: "duplicate", item: "createversion", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_dashboardcommon_showVersions(id);
			}
	});
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardcommon_switchVersion(id, versionid){
	
	if(id == versionid){
		CFW.ui.addToastWarning('Whoops! The IDs are the same, refresh the table then try to switch again.');
		return;
	}
	
		var params = {
			action: "update"
			, item: "switchversion"
			, id: id
			, versionid: versionid
		};
		
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_dashboardcommon_showVersions(id);
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_dashboardcommon_deleteVersion(originalID, id){
	
	var params = {action: "delete", item: "dashboard", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_dashboardcommon_showVersions(originalID);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected version could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}
	
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardcommon_showVersions(id){
	var versionsTab = $('#dashboardVersionsContent');
	versionsTab.html('');
	
	//-----------------------------
	// Create HTML Structure 

	versionsTab.append(
		  '<p>This is a list of all manually or automatically created version of this dashboard.</p>'
		  +'<button class="btn btn-sm btn-success" onclick="cfw_dashboardcommon_createVersion('+id+')">New Version</button>'
	);

			
	var versionsDiv =$('<div id="versionsDiv">');
	versionsTab.append(versionsDiv);
	
	//-----------------------------
	// Fetch Data
	
	var requestParams = {
		action: 'fetch'
		, item: 'dashboardversions'
		, id: id
	};
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDLIST_URL, requestParams, function(data){
				
				if(data.payload != null){
					
					let payload = data.payload;

					//======================================
					// Find current ID
					var currentVersionID; //needed to find out manually, else when switching twice you can end up with multiple current versions
					for(i in payload){
						if(payload[i].VERSION == 0){
							currentVersionID = payload[i].PK_ID;
						}
					}
					
					//======================================
					// Prepare actions
					var actionButtons = [ ];
					
					//-------------------------
					// View Button
					actionButtons.push(
						function (record, id){ 
							return '<a class="btn btn-success btn-sm" role="button" target="_blank" href="/app/dashboard/view?id='+record.PK_ID+'" alt="View" title="View" >'
							+ '<i class="fa fa-eye"></i>'
							+ '</a>';
						}
					);
					
					//-------------------------
					// Switch Button
					actionButtons.push(
						function (record, recordID){ 
							if(record.VERSION == 0){
								return '&nbsp;';
							}
							
							var version = record.VERSION;
							var versionID = record.PK_ID;
							return `<button class="btn btn-warning btn-sm" alt="Switch" title="Switch"
								onclick="
									CFW.ui.confirmExecute('Do you want to switch to the version <strong>${version}</strong>?'
										  +' Any widget tasks on the current dashboard will be disabled. You will have to re-enable them on the version you have switched too.'
										, 'Switch'
										, 'cfw_dashboardcommon_switchVersion(${currentVersionID}, ${versionID});')
								">
									<i class="fa fa-exchange-alt"></i>
								</button>`;
							
						}
					);
					
					//-------------------------
					// Delete Button
					actionButtons.push(
						function (record, id){
							
							if(record.VERSION == 0){
								return '&nbsp;';
							}
							
							var versionID = record.PK_ID;
							var name = record.NAME.replace(/\"/g,'&quot;');
							return `<button class="btn btn-danger btn-sm" alt="Switch" title="Delete"
								onclick="
									CFW.ui.confirmExecute('Do you want to delete the version <strong>${versionID}</strong> with name <strong>${name}</strong>?'
										, 'Delete'
										, 'cfw_dashboardcommon_deleteVersion(${currentVersionID}, ${versionID});')
								">
									<i class="fa fa-trash"></i>
								</button>`;
								
						});


					//---------------------------
					// Render Settings
					var dataToRender = {
						data: payload,
						titlefields: ["VERSION",  "NAME"],
						visiblefields: ["VERSION","NAME", "DESCRIPTION", "TIME_CREATED", "LAST_UPDATED"],
						actions: actionButtons,
						customizers: {
							VERSION: function(record, value) { 
								
								if(value == 0){
									return "current";
								}
								
								return value;
					 		}
					 		, TIME_CREATED: function(record, value) { 
								return cfw_format_epochToTimestamp(value);
					 		}
					 		, LAST_UPDATED: function(record, value) { 
								return cfw_format_epochToTimestamp(value);
					 		}
					 	},
						rendererSettings:{

						}
					};
													
					//--------------------------
					// Render Widget
					var renderResult = CFW.render.getRenderer('dataviewer').render(dataToRender);	
					versionsDiv.append(renderResult);
								
				}
			}
		);
	$.ajaxSetup({async: true});
}
	
	
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboardcommon_editDashboard(id, callbackJSOrFunc){
	

	// ##################################################
	// Create and show Modal
	// ##################################################
	var compositeDiv = $('<div id="editDashboardSettingsComposite">');
	
	// ----------------------------------
	// Create Pill Navigation
	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');

	list.append(
		'<li class="nav-item"><a class="nav-link active" id="dashboardSettingsTab" data-toggle="pill" href="#dashboardSettingsContent" role="tab" ><i class="fas fa-tools mr-2"></i>Settings</a></li>'
	  + '<li class="nav-item"><a class="nav-link" id="dashboardStatisticsTab" data-toggle="pill" href="#dashboardStatisticsContent" role="tab" onclick="cfw_dashboardcommon_showStatistics('+id+')" ><i class="fas fa-chart-bar"></i>&nbsp;Statistics</a></li>'
	  + '<li class="nav-item"><a class="nav-link" id="dashboardVersionsTab" data-toggle="pill" href="#dashboardVersionsContent" role="tab" onclick="cfw_dashboardcommon_showVersions('+id+')" ><i class="fas fa-code-branch"></i>&nbsp;Versions</a></li>'
	);
	
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="dashboardSettingsContent" role="tabpanel" aria-labelledby="dashboardSettingsTab"></div>'
			  +'<div class="tab-pane fade" id="dashboardStatisticsContent" role="tabpanel" aria-labelledby="dashboardStatisticsTab"></div>'
			  +'<div class="tab-pane fade" id="dashboardVersionsContent" role="tabpanel" aria-labelledby="dashboardVersionsTab"></div>'
		+'</div>' );	

	//-----------------------------------
	// Modal
	//-----------------------------------
	
	CFW.ui.showModalMedium(
			CFWL('cfw_core_settings', 'Settings'), 
			compositeDiv, 
			callbackJSOrFunc,
			true
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	var elTargeto = compositeDiv.find('#dashboardSettingsContent');
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "editdashboard", id: id}, elTargeto );

	$('#editDashboardSettingsComposite [data-toggle="tooltip"]').tooltip();		
}
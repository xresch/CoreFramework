
var CFW_DASHBOARDLIST_URL = "./list";

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
			+ "<li><b>Page Loads:&nbsp;</b> Above Page Loads, plus every time the dashboard was refreshed with the automatic refresh.</li>"
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
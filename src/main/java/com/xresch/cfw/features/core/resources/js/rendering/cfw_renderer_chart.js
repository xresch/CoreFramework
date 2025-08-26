
const CFW_RENDER_NAME_CHART = 'chart';
const CFW_AGGREGATED_CHARTS = ['radar', 'polarArea', 'pie', 'doughnut'];

// functions that take min x, max x, and chart object as input
var CFW_RENDERER_CHART_ZOOM_CALLBACKS = [];

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_registerZoomCallback(callbackFunction){ 
	CFW_RENDERER_CHART_ZOOM_CALLBACKS.push(callbackFunction);
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_defaultOnDoubleClick(renderDef, chartSettings, data){ 

	var chartDetailsDiv = $('<div class="w-100 h-100">'); 
	var isVerticalize = (chartSettings.datamode == 'datapoints');
	
	//------------------------------
	// Render Def	
	var clonedRenderDef = _.cloneDeep(renderDef);
	clonedRenderDef.rendererSettings.chart.datamode = 'datasets';
	clonedRenderDef.rendererSettings.chart.multichartcolumns = 1;
	
	// remove labels as they might be displayed wrongly
	clonedRenderDef.rendererSettings.chart.xlabel = null;
	clonedRenderDef.rendererSettings.chart.ylabel = null;
	
	//------------------------------
	// Iterate Datasets
	for(index in data.datasets){
		
		var currentDataset = data.datasets[index];
		
		//------------------------------
		// Chart 
		clonedRenderDef.data = [currentDataset];
		var renderedChart = CFW.render.getRenderer('chart').render(clonedRenderDef);		
		
		//------------------------------
		// Table
		clonedRenderDef.data = currentDataset.originalData;

		clonedRenderDef.rendererSettings.table = {
						  filterable: false
						, hover: true
						, verticalize: isVerticalize
					};
		
		var renderedTable = CFW.render.getRenderer('table').render(clonedRenderDef);		
		

		//------------------------------
		// Create Details
		
		var row =  $('<div class="row maxvh-50">'); 
		
		var chartColumn =
		 	$('<div class="col-4 maxvh-50">')
				.append(renderedChart);
				
		var tableColumn = 
			$('<div class="col-8 maxvh-50" style="overflow: scroll;">')
				.append(renderedTable);
		
		row.append(chartColumn)
		   .append(tableColumn);


		chartDetailsDiv
			.append('<h3>'+currentDataset.label+'</h3>')
			.append(row);
		
		;
	}
	
	//------------------------------
	// Show Modal
	CFW.ui.showModalLarge("Chart Details", chartDetailsDiv, null, true);
};


/****************************************************************************************
 * Chart Renderer
 * ==============
 * Renders a chart based on the given data.
 * There are various formats the input data can have, this is controlled by settings.datamode
 * 
 * datamode: 'groupbytitle'
 * ------------------------
 * This mode uses the data input format like on any other renderer.
 * One object in the array causes one data point on the graph.
 * Groups the data together int datasets by the titlefields defined in the renderer settings.
 * Define which field of the object should be used with settings.xfield and settings.yfield.
 *
 * Example: 
 * var chartData = [{ "server": "chocolateCalculator", "metric": "CPU%", "time": 1637842147, "value": 45.7}];
 * 
 * var dataToRender = {
 * 		data: chartData,
 *	    ...
 *		titlefields: ["server", "metric"], 
 *		rendererSettings:{
 *			chart: {
 *				datamode: 'groupbytitle',
 *				xfield: 'time',
 *				yfield: 'value',
 *				...
 *			}
 *	}};
 *
 * datamode: 'arrays'
 * ------------------------
 * This mode uses the data input format like on any other renderer.
 * One object in the array causes a full series on the graph
 * The fields defined with xfield and yfield do contain all values for the series as arrays.
 * The series name is defined by the title fields. 
 * 
 * Example: 
 * var chartData =  [{ "server": "", "metric": "CPU%", "times": [1637842115,1637842130,1637842145,1637842160], "values": [45.7, 56.3, 37.9, 41.3] }];
 * 
 * var dataToRender = {
 * 		data: chartData,
 *	    ...
 *		titlefields: ["server", "metric"], 
 *		rendererSettings:{
 *			chart: {
 *				datamode: 'arrays',
 *				xfield: 'times',
 *				yfield: 'values',
 *				...
 *			}
 *	}};
 *
 * * datamode: 'datapoints'
 * ------------------------
 * This mode uses the takes an array of objects, while the data is in a member "datapoints" as x/y values.
 * 
 * Example: 
 * var chartDatasets =  [
	{
		"ID": 43,
		"NAME": "Perfect Fairies Enchanted",
		"datapoints": {
			data: { "20231116": "51", "20231117": "50", "20231118": "54", "20231119": "55", "20231113": "79", "20231114": "75", "20231115": "80"}
		
		}
	},
	{
		"ID": 43,
		"NAME": "Enslaved Devils Sold",
		"datapoints": {
			data: { "20231116": "55", "20231117": "52", "20231118": "50", "20231119": "50", "20231113": "80", "20231114": "79", "20231115": "80"}
		}
	}
	...
]
 * 
 * var dataToRender = {
 * 		data: chartDatasets,
 *	    ...
 *		titlefields: ['NAME'], 
 *		rendererSettings:{
 *			chart: {
 *				datamode: 'datapoints',
 *				...
 *			}
 *	}};
 *
 *
 * datamode: 'datasets' (experimental)
 * ------------------------
 * This mode uses the datasets format of ChartJS. Can be used for rerendering charts when ondblclick-function is set.
 * One object in the array causes a full series on the graph.
 * The series name is defined by the datasets label field. 
 * xfield and yfield settings are ignored. 
 * 
 * Example: 
 * var chartDatasets =  [{ "label": "SeriesA", "data": [ {"x": 1637842115, "y": 45.7 }, {"x": 1637842130, "y": 56.3 } ]}
 *                       { "label": "SeriesB", "data": [ {"x": 1637842145, "y": 37.9 }, {"x": 1637842160, "y": 41.3 } ]}
 *                      ];
 * 
 * var dataToRender = {
 * 		data: chartDatasets,
 *	    ...
 *		titlefields: [], 
 *		rendererSettings:{
 *			chart: {
 *				datamode: 'datasets',
 *				...
 *			}
 *	}};
 *
 ****************************************************************************************/
function cfw_renderer_chart(renderDef) {
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
		// The type of the chart: 	line|steppedline|area|steppedarea|bar|scatter
		//							sparkline|sparkbar
		//							pie|doughnut|radar|polar
		//							gantt (xfield contains start time and yfield contains end time)
		//							(to be done: bubble)
		charttype: 'line',
		// How should the input data be handled groupbytitle|arrays 
		datamode: 'groupbytitle',
		// How the data should be aggregated for category charts(pie|doughnut|radar|polar), either: sum|avg|count
		aggregation: 'sum',
		// stack the bars, lines etc...
		stacked: false,
		// show or hide the legend
		showlegend: true, 
		// position of the legend
		legendpos: "auto", 
		// alignment of the legend: "start" | "center" | "end"
		legendalign: "center", 
		// show or hide the axes, useful to create sparkline like charts
		showaxes: true,
		// if true, connect lines if there is a gap in the data 
		spangaps: false,
		// make the chart responsive
		responsive: true,
		// The name of the field which contains the values for the x-axis
		xfield: null,
		// The name of the field which contains the values for the y-axis
		yfield: null,
		// The label for the x-axis
		xlabel: null,
		// The label for the y-axis
		ylabel: null,
		// The suggested minimum value for the x axis
		xmin: null,
		// The suggested minimum value for the y axis 
		ymin: 0,
		// The suggested maximum value for the y axis 
		ymax: null,
		//the type of the x axis: linear|logarithmic|category|time
		xtype: 'time',
		//the type of the y axis: linear|logarithmic|category|time
		ytype: 'linear',
		//the radius for the points shown on line and area charts
		pointradius: 0,
		//the tension of the line from 0 to 1 (default: 0)
		tension: 0,
		// the padding of the chart
		padding: '5px',
		// the minimum height of the chart(s), as percent or pixel value like 100px, 50% etc... (default: 100%)
		height: '100%',
		// the color of the x-axes grid lines
		xaxescolor: null,
		// the color of the y-axes grid lines
		yaxescolor: 'rgba(190,190,190, 0.2)',
		// the minimum unit used to display time: millisecond|second|minute|hour|day|week|month|quarter|year
		xminunit: 'millisecond',
		// the momentjs format used to parse the time, or a function(value) that returns a value that can be parsed by moment
		timeformat: null, 
		// allows to define an array of colors used for the charts
		colors: null,
		// if true show a details with the data of the series
		details: false,
		// the name of the renderer used for the details
		detailsrenderer: 'table',
		// size as number in percent of taken up area
		detailssize: 50,
		// position of the table, either one of: bottom | right | left (default: bottom)
		detailsposition: "bottom",
		// if multichart is true, each series is drawn in it's own chart.
		multichart: false,
		// define the fields the multicharts should be grouped by. Should be equal or a subset of the by-parameters 
		multichartby: [],
		// toogle if multicharts have a title
		multicharttitle: false,
		// define the number of columns for multicharts
		multichartcolumns: 1,
		// function to execute on double click, will receive an object with the chart settings and chart data: function(chartSettings, chartData)
		ondblclick: cfw_renderer_chart_defaultOnDoubleClick,
		// toggle zooming of charts
		zoom: true,
		// toggle refreshing of dashboards, queries etc...
		zoomrefresh: false,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.chart);
	
	//========================================
	// Colors
	if(settings.xaxescolor == null){
		settings.xaxescolor = Chart.defaults.color.replace('1.0)', '0.1)');
	}
	
	if(settings.yaxescolor == null){
		settings.yaxescolor = Chart.defaults.color.replace('1.0)', '0.2)');
	}
	
	//========================================
	// Make yfield an array to support multiple fields
	if( ! Array.isArray(settings.yfield) ){
		if(settings.yfield != null){
			settings.yfield = [settings.yfield]
		}else{
			settings.yfield = [];
		}
	};
	
	//========================================
	// Make multichartby an array to support multiple fields
	if( ! Array.isArray(settings.multichartby) ){
		if(settings.multichartby != null){
			settings.multichartby = [settings.multichartby]
		}else{
			settings.multichartby = [];
		}
	};
	
	//========================================
	// Initialize
	settings.doFill = false;
	settings.steppedValue = false;
	settings.indexAxis = 'x';
	settings.isLabelBased = false;
	settings.tooltipmode = 'index';
	
	switch(settings.charttype.toLowerCase()){
		case 'sparkarea':
			settings.showaxes = false;
			settings.showlegend = false;
			settings.padding = "2px";
			//fallthrough to case "area"
		case 'area':
			settings.charttype = 'line';
			settings.doFill = true;
			break;
		case 'bar':
			settings.doFill = true;
			break;
		case 'gantt':
			//settings.charttype = 'bar';
			settings.xtype = 'time';
			settings.ytype = 'category';
			settings.indexAxis = 'y';
			settings.doFill = true;
			settings.isLabelBased = true;
			//settings.multichart = false;
			settings.tooltipmode = 'nearest';
			settings.showlegend = false;
			settings.ondblclick = null;
			break;
		case 'sparkline':
			settings.charttype = 'line';
			settings.showaxes = false;
			settings.showlegend = false;
			settings.padding = "2px";
			settings.doFill = false;
			break;
		case 'sparkbar':
			settings.charttype = 'bar';
			settings.showaxes = false;
			settings.showlegend = false;
			settings.padding = "2px";
			break;
		case 'steppedline':
			settings.charttype = 'line';
			settings.steppedValue = 'middle';
			break;
		case 'steppedarea':
			settings.charttype = 'line';
			settings.doFill = true;
			settings.steppedValue = 'middle';
			break;
		case 'scatter':
			if(settings.pointradius == 0){
				settings.pointradius = 2;
			}
			break;

		case 'polar':
			settings.charttype = 'polarArea';
			break;
	}
	
	settings.isCategoryChart = CFW_AGGREGATED_CHARTS.includes(settings.charttype);
	
	if(settings.isCategoryChart){
		
		settings.showaxes = false;
		settings.zoom = false;
		settings.tooltipmode = 'nearest';
		
		if(settings.legendpos == "auto"){
			settings.legendpos = "right";
		}
	}
	
	if(settings.legendpos == "auto"){
		settings.legendpos = "bottom";
	}
	
	//========================================
	// Fix Multichart Endless Size Bug
	if(settings.multichart == true 
	&& settings.height.endsWith('%')
	&& !settings.isCategoryChart ){
		settings.height = "200px";
	}
		
	//========================================
	// Recalculate Size for Table
	if(settings.detailssize > 100 || settings.detailssize < 0){
		settings.detailssize = 50; 
	}
	
	//========================================
	// Create Workspace
	// ChartJS needs a DOM element to use
	// getComputedStyle.
	var workspace = CFW.ui.getWorkspace();

	//========================================
	// Create titleFormat

	if(renderDef.titleformat == null && renderDef.data.length > 0){
		renderDef.titleformat = '';
		let firstRecord = renderDef.data[0];

		if(renderDef.titlefields != null){
			for(let index in renderDef.titlefields){
				let titlefield = renderDef.titlefields[index];
				let label = renderDef.getLabel(titlefield, CFW_RENDER_NAME_CHART);
				renderDef.titleformat += label+'="{'+index+'}" / ';
				index++;
			}
		}else{
			let index = 0;
			renderDef.titleformat += '';
			/*for(let key in firstRecord){
				
				if(key == settings.xfield 
				|| settings.yfield.includes(key)
				) { continue; }
				
				let label = renderDef.getLabel(key, CFW_RENDER_NAME_CHART);
				renderDef.titleformat += label+'="{'+index+'}" / ';
				index++;
			}*/
		}
		renderDef.titleformat = renderDef.titleformat.substr(0, renderDef.titleformat.length-3);
		
	}

	//========================================
	// Create Datasets
	var datasets;
	if(settings.charttype == 'gantt'){
		 datasets = cfw_renderer_chart_createDatasetsForGantt(renderDef, settings);
	}else{
		switch(settings.datamode){
		
			case 'groupbytitle':
				 datasets = cfw_renderer_chart_createDatasetsGroupedByTitleFields(renderDef, settings);
				 break;
				 
			case 'arrays':
				 datasets = cfw_renderer_chart_createDatasetsFromArrays(renderDef, settings);
				 break;
			
			case 'datapoints':
				 datasets = cfw_renderer_chart_createDatasetsFromDatapoints(renderDef, settings);
				 break;
				 
			case 'datasets':
				 datasets = cfw_renderer_chart_prepareDatasets(renderDef, settings);
				 break;	 
		}
	}

	
	//========================================
	// sort by x to avoid displaying issues
	if(!settings.isCategoryChart && !settings.isLabelBased){
		for(i in datasets){
			datasets[i].data = _.sortBy(datasets[i].data, ['x']);
		}
	}
	
	
	//=======================================================
	// Create ChartJS Data Object
	//=======================================================
	let dataArray = [];
	let data;

	if(settings.isCategoryChart){
		
		//====================================================
		// Category Charts
		// Put everything into a single data set
		data = { labels: [], datasets: []};
		dataArray.push(data);
		
		var i = 0;
		for(var label in datasets){
			let current = datasets[label];
			var bgColor = current.backgroundColor;
			var borderColor = current.borderColor;
			var value;
			switch(settings.aggregation){
				case "sum":		value = current.cfwSum; break;
				case "avg":		value = current.cfwSum / current.cfwCount; break;
				case "count":	value = current.cfwCount; break;
				default:		value = current.cfwSum; break;
			}
			
			data.labels.push(label);
			// only add the first dataset, then the values of the other dataset are added to the first one
			if(data.datasets.length == 0){
				data.datasets.push(current);
				data.datasets[0].data = [];
				data.datasets[0].label = settings.aggregation;
				data.datasets[0].fill = true;
				data.datasets[0].backgroundColor = [];
				data.datasets[0].borderColor = [];
				data.datasets[0].hoverOffset = 5;
			}
			
			data.datasets[0].data.push(value);
			data.datasets[0].backgroundColor.push(bgColor);
			data.datasets[0].borderColor.push(bgColor);
			i++;
		}
		
	}else if (settings.charttype == 'gantt'){

		//====================================================
		// Gantt Charts
		settings.charttype = 'bar';

		if(!settings.multichart){
			//--------------------------------
		    // Single or Multichart
			data = {labels: [], datasets: []};
			dataArray.push(data);
			
			var colorsArray = [];
			
			for(label in datasets){
				
				let current = datasets[label];
				var currentData = current.data;
				
				data.labels.push(label);
				
				for(var i in currentData){
					if(data.datasets[i] == null){
						var newSet = cfw_renderer_chart_createDatasetObject(settings, "", i); 
						colorsArray.push(newSet.borderColor);
						newSet.borderColor = colorsArray;
						newSet.backgroundColor = colorsArray;
						data.datasets.push(newSet);
					}
					data.datasets[i].data.push(currentData[i]);
				}
	
			}
		}else{
			for(label in datasets){
				let current = datasets[label];
				var currentData = current.data;
				data = {
					labels: []
					, datasets: [current] 
				};
				for(var i in currentData){
					data.labels.push(label);
				}
				dataArray.push(data);
			}
				
		}

	}else{
		
		//====================================================
		// Regular Charts
		dataGroups = {};
				
		isFirst = true;
		for(label in datasets){
			
			let currentData = datasets[label];
			if(!settings.multichart){
				if(dataGroups["allSeries"] == undefined){ 
					dataGroups["allSeries"] = {datasets: []};
					dataArray.push(dataGroups["allSeries"]);
				}
				dataGroups["allSeries"].datasets.push(currentData);
			}else{
				// Show every dataset in it's own chart
				let originalData = currentData.originalData[0];
				let byFields = settings.multichartby;

				//-----------------------
				// Create GroupID
				let groupID = "";
				for(let i in byFields){
					let fieldname = byFields[i];
					groupID += originalData[fieldname];
					
				}
				
				if(groupID == ""){ groupID = label; } // if there is no group, make each series it's own group
				
				//-----------------------
				// Create Group
				
				if(dataGroups[groupID] == undefined){ 
					dataGroups[groupID] = {datasets: []};
					dataArray.push(dataGroups[groupID]);
				}
				
				let currentGroup = dataGroups[groupID];
				
				//-----------------------
				// Re-Color
				if(byFields.length > 0){
					
					let colorIndex = currentGroup.datasets.length;
					let newColors = cfw_renderer_chart_createDatasetColor(settings, colorIndex);
					
					currentData.backgroundColor = newColors.bg;
					currentData.borderColor = newColors.border;
				}
				
				//-----------------------
				// Add to Group
				currentGroup.datasets.push(currentData);
			}
		}
	}
	
	//========================================
	// Create Options
	var chartOptions = cfw_renderer_chart_createChartOptions(settings);

	//========================================
	// Create Chart Wrapper
	var allChartsDiv = $('<div class="cfw-chartjs-wrapper d-flex flex-row flex-grow-1 flex-wrap h-100 w-100">');
	allChartsDiv.attr('id',  'cfw-chart-'+CFW.utils.randomString(8));
	allChartsDiv.css("max-height", "100%");
	
	if(CFW.global.chartWrapperDivs == null){
		CFW.global.chartWrapperDivs = [];
	}
	//CFW.global.chartWrapperDivs.push(allChartsDiv);
	
	workspace.css("display", "block");
	workspace.append(allChartsDiv);
	
	//========================================
	// Create Chart(s)
	for(var index in dataArray){

		//--------------------------------
		// Initialize
		var currentData = dataArray[index];
		var chartCanvas = $('<canvas class="chartJSCanvas" height="100% !important" width="100%">');
		chartCanvas.attr('id',  'cfw-chart-'+CFW.utils.randomString(8));
		
		var columnCount = settings.multichartcolumns;
		if(!settings.multichart){ columnCount = 1; };
		var chartPlusDetailsWrapper = $('<div class="d-flex mh-100" style="width:'+(100/columnCount)+'%">');
		chartPlusDetailsWrapper.css("height", settings.height);
		chartPlusDetailsWrapper.css("padding", settings.padding);
		
		var chartWrapper = $('<div class="w-100">');
		chartWrapper.css("position", "relative");
		chartWrapper.css('height', "100%");
		chartWrapper.css("max-height", "100vh"); // prevent infinite Height loop
		chartWrapper.css("max-width", "100vw"); // prevent chart bigger then screen
		if(settings.ondblclick != null){
			chartWrapper.addClass("cursor-pointer");
			let clearlyReferencedData = currentData; // needed or else always the last in dataArray is shown on double click
			chartWrapper.on("dblclick", function(){ settings.ondblclick(renderDef, settings, clearlyReferencedData) });
		}
		
		if(settings.height != null){
			chartWrapper.css('height', settings.height +" !important");
		}
		chartWrapper.append(chartCanvas);
		chartPlusDetailsWrapper.append(chartWrapper);
		allChartsDiv.append(chartPlusDetailsWrapper);
		
		//--------------------------------
		// Set Title
		var chartOptionsClone = _.cloneDeep(chartOptions);
		if(settings.multichart && settings.multicharttitle){
			chartOptionsClone.plugins.title.display =  true;
			chartOptionsClone.plugins.title.text = currentData.datasets[0].label;
		}
		
		//--------------------------------
		// Add Table
		if(settings.details == true){
			var isSingleChart = (dataArray.length == 1);
			cfw_renderer_chart_addDetails(
					  renderDef
					, settings
					, currentData
					, chartPlusDetailsWrapper
					, chartWrapper
					, isSingleChart
					);	
		}
		
		//--------------------------------
		// Draw Chart
		var chartCtx = chartCanvas.get(0).getContext("2d");
		//var chartCtx = chartCanvas.get(0);
				
		new Chart(chartCtx, {
		    type: settings.charttype,
		    data: currentData,
		    options: chartOptionsClone
		});
		
	}
	
	return allChartsDiv;
}

/*window.addEventListener('beforeprint', () => {
  for (let id in Chart.instances) {
        Chart.instances[id].resize();
    }
});*/

CFW.render.registerRenderer(CFW_RENDER_NAME_CHART, new CFWRenderer(cfw_renderer_chart) );


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_setGlobals() {
	
	Chart.defaults.responsive = true; 
	Chart.defaults.maintainAspectRatio = false;
	Chart.defaults.color = "rgba(190,190,190, 1.0)";

	Chart.defaults.plugins.legend.display = false;
	Chart.defaults.plugins.legend.position =  'bottom';
	Chart.defaults.plugins.legend.labels.boxWidth = 16;

	Chart.defaults.animation.duration = 0;
		
	//Chart.defaults.datasets.line.showLine = false;
		
	Chart.defaults.layout = {
		// padding will be done by wrapper
        padding: {
            left: 0,
            right: 0,
            top: 0,
            bottom: 0
        }
    }
}

cfw_renderer_chart_setGlobals();

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createChartOptions(settings) {
	
	//========================================
	// Get Final Y Min Max
	var yminFinal = null;
	var ymaxFinal = null;
	if(settings.ymin != null){ yminFinal = settings.ymin; }
	if(settings.ymax != null){ ymaxFinal = settings.ymax; }
	
	//========================================
	// Return Options
	var chartOptions = {
	    	responsive: settings.responsive,
	    	maintainAspectRatio: false,
	    	resizeDelay: 300,
	    	indexAxis: settings.indexAxis, 
			scales: {
				x: {
					display: settings.showaxes,
					type: settings.xtype,
					distribution: 'linear',
					offset: true,
					stacked: settings.stacked,
					min: settings.xmin,
					time:{
						minUnit: settings.xminunit,
						parser:  settings.timeformat
					},
					grid: {
						display: true,
						color: settings.xaxescolor
					},
					border: {
						display: false,
						color: settings.xaxescolor
					},
					title: {
						display: (!CFW.utils.isNullOrEmpty(settings.xlabel)),
						text: settings.xlabel,
					},
					ticks: {
						min: 0,
						major: {
							enabled: true,
							//fontStyle: 'bold'
						},
						//source: 'data', // this corrupts horizontal charts
						autoSkip: true,
						autoSkipPadding: 15,
						//maxRotation: 0,
						//sampleSize: 1000
					},
					
				},
				y: { 
					display: settings.showaxes,
					stacked: settings.stacked,
					type: settings.ytype,
					suggestedMin: yminFinal,
					suggestedMax: ymaxFinal,
					grid: {
						display: true,
						color: settings.yaxescolor
					},
					border: {
						display: false,
						color: settings.yaxescolor
					},
					title: {
						display: (!CFW.utils.isNullOrEmpty(settings.ylabel)),
						text: settings.ylabel,
					},
					ticks: {
						min: 0,
						//source: 'data',
						autoSkip: true,
						autoSkipPadding: 15,
						//sampleSize: 1000,
						major: {
							enabled: true,
							//fontStyle: 'bold'
						}
					},
				}
			},
			
			elements: {
                point:{
                    radius: settings.pointradius
                }
            },            
			plugins:  {
				legend: {
		    		display: settings.showlegend,
		    		position: settings.legendpos,
		    		align: settings.legendalign,
		    	},
		    	// doesn't work, seems not to be part of chartjs but of QuickChart
	    	    //tickFormat: { notation: 'compact' },
		    	title: {}, // placeholder
				tooltip: {
					intersect: false,
					enabled: false,
					mode: settings.tooltipmode,
					external: cfw_renderer_chart_customTooltip,
				},
				
				zoom: {
					zoom: {
						drag: {
							enabled: settings.zoom
						},
						mode: 'x',
						onZoomComplete({ chart }) {
							const xScale = chart.scales.x;
							const start = xScale.min;
							const end = xScale.max;

							if(settings.zoomrefresh){
								for (let i in CFW_RENDERER_CHART_ZOOM_CALLBACKS) {
									let callbackFunction = CFW_RENDERER_CHART_ZOOM_CALLBACKS[i];
									callbackFunction(start, end, chart);
								}
							}
						}
					}
				}
			}
	    };
	    
	//----------------------------------
	// Custom Tick Format
	// this would corrupts labels for horizontal charts (indexAxis=y) like ganttCharts
	if(settings.indexAxis != 'y'){
		chartOptions.scales.y.ticks.callback =
				function(value, index, values) {
					return CFW.format.numbersInThousands(value, 1, false, false);
				};
	}
	
	//----------------------------------
	// for radial charts
	if(settings.charttype == 'radar'
	|| settings.charttype == 'polarArea'){
		chartOptions.scales.r = { 
			suggestedMin: yminFinal,
			suggestedMax: ymaxFinal,
			grid: { color: settings.xaxescolor },
			angleLines: { color: settings.yaxescolor },
	        ticks: {
	        	beginAtZero: true,
	        	showLabelBackdrop: false
	        }
	      };
	}
	
	//----------------------------------
	// workaround to avoid TypeError for tick generation
	if(settings.charttype == 'pie'
	|| settings.charttype == 'doughnut'){
		delete chartOptions.scales;
	}

	
	
	return chartOptions;
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_addDetails(renderDef, settings, currentData, chartPlusDetailsWrapper, chartWrapper, isSingleChart) {
	
	var cloneRenderDef = {
			rendererSettings: {
				table: { filterable: false, responsive: false, hover: true, striped: true, narrow: true }
				
			}
		};
	
	cloneRenderDef = Object.assign({}, renderDef, cloneRenderDef);

	if(settings.multichart == true || settings.datamode == 'datasets'){
		cloneRenderDef.data = currentData.datasets[0].originalData;
	}
	
	var detailsDiv = CFW.render.getRenderer(settings.detailsrenderer).render(cloneRenderDef);
	if(! ['statusbar', 'statusbarreverse', 'statusmap'].includes(settings.detailsrenderer)){
	detailsDiv.css("overflow", "auto");
	}

	if(settings.detailsposition == "bottom"){
		//--------------------------------
		// Table Bottom
		var heightValue = settings.height;
		if(heightValue.endsWith('%')){
			heightValue = "200px";
		}
		chartPlusDetailsWrapper.addClass("flex-column");

		heightValue = heightValue.replace("px", "");
		var percentMultiplier = (100-settings.detailssize)/100;
		if(isSingleChart){
			//------------------------------------
			// Use Percentages for single chart
			chartPlusDetailsWrapper.css("height", "100%");
			chartWrapper.css("height", percentMultiplier*100+"%");
		}else{
			var chartHeight = (heightValue * percentMultiplier) +"px";
			detailsDiv.addClass("flex-grow-1 flex-shrink-1");
			chartWrapper.css("height", chartHeight);
		}
		
		chartWrapper.css("flex-shrink", "0");
		chartPlusDetailsWrapper.append(detailsDiv);
	}else{
		//--------------------------------
		// Table Left or Right
		detailsDiv.removeClass("w-100 flex-grow-1");
		detailsDiv.css("height", "100%");
		detailsDiv.css("width", settings.detailssize+"%");
		
		chartWrapper.css("width", (100-settings.detailssize)+"%");
		chartWrapper.removeClass("w-100");
		
		if(settings.detailsposition == "right"){
			chartPlusDetailsWrapper.append(detailsDiv);
		}else{
			chartPlusDetailsWrapper.prepend(detailsDiv);
		}
	}
}

/******************************************************************
 * 
 ******************************************************************/

function cfw_renderer_chart_customTooltip(context) {
    
	//---------------------------------
	// Tooltip Element
    var $tooltip = $('#chartjs-tooltip');
	var tooltipModel = context.tooltip;

	//---------------------------------
    // Create element on first render
    if ($tooltip.length === 0) {
        $tooltip = $('<div id="chartjs-tooltip" class="bg-cfw-black"><table></table></div>');
        $('body').append($tooltip);
    }
    
    //---------------------------------
    // Hide if no tooltip
    if (tooltipModel.opacity === 0) {
        $tooltip.css('opacity', 0);
        return;
    }

    //---------------------------------
    // Set caret Position
    $tooltip.removeClass('above below no-transform');
    if (tooltipModel.yAlign) {
        $tooltip.addClass(tooltipModel.yAlign);
    } else {
        $tooltip.addClass('no-transform');
    }

    function getBody(bodyItem) {
        return bodyItem.lines;
    }
    
    //==============================================
    // Set Text
    if (tooltipModel.body) {
        


    	var titleLines = tooltipModel.title || [];
        var bodyLines = tooltipModel.body.map(getBody);

        var innerHtml = '<thead>';

        //---------------------------------
        // Title Lines
        titleLines.forEach(function(title) {
            innerHtml += '<tr><th>' + title + '</th></tr>';
        });
        innerHtml += '</thead><tbody>';

        //---------------------------------
        // Body Lines
        bodyLines.forEach(function(body, i) {
            let colors = tooltipModel.labelColors[i];
            let label = tooltipModel.dataPoints[i].dataset.label;
    		
    		//-----------------------------
    		// The ultimate Value handling
    		let value = tooltipModel.dataPoints[i].parsed.y;
    		if(value == undefined){ // needed e.g. for Pie Charts
    			
    			if(tooltipModel.dataPoints[i].parsed.r == undefined){
					value = tooltipModel.dataPoints[i].parsed;
				}else{
					value = tooltipModel.dataPoints[i].parsed.r;
				}
			}

    		//-----------------------------
    		// Create ze Color Box
            var colorBox = '<div class="cfw-color-box" '
            			  +'style=" background:' + colors.backgroundColor
            					+'; border-color:' + colors.borderColor
            				   + '; border-width: 2px;">&nbsp;</div>';
            
            //---------------------------------
            // Handle Special Cases
            finalBody = label + ":" + CFW.format.numbersInThousands(value, 1, false, false);
            if( Array.isArray(body)
            && body.length > 0){
            	
            	var first = body[0];
            	if(first.startsWith('[') && first.endsWith(']')){
            		
            		var array = JSON.parse(first);
            		if(array.length == 2
            		&& !isNaN(array[0]) 
            		&& !isNaN(array[1]) ){
            			// assume its Gantt charts from/to epoch time
            			finalBody = 
            					""+CFW.format.epochToTimestamp(array[0])
            					+" to <br>"+CFW.format.epochToTimestamp(array[1]);
            		}
            	}
            	
            }
            
            //---------------------------------
            // Add Body
            innerHtml += '<tr><td>' + colorBox + finalBody + '</td></tr>';
        });
        innerHtml += '</tbody>';

        var tableRoot = $tooltip.find('table');
        tableRoot.html(innerHtml);
    }

    var position = context.chart.canvas.getBoundingClientRect();
	const bodyFont = Chart.helpers.toFont(tooltipModel.options.bodyFont);

    //==============================================
    // Display, position, and set styles for font
    $tooltip.css('opacity', 1);
    $tooltip.css('position', 'absolute');
    $tooltip.css('left', position.left + window.pageXOffset + tooltipModel.caretX + 'px');
    $tooltip.css('top', position.top + window.pageYOffset + tooltipModel.caretY + 'px');
    $tooltip.css('font', bodyFont.string);
    //$tooltip.css('padding', tooltipModel.padding + 'px ' + tooltipModel.padding + 'px');
    $tooltip.css('padding', '5px');
    $tooltip.css('pointerEvents', 'none');
    $tooltip.css('z-index', 2048);
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetColor(settings, index) {
	
	let colors = settings.colors;
	//-----------------------------
	// Default
	if(colors == null){
		let hue = 195; 
		hue += index * 31;
		
		let borderColor = CFW.colors.randomSL(hue,65,100,55,70);
		let bgColor = borderColor.replace('1.0)', '0.65)');
	
		return { border: borderColor , bg: bgColor };
		
	}else if(Array.isArray(colors)){
		
		let color = colors[ index % colors.length ];
		
		let borderColor = CFW.colors.colorToRGBA(color, 1.0);
		borderColor = borderColor.replace('1)', '1.0)');
		let bgColor = borderColor.replace('1.0)', '0.80)');
		
		return { border: borderColor , bg: bgColor };
		
	}
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetObject(settings, label, index) {
	
	var colors = cfw_renderer_chart_createDatasetColor(settings, index);

	return {
			label: label, 
			data: [], //data used by chartjs
			originalData: [], // original data used for table
			backgroundColor: colors.bg,
			fill: settings.doFill,
            borderColor: colors.border,
            borderWidth: 1,
            spanGaps: settings.spangaps,
            stepped: settings.steppedValue,
            tension: settings.tension,
            cfwSum: 0,
            cfwCount: 0
		};
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsForGantt(renderDef, settings) {
	
	let datasets = {};

	let timelineStart = null; 
	
	//----------------------------
	// For Every yfield
	for(let k = 0; k < settings.yfield.length; k++){
		
		let yfieldname = settings.yfield[k];
		
		for(var i = 0; i < renderDef.data.length; i++){
			let currentRecord = renderDef.data[i];
			let yValue = currentRecord[yfieldname];
			
			//----------------------------
			// Create Label & Dataset
			let label = renderDef.getTitleString(currentRecord);
			if(settings.yfield.length > 1){
				label += " / " +yfieldname;
			}
			if(datasets[label] == undefined){
				datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, i);
			}
			
			let currentSet = datasets[label];
			
			//----------------------------
			// Add Values
			currentSet.originalData.push(currentRecord);
			
			let xValue = currentRecord[settings.xfield];
			
			currentSet.data.push([ 
				CFW.format.epochToTimestamp(xValue)
				, CFW.format.epochToTimestamp(yValue) 
			]) ;
			
			if(timelineStart > xValue
					|| timelineStart == null){
						timelineStart = xValue;
					}
			
		}
	}
	
	settings.xmin = CFW.format.epochToTimestamp(timelineStart);
	
	return datasets;
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsGroupedByTitleFields(renderDef, settings) {
	
	var datasets = {};
	
	//----------------------------
	// For Every yfield
	var colorCounter = 0;
	for(let k = 0; k < settings.yfield.length; k++){
		
		let yfieldname = settings.yfield[k];
		
		
		for(let i = 0; i < renderDef.data.length; i++){
			var currentRecord = renderDef.data[i];
			
			//----------------------------
			// Create Label & Dataset
			var label = renderDef.getTitleString(currentRecord);
			
			if( ! CFW.utils.isNullOrEmpty(label) ){
				label += " / " + yfieldname;
			}else{
				label = yfieldname;
			}
			
			if(datasets[label] == undefined){
				datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, colorCounter++);
			}
			
			//----------------------------
			// Add Values
			var value = currentRecord[yfieldname];
			datasets[label].originalData.push(currentRecord);
			
			if(settings.xfield == null){
				datasets[label].data.push(value);
				datasets[label].cfwSum += isNaN(value) ? 0 : parseFloat(value);
				datasets[label].cfwCount += 1;
			}else{
				
				if(currentRecord[settings.xfield] != null){
					datasets[label].data.push({
						x: currentRecord[settings.xfield], 
						y: value
					});
				}
				datasets[label].cfwSum += isNaN(value) ? 0 : parseFloat(value);
				datasets[label].cfwCount += 1;
			}
		}
	}
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsFromDatapoints(renderDef, settings) {
	
	var datasets = {};
	var colorCounter = 0;
	
	//----------------------------
	// For Every yfield
	for(let k = 0; k < settings.yfield.length; k++){
		
		let yfieldname = settings.yfield[k];
		for(var i = 0; i < renderDef.data.length; i++){
	
			var currentRecord = renderDef.data[i];
			
			//----------------------------
			// Create Label & Dataset
			var label = renderDef.getTitleString(currentRecord) + " / "+yfieldname;
			if(datasets[label] == undefined){
				datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, colorCounter++);
			}
			
			//----------------------------
			// Add Values
			//var value = currentRecord[yfieldname];
			var datapoints = currentRecord['datapoints'];
			datasets[label].originalData.push(currentRecord);
	
			for(x in datapoints.data){
				var y = datapoints.data[x]
	
				datasets[label].data.push({
					x: x, 
					y: y
				});
				
				datasets[label].cfwSum += isNaN(y) ? 0 : parseFloat(y);
				datasets[label].cfwCount += 1;
			}
				
		}
	}
	
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsFromArrays(renderDef, settings) {
	
	var datasets = {};
	var colorCounter = 0;
	
	//----------------------------
	// For Every yfield
	for(let k = 0; k < settings.yfield.length; k++){
		
		let yfieldname = settings.yfield[k];
		
		for(var i = 0; i < renderDef.data.length; i++){
			var currentRecord = renderDef.data[i];
			
			//----------------------------
			// Create Label & Dataset
			var label = renderDef.getTitleString(currentRecord)+" / "+yfieldname;
			if(datasets[label] == undefined){
				datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, colorCounter++);
			}
			
			var yArray = currentRecord[yfieldname];
			datasets[label].originalData.push(currentRecord);
			
			if(settings.xfield == null){
				datasets[label].data = yArray;
			}else{
				var xArray = currentRecord[settings.xfield];
				
				for(let i = 0; i < xArray.length; i++){
					if(xArray[i] != null){
						datasets[label].data.push({
							x: xArray[i], 
							y: yArray[i]
						});
					}
				}
			}
		}
	}
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_prepareDatasets(renderDef, settings) {
	
	var datasets = renderDef.data;

	for(var i = 0; i < renderDef.data.length; i++){
		var currentDataset = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset

		var colors = cfw_renderer_chart_createDatasetColor(settings, i);
		
		if( CFW.utils.isNullOrEmpty(currentDataset.label) ){			
			currentDataset.label = renderDef.getTitleString(currentRecord); 
		}
		
		if(currentDataset.backgroundColor == null){
			currentDataset.backgroundColor = colors.bg; 
		}
		
		if(currentDataset.borderColor == null){
			currentDataset.borderColor = colors.border; 
		}
		
		if(currentDataset.borderWidth == null){
			currentDataset.borderWidth = 1;
		}
		
		if(currentDataset.originalData == null){
			currentDataset.originalData = currentDataset; // data for data table
		}
		
		currentDataset.spanGaps = settings.spangaps;
		currentDataset.steppedLine = settings.steppedValue;
		currentDataset.tension = settings.tension;
	}
	/*	currentDataset = {
				label: label, 
				data: [], 
				backgroundColor: bgColor,
				fill: settings.doFill,
	            borderColor: borderColor,
	            borderWidth: 1,
	            spanGaps: settings.spangaps,
	            steppedLine: settings.steppedValue,
	            tension: 0,
	            cfwSum: 0,
	            cfwCount: 0
			};*/			
	return datasets;
}

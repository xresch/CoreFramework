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
		// the padding of the chart
		padding: '10px',
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
		// toogle if multicharts have a title
		multicharttitle: false,
		// toogle if multicharts have a title
		multichartcolumns: 1,
		// function to execute on double click, will receive an object with the chart settings and chart data: function(chartSettings, chartData)
		ondblclick: cfw_renderer_chart_defaultOnDoubleClick,
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
	// Initialize
	settings.doFill = false;
	settings.isSteppedline = false;

	switch(settings.charttype){
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
			settings.isSteppedline = true;
			break;
		case 'steppedarea':
			settings.charttype = 'line';
			settings.doFill = true;
			settings.isSteppedline = true;
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
	
	settings.isCategoryChart = ['radar', 'polarArea', 'pie', 'doughnut'].includes(settings.charttype);
	
	if(settings.isCategoryChart){
		settings.showaxes = false;
	}
	//========================================
	// Fix Multichart Endless Size Bug
	if(settings.multichart == true 
	&& settings.height.endsWith('%') ){
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
			for(let key in firstRecord){
				if(key == settings.xfield || key == settings.yfield) { continue; }
				let label = renderDef.getLabel(key, CFW_RENDER_NAME_CHART);
				renderDef.titleformat += label+'="{'+index+'}" / ';
				index++;
			}
		}
		renderDef.titleformat = renderDef.titleformat.substr(0, renderDef.titleformat.length-3);
		
	}

	//========================================
	// Create Datasets
	var datasets;
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

	
	
	
	//========================================
	// sort by x to avoid displaying issues
	if(!settings.isCategoryChart){
		for(i in datasets){
			datasets[i].data = _.sortBy(datasets[i].data, ['x']);
		}
	}
	
	//========================================
	// Create ChartJS Data Object
	var dataArray = [];
	var data;

	if(!settings.isCategoryChart){
		//--------------------------------
		// Regular Charts
		data = {datasets: []};
		dataArray.push(data);
		
		isFirst = true;
		for(label in datasets){
			if(!settings.multichart){
				// Show all datasets in a single chart
				data.datasets.push(datasets[label]);
			}else{
				// Show every dataset in it's own chart
				data.datasets.push(datasets[label]);
				if(!isFirst){
					dataArray.push(data);
				}else{
					isFirst = false;
				}
				
				data = {datasets: []};
			}
		}
	}else{
		//--------------------------------
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
		var chartPlusDetailsWrapper = $('<div class="d-flex" style="width:'+(100/columnCount)+'%">');
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
	var yminFinal = 0;
	var ymaxFinal = null;
	if(settings.ymin != null){ yminFinal = settings.ymin; }
	if(settings.ymax != null){ ymaxFinal = settings.ymax; }
	
	//========================================
	// Return Options
	var chartOptions = {
	    	responsive: settings.responsive,
	    	maintainAspectRatio: false,
	    	resizeDelay: 300,
			scales: {
				x: {
					display: settings.showaxes,
					type: settings.xtype,
					distribution: 'linear',
					offset: true,
					stacked: settings.stacked,
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
						source: 'data',
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
						source: 'data',
						autoSkip: true,
						autoSkipPadding: 15,
						//sampleSize: 1000,
						major: {
							enabled: true,
							//fontStyle: 'bold'
						},
						// Custom Tick Format
						callback : function(value, index, values) {
							
							if(!isNaN(value)){
								let sureNumber = parseFloat(value);
								if (sureNumber > 1000000000) {
									return (sureNumber / 1000000000).toFixed(2) + "G";
								} else if (sureNumber > 1000000) {
									return (sureNumber / 1000000).toFixed(2) + "M";
								} else if (sureNumber > 1000) {
									return (sureNumber / 1000).toFixed(2) + "K";
								} else{
									
									return sureNumber.toFixed(2);
								}
							}
							
							return value;
						}
					},
				}
			},
			
			elements: {
                point:{
                    radius: settings.pointradius
                },
            },
            // padding will be done by wrapper
//            layout: {
//                padding: {
//                    left: settings.padding,
//                    right: settings.padding,
//                    top: settings.padding,
//                    bottom: settings.padding
//                }
//            },
            
			plugins:  {
				legend: {
		    		display: settings.showlegend,
		    	},
		    	title: {}, // placeholder
				tooltip: {
					intersect: false,
					enabled: false,
					mode: 'index',
					external: cfw_renderer_chart_customTooltip,
					/*callbacks: {
						label: function(tooltipItem, myData) {
							var label = myData.datasets[tooltipItem.datasetIndex].label || '';
							if (label) {
								label += ': ';
							}
							label += parseFloat(tooltipItem.value).toFixed(2);
							return label;
						}
					}*/
	
				},
			}
	    };
	
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
console.log(currentData);
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
    // Tooltip Element
    var $tooltip = $('#chartjs-tooltip');
	var tooltipModel = context.tooltip;

    // Create element on first render
    if ($tooltip.length === 0) {
        $tooltip = $('<div id="chartjs-tooltip" class="bg-cfw-black"><table></table></div>');
        $('body').append($tooltip);
    }

    // Hide if no tooltip
    if (tooltipModel.opacity === 0) {
        $tooltip.css('opacity', 0);
        return;
    }

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

    // Set Text
    if (tooltipModel.body) {
        var titleLines = tooltipModel.title || [];
        var bodyLines = tooltipModel.body.map(getBody);

        var innerHtml = '<thead>';

        titleLines.forEach(function(title) {
            innerHtml += '<tr><th>' + title + '</th></tr>';
        });
        innerHtml += '</thead><tbody>';

        bodyLines.forEach(function(body, i) {
            var colors = tooltipModel.labelColors[i];

            var div = '<div class="cfw-color-box" '
            			  +'style=" background:' + colors.backgroundColor
            					+'; border-color:' + colors.borderColor
            				   + '; border-width: 2px;">&nbsp;</div>';
            
            innerHtml += '<tr><td>' + div + body + '</td></tr>';
        });
        innerHtml += '</tbody>';

        var tableRoot = $tooltip.find('table');
        tableRoot.html(innerHtml);
    }

    var position = context.chart.canvas.getBoundingClientRect();
	const bodyFont = Chart.helpers.toFont(tooltipModel.options.bodyFont);


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
function cfw_renderer_chart_createDatasetObject(settings, label, index) {
	
	hue = 195; 
	hue += index * 30;
	
	var borderColor = CFW.colors.randomSL(hue,65,100,55,70);
	var bgColor = borderColor.replace('1.0)', '0.65)');
	
	return {
			label: label, 
			data: [], //data used by chartjs
			originalData: [], // original data used for table
			backgroundColor: bgColor,
			fill: settings.doFill,
            borderColor: borderColor,
            borderWidth: 1,
            spanGaps: settings.spangaps,
            stepped: settings.isSteppedline,
            lineTension: 0,
            cfwSum: 0,
            cfwCount: 0
		};
	
}
	
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsGroupedByTitleFields(renderDef, settings) {
	
	var datasets = {};

	for(var i = 0; i < renderDef.data.length; i++){
		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		if(datasets[label] == undefined){
			datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, i);
		}
		
		//----------------------------
		// Add Values
		var value = currentRecord[settings.yfield];
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
	
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsFromDatapoints(renderDef, settings) {
	
	var datasets = {};

	for(var i = 0; i < renderDef.data.length; i++){

		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		if(datasets[label] == undefined){
			datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, i);
		}
		
		//----------------------------
		// Add Values
		var value = currentRecord[settings.yfield];
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
	
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsFromArrays(renderDef, settings) {
	
	var datasets = {};


	for(var i = 0; i < renderDef.data.length; i++){
		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		if(datasets[label] == undefined){
			datasets[label] =  cfw_renderer_chart_createDatasetObject(settings, label, i);
		}
		
		var yArray = currentRecord[settings.yfield];
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
	
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_prepareDatasets(renderDef, settings) {
	
	var datasets = renderDef.data;
	var hue = 165; 

	for(var i = 0; i < renderDef.data.length; i++){
		var currentDataset = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		hue += 31;
		var borderColor = CFW.colors.randomSL(hue,65,100,55,70);
		var bgColor = borderColor.replace('1.0)', '0.65)');
		
		if( CFW.utils.isNullOrEmpty(currentDataset.label) ){			
			currentDataset.label = renderDef.getTitleString(currentRecord); 
		}
		
		if(currentDataset.backgroundColor == null){
			currentDataset.backgroundColor = bgColor; 
		}
		
		if(currentDataset.backgroundColor == null){
			currentDataset.backgroundColor = borderColor; 
		}
		
		if(currentDataset.borderWidth == null){
			currentDataset.borderWidth = 1;
		}
		
		if(currentDataset.originalData == null){
			currentDataset.originalData = currentDataset; // data for data table
		}
		
		currentDataset.spanGaps = settings.spangaps;
		currentDataset.steppedLine = settings.isSteppedline;
		currentDataset.lineTension = 0;
	}
	/*	currentDataset = {
				label: label, 
				data: [], 
				backgroundColor: bgColor,
				fill: settings.doFill,
	            borderColor: borderColor,
	            borderWidth: 1,
	            spanGaps: settings.spangaps,
	            steppedLine: settings.isSteppedline,
	            lineTension: 0,
	            cfwSum: 0,
	            cfwCount: 0
			};*/
			
	return datasets;
}

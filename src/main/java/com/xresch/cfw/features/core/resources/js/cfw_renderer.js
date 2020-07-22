/******************************************************************
 * Execute a multi action.
 * Element needs the following JQuery.data() attributes:
 *   - checkboxSelector: JQuery selection string without ":checked"
 *   - function: the function that should be executed
 ******************************************************************/
function cfw_internal_executeMultiAction(buttonElement){
	
	var checkboxSelector = $(buttonElement).data('checkboxSelector');
	var callbackFunction = $(buttonElement).data('function');
		
	var recordContainerArray = [];
	var valuesArray = [];
	var recordsArray = [];
	
	$.each($(checkboxSelector+':checked'), function(){
		valuesArray.push( $(this).val() );
		recordsArray.push( $(this).data('record') );
		recordContainerArray.push( $(this).closest('.cfwRecordContainer').get(0) );
	});
	
	callbackFunction(recordContainerArray, recordsArray, valuesArray);
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_html(renderDefinition) {

	if( renderDefinition.data instanceof Element
	|| typeof renderDefinition.data == "string"){
		return renderDefinition.data;
	}else{
		return CFW.format.objectToHTMLList(renderDefinition.data);
	}
}
CFW.render.registerRenderer("html", new CFWRenderer(cfw_renderer_html) );

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_json(renderDef) {
	var wrapperDiv = $('<div class="flex-grow-1">');
	
	var randomID = CFW.utils.randomString(16);
	return wrapperDiv.append('<pre id="json-'+randomID+'"><code>'+JSON.stringify(renderDef.data, null, 2)+'</code></pre><script>hljs.highlightBlock($("#json-'+randomID+'").get(0));</script>');
}

CFW.render.registerRenderer("json", new CFWRenderer(cfw_renderer_json));

/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("csv",
	new CFWRenderer(
		function (renderDef) {
		}
	)
);

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_tiles(renderDef) {
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into alert tiles.</span>";
	}
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// size factor for the text in the tile, or tile size of labels are not shown
		sizefactor: 1,
		// show or hide labels
		showlabels: false, 
		// the border style of the tile, choose between: null | 'none' | 'round' | 'superround' | 'asymmetric' | 'superasymmetric' | 'ellipsis'
		borderstyle: null,
		// the border that should be applied, like '1px solid black'
		border: null,
		// show a popover with details about the data when hovering a tile
		popover: true
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.tiles);

	//===================================================
	// Create Alert Tiles
	//===================================================
	var allTiles = $('<div class="d-flex flex-row flex-grow-1 flex-wrap">');

	if(renderDef.data.length == 1){
		allTiles.addClass('flex-column h-100');
	}else{
		allTiles.addClass('flex-row ');
	}
	
	if(settings.showlabels != true && settings.showlabels != "true"){
		allTiles.addClass('align-items-start');
	}
				
	for(var i = 0; i < renderDef.data.length; i++ ){
		var currentRecord = renderDef.data[i];
		var currentTile = $('<div class="p-1">');
		
		//=====================================
		// Add padding
		if(settings.showlabels == true && settings.showlabels == "true"){
			if(settings.sizefactor <= 1.0)		{ currentTile.addClass('p-1'); }
			else if(settings.sizefactor <= 1.5)	{ currentTile.addClass('p-2'); }
			else if(settings.sizefactor <= 2.5)	{ currentTile.addClass('p-3'); }
			else 								{ currentTile.addClass('p-4'); }
		}
		//=====================================
		// Add Styles
		if(renderDef.bgstylefield != null){
			currentTile.addClass('bg-'+currentRecord[renderDef.bgstylefield]);
		}
		
		if(renderDef.textstylefield != null){
			currentTile.addClass('text-'+currentRecord[renderDef.textstylefield]);
		}
		
		if(settings.border != null){
			currentTile.css('border', settings.border);
		}
		
		if(settings.borderstyle != null){
			var baseradius = 10;
			switch(settings.borderstyle.toLowerCase()){
				case 'round':  			currentTile.css('border-radius', baseradius * settings.sizefactor+'px');
										break;
										
				case 'superround':  	currentTile.css('border-radius', baseradius * 2 * settings.sizefactor+'px');
										break;
				
				case 'asymmetric':  	var radius = baseradius * 2.4 * settings.sizefactor+'px ';
										radius += baseradius * 0.8 * settings.sizefactor+'px';
										currentTile.css('border-radius', radius);
										break;
										
				case 'superasymmetric':	var radius = baseradius * 5 * settings.sizefactor+'px ';
										radius += baseradius * 2 * settings.sizefactor+'px';
										currentTile.css('border-radius', radius);
										break;
										
				case 'ellipsis':  		currentTile.css('border-radius', '50%');
										break;						
				
			}
		}
		
		//=====================================
		// Add Details Click
		currentTile.data('record', currentRecord)
		currentTile.bind('click', function(e) {
			
			e.stopPropagation();
			//-------------------------
			// Create render definition
			var definition = Object.assign({}, renderDef);
			definition.data = $(this).data('record');
			if(definition.rendererSettings.table == null){
				definition.rendererSettings.table = {};
			}
			definition.rendererSettings.table.verticalize = true;
			definition.rendererSettings.table.striped = true;
			//remove alertstyle and textstyle
			var visiblefields = Object.keys(definition.data);
			visiblefields.pop();
			visiblefields.pop();
			
			definition.visiblefields = visiblefields;
			
			//-------------------------
			// Show Details Modal
			var renderer = CFW.render.getRenderer('table');
			cfw_showModal(
					CFWL('cfw_core_details', 'Details'), 
					renderer.render(definition))
			;
		})
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			currentTile.popover({
				trigger: 'hover',
				html: true,
				placement: 'auto',
				boundary: 'window',
				// title: 'Details',
				sanitize: false,
				content: cfw_renderer_tiles_createDetailsTable(currentRecord, renderDef)
			})
		}
		
		//=====================================
		// Create Tile
		if(settings.showlabels == true || settings.showlabels == "true" ){
			currentTile.addClass('cfw-tile');

			//-------------------------
			// Create Title
			var tileTitle = renderDef.getTitleHTML(currentRecord);

			currentTile.append('<span class="text-center" style="font-size: '+18*settings.sizefactor+'px;"><b>'+tileTitle+'</b></span>');
			//-------------------------
			// Add field Values as Cells
			for(var key in renderDef.visiblefields){
				var fieldname = renderDef.visiblefields[key];
				var value = currentRecord[fieldname];
				
				if(renderDef.customizers[fieldname] == null){
					if(value != null){
						currentTile.append('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.labels[fieldname]+':&nbsp;</strong>'+value+'</span>');
					}
				}else{
					var customizer = renderDef.customizers[fieldname];
					var customizedValue = customizer(currentRecord, value);
					if(customizedValue != null){
						var span = $('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.labels[fieldname]+':&nbsp;</strong></span>');
						span.append(customizedValue);
						currentTile.append(span);
					}
				}
			}
		} else {
			currentTile.css('margin', "0rem 0.125rem 0.25rem 0.125rem");
			currentTile.css('width', 50*settings.sizefactor+"px");
			currentTile.css('height', 50*settings.sizefactor+"px");
		}

		allTiles.append(currentTile);
	}
	
	return allTiles;

}

CFW.render.registerRenderer("tiles", new CFWRenderer(cfw_renderer_tiles));

function cfw_renderer_tiles_createDetailsTable(entry, renderDef){
	
	//-------------------------
	// Create render definition
	var definition = Object.assign({}, renderDef);
	definition.data = entry;
	
	if(definition.rendererSettings.table == null){
		definition.rendererSettings.table = {};
	}
	definition.rendererSettings.table.verticalize = true;
	definition.rendererSettings.table.narrow = true;
	definition.rendererSettings.table.filter = false;
	//remove alertstyle and textstyle
	var visiblefields = Object.keys(definition.data);
	visiblefields.pop();
	visiblefields.pop();
	
	definition.visiblefields = visiblefields;
	
	//-------------------------
	// Show Details Modal
	var renderer = CFW.render.getRenderer('table');
	var wrappedTable = renderer.render(definition);
	var sizingDiv = $('<div style="font-size: smaller;">');
	sizingDiv.append(wrappedTable);
	return sizingDiv;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_table(renderDef) {
	
	// renderDef.rendererSettings.table same as CFWTable Settings
	// plus verticalize: false
//	{
//		filterable: true,
//		responsive: true,
//		hover: true,
//		striped: true,
//		narrow: false,
//		stickyheader: false, 
//	 }
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into table.</span>";
	}
	
	var settings = renderDef.rendererSettings.table;
	if (settings == null){
		settings = {};
	}
	//-----------------------------------
	// Verticalize Single Records
	if(renderDef.data.length == 1 && settings.verticalize){
		var singleRecordData = [];
		var singleRecord = renderDef.data[0]
		
		//----------------------
		// Fields
		for(var i in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[i];
			var label = CFW.format.fieldNameToLabel(fieldname);
			var finalValue = singleRecord[fieldname];
			
			if(renderDef.customizers[fieldname] != null){
				var customizer = renderDef.customizers[fieldname];
				finalValue = customizer(currentRecord, finalValue);
			}
			singleRecordData.push({name: label, value: finalValue});
		}
		
		//-------------------------
		// Add Action buttons
		if(renderDef.actions.length > 0){
			var id = null;
			var actionButtonHTML = "";
			if(renderDef.idfield != null){
				id = singleRecord[renderDef.idfield];
			}
			var actionDiv = $('<div>');
			for(var fieldKey in renderDef.actions){
				actionDiv.append(renderDef.actions[fieldKey](singleRecord, id ));
			}
			singleRecordData.push({name: "Actions", value: actionDiv});
		}
		//-------------------------
		// Override Settings
		renderDef.data = singleRecordData;
		renderDef.customizers = {};
		renderDef.actions = [];
		renderDef.bulkActions = null;
		renderDef.visiblefields = ['name', 'value'];
		renderDef.labels = {
				name: CFWL('cfw_core_name', 'Name'), 
				value: CFWL('cfw_core_value', 'Value')
		};
						
	}
	
	//===================================================
	// Create Table
	//===================================================
	var cfwTable = new CFWTable(settings);
	
	//-----------------------------------
	// Create Headers
	var selectorGroupClass;
	if(renderDef.bulkActions != null){
		selectorGroupClass = "table-checkboxes-"+CFW.utils.randomString(16);
		var checkbox = $('<input type="checkbox" onclick="$(\'.'+selectorGroupClass+':visible\').prop(\'checked\', $(this).is(\':checked\') )" >');
		
		cfwTable.addHeader(checkbox);
	}
	
	for(var key in renderDef.visiblefields){
		var fieldname = renderDef.visiblefields[key];
		cfwTable.addHeader(renderDef.labels[fieldname]);
	}
	
	for(var key in renderDef.actions){
		cfwTable.addHeader("&nbsp;");
	}
				
	//-----------------------------------
	// Print Records
	for(var i = 0; i < renderDef.data.length; i++ ){
		var currentRecord = renderDef.data[i];
		var row = $('<tr class="cfwRecordContainer">');
		
		//-------------------------
		// Add Styles
		if(renderDef.bgstylefield != null){
			row.addClass('table-'+currentRecord[renderDef.bgstylefield]);
		}
		
		if(renderDef.textstylefield != null){
			if(currentRecord[renderDef.textstylefield] != null){
				row.addClass('text-'+currentRecord[renderDef.textstylefield]);
			}else{
				if(renderDef.bgstylefield != null && currentRecord[renderDef.bgstylefield] != null){
					row.addClass('text-dark');
				}
			}
		}
		
		//-------------------------
		// Checkboxes for selects
		var cellHTML = '';
		if(renderDef.bulkActions != null){
			
			var value = "";
			if(renderDef.idfield != null){
				value = currentRecord[renderDef.idfield];
			}
			var checkboxCell = $('<td>');
			var checkbox = $('<input class="'+selectorGroupClass+'" type="checkbox" value="'+value+'">');
			checkbox.data('idfield', renderDef.idfield);
			checkbox.data('record', currentRecord);
			checkboxCell.append(checkbox);
			row.append(checkboxCell);
		}
		
		//-------------------------
		// Add field Values as Cells
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			var value = currentRecord[fieldname];
			
			if(renderDef.customizers[fieldname] == null){
				
				if(settings.verticalize){
					//value already customized and may be a JQuery Object
					var cell = $('<td>');
					cell.append(value);
					row.append(cell);
				}else if(value != null){
					cellHTML += '<td>'+value+'</td>';
				}else{
					cellHTML += '<td>&nbsp;</td>';
				}
			}else{
				row.append(cellHTML);
				cellHTML = "";
				var customizer = renderDef.customizers[fieldname];
				var customizedValue = customizer(currentRecord, value);
				var cell = $('<td>');
				cell.append(customizedValue);
				row.append(cell);
				
			}
		}
		row.append(cellHTML);
		//-------------------------
		// Add Action buttons
		var id = null;
		if(renderDef.idfield != null){
			id = currentRecord[renderDef.idfield];
		}
		for(var fieldKey in renderDef.actions){
			var td = $('<td>');
			td.append(renderDef.actions[fieldKey](currentRecord, id ));
			row.append(td);
		}

		cfwTable.addRow(row);
	}
	
	//----------------------------------
	// Create multi buttons
	if(renderDef.bulkActions == null){
		return cfwTable.getTable();
	}else{
		var wrapperDiv = cfwTable.getTable();
		
		var actionsDivTop  = $('<div class="m-1">');
		var actionsDivBottom  = $('<div class="m-1">');
		for(var buttonLabel in renderDef.bulkActions){
			//----------------------------
			// Top 
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
				var func = renderDef.bulkActions[buttonLabel];
				var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivTop.append(button);
			}
			
			//----------------------------
			// Bottom
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
				var func = renderDef.bulkActions[buttonLabel];
				var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivBottom.append(button);
			}
		}
		
		if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
			wrapperDiv.prepend(actionsDivTop);
		}
		if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
			wrapperDiv.append(actionsDivBottom);
		}
		
		return wrapperDiv;
	}
}
CFW.render.registerRenderer("table", new CFWRenderer(cfw_renderer_table) );

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_panels (renderDef) {
				
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into table.</span>";
	}
	
	//===================================================
	// Create Table
	//===================================================
	//TODO: Wrapper Diff flex-grow-1 / w-100 h-100
	//var cfwTable = new CFWTable(renderDef.rendererSettings.table);
	var wrapper = $("<div class='flex-grow-1'>");
	
	var selectorGroupClass = "panel-checkboxes-"+CFW.utils.randomString(16);
	//-----------------------------------
	// Print Records
	for(var i = 0; i < renderDef.data.length; i++ ){
		
		//---------------------------
		// Preprarations
		var currentRecord = renderDef.data[i];
		
		 var panelSettings = {
				cardstyle: currentRecord[renderDef.bgstylefield],
				textstyle: currentRecord[renderDef.textstylefield],
				textstyleheader: null,
				title: $('<div>'),
				body: "&nbsp;",
		};
		 
		//---------------------------
		// Resolve Title				
		panelSettings.title.append(renderDef.getTitleHTML(currentRecord));	
		
		//-------------------------
		// Checkboxes for selects
		if(renderDef.bulkActions != null){
			
			var value = "";
			if(renderDef.idfield != null){
				value = currentRecord[renderDef.idfield];
			}
			
			var checkboxDiv = $('<div>');
			var checkbox = $('<input class="form-input float-left mt-1 mr-2 '+selectorGroupClass+'" type="checkbox" value="'+value+'" >');
			checkbox.data('idfield', renderDef.idfield);
			checkbox.data('record', currentRecord);
			checkboxDiv.append(checkbox);
			
			panelSettings.title.prepend(checkboxDiv);
		}
		
		//-------------------------
		// Add field Values as Unordered List
		var list = $("<ul>");
		var itemHTML = '';
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			var value = currentRecord[fieldname];
			
			if(renderDef.customizers[fieldname] == null){
				if(value != null){
					itemHTML += '<li><b>' + renderDef.labels[fieldname] + ':</b> ' + value + '</li>';
				}else{
					itemHTML += '&nbsp;';
				}
			}else{
				list.append(itemHTML);
				itemHTML = '';
				var customizer = renderDef.customizers[fieldname];
				var customizedValue = customizer(currentRecord, value)
				var item = $('<li><b>' + renderDef.labels[fieldname] + ':</b></li>');
				item.append(customizedValue);
				list.append(item);
			}
		}
		list.append(itemHTML);
		
		panelSettings.body = list;
		
		//-------------------------
		// Add Action buttons
		// TODO: IGNORE!!! for now....
//				var id = null;
//				if(renderDef.idfield != null){
//					id = currentRecord[renderDef.idfield];
//				}
//				for(var fieldKey in renderDef.actions){
//					
//					cellHTML += '<td>'+renderDef.actions[fieldKey](currentRecord, id )+'</td>';
//				}
//				row.append(cellHTML);
//				cfwTable.addRow(row);
		
		//-------------------------
		// Add Panel to Wrapper
		var cfwPanel = new CFWPanel(panelSettings);
		cfwPanel.appendTo(wrapper);
	}
	
	//----------------------------------
	// Create multi buttons
	if(renderDef.bulkActions != null){
		var actionsDivTop  = $('<div class="m-1">');
		var actionsDivBottom  = $('<div class="m-1">');
		for(var buttonLabel in renderDef.bulkActions){
			//----------------------------
			// Top 
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
				var func = renderDef.bulkActions[buttonLabel];
				var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivTop.append(button);
			}
			
			//----------------------------
			// Bottom
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
				var func = renderDef.bulkActions[buttonLabel];
				var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivBottom.append(button);
			}
		}
		
		if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
			wrapper.prepend(actionsDivTop);
		}
		if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
			wrapper.append(actionsDivBottom);
		}
		
	}
	return wrapper;
}

CFW.render.registerRenderer("panels", new CFWRenderer(cfw_renderer_panels) );


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart(renderDef) {
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
		// The type of the chart: line|steppedline|area|steppedarea|bar|scatter (to be done radar|pie|doughnut|polarArea|bubble)
		charttype: 'line',
		// stack the bars, lines etc...
		stacked: false,
		// show or hide the legend
		showlegend: true, 
		// make the chart responsive
		responsive: true,
		// The name of the field which contains the values for the x-axis
		xfield: null,
		// The name of the field which contains the values for the y-axis
		yfield: null,
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
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.chart);
	
	
	//========================================
	// Initialize
	var doFill = false;
	var isSteppedline = false;
	
	if(settings.charttype == 'area'){
		settings.charttype = 'line';
		doFill = true;
	}else if(settings.charttype == 'steppedline'){
		settings.charttype = 'line';
		isSteppedline = true;
	}else if(settings.charttype == 'steppedarea'){
		settings.charttype = 'line';
		doFill = true;
		isSteppedline = true;
	}else if(settings.charttype == 'scatter'){
		if(settings.pointradius == 0){
			settings.pointradius = 2;
		}
	}
		
	//========================================
	// Create Workspace
	// ChartJS needs a DOM element to use
	// getComputedStyle.
	var workspace = $('#cfw-chartrenderer-workspace');

	if(workspace.length == 0){
		workspace = $('<div id="cfw-chartrenderer-workspace" style="display: none;">');
		$('body').append(workspace);				
	}

	
	//========================================
	// Create Datasets
	var datasets = {};
	var hue = 120; 
	
	for(var i = 0; i < renderDef.data.length; i++){
		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		
		if(datasets[label] == undefined){
			hue += 40;
			var borderColor = CFW.colors.randomHSL(hue,65,100,55,70);
			var bgColor = borderColor.replace('1.0)', '0.65)');
			datasets[label] = {
					label: label, 
					data: [], 
					backgroundColor: bgColor,
					fill: doFill,
		            borderColor: borderColor,
		            borderWidth: 1,
		            spanGaps: false,
		            steppedLine: isSteppedline,
		            lineTension: 0,
		            cfwSum: 0,
		            cfwCount: 0
				};
			
		}
		
		var value = currentRecord[settings.yfield];
		
		if(settings.xfield == null){
			datasets[label].data.push(value);
			//datasets[label].cfwSum += isNaN(value) ? 0 : parseFloat(value);
			//datasets[label].cfwCount += 1;
		}else{
			datasets[label].data.push({
				x: currentRecord[settings.xfield], 
				y: value
			});
			//datasets[label].cfwSum += isNaN(value) ? 0 : parseFloat(value);
			//datasets[label].cfwCount += 1;
		}
	}
	
	//========================================
	// Create ChartJS Data Object
	var data = {};
	if(settings.charttype != 'radar'){
		data.datasets = [];
		
		for(label in datasets){
			data.datasets.push(datasets[label]);
		}
	}else{
		data.labels = []
		data.datasets = [{data: []}];
		
		for(label in datasets){
			data.labels.push(label)
			data.datasets[0].data.push(datasets[label].cfwSum / datasets[label].cfwCount);
		}
		console.log(data)
	}

	
	//========================================
	// Create Options
	
	var chartOptions =  {
	    	responsive: settings.responsive,
	    	maintainAspectRatio: false,
	    	legend: {
	    		display: settings.showlegend,
	    		position: 'bottom',
	    		labels: {
	    			boxWidth: 16,
	    			
	    		}
	    	},
	    	animation: {
				duration: 0
			},
			scales: {
				xAxes: [{
					type: settings.xtype,
					distribution: 'linear',
					offset: true,
					stacked: settings.stacked,
					gridLines: {
						drawBorder: false,
						color: 'rgba(128,128,128, 0.2)'
					},
					ticks: {
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
					
				}],
				yAxes: [{
					stacked: settings.stacked,
					type: settings.ytype,
					gridLines: {
						drawBorder: false,
						color: 'rgba(128,128,128, 0.8)'
					},
					scaleLabel: {
						display: false,
						labelString: 'Closing price ($)'
					},
					ticks: {
						source: 'data',
						autoSkip: true,
						autoSkipPadding: 15,
						//sampleSize: 1000,
						major: {
							enabled: true,
							//fontStyle: 'bold'
						},
					},
				}]
			},
			elements: {
                point:{
                    radius: settings.pointradius
                }
            },
			tooltips: {
				intersect: false,
				mode: 'index',
//				callbacks: {
//					label: function(tooltipItem, myData) {
//						var label = myData.datasets[tooltipItem.datasetIndex].label || '';
//						if (label) {
//							label += ': ';
//						}
//						label += parseFloat(tooltipItem.value).toFixed(2);
//						return label;
//					}
//				}
			},
			layout: {
	            padding: {
	                left: 10,
	                right: 10,
	                top: 10,
	                bottom: 10
	            }
	        }
	    };

	if(settings.ymin != null){ chartOptions.scales.yAxes[0].ticks.suggestedMin = settings.ymin; }
	if(settings.ymax != null){ chartOptions.scales.yAxes[0].ticks.suggestedMax = settings.ymax; }
	
	//========================================
	// Create Chart
	var chartCanvas = $('<canvas class="chartJSCanvas" width="100%">');
	var chartCtx = chartCanvas.get(0).getContext("2d");
	workspace.append(chartCanvas);
	
	new Chart(chartCtx, {
	    type: settings.charttype,
	    data: data,
	    options: chartOptions
	});
	
	// Wrap canvas to avoid scrollbars 
	var wrapper = $('<div class="cfw-chartjs-wrapper" style="width: 100%; height: 100%;">');
	wrapper.append(chartCanvas);
	return wrapper;
}

CFW.render.registerRenderer("chart", new CFWRenderer(cfw_renderer_chart) );
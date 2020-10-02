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
function cfw_renderer_csv(renderDef) {
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// the delimiter for the csv
		delimiter: ';',
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.csv);
	
	//-----------------------------------
	// Target Element
	let pre = $('<pre class="card p-3">');
	let code = $('<code ondblclick="CFW.selection.selectElementContent(this)">');
	pre.append(code);
	
	//-----------------------------------
	// Headers
	let headers = '';
	for(var key in renderDef.visiblefields){
		var fieldname = renderDef.visiblefields[key];
		
		headers += renderDef.labels[fieldname] + settings.delimiter;
	}
	// remove last semicolon
	headers = headers.substring(0, headers.length-1);
	code.append(headers+"\n");
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		let currentRecord = renderDef.data[i];
		let record = "";
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			// do not use customized values as it might return html
			var value = currentRecord[fieldname];
			
			if(typeof value === "object" ){
				value = JSON.stringify(value);
			}else{
				if(value != null){
					value = (""+value).replace('\n', '\\n');
				}else{
					value = "";
				}
			}
			
			record += value + settings.delimiter;
		}
		// remove last semicolon
		record = record.substring(0, record.length-1);
		code.append(record+"\n");
	}

	return pre;
}
CFW.render.registerRenderer("csv",  new CFWRenderer(cfw_renderer_csv));

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
		showlabels: true, 
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
			var radius;
			switch(settings.borderstyle.toLowerCase()){
				case 'round':  			currentTile.css('border-radius', baseradius * settings.sizefactor+'px');
										break;
										
				case 'superround':  	currentTile.css('border-radius', baseradius * 2 * settings.sizefactor+'px');
										break;
				
				case 'asymmetric':  	radius = baseradius * 2.4 * settings.sizefactor+'px ';
										radius += baseradius * 0.8 * settings.sizefactor+'px';
										currentTile.css('border-radius', radius);
										break;
										
				case 'superasymmetric':	radius = baseradius * 5 * settings.sizefactor+'px ';
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

			currentTile.append('<span class="text-center" style="font-size: '+18*settings.sizefactor+'px;"><strong>'+tileTitle+'</strong></span>');
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
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
			filterable: true,
			responsive: true,
			hover: true,
			striped: true,
			narrow: false,
			stickyheader: false, 
			// define if single element arrays should be converted into vertical table (convert columns to rows)
			verticalize: false,
			headerclasses: [],
	};
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into table.</span>";
	}
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.table);

	//-----------------------------------
	// Verticalize Single Records
	if(renderDef.data.length == 1 && settings.verticalize){
		let singleRecordData = [];
		let singleRecord = renderDef.data[0]
		
		//----------------------
		// Fields
		for(let i in renderDef.visiblefields){
			let fieldname = renderDef.visiblefields[i];
			let label = CFW.format.fieldNameToLabel(fieldname);
			let finalValue = singleRecord[fieldname];
			
			if(renderDef.customizers[fieldname] != null){
				let customizer = renderDef.customizers[fieldname];
				finalValue = customizer(singleRecord, finalValue);
			}
			singleRecordData.push({name: label, value: finalValue});
		}
		
		//-------------------------
		// Add Action buttons
		if(renderDef.actions.length > 0){
			let id = null;
			let actionButtonHTML = "";
			if(renderDef.idfield != null){
				id = singleRecord[renderDef.idfield];
			}
			var actionDiv = $('<div>');
			for(let fieldKey in renderDef.actions){
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
		let checkbox = $('<input type="checkbox" onclick="$(\'.'+selectorGroupClass+':visible\').prop(\'checked\', $(this).is(\':checked\') )" >');
		
		cfwTable.addHeader(checkbox);
	}
	
	for(let i = 0; i < renderDef.visiblefields.length; i++){
		let fieldname = renderDef.visiblefields[i];
		cfwTable.addHeader(
				renderDef.labels[fieldname], 
				(i <= settings.headerclasses.length-1 ? settings.headerclasses[i] : null)
		);
	}
	
	for(let key in renderDef.actions){
		cfwTable.addHeader("&nbsp;");
	}
				
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		let currentRecord = renderDef.data[i];
		let row = $('<tr class="cfwRecordContainer">');
		
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
			
			let value = "";
			if(renderDef.idfield != null){
				value = currentRecord[renderDef.idfield];
			}
			let checkboxCell = $('<td>');
			let checkbox = $('<input class="'+selectorGroupClass+'" type="checkbox" value="'+value+'">');
			checkbox.data('idfield', renderDef.idfield);
			checkbox.data('record', currentRecord);
			checkboxCell.append(checkbox);
			row.append(checkboxCell);
		}
		
		//-------------------------
		// Add field Values as Cells
		for(let key in renderDef.visiblefields){
			let fieldname = renderDef.visiblefields[key];
			let value = currentRecord[fieldname];
			
			if(renderDef.customizers[fieldname] == null){
				
				if(settings.verticalize){
					//value already customized and may be a JQuery Object
					let cell = $('<td>');
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
				let customizer = renderDef.customizers[fieldname];
				let customizedValue = customizer(currentRecord, value);
				let cell = $('<td>');
				cell.append(customizedValue);
				row.append(cell);
				
			}
		}
		row.append(cellHTML);
		//-------------------------
		// Add Action buttons
		let id = null;
		if(renderDef.idfield != null){
			id = currentRecord[renderDef.idfield];
		}
		for(let fieldKey in renderDef.actions){
			let td = $('<td>');
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
		let wrapperDiv = cfwTable.getTable();
		
		let actionsDivTop  = $('<div class="m-1">');
		let actionsDivBottom  = $('<div class="m-1">');
		for(let buttonLabel in renderDef.bulkActions){
			//----------------------------
			// Top 
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivTop.append(button);
			}
			
			//----------------------------
			// Bottom
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
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
	// Create Pannels
	//===================================================
	var wrapper = $("<div class='flex-grow-1'>");
	
	var selectorGroupClass = "panel-checkboxes-"+CFW.utils.randomString(16);
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		
		//---------------------------
		// Preprarations
		let currentRecord = renderDef.data[i];
		
		let panelSettings = {
				cardstyle: currentRecord[renderDef.bgstylefield],
				textstyle: currentRecord[renderDef.textstylefield],
				textstyleheader: null,
				title: $('<div>'),
				titleright: "&nbsp;",
				body: "&nbsp;",
		};
		 
		//---------------------------
		// Resolve Title				
		panelSettings.title.append(renderDef.getTitleHTML(currentRecord));	
		
		//-------------------------
		// Add Action buttons
		if(renderDef.actions.length > 0){
			let id = null;
			if(renderDef.idfield != null){
				id = currentRecord[renderDef.idfield];
			}
			
			let actionDiv = $('<div>')
			for(let fieldKey in renderDef.actions){
				actionDiv.append(renderDef.actions[fieldKey](currentRecord, id ));
			}
			
			panelSettings.titleright = actionDiv;
		}
		//-------------------------
		// Checkboxes for selects
		if(renderDef.bulkActions != null){
			
			let value = "";
			if(renderDef.idfield != null){
				value = currentRecord[renderDef.idfield];
			}
			
			let checkboxDiv = $('<div>');
			let checkbox = $('<input class="form-input float-left mt-1 mr-2 '+selectorGroupClass+'" type="checkbox" value="'+value+'" >');
			checkbox.data('idfield', renderDef.idfield);
			checkbox.data('record', currentRecord);
			checkboxDiv.append(checkbox);
			
			panelSettings.title.prepend(checkboxDiv);
		}
		
		//-------------------------
		// Add field Values as Unordered List
		let list = $("<ul>");
		let itemHTML = '';
		for(let key in renderDef.visiblefields){
			let fieldname = renderDef.visiblefields[key];
			let value =renderDef.getCustomizedValue(currentRecord,fieldname);
		
			itemHTML += '<li><strong>' + renderDef.labels[fieldname] + ':</strong> ' + value + '</li>';

		}
		list.append(itemHTML);
		
		panelSettings.body = list;
				
		//-------------------------
		// Add Panel to Wrapper
		var cfwPanel = new CFWPanel(panelSettings);
		cfwPanel.appendTo(wrapper);
	}
	
	//----------------------------------
	// Create multi buttons
	if(renderDef.bulkActions != null){
		let actionsDivTop  = $('<div class="m-1">');
		let actionsDivBottom  = $('<div class="m-1">');
		for(let buttonLabel in renderDef.bulkActions){
			//----------------------------
			// Top 
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivTop.append(button);
			}
			
			//----------------------------
			// Bottom
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
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
function cfw_renderer_cards (renderDef) {
				
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// the number of columns the cards should be displayed in
		maxcolumns: 3,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.cards);
	
	//===================================================
	// Create Pannels
	//===================================================
	var wrapper = $("<div class='flex-grow-1'>");
	
	var selectorGroupClass = "card-checkboxes-"+CFW.utils.randomString(16);
	
	//-----------------------------------
	// Print Records
	var row = $('<div class="row">');
	for(let i = 0; i < renderDef.data.length; i++ ){
		
		//---------------------------
		// Preprarations
		let currentRecord = renderDef.data[i];
		
		let cardSettings = {
				cardstyle: currentRecord[renderDef.bgstylefield],
				textstyle: currentRecord[renderDef.textstylefield],
				textstyleheader: null,
				title: $('<div>'),
				titleright: "&nbsp;",
				body: "&nbsp;",
		};
		 
		//---------------------------
		// Resolve Title				
		cardSettings.title.append(renderDef.getTitleHTML(currentRecord));	
		
		//-------------------------
		// Add Action buttons
		if(renderDef.actions.length > 0){
			let id = null;
			if(renderDef.idfield != null){
				id = currentRecord[renderDef.idfield];
			}
			
			let actionDiv = $('<div>')
			for(let fieldKey in renderDef.actions){
				actionDiv.append(renderDef.actions[fieldKey](currentRecord, id ));
			}
			
			cardSettings.titleright = actionDiv;
		}
		//-------------------------
		// Checkboxes for selects
		if(renderDef.bulkActions != null){
			
			let value = "";
			if(renderDef.idfield != null){
				value = currentRecord[renderDef.idfield];
			}
			
			let checkboxDiv = $('<div>');
			let checkbox = $('<input class="form-input float-left mt-1 mr-2 '+selectorGroupClass+'" type="checkbox" value="'+value+'" >');
			checkbox.data('idfield', renderDef.idfield);
			checkbox.data('record', currentRecord);
			checkboxDiv.append(checkbox);
			
			cardSettings.title.prepend(checkboxDiv);
		}
		
		//-------------------------
		// Add field Values as Unordered List
		let list = $("<ul>");
		let itemHTML = '';
		for(let key in renderDef.visiblefields){
			let fieldname = renderDef.visiblefields[key];
			let value =renderDef.getCustomizedValue(currentRecord,fieldname);
		
			itemHTML += '<li><strong>' + renderDef.labels[fieldname] + ':</strong> ' + value + '</li>';

		}
		list.append(itemHTML);
		
		cardSettings.body = list;
				
		//-------------------------
		// Add Card to Row
		var cfwCard = new CFWCard(cardSettings);
		row.append(cfwCard);
		//new row
		if( i > 0 && (i % settings.maxcolumns) == 0){
			wrapper.append(row);
			row = $('<div class="row">');
		}
		var column = $('<div class="col-sm">');
		
		cfwCard.appendTo(column);
		row.append(column);
		
		//-------------------------
		// Add Last Row 
		if(i == renderDef.data.length-1){
			wrapper.append(row);
		}
	}
	
	//----------------------------------
	// Create multi buttons
	if(renderDef.bulkActions != null){
		let actionsDivTop  = $('<div class="m-1">');
		let actionsDivBottom  = $('<div class="m-1">');
		for(let buttonLabel in renderDef.bulkActions){
			//----------------------------
			// Top 
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
				button.data('checkboxSelector', '.'+selectorGroupClass); 
				button.data("function", func); 
				actionsDivTop.append(button);
			}
			
			//----------------------------
			// Bottom
			if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
				let func = renderDef.bulkActions[buttonLabel];
				let button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
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

CFW.render.registerRenderer("cards", new CFWRenderer(cfw_renderer_cards) );

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
				enabled: false,
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
				custom: function(tooltipModel) {
	                // Tooltip Element
	                var $tooltip = $('#chartjs-tooltip');

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

	                // `this` will be the overall tooltip
	                var position = this._chart.canvas.getBoundingClientRect();

	                // Display, position, and set styles for font
	                $tooltip.css('opacity', 1);
	                $tooltip.css('position', 'absolute');
	                $tooltip.css('left', position.left + window.pageXOffset + tooltipModel.caretX + 'px');
	                $tooltip.css('top', position.top + window.pageYOffset + tooltipModel.caretY + 'px');
	                $tooltip.css('fontFamily', tooltipModel._bodyFontFamily);
	                $tooltip.css('fontSize', tooltipModel.bodyFontSize + 'px');
	                $tooltip.css('fontStyle', tooltipModel._bodyFontStyle);
	                $tooltip.css('padding', tooltipModel.yPadding + 'px ' + tooltipModel.xPadding + 'px');
	                $tooltip.css('pointerEvents', 'none');
	                $tooltip.css('z-index', 128);
	            }
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

	//========================================
	// Set Min Max
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

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer(renderDef) {
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
			// The available renderers which can be chosen with the Display As option
			renderers: [
				{	
					label: 'Table', // Label to display on the UI
					name: 'table',  // the name of the renderer
					// Override the default renderer settings
					renderdef: {
						rendererSettings: {
							table: {filterable: false},
						},
					}
				}
			],
			// The initial page to be drawn.
			initialpage: 1,
			// The number of items options for the page size selector. -1 stands for "All".
			sizes: [10, 25, 50, 100, 200, 500, 1000, -1],
			// The size selected by default
			defaultsize: 50,
			// if a store ID is provided, the settings will be saved and restored when refreshing the viewer or the page.
			storeid: null,
			// enable sorting. 
			//sortable: false,
			// the interface to fetch the data from
			datainterface: {
				//The url to fetch the data from. If null the data from rendererSettings.data will be used.
				url:  null,
				//The item which should be fetched from the server.
				item:  'default',
				//The param name for the item the action should be applied to
				itemparam:  'item',
				//The param name for the action executed by the dataviewer
				actionparam:  'action',
				//The  param name for the maximum size of the result
				sizeparam:  'pagesize',
				//The param name for the page to fetch
				pageparam: 'pagenumber',
				//The offset for fetching the results
				//sortbyparam: 'sortby',
				//The offset for fetching the results
				//sortdirectionparam: 'sort',
				//The filter string used for filtering the results
				filterqueryparam: 'filterquery',
				//The name of the field containing the total number of rows
				totalrowsfield: 'TOTAL_RECORDS',
			},
	};
	
	//var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.dataviewer);
	var settings = _.merge({}, defaultSettings, renderDef.rendererSettings.dataviewer);	
	
	let dataviewerID = "dataviewer-"+CFW.utils.randomString(12);
	let dataviewerDiv = $('<div class="cfw-dataviewer" id="'+dataviewerID+'">');
	
	dataviewerDiv.data('renderDef', renderDef);
	dataviewerDiv.data('settings', settings);
	
	
	dataviewerDiv.append(cfw_renderer_dataviewer_createMenuHTML(dataviewerID, settings));
	dataviewerDiv.append('<div class="cfw-dataviewer-content">');
	
	cfw_renderer_dataviewer_fireChange(dataviewerDiv, settings.initialpage);
		
	return dataviewerDiv;
	
}

CFW.render.registerRenderer("dataviewer", new CFWRenderer(cfw_renderer_dataviewer) );

/******************************************************************
 * 
 * @param dataviewerIDOrJQuery cssSelector string like '#dataviewer-6a5b39ai' or a JQuery object
 * @param pageToRender page number
 ******************************************************************/
function cfw_renderer_dataviewer_fireChange(dataviewerIDOrJQuery, pageToRender) {
	
	//=====================================================
	// Initialize
	var dataviewerDiv = $(dataviewerIDOrJQuery);
	var settingsDiv = dataviewerDiv.find('.cfw-dataviewer-settings');
	var targetDiv = dataviewerDiv.find('.cfw-dataviewer-content');
	var dataviewerID = "#"+dataviewerDiv.attr('id');
	var renderDef = dataviewerDiv.data('renderDef');
	var settings = dataviewerDiv.data('settings');
	
	//=====================================================
	// Handle Page to Render
	if(pageToRender == null || pageToRender == undefined){
		pageToRender = dataviewerDiv.data('currentpage');
		if(pageToRender == null || pageToRender == undefined){
			pageToRender = 1;
		}
	}
	
	dataviewerDiv.data('currentpage', pageToRender);
	
	//=====================================================
	// Get Settings
	var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	var filterquery = settingsDiv.find('input[name="filterquery"]').val();
	var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
	var offset = (pageSize > 0 ) ? pageSize * (pageToRender-1): 0;
	
	if(settings.storeid != null){
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][pageSize]', pageSize);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][filterquery]', filterquery);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][rendererIndex]', rendererIndex);
	}
	
	//=====================================================
	// Get Render Results
	if(settings.datainterface.url == null){
		
		//-------------------------------------
		// Static 
		if(CFW.utils.isNullOrEmpty(filterquery)){
			let totalRecords = renderDef.data.length;
			let dataToRender = _.slice(renderDef.data, offset, offset+pageSize);
			if(pageSize == -1){
				dataToRender = renderDef.data;
			}
			console.log(dataToRender);
			cfw_renderer_dataviewer_renderPage(dataviewerDiv, dataToRender, totalRecords, pageToRender);
		}else{
			filterquery = filterquery.toLowerCase();
			let filteredData = _.filter(renderDef.data, function(o) { 
				    return JSON.stringify(o).toLowerCase().includes(filterquery); 
			});
			let totalRecords = filteredData.length;
			let dataToRender = _.slice(filteredData, offset, offset+pageSize);
			if(pageSize == -1){
				dataToRender = filteredData;
			}			
			cfw_renderer_dataviewer_renderPage(dataviewerDiv, dataToRender, totalRecords, pageToRender);
		}
	}else{
		
		//-------------------------------------
		// Dynamic
		let params = {};
		params[settings.datainterface.actionparam] = "fetchpartial";
		params[settings.datainterface.sizeparam] = pageSize;
		params[settings.datainterface.pageparam] = pageToRender;
		params[settings.datainterface.filterqueryparam] = filterquery;
		params[settings.datainterface.itemparam] = settings.datainterface.item;
//		params[settings.datainterface.sortbyparam] = ;
//		params[settings.datainterface.sortdirectionparam] = ;
//		params[settings.datainterface.filterparam] = ;
//		params[settings.datainterface.totalrowsfield] = ;
		
		CFW.http.getJSON(settings.datainterface.url, params, function(data){
			
			if(data.payload != null){
				let dataToRender = data.payload;
				let totalRecords = (dataToRender.length > 0) ? dataToRender[0][settings.datainterface.totalrowsfield] : 0;
				
				cfw_renderer_dataviewer_renderPage(dataviewerDiv, dataToRender, totalRecords, pageToRender);
			}
		}
	);
	}
			
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_renderPage(dataviewerDiv, dataToRender, totalRecords, pageToRender) {
	
	//-------------------------------------
	// Initialize
	//var dataviewerDiv = $(dataviewerID);
	var settingsDiv = dataviewerDiv.find('.cfw-dataviewer-settings');
	var targetDiv = dataviewerDiv.find('.cfw-dataviewer-content');
	var dataviewerID = "#"+dataviewerDiv.attr('id');
	var renderDef = dataviewerDiv.data('renderDef');
	var dataviewerSettings = dataviewerDiv.data('settings');
	
	//-------------------------------------
	// Get Settings
	//var totalRecords = dataToRender.length;
	var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	
	var offset = pageSize * (pageToRender-1);
		
	//-------------------------------------
	// Prepare Renderer
	
	let renderDefOverrides = dataviewerSettings.renderers[0].renderdef;
	let rendererName = dataviewerSettings.renderers[0].name;
	if(dataviewerSettings.renderers.length > 1){
		var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
		renderDefOverrides = dataviewerSettings.renderers[rendererIndex].renderdef;
		rendererName = dataviewerSettings.renderers[rendererIndex].name;
	}
	//-------------------------------------
	// Call Renderer
	let renderDefClone = _.assign({}, renderDef, renderDefOverrides);
	renderDefClone.data = dataToRender;
	var renderResult = CFW.render.getRenderer(rendererName).render(renderDefClone);
	
	//-------------------------------------
	// Create Paginator
	var pageNavigation = cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageToRender);
	
	targetDiv.html('');
	targetDiv.append(pageNavigation);
	targetDiv.append(renderResult);
	targetDiv.append(pageNavigation);
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createMenuHTML(dataviewerID, dataviewerSettings) {
	
	//--------------------------------------
	// Initialize Variables
	var onchangeAttribute = ' onchange="cfw_renderer_dataviewer_fireChange(\'#'+dataviewerID+'\', 1)" ';
	var html = '<div class="cfw-dataviewer-settings">';
	
	//--------------------------------------
	// Prepare Settings
	var selectedRendererIndex = 0;
	var selectedSize = dataviewerSettings.defaultsize;
	var filterquery = '';
	
	if(dataviewerSettings.storeid != null){
		selectedRendererIndex 	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][rendererIndex]', selectedRendererIndex);
		selectedSize 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][pageSize]', selectedSize);
		filterquery 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][filterquery]', filterquery);
	}
	
	//--------------------------------------
	// Display As
	if(dataviewerSettings.renderers.length > 1){
		
		html += '<div class="float-right ml-2">'
			+'	<label for="displayas">Display As:&nbsp;</label>'
			+'	<select name="displayas" class="form-control form-control-sm" title="Choose Display" '+onchangeAttribute+'>'
		
			for(index in dataviewerSettings.renderers){
				var renderer = dataviewerSettings.renderers[index];
				var selected = (index == selectedRendererIndex) ? 'selected' : '';
				
				html += '<option value="'+index+'" '+selected+'>'+renderer.label+'</option>';
			}
		
		html += '	</select>'
				+'</div>';
	}
	//--------------------------------------
	// Page Size
	html += '<div class="float-right ml-2">'
		+'	<label for="pagesize">Page Size:&nbsp;</label>'
		+'	<select name="pagesize" class="form-control form-control-sm" title="Choose Page Size" '+onchangeAttribute+'>'
	
		for(key in dataviewerSettings.sizes){
			var size = dataviewerSettings.sizes[key];
			var selected = (size == selectedSize) ? 'selected' : '';
			
			if(size != -1){
				html += '<option value="'+size+'" '+selected+'>'+size+'</option>';
			}else{
				html += '<option value="'+size+'" '+selected+'>All</option>';
			}
		}
	
	html += '	</select>'
			+'</div>';
	
	//--------------------------------------
	// Filter Query
	html += '<div class="float-right  ml-2">'
		+'	<label for="filterquery">Filter:&nbsp;</label>'
		+'	<input type="text" name="filterquery" class="form-control form-control-sm" value="'+filterquery+'" placeholder="Filter..."  title="Filter the Results" '+onchangeAttribute+'>'
		+'</div>';
	
	html += '</div>';
	
	return html;
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createPageListItem(dataviewerID, page, label, isActive) {
	return '<li class="page-item '+(isActive ? 'active':'')+'">'
				+'<a class="page-link" onclick="cfw_renderer_dataviewer_fireChange(\''+dataviewerID+'\', '+page+')">'+label+'</a>'
			+'</li>';
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageActive) {
	//============================================
	// Variables
	var totalPages = Math.ceil(totalRecords / pageSize);
	
	var itemText = "Items";
	if(totalRecords == 1) { itemText = "Item"}
	
	var pagesText = "Pages";
	if(totalPages == 1) { pagesText = "Page"}
	
	
	//============================================
	// HTML
	var html = 
		'<nav aria-label="Page Navigation" class="d-flex justify-content-end align-items-center mb-2">'
			+'<span class="mr-2"><strong>'+totalRecords+'</strong> '+itemText+' on <strong>'+totalPages+'</strong> '+pagesText+'</span>'
			+'<ul class="pagination pagination-sm m-0">'
			+ cfw_renderer_dataviewer_createPageListItem(dataviewerID, 1,'<i class="fas fa-angle-double-left"></i>', false)
			+ cfw_renderer_dataviewer_createPageListItem(dataviewerID, ((pageActive > 1) ? pageActive-1 : 1),'<i class="fas fa-angle-left"></i>', false);
			
	var pageListItems = '';
	

	//============================================
	// Create Pages

	pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, pageActive, pageActive, true);
	let pagesCreated = 1;

	//-------------------------------
	// Lower Pages
	let currentPage = pageActive-1;
	for(let i = 1; i < 4 && currentPage >= 1; i++ ){
		pageListItems = 
			cfw_renderer_dataviewer_createPageListItem(dataviewerID, currentPage,currentPage, false)
			+ pageListItems ;
		
		pagesCreated++;
		currentPage--;
	}
	if(currentPage > 2){
		var jumpPage = Math.ceil(currentPage / 2);
		pageListItems = cfw_renderer_dataviewer_createPageListItem(dataviewerID, jumpPage,jumpPage, false)
						+ pageListItems;
		pagesCreated++;
	}
	
	//-------------------------------
	// Higher Pages
	currentPage = pageActive+1;
	for(; pagesCreated < 8 && currentPage <= totalPages; ){
		pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, currentPage,currentPage, false);
		pagesCreated++;
		currentPage++;
	}
	if(currentPage < totalPages-1){
		var jumpPage = currentPage+Math.ceil((totalPages-currentPage) / 2);
		pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, jumpPage,jumpPage, false);
	}

		
	//-------------------------------------
	// Add Navigate Forward Buttons
	html += pageListItems;
	html +=cfw_renderer_dataviewer_createPageListItem(dataviewerID, ((pageActive < totalPages) ? pageActive+1 : totalPages),'<i class="fas fa-angle-right"></i>', false)
		 + cfw_renderer_dataviewer_createPageListItem(dataviewerID, totalPages,'<i class="fas fa-angle-double-right"></i>', false);
	
			
	html +='</ul></nav>';

	return html;
}

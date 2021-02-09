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
					value = (""+value).replaceAll('\n', '\\n')
									  .replaceAll('\r', '\\r')
									  .replaceAll('<', '&lt;');
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
			// define if the verticalized fieldnames should be capitalized and underscores removed
			verticalizelabelize: true,
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
			let label = fieldname;
			if(settings.verticalizelabelize){
				label = CFW.format.fieldNameToLabel(fieldname);
			}
			let finalValue = singleRecord[fieldname];
			
			if(renderDef.customizers[fieldname] != null){
				let customizer = renderDef.customizers[fieldname];
				finalValue = customizer(singleRecord, finalValue);
			}
			else if(renderDef.customizers['value'] != null){
				let customizer = renderDef.customizers['value'];
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
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
		//set to true to make the header smaller
		narrow: false,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.panels);
	
	
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
				narrow: settings.narrow,
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
		//set to true to make the header smaller
		narrow: false,
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
				narrow: settings.narrow,
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
		// How should the input data be handled groupbytitle|arrays 
		datamode: 'groupbytitle',
		// stack the bars, lines etc...
		stacked: false,
		// show or hide the legend
		showlegend: true, 
		// show or hide the axes, useful to create sparkline like charts
		showaxes: true,
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
		// the padding in pixels of the chart
		padding: 10,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.chart);
	
	
	//========================================
	// Initialize
	settings.doFill = false;
	settings.isSteppedline = false;
	
	if(settings.charttype == 'area'){
		settings.charttype = 'line';
		settings.doFill = true;
	}else if(settings.charttype == 'steppedline'){
		settings.charttype = 'line';
		settings.isSteppedline = true;
	}else if(settings.charttype == 'steppedarea'){
		settings.charttype = 'line';
		settings.doFill = true;
		settings.isSteppedline = true;
	}else if(settings.charttype == 'scatter'){
		if(settings.pointradius == 0){
			settings.pointradius = 2;
		}
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
		let index = 0;
		for(let key in firstRecord){
			if(key == settings.xfield || key == settings.yfield) { continue; }
			renderDef.titleformat += key+'="{'+index+'}" / ';
			index++;
		}
		renderDef.titleformat = renderDef.titleformat.substr(0, renderDef.titleformat.length-3);
		
	}
	
	//========================================
	// Create Datasets
	var datasets;
	if(settings.datamode == 'groupbytitle'){
		var datasets = cfw_renderer_chart_createDatasetsGroupedByTitleFields(renderDef, settings);
	}else if(settings.datamode == 'arrays'){
		var datasets = cfw_renderer_chart_createDatasetsFromArrays(renderDef, settings);
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
	    	legend: {
	    		display: settings.showlegend,
	    	},
			scales: {
				xAxes: [{
					display: settings.showaxes,
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
					display: settings.showaxes,
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
						// Custom Tick Format
						callback : function(value, index, values) {

							if (value > 1000000000) {
								return (value / 1000000000).toFixed(1) + "G";
							} else if (value > 1000000) {
								return (value / 1000000).toFixed(1) + "M";
							} else if (value > 1000) {
								return (value / 1000).toFixed(1) + "K";
							} else {
								return value;
							}
						}
					},
				}]
			},
			
			elements: {
                point:{
                    radius: settings.pointradius
                }
            },
            
            layout: {
                padding: {
                    left: settings.padding,
                    right: settings.padding,
                    top: settings.padding,
                    bottom: settings.padding
                }
            },
                
			tooltips: {
				intersect: false,
				enabled: false,
				mode: 'index',
				custom: cfw_renderer_chart_customTooltip,
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
	var wrapper = $('<div class="cfw-chartjs-wrapper">');
	wrapper.append(chartCanvas);
	return wrapper;
}

CFW.render.registerRenderer("chart", new CFWRenderer(cfw_renderer_chart) );

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_setGlobals() {
	Chart.defaults.global.responsive = true; 
	Chart.defaults.global.maintainAspectRatio = false;

	Chart.defaults.global.legend.display = false;
	Chart.defaults.global.legend.position =  'bottom';
	Chart.defaults.global.legend.labels.boxWidth = 16;

	Chart.defaults.global.animation.duration = 0;
		
	//Chart.defaults.global.datasets.line.showLine = false;
		
	Chart.defaults.global.layout = {
        padding: {
            left: 10,
            right: 10,
            top: 10,
            bottom: 10
        }
    }
}
cfw_renderer_chart_setGlobals();
/******************************************************************
 * 
 ******************************************************************/

function cfw_renderer_chart_customTooltip(tooltipModel) {
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

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsGroupedByTitleFields(renderDef, settings) {
	
	var datasets = {};
	var hue = 165; 

	for(var i = 0; i < renderDef.data.length; i++){
		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		if(datasets[label] == undefined){
			hue += 30;
			var borderColor = CFW.colors.randomHSL(hue,65,100,55,70);
			var bgColor = borderColor.replace('1.0)', '0.65)');
			datasets[label] = {
					label: label, 
					data: [], 
					backgroundColor: bgColor,
					fill: settings.doFill,
		            borderColor: borderColor,
		            borderWidth: 1,
		            spanGaps: false,
		            steppedLine: settings.isSteppedline,
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
	
	return datasets;
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_chart_createDatasetsFromArrays(renderDef, settings) {
	
	var datasets = {};
	var hue = 165; 

	for(var i = 0; i < renderDef.data.length; i++){
		var currentRecord = renderDef.data[i];
		
		//----------------------------
		// Create Label & Dataset
		var label = renderDef.getTitleString(currentRecord);
		if(datasets[label] == undefined){
			hue += 30;
			var borderColor = CFW.colors.randomHSL(hue,65,100,55,70);
			var bgColor = borderColor.replace('1.0)', '0.65)');
			datasets[label] = {
					label: label, 
					data: [], 
					backgroundColor: bgColor,
					fill: settings.doFill,
		            borderColor: borderColor,
		            borderWidth: 1,
		            spanGaps: false,
		            steppedLine: settings.isSteppedline,
		            lineTension: 0,
		            cfwSum: 0,
		            cfwCount: 0
				};
			
		}
		
		var yArray = currentRecord[settings.yfield];
		
		if(settings.xfield == null){
			datasets[label].data = yArray;
		}else{
			var xArray = currentRecord[settings.xfield];
			
			for(let i = 0; i < xArray.length; i++)
			datasets[label].data.push({
				x: xArray[i], 
				y: yArray[i]
			});
		}
	}
	
	return datasets;
}


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
	// Handle Filter Highlight
	if(CFW.utils.isNullOrEmpty(filterquery)){
		settingsDiv.find('input[name="filterquery"]').removeClass('bg-cfw-yellow');
	}else{
		settingsDiv.find('input[name="filterquery"]').addClass('bg-cfw-yellow');
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
				
				if(renderer != null){
					html += '<option value="'+index+'" '+selected+'>'+renderer.label+'</option>';
				}
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
	let filterHighlightClass = CFW.utils.isNullOrEmpty(filterquery) ? '' : 'bg-cfw-yellow';

	html += '<div class="float-right  ml-2">'
		+'	<label for="filterquery">Filter:&nbsp;</label>'
		+'	<input type="text" name="filterquery" class="form-control form-control-sm '+filterHighlightClass+'" value="'+filterquery+'" placeholder="Filter..."  title="Filter the Results" '+onchangeAttribute+'>'
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
	
	var totalPages = (pageSize == -1) ? 1 : Math.ceil(totalRecords / pageSize);
	
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

/******************************************************************
 * 
 ******************************************************************/
CFW_GLOBAL_HIERARCHY_URL = '/app/hierarchy';
function cfw_renderer_hierarchy_sorter(renderDef) {
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// the delimiter for the csv
		url: CFW_GLOBAL_HIERARCHY_URL,
		// the id of the hierarchy config, by default the lowercase classname of the hierarchical CFWObject. (Default of this setting: null) 
		configid: null,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.hierarchy_sorter);
	
	//-----------------------------------
	// Target Element
	let resultWrapper = $('<div>');
		
	//--------------------------------
	// Print Hierarchy
	if(renderDef.data != undefined){

		for(key in renderDef.data){
			cfw_renderer_hierarchysorter_printHierarchyElement(renderDef, settings, resultWrapper, renderDef.data[key])
		}
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no items can be displayed.');
	}
	return resultWrapper;
}
CFW.render.registerRenderer("hierarchy_sorter",  new CFWRenderer(cfw_renderer_hierarchy_sorter));


/******************************************************************
*
* @param configID the name of the hierarchy config
* @param parentElement the parent JQuery element
* @param oldParent where the element was dragged from
* @param oldPrev the old previous element on dragstart
* @param childElement the dragged element
* @return 
******************************************************************/
function cfw_renderer_hierarchysorter_moveChildToParent(configID, parentElement, oldParent, oldPrev, childElement){
	
	var parentID = parentElement.data('parentid');
	var childID = childElement.data('childid');
	
	params = {action: "update", item: "parent", configid: configID, parentid: parentID, childid: childID};
	
	CFW.http.getJSON(HIERARCHY_URL, params, 
		function(data) {
			CFW_GLOBAL_HIERARCHYSORTER.parentUpdateInProgress = false;
		
			//------------------------------------------
			// if the parent could not be updated
			// move the child back to the old location
			if(!data.success){
				if(oldPrev.length != 0){
					oldPrev.after(childElement);
				}else{
					oldParent.prepend(childElement);
				}
				
				// Let it blink 3 times
				childElement.find('.card-header').addClass('bg-primary')
				childElement.fadeOut(500).fadeIn(500)
					.fadeOut(500).fadeIn(500, function(){
						childElement.find('.card-header').removeClass('bg-primary')
					}); 
				
				
				
			}
	});
}


/******************************************************************
*
* @param data as returned by CFW.http.getJSON()
* @return 
******************************************************************/
//cache for better performance
var CFW_GLOBAL_HIERARCHYSORTER = {
		oldParent: null,
		oldPrev: null,
		notDraggedDroptargets : null,
		lastDragoverMillis: null, 
		lastDragoverTarget: null,
		dropTargetChangeMillis: null,
		parentUpdateInProgress : false,
}

function cfw_renderer_hierarchysorter_printHierarchyElement(renderDef, settings, parent, currentItem){
	
	var id = currentItem.PK_ID;
	//--------------------------------------
	// Create Draggable element
	var draggableItem = $('<div id="sortable-item-'+id+'" data-childid="'+id+'" class="cfw-draggable" draggable="true">');
	var draggableHeader = $('<div id="sortable-header-'+id+'" class="cfw-draggable-handle card-header p-2 pl-3">'
			+ '<div class="cfw-fa-box cursor-pointer" role="button" data-toggle="collapse" data-target="#children-'+id+'" aria-expanded="false"></i>'
				+ '<i class="fas fa-chevron-right mr-2"></i>'
				+ '<i class="fas fa-chevron-down mr-2"></i>'
			+ '</div>'
			+renderDef.getTitleHTML(currentItem)
			+'</div>');
	
	draggableItem.on('dragstart', function(e){
		//-----------------------------
		// get dragged element, if none makr the current object as dragged
		var draggable = $('.cfw-draggable.dragging');
		
		if(draggable.length == 0){
			CFW_GLOBAL_HIERARCHYSORTER.oldParent = $(this).parent();
			CFW_GLOBAL_HIERARCHYSORTER.oldPrev = $(this).prev();
			CFW_GLOBAL_HIERARCHYSORTER.notDraggedDroptargets=$('.cfw-draggable:not(.dragging) .cfw-droptarget').toArray();
			CFW_GLOBAL_HIERARCHYSORTER.lastDragoverMillis=Date.now();
			$(this).addClass('dragging');
		}
	});
	
	draggableItem.on('dragend', function(e){
		e.preventDefault();
		
		if( !CFW_GLOBAL_HIERARCHYSORTER.parentUpdateInProgress ){
			CFW_GLOBAL_HIERARCHYSORTER.parentUpdateInProgress = true;
			
			var childElement = $(this);
			var parentElement = $(this).parent();

			cfw_renderer_hierarchysorter_moveChildToParent(
						settings.configid, 
						parentElement, 
						CFW_GLOBAL_HIERARCHYSORTER.oldParent, 
						CFW_GLOBAL_HIERARCHYSORTER.oldPrev, 
						childElement
					);
			
			$(this).removeClass('dragging');
			CFW_GLOBAL_HIERARCHYSORTER.notDraggedDroptargets=null;
		}
	});
	
	
	draggableItem.on('dragover', function(e){
		
		e.preventDefault();
		
		//---------------------------------------------------------
		// Major Performance Improvement: do only all 100ms as
		// dragover event is executed a hell lot of times
		//---------------------------------------------------------
		if(Date.now() - CFW_GLOBAL_HIERARCHYSORTER.lastDragoverMillis < 100){
			return;
		}
		CFW_GLOBAL_HIERARCHYSORTER.lastDragoverMillis = Date.now();
		
		//--------------------------------------
		// Evaluate Closest Drop Target
		//--------------------------------------
		var draggable = $('.cfw-draggable.dragging');
		
		var dropTarget = CFW_GLOBAL_HIERARCHYSORTER.notDraggedDroptargets.reduce(function(closest, currentTarget) {
			let box = $(currentTarget).prev('.cfw-draggable-handle').get(0).getBoundingClientRect();
			let offset = e.clientY - box.top - box.height / 2;
			
			if (offset < 0 && offset > closest.offset) {
				return { offset: offset, element: $(currentTarget) };
			} else {
				return closest;
			}
			
		}, { offset: Number.NEGATIVE_INFINITY }).element;
		
		//var dropTarget = dropTargetParent.find('> ul');
//		console.log("===========================");
//		console.log(draggable);
//		console.log(dropTarget);
		
		//--------------------------------------
		// Append: make sure droptarget is not 
		// a child of draggable
	    if (dropTarget != null 
	    && draggable.find('#'+dropTarget.attr('id')).length == 0) {
	    	
	    	//-------------------------------------
	    	// Get Values for Panel Expansion
	    	if(CFW_GLOBAL_HIERARCHYSORTER.previousDropTarget != dropTarget.attr('id')){
	    		CFW_GLOBAL_HIERARCHYSORTER.dropTargetChangeMillis = Date.now();
	    		CFW_GLOBAL_HIERARCHYSORTER.previousDropTarget = dropTarget.attr('id');
	    	}
	    	
	    	//-------------------------------------
	    	// Move Object
	    	//Wait for number of millis of hovering over the element before expansion
    	    if(Date.now() - CFW_GLOBAL_HIERARCHYSORTER.dropTargetChangeMillis > 500){
	    		dropTarget.collapse('show');
	    		dropTarget.prepend(draggable);
    	    }
    		

	    }
	});
	
	//--------------------------------------
	// Create Children
	var childlist = $('<div id="children-'+id+'" data-parentid="'+id+'" class="cfw-droptarget pl-4 collapse">')

	for(key in currentItem.children){
		cfw_renderer_hierarchysorter_printHierarchyElement(renderDef, settings, childlist, currentItem.children[key]);
	}
	
	//--------------------------------------
	// Append 
	draggableItem.append(draggableHeader)
	draggableItem.append(childlist);
	parent.append(draggableItem);
}

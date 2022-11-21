
CFW_RENDER_NAME_CSV = 'csv';
CFW_RENDER_NAME_JSON = 'json';
CFW_RENDER_NAME_XML = 'xml';
CFW_RENDER_NAME_TITLE = 'title';
CFW_RENDER_NAME_TABLE = 'table';
CFW_RENDER_NAME_TILES = 'tiles';
CFW_RENDER_NAME_STATUSBAR = 'statusbar';
CFW_RENDER_NAME_STATUSBAR_REVERSE = 'statusbarreverse';

CFW_RENDER_NAME_STATUSMAP = 'statusmap';
CFW_RENDER_NAME_STATUSMAP_1to1 = 'statusmap_1to1';
CFW_RENDER_NAME_STATUSMAP_2to1 = 'statusmap_2to1';
CFW_RENDER_NAME_STATUSMAP_4to1 = 'statusmap_4to1';
CFW_RENDER_NAME_STATUSMAP_8to1 = 'statusmap_8to1';
CFW_RENDER_NAME_STATUSMAP_1to2 = 'statusmap_1to2';
CFW_RENDER_NAME_STATUSMAP_1to4 = 'statusmap_1to4';
CFW_RENDER_NAME_STATUSMAP_1to8 = 'statusmap_1to8';

CFW_RENDER_NAME_STATUSLIST = 'statuslist';
CFW_RENDER_NAME_CARDS = 'cards';
CFW_RENDER_NAME_PANELS = 'panels';

CFW_RENDER_POPOVER_DEFAULTS = {
				trigger: 'hover',
				html: true,
				placement: 'auto',
				boundary: 'window',
				animation: false,
				delay: { "show": 200, "hide": 0 },
				// title: 'Details',
				sanitize: false
			}

/******************************************************************
 * Class to render HTML from JSON data.
 * 
 ******************************************************************/
class CFWRenderer{
	
	 constructor(renderFunction){
		 
		 this.renderFunction = renderFunction;
		 
		 this.defaultRenderDefinition = {
			// the data that should be rendered as an array
		 	data: null,
			// (Optional) name of the field that is used as the identifier of the data
		 	idfield: null,
		 	// (Optional) names of the fields that are used for a titles. Takes the first field from the first object if null
		 	titlefields: null,
		 	// The format of the title, use {0}, {1} ... {n} as placeholders, concatenates all title fields if null(default)
		 	titleformat: null,
		 	// (Optional) Names of the fields that should be rendered and in the current order. If null or undefined, will display all fields
		 	visiblefields: null,
			// (Optional) Names of the fields that data should be sorted by. (Note: Do not set this if you enable sorting for Dataviewer to avoid performance overhead) 
		 	sortbyfields: null,
			// Direction of the sorting: "asc"|"desc" 
		 	sortbydirection: "asc",
		 	// (Optional) Custom labels for fields, add them as "{fieldname}: {label}". If a label is not defined for a field, uses the capitalized field name
		 	labels: {},
		 	// field containing the bootstrap style (primary, info, danger ...) that should be used as the background
		 	bgstylefield: null,
		    // field containing the bootstrap style (primary, info, danger ...) that should be used as for texts
		 	textstylefield: null,
		 	// functions that return a customized htmlString to display a customized format, add as "<fieldname>: function(record, value, rendererName, fieldname)".  Cannot return a JQuery object.
		 	customizers: {},
		 	// array of functions that return html for buttons, add as "<fieldname>: function(record, id)". Cannot return a JQuery object.
		 	actions: [ ],
			// list of functions that should be working with multiple items. fieldname will be used as the button label
		 	bulkActions: null,
		 	// position of the multi actions, either top|bottom|both|none
		 	bulkActionsPos: "top",
			// define if the data is hierarchical (Default: false)
			hierarchy: false,
			// define if the hierarchy should be displayed as tree or flat
		 	hierarchyAsTree: true, 
			// the field containing the array with children
			hierarchyChildrenField: "children",
			// number of pixels used for indentation
			hierarchyIndentation: 20,
			// icon classes to define the icon(fontawesome)
			hierarchyIconClasses: "fas fa-level-up-alt fa-rotate-90 mr-2",
		 	// settings specific for the renderer, add as "rendererSettings.{rendererName}.{setting}"
		 	rendererSettings: {},
				
			/*************************************************************
			 * Customize The Value
			 *************************************************************/
		 	getCustomizedValue: function(record, fieldname, rendererName){
		 		
			 		var value = record[fieldname];
			 		if(this.customizers[fieldname] == null){
						return value;
					}else{
						var customizer = this.customizers[fieldname];
						return customizer(record, value, rendererName, fieldname);
					}
			 	},
			
			/*************************************************************
			 * Create Title HTML (uses customized values)
			 *************************************************************/
		 	getTitleHTML:  function(record){
				//-------------------------
				// Create title Base
				var title;
		 		if(!CFW.utils.isNullOrEmpty(this.titleformat)){
		 			var title = this.titleformat;
		 		}else{
					title = '';
				}
		 		
				//-------------------------
				// Create Title

		 		for(var j = 0; j < this.titlefields.length; j++){
					var fieldname = this.titlefields[j];
					var value = this.getCustomizedValue(record,fieldname);
					
					if( value instanceof jQuery ){ value = $('<div>').append(value).html(); }
					
					if(!CFW.utils.isNullOrEmpty(this.titleformat)){
						title = title.replace('{'+j+'}', value);
					}else{
						title += ' '+value;
					}
				}
		 		
		 		title = title.replace(/\{\d\}/g, '');
		 		return title.trim();
			},
			
			/*************************************************************
			 * Create Title String (does not use customize values)
			 *************************************************************/
			getTitleString:  function(record){
		 		
				//-------------------------
				// Create title Base
				var title;
		 		if(!CFW.utils.isNullOrEmpty(this.titleformat)){
		 			var title = this.titleformat;
		 		}else{
					title = '';
				}
		 		
				//-------------------------
				// Create Title
		 		for(var j = 0; j < this.titlefields.length; j++){

		 			var fieldname = this.titlefields[j];
		 			var value = record[fieldname];
		 			
		 			if( value != null){
		 				
	 					if(this.titleformat != null){
							title = title.replace('{'+j+'}', value);
						}else{
							title += ' '+value;
						}

					}

				}
		 		title = title.replace(/\{\d\}/g, '');
		 		return title.trim();
			},
			
			/*************************************************************
			 * Adds coloring to the element based on the records value and
			 * the value added in the fields bgstylefield and textstylefield.
			 *            - a CSS color name
			 *            - an RGB color code starting with '#'
			 *            - CFW color class starting with 'cfw-'
		 	 * @param record the record associated with the element
			 * @param element the DOM or JQuery element to colorize
			 * @param type either 'bg' | 'text' | 'border' 
			 * @param borderSize Optional size definition for the border.
			 *************************************************************/
		 	colorizeElement: function(record, element, type, borderSize){
				var $element = $(element);
				//--------------------------------------
				// Handle BG and Border
				if(this.bgstylefield != null && type != "text"){
					let color = record[this.bgstylefield];
					CFW.colors.colorizeElement(element, color, type, borderSize);
				}else if(this.textstylefield != null && type == "text"){
					let color = record[this.textstylefield];
					CFW.colors.colorizeElement(element, color, type, borderSize);
				}
		 		
		 	},
		 };
		  
	 }
	 
	 /********************************************
	  * Returns a String in the format YYYY-MM-DD
	  ********************************************/
	 prepareDefinition(definition){
		 var data = definition.data;
		 var firstObject = null;
		 
		 //---------------------------
		 // get first object
		 if(Array.isArray(data)){
			 definition.datatype = "array";
			 if(data.length > 0){
				 firstObject = data[0];
			 }
		 }else if(typeof data == "object"){
			 definition.datatype = "array";
			 definition.data = [data];
			 firstObject = data;
		 }else {
			 definition.datatype = typeof data;
		 }
		 
		 //---------------------------
		 // Get Visible Fields
		 if(firstObject != null && typeof firstObject == 'object'){
			 
			 //--------------------------
			 // resolve default visible fields
			 if(definition.visiblefields == null){ 
				 definition.visiblefields = [];
				 for(let key in firstObject){
					 definition.visiblefields.push(key);
				 }
			 }
			 
			 //--------------------------
			 // resolve title fields
			 if(definition.titlefields == null || definition.titlefields.length == 0 ){ 
				 if(definition.visiblefields.length > 0){ 
					 definition.titlefields = [definition.visiblefields[0]];
				 }else{
					 // Use first field for titles
					 definition.titlefields = [Object.keys(firstObject)[0]];
				 }
			 }
		 }
		 
		 
		 
		 //---------------------------
		 // Create Labels
		 for(let key in definition.visiblefields){
			let fieldname = definition.visiblefields[key];
			if(definition.labels[fieldname] == null){
				definition.labels[fieldname] = CFW.format.fieldNameToLabel(fieldname);
			}
		}
		
		//---------------------------
		// Lowercase
		definition.bulkActionsPos = definition.bulkActionsPos.toLowerCase();
		
		
		//---------------------------
		// Sort
		if( !CFW.utils.isNullOrEmpty(definition.sortbyfields) ){
			
			let sortDirection = (definition.sortbydirection == null) ? ['asc'] : [definition.sortbydirection];


			let sortFunctionArray = [];
			let sortDirectionArray = [];
			for(var index in definition.sortbyfields){
				let sortbyField = definition.sortbyfields[index];
				
				sortDirectionArray.push(sortDirection);
				sortFunctionArray.push(
					record => {
						if (typeof record[sortbyField] === 'string'){
							// make lowercase to have proper string sorting
							return record[sortbyField].toLowerCase();
						}
						
						return record[sortbyField];
						
					}
				);
			};
			
			definition.data = _.orderBy(definition.data, sortFunctionArray, sortDirectionArray);
		}
		 
	 }
	 
	 /********************************************
	  * Returns a html string 
	  ********************************************/
	 render(renderDefinition){
		 var definition = Object.assign({}, this.defaultRenderDefinition, renderDefinition);	
		 
		 this.prepareDefinition(definition);
		 
		 return this.renderFunction(definition);
	 }
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_common_createDefaultPopupTable(entry, renderDef){
	
	//-------------------------
	// Create render definition
	var definition = Object.assign({}, renderDef);
	definition.data = entry;
	
	if(definition.rendererSettings.table == null){
		definition.rendererSettings.table = {};
	}
	definition.rendererSettings.table.verticalize = true;
	definition.rendererSettings.table.narrow = true;
	definition.rendererSettings.table.striped = true;
	definition.rendererSettings.table.filter = false;
		
	//-------------------------
	//remove alertstyle and textstyle
	var visiblefields = Object.keys(definition.data);
	visiblefields = _.without(visiblefields, definition.bgstylefield, definition.textstylefield);
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
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// define if the JSON should be highlighted, can freeze browser on large JSON string
		highlight: false,
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.csv);
	
	//-----------------------------------
	// Create JSON 
	var randomID = CFW.utils.randomString(16);
	var wrapperDiv = $('<div class="flex-grow-1">');
	wrapperDiv.append('<pre class="card p-3 maxvh-80 overflow-auto" id="json-'+randomID+'"><code>'+JSON.stringify(renderDef.data, null, 2)+'</code></pre>');
	
	//-----------------------------------
	// highlight
	if(settings.highlight){
		wrapperDiv.append('<script>hljs.highlightBlock($("#json-'+randomID+'").get(0));</script>');
	}
	
	return wrapperDiv;
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
		// customizers used for customizing CSV values. Do only return text.
		csvcustomizers: {}
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.csv);
	
	//-----------------------------------
	// Target Element
	let pre = $('<pre class="card p-3 maxvh-80 overflow-auto" ondblclick="CFW.selection.selectElementContent(this)">');
	//let pre = $('<pre>');
	pre.append(pre);
	
	//-----------------------------------
	// Headers
	let headers = '';
	for(var key in renderDef.visiblefields){
		var fieldname = renderDef.visiblefields[key];
		
		headers += '"' +renderDef.labels[fieldname] + '"' + settings.delimiter;
	}
	// remove last semicolon
	headers = headers.substring(0, headers.length-1);
	pre.append(headers+"\r\n");
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		let currentRecord = renderDef.data[i];
		let recordCSV = "";
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			
			// do not use normal customized values as it might return html
			var value = currentRecord[fieldname];
			if(settings.csvcustomizers[fieldname] != undefined){
				value = settings.csvcustomizers[fieldname](currentRecord, value, CFW_RENDER_NAME_CSV);
			}
			
			if(value == null){
				value = "";
			}else{
				if(typeof value === "object" ){
				value = JSON.stringify(value);
				}else{

					value = (""+value).replaceAll('\n', '\\n')
									  .replaceAll('\r', '\\r')
									  .replaceAll('<', '&lt;')
									  .replaceAll('"', '""');
				}
			}
			
			recordCSV += '"' + value + '"' + settings.delimiter;
		}
		// remove last semicolon
		recordCSV = recordCSV.substring(0, recordCSV.length-1);
		
		//=====================================
		// Create Colored span
		let span = $('<span>');
		span.html(recordCSV+'</span>\r\n')
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, span, "bg");
		renderDef.colorizeElement(currentRecord, span, "text");
		
		pre.append(span);
	}

	return pre;
}
CFW.render.registerRenderer(CFW_RENDER_NAME_CSV,  new CFWRenderer(cfw_renderer_csv));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_title(renderDef) {
		 
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// this renderer has no additional settings
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.title);
	
	//-----------------------------------
	// Target Element
	let resultDiv = $('<div class="w-100 h-100">');
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		renderDef.visiblefields
		let currentRecord = renderDef.data[i];
		let recordTitle = renderDef.getTitleHTML(currentRecord);;

		//=====================================
		// Create Colored span
		let wrapper = $('<div>');
		wrapper.html(recordTitle)
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, wrapper, "bg");
		renderDef.colorizeElement(currentRecord, wrapper, "text");
		
		resultDiv.append(wrapper);
	}

	return resultDiv;
}
CFW.render.registerRenderer(CFW_RENDER_NAME_TITLE,  new CFWRenderer(cfw_renderer_title));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_xml(renderDef) {
		 
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// customizers used for customizing XML values. Do only return text.
		xmlcustomizers: {}
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.xml);
	
	//-----------------------------------
	// Target Element
	
	let pre = $('<pre class="card p-3 maxvh-80 overflow-auto" ondblclick="CFW.selection.selectElementContent(this)">');
	//let pre = $('<pre>');
	pre.append(pre);
	
	//-----------------------------------
	// Start
	
	let html = "<data>\r\n";
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		let currentRecord = renderDef.data[i];
		let record = "<record>\r\n";
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			
			// do not use normal customized values as it might return html
			var value = currentRecord[fieldname];
			if(settings.xmlcustomizers[fieldname] != undefined){
				value = settings.xmlcustomizers[fieldname](currentRecord, value, CFW_RENDER_NAME_XML);
			}
			
			
			if(value == null){
				value = "";
			}else{
				if(typeof value === "object" ){
				value = JSON.stringify(value);
				}else{

					value = (""+value).replaceAll('<', '&lt;');
				}
			}
			
			record += `\t<${fieldname}>${value}</${fieldname}>\r\n`;
		}
		
		record += "</record>\r\n";
		html += record;
	}
	
	html += "</data>\r\n";

	pre.text(html);
	return pre;
}
CFW.render.registerRenderer(CFW_RENDER_NAME_XML,  new CFWRenderer(cfw_renderer_xml));

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
		// if show labels is false, and only one tile is rendered, expand the tile to 100% height and with if this is set to true 
		expandsingle: false,
		// show a popover with details about the data when hovering a tile
		popover: true,
		// The function(record, renderDef) used to create the popover and details modal
		popoverFunction: cfw_renderer_common_createDefaultPopupTable
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.tiles);

	//===================================================
	// Create Alert Tiles
	//===================================================
	var allTiles = $('<div class="d-flex flex-row flex-grow-1 flex-wrap">');
	var totalRecords = renderDef.data.length;
	if(totalRecords == 1){
		allTiles.addClass('flex-column h-100');
	}else{
		allTiles.addClass('flex-row ');
	}
	
	if(settings.showlabels == true || settings.showlabels == "true" || totalRecords == 1){
		allTiles.addClass('h-100');
	}else{
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
		renderDef.colorizeElement(currentRecord, currentTile, "bg");
		renderDef.colorizeElement(currentRecord, currentTile, "text");
		
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
		currentTile.data('record', currentRecord);
		currentTile.bind('click', function(e) {
			//e.stopPropagation();
			// make sure popover gets closed
			$(this).popover("hide");
		
			recordData = $(this).data('record');
			cfw_ui_showModal(
					CFWL('cfw_core_details', 'Details'),
					settings.popoverFunction(recordData, renderDef)
				);
		})
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction(currentRecord, renderDef);
			currentTile.popover(popoverSettings);
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
					var customizedValue = customizer(currentRecord, value, CFW_RENDER_NAME_TILES, fieldname);
					if(customizedValue != null){
				
						var span = $('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.labels[fieldname]+':&nbsp;</strong></span>');
						span.append(customizedValue);
						currentTile.append(span);
					}
				}
			}
		} else {
			currentTile.css('margin', "0rem 0.125rem 0.25rem 0.125rem");
			if(!settings.expandsingle || totalRecords > 1){
				currentTile.css('width', 50*settings.sizefactor+"px");
				currentTile.css('height', 50*settings.sizefactor+"px");
			}else{
				currentTile.addClass('h-100');
			}
		}

		allTiles.append(currentTile);
	}
	
	return allTiles;

}

CFW.render.registerRenderer(CFW_RENDER_NAME_TILES, new CFWRenderer(cfw_renderer_tiles));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_statusbar(renderDef, reverseOrder) {
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into statusbar.</span>";
	}
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		//height for the status bar
		height: "100%",
		// min height for the statusbar
		minheight: "10px",
		// define if the order of the items should be reversed
		reverse: reverseOrder,
		// show a popover with details about the data when hovering a tile
		popover: true,
		// The function(record, renderDef) used to create the popover and details modal
		popoverFunction: cfw_renderer_common_createDefaultPopupTable
		
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.statusbar);

	//===================================================
	// Create Tiles for Bar
	//===================================================
	
	var reverseSuffix = "";
	if(settings.reverse){
		reverseSuffix = "-reverse";
	}
	var allTiles = $('<div class="d-flex flex-row'+reverseSuffix+' flex-grow-1 align-items-start"></div>');
	//allTiles.addClass('h-100');
	allTiles.css('height', settings.height);
	allTiles.css('font-size', "0px"); //allow tiles to be very small
				
	for(var i = 0; i < renderDef.data.length; i++ ){
		var currentRecord = renderDef.data[i];
		var currentTile = $('<div class="p-0 flex-fill">&nbsp;</div>');
		currentTile.css('height', "100%");
		currentTile.css('min-height', settings.minheight);
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, currentTile, "bg");
		
		//=====================================
		// Add Details Click
		currentTile.data('record', currentRecord)
		currentTile.bind('click', function(e) {
			//e.stopPropagation();		
			// make sure to hide popovers
			$(this).popover('hide');
			
			recordData = $(this).data('record');
			cfw_ui_showModal(
					CFWL('cfw_core_details', 'Details'), 
					settings.popoverFunction(recordData, renderDef))
			;
		})
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction(currentRecord, renderDef);
			currentTile.popover(popoverSettings);
		}

		allTiles.append(currentTile);
	}
	
	return allTiles;

}

function cfw_renderer_statusbar_reverse(renderDef, reverseOrder) {
	return cfw_renderer_statusbar(renderDef, true);
}

CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSBAR, new CFWRenderer(cfw_renderer_statusbar));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSBAR_REVERSE, new CFWRenderer(cfw_renderer_statusbar_reverse));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_statusmap(renderDef, widthfactor, heightfactor) {
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into status map.</span>";
	}
	
	if(renderDef.data.length == 0){
		return "&nbsp;";
	}
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// aspect ratio factor for the width. Used to calculate number of columns in the map.
		widthfactor: widthfactor,
		// aspect ratio factor for the height. Used to calculate number of columns in the map.
		heightfactor: heightfactor,
		// show a popover with details about the data when hovering a tile
		popover: true,
		// The function(record, renderDef) used to create the popover and details modal content
		popoverFunction: cfw_renderer_common_createDefaultPopupTable
		
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.statusmap);
	
	//===================================================
	// Calculate Aspect Ratio
	//===================================================
	var aspectRatio = settings.widthfactor / settings.heightfactor;
	var listenOnResize = false;

	if(settings.widthfactor == -1 || settings.heightfactor == -1){
		aspectRatio = 1;
		listenOnResize = true;
	}
	// landscape example: 2 / 1 = 2.0
	// portrait example:  1 / 2 = 0.5
		
	//===================================================
	// Create Tiles for Map
	//===================================================
	var allTiles = $('<div class="d-flex flex-column flex-grow-1 h-100"></div>');
	allTiles.css("font-size", "0px");
	// avoid aspect ratio being Infinity by setting a min size
	allTiles.css("min-height", "10px");
	allTiles.css("min-width", "10px");	
	//===================================================
	// Add Resize Observer
	//===================================================
	if(!listenOnResize){
		cfw_renderer_statusmap_createTiles(renderDef, settings, allTiles, aspectRatio);
	}else{
		let timerID = 'redrawthrottle'+CFW.utils.randomString(12);
		var resizeObserver = new ResizeObserver(function(e){
			
			//-------------------------------------
			// Throttle: Check 100ms passed since last redraw
			var currentMillis = Date.now();
			
			var lastMillis = CFW.cache.data[timerID];
	
			if(lastMillis != null && (currentMillis - lastMillis) < 1000){
				
				return;
			}
			CFW.cache.data[timerID] = currentMillis;
			
			//-------------------------------------
			// Redraw
			var width = allTiles.width();
			var height = allTiles.height();
			var newAspectRatio = width/height;
			
			if(newAspectRatio == Infinity){
				newAspectRatio = 1;
			}
			allTiles.html('');
			
			resizeObserver.disconnect();
			   
				cfw_renderer_statusmap_createTiles(renderDef, settings, allTiles, newAspectRatio);
			
			resizeObserver.observe(allTiles.get(0));
		});
		
		resizeObserver.observe(allTiles.get(0));
	}

	return allTiles;

}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_statusmap_createTiles(renderDef, settings, target, aspectRatio) {
	
	
	//===================================================
	// Precheck: NaN would cause OutOfMemory
	//===================================================
	if(isNaN(aspectRatio) || aspectRatio <= 0){
		return;
	}
	//===================================================
	// Calculate Number of Columns
	//===================================================
	var itemCount = renderDef.data.length;
	
	var numberColumns = 1;
	var currentRatio = 0;
	for(;currentRatio < aspectRatio; numberColumns++ ){
		currentRatio = numberColumns / (itemCount / numberColumns);
	}
	numberColumns--;
	
	//===================================================
	// Create Tiles
	//===================================================
	var currentRow = $('<div class="d-flex flex-row flex-grow-1 w-100"></div>');

	target.append(currentRow);
	
	for(var i = 1; i <= itemCount || ((i-1) % numberColumns) != 0 ; i++ ){

		var currentTile = $('<div class="p-0 flex-fill">&nbsp;</div>');
		currentTile.css('height', "100%");
		currentRow.append(currentTile);

		//=====================================
		// Check has more records, else empty tile
		if(i > renderDef.data.length){
			//make same size to have proper alignment
			currentTile.addClass("border-cfw-invisible");
			
			continue;
		}
		
		var currentRecord = renderDef.data[i-1];
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, currentTile, "bg");
		currentTile.addClass("border-cfw-overlay");
		
		//=====================================
		// Add Details Click
		currentTile.data('record', currentRecord)
		currentTile.bind('click', function(e) {
			e.stopPropagation();
			recordData = $(this).data('record');
			cfw_ui_showModal(
					CFWL('cfw_core_details', 'Details'), 
					settings.popoverFunction(recordData, renderDef))
			;
		})
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction(currentRecord, renderDef);
			currentTile.popover(popoverSettings);
		}
	
		//if current row has reached numberColumns create new row, except if it was the last item
		if( (i % numberColumns) == 0 && i < itemCount){
			currentRow = $('<div class="d-flex flex-row flex-grow-1 w-100"></div>');
			target.append(currentRow);
		}
	}
}

function cfw_renderer_statusmap_auto(renderDef){ return cfw_renderer_statusmap(renderDef, -1, -1); }
function cfw_renderer_statusmap_1to1(renderDef){ return cfw_renderer_statusmap(renderDef, 1, 1); }
function cfw_renderer_statusmap_2to1(renderDef){ return cfw_renderer_statusmap(renderDef, 2, 1); }
function cfw_renderer_statusmap_4to1(renderDef){ return cfw_renderer_statusmap(renderDef, 4, 1); }
function cfw_renderer_statusmap_8to1(renderDef){ return cfw_renderer_statusmap(renderDef, 8, 1); }
function cfw_renderer_statusmap_1to2(renderDef){ return cfw_renderer_statusmap(renderDef, 1, 2); }
function cfw_renderer_statusmap_1to4(renderDef){ return cfw_renderer_statusmap(renderDef, 1, 4); }
function cfw_renderer_statusmap_1to8(renderDef){ return cfw_renderer_statusmap(renderDef, 1, 8); }

CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP, new CFWRenderer(cfw_renderer_statusmap_auto));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_1to1, new CFWRenderer(cfw_renderer_statusmap_1to1));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_2to1, new CFWRenderer(cfw_renderer_statusmap_2to1));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_4to1, new CFWRenderer(cfw_renderer_statusmap_4to1));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_8to1, new CFWRenderer(cfw_renderer_statusmap_8to1));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_1to2, new CFWRenderer(cfw_renderer_statusmap_1to2));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_1to4, new CFWRenderer(cfw_renderer_statusmap_1to4));
CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP_1to8, new CFWRenderer(cfw_renderer_statusmap_1to8));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_statuslist(renderDef) {
	
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
		popover: true,
		// The function(record, renderDef) used to create the popover and details modal
		popoverFunction: cfw_renderer_common_createDefaultPopupTable
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.statuslist);

	//===================================================
	// Create Alert Tiles
	//===================================================
	var allTiles = $('<div class="d-flex flex-column flex-wrap h-100">');

				
	for(var i = 0; i < renderDef.data.length; i++ ){
		var currentRecord = renderDef.data[i];
		var currentListItem = $('<div class="d-flex flex-row align-items-center">')
		var currentTile = $('<div>');
		
		currentTile.css('width', 20*settings.sizefactor+"px");
		currentTile.css('height', 20*settings.sizefactor+"px");
		
		currentListItem.append(currentTile);
		//=====================================
		// Add padding
		if(settings.sizefactor <= 1.0)		{ currentListItem.addClass('pb-1 pl-3'); }
		else if(settings.sizefactor <= 1.5)	{ currentListItem.addClass('pb-2 pl-3'); }
		else if(settings.sizefactor <= 2.5)	{ currentListItem.addClass('pb-3 pl-3'); }
		else 								{ currentListItem.addClass('pb-4 pl-4'); }
		
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, currentTile, "bg");
		renderDef.colorizeElement(currentRecord, currentTile, "text");
		
		if(settings.border != null){
			currentTile.css('border', settings.border);
		}
		
		if(settings.borderstyle != null){
			var baseradius = 3;
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
		currentTile.data('record', currentRecord);
		currentTile.bind('click', function(e) {
			e.stopPropagation();
			recordData = $(this).data('record');
			cfw_ui_showModal(
					CFWL('cfw_core_details', 'Details'), 
					settings.popoverFunction(recordData, renderDef))
			;
		})
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction(currentRecord, renderDef);
			currentTile.popover(popoverSettings);
		}
		
		//=====================================
		// Create List Item Label
		var recordTitle = renderDef.getTitleHTML(currentRecord);

		if(settings.showlabels){
			currentListItem.append('<span style="font-size: '+18*settings.sizefactor+'px;">&nbsp;'+recordTitle+'</span>');
		}
		allTiles.append(currentListItem);
	}
	
	return allTiles;

}

CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSLIST, new CFWRenderer(cfw_renderer_statuslist));

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
				finalValue = customizer(singleRecord, finalValue, CFW_RENDER_NAME_TABLE, fieldname);
			}
			else if(renderDef.customizers['value'] != null){
				let customizer = renderDef.customizers['value'];
				finalValue = customizer(singleRecord, finalValue, CFW_RENDER_NAME_TABLE, fieldname);
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
		cfw_renderer_table_addRow(cfwTable, currentRecord, renderDef, settings, selectorGroupClass, 0);
	}
	//----------------------------------
	// If narrow make buttons smaller
	var wrappedTable = cfwTable.getTable();
	if(settings.narrow){
		wrappedTable.find('.btn-sm')
			.addClass('btn-xs')
			.removeClass('btn-sm');
		
	}
	
	//----------------------------------
	// Create multi buttons
	if(renderDef.bulkActions == null){
		return wrappedTable;
	}else{

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
			wrappedTable.prepend(actionsDivTop);
		}
		if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
			wrappedTable.append(actionsDivBottom);
		}
		
		return wrappedTable;
	}
}
CFW.render.registerRenderer(CFW_RENDER_NAME_TABLE, new CFWRenderer(cfw_renderer_table) );

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_table_addRow(targetTable, currentRecord, renderDef, settings, selectorGroupClass, hierarchyDepth){

	let row = $('<tr class="cfwRecordContainer">');
	
	//-------------------------
	// Add Styles
	renderDef.colorizeElement(currentRecord, row, "table");
	renderDef.colorizeElement(currentRecord, row, "text");
	
	// Set text to white if bg is set but textcolor not 
	if(renderDef.textstylefield != null 
	&& renderDef.bgstylefield != null 
	&& currentRecord[renderDef.textstylefield] == null
	&& currentRecord[renderDef.bgstylefield] != null
	&& currentRecord[renderDef.bgstylefield] != "cfw-none"){
		row.addClass('text-white');
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
			}else {
				
				//---------------------------------------
				// Create Cell, add indentation on title fields if neccessary
				if(value == null){ value = "&nbsp;"}
						
				if(!renderDef.hierarchy || !renderDef.titlefields.includes(fieldname)){
					cellHTML += '<td>'+value+'</td>';
				}else{
					cellHTML += '<td><div style="padding-left: '+renderDef.hierarchyIndentation*hierarchyDepth+'px;">'
						+ '<i class="'+renderDef.hierarchyIconClasses+'"></i>'
						+ value
					+'</div></td>';
				}
			}

		}else{
			//add all cells created up until now
			row.append(cellHTML);
			cellHTML = "";
			let customizer = renderDef.customizers[fieldname];
			let customizedValue = customizer(currentRecord, value, CFW_RENDER_NAME_TABLE, fieldname);

			//---------------------------------------
			// Create Cell, add indentation on title fields if neccessary
			let cell = $('<td>');
			if(!renderDef.hierarchy || !renderDef.titlefields.includes(fieldname)){
				cell.append(customizedValue);
			}else{
				let indendationDiv = $('<div style="padding-left: '+renderDef.hierarchyIndentation*hierarchyDepth+'px;">');
				indendationDiv.append(customizedValue);
				cell.append(indendationDiv);
			}
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
	
	//-------------------------
	// Print Row
	targetTable.addRow(row);
	
	//-------------------------
	// Print Children
	if(renderDef.hierarchy){
		var children = currentRecord[renderDef.hierarchyChildrenField];
		if(children != null){
			for(var index in children){
				let currentChild = children[index];
				let depth = (renderDef.hierarchyAsTree) ?  hierarchyDepth+1 :  0;
				cfw_renderer_table_addRow(targetTable, currentChild, renderDef, settings, selectorGroupClass, depth);
			}
		}
	}
	
}
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
		
		let currentRecord = renderDef.data[i];

		cfw_renderer_panels_addPanel(wrapper, currentRecord, renderDef, settings, selectorGroupClass);

	}
	
	//----------------------------------
	// Make buttons smaller
	wrapper.find('.btn-sm')
		.addClass('btn-xs')
		.removeClass('btn-sm');
	
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
function cfw_renderer_panels_addPanel(targetElement, currentRecord, renderDef, settings, selectorGroupClass){
	//---------------------------
	// Preprarations
	
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
		
		//do not toggle panel collapse when clicking checkbox 
		checkbox.on('click', function(e){
			    e.stopPropagation();
			});
		checkboxDiv.append(checkbox);
		
		panelSettings.title.prepend(checkboxDiv);
	}
	
	//-------------------------
	// Add field Values as Unordered List
	let list = $("<ul>");
	for(let key in renderDef.visiblefields){
		let fieldname = renderDef.visiblefields[key];
		let value = renderDef.getCustomizedValue(currentRecord,fieldname, CFW_RENDER_NAME_PANELS);
		
		if(!CFW.utils.isNullOrEmpty(value)){
			item = $('<li><strong>' + renderDef.labels[fieldname] + ':&nbsp;</strong></li>');
			item.append(value);
			list.append(item);
		}
	}
	
	
	panelSettings.body = list;
			
	//-------------------------
	// Add Panel to Wrapper
	var cfwPanel = new CFWPanel(panelSettings);
	cfwPanel.appendTo(targetElement);
	
	//-------------------------
	// Print Children
	if(renderDef.hierarchy){
		var children = currentRecord[renderDef.hierarchyChildrenField];
		if(children != null){
			for(var index in children){
				let currentChild = children[index];
				if(renderDef.hierarchyAsTree){
					cfw_renderer_panels_addPanel(cfwPanel.getPanelBody(), currentChild, renderDef, settings, selectorGroupClass);
				}else{
					cfw_renderer_panels_addPanel(targetElement, currentChild, renderDef, settings, selectorGroupClass);
				}
			}
		}
	}
}

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
	// Create Cards
	//===================================================
	var wrapper = $("<div class='flex-grow-1'>");
	
	var selectorGroupClass = "card-checkboxes-"+CFW.utils.randomString(16);
	
	//-----------------------------------
	// Print Records
	var row = $('<div class="row m-0">');
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
		for(let key in renderDef.visiblefields){
			let fieldname = renderDef.visiblefields[key];
			let value = renderDef.getCustomizedValue(currentRecord,fieldname, CFW_RENDER_NAME_PANELS);
						
			if(!CFW.utils.isNullOrEmpty(value)){
				item = $('<li><strong>' + renderDef.labels[fieldname] + ':&nbsp;</strong></li>');
				item.append(value);
				list.append(item);
			}
		}
		
		cardSettings.body = list;
				
		//-------------------------
		// Add Card to Row
		var cfwCard = new CFWCard(cardSettings);
		row.append(cfwCard);
		//new row
		if( i > 0 && (i % settings.maxcolumns) == 0){
			wrapper.append(row);
			row = $('<div class="row m-0">');
		}
		var column = $('<div class="col-sm pr-0 mr-0">');
		
		cfwCard.appendTo(column);
		row.append(column);
		
		//-------------------------
		// Add Last Row 
		if(i == renderDef.data.length-1){
			wrapper.append(row);
		}
	}
	
	//----------------------------------
	// Make buttons smaller
	wrapper.find('.btn-sm')
		.addClass('btn-xs')
		.removeClass('btn-sm');
		
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
 * datamode: 'datasets' (experimental)
 * ------------------------
 * This mode uses the datasets format of ChartJS.
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
		// if true, connect lines if there is a gap in the data 
		spangaps: false,
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
		// the color of the x-axes grid lines
		xaxescolor: 'rgba(128,128,128, 0.2)',
		// the color of the y-axes grid lines
		yaxescolor: 'rgba(128,128,128, 0.8)',
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

		if(renderDef.titlefields != null){
			for(let index in renderDef.titlefields){
				let titlefield = renderDef.titlefields[index];
				let label = renderDef.labels[titlefield];
				renderDef.titleformat += label+'="{'+index+'}" / ';
				index++;
			}
		}else{
			let index = 0;
			for(let key in firstRecord){
				if(key == settings.xfield || key == settings.yfield) { continue; }
				let label = renderDef.labels[key];
				renderDef.titleformat += label+'="{'+index+'}" / ';
				index++;
			}
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
	}else if(settings.datamode == 'datasets'){
		datasets = cfw_renderer_chart_prepareDatasets(renderDef, settings); 
	}
	
	//========================================
	// sort by x to avoid displaying issues
	for(i in datasets){
		
		datasets[i].data = _.sortBy(datasets[i].data, ['x']);
	}
	
	//========================================
	// Create ChartJS Data Object
	var data = {};
	if(settings.charttype != 'radar'){
		data.datasets = [];
		
		for(label in datasets){
			data.datasets.push(datasets[label]);
		}
	}
	/*else{
		data.labels = []
		data.datasets = [{data: []}];
		
		for(label in datasets){
			data.labels.push(label)
			data.datasets[0].data.push(datasets[label].cfwSum / datasets[label].cfwCount);
		}

	}*/
	
	
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
						color: settings.xaxescolor
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
						color: settings.yaxescolor
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
			var borderColor = CFW.colors.randomSL(hue,65,100,55,70);
			var bgColor = borderColor.replace('1.0)', '0.65)');
			datasets[label] = {
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
				};
			
		}
		
		var value = currentRecord[settings.yfield];
		
		if(settings.xfield == null){
			datasets[label].data.push(value);
			//datasets[label].cfwSum += isNaN(value) ? 0 : parseFloat(value);
			//datasets[label].cfwCount += 1;
		}else{
			
			if(currentRecord[settings.xfield] != null){
				datasets[label].data.push({
					x: currentRecord[settings.xfield], 
					y: value
				});
			}
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
			var borderColor = CFW.colors.randomSL(hue,65,100,55,70);
			var bgColor = borderColor.replace('1.0)', '0.65)');
			datasets[label] = {
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
				};
			
		}
		
		var yArray = currentRecord[settings.yfield];
		
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
		
		if( CFW.utils.isNullOrEmpty(currentDataset.label) ){			currentDataset.label = renderDef.getTitleString(currentRecord); }
		
		currentDataset.backgroundColor = bgColor; 
		currentDataset.borderColor = borderColor; 
		currentDataset.borderWidth = 1;
		
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
			// The index of the initial renderer. Default is -1, takes first one in the list or last selected by user if storeid is specified. 
			rendererIndex: -1,
			// The initial page to be drawn.
			initialpage: 1,
			// The number of items options for the page size selector. 
			sizes: [10, 25, 50, 100, 200, 500, 1000, 10000],
			// The size selected by default
			defaultsize: 50,
			// if a store ID is provided, the settings will be saved and restored when refreshing the viewer or the page.
			storeid: null,
			// enable sorting. 
			sortable: true,
			// array of the fields available for sorting, if null or empty array, renderDefinition.visiblefields will be used
			sortfields: null,
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
				//The param name for the field used to sort the results
				sortbyparam: 'sortby',
				//The param name for the sort direction (will be either 'asc' or 'desc')
				sortascendingparam: 'isascending',
				//The filter string used for filtering the results
				filterqueryparam: 'filterquery',
				//custom parameters which should be added to the request, e.g. {"paramname": "value", "param2": "anothervalue", ...}
				customparams: {},
				//The name of the field containing the total number of rows
				totalrowsfield: 'TOTAL_RECORDS',
				//a function to pre-process the data: function(data), data will be an array of records
				preprocess: null
			},
	};
	
	//var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.dataviewer);
	var settings = _.merge({}, defaultSettings, renderDef.rendererSettings.dataviewer);	
	
	let dataviewerID = "dataviewer-"+CFW.utils.randomString(12);
	let dataviewerDiv = $('<div class="cfw-dataviewer" id="'+dataviewerID+'">');
	
	dataviewerDiv.data('renderDef', renderDef);
	dataviewerDiv.data('settings', settings);
	
	
	dataviewerDiv.append(cfw_renderer_dataviewer_createMenuHTML(dataviewerID, renderDef, settings, settings.rendererIndex) );
	
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
	// settings fir dataviewer
	var settings = dataviewerDiv.data('settings');
	
	// create params object to have everything together and can be passed to other functions
	var params = {
		dataviewerDiv: dataviewerDiv
		,settingsDiv: settingsDiv
		,targetDiv: targetDiv
		,dataviewerID: dataviewerID
		,renderDef: renderDef
		,settings: settings
	}
	
	//=====================================================
	// Get finalRenderDef and apply adjustments to dataviewer
	// settings
	cfw_renderer_dataviewer_resolveSelectedRendererDetails(params);
	
	if(params.finalRenderDef.rendererSettings != null
	&& params.finalRenderDef.rendererSettings.dataviewer != null){
		settings = _.merge({}, params.settings, params.finalRenderDef.rendererSettings.dataviewer);
		params.settings = settings;
	}
	
	if(settings.sortable){
		settingsDiv.find('select[name="sortby"]').parent().removeClass("d-none");
	}else{
		settingsDiv.find('select[name="sortby"]').parent().addClass("d-none");
	}
		
	//=====================================================
	// Handle Page to Render
	if(pageToRender == null || pageToRender == undefined){
		pageToRender = dataviewerDiv.data('currentpage');
		if(pageToRender == null || pageToRender == undefined){
			pageToRender = 1;
		}
	}
	params.pageToRender = pageToRender;
	
	dataviewerDiv.data('currentpage', pageToRender);

	//=====================================================
	// Get Settings
	var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	var filterquery = settingsDiv.find('input[name="filterquery"]').val();
	var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
	var sortbyField = settingsDiv.find('select[name="sortby"]').val();
	var sortbyDirection = settingsDiv.find('select[name="sortby"]').find('option:selected').data('direction');
	
	if(pageSize != null){
		pageSize = parseInt(pageSize);
	}
	var offset = (pageSize > 0 ) ? pageSize * (pageToRender-1): 0;
		
	if(settings.storeid != null){
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][pageSize]', pageSize);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][filterquery]', filterquery);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][rendererIndex]', rendererIndex);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][sortbyField]', sortbyField);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][sortbyDirection]', sortbyDirection);
	}
	
	// Add to params
	params.pageSize = pageSize;
	params.filterquery = filterquery;
	params.rendererIndex = rendererIndex;
	params.sortbyField = sortbyField;
	params.sortbyDirection = sortbyDirection;
	params.offset = offset;
	
	//=====================================================
	// Handle Filter Highlight
	if(CFW.utils.isNullOrEmpty(filterquery)){
		settingsDiv.find('input[name="filterquery"]').removeClass('bg-cfw-yellow');
	}else{
		settingsDiv.find('input[name="filterquery"]').addClass('bg-cfw-yellow');
	}
	
	//=====================================================
	// Create sorting function
	
	//default to "asc" if undefined
	let sortDirectionArray = (sortbyDirection == null) ? ['asc'] : [sortbyDirection];

	let sortFunctionArray = [
		record => {
			
			if (typeof record[sortbyField] === 'string'){
				// make lowercase to have proper string sorting
				return record[sortbyField].toLowerCase();
			}
			
			return record[sortbyField];
			
		}
	];

	
	//=====================================================
	// Get Render Results
	if(settings.datainterface.url == null){

		//-------------------------------------
		// Static 
		if(CFW.utils.isNullOrEmpty(filterquery)){
			
			//---------------------------------
			// Sort
			let sortedData = renderDef.data;
			if(settings.sortable && sortbyField != null){
				sortedData = _.orderBy(sortedData, sortFunctionArray, sortDirectionArray);
			}
			
			//---------------------------------
			// Pagination
			let totalRecords = sortedData.length;
			let dataToRender = _.slice(sortedData, offset, offset+pageSize);
			if(pageSize == -1){
				dataToRender = sortedData;
			}
			params.totalRecords = totalRecords;
			params.dataToRender = dataToRender;
			cfw_renderer_dataviewer_renderPage(params);
		}else{
			
			//---------------------------------
			// Filter
			filterquery = filterquery.toLowerCase();
			let filteredData = _.filter(renderDef.data, function(o) { 
				    return JSON.stringify(o).toLowerCase().includes(filterquery); 
			});
			
			//---------------------------------
			// Sort
			let sortedData = filteredData;
			if(settings.sortable && sortbyField != null){
				sortedData = _.orderBy(sortedData, sortFunctionArray, sortDirectionArray);
			}
			
			//---------------------------------
			// Pagination
			let totalRecords = sortedData.length;
			let dataToRender = _.slice(sortedData, offset, offset+pageSize);
			if(pageSize == -1){
				dataToRender = sortedData;
			}	
			params.totalRecords = totalRecords;
			params.dataToRender = dataToRender;
			cfw_renderer_dataviewer_renderPage(params);
		}
	}else{
		
		//-------------------------------------
		// Dynamic
		let httpParams = {};
		httpParams[settings.datainterface.actionparam] = "fetchpartial";
		httpParams[settings.datainterface.sizeparam] = pageSize;
		httpParams[settings.datainterface.pageparam] = pageToRender;
		httpParams[settings.datainterface.filterqueryparam] = filterquery;
		httpParams[settings.datainterface.itemparam] = settings.datainterface.item;
		httpParams[settings.datainterface.sortbyparam] = sortbyField;
		httpParams[settings.datainterface.sortascendingparam] = (sortbyDirection == 'desc') ? false : true;

		for(key in settings.datainterface.customparams){
			httpParams[key] = settings.datainterface.customparams[key];
		}
		
		CFW.http.getJSON(settings.datainterface.url, httpParams, function(data){
			
			if(data.payload != null){
				let dataToRender = data.payload;
				let totalRecords = (dataToRender.length > 0) ? dataToRender[0][settings.datainterface.totalrowsfield] : 0;
				
				//----------------------------------
				// Call preprocess function
				if(settings.datainterface.preprocess != null){
					settings.datainterface.preprocess(dataToRender);
				}
				params.totalRecords = totalRecords;
				params.dataToRender = dataToRender;
				cfw_renderer_dataviewer_renderPage(params);
			}
		}
	);
	}
			
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_renderPage(params) {
	
	params.dataviewerDiv.data('visibledata', params.dataToRender);
	//-------------------------------------
	// Initialize
	//var dataviewerDiv = $(dataviewerID);
	var settingsDiv = params.settingsDiv;
	var targetDiv = params.targetDiv;
	var dataviewerID = params.dataviewerID;
	var renderDef = params.renderDef;
	var dataviewerSettings = params.settings;
	var pageToRender = params.pageToRender;
	var pageSize = params.pageSize;
	var dataToRender = params.dataToRender;
	var totalRecords = params.totalRecords;
	
	//-------------------------------------
	// Get Settings
	//var totalRecords = dataToRender.length;
	//var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	
	var offset = pageSize * (pageToRender-1);
		
	//-------------------------------------
	// Call Renderer
	params.finalRenderDef.data = dataToRender;
	var renderResult = CFW.render.getRenderer(params.rendererName).render(params.finalRenderDef);
	var renderWrapper = $('<div class="cfw-dataviewer-renderresult d-flex w-100">');
	renderWrapper.append(renderResult);
	//-------------------------------------
	// Create Paginator
	var pageNavigation = cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageToRender);
	

	targetDiv.html('');
	targetDiv.append(pageNavigation);
	targetDiv.append(renderWrapper);
	targetDiv.append(pageNavigation);
}

/******************************************************************
 * Evaluates what the selected renderer is and adds the fields
 * "rendererName" and "finalRenderDef"to the params object passed 
 * to this function.
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_resolveSelectedRendererDetails(params) {
	
	let dataviewerSettings = params.settings;
	let settingsDiv = params.settingsDiv;
	let renderDef = params.renderDef;
	
	//-------------------------------------
	// Prepare Renderer
	let renderDefOverrides = dataviewerSettings.renderers[0].renderdef;
	let rendererName = dataviewerSettings.renderers[0].name;
	if(dataviewerSettings.renderers.length > 1){
		var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
		renderDefOverrides = dataviewerSettings.renderers[rendererIndex].renderdef;
		rendererName = dataviewerSettings.renderers[rendererIndex].name;
	}

	let finalRenderDef = _.assign({}, renderDef, renderDefOverrides);
	
	params.rendererName =  rendererName;
	params.finalRenderDef =	finalRenderDef;
		
}
	
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createMenuHTML(dataviewerID, renderDef, dataviewerSettings, initialRendererIndex) {
	
	//--------------------------------------
	// Initialize Variables
	var onchangeAttribute = ' onchange="cfw_renderer_dataviewer_fireChange(\'#'+dataviewerID+'\', 1)" ';
	var html = '<div class="cfw-dataviewer-settings">';
	
	//--------------------------------------
	// Prepare Settings
	var selectedRendererIndex = 0;
	var selectedSortbyField = null;
	var selectedSortbyDirection = "asc";
	var selectedSize = dataviewerSettings.defaultsize;
	var filterquery = '';
	
	if(dataviewerSettings.storeid != null){
		selectedRendererIndex 	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][rendererIndex]', selectedRendererIndex);
		selectedSortbyField 	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][sortbyField]', selectedSortbyField);
		selectedSortbyDirection	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][sortbyDirection]', selectedSortbyDirection);
		selectedSize 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][pageSize]', selectedSize);
		filterquery 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][filterquery]', filterquery);
	}
	
	if(initialRendererIndex != null && initialRendererIndex > -1){
		selectedRendererIndex = initialRendererIndex;
	}
	
	//--------------------------------------
	// Display As
	if(dataviewerSettings.renderers.length > 1){
		
		html += '<div class="float-right ml-2">'
			+'	<label for="displayas">Display As:&nbsp;</label>'
			+'	<select name="displayas" class="dataviewer-displayas form-control form-control-sm" '+onchangeAttribute+'>'
		
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
	// Sort By
	if(dataviewerSettings.sortable){
		
		let sortfields = dataviewerSettings.sortfields;
		if(sortfields == null || sortfields.length == null){
			sortfields = renderDef.visiblefields;
		}
		html += '<div class="float-right ml-2">'
			+'	<label for="sortby">Sort By:&nbsp;</label>'
			+'	<select name="sortby" class="dataviewer-sortby form-control form-control-sm" '+onchangeAttribute+'>'
		
			let ascendingHTML = ""; 
			let descendingHTML = ""; 
			for(index in sortfields){
				var fieldName = sortfields[index];
				var fielLabel = renderDef.labels[fieldName];
				
				
				var selectedAsc = '';
				var selectedDesc = '';
				if(index == 0 && selectedSortbyField == null){
					selectedAsc = 'selected';
				}else{
					
					if(fieldName == selectedSortbyField){
						if(selectedSortbyDirection == 'desc'){
							selectedDesc = 'selected';
						}else{
							//default
							selectedAsc = 'selected';
						}
					}
				}
						
				ascendingHTML += '<option value="'+fieldName+'" data-direction="asc" '+selectedAsc+'>&uarr; '+fielLabel+'</option>';
				descendingHTML += '<option value="'+fieldName+'" data-direction="desc" '+selectedDesc+'>&darr; '+fielLabel+'</option>';
				
			}
		
		html += ascendingHTML + descendingHTML + '	</select>'
				+'</div>';
	}
	
	//--------------------------------------
	// Page Size
	html += '<div class="float-right ml-2">'
		+'	<label for="pagesize">Page Size:&nbsp;</label>'
		+'	<select name="pagesize" class="dataviewer-pagesize form-control form-control-sm" '+onchangeAttribute+'>'
	
		for(key in dataviewerSettings.sizes){
			var size = dataviewerSettings.sizes[key];
			var selected = (size == selectedSize) ? 'selected' : '';
			
			html += '<option value="'+size+'" '+selected+'>'+size+'</option>';

		}
	
	html += '	</select>'
			+'</div>';
	
	//--------------------------------------
	// Filter Query
	let filterHighlightClass = CFW.utils.isNullOrEmpty(filterquery) ? '' : 'bg-cfw-yellow';

	html += '<div class="float-right  ml-2">'
		+'	<label for="filterquery">Filter:&nbsp;</label>'
		+'	<input type="text" name="filterquery" class="dataviewer-filterquery form-control form-control-sm '+filterHighlightClass+'" value="'+filterquery.replaceAll('"','&quot;')+'" placeholder="Filter..." '+onchangeAttribute+'>'
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
	
	CFW.http.getJSON(CFW_GLOBAL_HIERARCHY_URL, params, 
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
* Changes the position of an item in relation to its siblings.
* 
* @param configID the name of the hierarchy config
* @param childElement the dragged element
* @param moveup true to move up, false to move down
* @return 
******************************************************************/
function cfw_renderer_hierarchysorter_changeChildPosition(configID, childElement, moveup){
	
	var childID = childElement.data('childid');
	
	params = {action: "update", item: "childposition", configid: configID, childid: childID, moveup: moveup};
	
	CFW.http.getJSON(CFW_GLOBAL_HIERARCHY_URL, params, 
		function(data) {
			CFW_GLOBAL_HIERARCHYSORTER.parentUpdateInProgress = false;
		
			//------------------------------------------
			// on success move reflect changes in tree
			if(data.success){
				if(moveup){
					childElement.insertBefore(childElement.prev());
				}else{
					childElement.insertAfter(childElement.next());
				}
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
			/*+ '<div class="cfw-fa-box cursor-pointer" >'
				+ '<i class="fas fa-caret-up mr-2"></i>'
			+ '</div>'
			+ '<div class="cfw-fa-box cursor-pointer" >'
				+ '<i class="fas fa-caret-down mr-2"></i>'
			+ '</div>'*/
			+ '<div class="cfw-fa-box cursor-pointer" role="button" data-toggle="collapse" data-target="#children-'+id+'" aria-expanded="false">'
				+ '<i class="fas fa-chevron-right mr-2"></i>'
				+ '<i class="fas fa-chevron-down mr-2"></i>'
			+ '</div>'
			+renderDef.getTitleHTML(currentItem)
			+'</div>');
	
	//--------------------------------------
	// MoveDown Button
	var moveDownButton = $('<div class="cfw-fa-box cursor-pointer" >'
				+ '<i class="fas fa-caret-down mr-2"></i>'
			+ '</div>')
	
	moveDownButton.on('click', function(e){	
		var itemElement = $(this).parent().parent();
		cfw_renderer_hierarchysorter_changeChildPosition(settings.configid, itemElement, false );
	});
	draggableHeader.prepend(moveDownButton);
		
	//--------------------------------------
	// MoveUp Button
	var moveUpButton = $('<div class="cfw-fa-box cursor-pointer" >'
				+ '<i class="fas fa-caret-up mr-2"></i>'
			+ '</div>')
	
	moveUpButton.on('click', function(e){	
			var itemElement = $(this).parent().parent();
			cfw_renderer_hierarchysorter_changeChildPosition(settings.configid, itemElement, true );
	});
	draggableHeader.prepend(moveUpButton);
	
	
	//--------------------------------------
	// Add Drag Start Listener
	draggableItem.on('dragstart', function(e){
		//-----------------------------
		// get dragged element, if none make the current object as dragged
		var draggable = $('.cfw-draggable.dragging');
		
		if(draggable.length == 0){
			CFW_GLOBAL_HIERARCHYSORTER.oldParent = $(this).parent();
			CFW_GLOBAL_HIERARCHYSORTER.oldPrev = $(this).prev();
			CFW_GLOBAL_HIERARCHYSORTER.notDraggedDroptargets=$('.cfw-draggable:not(.dragging) .cfw-droptarget').toArray();
			CFW_GLOBAL_HIERARCHYSORTER.lastDragoverMillis=Date.now();
			$(this).addClass('dragging');
		}
	});
	
	//--------------------------------------
	// Add Drag End Listener
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
			$(".cfw-droptarget-active").removeClass("cfw-droptarget-active");
			CFW_GLOBAL_HIERARCHYSORTER.notDraggedDroptargets=null;
		}
	});
	
	
	draggableItem.on('dragover', function(e){
		
		e.preventDefault();
		
		//---------------------------------------------------------
		// Major Performance Improvement: do only all 100ms as
		// dragover event is executed a hell lot of times
		//---------------------------------------------------------
		if(Date.now() - CFW_GLOBAL_HIERARCHYSORTER.lastDragoverMillis < 200){
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
    	    if(Date.now() - CFW_GLOBAL_HIERARCHYSORTER.dropTargetChangeMillis > 400){
				
				//-------------------------------------
		    	// add Color	
				$(".cfw-droptarget-active").removeClass("cfw-droptarget-active");
				dropTarget.prev().addClass("cfw-droptarget-active");
				
				//-------------------------------------
		    	// Open panel and prepend dragged 
				dropTarget.append(draggable);	    		
				dropTarget.collapse('show');
	    		
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

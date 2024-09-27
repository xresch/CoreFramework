
CFW_RENDER_NAME_CSV = 'csv';
CFW_RENDER_NAME_JSON = 'json';
CFW_RENDER_NAME_XML = 'xml';
CFW_RENDER_NAME_TITLE = 'title';
CFW_RENDER_NAME_TABLE = 'table';
CFW_RENDER_NAME_TILES = 'tiles';
CFW_RENDER_NAME_TILEANDBAR = 'tileandbar';
CFW_RENDER_NAME_STATUSTILES = 'statustiles';
CFW_RENDER_NAME_STATUSBAR = 'statusbar';
CFW_RENDER_NAME_STATUSBAR_REVERSE = 'statusbarreverse';

CFW_RENDER_NAME_STATUSMAP = 'statusmap';

CFW_RENDER_NAME_STATUSLIST = 'statuslist';
CFW_RENDER_NAME_CARDS = 'cards';
CFW_RENDER_NAME_PANELS = 'panels';
CFW_RENDER_NAME_CHART = 'chart';
CFW_RENDER_NAME_DATAVIEWER = 'dataviewer';

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
		 	// (Optional) names of the fields that are used for a titles. Takes the first field from the first object if null. If empty array, no title
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
			 * Customize The Value
			 *************************************************************/
		 	getLabel: function(fieldname, rendererName){
		 		
			 		if(this.labels[fieldname] == null){
						return fieldname;
					}else if(typeof this.labels[fieldname] == 'string'){
						return this.labels[fieldname];
					}else if(typeof this.labels[fieldname] == 'function'){
						var getLabelFunction = this.labels[fieldname];
						return getLabelFunction(fieldname, rendererName);
					}
			 	},
			
			/*************************************************************
			 * Create Title HTML (uses customized values)
			 *************************************************************/
		 	getTitleHTML:  function(record){
				
				//-------------------------
				// Empty Title
				if( this.titlefields.length == 0){
					return "";
				}
				
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
				// Empty Title
				if( this.titlefields.length == 0 ){
					return "";
				}
				
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
			 if(definition.titlefields == null){ 
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
function cfw_renderer_common_openDetailsTable(e){

	e.stopPropagation();
	
	// well haz to do thiz or they may get stück
	$('.popover').remove();
	
	if($(this).closest('.block-modal').length > 0){
		return;
	}
	
	settings = $(this).data('settings');
	recordData = $(this).data('record');
	
	cfw_ui_showModal(
			CFWL('cfw_core_details', 'Details'), 
			settings.popoverFunction.call(this) )
	;
		
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_common_createDefaultPopupTable(){
	
	var entry = $(this).data('record');
	var renderDef = $(this).data('renderDef');

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
	wrapperDiv.append('<pre id="json-'+randomID
						+'" ondblclick="CFW.selection.selectElementContent(this)"><code>'
							+JSON.stringify(renderDef.data, null, 2)
						+'</code></pre>'
						);
	
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
	let pre = $('<pre ondblclick="CFW.selection.selectElementContent(this)">');
	//let pre = $('<pre>');
	pre.append(pre);
	
	//-----------------------------------
	// Headers
	let headers = '';
	for(var key in renderDef.visiblefields){
		var fieldname = renderDef.visiblefields[key];
		
		headers += '"' +renderDef.getLabel(fieldname, CFW_RENDER_NAME_CSV) + '"' + settings.delimiter;
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

	var wrapperDiv = $('<div class="flex-grow-1">');
	wrapperDiv.append(pre);
	return wrapperDiv;
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
	
	let pre = $('<pre ondblclick="CFW.selection.selectElementContent(this)">');
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
	
	var wrapperDiv = $('<div class="flex-grow-1">');
	wrapperDiv.append(pre);
	return wrapperDiv;
}
CFW.render.registerRenderer(CFW_RENDER_NAME_XML,  new CFWRenderer(cfw_renderer_xml));

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_tiles(renderDef) {
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into tiles.</span>";
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
		currentTile.data('renderDef', renderDef);
		currentTile.data('settings', settings);
		currentTile.bind('click', cfw_renderer_common_openDetailsTable)
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction;
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
						currentTile.append('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.getLabel(fieldname, CFW_RENDER_NAME_TILES)+':&nbsp;</strong>'+value+'</span>');
					}
				}else{
					var customizer = renderDef.customizers[fieldname];
					var customizedValue = customizer(currentRecord, value, CFW_RENDER_NAME_TILES, fieldname);
					if(customizedValue != null){
				
						var span = $('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.getLabel(fieldname, CFW_RENDER_NAME_TILES)+':&nbsp;</strong></span>');
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
 * Shorthand for getting tiles without labels.
 ******************************************************************/
function cfw_renderer_statustiles(renderDef){
	var clonedDef = Object.assign({}, renderDef);
	
	if(clonedDef.rendererSettings.statustiles == undefined){
		if(clonedDef.rendererSettings == undefined){
			clonedDef.rendererSettings = {};
		}
		clonedDef.rendererSettings.statustiles = {};
	} 
	
	clonedDef.rendererSettings.statustiles.showlabels = false;
	clonedDef.rendererSettings.statustiles.popover = true;
	
	clonedDef.rendererSettings.tiles = Object.assign({},
			clonedDef.rendererSettings.tiles, 
			clonedDef.rendererSettings.statustiles
		);
	
	return cfw_renderer_tiles(clonedDef); 
}

CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSTILES, new CFWRenderer(cfw_renderer_statustiles));


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_tileandbar(renderDef) {
	
	//-----------------------------------
	// Check Data
	if(renderDef.datatype != "array"){
		return "<span>Unable to convert data into tile.</span>";
	}
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// Class or color to use for the tile that sums up the status of all monitors
		summarystyle: null,
		// Class or color to use for the text of the tile
		summarytextstyle: 'white',
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.tileandbar);

	//---------------------------------
	// Evaluate summarystyle
	if(settings.summarystyle == null){
		var worstStatusStyle = CFW.style.notevaluated;
		for(var index in renderDef.data){
			var current = renderDef.data[index];
			var alertstyle = current[renderDef.bgstylefield];
			worstStatusStyle = CFW.colors.getThresholdWorse(worstStatusStyle, alertstyle);
		}
		
		settings.summarystyle = worstStatusStyle;
	}

	//---------------------------------
	// Create Grouped Data for Rendering
	var summaryData = {
		items: renderDef.data
	};
	
	summaryData[renderDef.bgstylefield] = settings.summarystyle;
	summaryData[renderDef.textstylefield] = settings.summarytextstyle;
	
	//--------------------------------
	// Adjust Render Definition
	var renderDefClone = _.cloneDeep(renderDef);
	renderDefClone.data = summaryData;

	renderDefClone.rendererSettings.tiles.expandsingle = true;
	renderDefClone.rendererSettings.tiles.popover = true;
	
	if(renderDefClone.rendererSettings.tiles == null){
		renderDefClone.rendererSettings.tiles = {};
	}
	
	renderDefClone.rendererSettings.tiles.showlabels = false;
	
	renderDefClone.customizers.items =  function(record, value) { 
			if(value != null && value != ""){

			var itemsRenderDef = _.cloneDeep(renderDef);
			itemsRenderDef.data = value;
			itemsRenderDef.rendererSettings.tiles.showlabels = false; 
			itemsRenderDef.rendererSettings.tiles.popover = true; 
			itemsRenderDef.rendererSettings.tiles.sizefactor = 0.5; 
			
			return  CFW.render.getRenderer("tiles").render(itemsRenderDef); 
		
			}else {
				return "&nbsp;";
			}
	};
	
	//--------------------------
	// Create Tiles and Status Bar
	var tiles = CFW.render.getRenderer('tiles').render(renderDefClone);
	tiles.removeClass('h-100');
	tiles.css('height', '90%');
	tiles.find('div').css('margin', '0px');
	
	var statusBarData = Object.assign({}, renderDefClone);
	statusBarData.data = renderDef.data;
	var statusbar = CFW.render.getRenderer('statusbar').render(statusBarData);
	statusbar.css('height', '10%');
	
	var tileAndBarWrapper = $('<div class="d-flex-column w-100 h-100">');
	tileAndBarWrapper.append(tiles);
	tileAndBarWrapper.append(statusbar);
	
	return tileAndBarWrapper;
	
}

CFW.render.registerRenderer(CFW_RENDER_NAME_TILEANDBAR, new CFWRenderer(cfw_renderer_tileandbar));


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
		currentTile.data('record', currentRecord);
		currentTile.data('renderDef', renderDef);
		currentTile.data('settings', settings);
		currentTile.bind('click', cfw_renderer_common_openDetailsTable)
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction;
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
CFW.global.statusmapElements = [];

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
	// Calculate SizeFactors
	//===================================================	
	var sizeFactors = cfw_renderer_statusmap_calculateSizeFactors(widthfactor, heightfactor, renderDef.data.length);
	
	//===================================================
	// Create Tile Wrapper
	//===================================================
	var allTiles = $('<div class="w-100 h-100"></div>');
	allTiles.attr('id',  'statusmap-'+CFW.utils.randomString(16))
	allTiles.data('settings',  settings);
	allTiles.data('renderDef',  renderDef);
	allTiles.css("font-size", "0px");
	allTiles.css("display", "grid");
	allTiles.css("grid-template-columns", "repeat("+sizeFactors.columnCount+", 1fr)");
	allTiles.css("grid-template-rows", "repeat("+sizeFactors.rowCount+", 1fr)");
	
	// avoid aspect ratio being Infinity by setting a min size
	allTiles.css("min-height", "10px");
	allTiles.css("min-width", "10px");	
	
	//===================================================
	// Render Tiles and push to map
	//===================================================
	cfw_renderer_statusmap_createTiles(renderDef, settings, allTiles);
	CFW.global.statusmapElements.push(allTiles);

	return allTiles;

}

/******************************************************************
 * Returns an element
 ******************************************************************/
function cfw_renderer_statusmap_calculateSizeFactors(widthfactor, heightfactor, itemCount){
	
	// landscape example: 2 / 1 = 2.0
	// portrait example:  1 / 2 = 0.5
	var sizeFactors = {
		  inputWidthFactor: widthfactor
		, inputHeightFactor: heightfactor
		, inputAspectRatio: widthfactor / heightfactor
		, finalAspectRatio: 1
		, itemCount: itemCount
		, columnCount: 1
		, rowCount: 1
		, listenOnResize: false
	}
	//===================================================
	// Calculate Aspect Ratio
	//===================================================

	if(widthfactor == -1 || heightfactor == -1){
		sizeFactors.inputAspectRatio = 1;
		sizeFactors.listenOnResize = true;
	}
	
	if(sizeFactors.inputAspectRatio == Infinity){
		sizeFactors.inputAspectRatio = 1;
	}
	
	
	//===================================================
	// Calculate Number of Columns/Rows
	//===================================================

	var numberColumns = 1;
	var currentRatio = 0;
	for(;currentRatio < sizeFactors.inputAspectRatio; numberColumns++ ){
		currentRatio = numberColumns / (sizeFactors.itemCount / numberColumns);
	}
	
	sizeFactors.finalAspectRatio = currentRatio;
	var columnCount = Math.floor(numberColumns--);
	var rowCount = Math.floor(columnCount / currentRatio);

	while((columnCount * rowCount) < sizeFactors.itemCount ){
		columnCount++;
		if( (columnCount * rowCount) < sizeFactors.itemCount ){
			rowCount++;
		}
	}
	sizeFactors.columnCount = columnCount;
	sizeFactors.rowCount = rowCount;
	
	return sizeFactors;
}
/******************************************************************
 * Custom Resize Handler, because ResizeObserver sometimes causes 
 * infinite loops and causes high CPU consumption.
 ******************************************************************/
function cfw_renderer_statusmap_resizeHandler(){
			
	window.requestAnimationFrame(() => {
		
		var statusmapArray = CFW.global.statusmapElements;

		for(var i = 0; i < statusmapArray.length; i++){
			
			var statusmapDiv = statusmapArray[i];
			var statusmapDivID = statusmapDiv.attr('id');
			
			//-------------------------------------
			// Cleanup if removed
			if( !document.contains(statusmapDiv[0]) ){
				statusmapArray.splice(i, 1);
				i--;
				continue;
			}
			
			//-------------------------------------
			// Throttle: Only redraw when change is at least 10%
			var width = statusmapDiv.width();
			var height = statusmapDiv.height();
			var lastwidth = CFW.cache.data[statusmapDivID+"lastwidth"];
			var lastheight = CFW.cache.data[statusmapDivID+"lastheight"];
	
			
			if(lastwidth != null){
				var changeWidth = Math.abs(1.0 - (lastwidth / width));
				var changeHeight = Math.abs(1.0 - (lastheight / height));

				if(changeWidth < 0.10 && changeHeight < 0.10 ){
					continue;
				}
			}
			
			CFW.cache.data[statusmapDivID+"lastwidth"] = width;
			CFW.cache.data[statusmapDivID+"lastheight"] = height;
			
			//===================================================
			// Calculate SizeFactors
			//===================================================
			renderDef = statusmapDiv.data('renderDef');
			settings = statusmapDiv.data('settings');

			var sizeFactors = cfw_renderer_statusmap_calculateSizeFactors(width, height, renderDef.data.length);
			
			//===================================================
			// Update CSS 
			//===================================================
	
			statusmapDiv.css("grid-template-columns", "repeat("+sizeFactors.columnCount+", 1fr)");
			statusmapDiv.css("grid-template-rows", "repeat("+sizeFactors.rowCount+", 1fr)");

		}
		
	});
}

CFW.global.statusmapResizeInterval = window.setInterval(cfw_renderer_statusmap_resizeHandler, 500);


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_statusmap_createTiles(renderDef, settings, target) {
	
	//===================================================
	// Calculate Number of Columns
	//===================================================
	var itemCount = renderDef.data.length;
	
	//===================================================
	// Create Tiles
	//===================================================

	for(var i = 0; i < itemCount; i++ ){

		var currentTile = $('<div>');
		currentTile.addClass("smap-cell");
		currentTile.text("&nbsp;");
		//=====================================
		// Check has more records, else empty tile
		if(i > renderDef.data.length){
			continue;
		}
		
		target.append(currentTile);
		var currentRecord = renderDef.data[i];
		
		//=====================================
		// Add Styles
		renderDef.colorizeElement(currentRecord, currentTile, "bg");
		
		//=====================================
		// Add Details Click
		currentTile.data('record', currentRecord);
		currentTile.data('renderDef', renderDef);
		currentTile.data('settings', settings);
		currentTile.bind('click', cfw_renderer_common_openDetailsTable)
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction;
			currentTile.popover(popoverSettings);
		}
	
	}
}

function cfw_renderer_statusmap_auto(renderDef){ return cfw_renderer_statusmap(renderDef, -1, -1); }

CFW.render.registerRenderer(CFW_RENDER_NAME_STATUSMAP, new CFWRenderer(cfw_renderer_statusmap_auto));

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
		currentTile.data('renderDef', renderDef);
		currentTile.data('settings', settings);
		currentTile.bind('click', cfw_renderer_common_openDetailsTable)
		
		//=====================================
		// Add Details Popover
		if(settings.popover){
			var popoverSettings = Object.assign({}, CFW_RENDER_POPOVER_DEFAULTS);
			popoverSettings.content = settings.popoverFunction;
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
			narrow: true,
			stickyheader: false, 
			// define if single element arrays should be converted into vertical table (convert columns to rows)
			verticalize: false,
			// define if the verticalized fieldnames should be using labels
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
				label = renderDef.getLabel(fieldname, CFW_RENDER_NAME_TABLE);
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
				renderDef.getLabel(fieldname, CFW_RENDER_NAME_TABLE), 
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
				if(typeof value != "boolean" ){
					cell.append(value);
				}else{
					cell.append(""+value);
				}
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
		narrow: true,
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
			item = $('<li><strong>' + renderDef.getLabel(fieldname, CFW_RENDER_NAME_PANELS) + ':&nbsp;</strong></li>');
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
		//set to false to make the header bigger
		narrow: true,
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
				item = $('<li><strong>' + renderDef.getLabel(fieldname, CFW_RENDER_NAME_CARDS) + ':&nbsp;</strong></li>');
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
			// Defines how the menu should be rendered
			menu: 'default', // either 'default' | 'button' | 'none' | true | false 
			// Defines how the pagination should be rendered
			pagination: 'both', // either 'both' | 'top' | 'botton' | 'none' | true | false 
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
			//a function to call after a page has been rendered
			postprocess: null,
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
				preprocess: null,
			},
	};
	
	//-----------------------------------
	// Merge settings
	var settings = _.merge({}, defaultSettings, renderDef.rendererSettings.dataviewer);	
	
	// _.merge() merges arrays, we want to override the defaults 
	if( Array.isArray(renderDef.rendererSettings.dataviewer.sizes) ){
		settings.sizes = renderDef.rendererSettings.dataviewer.sizes;
	}
	
	//-----------------------------------
	// Create DataviewerDiv
	let dataviewerID = "dataviewer-"+CFW.utils.randomString(12);
	let dataviewerDiv = $('<div class="cfw-dataviewer" id="'+dataviewerID+'">');
	
	dataviewerDiv.data('renderDef', renderDef);
	dataviewerDiv.data('settings', settings);
	
	//-----------------------------------
	// Render without Dataviewer if menu=none
	if(settings.menu == 'none' || settings.menu == false){
		var params = cfw_renderer_dataviewer_createParams(dataviewerDiv, settings.initialpage);
		rendererName = params.rendererName.trim().toLowerCase();
		
		let renderResult = CFW.render.getRenderer(rendererName).render(params.finalRenderDef)
		// following makes too much troubles, don't do it
		// let renderWrapper = $('<div class="cfw-dataviewer-renderresult d-flex flex-grow-1 h-100 w-100">');
		// renderWrapper.append(renderResult);
		
		return renderResult;	
	}
	
	//-----------------------------------
	// Render Dataviewer	
	
	dataviewerDiv.append(cfw_renderer_dataviewer_createMenuHTML(dataviewerID, renderDef, settings, settings.rendererIndex) );
	
	dataviewerDiv.append('<div class="cfw-dataviewer-content  flex-column flex-grow-1">');
	
	cfw_renderer_dataviewer_fireChange(dataviewerDiv, settings.initialpage);
		
	return dataviewerDiv;
	
}

CFW.render.registerRenderer("dataviewer", new CFWRenderer(cfw_renderer_dataviewer) );

/******************************************************************
 * 
 * @param dataviewerIDOrJQuery cssSelector string like '#dataviewer-6a5b39ai' or a JQuery object
 * @param pageToRender page number
 ******************************************************************/
function cfw_renderer_dataviewer_createParams(dataviewerIDOrJQuery, pageToRender) {
	
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
	
	return params;
}

/******************************************************************
 * 
 * @param dataviewerIDOrJQuery cssSelector string like '#dataviewer-6a5b39ai' or a JQuery object
 * @param pageToRender page number
 ******************************************************************/
function cfw_renderer_dataviewer_fireChange(dataviewerIDOrJQuery, pageToRender) {
	
	//=====================================================
	// Initialize
	var params = cfw_renderer_dataviewer_createParams(dataviewerIDOrJQuery, pageToRender);
	
	//var dataviewerDiv = params.dataviewerDiv ;
	//var targetDiv = params.targetDiv ;
	//var dataviewerID = params.dataviewerID ;
	var settingsDiv = params.settingsDiv ;
	var renderDef = params.renderDef ;
	var settings = params.settings ;
	var filterquery = params.filterquery ;
	var sortbyField = params.sortbyField ;
	var sortbyDirection = params.sortbyDirection ;
	var offset = params.offset ;
	var pageSize = params.pageSize;
	
	//=====================================================
	// Handle Filter Highlight
	if(CFW.utils.isNullOrEmpty(filterquery)){
		settingsDiv.find('input[name="filterquery"]').removeClass('bg-cfw-yellow');
		settingsDiv.find('input[name="filterquery"]')
					.closest('.dropleft')
					.find('.btn-primary')
					.removeClass('bg-cfw-yellow');
	}else{
		settingsDiv.find('input[name="filterquery"]').addClass('bg-cfw-yellow');
		settingsDiv.find('input[name="filterquery"]')
					.closest('.dropleft')
					.find('.btn-primary')
		.addClass('bg-cfw-yellow');
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
			if(pageSize == -1
			|| settings.pagination == 'none'
			|| settings.pagination == false ){
				// reset to unpaginated 
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
			if(pageSize == -1
			|| settings.pagination == 'none'
			|| settings.pagination == false ){
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
	
	//----------------------------------
	// Callback
	if(settings.postprocess != null){
		settings.postprocess($(dataviewerIDOrJQuery));
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
	var renderWrapper = $('<div class="cfw-dataviewer-renderresult d-flex flex-grow-1 w-100">');
	renderWrapper.append(renderResult);
	
	//-------------------------------------
	// Create Paginator
	var pageNavigation = cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageToRender);
	

	targetDiv.html('');
	
	if(dataviewerSettings.pagination == "both" 
	|| dataviewerSettings.pagination == "top"
	|| dataviewerSettings.pagination == true){
		targetDiv.append(pageNavigation);
	}
	targetDiv.append(renderWrapper);
	
	if(dataviewerSettings.pagination == "both" 
	|| dataviewerSettings.pagination == "bottom"
	|| dataviewerSettings.pagination == true){
		targetDiv.append(pageNavigation);
	}
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
		// in case of no menu
		if(rendererIndex == null){ rendererIndex = dataviewerSettings.rendererIndex; }
		renderDefOverrides = dataviewerSettings.renderers[rendererIndex].renderdef;
		rendererName = dataviewerSettings.renderers[rendererIndex].name;
	}

	let finalRenderDef = _.merge({}, renderDef, renderDefOverrides);
	
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
				var fielLabel = renderDef.getLabel(fieldName, CFW_RENDER_NAME_DATAVIEWER);
				
				
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
	if(dataviewerSettings.pagination != "none" 
	&& dataviewerSettings.pagination != false){

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
	}
	//--------------------------------------
	// Filter Query
	let filterHighlightClass = CFW.utils.isNullOrEmpty(filterquery) ? '' : 'bg-cfw-yellow';

	html += '<div class="float-right  ml-2">'
		+'	<label for="filterquery">Filter:&nbsp;</label>'
		+'	<input type="text" name="filterquery" class="dataviewer-filterquery form-control form-control-sm '+filterHighlightClass+'" value="'+filterquery.replaceAll('"','&quot;')+'" placeholder="Filter..." '+onchangeAttribute+'>'
		+'</div>';
	
	html += '</div>';
	
	//--------------------------------------
	// Toogle Button
	
	var topCorrectionCSS = "";
	if(dataviewerSettings.pagination == "bottom" 
	|| dataviewerSettings.pagination == "none"
	|| dataviewerSettings.pagination == false){
		topCorrectionCSS = "top: 0px;";
	}
	
	if(dataviewerSettings.menu == 'button'){
		var dropDownID = 'dropdownMenuButton'+CFW.utils.randomString(12);
		var dropdownHTML = '<div class="dropleft d-inline pl-1 cfw-dataviewer-settings-button">'
			+ '<button  type="button" class="btn btn-xs btn-primary mb-2 '+filterHighlightClass+'"'
					+' id="'+dropDownID+'" '
					+' style="'+topCorrectionCSS+'" '
					+' data-toggle="dropdown" '
					+' aria-haspopup="true" '
					+' aria-expanded="false">'
			+ '  <i class="fas fa-sliders-h"></i> '
			+ '</button>'
			+ '  <div class="dropdown-menu p-2" onclick="event.stopPropagation()"  aria-labelledby="'+dropDownID+'">'
			     + html
			+'   </div> '
			+'</div>';
			
		return dropdownHTML;
		
	}
	
	// default
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

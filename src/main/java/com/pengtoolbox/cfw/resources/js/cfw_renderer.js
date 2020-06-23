
/******************************************************************
 * 
 ******************************************************************/

CFW.render.registerRenderer("html",
	new CFWRenderer(
		function (renderDefinition) {

			if( renderDefinition.data instanceof Element
			|| typeof renderDefinition.data == "string"){
				return renderDefinition.data;
			}else{
				return CFW.format.objectToHTMLList(renderDefinition.data);
			}
		})
);

/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("json",
	new CFWRenderer(
		function (renderDef) {
			var wrapperDiv = $('<div class="flex-grow-1">');
			
			var randomID = CFW.utils.randomString(16);
			return wrapperDiv.append('<pre id="json-'+randomID+'"><code>'+JSON.stringify(renderDef.data, null, 2)+'</code></pre><script>hljs.highlightBlock($("#json-'+randomID+'").get(0));</script>');
		}
	)
);
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
CFW.render.registerRenderer("tiles",
	new CFWRenderer(
		function (renderDef) {
					
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
				border: null
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
				var currentTile = $('<div class="d-flex p-2 m-1 cursor-pointer">');
				
				//=====================================
				// Add padding
				if(settings.showlabels == true && settings.showlabels == "true"){
					if(settings.sizefactor <= 0.5)		{ currentTile.addClass('p-1'); }
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
				// Create Tile
				if(settings.showlabels == true || settings.showlabels == "true" ){
					currentTile.addClass('flex-column flex-grow-1 justify-content-center align-items-center text-center');

					//-------------------------
					// Create Title
					var tileTitle = '';
					for(var j = 0; j < renderDef.titlefields.length; j++){
						var fieldname = renderDef.titlefields[j];
						tileTitle += currentRecord[fieldname];
						if(j < renderDef.titlefields.length-1){
							tileTitle += renderDef.titledelimiter;
						}
						
					}
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
					currentTile.css('width', 50*settings.sizefactor+"px");
					currentTile.css('height', 50*settings.sizefactor+"px");
				}

				allTiles.append(currentTile);
			}
			
			return allTiles;
		
		}
	)
);


/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("table",
	new CFWRenderer(
		function (renderDef) {
			
			// renderDef.rendererSettings.table same as CFWTable Settings
			// plus verticalize: false
//			{
//				filterable: true,
//				responsive: true,
//				hover: true,
//				striped: true,
//				narrow: false,
//				stickyheader: false, 
//			 }
			
			//-----------------------------------
			// Check Data
			if(renderDef.datatype != "array"){
				return "<span>Unable to convert data into table.</span>";
			}
			
			var settings = renderDef.rendererSettings.table;
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
					for(var fieldKey in renderDef.actions){
						actionButtonHTML += renderDef.actions[fieldKey](singleRecord, id );
					}
					singleRecordData.push({name: "Actions", value: actionButtonHTML});
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
			var cfwTable = new CFWTable(settings.verticalize);
			
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
				
				//-------------------------
				// Add Action buttons
				var id = null;
				if(renderDef.idfield != null){
					id = currentRecord[renderDef.idfield];
				}
				for(var fieldKey in renderDef.actions){
					
					cellHTML += '<td>'+renderDef.actions[fieldKey](currentRecord, id )+'</td>';
				}
				row.append(cellHTML);
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
	})
);

/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("panels",
	new CFWRenderer(
		function (renderDef) {
						
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
				var titleString = "";
				for (var j = 0; j < renderDef.titlefields.length; j++) {
					if (j != 0) {
						titleString += renderDef.titledelimiter;
					}
					titleString += currentRecord[renderDef.titlefields[j]];
				}
				
				panelSettings.title.append(titleString);	
				
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
	})
);

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

const CFW_RENDER_NAME_PROPERTIES = 'properties';

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_properties (renderDef) {
				
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

		cfw_renderer_properties_addPanel(wrapper, currentRecord, renderDef, settings, selectorGroupClass);

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

CFW.render.registerRenderer(CFW_RENDER_NAME_PROPERTIES, new CFWRenderer(cfw_renderer_properties) );

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_properties_addPanel(targetElement, currentRecord, renderDef, settings, selectorGroupClass){
	
	//---------------------------
	// Preprarations
	let panelSettings = {
			cardstyle: null,
			textstyle: null,
			textstyleheader: null,
			title: $('<div class="d-flex">'),
			titleright: "&nbsp;",
			body: "&nbsp;",
			narrow: settings.narrow,
	};
	 
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
	// Title fields
	let partTitlefields = $('<div class="flex-column minw-20">'); 
	for(let key in renderDef.titlefields){
		let fieldname = renderDef.titlefields[key];
		let value = renderDef.getCustomizedValue(currentRecord,fieldname, CFW_RENDER_NAME_PROPERTIES);
		
		let propertyDisplay = $('<div><b>'+fieldname+':</b>&nbsp;</div>');
		let valueSpan = $('<span class="text-secondary">');
		valueSpan.append(value);
		propertyDisplay.append(valueSpan);
		partTitlefields.append(propertyDisplay);
	}
	
	
	//-------------------------
	// Visible Fields
	let partVisiblefields = $('<div class="flex-grow-1 border-left pl-2 word-break-word">'); 
	
	for(let key in renderDef.visiblefields){
		let fieldname = renderDef.visiblefields[key];
		let value = renderDef.getCustomizedValue(currentRecord,fieldname, CFW_RENDER_NAME_PROPERTIES);
		
		let propertyDisplay = $('<span class="mr-2"><b>'+fieldname+':</b>&nbsp;</span>');
		let valueSpan = $('<span class="text-secondary">');
		valueSpan.append(value);
		propertyDisplay.append(valueSpan);
		partVisiblefields.append(propertyDisplay);
		
	}
	
	//-------------------------
	// All fields to Body
	let list = $("<ul>");
	for(let fieldname in currentRecord){
		
		if(fieldname == renderDef.bgstylefield 
		|| fieldname == renderDef.textstylefield ){ continue };
		
		let value = renderDef.getCustomizedValue(currentRecord,fieldname, CFW_RENDER_NAME_PROPERTIES);
		let label = renderDef.getLabel(fieldname, CFW_RENDER_NAME_PROPERTIES);
		let item = $('<li><strong>' + label + ':&nbsp;</strong></li>');
		item.append(value);
		list.append(item);
		
	}
	
	panelSettings.body = list;
			
	//-------------------------
	// Create Panel
	panelSettings.title.append(partTitlefields);		
	panelSettings.title.append(partVisiblefields);		
	
	var cfwPanel = new CFWPanel(panelSettings);
	var panel = cfwPanel.getPanel();
	
	//-------------------------
	// Add Color Bar
	var bgStyle = currentRecord[renderDef.bgstylefield];
	if(bgStyle != null){
		let colorBar = $('<div class="pt-1">');
		CFW.colors.colorizeElement(colorBar, bgStyle, "bg");	
		panel.prepend(colorBar);
	}
	
	targetElement.append(panel);
	
	
	//-------------------------
	// Print Children
	if(renderDef.hierarchy){
		var children = currentRecord[renderDef.hierarchyChildrenField];
		if(children != null){
			for(var index in children){
				let currentChild = children[index];
				if(renderDef.hierarchyAsTree){
					cfw_renderer_properties_addPanel(cfwPanel.getPanelBody(), currentChild, renderDef, settings, selectorGroupClass);
				}else{
					cfw_renderer_properties_addPanel(targetElement, currentChild, renderDef, settings, selectorGroupClass);
				}
			}
		}
	}
}

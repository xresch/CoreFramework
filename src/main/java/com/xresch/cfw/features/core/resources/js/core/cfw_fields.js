/**************************************************************************************************************
 * Contains the various functions that are used to initialize input fields.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/

 
/**************************************************************************************
 * 
 * @param fieldIDOrJQuery either the id of the field e.g. "myField" or a JQueryObject
 *************************************************************************************/
function cfw_internal_initializeBooleanSwitch(fieldIDOrJQuery){
	
	//-------------------------
	// Initialize
	let inputField;
	if (fieldIDOrJQuery instanceof jQuery){
		inputField = fieldIDOrJQuery;
	}else{
		inputField = $('#'+fieldIDOrJQuery);
	}
	inputField.addClass('d-none');
	
	let isSelected = CFW.utils.isTrue( inputField.val() );
	
	let labelYes = CFWL("cfw_core_yes", "Yes");
	let labelNo = CFWL("cfw_core_no", "No");

	//-------------------------
	// Check is Disabled
	let booleanSwitch;
	if( CFW.utils.isTrue(inputField.prop('disabled')) ){
		
		if(isSelected){		booleanSwitch = $(`<label>${labelYes}</label>`); }
		else{				booleanSwitch = $(`<label>${labelNo}</label>`); }
		
	}else{
		//-------------------------
		// Make Boolean Switch
	
		booleanSwitch = $(`<label class="cfw-switch">
				<div class="cfw-switch-slider" onclick="cfw_internal_toggleBooleanSwitchValue(this);">
					 <div class="cfw-switch-slider-on">${labelYes}</div>
					 <div class="cfw-switch-slider-button">&nbsp;</div>
					 <div class="cfw-switch-slider-off">${labelNo}</div>
				</div>
			</label>`
			);
	}
		
	inputField.before(booleanSwitch);
	booleanSwitch.prepend(inputField);
	
	cfw_internal_setBooleanSwitchValue(booleanSwitch, isSelected);
}

/**************************************************************************************
 * 
 * @param switchButton the button that was clicked
 *************************************************************************************/
function cfw_internal_toggleBooleanSwitchValue(switchButton){
	
	var elButton = $(switchButton); 
	var elSwitchWrapper =  elButton.closest('.cfw-switch');
	var hontouNiSelected = elSwitchWrapper.hasClass('switch-on');
		
	cfw_internal_setBooleanSwitchValue(elSwitchWrapper, !hontouNiSelected)


}

/**************************************************************************************
 * 
 * @param elSwitchWrapper the main switch element
 *************************************************************************************/
function cfw_internal_setBooleanSwitchValue(elSwitchWrapper, isSelected){
	
	isSelected = CFW.utils.isTrue(isSelected);
	
	var zeInput =  elSwitchWrapper.find('input');
	
	zeInput.val(isSelected);
	
	if(isSelected){
		elSwitchWrapper.addClass('switch-on');
		elSwitchWrapper.removeClass('switch-off');
	}else{
		elSwitchWrapper.removeClass('switch-on');
		elSwitchWrapper.addClass('switch-off');
	}
	
}

/**************************************************************************************
 * Initialize a form create with the CFWForm Java Class.
 * @param formIDOrObject the ID, element or JQueryObject of the form
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeForm(formIDOrObject){
	if(formIDOrObject != null){
		let form = $(formIDOrObject)
		var formID = form.attr("id");
		// workaround, force evaluation
		eval(form.find("script").text());
		eval("intializeForm_"+formID+"();");
	}
}

/**************************************************************************************
 * Initialize a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeSummernote(formID, editorID){
	
	var formSelector = '#'+formID;
	var editorSelector = formSelector+' #'+editorID;

	//--------------------------------------
	// Initialize Editor
	//--------------------------------------
	var editor = $(editorSelector);
	
	if(editor.length == 0){
		CFW.ui.addToastDanger('Error: the editor field is unknown: '+fieldID);
		return;
	}
	
	editor.summernote({
        placeholder: 'Enter your Text',
        tabsize: 2,
        height: 200,
        dialogsInBody: true
      });
	
	//--------------------------------------
	// Blur for saving
	//--------------------------------------
	$('.note-editing-area').on('blur', function() {
		if ($('#summernote').summernote('codeview.isActivated')) {
			$('#summernote').summernote('codeview.deactivate');
		}
	});
	
	//--------------------------------------
	// Get Editor Contents
	//--------------------------------------
	$.get('/cfw/formhandler', {id: formID, summernoteid: editorID})
	  .done(function(response) {
		  $(editor).summernote("code", response.payload.html);
	  })
	  .fail(function(response) {
		  CFW.ui.addToastDanger("Issue Loading Editor Content", "danger", CFW.config.toastErrorDelay);
	  })
	  .always(function(response) {
		  cfw_internal_handleMessages(response);			  
	  });
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeQueryEditor(elementidOrJQuery){
	
	let textarea = null;
	
	if(typeof elementidOrJQuery == "string"){
		let id = '#'+elementidOrJQuery;
		textarea = $(id);
	}else{
		textarea = $(elementidOrJQuery);
	}

	return new CFWQueryEditor(textarea, {});

}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeExpandableTextareaField(fieldID){
	var id = '#'+fieldID;

	var textarea = $(id);

	var wrapper = $('<div class="cfw-expandable-textarea-wrapper w-100">');
	wrapper.append('<i class="fas fa-expand" onclick="cfw_initializeExpandableTextareaField_toggle(this)" ></i>');
	textarea.before(wrapper);
	wrapper.append(textarea);	

}

/*************************************************************************************
 * Create a filterable select.
 * @param fieldID id of the input field. The value of the field 
 *        will be used as the selected value.
 * @param valueLabelOptions an object of objects containing values and
 *        labels, e.g. [ { value: 1, label: MyLabel}, ... ]
 * @param filterable true if a the select should have a filter, false otherwise
 * 
 *************************************************************************************/
function cfw_initializeSelect(fieldID, valueLabelOptions, filterable){
	
	let id = '#'+fieldID;

	let originalField = $(id);
	let selectedValue = originalField.val();
	
	originalField.css('display', 'none');
	
	//--------------------------
	// Create Wrapper
	let wrapper = $('<div id="'+fieldID+'-cfw-select" class="cfw-select w-100">');
	wrapper.data('options', valueLabelOptions);
	originalField.before(wrapper);
	wrapper.append(originalField);	
		
	//--------------------------
	// Create Dropdown
	let classes = originalField.attr('class');
	let dropdownHTML = `<div class="dropdown">
			<button id="${id}-dropdownMenuButton" class="form-control mb-2 dropdown-toggle dropdown-toggle-wide ${classes}" style="text-align: start;" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
			   &nbsp;
			</button>
			<div class="dropdown-menu dropdown-scroll mvw-80" aria-labelledby="${id}-dropdownMenuButton">
		`
		;
	
	//--------------------------
	// Create Options
	let containsSelectedValue = false;
	if(valueLabelOptions != null){
		for(let i = 0; i < valueLabelOptions.length; i++){
			let currentOption = valueLabelOptions[i];
			
			if(selectedValue == currentOption.value){ containsSelectedValue = true; }
			
			let label = '&nbsp;';
			if( !CFW.utils.isNullOrEmpty(currentOption.label) ) {
					label = currentOption.label
			}
			let noApostrophe = currentOption.value
										.replaceAll('"', '&quot;')
										.replaceAll("'", "\\'")
										;
										
			dropdownHTML += ' <a class="dropdown-item filterable" onclick="cfw_setSelectValue(\''+fieldID+'\', \''+noApostrophe+'\')">'+label+'</a>';
		}
	}
	
	dropdownHTML += '</div> </div>';
	
	//--------------------------------
	// Select first if not Selected
	if( ! containsSelectedValue
	&& valueLabelOptions != null
	&& valueLabelOptions.length > 0 ){
		selectedValue = valueLabelOptions[0].value;
	}
	
	//--------------------------------
	// Finishing Touch
	wrapper.append(dropdownHTML);

	cfw_setSelectValue(fieldID, selectedValue);
	
	//--------------------------------
	// Add Filter
	let menu = wrapper.find('.dropdown-menu');
	
	//--------------------------------
	// Add scroll to top
	wrapper.on('shown.bs.dropdown', function(obj) {
	    menu.animate({scrollTop: (-1 * menu[0].scrollHeight) }, 1000)
	});
	
  	//--------------------------------
	// Add Filter
	let filterField = $('<input type="text" class="form-control-sm w-fill ml-1 mr-1"'
								+' placeholder="Filter..."'
								+' onkeyup="cfw_filterSelect(this, event, \''+fieldID+'\')">');
	
	menu.prepend(filterField);
	
}


/*************************************************************************************
 * Filter ze select options.
 * @param filterField the field that filters
 * @param event 
 * @param fieldID of the the originalFieldID
 * 
 *************************************************************************************/
function cfw_filterSelect(filterField, event, fieldID){
	
	event.preventDefault();
	event.stopPropagation();
		
	CFW.utils.filterItems('#'+fieldID+'-cfw-select', filterField, '.filterable');
	
	//=====================================================
	// Handle Filter Highlight
	let field = $(filterField);
	if(CFW.utils.isNullOrEmpty(field.val())){
		field.removeClass('bg-cfw-yellow');
		field.closest(".dropdown-menu").removeClass('border-cfw-yellow border-3');
		
	}else{
		field.addClass('bg-cfw-yellow');
		field.closest(".dropdown-menu").addClass('border-cfw-yellow border-3');
	}
	
}

/*************************************************************************************
 * Set ze selected value.
 * @param optionElement the originalID of the field
 * @param valueToSelect an object of objects containing values and
 *        labels, e.g. [ { value: 1, label: MyLabel}, ... ]
 * @param filterable true if a the select should have a filter, false otherwise
 * 
 *************************************************************************************/
function cfw_setSelectValue(fieldID, valueToSelect){
	
	var inputField = $('#'+fieldID);
	var wrapper = inputField.closest('.cfw-select');
	var options = wrapper.data('options');
	
	var button = wrapper.find('button');
	
	if(options != null){
		for(let i = 0; i < options.length; i++){
			
			var currentOption = options[i];

			if(currentOption.value == valueToSelect){

				inputField.val(currentOption.value);
				
				if( !CFW.utils.isNullOrEmpty(currentOption.label) ) {
					button.html(currentOption.label);
				}else{
					button.html("&nbsp;");
				}
				
			}
			
		}
	}
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeExpandableTextareaField_toggle(buttonElement){

	var button = $(buttonElement);
	var wrapper = $(buttonElement).closest(".cfw-expandable-textarea-wrapper");

	if(wrapper.hasClass('expanded')){
		wrapper.removeClass('expanded');
		button.removeClass('fa-compress');
		button.addClass('fa-expand');
		
	}else{
		wrapper.addClass('expanded');
		button.addClass('fa-compress');
		button.removeClass('fa-expand');
	}

}


/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeTagsField(fieldID, maxTags){
	
	var id = '#'+fieldID;

	var tagsfield = $(id);
	
	//$(id).tagsinput();

	
	tagsfield.tagsinput({
		tagClass: 'cfw-tag btn-primary',
		maxTags: maxTags,
		maxChars: 1024,
		trimValue: true,
		allowDuplicates: false,
		addOnBlur: false
//		confirmKeys: [188, 13]
	});
	
	//----------------------------------
	// Add Classes
	var bootstrapTagsinput = $(id+'-tagsinput').closest('.bootstrap-tagsinput');
	if(tagsfield.hasClass('form-control-sm')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-sm')
	}
	
	if(tagsfield.closest('form').hasClass('form-inline')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-inline');
	}
	
	//----------------------------------
	// Hack to make it work more stable
	$(id+'-tagsinput').on('keyup', function (e) {
		// Enter and Comma
		if (e.keyCode == 13 || e.keyCode == 188) {
			$(id).tagsinput('add', this.value);
			this.value = '';
		}
	});
	
	
	
}

/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeTagsSelectorField(fieldID, maxTags, values){
	
	var id = '#'+fieldID;

	var tagsfield = $(id);
	
	// mark with CSS class for selecting the class afterwards
	//tagsfield.addClass("cfw-tags-selector");
	
	tagsfield.tagsinput({
		tagClass: 'cfw-tag btn-primary',
		itemValue: 'value',
		itemText: 'label',
		maxTags: maxTags,
		//maxChars: 30,
		trimValue: true,
		allowDuplicates: false,
//		onTagExists: function(item, $tag) {
//			$tag.fadeIn().fadeIn();
//
//		}
	});
	
	//----------------------------------
	// Add Classes
	var bootstrapTagsinput = $(id+'-tagsinput').closest('.bootstrap-tagsinput');
	if(tagsfield.hasClass('form-control-sm')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-sm')
	}
	
	if(tagsfield.closest('form').hasClass('form-inline')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-inline');
	}
	
	//----------------------------------
	// Add Values
	for(var key in values){
		tagsfield.tagsinput('add', { "value": key , "label": values[key] });
	}

}

/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeCheckboxesField(fieldID, options, values){
	
	var id = '#'+fieldID;

	var checkboxesField = $(id);

	var wrapper = $('<div class="cfw-checkboxes-field-wrapper flex-grow-1 flex-column">');
	checkboxesField.before(wrapper);
	wrapper.append(checkboxesField);
	checkboxesField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = checkboxesField.attr('class');
	checkboxesField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(var key in options){
		var label = options[key];
		var checked = (values != null && values[key] == "true") ? "checked" : "";
		wrapper.append('<label><input type="checkbox" class="cfw-checkbox" id="'+fieldID+'-'+key+'" name="'+key+'" onchange="cfw_internal_updateCheckboxesField(this)" '+checked+' />'+label+'</label>');
	}
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateCheckboxesField(element){
	
	var wrapper = $(element).closest('.cfw-checkboxes-field-wrapper');
	
	var originalField = wrapper.find('> input').first();
	
	var checkboxValues = {}
	
	checkboxesStateJsonString = originalField.val();

	if(!CFW.utils.isNullOrEmpty(checkboxesStateJsonString) && checkboxesStateJsonString != "null"){
		checkboxValues = JSON.parse(checkboxesStateJsonString);
	}
	
	wrapper.find('.cfw-checkbox').each(function(index, element){
		let current = $(element);
		let key = current.attr("name");
		let value = current.prop('checked');
		
		if(!CFW.utils.isNullOrEmpty(key)
		|| !CFW.utils.isNullOrEmpty(value)){
			checkboxValues[key] = value;
		}
	})
	
	originalField.val(JSON.stringify(checkboxValues));

}


/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeValueLabelField(fieldID, values){
	
	var id = '#'+fieldID;

	var valueLabelField = $(id);

	
	var wrapper = $('<div class="cfw-value-label-field-wrapper flex-grow-1">');
	valueLabelField.before(wrapper);
	wrapper.append(valueLabelField);
	valueLabelField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = valueLabelField.attr('class');
	valueLabelField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(var key in values){
		var fields = cfw_initializeValueLabelField_createField(key, values[key]);
		wrapper.append(fields);
	}
	
	//----------------------------------
	// Add At least one Empty Line
	wrapper.append(cfw_initializeValueLabelField_createField('', ''));
	
	//----------------------------------
	// Add Create Button
	wrapper.append(
			'<div class="d-flex">'
				+'<div class="flex-grow-1">&nbsp;</div>'
				+'<div class="btn btn-sm btn-primary" onclick="cfw_internal_addValueLabelField(this)"><i class="fas fa-plus-circle"></i></div>'
			+'</div>'
		);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeValueLabelField_createField(key, value){
	return '<div class="cfw-value-label d-flex pb-1">'
			+'<div class="d-flex flex-column">'
				+'<i class="fas fa-sort-up cfw-field-sort-up" onclick="cfw_internal_sortValueLabelField(this, \'up\')" ></i>'
				+'<i class="fas fa-sort-down cfw-field-sort-down" onclick="cfw_internal_sortValueLabelField(this, \'down\')"></i>'
			+'</div>'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Value" onchange="cfw_internal_updateValueLabelField(this)" value="'+key.replaceAll('"', '&quot;')+'">'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Label" onchange="cfw_internal_updateValueLabelField(this)" value="'+value.replaceAll('"', '&quot;')+'">'	
	+'</div>';	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_addValueLabelField(element){
	
	var button = $(element)
	var buttonParent = button.parent();
	
	buttonParent.before(cfw_initializeValueLabelField_createField('', ''))

}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateValueLabelField(element){
	
	var wrapper = $(element).closest('.cfw-value-label-field-wrapper');
	
	var originalField = wrapper.find('> input');
	
	var newValueLabels = {};
	wrapper.find('.cfw-value-label').each(function(index, element){
		let current = $(element);
		let value = current.find('input:first').val();
		let label = current.find('input:last').val();
		
		if(!CFW.utils.isNullOrEmpty(value)
		|| !CFW.utils.isNullOrEmpty(label)){
			newValueLabels[value] = label;
		}
	})
	
	originalField.val(JSON.stringify(newValueLabels));

}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_sortValueLabelField(element, upOrDown){
	
	var clickedButton = $(element);
	var currentValueLabel = clickedButton.closest(".cfw-value-label");
	
	
	if(upOrDown.toLowerCase() == "up"){
		var previousValueLabel = currentValueLabel.prev(".cfw-value-label");
		previousValueLabel.before(currentValueLabel);
	}else{
		var nextValueLabel = currentValueLabel.next(".cfw-value-label");
		nextValueLabel.after(currentValueLabel);
	}
	
	 cfw_internal_updateValueLabelField(element);
}

/**************************************************************************************
 * Initialize a Custom List Field created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeCustomListField(fieldID, values){
	
	var id = '#'+fieldID;

	var customListField = $(id);

	
	var wrapper = $('<div class="cfw-custom-list-field-wrapper flex-grow-1">');
	customListField.before(wrapper);
	wrapper.append(customListField);
	customListField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = customListField.attr('class');
	customListField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(let index in values){
		var fields = cfw_initializeCustomListField_createField(values[index]);
		wrapper.append(fields);
	}
	
	//----------------------------------
	// Add At least one Empty Line
	wrapper.append(cfw_initializeCustomListField_createField('', ''));
	
	//----------------------------------
	// Add Create Button
	wrapper.append(
			'<div class="d-flex">'
				+'<div class="flex-grow-1">&nbsp;</div>'
				+'<div class="btn btn-sm btn-primary" onclick="cfw_internal_addCustomListField(this)"><i class="fas fa-plus-circle"></i></div>'
			+'</div>'
		);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeCustomListField_createField(value){
	return '<div class="cfw-custom-list-item d-flex">'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Value" onchange="cfw_internal_updateCustomListField(this)" value="'+value.replaceAll('"', '&quot;')+'">'	
	+'</div>';	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_addCustomListField(element){
	
	var button = $(element)
	var buttonParent = button.parent();
	
	buttonParent.before(cfw_initializeCustomListField_createField(''))

}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateCustomListField(element){
	
	var wrapper = $(element).closest('.cfw-custom-list-field-wrapper');
	
	var originalField = wrapper.find('> input');
	
	var newListValues = [];
	wrapper.find('.cfw-custom-list-item').each(function(index, element){
		let current = $(element);
		let value = current.find('input').val();
		
		if(!CFW.utils.isNullOrEmpty(value)){
			newListValues.push(value);
		}
	})
	
	originalField.val(JSON.stringify(newListValues));

}

/**************************************************************************************
 * Initialize a ChartSettingsField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeChartSettingsField(fieldID, jsonData){
	
	var selector = '#'+fieldID;

	var chartSettingsField = $(selector);

	var wrapper = $('<div class="cfw-chartsettings-field-wrapper flex-grow-1">');
	chartSettingsField.before(wrapper);
	wrapper.append(chartSettingsField);
	
	chartSettingsField.val(JSON.stringify(jsonData));
	
	//----------------------------------
	// Add Classes
	//var classes = chartSettingsField.attr('class');
	//chartSettingsField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="dropdown dropright">
		<button id="chartSettingsButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			Chart Settings
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">

			<div class="row m-1"><strong>Series Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CHARTTYPE">Chart Type:</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-CHARTTYPE"  class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')" onkeydown="return event.key != 'Enter';" id="chart_type">
						<option value="area">Area</option>
						<option value="line">Line</option>
						<option value="bar">Bar</option>
						<option value="scatter">Scatter</option>
						<option value="steppedarea">Stepped Area</option>
						<option value="steppedline">Stepped Line</option>
						<option value="sparkline">Spark Line</option>
						<option value="sparkarea">Spark Area</option>
						<option value="sparkbar">Spark Bar</option>
						<option value="pie">Pie</option>
						<option value="doughnut">Doughnut</option>
						<option value="radar">Radar</option>
						<option value="polar">Polar</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-POINTRADIUS">
					Point Radius:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-POINTRADIUS" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-TENSION">
					Tension(0-1):
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-TENSION" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-STACKED">
					Stacked:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-STACKED" name="${fieldID}-STACKED" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-STACKED" name="${fieldID}-STACKED" value="false" checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SHOWLEGEND">
					Show Legend:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWLEGEND" name="${fieldID}-SHOWLEGEND" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWLEGEND" name="${fieldID}-SHOWLEGEND" value="false"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>

			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SPANGAPS">
					Connect Gaps:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SPANGAPS" name="${fieldID}-SPANGAPS" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SPANGAPS" name="${fieldID}-SPANGAPS" value="false"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>

			<div class="row m-1"><strong>Axes Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SHOWAXES">
					Show Axes:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWAXES" name="${fieldID}-SHOWAXES" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWAXES" name="${fieldID}-SHOWAXES" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-XAXIS_TYPE">
					X Axis Type:
				</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-XAXIS_TYPE" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
						<option value="time" selected="">Time</option>
						<option value="linear">Linear</option>
						<option value="logarithmic">Logarithmic</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_TYPE">
					Y Axis Type:
				</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-YAXIS_TYPE" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
						<option value="linear">Linear</option>
						<option value="logarithmic">Logarithmic</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_MIN">
					Y Minimum:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-YAXIS_MIN" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_MAX">
					Y Maximum:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-YAXIS_MAX" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1"><strong>Multi Chart Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHART">
					Multiple Charts:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHART" name="${fieldID}-MULTICHART" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHART" name="${fieldID}-MULTICHART" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHARTTITLE">
					Show Title:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHARTTITLE" name="${fieldID}-MULTICHARTTITLE" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHARTTITLE" name="${fieldID}-MULTICHARTTITLE" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHARTCOLUMNS">
					Columns:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-MULTICHARTCOLUMNS" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-HEIGHT">
					Chart Height:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-HEIGHT" type="text" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
															
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_applyChartSettings(fieldID, wrapper, jsonData);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_applyChartSettings(fieldID, wrapper, chartSettings){
	
	var selector = "#"+fieldID; 
	
	if(chartSettings == null || chartSettings.charttype == null){
		return;
	}

	wrapper.find(selector+"-CHARTTYPE").val(chartSettings.charttype );
	wrapper.find(selector+"-POINTRADIUS").val(chartSettings.pointradius );
	wrapper.find(selector+"-TENSION").val(chartSettings.tension );
	wrapper.find(selector+"-XAXIS_TYPE").val(chartSettings.xtype );
	wrapper.find(selector+"-YAXIS_TYPE").val(chartSettings.ytype );
	wrapper.find(selector+"-YAXIS_MIN").val(chartSettings.ymin );
	wrapper.find(selector+"-YAXIS_MAX").val(chartSettings.ymax );
	
	wrapper.find(selector+"-STACKED[value='" + chartSettings.stacked + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SHOWLEGEND[value='" + chartSettings.showlegend + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SHOWAXES[value='" + chartSettings.showaxes + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SPANGAPS[value='" + chartSettings.spangaps + "']").attr("checked", "checked");
	
	wrapper.find(selector+"-MULTICHART[value='" + chartSettings.multichart + "']").attr("checked", "checked");  
	wrapper.find(selector+"-MULTICHARTTITLE[value='" + chartSettings.multicharttitle + "']").attr("checked", "checked");  
	wrapper.find(selector+"-MULTICHARTCOLUMNS").val(chartSettings.multichartcolumns );
	wrapper.find(selector+"-HEIGHT").val(chartSettings.height );
		
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateChartSettings(elementID){
	
	var selector = "#"+elementID;
	var originalField = $(selector);
	var wrapper = originalField.closest('.cfw-chartsettings-field-wrapper');
	
	//--------------------------------------
	// Create Data Structure
	var chartSettings = {};
	
	chartSettings.charttype 		= $(selector+'-CHARTTYPE').val();
	chartSettings.xtype 			= $(selector+'-XAXIS_TYPE').val();
	chartSettings.ytype 			= $(selector+'-YAXIS_TYPE').val();
	chartSettings.ymin 				= $(selector+'-YAXIS_MIN').val();
	chartSettings.ymax				= $(selector+'-YAXIS_MAX').val();
	chartSettings.stacked  			= $(selector+"-STACKED:checked").val();  
	chartSettings.showlegend		= $(selector+"-SHOWLEGEND:checked").val();  
	chartSettings.showaxes			= $(selector+"-SHOWAXES:checked").val();  
	chartSettings.spangaps			= $(selector+"-SPANGAPS:checked").val();  
	chartSettings.pointradius		= $(selector+"-POINTRADIUS").val();  
	chartSettings.tension			= $(selector+"-TENSION").val();  
	
	chartSettings.multichart		= $(selector+"-MULTICHART:checked").val();  
	chartSettings.multicharttitle 	= $(selector+"-MULTICHARTTITLE:checked").val();  
	chartSettings.multichartcolumns	= $(selector+'-MULTICHARTCOLUMNS').val();
	chartSettings.height			= $(selector+'-HEIGHT').val();
	
	//--------------------------------------
	// Convert Numbers and Booleans
	chartSettings.pointradius 		= !isNaN(chartSettings.pointradius) ? parseFloat(chartSettings.pointradius) : 0;
	chartSettings.tension 			= !isNaN(chartSettings.tension) ? parseFloat(chartSettings.tension) : 0.0;
	chartSettings.ymin 				= !isNaN(chartSettings.ymin) ? parseFloat(chartSettings.ymin) : 0;
	chartSettings.ymax 				= !isNaN(chartSettings.ymax) ? parseFloat(chartSettings.ymax) : null;
	chartSettings.multichartcolumns = !isNaN(chartSettings.multichartcolumns) ? parseInt(chartSettings.multichartcolumns) : null;
	
	chartSettings.stacked  			= (chartSettings.stacked.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.showlegend  		= (chartSettings.showlegend.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.showaxes  		= (chartSettings.showaxes.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.multichart  		= (chartSettings.multichart.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.multicharttitle  	= (chartSettings.multicharttitle.trim().toLowerCase() == "true") ? true : false;  
	
	//--------------------------------------
	// Set ChartSettings
	originalField.val(JSON.stringify(chartSettings));
	//originalField.dropdown('toggle');

}

/**************************************************************************************
 * Initialize a ChartSettingsField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeColorPickerField(fieldID, colorOrClass){
	
	var selector = '#'+fieldID;

	var colorpickerField = $(selector);

	var wrapper = $('<div class="cfw-chartsettings-field-wrapper flex-grow-1">');
	colorpickerField.before(wrapper);
	wrapper.append(colorpickerField);
	
	colorpickerField.val(colorOrClass);
	
	//----------------------------------
	// Add Classes
	//var classes = chartSettingsField.attr('class');
	//chartSettingsField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="cfw-colorpicker dropdown dropright">
		<button id="${fieldID}-BUTTON" class="btn btn-sm" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			&nbsp;
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">
			
			<div class="row m-1"><strong>Choose Color</strong></div>
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Custom:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-CUSTOMCOLOR" class="form-control-inline form-control-sm col-md-12" type="color" onchange="cfw_internal_updateColorPickerValueCustom(\'${fieldID}\')" >	
				</div>
			</div>
		
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Default:
				</label>   
				<div class="col-sm-9">
					<div class="cfw-color-box cursor-pointer bg-cfw-none" onclick="cfw_internal_updateColorPickerValue('${fieldID}', null)">&nbsp;</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Transparent:
				</label>   
				<div class="col-sm-9">
					<div class="cfw-color-box cursor-pointer  bg-cfw-transparent" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-transparent')">&nbsp;</div>
				</div>
			</div>
			
			<div class="row m-1"><strong>Default Colors</strong></div>
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-green" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-green')">&nbsp;</div>
					<div class="cfw-color-box cursor-pointer bg-cfw-limegreen" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-limegreen')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-yellow" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-yellow')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-orange" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-orange')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-red" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-red')">&nbsp;</div>	
				</div>
			</div>	
			
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-blue" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-blue')">&nbsp;</div>
					<div class="cfw-color-box cursor-pointer bg-cfw-indigo" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-indigo')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-purple" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-purple')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-pink" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-pink')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-cyan" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-cyan')">&nbsp;</div>	
				</div>
			</div>	
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-white" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-white')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-lightgray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-lightgray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-gray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-gray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-darkgray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-darkgray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-black" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-black')">&nbsp;</div>	
				</div>
			</div>	
												
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_updateColorPickerValue(fieldID, colorOrClass);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateColorPickerValueCustom(fieldID){

	let selector = "#"+fieldID; 
	
	let colorOrClass = $(selector+"-CUSTOMCOLOR").val();
	

	cfw_internal_updateColorPickerValue(fieldID, colorOrClass);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateColorPickerValue(fieldID, colorOrClass){
	
	let selector = "#"+fieldID; 
	let originalField = $(selector);
	
	//--------------------------
	// Update Selected Value
	originalField.val(colorOrClass);

	//--------------------------
	// Get UI thingies to Update
	let button = $(selector+"-BUTTON");
	let customPicker = $(selector+"-CUSTOMCOLOR");
	
	//--------------------------
	// Update Colors
	button.css("background-color", "");
	button.attr('class', 'btn btn-sm');
	if(!CFW.utils.isNullOrEmpty(colorOrClass)){
		
		CFW.colors.colorizeElement(button, colorOrClass, "bg", null);
		
		let hexColor = colorOrClass;

		if(!hexColor.startsWith("#")){
			let workspace = CFW.ui.getWorkspace();
			let tempDiv = $('<div>');
			workspace.append(tempDiv); // needed or won't get a color when using css-classes
			
			CFW.colors.colorizeElement(tempDiv, colorOrClass, "bg", null);
			let rgbColor = tempDiv.css('background-color');

			hexColor = CFW.colors.RGB2HEX(rgbColor);
		}
		customPicker.val(hexColor);
	}else{
		
		CFW.colors.colorizeElement(button, "cfw-none", "bg", null);
	}
	
}

/**************************************************************************************
 * Initialize a ScheduleField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeScheduleField(fieldID, jsonData){
	
	var selector = '#'+fieldID;

	var scheduleField = $(selector);

	var wrapper = $('<div class="cfw-schedule-field-wrapper flex-grow-1">');
	scheduleField.before(wrapper);
	wrapper.append(scheduleField);
	
	scheduleField.val(JSON.stringify(jsonData));
	
	//----------------------------------
	// Add Classes
	//var classes = scheduleField.attr('class');
	//scheduleField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="dropdown">
		<button id="scheduleButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
			Select Schedule
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu col-sm-12" aria-labelledby="dropdownMenuButton" onclick="event.stopPropagation();">
			
			<div class="row m-1"><strong>Timeframe</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-STARTDATETIME">Start Time:</label>   
				<div class="col-sm-9">
					<div class="custom-control-inline w-100 mr-0">
						<input id="${fieldID}-STARTDATETIME-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-STARTDATETIME')" class="col-md-8 form-control form-control-sm">
						<input id="${fieldID}-STARTDATETIME-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-STARTDATETIME')" class="col-md-4 form-control form-control-sm">	   
						<input id="${fieldID}-STARTDATETIME" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-STARTDATETIME" onkeydown="return event.key != 'Enter';">
					</div>
				</div>
			</div>

			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-ENDDATETIME">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="END_DATE_TIME" onkeydown="return event.key != 'Enter';"> End Time:
				</label>   
				<div class="col-sm-9">
					<div class="custom-control-inline w-100 mr-0">
						<input id="${fieldID}-ENDDATETIME-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-ENDDATETIME')" class="col-md-8 form-control form-control-sm">
						<input id="${fieldID}-ENDDATETIME-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-ENDDATETIME')" class="col-md-4 form-control form-control-sm">	   
						<input id="${fieldID}-ENDDATETIME" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-ENDDATETIME" onkeydown="return event.key != 'Enter';">
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="RUN_FOREVER" > Run Forever
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="EXECUTION_COUNT" > End after <input id="${fieldID}-EXECUTIONCOUNT" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>execution(s)</span>
				</div>   
			</div>
			
			<div class="row m-1"><strong>Interval</strong></div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_X_MINUTES" > Every <input id="${fieldID}-EVERYXMINUTES" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>minute(s)</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_X_DAYS" > Every <input id="${fieldID}-EVERYXDAYS" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>day(s)</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_WEEK" > Every week on 
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-MON" name="${fieldID}-EVERYWEEK-MON" > <span>Mon</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-TUE" name="${fieldID}-EVERYWEEK-TUE" > <span>Tue</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-WED" name="${fieldID}-EVERYWEEK-WED" > <span>Wed</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-THU" name="${fieldID}-EVERYWEEK-THU" > <span>Thu</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-FRI" name="${fieldID}-EVERYWEEK-FRI" > <span>Fri</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-SAT" name="${fieldID}-EVERYWEEK-SAT" > <span>Sat</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-SUN" name="${fieldID}-EVERYWEEK-SUN" > <span>Sun</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="CRON_EXPRESSION" > Cron Expression: <input id="${fieldID}-CRON_EXPRESSION" list="cron_samples" class="form-control-inline form-control-sm">
					<datalist id="cron_samples">
						<option value="42 * * ? * *">Every minute at the 42th second</option>
						<option value="0 15,30,45 * ? * *">Every hour at minutes 15, 30 and 45 </option>
						<option value="0 0 22 ? * MON-FRI">Every business day at 22 o'clock </option>
						<option value="0 0,15,30,45 8-18 ? * MON-FRI">Every business day from 08:00 to 18:00 every 15 minutes  </option>
						<option value="0 0 4 L-2 * ?">Every month on the second to last day of the month, at 04:00</option>
						<option value="0 0 12 1L * ?">Every month on the last Sunday, at noon</option>
						<option value="0 0 12 2L * ?">Every month on the last Monday, at noon</option>
			<option value=""></option>
			<option value=""></option>
					</datalist>
				</div>   
			</div>
			
			<div class="row m-1">
				<div class="col-sm-12">
					<button class="btn btn-sm btn-primary" onclick="cfw_internal_confirmSchedule('${fieldID}');" type="button">
						{!cfw_core_confirm!}
					</button>
					<button class="btn btn-sm btn-primary" onclick="$('#${fieldID}').dropdown('toggle');" type="button">
						{!cfw_core_close!}
					</button>
				</div>
			</div>
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_applySchedule(fieldID, wrapper, jsonData);
}


/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_applySchedule(fieldID, wrapper, scheduleData){
	
	var selector = "#"+fieldID; 
	
	if(scheduleData == null || scheduleData.timeframe == null){
		return;
	}
	
	cfw_initializeTimefield(fieldID+'-STARTDATETIME', scheduleData.timeframe.startdatetime);
	cfw_initializeTimefield(fieldID+'-ENDDATETIME', scheduleData.timeframe.enddatetime);
	
	//values: RUN_FOREVER, ENDDATETIME, EXECUTION_COUNT
	wrapper.find(selector+"-RADIO-ENDTYPE[value='" + scheduleData.timeframe.endtype + "']").attr("checked", "checked");  
	wrapper.find(selector+"-EXECUTIONCOUNT").val(scheduleData.timeframe.executioncount );
	
	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	wrapper.find(selector+"-RADIO-INTERVAL[value='" + scheduleData.interval.intervaltype + "']").attr("checked", "checked");  
	wrapper.find(selector+"-EVERYXMINUTES").val(scheduleData.interval.everyxminutes	);
	wrapper.find(selector+"-EVERYXDAYS").val(scheduleData.interval.everyxdays );
	
	if(scheduleData.interval.everyweek.MON){ wrapper.find(selector+"-EVERYWEEK-MON").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.TUE){ wrapper.find(selector+"-EVERYWEEK-TUE").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.WED){ wrapper.find(selector+"-EVERYWEEK-WED").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.THU){ wrapper.find(selector+"-EVERYWEEK-THU").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.FRI){ wrapper.find(selector+"-EVERYWEEK-FRI").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.SAT){ wrapper.find(selector+"-EVERYWEEK-SAT").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.SUN){ wrapper.find(selector+"-EVERYWEEK-SUN").attr("checked", "checked"); }

	wrapper.find(selector+"-CRON_EXPRESSION").val(scheduleData.interval.cronexpression);
	
}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_confirmSchedule(elementID){
	
	var selector = "#"+elementID;
	var originalField = $(selector);
	var wrapper = originalField.closest('.cfw-schedule-field-wrapper');
	
	//--------------------------------------
	// Create Data Structure
	var scheduleData = {
			  timezoneOffset: new Date().getTimezoneOffset()
			, timeframe: {}
			, interval: {}
	};
	
	scheduleData.timeframe.startdatetime 	= isNaN($(selector+'-STARTDATETIME').val()) ? null : parseInt($(selector+'-STARTDATETIME').val());
	scheduleData.timeframe.endtype  	= $(selector+"-RADIO-ENDTYPE:checked").val();  //values: RUN_FOREVER, ENDDATETIME, EXECUTION_COUNT
	scheduleData.timeframe.enddatetime  	= isNaN($(selector+'-ENDDATETIME').val()) ? null : parseInt($(selector+'-ENDDATETIME').val());;
	scheduleData.timeframe.executioncount  	= $(selector+"-EXECUTIONCOUNT").val();
	
	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	scheduleData.interval.intervaltype  	= $(selector+"-RADIO-INTERVAL:checked").val();
	scheduleData.interval.everyxminutes		= $(selector+"-EVERYXMINUTES").val();
	scheduleData.interval.everyxdays  		= $(selector+"-EVERYXDAYS").val();
	
	scheduleData.interval.everyweek  			= {}
	scheduleData.interval.everyweek.MON  		= $(selector+"-EVERYWEEK-MON").is(":checked");
	scheduleData.interval.everyweek.TUE  		= $(selector+"-EVERYWEEK-TUE").is(":checked");
	scheduleData.interval.everyweek.WED  		= $(selector+"-EVERYWEEK-WED").is(":checked");
	scheduleData.interval.everyweek.THU  		= $(selector+"-EVERYWEEK-THU").is(":checked");
	scheduleData.interval.everyweek.FRI  		= $(selector+"-EVERYWEEK-FRI").is(":checked");
	scheduleData.interval.everyweek.SAT  		= $(selector+"-EVERYWEEK-SAT").is(":checked");
	scheduleData.interval.everyweek.SUN  		= $(selector+"-EVERYWEEK-SUN").is(":checked");

	scheduleData.interval.cronexpression  	= $(selector+"-CRON_EXPRESSION").val();
	
	//--------------------------------------
	// Validate
	var isValid = true;
	
	if( CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime ) ){
		CFW.ui.addToastDanger('Please specify a start time.');
		isValid = false;
	}
	
	if( CFW.utils.isNullOrEmpty(scheduleData.timeframe.endtype) ){
		CFW.ui.addToastDanger('Please select/specify how long the schedule will be valid.');
		isValid = false;
	}

	if(scheduleData.timeframe.endtype === "ENDDATETIME" 
	&& ( CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime)
	   || scheduleData.timeframe.enddatetime < scheduleData.timeframe.startdatetime) 
	){
		CFW.ui.addToastDanger('Please specify an end time that is after the start time.')
		isValid = false;
	}
	
	if(scheduleData.timeframe.endtype === "EXECUTION_COUNT" 
	&& CFW.utils.isNullOrEmpty(scheduleData.timeframe.executioncount) ) {
		CFW.ui.addToastDanger('Please specify the number of executions.')
		isValid = false;
	}

	if( CFW.utils.isNullOrEmpty(scheduleData.interval.intervaltype) ){
		CFW.ui.addToastDanger('Please select and interval.');
		isValid = false;
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_X_MINUTES" 
	&& CFW.utils.isNullOrEmpty(scheduleData.interval.everyxminutes) ) {
		CFW.ui.addToastDanger('Please specify the number of minutes.')
		isValid = false;
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_X_DAYS"){ 
	
		if(CFW.utils.isNullOrEmpty(scheduleData.interval.everyxdays) ) {
			CFW.ui.addToastDanger('Please specify the number of days.')
			isValid = false;
		}
		
		if(scheduleData.timeframe.endtype === "EXECUTION_COUNT"  ) {
			CFW.ui.addToastDanger('Execution count is not supported for interval in days.')
			isValid = false;
		}
		
		
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_WEEK" ) {
		let noDaysSelected = true;
		
		if(     scheduleData.interval.everyweek.MON) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.TUE) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.WED) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.THU) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.FRI) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.SAT) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.SUN) { noDaysSelected = false; }
		
		if(noDaysSelected){
			CFW.ui.addToastDanger('Please select at least one week day.')
			isValid = false;
		}
	}
	
	if(scheduleData.interval.intervaltype === "CRON_EXPRESSION"){
		
		if(CFW.utils.isNullOrEmpty(scheduleData.interval.cronexpression) ) {
			CFW.ui.addToastDanger('Please specify a cron expression.')
			isValid = false;
		}
		
		if(scheduleData.timeframe.endtype === "EXECUTION_COUNT"  ) {
			CFW.ui.addToastDanger('Execution count is not supported for CRON expressions.')
			isValid = false;
		}
	}
	
	//--------------------------------------
	// Set Schedule
	if(isValid){
		originalField.val(JSON.stringify(scheduleData));
		originalField.dropdown('toggle');
	}

}

/**************************************************************************************
 * Initialize a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeTimefield(fieldID, epochMillis){
	
	var id = '#'+fieldID;
	var datepicker = $(id+'-datepicker');
	var timepicker = $(id+'-timepicker');
	
	if(datepicker.length == 0){
		CFW.ui.addToastDanger('Error: the datepicker field is unknown: '+fieldID);
		return;
	}
	
	if(epochMillis != null){
		$(id).val(epochMillis);
		let sureInteger = parseInt(epochMillis, 10);

		var date = moment(sureInteger);
		datepicker.first().val(date.format("YYYY-MM-DD"));

		if(timepicker.length > 0 != null){
			timepicker.first().val(date.format("HH:mm"));
		}
	}
		
}

/**************************************************************************************
 * Update a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_updateTimeField(fieldID){
	
	var id = '#'+fieldID;
	var datepicker = $(id+'-datepicker');
	var timepicker = $(id+'-timepicker');
	
	if(datepicker.length == 0){
		CFW.ui.addToastDanger('Error: the datepicker field is unknown: '+fieldID)
	}
	
	var dateString = datepicker.first().val();
	
	if(timepicker.length > 0){
		var timeString = timepicker.first().val();
		if(timeString.length > 0 ){
			dateString = dateString +"T"+timeString;
		}
	}
	
	if(dateString.length > 0){
		$(id).val(Date.parse(dateString));
	}else{
		$(id).val('');
	}
	
}

/**************************************************************************************
 * Takes the ID of a text field which will be the target to store the timeframe picker
 * value. 
 * The original field gets hidden and will be replaced by the timeframe picker itself. 
 * 
 * @param fieldID the id of the target field(without '#')
 * @param initialData the json object containing the initial value of the field as epoch time:
 *        {
 *				earliest: 123455678989,
 *              latest: 1234567890
 *        }
 * @param onchangeCallbackFunction function taking parameters func(fieldID, updateType, earliest, latest);
 *           updateType would be one of: 'custom' | 'shift-earlier' | 'shift-later' | a preset
 *************************************************************************************/
function cfw_initializeTimeframePicker(fieldID, initialData, onchangeCallbackFunction){
	
	var selector = '#'+fieldID;

	var timeframeStoreField = $(selector);
	timeframeStoreField.addClass('d-none');
	
	var wrapper = $('<div class="cfw-timeframepicker-wrapper" data-id="'+fieldID+'">');
	timeframeStoreField.before(wrapper);
	wrapper.append(timeframeStoreField);
		
	//----------------------------------
	// Set Intial Value
	var pickerDataString = JSON.stringify(initialData);
	timeframeStoreField.val(pickerDataString);
	
	//----------------------------------
	// Add Classes
	//var classes = scheduleField.attr('class');
	//scheduleField.addClass('d-none');

	//----------------------------------
	// Create HTML
	wrapper.append( `
<div id="${fieldID}-picker" class="btn-group">
	<button class="btn btn-sm btn-primary btn-inline" onclick="cfw_timeframePicker_shift(this, 'earlier');" type="button">
		<i class="fas fa-chevron-left"></i>
	</button>
	
	<div class="dropdown">
		<button id="${fieldID}-timeframeSelectorButton" class="btn btn-sm btn-primary dropdown-toggle" type="button"  data-toggle="collapse" data-target="#${fieldID}-timepickerDropdown" aria-haspopup="true" aria-expanded="false">
			{!cfw_core_last!} 30 {!cfw_core_minutes!}
		</button>
	
	<div id="${fieldID}-timepickerDropdown" class="dropdown-menu" style="width: 400px;" aria-labelledby="dropdownMenuButton">			
		<div class="accordion" id="timePickerAccordion">
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-0">
					<div role="button" data-toggle="collapse" data-target="#collapse0" aria-expanded="true">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>Presets</span>
						</div>
					</div>
				</div>
				<div class="collapse show" id="collapse0" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-0">
					<div class="card-body p-1">
						<div class="row ml-0 mr-0">
							<div class="col-6">
								<a id="time-preset-5-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '5-m');">{!cfw_core_last!} 5 {!cfw_core_minutes!}</a>
								<a id="time-preset-15-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '15-m');">{!cfw_core_last!} 15 {!cfw_core_minutes!}</a>
								<a id="time-preset-30-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '30-m');">{!cfw_core_last!} 30 {!cfw_core_minutes!}</a>
								<a id="time-preset-1-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '1-h');">{!cfw_core_last!} 1 {!cfw_core_hour!}</a>
								<a id="time-preset-2-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-h');">{!cfw_core_last!} 2 {!cfw_core_hours!}</a>
								<a id="time-preset-4-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '4-h');">{!cfw_core_last!} 4 {!cfw_core_hours!}</a>
								<a id="time-preset-6-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '6-h');">{!cfw_core_last!} 6 {!cfw_core_hours!}</a>
								<a id="time-preset-12-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '12-h');">{!cfw_core_last!} 12 {!cfw_core_hours!}</a>
								<a id="time-preset-24-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '24-h');">{!cfw_core_last!} 24 {!cfw_core_hours!}</a>
							</div>
							<div class="col-6">
								<a id="time-preset-2-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-d');">{!cfw_core_last!} 2 {!cfw_core_days!}</a>
								<a id="time-preset-7-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '7-d');">{!cfw_core_last!} 7 {!cfw_core_days!}</a>
								<a id="time-preset-14-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '14-d');">{!cfw_core_last!} 14 {!cfw_core_days!}</a>
								<a id="time-preset-1-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '1-M');">{!cfw_core_last!} 1 {!cfw_core_month!}</a>
								<a id="time-preset-2-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-M');">{!cfw_core_last!} 2 {!cfw_core_months!}</a>
								<a id="time-preset-3-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '3-M');">{!cfw_core_last!} 3 {!cfw_core_months!}</a>
								<a id="time-preset-6-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '6-M');">{!cfw_core_last!} 6 {!cfw_core_months!}</a>
								<a id="time-preset-12-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '12-M');">{!cfw_core_last!} 12 {!cfw_core_months!}</a>
								<a id="time-preset-24-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '24-M');">{!cfw_core_last!} 24 {!cfw_core_months!}</a>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-1">
					<div role="button" data-toggle="collapse" data-target="#collapse1" aria-expanded="false">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>Relative Time</span>
						</div>
					</div>
				</div>
				<div class="collapse" id="collapse1" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-1">
					<div class="card-body p-1">
						
						
						<label class="col-sm-12 col-form-label col-form-label-sm">Present Time Offset:</label>
						
						<div class="row m-1">  
							<div class="col-sm-12">
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-RELATIVE_OFFSET-count" type="number" class="col-md-4 form-control form-control-sm" min="0" value="4" >
			    					<select id="${fieldID}-RELATIVE_OFFSET-unit" class="col-md-8 form-control form-control-sm" >	
			    						<option value="m">{!cfw_core_minutes!}</option>
			    						<option value="h">{!cfw_core_hours!}</option>
			    						<option value="d">{!cfw_core_days!}</option>
			    						<option value="M">{!cfw_core_months!}</option>
			    					</select>   	
								</div>
							</div>
						</div>
						
						<div class="row m-1">
							<div class="col-sm-12">
								<button class="btn btn-sm btn-primary" onclick="cfw_timeframePicker_confirmRelativeOffset(this);" type="button">
									{!cfw_core_confirm!}
								</button>
							</div>
						</div>
						
						
						
					</div>
				</div>
			</div>
			
			
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-2">
					<div role="button" data-toggle="collapse" data-target="#collapse2" aria-expanded="false">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>{!cfw_core_customtime!}</span>
						</div>
					</div>
				</div>
				<div class="collapse" id="collapse2" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-2">
					<div class="card-body p-1">
						<div class="row m-1">  
							<label class="col-sm-2 col-form-label col-form-label-sm" for="${fieldID}-CUSTOM_EARLIEST">{!cfw_core_earliest!}:</label>   
							<div class="col-sm-10">
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-CUSTOM_EARLIEST-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_EARLIEST')" class="col-md-7 form-control form-control-sm">
			    					<input id="${fieldID}-CUSTOM_EARLIEST-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_EARLIEST')" class="col-md-5 form-control form-control-sm">	   
			    					<input id="${fieldID}-CUSTOM_EARLIEST" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-CUSTOM_EARLIEST" onkeydown="return event.key != 'Enter';">
								</div>
							</div>
						</div>
						<div class="row m-1">  
							<label class="col-sm-2 col-form-label col-form-label-sm" for="${fieldID}-CUSTOM_LATEST">{!cfw_core_latest!}:</label>   
							<div class="col-sm-10">  
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-CUSTOM_LATEST-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_LATEST')" class="col-md-7 form-control form-control-sm">
			    					<input id="${fieldID}-CUSTOM_LATEST-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_LATEST')" class="col-md-5 form-control form-control-sm">	   
			    					<input id="${fieldID}-CUSTOM_LATEST" type="hidden" class="form-control" placeholder="Latest" name="${fieldID}-CUSTOM_LATEST" onkeydown="return event.key != 'Enter';">
								</div>
							</div>
						</div>
						<div class="row m-1">
							<div class="col-sm-12">
								<button class="btn btn-sm btn-primary" onclick="cfw_timeframePicker_confirmCustom(this);" type="button">
									{!cfw_core_confirm!}
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	</div>
	<button class="btn btn-sm btn-primary btn-inline" onclick="cfw_timeframePicker_shift(this, 'later');" type="button">
		<i class="fas fa-chevron-right"></i>
	</button>
</div>

	`);
	
	//----------------------------------
	// Initialize Value, do not trigger
	// callback
	cfw_timeframePicker_applyTime(fieldID, initialData);
	
	
	//----------------------------------
	// StoreCallback
	if( onchangeCallbackFunction != null){
		CFW.global.timeframePickerOnchangeHandlers[fieldID] = onchangeCallbackFunction;
	}
}

/*******************************************************************************
 * 
 * @param fieldID without id-hashtag
 * @param timeObject json object like retrieved from a timeframe picker 
 * 			{ offset: "", earliest: "", latest: ""}
 ******************************************************************************/
function cfw_timeframePicker_applyTime(fieldID, timeObject){
	
	if(timeObject != null){

		if(timeObject.offset == null){
			cfw_timeframePicker_setCustom(fieldID, timeObject.earliest, timeObject.latest);
		}else{
			cfw_timeframePicker_setOffset("#"+fieldID, timeObject.offset);
		}
	}
}
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_storeValue(fieldID, offset, earliest, latest){
	
	var selector = '#'+fieldID;
	
	// -----------------------------------------
	// Update FieldData
	var pickerData = {
		offset: 	offset,
		earliest:	parseFloat(earliest),
		latest: 	parseFloat(latest),
		clientTimezoneOffset: 	new Date().getTimezoneOffset()
	}

	$(selector).val(JSON.stringify(pickerData));
	
	// -----------------------------------------
	// Update Custom Time Selector
	cfw_initializeTimefield(fieldID+'-CUSTOM_EARLIEST', earliest);
	cfw_initializeTimefield(fieldID+'-CUSTOM_LATEST', latest);
	
	var callback = CFW.global.timeframePickerOnchangeHandlers[fieldID];
	
	if(callback != null){
		callback(fieldID, pickerData);
	}
	
}
	
/*******************************************************************************
 * 
 * @param origin either JQueryObject or id with leading #
 * @param offset the offset to set.
 ******************************************************************************/
function cfw_timeframePicker_setOffset(origin, offset){
	
	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	
	var fieldID = wrapper.data('id');
	var selector = '#'+fieldID;
	

	var split = offset.split('-');
	var count = split[0];
	var unit = split[1];
	var earliestMillis = moment().utc().subtract(count, unit).utc().valueOf();
	var latestMillis = moment().utc().valueOf();

	//-----------------------------------------
	// Update Label
	var unitLocale = (unit == "m") ? 'minutes' 
					: (unit == "h") ? 'hours' 
					: (unit == "d") ? 'days' 
					: (unit == "M") ? 'months' 
					: "unknown"
					;
	
	var label = CFWL('cfw_core_last', 'Last')+" "+count+" "+CFWL('cfw_core_'+unitLocale, " ???");

	$(selector+'-timeframeSelectorButton').text(label);
	
	// -----------------------------------------
	// Update Relative Offset Picker
	$(selector+'-RELATIVE_OFFSET-count').val(count);
	$(selector+'-RELATIVE_OFFSET-unit').val(unit);

	if(isNaN(count)){
		CFW.ui.addToastWarning("Count must be at least 1.");
		return;
	}
	// -----------------------------------------
	// Update Original Field
	cfw_timeframePicker_storeValue(fieldID, offset, earliestMillis, latestMillis);
	
	var dropdown = $(origin).closest(selector+'-timepickerDropdown');
	dropdown.collapse('hide');
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_confirmRelativeOffset(origin){

	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	var fieldID = wrapper.data('id');
		
	var count = $('#'+fieldID+'-RELATIVE_OFFSET-count').val();
	var unit = $('#'+fieldID+'-RELATIVE_OFFSET-unit').val();

	if(isNaN(count)){
		CFW.ui.addToastWarning("Count must be at least 1.");
		return;
	}
	
	cfw_timeframePicker_setOffset(origin, count+"-"+unit);
	
	//---------------------------
	// Hide Dropdown
	var dropdown = $(origin).closest('#'+fieldID+'-timepickerDropdown');
	dropdown.collapse('hide');
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_setCustom(fieldID, earliestMillis, latestMillis){
	var selector = '#'+fieldID;
	let selectorButton = $(selector+'-timeframeSelectorButton');
	
	if(selectorButton.length > 0){
		selectorButton.text(
			cfw_format_timerangeToString(earliestMillis, latestMillis)
		);
		
		// -----------------------------------------
		// Update Original Field
		cfw_timeframePicker_storeValue(fieldID, null, parseFloat(earliestMillis), parseFloat(latestMillis) );
	}
		
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_confirmCustom(origin){

	let wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	let fieldID = wrapper.data('id');
		
	let earliestMillis = $('#'+fieldID+'-CUSTOM_EARLIEST').val();
	let latestMillis = $('#'+fieldID+'-CUSTOM_LATEST').val()

	if( parseInt(earliestMillis) >= parseInt(latestMillis) ) {
		CFW.ui.addToastWarning("Earliest time has to be before latest time.");
		return;
	}
	cfw_timeframePicker_setCustom(fieldID, earliestMillis, latestMillis);
	
	//---------------------------
	// Hide Dropdown
	var dropdown = $(origin).closest('#'+fieldID+'-timepickerDropdown');
	dropdown.collapse('hide');
	
}


/*******************************************************************************
 * 
 * @param direction
 *            'earlier' or 'later'
 ******************************************************************************/
function cfw_timeframePicker_shift(origin, direction){
	
	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	var fieldID = wrapper.data('id');
	var selector = '#'+fieldID;
	
	var storeField = $(selector);
	var dataString = storeField.val();
	
	var data = JSON.parse(dataString);
	//var label = wrapper.find("#time-preset-"+preset).text();
	
	wrapper.find(selector+'-timeframeSelectorButton').text(CFWL('cfw_core_customtime', "Custom Time"));
	
	var offsetMillis = data.latest - data.earliest;
	
	var offsetEarliest;
	var offsetLatest;
	if(direction == 'earlier'){
		offsetLatest = data.earliest;
		offsetEarliest = data.earliest - offsetMillis;
	}else{
		offsetEarliest = data.latest;
		offsetLatest = data.latest + offsetMillis;
	}
	
	cfw_timeframePicker_setCustom(fieldID, offsetEarliest, offsetLatest);
	
}

/**************************************************************************************
 * Takes the ID of a text field which will be the target to store the file picker
 * value. 
 * The original field gets hidden and will be replaced by the timeframe picker itself. 
 * 
 * @param fieldID the id of the target field(without '#')
 * @param isMultiple boolean, define if the picker allows to select multiple files
 * @param replaceExisting boolean define if existing file ID should be kept and only the data should be replaced.
 * 						  Only works when isMultiple = false
 * @param initialData the json object containing the initial value of the field as epoch time:
 *        {
 *			   id: 123
 *           , name: "filename.txt"
 * 			 , mimetype: "plain/text"
 * 			 , extension: "txt"
 * 			 , size: 6789 
 *        }
 *************************************************************************************/
function cfw_initializeFilePicker(fieldID, isMultiple, initialData, replaceExisting){
	
	var selector = '#'+fieldID;

	var originalField = $(selector);
	originalField.addClass('d-none');

	let wrapperID = fieldID+"-filepicker-"+CFW.utils.randomString(12);
	
	var wrapper = $('<div id="'+wrapperID+'" class="cfw-filepicker-wrapper" data-id="'+fieldID+'">');
	originalField.before(wrapper);
	wrapper.append(originalField);
	wrapper.data("isMultiple", isMultiple)
	wrapper.data("replaceExisting", replaceExisting)
			
	//----------------------------------
	// Set Intial Value
	let multiple = "";
	if(isMultiple){
		multiple = "multiple";
	}
	//----------------------------------
	// Create HTML

	wrapper.append( `
<div>
	<p>Upload a file with the file dialog or by dragging and dropping onto the dashed region.</p>
	
	<label class="btn btn-sm btn-primary">
		<i class="fas fa-upload"></i> Upload

	    <input 	type="file"
	    		id="${wrapperID}-filechooser" 
	    		accept="*/*" 
	    		style="display: none;"
	    		 ${multiple} 
	    		onchange="cfw_filepicker_handleSelectedFiles(this)">
	</label>
    <div class="cfw-filepicker-selected card p-2">No file selected</div>
</div>
	`);

	//----------------------------------
	// Set Intial Value
	if(! CFW.utils.isNullOrEmpty(initialData) ){
		cfw_filepicker_setSelectedFiles(wrapper, originalField, initialData);

	}
	//----------------------------------
	// Add Highlight Event Handlers
	
	
	//----------------------------------
	// Add Highlight Event Handlers
	
	let functionHighlight = function(e){
		e.preventDefault();
		e.stopPropagation();
		
		let  element = e.target || e.srcElement;
		if(element != null){
			wrapper.addClass('highlight');  
		}
		
	};
	
	['dragenter', 'dragover']
		.forEach(eventName => {
			wrapper.on(eventName, functionHighlight);
			wrapper.children().on(eventName, functionHighlight);
		});
	
	//----------------------------------
	// Add Unhighlight Event Handlers
	
	let functionUnhighlight = function(e){
		e.preventDefault();
		e.stopPropagation();
		
		let  element = e.target || e.srcElement;
		if(element != null){
			wrapper.removeClass('highlight');  
		}
		
	};
	
	['dragleave', "drop"]
		.forEach(eventName => {
			wrapper.on(eventName, functionUnhighlight);
			wrapper.children().on(eventName, functionUnhighlight);
		});
	
	
	//----------------------------------
	// Handle File Drop
	let functionDrop =  function (e) {
		
		e.preventDefault();
		e.stopPropagation();

		let dt = e.originalEvent.dataTransfer;
		let files = dt.files;
		
		if(!isMultiple && files.length > 1){
			alert('Sorry only a single file can be uploaded.');
			return;
		}
		
		cfw_filepicker_uploadFiles(wrapper, originalField, files);
		
	  	
	};

	wrapper.on('drop', functionDrop);
	wrapper.children().on('drop', functionDrop);	

	
}

/**************************************************************************************
 * Uploads the file selected with the choose file dialog.
 * 
 * @param files array of files 
 *************************************************************************************/
function cfw_filepicker_handleSelectedFiles(sourceElement) {
	
	let files = sourceElement.files;
	let wrapper = $(sourceElement).closest('.cfw-filepicker-wrapper');
	let originalField = wrapper.find("input[data-role='filepicker']");
	
	cfw_filepicker_uploadFiles(wrapper, originalField, files);
	
}

/**************************************************************************************
 * Sets the selected files in the UI
 * 
 * @param files array of files 
 *************************************************************************************/
function cfw_filepicker_setSelectedFiles(wrapper, originalField, filesArray) {

	originalField.val( JSON.stringify(filesArray) ) ;

/*	console.log("=====================");
	console.log(filesArray);
	console.log(wrapper);
	console.log(originalField);
	console.log(originalField.val());*/
	
	let selectedDiv = wrapper.find('.cfw-filepicker-selected');
	selectedDiv.html("");
		
	for(i = 0; i < filesArray.length; i++){
		let currentFile = filesArray[i];
		let size = CFW.format.numbersInThousands(currentFile.size, 1, true, true);
		selectedDiv.append('<p><b>'+currentFile.name+'</b> ('+size+')</p>');
	}
}

/**************************************************************************************
 * Uploads one or more files to the server.
 * 
 * @param files array of files 
 *************************************************************************************/
function cfw_filepicker_uploadFiles(wrapper, originalField, files) {
	
	let wrapperID =  wrapper.attr('id');
	let isMultiple = wrapper.data("isMultiple");
	let replaceExisting = wrapper.data("replaceExisting");
	
	
	if(files.length <= 0){ return; }
	
	CFW.ui.toggleLoader(true, wrapperID);
	
	cfw_utils_sleep(500).then(() => {
		
		//-------------------------------
		// Upload Files
		let uploadedArray = [];
		$.ajaxSetup({async: false});
		
			for(let i = 0; i < files.length; i++){
				
				console.log("============");
				console.log("i: "+i);
				console.log(files.length);
				let file = files[i];
				
				let CFW_URL_FILEUPLOAD = '/app/stream/fileupload';
				
				let formData = new FormData()
				
				let originalValue = originalField.val();
				if(originalValue != null
				&& originalValue != undefined){
		  			formData.append('originalData', originalValue )
		  		}
		  		
		  		formData.append('replaceExisting', replaceExisting)
		  		formData.append('name', file.name)
		  		formData.append('size', file.size)
		  		formData.append('type', file.type)
		  		formData.append('lastModified', file.lastModified)
		  		formData.append('file', file)
		
				CFW.http.postFormData(CFW_URL_FILEUPLOAD, formData, function(response, status, xhr){
					if(response.payload != null){
						uploadedArray = _.concat(uploadedArray, response.payload);
					}
				});
		
			}
		$.ajaxSetup({async: true});
		
		//-------------------------------
		// Update Selected Files
		
		let originalArray = [];
		if( isMultiple && !CFW.utils.isNullOrEmpty(originalField.val()) ) {
			originalArray = JSON.parse(originalField.val());
		}
		
		let finalArray = _.concat(originalArray, uploadedArray);
		
		//-------------------------------
		// Update Selection in UI
		cfw_filepicker_setSelectedFiles(wrapper, originalField, finalArray);
		
		CFW.ui.toggleLoader(false, wrapperID);
	});
	
}


/**************************************************************************************
 * Will add a function that will be called before sending the autocomplete request
 * to the server.
 * You can add more parameters to the paramObject provided to the callback function
 * 
 * @param functionToEnhanceParams(inputField, paramObject, originalParamObject) 
 * 		callback function, will get an object that can be enhanced with more parameters
 * 			inputField: element on which autocomplete was triggered
 * 			paramObject: parameters for the autocomplete request, change this object.
 * 			originalParamObject: the original parameters without changes
 *************************************************************************************/
function cfw_autocomplete_addParamEnhancer(functionToEnhanceParams){
	
	CFW.global.autcompleteParamEnhancerFunctions.push(functionToEnhanceParams);
}


/**************************************************************************************
 * Initialize an autocomplete added to a CFWField with setAutocompleteHandler().
 * Can be used to make a static autocomplete using the second parameter.
 * 
 * @param formID the id of the form
 * @param fieldName the name of the field
 * @param minChars the minimum amount of chars for triggering the autocomplete
 * @param maxResults the max number of results listed for the autocomplete
 * @param array (optional) an array of strings ir objects like "{value: value, label: label}" 
 * 				used for the autocomplete
 * @param triggerWithCtrlSpace (optional)set to true to only trigger autocomplete with Ctrl+Space
 * @param target (optional)set the target of the autocomplete results, either JQuery or selector
 * @return nothing
 *************************************************************************************/
function cfw_autocompleteInitialize(formID, fieldName, minChars, maxResults, array, triggerWithCtrlSpace, target){
	
	//---------------------------------------
	// Initialize	
	CFW.global.autocompleteFocus = -1;
	
	var $input = $("#"+fieldName);	
	if($input.attr('data-role') == "tagsinput"){
		$input = $("#"+fieldName+"-tagsinput");
		//$input.parent().prepend('<i class="fas fa-search pr-2"><i>');
	}
	
	var settings = {
		$input: 				$input,
		formID:					formID,
		fieldName: 				fieldName,
		minChars:				minChars,
		maxResults:				maxResults,
		triggerWithCtrlSpace:	triggerWithCtrlSpace,
	}
	
	//prevent browser default auto fill
	$input.attr('autocomplete', 'off');
	
	settings.inputField = settings.$input.get(0);
	settings.autocompleteID = settings.inputField.id + "-autocomplete";
	
	settings.autocompleteTarget = $input.parent();
	if(target != null){
		settings.autocompleteTarget = $(target);
	}
	
	// For testing
	//var array = ["Afghanistan","Albania","Algeria","Andorra","Angola","Anguilla"];
	
	//--------------------------------------------------------------
	// STATIC ARRAY
	// ============
	// execute a function when someone writes in the text field:
	//--------------------------------------------------------------
	if(array != null){
		$input.on('input', function(e) {
			
			var filteredArray = [];
			var searchString = settings.inputField.value;
		    if ( ! CFW.utils.isNullOrEmpty(searchString) ){
				searchString = searchString.trim();
			}
			
			//----------------------------
		    // Filter Array
		    for (let i = 0; i < array.length && filteredArray.length < maxResults; i++) {
		      
			   	var currentValue = array[i];
			    
			    if(typeof currentValue == "object"){
					if (currentValue.label.toUpperCase().indexOf(searchString.toUpperCase()) >= 0) {
				   		filteredArray.push(currentValue);
				   	}
				}else{
				   	if (currentValue.toUpperCase().indexOf(searchString.toUpperCase()) >= 0) {
				   		filteredArray.push({value: currentValue, label: currentValue});
				   	}
				}
			}
			//----------------------------
		    // Show AutoComplete	
			cfw_autocompleteShow(settings.inputField
						, settings.autocompleteTarget
						, 0
						, {lists:[{ items: filteredArray} ]
						, description: null}
						);
		});
	}
	
	//--------------------------------------------------------------
	// DYNAMIC SERVER SIDE AUTOCOMPLETE
	//--------------------------------------------------------------
	if(array == null){
		
		$input.on('input', function(e) {
			//cfw_autocompleteEventHandler(e, fieldName, $input, minChars, maxResults, triggerWithCtrlSpace, autocompleteTarget);
			cfw_autocompleteEventHandler(e, settings);
		});
		
		//--------------------------------
		// Do Ctrl+Space separately, as keyup
		// would also trigger on Arrow Keys etc...
		$input.on('keydown', function(e) {
			
			if (triggerWithCtrlSpace && (e.ctrlKey && e.keyCode == 32)) {
				//cfw_autocompleteEventHandler(e, fieldName, $input, minChars, maxResults, triggerWithCtrlSpace, autocompleteTarget);
				cfw_autocompleteEventHandler(e, settings);
			}
			
			// close autocomplete on Esc
			if ( e.keyCode == 27) {
				cfw_autocompleteCloseAll();
			}
			
		});
	}
	
	//--------------------------------------------------------------
	// execute a function presses a key on the keyboard
	//--------------------------------------------------------------
	$input.on('keydown',  function(e) {
		

		var itemList = $("#"+settings.autocompleteID);
		var items;
		
		if (itemList != null && itemList.length > 0){ 
			items = itemList.find(".autocomplete-item")
		};

		//---------------------------
		// Down Arrow
		if (e.keyCode == 40) {
			  CFW.global.autocompleteFocus++;
			  markActiveItem(e, items);
			  return;
		}
		//---------------------------
		// Up Arrow
		if (e.keyCode == 38) { 
			  CFW.global.autocompleteFocus--;
			  markActiveItem(e, items);
			  return;
		}
		
		//---------------------------
		// Enter
		if (e.keyCode == 13) {
			/* If the ENTER key is pressed, prevent the form from being submitted. 
			 * Still do it for tagsinput.js fields*/
			if($input.attr('placeholder') != "Tags" && $input.prop("tagName") != "TEXTAREA"){
				e.preventDefault();
			}
			if (CFW.global.autocompleteFocus > -1) {
				/* and simulate a click on the "active" item. */
				if (itemList != null && items != null){
					items.eq(CFW.global.autocompleteFocus).click();
					e.preventDefault();
				}
			}else{
				// Close if nothing selected
				cfw_autocompleteCloseAll();
			}
		}
	});
	

	
	//--------------------------------------------------------------
	// a function to classify an item as "active"
	//--------------------------------------------------------------
	function markActiveItem(e, items) {
		if (!items) return false;
		/* start by removing the "active" class on all items: */
		removeActiveClass(items);
		if (CFW.global.autocompleteFocus >= items.length) CFW.global.autocompleteFocus = 0;
		if (CFW.global.autocompleteFocus < 0) CFW.global.autocompleteFocus = (items.length - 1);
		/* add class "autocomplete-active": */
		items.eq(CFW.global.autocompleteFocus).addClass("autocomplete-active");
		e.preventDefault();
	}
	
	//--------------------------------------------------------------
	// a function to remove the "active" class from all 
	// autocomplete items.
	//--------------------------------------------------------------
	function removeActiveClass(items) {
		items.removeClass("autocomplete-active");
	}
	

}

/**************************************************************************************
 * Listen on field input or Ctrl+Space and fetch data for autocomplete.

 *************************************************************************************/
function cfw_autocompleteEventHandler(e, settings) {

	
	// --------------------------------
	// Verify only do on  Ctrl+Space
	if (settings.triggerWithCtrlSpace && !(e.ctrlKey && e.keyCode == 32)) {
		return;
	}

	// Only do autocomplete if at least N characters are typed			
	if(settings.$input.val().length >= settings.minChars){
		// use a count and set timeout to wait for the user 
		// finishing his input before sending a request to the
		// server. Reduces overhead.
		var currentCount = ++CFW.global.autocompleteCounter;
		
		//----------------------------
		// Show Loader
		var loader = settings.autocompleteTarget.find('#autocomplete-loader');
		if(loader.length == 0){
			loader = $('<div id="autocomplete-loader"><small><i class="fa fa-cog fa-spin fa-1x"></i><span>&nbsp;Loading...</span></small></div>');
			settings.autocompleteTarget.append(loader);
		}
		
		//----------------------------
		// Load Autocomplete
		setTimeout(
			function(){
				
				//only execute if it is the last triggered autocomplete request
				if(currentCount != CFW.global.autocompleteCounter){
					return;
				}
				
				var params = CFW.format.formToParams(settings.$input.closest('form'));

				params[CFW.global.formID] = settings.formID;
				params.cfwAutocompleteFieldname = settings.fieldName;
				params.cfwAutocompleteSearchstring = settings.inputField.value;
				params.cfwAutocompleteCursorPosition = settings.inputField.selectionStart;
				
				//function to customize the autocomplete
				let origParamsClone = _.cloneDeep(params);
				for(let i in CFW.global.autcompleteParamEnhancerFunctions){
					let currentFunc = CFW.global.autcompleteParamEnhancerFunctions[i];
					currentFunc(settings.$input, params, _.cloneDeep(origParamsClone));
				}
				
				cfw_http_postJSON('/cfw/autocomplete', params, 
					function(data) {
						loader.remove();
						cfw_autocompleteShow(settings.inputField, settings.autocompleteTarget, settings.inputField.selectionStart, data.payload);
					})
			},
			1000);
	}
}	
	
/**************************************************************************************
 * close all autocomplete lists in the document.
 *************************************************************************************/
function cfw_autocompleteCloseAll() {	
	CFW.global.autocompleteFocus = -1;
	$('.autocomplete-wrapper').remove();
}

/**************************************************************************************
 * Show autocomplete
 * @param inputField domElement to show the autocomplete for.
 * @param autocompleteResults JSON object with the structure:
  {
	"lists": [
		[
			{"value": "Value0", "label": "Label0", "description": "some description or null" },
			{"value": "Value1", "label": "Label1", "description": "some description or null" },
			{...}
		],
		[
			{"value": "AnotherValue0", "label": "AnotherLabel0", "description": "some description or null" },
			{"value": "AnotherValue1", "label": "AnotherLabel1", "description": "some description or null" },
			{...}
		],
		[...]
	],
	"description": "<p>This is your HTML Description. Feel free to add some stuff like a list:<p <ol><li>Do this</li><li>Do that</li><li>Do even more...</li></ol>"
}
 *************************************************************************************/
function cfw_autocompleteShow(inputField, autocompleteTarget, cursorPosition, autocompleteResults){
	
	//----------------------------
    // Initialize and Cleanup
	var searchString = inputField.value;
	var autocompleteID = inputField.id + "-autocomplete";
		
    cfw_autocompleteCloseAll();
    if (!searchString) { return false;}
    if(autocompleteResults == null){ return false;};
    
    //----------------------------
    // Create Multiple Item Lists
    var autocompleteWrapper = $('<div id="'+autocompleteID+'" class="autocomplete-wrapper">');
    
  	//----------------------------
    // Add Description
	if(autocompleteResults.description != null){
		var description = $('<div class="autocomplete-description">');
		description.html(autocompleteResults.description);
		
		//Do not close autocomplete when clicking into description.
		description.on("click", function(e) {
			e.stopPropagation();
		});
		
		$(inputField).parent().append(description);
	}
	autocompleteWrapper.append(description);

  	//----------------------------
    // Add Lists
    var multipleLists = $('<div class="autocomplete-multilist d-flex flex-row">');
    
    // Plus 20% to give some range for flex display
    let maxWidthPercent = (100 / autocompleteResults.lists.length)+20;
    
    for(var key in autocompleteResults.lists){
    	var current = autocompleteResults.lists[key];
    	 multipleLists.append(cfw_autocompleteCreateItemList(inputField, cursorPosition, current, maxWidthPercent));
    }
    
    autocompleteWrapper.append(multipleLists);
	
	autocompleteTarget.append(autocompleteWrapper);
	
}

/**************************************************************************************
 * Creates a list for the autocomplete multilist.
 * @param inputField domElement to show the autocomplete for.
 * @param values a list JSON object containing elements like:
 *    {
 *		"title": "List title or null",
 *		"items": [
 *    		{"value": "Value0", "label": "Label0", "description": "some description or null" }
 *		]
 *	  }
 * 
 *************************************************************************************/
function cfw_autocompleteCreateItemList(targetInputField, cursorPosition, listObject, maxWidthPercent){
	//----------------------------
    // Initialize and Cleanup
	var searchString = targetInputField.value;
	var autocompleteID = targetInputField.id + "-autocomplete-list";
	var isTagsInput = false;
	var isTagsselector = false;

	if(targetInputField.id != null && targetInputField.id.endsWith('-tagsinput')){
		isTagsInput = true;
		isTagsselector = $(targetInputField).parent().siblings('input').hasClass('cfw-tags-selector');
	}
	
	var itemList =  $("<div>");
	
	itemList.attr("id", autocompleteID);
	itemList.css("max-width", maxWidthPercent+"%");
	itemList.addClass("autocomplete-list flex-fill");
			
	var itemClass = "autocomplete-item";
	if($(targetInputField).hasClass("form-control-sm")){
		itemClass += " autocomplete-item-sm";
	}
	
	
	//----------------------------
	// Add List Title
	if( !CFW.utils.isNullOrEmpty(listObject.title)){
		var listTitleElement = $('<div class="autocomplete-list-title"><b>'+listObject.title+'</b><div>');
		//Do not close autocomplete when clicking on title.
		listTitleElement.on("click", function(e) { e.stopPropagation(); });
		
		itemList.append(listTitleElement)
	}
	
	
	//----------------------------
	// Iterate values object
	listItems = listObject.items;
	for (var key in listItems) {
		
	   	var currentValue = listItems[key].value;
	   	var label = ""+listItems[key].label;
		var description = listItems[key].description;	
		// Methods: exchange | append | replacelast | replacebeforecursor
		var method = listItems[key].method;	
		
		//----------------------------
		// Create Item
		var item = $('<div class="'+itemClass+'">');
		
		// make the matching letters bold:
		var index = label.toUpperCase().indexOf(searchString.toUpperCase());
		if(index == 0){
			item.append(
				"<strong>" + label.substr(0, searchString.length) + "</strong>"
				+ label.substr(searchString.length)
				);
		}else if(index > 0){
			var part1 = label.substr(0, index);
			var part2 = label.substr(index, searchString.length);
			var part3 = label.substr(index+searchString.length);
			item.append(part1 + "<strong>" +part2+ "</strong>" +part3);
		}else {
			item.append(label);
		}
		
		if(description != null){
			item.append('<p>'+description+'</p>');
		}
		
		//-----------------------
		// Create Field
		var hiddenInput = $('<input type="hidden" data-label="'+label+'" data-tagsinput="'+isTagsInput+'" data-tagsselector="'+isTagsselector+'">');
		hiddenInput.val(currentValue);
		item.append(hiddenInput);
		
		item.on("click", function(e) { 
			e.stopPropagation();
			e.preventDefault()
			var element = $(this).find('input');
			var itemValue = element.val();
			var itemLabel = element.data('label');
			var itemIsTagsInput = element.data('tagsinput');
			var itemIsTagsSelector = element.data('tagsselector');
			
			if(!itemIsTagsInput){
				if(method == 'exchange'){
					targetInputField.value = itemValue;
					
				}else if(method == 'append'){
					targetInputField.value = targetInputField.value + itemValue;
					
				}else if(method.startsWith('replacelast:')){
					var stringToReplace = method.substring('replacelast:'.length);
					var tempValue = targetInputField.value;
					tempValue = tempValue.substring(0, tempValue.lastIndexOf(stringToReplace));
					targetInputField.value = tempValue + itemValue;
				}else if(method.startsWith('replacebeforecursor:')){
					var stringToReplace = method.substring('replacebeforecursor:'.length);
					var originalValue = targetInputField.value;
					var beforeCursor = originalValue.substring(0, cursorPosition);
					var afterCursor = originalValue.substring(cursorPosition);
					var beforeCursorWithoutReplaceString = beforeCursor.substring(0, beforeCursor.lastIndexOf(stringToReplace));
					targetInputField.value = beforeCursorWithoutReplaceString + itemValue + afterCursor;
					
					var newCursorPos =  (beforeCursorWithoutReplaceString + itemValue).length;
					targetInputField.focus();

					targetInputField.selectionStart = newCursorPos;
					targetInputField.setSelectionRange(newCursorPos, newCursorPos);
				}
				cfw_autocompleteCloseAll();
			}else{

				targetInputField.value = '';
				
				if(itemIsTagsSelector){
					// Do for Selector
					$(targetInputField).parent().siblings('input').tagsinput('add', { "value": itemValue , "label": itemLabel });
				}else{
					// Do For TagsInput
					$(targetInputField).parent().siblings('input').tagsinput('add', itemValue);
					
				}
				cfw_autocompleteCloseAll();
			}
		});
		itemList.append(item);
	    
	}
	
	return itemList;
}



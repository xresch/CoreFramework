
//CFW_WIDGET_CHECKLIST_EDIT_BUTTON = '<i class="fas fa-pen cursor-pointer ml-1" onclick="cfw_widget_checklist_triggerEdit(this)"></i>';
CFW_WIDGET_CHECKLIST_REMOVE_BUTTON = '<i class="fas fa-times text-cfw-red cursor-pointer ml-1" onclick="cfw_widget_checklist_checkboxChange(this, true)"></i>';

(function (){

	CFW.dashboard.registerWidget("cfw_checklist",
		{
			category: CFW.dashboard.global.categoryDefault,
			menuicon: "fas fa-tasks",
			menulabel: CFWL('cfw_widget_cfwchecklist', 'Checklist'),
			description: CFWL('cfw_widget_cfwchecklist_desc', 'Displays a checklist. Every line in the text will be shown as a separate item.'),
			
			createWidgetInstance: function (widgetObject, params, callback) {			
				
				//-------------------------
				// Prepare input
				var lines = "";
				if(widgetObject.JSON_SETTINGS.content != null){
					let content = widgetObject.JSON_SETTINGS.content;
					lines = content.trim().split(/\r\n|\r|\n/);
				}else{
					callback(widgetObject, '');
					return;
				}
				
				//-------------------------
				//Create List HTML
				var checkboxGroup = $('<div class="cfw-widget-checklist form-group w-100">');
				checkboxGroup.data('widgetObject', widgetObject);
				
				checkboxGroup.append('<button type="button" class="btn btn-xs btn-primary mb-2" onclick="cfw_widget_checklist_triggerAdd(this)"> <i class="fas fa-plus"></i> </button>');
				
			 	for(var i = 0; i < lines.length; i++){

			 		var value = lines[i].trim();

			 		var checkboxDiv = cfw_widget_checklist_createCheckboxElement(value, widgetObject.JSON_SETTINGS.strikethrough);
			 		checkboxGroup.append(checkboxDiv);
			 	}
			 				
				callback(widgetObject, checkboxGroup);
				
			},
						
		}
	);
})();

/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_createCheckboxElement(value, isStrikethrough, isChecked){
	
	var checkboxGUID = "checkbox-"+CFW.utils.randomString(8);
	var checked = "";
	var strikethrough = ''; 
	var button = ''; 
				
	if(value.toLowerCase().startsWith("x ")){
		value = value.slice(1);
		isChecked = true;
	}
	
	if(isChecked){
		value = value.slice(1);
		checked = 'checked="checked"';
		button = CFW_WIDGET_CHECKLIST_REMOVE_BUTTON;
		if(isStrikethrough){
			strikethrough = 'strikethrough-checkbox';
		}
	}
				
	var checkboxDiv = $(
		'<div class="form-check '+strikethrough+'">'
			+'<input class="form-input form-input-sm w-100 valuebox d-none" type="text"'
					+' onkeypress="cfw_widget_checklist_editKeyPress(event, this)"'
					+' onblur="cfw_widget_checklist_confirmEdit(this)"'
					+'>'
			+'<input class="form-check-input" type="checkbox" onchange="cfw_widget_checklist_checkboxChange(this, false)" id="'+checkboxGUID+'" '+checked+' >'
			+'<label class="form-check-label" ondblclick="cfw_widget_checklist_triggerEdit(this)"></label>'
			+ button
		+'</div>');
	checkboxDiv.find('.form-input').val(value);
	checkboxDiv.find('label').html(CFW.utils.urlToLink(value));
	
	return checkboxDiv;
}
	
/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_triggerAdd(buttonElement){

	var checklist = $(buttonElement).closest('.cfw-widget-checklist');
	var newCheckboxItem = cfw_widget_checklist_createCheckboxElement("");

	checklist.prepend(newCheckboxItem);
	
	var valuebox = newCheckboxItem.find('.valuebox');
	cfw_widget_checklist_triggerEdit(valuebox);
	
}

/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_triggerEdit(element){

	var parent = $(element).closest('.form-check');
	var checkbox = parent.find('.form-check-input');
	var label = parent.find('.form-check-label');
	var valuebox = parent.find('.valuebox');
	
	label.addClass("d-none");
	checkbox.addClass("d-none");
	
	valuebox.removeClass("d-none");
	valuebox.focus();
	
}

/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_editKeyPress(e){
		
	//---------------------------
	// Do nothing if not Enter
	if ( e.keyCode != 13) {
		return;
	}
	
	//---------------------------
	// Revert display and save
	
	var element = event.target || event.srcElement;
	$(element).blur();
	//cfw_widget_checklist_confirmEdit(element);
	
}

/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_confirmEdit(element){
	
	//---------------------------
	// Revert display and save

	var parent = $(element).closest('.form-check');
	var checkbox = parent.find('.form-check-input');
	var label = parent.find('.form-check-label');
	var valuebox = parent.find('.valuebox');
	
	label.html(CFW.utils.urlToLink(valuebox.val()));
	
	label.removeClass("d-none");
	checkbox.removeClass("d-none");
	
	valuebox.addClass("d-none");
	
	cfw_widget_checklist_checkboxChange(element, false);
	
}
	
/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_checkboxChange(element, doRemove){
	
	var parent = $(element).closest('.form-check');
	var checklistWrapper = $(element).closest('.cfw-widget-checklist');
	var widgetObject = $(checklistWrapper).data('widgetObject');
	var doSort = widgetObject.JSON_SETTINGS.doSort;
	var checkbox = parent.find('.form-check-input');
	var isChecked = checkbox.prop("checked");
	var group = checkbox.closest('.grid-stack-item');
	
	if(JSDATA.canEdit){

		var widgetObject = group.data('widgetObject');
		
		//---------------------
		// Handle Strikethrough
		if(isChecked){
			parent.find('label').after(CFW_WIDGET_CHECKLIST_REMOVE_BUTTON);
			if(widgetObject.JSON_SETTINGS.strikethrough){
				parent.addClass('strikethrough-checkbox');
			}
			
		}else{
			parent.removeClass('strikethrough-checkbox');
			parent.find('i').remove();
		}
		
		//---------------------
		// Handle Items
		var itemsUnchecked = '';
		var itemsChecked = '';
		group.find('.form-check').each(function(){
			var currentParent = $(this);
			var currentBox = currentParent.find('.form-check-input');
			var value = currentParent.find('.form-input').val();
			var checked = currentBox.is(':checked');
			
			if(doRemove && checkbox.attr('id') == currentBox.attr('id') ){
				return 1;
			}
			
			var checkedString = "";
			if (checked){	checkedString += 'X '; }
			
			//if doSort=false, all items will be put into itemsChecked variable
			if (checked || !doSort){
				itemsChecked += checkedString + value + "\r\n";
			}else{
				itemsUnchecked += checkedString + value + "\r\n";
			}
			
		});
		
		widgetObject.JSON_SETTINGS.content = itemsUnchecked + itemsChecked;
		 
		cfw_dashboard_widget_save_state(widgetObject, true); 
		cfw_dashboard_widget_rerender(widgetObject.guid, true);
	}else{
		checkbox.prop("checked", !isChecked);
		CFW.ui.addToastWarning('You don\'t have the required permissions to change this dashboard.');
	}

};

/**********************************************************************
 *
 **********************************************************************/
/*function cfw_widget_checklist_triggerRemove(buttonElement){
		
	var button = $(buttonElement);
	var parent = button.closest('.form-check');
	var checkboxToDelete = parent.find('input');
	var group = button.closest('.grid-stack-item');
	
	if(JSDATA.canEdit){

		var widgetObject = group.data('widgetObject');

		//---------------------
		// Handle Items
		var itemsUnchecked = '';
		var itemsChecked = '';
		group.find('input[type="checkbox"]').each(function(){
			var currentBox = $(this);
			var value = currentBox.attr('value');
			var checked = currentBox.is(':checked');
			
			if(checkboxToDelete.attr('id') == currentBox.attr('id') ){
				return 1;
			}
			
			if (checked){
				itemsChecked += 'X ' + value + "\r\n";
			}else{
				itemsUnchecked += value + "\r\n";
			}
			
		});
		
		widgetObject.JSON_SETTINGS.content = itemsUnchecked + itemsChecked;
		 
		cfw_dashboard_widget_save_state(widgetObject, true); 
		cfw_dashboard_widget_rerender(widgetObject.guid, true);
		
	}else{
		checkbox.prop("checked", !isChecked);
		CFW.ui.addToastWarning('You don\'t have the required permissions to change this dashboard.');
	}
}*/
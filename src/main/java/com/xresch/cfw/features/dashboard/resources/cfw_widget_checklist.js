
CFW_WIDGET_CHECKLIST_REMOVE_BUTTON = '<i class="fas fa-times text-cfw-red cursor-pointer" onclick="cfw_widget_checklist_triggerRemove(this)"></i>';

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
				}
				
				//-------------------------
				//Create List HTML
				var checkboxGroup = $('<div class="form-group">');
				checkboxGroup.data('widgetObject', widgetObject);
				
			 	for(var i = 0; i < lines.length; i++){
			 		var checkboxGUID = "checkbox-"+CFW.utils.randomString(16);
			 		var value = lines[i].trim();
			 		var checked = "";
			 		var strikethrough = ''; 
			 		var removeButton = ''; 

			 		if(value.toLowerCase().startsWith("x ")){
			 			value = value.slice(1);
			 			checked = 'checked="checked"';
			 			if(widgetObject.JSON_SETTINGS.strikethrough){
			 				strikethrough = 'strikethrough-checkbox';
							removeButton = CFW_WIDGET_CHECKLIST_REMOVE_BUTTON;
			 			}
			 		}
			 		var checkboxDiv = $(
			 			'<div class="form-check '+strikethrough+'">'
			 				+'<input class="form-check-input" type="checkbox" onchange="cfw_widget_checklist_checkboxChange(this)" id="'+checkboxGUID+'" '+checked+' >'
							+'<label class="form-check-label" for="'+checkboxGUID+'"></label>'
			 				+ removeButton
			 			+'</div>');
			 		checkboxDiv.find('input').val(value);
			 		checkboxDiv.find('label').html(CFW.utils.urlToLink(value));
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
function cfw_widget_checklist_checkboxChange(checkboxElement){
	var checkbox = $(checkboxElement);
	var isChecked = checkbox.prop("checked");
	var parent = checkbox.closest('.form-check');
	var group = checkbox.closest('.grid-stack-item');
	
	if(JSDATA.canEdit){

		var widgetObject = group.data('widgetObject');
		
		//---------------------
		// Handle Strikethrough
		if(isChecked && widgetObject.JSON_SETTINGS.strikethrough){
			parent.addClass('strikethrough-checkbox');
			parent.find('label').after(CFW_WIDGET_CHECKLIST_REMOVE_BUTTON);
		}else{
			parent.removeClass('strikethrough-checkbox');
			parent.find('i').remove();
		}
		
		//---------------------
		// Handle Strikethrough
		var newContent = '';
		group.find('input[type="checkbox"]').each(function(){
			var currentBox = $(this);
			var value = currentBox.attr('value');
			var checked = currentBox.is(':checked');
			
			if (checked){
				newContent += 'X '+value;
			}else{
				newContent += value;
			}
			newContent += "\r\n";
		});
		
		widgetObject.JSON_SETTINGS.content = newContent;
		 
		cfw_dashboard_widget_save_state(widgetObject, true); 
	}else{
		checkbox.prop("checked", !isChecked);
		CFW.ui.addToastWarning('You don\'t have the required permissions to change this dashboard.');
	}

};

/**********************************************************************
 *
 **********************************************************************/
function cfw_widget_checklist_triggerRemove(buttonElement){
	
	console.log('remove');
	
	var button = $(buttonElement);
	var parent = button.closest('.form-check');
	var checkboxToDelete = parent.find('input');
	var group = button.closest('.grid-stack-item');
	
	if(JSDATA.canEdit){

		var widgetObject = group.data('widgetObject');

		//---------------------
		// Handle Strikethrough
		var newContent = '';
		group.find('input[type="checkbox"]').each(function(){
			var currentBox = $(this);
			var value = currentBox.attr('value');
			var checked = currentBox.is(':checked');
			
			if(checkboxToDelete.attr('id') == currentBox.attr('id') ){
				return 1;
			}
			
			if (checked){
				newContent += 'X '+value;
			}else{
				newContent += value;
			}
			newContent += "\r\n";
		});
		
		widgetObject.JSON_SETTINGS.content = newContent;
		 
		cfw_dashboard_widget_save_state(widgetObject, true); 
		cfw_dashboard_widget_rerender(widgetObject.guid, true);
		
	}else{
		checkbox.prop("checked", !isChecked);
		CFW.ui.addToastWarning('You don\'t have the required permissions to change this dashboard.');
	}
}
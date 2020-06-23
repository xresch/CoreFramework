(function (){

	CFW.dashboard.registerWidget("cfw_checklist",
		{
			category: "Static Widgets",
			menuicon: "fas fa-tasks",
			menulabel: CFWL('cfw_widget_cfwchecklist', 'Checklist'),
			description: CFWL('cfw_widget_cfwchecklist_desc', 'Displays a checklist. Every line in the text will be shown as a separate item.'),
			
			createWidgetInstance: function (widgetObject, callback) {			
				
				//-------------------------
				// Prepare input
				var lines = "";
				if(widgetObject.JSON_SETTINGS.content != null){
					lines = widgetObject.JSON_SETTINGS.content.trim().split(/\r\n|\r|\n/);
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

			 		if(value.toLowerCase().startsWith("x")){
			 			value = value.slice(1);
			 			checked = 'checked="checked"';
			 			if(widgetObject.JSON_SETTINGS.strikethrough){
			 				strikethrough = 'strikethrough-checkbox';
			 			}
			 		}
			 		var checkboxHTML = 
			 			'<div class="form-check">'
			 				+'<input class="form-check-input '+strikethrough+'" type="checkbox" onchange="cfw_widget_checklist_checkboxChange(this)" value="'+value+'" id="'+checkboxGUID+'" '+checked+' >'
			 				+'<label class="form-check-label" for="'+checkboxGUID+'">'+value+'</label>'
			 			+'</div>';
			 		
			 		checkboxGroup.append(checkboxHTML);
			 	}
			 				
				callback(widgetObject, checkboxGroup);
				
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				var settingsForm = $(form);
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				//widgetObject.JSON_SETTINGS.content = settingsForm.find('textarea[name="content"]').val();
				//widgetObject.JSON_SETTINGS.strikethrough = ( settingsForm.find('input[name="strikethrough"]:checked').val() == "true" );
				return true;
			}
			
		}
	);
})();


function cfw_widget_checklist_checkboxChange(checkboxElement){
	var checkbox = $(checkboxElement);
	var isChecked = checkbox.prop("checked");
	var group = checkbox.closest('.form-group');
	
	if(JSDATA.canEdit){

		var widgetObject = group.data('widgetObject');
		
		//---------------------
		// Handle Strikethrough
		if(isChecked && widgetObject.JSON_SETTINGS.strikethrough){
			checkbox.addClass('strikethrough-checkbox');
		}else{
			checkbox.removeClass('strikethrough-checkbox');
		}
		
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
		 
		cfw_dashboard_saveWidgetState(widgetObject, true); 
	}else{
		checkbox.prop("checked", !isChecked);
		CFW.ui.addToastWarning('You don\'t have the required permissions to change this dashboard.');
	}

};
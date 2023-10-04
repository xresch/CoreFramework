
CFW_PARAMETER_SCOPE = "default";
CFW_PARAMETER_ID = -999;

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_setScope(CFW_PARAMETER_SCOPE, id){
	CFW_PARAMETER_SCOPE = CFW_PARAMETER_SCOPE;
	CFW_PARAMETER_ID = id;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_edit(){
	
	// ----------------------------
	// Create Content Div
	let contentDiv = $('<div>');
	contentDiv.append('<p>Parameters will substitute values in the widgets on the dashboard.</p>');

	// ----------------------------
	// Create Add Params Button
	let addParametersButton = 
		$('<button class="btn btn-sm btn-success mb-3"><i class="fas fa-plus-circle"></i>&nbsp;Add Parameters</button>')
				.click(cfw_parameter_showAddParametersModal);
	
	contentDiv.append(addParametersButton);		
	
	// ----------------------------
	// Create Param List Div
	let paramListDiv = $('<div id="param-list">');
	contentDiv.append(paramListDiv);
	
	CFW.ui.showModalLarge('Parameters', contentDiv);
	
    cfw_parameter_loadParameterForm();
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_save(){
	var paramListDiv = $('#param-list');
	var form = paramListDiv.find('form');
	var formID = form.attr('id');
	
	// paramListDiv.find('button').click();
	cfw_internal_postForm('/cfw/formhandler', '#'+formID, function(data){
		if(data.success){
			cfw_dashboard_draw(false, false);
		}
	});
	
}
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_loadParameterForm(){
	
	var paramListDiv = $('#param-list');
	var form = paramListDiv.find('form');
	var formID = form.attr('id');
	// --------------------------------
	// Trigger Save
	if(form.length == 0){
		loadForm();
	}else{
		// paramListDiv.find('button').click();
		cfw_internal_postForm('/cfw/formhandler', '#'+formID, function(data){
			
			// -------------------------------
			// Update if save was successfull
			if(data.success){
				loadForm();
			}
		});
	
	}
		
	function loadForm(){
		var paramListDiv = $('#param-list');
		paramListDiv.html('');
		
		CFW.http.createForm(CFW_DASHBOARDVIEW_URL, 
				{action: "fetch", item: "paramform", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, 
				paramListDiv, 
				function (formID){
					
					// ----------------------------
					// Replace Save Action
					paramListDiv.find('form > button').attr('onclick', 'cfw_parameter_save()');
					
					// ----------------------------
					// Add Header
					paramListDiv.find('form thead tr').append('<th>&nbsp</th>');
					
					// ----------------------------
					// Add Delete Buttons
					formRows = paramListDiv.find('form tbody tr');
					
					formRows.each(function (){
						row = $(this);
						
						row.append('<td><div class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+ 'onclick="cfw_parameter_removeConfirmed('+row.data('id')+');">'
							+ '<i class="fa fa-trash"></i>'
							+ '</div></td>')
					})
					
					// ----------------------------
					// Replace widgetType with Label
					formFirstColumn = paramListDiv.find('form tbody tr').find('td:first');
					formRows.each(function (index, element){
						let columnSpan = $(element).find('span:first');
						
						let widgetType = columnSpan.text();
						let definition = cfw_dashboard_getWidgetDefinition(widgetType);
						let label = (definition != undefined) ? definition.menulabel : undefined;
						
						if(label != undefined){
							columnSpan.text(label);
						}
					})
					
				}
			);
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_add(widgetType, widgetSetting, label){
	
	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: 'create', item: 'param', widgetType: widgetType, widgetSetting: widgetSetting, label: label, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){
		if(data.success){
			// Reload Form
			cfw_parameter_loadParameterForm();
		}
	});
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_removeConfirmed(parameterID){
	CFW.ui.confirmExecute('Do you really want to delete this parameter? (Cannot be undone)', 'Remove', "cfw_parameter_remove('"+parameterID+"')" );
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_remove(parameterID) {
	var formID = $('#param-list form').attr('id');
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'delete', item: 'param', paramid: parameterID, formid: formID, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){

			if(data.success){
				// Remove from Form
				$('#param-list tr[data-id="'+parameterID+'"]').remove();
				
			}
		}
	);

};

/*******************************************************************************
 * applies the parameters to the fields of the object.
 * This can either be a widgetObject.JSON_SETTINGS object, or an object containing
 * parameters for a http request(e.g. for autocomplete).
 *
 * @param object the object to apply the parameters too
 * @param finalParams the parameters to be applied
 * @param widgetType the type of dashboard widget or null if not applicable

 * @returns copy of the object with applied parameters
 ******************************************************************************/
function cfw_parameter_applyToFields(object, finalParams,  widgetType) {

	//###############################################################################
	//############################ IMPORTANT ########################################
	//###############################################################################
	// When changing this method you have to apply the same changes in the java 
	// method:
	// ServletDashboardView.java >> replaceParamsInSettings()
	//
	//###############################################################################

// FK_ID_DASHBOARD: 2081
// IS_MODE_CHANGE_ALLOWED: true
// MODE: "MODE_SUBSTITUTE"
// NAME: "param_name_VMUkHT"
// PARAM_TYPE: "SELECT"
// PK_ID: 164
// VALUE: "737"
// LABEL: "environment"
// WIDGET_TYPE: "emp_prometheus_range_chart"
	
	var settingsString = JSON.stringify(object);
	
	//=============================================
	// Handle SUBSTITUTE PARAMS
	//=============================================
	var globalOverrideParams = [];
	for(var index in finalParams){
		let currentParam = finalParams[index];
		let paramMode = currentParam.MODE;
		let currentSettingName = currentParam.LABEL;
		
		// ----------------------------------------
		// Handle Global Params
		if(currentParam.MODE === "MODE_GLOBAL_OVERRIDE"
		&& (widgetType == null || currentParam.WIDGET_TYPE === widgetType) ){
			globalOverrideParams.push(currentParam)
			continue;
		}
		
		// ----------------------------------------
		// Substitute Params
		let stringifiedValue = JSON.stringify(currentParam.VALUE);
		
		// remove
		if (typeof currentParam.VALUE == "string"){
			// reomve quotes
			stringifiedValue = stringifiedValue.substring(1, stringifiedValue.length-1)
		}
		
		settingsString = settingsString.replaceAll('$'+currentParam.NAME+'$', stringifiedValue);
		
	}
	
	//=============================================
	// Handle GLOBAL OVERRIDE PARAMS
	//=============================================
	var newSettingsObject = JSON.parse(settingsString);
	
	for(var index in globalOverrideParams){
		let currentParam = globalOverrideParams[index];
		let paramValue = currentParam.VALUE;
		let currentSettingName = currentParam.LABEL;

		switch(currentParam.PARAM_TYPE){
			case 'TAGS_SELECTOR':
			case 'CHART_SETTINGS':
				if(typeof paramValue == 'object'){
					newSettingsObject[currentSettingName] = paramValue;
				}else{
					newSettingsObject[currentSettingName] = JSON.parse(paramValue);
				}
				break;
			case 'BOOLEAN':  
				paramValue = paramValue.toLowerCase().trim();
				switch(paramValue){
		        	case "true": case "yes": case "1": newSettingsObject[currentSettingName] = true; break;
		        	case "false": case "no": case "0": newSettingsObject[currentSettingName] = false; break;
		        	default: newSettingsObject[currentSettingName] = Boolean(paramValue); break;
				}
				break;
			
			// TEXT, NUMBER, TEXTAREA, PASSWORD, EMAIL, SELECT, LIST
			default:
				// objects, numbers etc...
				newSettingsObject[currentSettingName] = paramValue;

				break;
		}
	}

	return newSettingsObject;
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_getUserParamsStoreKey(){
	return +CFW_PARAMETER_SCOPE+'['+CFW_PARAMETER_ID+'].userparams';
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_storeUserParams(params){
	var storekey = cfw_parameter_getUserParamsStoreKey();
	CFW.cache.storeValueForPage(storekey, JSON.stringify(params));
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_getStoredUserParams(){
	var storekey = cfw_parameter_getUserParamsStoreKey();
	
	var storedParamsString = CFW.cache.retrieveValueForPage(storekey);
	if(storedParamsString != undefined){
		var storedViewerParams = JSON.parse(storedParamsString);
	}else{
		var storedViewerParams = {};
	}
	
	return storedViewerParams;
}


/*******************************************************************************
 * Overrides default params with the values set by the Parameter Widgets and 
 * returns a clone of the object held by CFW_DASHBOARD_PARAMS. Also adds the
 * parameters earliest and latest with epoch time from the time picker.
 ******************************************************************************/
function cfw_parameter_getFinalParams(){
	
	var storedViewerParams = cfw_parameter_getStoredUserParams();
	var mergedParams = _.cloneDeep(CFW_DASHBOARD_PARAMS);
	
	//Add earliest and latest params
	//mergedParams.push({NAME: "earliest", VALUE: ""+CFW_DASHBOARD_TIME_EARLIEST_EPOCH});
	//mergedParams.push({NAME: "latest", VALUE: ""+CFW_DASHBOARD_TIME_LATEST_EPOCH});

		
	for(var index in mergedParams){
		
		var currentParam = mergedParams[index];
		var paramName = currentParam.NAME;
		var viewerCustomValue = storedViewerParams[paramName];
		
		//----------------------------------------
		// Remove value if dynamic
		// will use viewerCustomValue if available
		if(currentParam.IS_DYNAMIC){
			currentParam.VALUE = "";
		}
		
		if(!CFW.utils.isNullOrEmpty(viewerCustomValue)){
			
			//---------------------------------------------
			// Override with Custom Param
			currentParam.VALUE = viewerCustomValue;

			if(currentParam.PARAM_TYPE == 'TAGS_SELECTOR'){
				var tagsInputObject = JSON.parse(viewerCustomValue);
				currentParam.VALUE = tagsInputObject;
			}else if(typeof viewerCustomValue == "string"){
				currentParam.VALUE = viewerCustomValue;
			}else{
				currentParam.VALUE = JSON.stringify(viewerCustomValue);
			}
		}else{
			
			//---------------------------------------------
			// Use and prepare Default Param Values
			if(currentParam.PARAM_TYPE == 'TAGS_SELECTOR'){
					var tagsInputObject = JSON.parse(currentParam.VALUE);
					currentParam.VALUE = tagsInputObject;
			}else if( currentParam.PARAM_TYPE ="VALUE_LABEL"
				  &&  currentParam.VALUE != null 
				  &&  currentParam.VALUE.startsWith("{")){
				let valueLabelOptions = JSON.parse(currentParam.VALUE);
				let keys = Object.keys(valueLabelOptions);
				if(keys.length > 0){
					currentParam.VALUE = keys[0];
				}
			}
		}
	}
	
	return mergedParams;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_showAddParametersModal(){
	
	// ----------------------------
	// Create Content Div
	let contentDiv = $('<div>');
	
	// --------------------------------------
	// General
	
	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "availableparams", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
		
		let paramsArray = data.payload;
		
		// -----------------------------------
		// Table Renderer
		var rendererSettings = {
				data: paramsArray,
			 	idfield: null,
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['label'],
			 	titleformat: '{0}',
			 	visiblefields: ['widgetType', 'label'],
			 	labels: {
			 		widgetType: "Widget",
			 		label: "Setting",
			 	},
			 	customizers: {
			 		widgetType: 
			 			function(record, value) { 
				 			if(record.widgetType == null){
				 					return "&nbsp;";
				 			}else{
				 				return cfw_dashboard_getWidgetDefinition(record.widgetType).menulabel;
				 			} 
				 		},
			 	},
				actions: [
					function (record, id){
						let widgetType = (record.widgetType != null) ? "'"+record.widgetType+"'" : null;
						let widgetSetting = (record.widgetSetting != null) ? "'"+record.widgetSetting+"'" : null;
						let label = (record.label != null) ? "'"+record.label+"'" : null;
						return '<button class="btn btn-success btn-sm" alt="Delete" title="Add Param" onclick="cfw_parameter_add('+widgetType+', '+widgetSetting+', '+label+')">'
								+ '<i class="fa fa-plus-circle"></i>'
								+ '</button>';

					},
				],
				
				rendererSettings: {
					table: {narrow: true, filterable: true}
				},
			};
				
		var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
		
		contentDiv.append(renderResult);
		
		CFW.ui.showModalSmall('Add Parameters', contentDiv, "CFW.cache.clearCache();");
	});

}


